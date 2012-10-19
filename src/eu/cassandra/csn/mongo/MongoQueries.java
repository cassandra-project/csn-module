/*   
   Copyright 2011-2012 The Cassandra Consortium (cassandra-fp7.eu)


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.cassandra.csn.mongo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import eu.cassandra.csn.graph.InstallationInfo;
import eu.cassandra.csn.graph.MyNode;
import eu.cassandra.csn.gui.util.ConsumptionDetector;

public class MongoQueries {

	public static final int INSTALLATION_TYPE = 0;
	public static final int PERSON_TYPE = 1;
	public static final int AVERAGE_CONSUMPTION = 2;
	public static final int PEAK_CONSUMPTION = 3;
	public static final int KL_SIM = 4;
	public static final int KL_DISSIM = 5;

	public static String[][] getRuns(){
		DBCursor cursor = DBConn.getConn("test").getCollection("runs").find();

		String[][] data = new String[cursor.size()][3];
		int counter = 0;
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			if((Integer)obj.get("percentage") == 100) {
				String c = String.valueOf(counter);
				String id = "";

				id = obj.get("_id").toString();

				int instCounter = DBConn.getMongo().getDB(id).getCollection("installations").find().count();
				if(instCounter>limit)
					instCounter = limit;

				data[counter][0] = c;
				data[counter][1] = id;
				data[counter][2] = String.valueOf( instCounter);
				counter++;
			}
		}
		cursor.close();
		return data;
	}

	/**
	 * 
	 * @param numberOfNodes
	 * @return
	 */
	public static ConsumptionInfo calculateGraphStats(int numberOfNodes) {
		return calculateGraphStats(numberOfNodes,null);
	}

	/**
	 * 
	 * @param numberOfNodes
	 * @param inst_id
	 * @return
	 */
	public static ConsumptionInfo calculateGraphStats(int numberOfNodes,String inst_id) {
		double max = 0.0;
		double sum = 0.0;
		double avg = 0.0;


		DBCursor simParam = DBConn.getConn().getCollection("sim_param").find();
		int days = 0;
		while(simParam.hasNext()) {
			DBObject o = simParam.next();
			if(days == 0)
				days = Integer.parseInt(o.get("numberOfDays").toString());
			else 
				break;
		}
		simParam.close();

		DBCursor res2 ;
		if(inst_id == null) {
			res2 = DBConn.getConn().getCollection("inst_results").find(
					new BasicDBObject(),new BasicDBObject("p",1)).sort(new BasicDBObject( "p",-1)).limit(1);
		}else{
			res2 = DBConn.getConn().getCollection("inst_results").find(
					new BasicDBObject("inst_id",inst_id),new BasicDBObject("p",1)).sort(new BasicDBObject( "p",-1)).limit(1);
		}
		while(res2.hasNext()) {
			DBObject o = res2.next();
			max = Double.parseDouble(o.get("p").toString());
		}
		res2.close();

		//TODO: Change results to aggregated results, once they have been implemented
		if(numberOfNodes != 1) {
			avg = max/2.3 * days * 24;
			sum = avg * (double)numberOfNodes;
		}
		else {
			avg = max/2.3;
			sum = avg * days * 24 / 1000;
		}

		//Group
		//		BasicDBObject groupCmd = new BasicDBObject("ns","inst_results");
		//		groupCmd.append("$keyf", "");
		//		groupCmd.append("$reduce", "function(obj,prev){prev.y+=obj.p}");
		//		groupCmd.append("initial",  new BasicDBObject("y",0));
		//		DBObject o = DBConn.getConn().getCollection("inst_results").group(groupCmd);

		//Map Reduce
		//		String map = "function(){" +
		//				"emit(this.inst_id, {count: 1, sum: this.p});" +
		//				"};";
		//
		//		String reduce = "function( key , values ){" +
		//				"var n = { count: 0, sum: 0}; " +
		//				"for ( var i = 0; i < values.length; i ++ ) {" +
		//				"n.sum += values[i].sum;" +
		//				"n.count += values[i].count;" +
		//				"};" +
		//				"return n;" +
		//				"};";
		//
		//		MapReduceOutput out = DBConn.getConn().getCollection("inst_results").mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, null);
		//		for ( DBObject obj3 : out.results() ) {
		//		}

		//One by One
		//		DBCursor res = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject(), new BasicDBObject("p",1));
		//		while(res.hasNext()) {
		//			DBObject obj = res.next();
		//			double value = Double.parseDouble(obj.get("p").toString());
		//			sum += value;
		//			if(value > max)
		//				max = value;
		//		}
		//		avg = sum/(double)numberOfNodes;
		return new ConsumptionInfo(days,sum,avg,max);
	}

	/**
	 * 
	 * @param inst_id
	 * @return
	 */
	public static InstallationInfo getInstallationInfo(String inst_id) {
		InstallationInfo instInfo = null;
		HashMap<String,Vector<InstallationInfo>> m = getInstallations(MongoQueries.INSTALLATION_TYPE,inst_id);
		Iterator<String> iter =  m.keySet().iterator();
		while(iter.hasNext()) {
			String x = iter.next();
			Vector<InstallationInfo> isntVec= m.get(x);
			for(InstallationInfo inst : isntVec) {
				instInfo = inst;
			}
		}
		if(instInfo != null) {
			ConsumptionInfo consumptionInfo = calculateGraphStats(1,inst_id);
			instInfo.setAvgConsumption(consumptionInfo.getAvgComnsumption());
			instInfo.setPeakComsumption(consumptionInfo.getMaxComnsumption());
			instInfo.setTotalConsumption(consumptionInfo.getTotalComnsumption());
		}
		return instInfo;
	}


	/**
	 * 
	 * @param edgeType
	 * @return
	 */
	public static HashMap<String,Vector<InstallationInfo>> getInstallations(int edgeType){
		return getInstallations(edgeType,null);
	}

	/**
	 * 
	 * @param nodesDiscovered
	 * @param nodes
	 * @param max
	 * @return
	 */
	public static Vector<MyNode> getMaxMinInstallations(Vector<MyNode> nodesDiscovered, Iterator<MyNode> nodes, boolean max) {
		double mValue;
		if(max)
			mValue = Double.MIN_VALUE;
		else
			mValue = Double.MAX_VALUE;
		MyNode mNode = null;
		while(nodes.hasNext()) {
			MyNode n = nodes.next();
			DBCursor res = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject( "inst_id",n.getId())).sort(new BasicDBObject( "p",-1)).limit(1);		
			while(res.hasNext()) {
				Double value = Double.parseDouble(res.next().get("p").toString());
				if(max) {
					if(value >= mValue) {
						mValue = value;
						mNode = n;
					}
				}
				else {
					if(value <= mValue) {
						mValue = value;
						mNode = n;
					}
				}
			}
			res.close();
		}
		mNode.setBig(true);
		nodesDiscovered.add(mNode);
		return nodesDiscovered;
	}

	private static int limit = 50;

	/**
	 * 
	 * @param inst_id
	 * @return
	 */
	public static Double[] getInstallationResults(String inst_id) {
		DBCursor cursor = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject("inst_id",inst_id)).sort(new BasicDBObject("tick",1)).limit(5*1440);
		Vector<Double> d = new Vector<Double>();
		while(cursor.hasNext()) {
			d.add(Double.valueOf(cursor.next().get("p").toString()));
		}
		cursor.close();
		Double[] data = d.toArray(new Double[0]);
		return data;
	}

	/**
	 * 
	 * @param edgeType
	 * @return
	 */
	public static HashMap<String,Vector<InstallationInfo>> getInstallationsKL(int edgeType){
		HashMap<String,Vector<InstallationInfo>> installations = new HashMap<String,Vector<InstallationInfo>>();
		DBCursor cursor = DBConn.getConn().getCollection("installations").find().limit(limit);
		Vector<InstallationInfo> instInfos = new Vector<InstallationInfo>();
		List<DBObject> insts = cursor.toArray();
		double[][] dataForKL = new double[Math.min(insts.size(),limit)][];
		cursor.close();
		for(int i=0;i<Math.min(insts.size(),limit);i++) {
			DBObject obj = insts.get(i);
			String id = obj.get("_id").toString();
			String name = null;
			if(obj.containsField("name"))
				name = obj.get("name").toString();
			String instType = null;
			if(obj.containsField("type"))
				instType = obj.get("type").toString();
			InstallationInfo instInfo = new InstallationInfo(id,name,instType,null);
			instInfos.add(instInfo);
			DBCursor cursor2 = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject("inst_id",id)).sort(new BasicDBObject("tick",1));
			List<DBObject> res = cursor2.toArray();
			cursor2.close();
			double[] data1Inst = new double[res.size()];
			for(int j=0;j<Math.min(insts.size(),limit);j++) {
				data1Inst[j] = Double.parseDouble(res.get(j).get("p").toString());
			}
			dataForKL[i] = data1Inst;
		}

		double[][] results = ConsumptionDetector.estimateKLD(dataForKL);

		for(int i=0;i<instInfos.size();i++) {
			instInfos.get(i).setKlValuesWithOtherInsts(results[i]);
		}
		installations.put("ALL", instInfos);

		return installations;
	}

	/**
	 * 
	 * @param edgeType
	 * @param inst_id
	 * @return
	 */
	public static HashMap<String,Vector<InstallationInfo>> getInstallations(int edgeType, String inst_id){
		if(edgeType == KL_SIM || edgeType == KL_DISSIM) {
			return getInstallationsKL(edgeType);	
		}
		else {
			HashMap<String,Vector<InstallationInfo>> installations = new HashMap<String,Vector<InstallationInfo>>();
			if(DBConn.getConn() != null) {
				DBCursor cursor;
				if(inst_id == null)
					cursor = DBConn.getConn().getCollection("installations").find();
				else 
					cursor = DBConn.getConn().getCollection("installations").find(new BasicDBObject("_id",new ObjectId(inst_id)));
				int cc = 0;
				while(cursor.hasNext()) {
					cc++;
					if(cc ==30)
						break;
					DBObject obj = cursor.next();
					String id = obj.get("_id").toString();
					String name = null;
					if(obj.containsField("name"))
						name = obj.get("name").toString();
					String instType = null;
					if(obj.containsField("type"))
						instType = obj.get("type").toString();

					InstallationInfo instInfo = new InstallationInfo(id,name,instType,null);
					String keyType = null;
					if(edgeType == INSTALLATION_TYPE) {
						keyType = instType;
					}
					else if(edgeType == PERSON_TYPE) {
						DBObject person = DBConn.getConn().getCollection("persons").findOne(new BasicDBObject( "inst_id",id));
						String personType = null;
						if(person.containsField("type"))
							personType = person.get("type").toString();
						keyType = personType;
					}
					else if(edgeType == AVERAGE_CONSUMPTION) {
						DBCursor res = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject( "inst_id",id));
						int counter = 0;
						double avg = 0;
						while(res.hasNext()) {
							counter++;
							double p = Double.parseDouble(res.next().get("p").toString());
							avg += p;
						}
						res.close();
						avg /= counter;
						int avgConsumType = (int)(avg/10);
						keyType = String.valueOf(avgConsumType);
					}
					else if(edgeType == PEAK_CONSUMPTION) {
						DBCursor res = DBConn.getConn().getCollection("inst_results").find(new BasicDBObject( "inst_id",id)).sort(new BasicDBObject( "p",-1)).limit(1);
						double max = 0;
						while(res.hasNext()) {
							max = Double.parseDouble(res.next().get("p").toString());
						}
						res.close();
						int maxConsumType = (int)(max/10);
						keyType = String.valueOf(maxConsumType);
					}
					else {
						keyType = id;
					}
					if(!installations.containsKey(keyType)) {
						Vector<InstallationInfo> v = new Vector<InstallationInfo>();
						v.add(instInfo);
						installations.put(keyType,v);
					}
					else {
						Vector<InstallationInfo> v = installations.get(keyType); 
						v.add(instInfo);
						installations.put(keyType,v);
					}
				}
				cursor.close();
			}
			return installations;
		}
	}

	//	public static HashMap<String,Vector<String>> getInstallationEdgeInfo(){
	//		if(DBConn.getConn() != null) {
	//			DBCursor cursor = DBConn.getConn().getCollection("installations").find();
	//			int c = 0;
	//			while(cursor.hasNext()) {
	//				DBObject obj = cursor.next();
	//				String id = obj.get("_id").toString();
	//				String type = null;
	//				if(obj.containsField("type"))
	//					type = obj.get("type").toString();
	//			}
	//			cursor.close();
	//		}
	//		return null;
	//	}



}