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
package eu.cassandra.csn;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.uci.ics.jung.graph.SparseMultigraph;
import eu.cassandra.csn.graph.MyLink;
import eu.cassandra.csn.graph.MyNode;
import eu.cassandra.csn.gui.CSN;
import eu.cassandra.csn.gui.Menu;
import eu.cassandra.csn.gui.Stats;
import eu.cassandra.csn.mongo.DBConn;

public class Main {

	public static JFrame MainFrame;

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		DBConn.initDBConn(null);

		MainFrame = new JFrame("Cassandra CSN Module");

		Menu.addMenu(MainFrame);
		JPanel statsPanel = Stats.setNetworkStats(MainFrame);
		JPanel graphPanel = CSN.setUpView();
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,graphPanel,statsPanel);

		MainFrame.getContentPane().add(splitPane);
		MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MainFrame.setSize(800,600);
		MainFrame.setExtendedState(MainFrame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		CSN.getVisualizationViewer().getGraphLayout().setGraph(SparseMultigraph.<MyNode,MyLink>getFactory().create());
		CSN.getVisualizationViewer().repaint();
		MainFrame.setVisible(true);

	}
}
