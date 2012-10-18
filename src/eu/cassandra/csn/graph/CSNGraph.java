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
package eu.cassandra.csn.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JLabel;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.cassandra.csn.gui.CSN;
import eu.cassandra.csn.gui.Menu;
import eu.cassandra.csn.gui.Stats;
import eu.cassandra.csn.mongo.MongoQueries;

public class CSNGraph {

	/**
	 * 
	 * @param initGraph
	 * @param edgeType
	 */
	public static void resetGraph(boolean initGraph,int edgeType) {
		if(initGraph)
			initGraph(edgeType);
		else {
			Graph<MyNode, MyLink> graph = SparseMultigraph.<MyNode,MyLink>getFactory().create();
			CSN.getVisualizationViewer().getGraphLayout().setGraph(graph);			
		}
		CSN.getVisualizationViewer().repaint();
		CSN.setClusters(null);
		CSN.getSlider().setEnabled(false);
		CSN.getSliderLabel().setEnabled(false);
		CSN.getSliderLabel().setText("Change the clustering options of the graph:");

		Color c =  CSN.similarColors[0 %  CSN.similarColors.length];
		CSN.colorGraph(CSNGraph.getGraph().getVertices(), c);
		for (MyLink e : CSNGraph.getGraph().getEdges()) {
			CSN.getEdgePaints().put(e, Color.black);
		}

		Double[] data = new Double[0];
		XYSeries series1 = new XYSeries("First");
		for (int i = 0; i < data.length; i++) {
			series1.add(i, data[i]);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		Stats.chartPanel.getChart().getXYPlot().setDataset(dataset);
	}

	/**
	 * 
	 * @param edgeType
	 */
	private static void initGraph(int edgeType) {
		initGraph(edgeType, 0.5); 
	}


	private static HashMap<String,MyNode> vertexMap = new HashMap<String, MyNode>();
	private static HashMap<String,Vector<InstallationInfo>>  installations;

	/**
	 * 
	 * @param edgeType
	 * @param klThreshold
	 */
	private static void initGraph(int edgeType, double klThreshold) {
		CSN.setClusters(null);
		CSNGraph.edgeType = edgeType;
		if(edgeType > Integer.MIN_VALUE) {
			Graph<MyNode, MyLink> graph = SparseMultigraph.<MyNode,MyLink>getFactory().create();
			installations = MongoQueries.getInstallations(edgeType);
			for(Vector<InstallationInfo> v :installations.values()) {
				for(InstallationInfo instInfo : v) {
					MyNode node = new MyNode(instInfo.getID(),instInfo.getInstallationName());
					graph.addVertex(node);
					vertexMap.put(instInfo.getID(), node);
				}
			}
			if(edgeType != MongoQueries.KL_SIM && edgeType != MongoQueries.KL_DISSIM) {
				CSN.getSliderEdgeLabel().setEnabled(false);
				CSN.getEdgeSlider().setEnabled(false);
				for(Vector<InstallationInfo> v :installations.values()) {
					for(int i=0;i<v.size();i++) {
						for(int j=i+1;j<v.size();j++) {
							MyLink link = new MyLink(v.get(i) + "_" + v.get(j) , 1, vertexMap.get(v.get(i).getID())  , vertexMap.get(v.get(j).getID()));
							graph.addEdge(link,vertexMap.get(v.get(i).getID()),vertexMap.get(v.get(j).getID()));
						}
					}
				}
			}
			else {
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;

				Vector<InstallationInfo> insts = installations.get("ALL"); 
				for(int i=0;i<insts.size();i++) {
					InstallationInfo inst = insts.get(i);
					double[] sim = inst.getKlValuesWithOtherInsts();
					for(int j=0;j<sim.length;j++) {
						if(sim[j] > max)
							max = sim[j];
						if(sim[j] < min)
							min = sim[j];
					}
				}
				double interval = (max - min)/10;
				Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
				for(int i=0;i<=10;i++) {
					labelTable.put( new Integer(i), new JLabel(String.format("%.4g%n",interval*i)));
				}

				CSN.getEdgeSlider().setLabelTable( labelTable );
				CSN.getEdgeSlider().setMaximum(labelTable.size()-1);
				CSN.getSliderEdgeLabel().setEnabled(true);
				CSN.getEdgeSlider().setEnabled(true);
				CSN.getEdgeSlider().setValue(0);
				addEdgesBasedOnKL(0.0);
			}
			CSN.getVisualizationViewer().getGraphLayout().setGraph(graph);	
			Stats.updateNetworkStats(graph); 
		}
		Menu.setKKLayoutSelected();
	}

	public static int edgeType = -1;

	/**
	 * 
	 * @param edgeType
	 * @param klThreshold
	 */
	public static void addEdgesBasedOnKL(double klThreshold) {
		Graph<MyNode, MyLink> graph = CSNGraph.getGraph();
		MyLink[] links = graph.getEdges().toArray(new MyLink[0]);
		for(int i=0;i<links.length;i++) {
			graph.removeEdge(links[i]);
		}
		Vector<InstallationInfo> insts = installations.get("ALL"); 
		for(int i=0;i<insts.size();i++) {
			InstallationInfo inst = insts.get(i);
			double[] sim = inst.getKlValuesWithOtherInsts();
			for(int j=i+1;j<sim.length;j++) {
				if(edgeType == MongoQueries.KL_SIM) {
					if(sim[j]>=klThreshold) {
						MyLink link = new MyLink(inst.getID() + "_" + insts.get(j).getID(), sim[j], vertexMap.get(inst.getID())  , vertexMap.get( insts.get(j).getID()));
						graph.addEdge(link,vertexMap.get( inst.getID()),vertexMap.get( insts.get(j).getID()));
					}
				}
				else if(sim[j]<klThreshold) {
					MyLink link = new MyLink(inst.getID() + "_" + insts.get(j).getID(), sim[j], vertexMap.get(inst.getID())  , vertexMap.get( insts.get(j).getID()));
					graph.addEdge(link,vertexMap.get( inst.getID()),vertexMap.get( insts.get(j).getID()));
				}
			}
		}
		for (MyLink e : CSNGraph.getGraph().getEdges()) {
			CSN.getEdgePaints().put(e, Color.black);
		}
		CSN.getVisualizationViewer().getGraphLayout().setGraph(graph);
		CSN.getVisualizationViewer().repaint();
		Stats.updateNetworkStats(graph); 
	}

	/**
	 * 
	 * @return
	 */
	public static Graph<MyNode, MyLink> getGraph(){
		return CSN.getVisualizationViewer().getGraphLayout().getGraph();
	}
}
