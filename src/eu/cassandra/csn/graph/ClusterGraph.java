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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



import edu.uci.ics.jung.algorithms.cluster.BicomponentClusterer;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import eu.cassandra.csn.gui.CSN;
import eu.cassandra.csn.gui.Stats;

public class ClusterGraph {

	public static final int BetweennessClusterer = 0;
	public static final int BicomponentClusterer = 1;
	public static final int VoltageClusterer = 2;
	public static final int WeakComponentClusterer = 3;

	/**
	 * 
	 * @param clusterType
	 * @param clusterParam
	 */
	public static void clusterAndRecolor(int clusterType, int clusterParam) {
		if(clusterType == BicomponentClusterer) {
			biClusterer();
			CSN.getSlider().setEnabled(false);
			CSN.getSliderLabel().setEnabled(false);
			CSN.getSliderLabel().setText("Change the clustering options of the graph:");
		}
		else if(clusterType == VoltageClusterer) {
			if(!CSN.getSlider().isEnabled() || CSN.getSliderLabel().getText().contains("etweenness")) {
				CSN.getSlider().setEnabled(true);
				CSN.getSliderLabel().setEnabled(true);
				CSN.getSliderLabel().setText("Change the clustering options of the graph: Number of clusters for the VoltageClusterer to create");
				int max = CSN.getVisualizationViewer().getGraphLayout().getGraph().getVertexCount();
				CSN.getSlider().setMaximum(max);
				CSN.getSlider().setMinimum(1);
				CSN.getSlider().setValue(1);
				double divider = (max-1)/20;
				CSN.getSlider().setMajorTickSpacing((int)((max-1)/divider));
			}
			else {
				voClusterer(clusterParam);
			}
		}
		else if(clusterType == WeakComponentClusterer) { 
			weakClusterer();
			CSN.getSlider().setEnabled(false);
			CSN.getSliderLabel().setEnabled(false);
			CSN.getSliderLabel().setText("Change the clustering options of the graph:");
		}
		else {
			if(!CSN.getSlider().isEnabled()|| CSN.getSliderLabel().getText().contains("oltage")) {
				CSN.getSlider().setEnabled(true);
				CSN.getSliderLabel().setEnabled(true);
				CSN.getSliderLabel().setText("Change the clustering options of the graph: Number of removed edges from the EdgeBetweennessClusterer algorithm");
				int max = CSN.getVisualizationViewer().getGraphLayout().getGraph().getEdgeCount();
				CSN.getSlider().setMaximum(max);
				CSN.getSlider().setMinimum(0);
				CSN.getSlider().setValue(0);
			}
			else {
				edgeClusterer(clusterParam); 
			}
		}
	}

	/**
	 * 
	 */
	private static void biClusterer() {
		BicomponentClusterer<MyNode,MyLink> clusterer = new BicomponentClusterer<MyNode,MyLink>();
		UndirectedGraph<MyNode, MyLink> newG = new UndirectedSparseGraph<MyNode, MyLink>();
		Iterator<MyNode> iter = CSNGraph.getGraph().getVertices().iterator();
		while(iter.hasNext()) {
			newG.addVertex(iter.next());
		}
		Iterator<MyLink> iter2 = CSNGraph.getGraph().getEdges().iterator();
		while(iter2.hasNext()) {
			MyLink l = iter2.next();
			newG.addEdge(l,l.getNode1(),l.getNode2());
		}

		cluster( clusterer.transform(newG),null); 
	}

	/**
	 * 
	 */
	private static void weakClusterer() {
		WeakComponentClusterer<MyNode,MyLink> clusterer = new WeakComponentClusterer<MyNode,MyLink>();
		cluster( clusterer.transform(CSNGraph.getGraph()),null); 
	}

	/**
	 * 
	 * @param numberOfClusters
	 */
	private static void voClusterer(int numberOfClusters) {
		VoltageClusterer<MyNode,MyLink> clusterer = new VoltageClusterer<MyNode,MyLink>(CSNGraph.getGraph(),CSNGraph.getGraph().getVertexCount());
		Set<Set<MyNode>> set = new HashSet<Set<MyNode>>();
		set.addAll(clusterer.cluster(numberOfClusters));
		cluster( set,null); 
	}

	/**
	 * 
	 * @param numberOfEdgesToRemove
	 */
	private static void edgeClusterer(int numberOfEdgesToRemove) {
		EdgeBetweennessClusterer<MyNode,MyLink> clusterer = new EdgeBetweennessClusterer<MyNode,MyLink>(numberOfEdgesToRemove);
		cluster( clusterer.transform(CSNGraph.getGraph()),clusterer.getEdgesRemoved()); 
	}

	/**
	 * 
	 * @param clusterSet
	 * @param edges
	 */
	public static void cluster(Set<Set<MyNode>> clusterSet,List<MyLink> edges ) {
		int i = 0;
		for (Iterator<Set<MyNode>> cIt = clusterSet.iterator(); cIt.hasNext();) {
			Collection<MyNode> vertices = cIt.next();
			Color c =  CSN.similarColors[i %  CSN.similarColors.length];
			CSN.colorGraph(vertices, c);
			i++;
		}
		for (MyLink e : CSNGraph.getGraph().getEdges()) {
			if (edges == null || !edges.contains(e)) {
				CSN.getEdgePaints().put(e, Color.black);
			} else {
				CSN.getEdgePaints().put(e, Color.lightGray);
			}
		}
		CSN.getVisualizationViewer().repaint();
		CSN.setClusters(clusterSet);
		Stats.getTableModel().setValueAt((CSN.getClusters()==null?"":CSN.getClusters().size()) , 10, 1);
	}
}
