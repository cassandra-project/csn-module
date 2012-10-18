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

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import eu.cassandra.csn.graph.CSNAnalysis;
import eu.cassandra.csn.graph.CSNGraph;
import eu.cassandra.csn.graph.ClusterGraph;
import eu.cassandra.csn.graph.MyLink;
import eu.cassandra.csn.graph.MyNode;
import eu.cassandra.csn.graph.SaveGraph;
import eu.cassandra.csn.gui.panels.ConnectToMongoPanel;

public class Menu {

	private static File f = null; 
	private static JMenuItem save = new JMenuItem("Save Pajek File");
	private static JCheckBoxMenuItem kk = new JCheckBoxMenuItem("KK Layout");
	private static JCheckBoxMenuItem circle = new JCheckBoxMenuItem("Circle Layout");
	private static JCheckBoxMenuItem fr = new JCheckBoxMenuItem("FR Layout");
	private static JCheckBoxMenuItem fr2 = new JCheckBoxMenuItem("FR Layout2");
	private static JCheckBoxMenuItem spring = new JCheckBoxMenuItem("Spring Layout");
	private static JCheckBoxMenuItem isoMLayout = new JCheckBoxMenuItem("ISO M Layout");
	private static JMenu arrange;

	/**
	 * 
	 * @param g
	 * @return
	 */
	public static Layout<MyNode,MyLink> getSelectedLayout(Graph<MyNode,MyLink> g) {
		Layout<MyNode,MyLink> layout;
		if(circle.isSelected()) 
			layout = new CircleLayout<MyNode,MyLink>(g);
		else if(fr.isSelected()) 
			layout = new FRLayout2<MyNode,MyLink>(g);
		else if(fr2.isSelected()) 
			layout = new FRLayout2<MyNode,MyLink>(g);
		else if(spring.isSelected()) 
			layout = new SpringLayout<MyNode,MyLink>(g);
		else if(isoMLayout.isSelected()) {
			layout = new ISOMLayout<MyNode,MyLink>(g);
		}
		else
			layout = new KKLayout<MyNode,MyLink>(g);
		return layout;
	}

	/**
	 * 
	 */
	public static void setKKLayoutSelected() {
		for(int i=0;i<arrange.getItemCount();i++) {
			if(arrange.getItem(i).isSelected()) {
				arrange.getItem(i).setSelected(true);
				return;
			}
		}
		kk.setSelected(true);
		CSN.rearrange(CSN.KKLAYOUT);	
	}

