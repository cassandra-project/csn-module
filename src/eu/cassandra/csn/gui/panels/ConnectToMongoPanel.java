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
package eu.cassandra.csn.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import eu.cassandra.csn.Main;
import eu.cassandra.csn.graph.CSNGraph;
import eu.cassandra.csn.gui.CSN;
import eu.cassandra.csn.gui.util.MyDefaultTableModel;
import eu.cassandra.csn.mongo.DBConn;
import eu.cassandra.csn.mongo.MongoQueries;

public class ConnectToMongoPanel {

	private JTable table;
	private JDialog jDialog;
	private JComboBox combo;
	private MyDefaultTableModel tableModel = new MyDefaultTableModel();
	/**
	 * 
	 * @param frame
	 */
	public void showConnectToMongoPanel() {
		table = new JTable();
		jDialog = new JDialog(Main.MainFrame);

		jDialog.setTitle("Open MongoDB Database");
		jDialog.setModal(true);

		JLabel title = new JLabel("Completed Runs:");

		table.setModel(tableModel);
		table.setFillsViewportHeight(true);
		tableModel.setDataVector(MongoQueries.getRuns(),new String[] {"#","Run ID","Installations"});
		Vector aa = new Vector();
		aa.add("1");
		aa.add("50eab4afe4b077674b3cec22");
		aa.add("30");
		tableModel.addRow(aa);
		table.getSelectionModel().setSelectionInterval(0, 0);
		JScrollPane scroll = new JScrollPane(table);
		JButton open = new JButton("Open");		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okAction();
			}
		});
		JButton close = new JButton("Close");		
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jDialog.dispose();
			}
		});

		String[] options = new String[] {
				"Installation Type", 
				"Person Type", 
				"Average Consumption", 
				"Peak Consumption", 
				"Similar Consumption", 
				"Dissimilar Consumption", 
				"Similar Heating Consumption", 
				"Similar Cooking Consumption" , 
		"..."};
		combo = new JComboBox(options);

		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(new JLabel("Setup Network Based On:"));
		buttons.add(combo);
		buttons.add(open);
		buttons.add(close);

		jDialog.setLayout(new BorderLayout());

		jDialog.add(title,BorderLayout.PAGE_START);
		jDialog.add(scroll,BorderLayout.CENTER);
		jDialog.add(buttons,BorderLayout.PAGE_END);

		addCancelByEscapeKey();

		jDialog.setMinimumSize(new Dimension(600,400));
		jDialog.pack();
		jDialog.setLocationRelativeTo(Main.MainFrame);
		jDialog.setVisible(true);
	}

	/**
	 * 
	 */
	private void okAction() {
		if(table.getSelectedRow() >= 0) {
			DBConn.openDB(tableModel.getValueAt(table.getSelectedRow(), 1).toString());
			CSNGraph.resetGraph(true,combo.getSelectedIndex());
			CSN.rearrange(CSN.KKLAYOUT);
			jDialog.dispose();
		}
	}

	/**
	 * 
	 */
	private void addCancelByEscapeKey(){
		String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		InputMap inputMap = jDialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);
		AbstractAction cancelAction = new AbstractAction(){
			private static final long serialVersionUID = 5103398887267739150L;
			public void actionPerformed(ActionEvent e){
				jDialog.dispose();
			}
		}; 
		jDialog.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}
}
