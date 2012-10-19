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
package eu.cassandra.csn.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;
import eu.cassandra.csn.graph.InstallationInfo;
import eu.cassandra.csn.graph.MyLink;
import eu.cassandra.csn.graph.MyNode;
import eu.cassandra.csn.gui.util.MyDefaultTableModel;
import eu.cassandra.csn.mongo.ConsumptionInfo;
import eu.cassandra.csn.mongo.MongoQueries;

public class Stats {

	/**
	 * 
	 * @param graph
	 */
	public static void updateNetworkStats(Graph<MyNode, MyLink> graph) {
		ConsumptionInfo consInfo = MongoQueries.calculateGraphStats(graph.getVertexCount());

		BetweennessCentrality<MyNode,MyLink> ranker = new BetweennessCentrality<MyNode,MyLink>(graph);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();

		int unconnectedVertices = 0;
		double vertexAvgBetweennessCentrality = 0.0;
		Iterator<MyNode> nodeIter = graph.getVertices().iterator();
		while(nodeIter.hasNext()) {
			int outLinksOfNode = 0;
			MyNode node = nodeIter.next();
			Iterator<MyLink> lIter = graph.getOutEdges(node).iterator();
			while(lIter.hasNext()) {
				MyLink link = lIter.next();
				if(CSN.getEdgePaints() == null || !CSN.getEdgePaints().containsKey(link)) {
					outLinksOfNode++;
					break;
				}
			}
			if(outLinksOfNode == 0)		
				unconnectedVertices++;

			vertexAvgBetweennessCentrality += ranker.getVertexRankScore(node);
		}
		vertexAvgBetweennessCentrality /= graph.getVertexCount();

		double edgeAvgBetweennessCentrality = 0.0;
		Iterator<MyLink> linkIter = graph.getEdges().iterator();
		while(linkIter.hasNext()) {
			MyLink node = linkIter.next();
			edgeAvgBetweennessCentrality +=ranker.getEdgeRankScore(node);
		}
		edgeAvgBetweennessCentrality /= graph.getEdgeCount();

		tableModel.setValueAt((consInfo.getDaysRun()!=0?consInfo.getDaysRun():""), 0, 1);
		tableModel.setValueAt(graph.getVertexCount(), 1, 1);
		tableModel.setValueAt(graph.getEdgeCount(), 2, 1);
		tableModel.setValueAt(DistanceStatistics.diameter(graph,
				new DijkstraShortestPath<MyNode, MyLink>(graph),true), 3, 1);
		tableModel.setValueAt(vertexAvgBetweennessCentrality, 4, 1);
		tableModel.setValueAt(edgeAvgBetweennessCentrality, 5, 1);
		tableModel.setValueAt(consInfo.getTotalComnsumption()/1000000 + " MWh", 6, 1);
		tableModel.setValueAt(consInfo.getAvgComnsumption()/1000000+ " MWh", 7, 1);
		tableModel.setValueAt(consInfo.getMaxComnsumption()/1000000+ " MW", 8, 1);
		tableModel.setValueAt(unconnectedVertices, 9, 1);
	}

	/**
	 * 
	 * @param instInfo
	 */
	public static void updateTableModelSelected(Vector<InstallationInfo> instInfos) {
		for(int i=tableModelSelected.getRowCount()-1;i>=0;i--) {
			tableModelSelected.removeRow(i);
		}
		for(int i=0;i<instInfos.size();i++) {
			InstallationInfo instInfo =	instInfos.get(i);
			String[] rowData = new String[5];
			rowData[0] = (instInfo.getInstallationName()!=null?instInfo.getInstallationName():"");
			rowData[1] = (instInfo.getInstallationType()!=null?instInfo.getInstallationType():"");
			rowData[2] = String.valueOf((instInfo.getTotalConsumption()!=0.0?instInfo.getTotalConsumption():"")) + " kWh";
			rowData[3] = String.valueOf((instInfo.getPeakComsumption()!=0.0?instInfo.getPeakComsumption():"") + " W");
			rowData[4] = String.valueOf((instInfo.getAvgConsumption()!=0.0?instInfo.getAvgConsumption():"") + " W");
			tableModelSelected.addRow(rowData);
		}
		Double[] data;
		if(instInfos.size()==1) {
			String inst_id = instInfos.get(0).getID();
			data = MongoQueries.getInstallationResults(inst_id);
		}
		else {
			data = new Double[0];
		}
		XYSeries series1 = new XYSeries("First");
		for (int i = 0; i < data.length/15; i++) {
			double d = 0;
			for(int j=0;j<60;j++) {
				d += data[j*60 + i];
			}
			series1.add(i, d);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		chartPanel.getChart().getXYPlot().setDataset(dataset);
	}


	private static MyDefaultTableModel tableModel;
	private static MyDefaultTableModel tableModelSelected;

	/**
	 * 
	 * @return
	 */
	public static MyDefaultTableModel getTableModelSelected() {
		return tableModelSelected;
	}

	/**
	 * 
	 * @return
	 */
	public static MyDefaultTableModel getTableModel() {
		return tableModel;
	}

	public static ChartPanel chartPanel;

	/**
	 * 
	 * @param frame
	 */
	public static void setNetworkStats(JFrame frame) {
		String[][] data = new String[][] {
				{"Virtual Days:",""},
				{"Number of nodes:",""},
				{"Number of edges:",""},
				{"Graph diameter:",""},
				{"Vertex Betweenness Centrality:",""},
				{"Edge Betweenness Centrality:",""},
				{"Total consumption:",""},
				{"Average consumption:",""},
				{"Peak consumption:",""},
				{"Unconnected vertices:",""},
				{"Clusters:",""}
		}; 
		String[] columnName = new String[] {"Metric","Value"}; 

		tableModel = new MyDefaultTableModel(data,columnName);
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		String[][] dataSelected = new String[5][];
		String[] columnNameSelected = new String[] {"Name","Type","Total Consumption","Peak Comsumption","Avg Consumption"}; 
		tableModelSelected = new MyDefaultTableModel(dataSelected,columnNameSelected);
		JTable tableSelected = new JTable(tableModelSelected);
		JScrollPane scrollPaneSelected = new JScrollPane(tableSelected);
		tableSelected.setFillsViewportHeight(true);
		tableSelected.setPreferredSize(new Dimension(1600,100));
		scrollPaneSelected.setPreferredSize(new Dimension(1600,100));



		JPanel statsPanel = new JPanel(new BorderLayout());
		statsPanel.add(scrollPane,BorderLayout.CENTER);
		chartPanel = Charts.createGraph("Power Consumption", "Hours", "Power (W)", new Double[0]);

		statsPanel.add(chartPanel,BorderLayout.PAGE_END);

		frame.add(statsPanel,BorderLayout.EAST);
		frame.add(scrollPaneSelected,BorderLayout.PAGE_END);

	}
}