	/**
	 * 
	 * @param frame
	 */
	public static void addMenu(final JFrame frame) {
		JMenuBar menuBar = new JMenuBar();

		//File
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menuBar.add(file);

		JMenuItem newM = new JMenuItem("New",KeyEvent.VK_T);
		newM.setIcon(new ImageIcon("images/new.png"));
		file.add(newM);
		newM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				CSNGraph.resetGraph(false,-1);
				CSN.getEdgeSlider().setEnabled(false);
			}
		});  

		JMenuItem open = new JMenuItem("Open");
		open.setIcon(new ImageIcon("images/open.png"));
		file.add(open);
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				new ConnectToMongoPanel().showConnectToMongoPanel();
			}
		});  

		save.setEnabled(false);
		save.setIcon(new ImageIcon("images/save2.png"));
		file.add(save);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(f == null) {
					JFileChooser jfc = new JFileChooser();
					int result = jfc.showSaveDialog(frame);
					if (result == JFileChooser.CANCEL_OPTION)
						return;
					f = jfc.getSelectedFile();
				}
				SaveGraph.writeCSV(f);
			}});


		JMenuItem saveAs = new JMenuItem("Save As Pajek File");
		saveAs.setIcon(new ImageIcon("images/save_Project.png"));
		file.add(saveAs);
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				int result = jfc.showSaveDialog(frame);
				if (result == JFileChooser.CANCEL_OPTION)
					return;
				f = jfc.getSelectedFile();
				SaveGraph.writeCSV(f);
				save.setEnabled(true);
			}});

		JMenuItem exportJPG = new JMenuItem("Export Image");
		exportJPG.setIcon(new ImageIcon("images/save.png"));
		file.add(exportJPG);
		exportJPG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				int result = jfc.showSaveDialog(frame);
				if (result == JFileChooser.CANCEL_OPTION)
					return;
				SaveGraph.writeJPG(jfc.getSelectedFile());
			}});

		JMenuItem quit = new JMenuItem("Quit");
		quit.setIcon(new ImageIcon("images/exit.png"));
		file.add(quit);
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		}); 

		//Edit
		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(edit);

		//		JMenuItem copy = new JMenuItem("Copy");;
		//		copy.setIcon(new ImageIcon("images/middle.gif"));
		//		edit.add(copy);

		ButtonGroup editNetworkGroup = new ButtonGroup();

		JMenu editNetwork = new JMenu("Edit Network");;
		editNetwork.setIcon(new ImageIcon("images/edit.png"));
		edit.add(editNetwork);

		JCheckBoxMenuItem transforming = new JCheckBoxMenuItem("Transform Graph");
		editNetworkGroup.add(transforming);
		transforming.setSelected(true);
		transforming.setIcon(new ImageIcon("images/middle.gif"));
		editNetwork.add(transforming);
		transforming.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSN.setModalGraphMouse(0);
			}});

		JCheckBoxMenuItem picking = new JCheckBoxMenuItem("Pick Node");
		editNetworkGroup.add(picking);
		//picking.setIcon(new ImageIcon("images/edit.png"));
		editNetwork.add(picking);
		picking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSN.setModalGraphMouse(1);
			}});

		//		JCheckBoxMenuItem annotate = new JCheckBoxMenuItem("Annotate");
		//		editNetworkGroup.add(annotate);
		//		annotate.setIcon(new ImageIcon("images/middle.gif"));
		//		editNetwork.add(annotate);
		//		annotate.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent arg0) {
		//				CSN.setModalGraphMouse(2);
		//			}});
		//
		//		JCheckBoxMenuItem editGraph = new JCheckBoxMenuItem("Edit");
		//		editNetworkGroup.add(editGraph);
		//		editGraph.setIcon(new ImageIcon("images/middle.gif"));
		//		editNetwork.add(editGraph);
		//		editGraph.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent arg0) {
		//				CSN.setModalGraphMouse(3);
		//			}});

		TransferActionListener transferActionListener = new TransferActionListener();
		JMenuItem copy = new JMenuItem("Copy");
		copy.setIcon(new ImageIcon("images/copy.png"));
		copy.setActionCommand((String)TransferHandler.getCopyAction().
				getValue(Action.NAME));
		copy.addActionListener(transferActionListener);
		copy.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		copy.setMnemonic(KeyEvent.VK_T);
		edit.add(copy);

		JMenuItem cut = new JMenuItem("Cut");
		cut.setIcon(new ImageIcon("images/cut.png"));
		cut.setActionCommand((String)TransferHandler.getCutAction().
				getValue(Action.NAME));
		cut.addActionListener(transferActionListener);
		cut.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		cut.setMnemonic(KeyEvent.VK_T);
		edit.add(cut);

		JMenuItem paste = new JMenuItem("Paste");
		paste.setIcon(new ImageIcon("images/paste.png"));
		paste.setActionCommand((String)TransferHandler.getPasteAction().
				getValue(Action.NAME));
		paste.addActionListener(transferActionListener);
		paste.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		paste.setMnemonic(KeyEvent.VK_T);
		edit.add(paste);

		//Arrange
		ButtonGroup group = new ButtonGroup();

		arrange = new JMenu("Arrange");
		menuBar.add(arrange);
		group.add(circle);
		circle.setIcon(new ImageIcon("images/blueBall.png"));
		arrange.add(circle);
		circle.addActionListener(new ChangeLayoutActionListener());
		circle.setSelected(true);

		group.add(fr);
		fr.setIcon(new ImageIcon("images/redBall.png"));
		arrange.add(fr);
		fr.addActionListener(new ChangeLayoutActionListener());

		group.add(fr2);
		fr2.setIcon(new ImageIcon("images/uri.png"));
		arrange.add(fr2);
		fr2.addActionListener(new ChangeLayoutActionListener());

		group.add(isoMLayout);
		isoMLayout.setIcon(new ImageIcon("images/blueSphere.png"));
		arrange.add(isoMLayout);
		isoMLayout.addActionListener(new ChangeLayoutActionListener());


		group.add(kk);
		kk.setIcon(new ImageIcon("images/yellowSphere.png"));
		arrange.add(kk);
		kk.addActionListener(new ChangeLayoutActionListener());

		group.add(spring);
		spring.setIcon(new ImageIcon("images/refresh.png"));
		arrange.add(spring);
		spring.addActionListener(new ChangeLayoutActionListener());

		//CSN
		JMenu csnAnalysis = new JMenu("CSN Analysis");
		csnAnalysis.setMnemonic(KeyEvent.VK_E);
		menuBar.add(csnAnalysis);

		JMenu cluster = new JMenu("Cluster");
		cluster.setIcon(new ImageIcon("images/createOnt.png"));
		csnAnalysis.add(cluster);

		JMenuItem cluster1 = new JMenuItem("EdgeBetweennessClusterer");;
		cluster1.setIcon(new ImageIcon("images/middle.gif"));
		cluster1.setToolTipText("<html>An algorithm for computing clusters (community structure) <br>" +
				"in graphs based on edge betweenness. The betweenness of an edge is defined as <br>" +
				"the extent to which that edge lies along shortest paths between all pairs of nodes. <br>" +
				"This algorithm works by iteratively following the 2 step process: <br>" + 
				"->Compute edge betweenness for all edges in current graph <br>" + 
				"->Remove edge with highest betweenness</html>");
		cluster.add(cluster1);
		cluster1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClusterGraph.clusterAndRecolor(ClusterGraph.BetweennessClusterer,0);
			}});

		JMenuItem cluster2 = new JMenuItem("BicomponentClusterer");;
		cluster2.setIcon(new ImageIcon("images/middle.gif"));
		cluster2.setToolTipText("<html>Finds all biconnected components (bicomponents) of an undirected graph. <br>" +
				"A graph is a biconnected component if at least 2 vertices must be removed in order <br>" +
				"to disconnect the graph. (Graphs consisting of one vertex, or of two connected vertices, <br>" +
				"are also biconnected.) Biconnected components of three or more vertices have the property <br>" +
				"that every pair of vertices in the component are connected by two or more vertex-disjoint <br>" +
				"paths.</html> ");
		cluster.add(cluster2);
		cluster2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClusterGraph.clusterAndRecolor(ClusterGraph.BicomponentClusterer,-1);
			}});

		JMenuItem cluster3 = new JMenuItem("VoltageClusterer");;
		cluster3.setIcon(new ImageIcon("images/middle.gif"));
		cluster3.setToolTipText("<html>Clusters vertices of a Graph based on their ranks as calculated by VoltageScorer.<br>" +
				"The algorithm proceeds as follows: <br>" + 
				"->first, generate a set of candidate clusters as follows: <br>" + 
				"   ->pick (widely separated) vertex pair, run VoltageScorer <br>" + 
				"  ->group the vertices in two clusters according to their voltages <br>" + 
				"  ->store resulting candidate clusters  <br>" + 
				"->second, generate k-1 clusters as follows: <br>" + 
				"  ->pick a vertex v as a cluster 'seed' <br>" + 
				"  ->calculate co-occurrence over all candidate clusters of v with each other vertex <br>" + 
				"  ->separate co-occurrence counts into high/low; high vertices constitute a cluster <br>" + 
				"  ->remove v's vertices from candidate clusters; continue  <br>" + 
				"->finally, remaining unassigned vertices are assigned to the kth (\"garbage\") cluster. <br></html>"); 
		cluster.add(cluster3);
		cluster3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClusterGraph.clusterAndRecolor(ClusterGraph.VoltageClusterer,1);
			}
		});

		JMenuItem cluster4 = new JMenuItem("WeakComponentClusterer");;
		cluster4.setIcon(new ImageIcon("images/middle.gif"));
		cluster4.setToolTipText("<html>Finds all weak components in a graph as sets of vertex sets. <br>" +
				"A weak component is defined as a maximal subgraph in which all pairs of vertices <br>" +
				"in the subgraph are reachable from one another in the underlying undirected subgraph. <br></html>");
		cluster.add(cluster4);
		cluster4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClusterGraph.clusterAndRecolor(ClusterGraph.WeakComponentClusterer,-1);
			}
		});

		JMenuItem groupGraph = new JMenuItem("Group Clusters");;
		groupGraph.setIcon(new ImageIcon("images/group.png"));
		csnAnalysis.add(groupGraph);
		groupGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSN.groupCluster();
			}
		});

		JMenu discover = new JMenu("Discover");;
		discover.setIcon(new ImageIcon("images/discover.png"));
		csnAnalysis.add(discover);

		JMenuItem influencers = new JMenuItem("Influencers");;
		influencers.setIcon(new ImageIcon("images/middle.gif"));
		discover.add(influencers);
		influencers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSNAnalysis.findInfluencers();
			}
		});

		JMenuItem maxConsumption = new JMenuItem("Max Peak Consumption");;
		maxConsumption.setIcon(new ImageIcon("images/middle.gif"));
		discover.add(maxConsumption);
		maxConsumption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSNAnalysis.findMaxConsumption();
			}
		});

		JMenuItem minConsumption = new JMenuItem("Min Peak Consumption");;
		minConsumption.setIcon(new ImageIcon("images/middle.gif"));
		discover.add(minConsumption);
		minConsumption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CSNAnalysis.findMinConsumption();
			}
		});

		
		JMenuItem more = new JMenuItem("More...");;
		more.setIcon(new ImageIcon("images/middle.gif"));
		discover.add(more);


		//About
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		menuBar.add(help);

		JMenuItem helpM = new JMenuItem("Help");;
		helpM.setIcon(new ImageIcon("images/help.png"));
		help.add(helpM);	
		helpM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame,
						"<html>For help please visit <a href=\"http://www.cassandra-fp7.eu\">http://www.cassandra-fp7.eu/</a></html>",
						"Help",  JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JMenuItem about = new JMenuItem("About");;
		about.setIcon(new ImageIcon("images/about.png"));
		help.add(about);
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame,"" +
						"<html>" +
						"Energy markets have undergone fundamental changes at the conceptual level<br>" +
						"over the last years. The necessity for sustainability has transformed the<br>" +
						"traditional power production scheme to a distributed energy resource one,<br>" +
						"while the future points towards a great number of decentralized, small-scale<br>" +
						"production sites based on renewable energy sources, also. Project CASSANDRA<br>" +
						"aims to build a platform for the realistic modeling of the energy market stake-<br>" +
						"holders, also involving small-scale consumers. CASSANDRA will provide users <br>" +
						"with the ability to test and benchmark working scenarios that can affect system<br>" +
						"operation and company/environmental policies at different levels of abstraction,<br>" +
						"starting from a basic level (single consumer) and shifting up to large consumer <br>" +
						"areas (i.e. a city). The project main outcomes will be the aggregation methodology<br>" +
						"and the framework of key performance indicators for scenario assessment, as well<br>" +
						"as an expandable software platform that providing different energy stakeholders <br>" +
						"with the ability to model the energy market, in order to assess scenarios for their<br>" +
						"own purposes." +
						"</html>", 
						"About", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("img/cassandra_logo.png"));
			}
		});
		frame.setJMenuBar(menuBar);
	}

	/**
	 *
	 */
	public static class TransferActionListener implements ActionListener, PropertyChangeListener {
		private JComponent focusOwner = null;

		public TransferActionListener() {
			KeyboardFocusManager manager = KeyboardFocusManager.
					getCurrentKeyboardFocusManager();
			manager.addPropertyChangeListener("permanentFocusOwner", this);
		}

		public void propertyChange(PropertyChangeEvent e) {
			Object o = e.getNewValue();
			if (o instanceof JComponent) {
				focusOwner = (JComponent)o;
			} else {
				focusOwner = null;
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (focusOwner == null)
				return;
			String action = (String)e.getActionCommand();
			Action a = focusOwner.getActionMap().get(action);
			if (a != null) {
				a.actionPerformed(new ActionEvent(focusOwner,
						ActionEvent.ACTION_PERFORMED,
						null));
			}
		}
	}

	/**
	 *
	 */
	public static class ChangeLayoutActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(arg0.getSource() == circle)
				CSN.rearrange(CSN.CIRCLELAYOUT);
			else if(arg0.getSource() == fr)
				CSN.rearrange(CSN.FRLAYOUT);	
			else if(arg0.getSource() == fr2)
				CSN.rearrange(CSN.FRLAYOUT2);	
			else if(arg0.getSource() == isoMLayout)
				CSN.rearrange(CSN.ISOMLAYOUT2);
			else if(arg0.getSource() == spring)
				CSN.rearrange(CSN.SPRINGLAYOUT);
			else
				CSN.rearrange(CSN.KKLAYOUT);
		}
	}
}
