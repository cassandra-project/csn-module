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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import eu.cassandra.csn.gui.CSN;
import eu.cassandra.csn.mongo.MongoQueries;

public class CSNAnalysis {

	/**
	 * 
	 */
	private static void resetColor() {
		Iterator<MyNode> iter = CSNGraph.getGraph().getVertices().iterator();
		while(iter.hasNext()) {
			iter.next().setBig(false);
		}
	}

	/**
	 * 
	 */
	public static void findInfluencers() {
		CSNAnalysis.resetColor();
		Collection<MyNode> vertices = new Vector<MyNode>();

		if(CSN.getClusters() == null) { 
			vertices = CSNAnalysis.findInfluencers(vertices,CSNGraph.getGraph().getVertices().iterator());
		}
		else {
			Iterator<Set<MyNode>> setIter = CSN.getClusters().iterator();
			while(setIter.hasNext()) {
				Set<MyNode> set = setIter.next();
				vertices = CSNAnalysis.findInfluencers(vertices,set.iterator());
			}
		}
		//		CSN.colorGraph(vertices, new Color(255,0,0));
		CSN.getVisualizationViewer().repaint();
	}

	/**
	 * 
	 * @param vertices
	 * @param nodes
	 * @return
	 */
	private static Collection<MyNode> findInfluencers(Collection<MyNode> vertices,Iterator<MyNode> nodes){
		MyNode nodeToColor = null;
		int max = -1;
		while(nodes.hasNext()) {
			MyNode n = nodes.next();
			int degree = CSNGraph.getGraph().outDegree(n);
			if(degree > max) {
				max = degree;
				nodeToColor = n;
			}
		}
		vertices.add(nodeToColor);
		nodeToColor.setBig(true);
		return vertices;
	}

	/**
	 * 
	 */
	public static void findMaxConsumption() {
		findMinMaxConsumption(true);
	}

	/**
	 * 
	 */
	public static void findMinConsumption() {
		findMinMaxConsumption(false);
	}

	/**
	 * 
	 * @param isMax
	 */
	public static void findMinMaxConsumption(boolean isMax) {
		CSNAnalysis.resetColor();
		Vector<MyNode> vertices = new Vector<MyNode>();

		if(CSN.getClusters() == null) { 
			vertices =  MongoQueries.getMaxMinInstallations(vertices,CSNGraph.getGraph().getVertices().iterator(),isMax);
		}
		else {
			Iterator<Set<MyNode>> setIter = CSN.getClusters().iterator();
			while(setIter.hasNext()) {
				Set<MyNode> set = setIter.next();
				vertices =  MongoQueries.getMaxMinInstallations(vertices,set.iterator() ,isMax);
			}
		}
		//		CSN.colorGraph(vertices, new Color(255,0,0));
		CSN.getVisualizationViewer().repaint();
	}
}
