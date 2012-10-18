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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.util.Animator;
import eu.cassandra.csn.Main;
import eu.cassandra.csn.graph.CSNGraph;
import eu.cassandra.csn.graph.ClusterGraph;
import eu.cassandra.csn.graph.InstallationInfo;
import eu.cassandra.csn.graph.MyLink;
import eu.cassandra.csn.graph.MyNode;
import eu.cassandra.csn.mongo.MongoQueries;

/**
 *
 */
public class CSN {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<MyNode,Paint> vertexPaints = LazyMap.<MyNode,Paint>decorate(new HashMap<MyNode,Paint>(),new ConstantTransformer(Color.white));
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Map<MyLink,Paint> edgePaints = LazyMap.<MyLink,Paint>decorate(new HashMap<MyLink,Paint>(),new ConstantTransformer(Color.blue));
	private static AggregateLayout<MyNode,MyLink> aggrLayout = new AggregateLayout<MyNode,MyLink>(new FRLayout<MyNode,MyLink>(SparseMultigraph.<MyNode,MyLink>getFactory().create()));
	private static final VisualizationViewer<MyNode,MyLink> vv = new VisualizationViewer<MyNode,MyLink>(aggrLayout);
	private static DefaultModalGraphMouse<Object, Object> gm = new DefaultModalGraphMouse<Object, Object>();
	private static JSlider slider = new JSlider(JSlider.HORIZONTAL);
	private static JLabel sliderLabel = new JLabel("Change the clustering options of the graph:");
	private static JSlider sliderEdge = new JSlider(JSlider.HORIZONTAL);
	private static JLabel sliderLabelEdge = new JLabel("Change the minimum link threshold of the graph:");


	/**
	 * 
	 * @param graph
	 * @return
	 */
	public static JPanel setUpView(){
		JPanel csnPanel = new JPanel();
		vv.getGraphLayout().setSize(new Dimension(csnPanel.getWidth(),csnPanel.getHeight()));
		setUpVV();
		GraphZoomScrollPane graphZoomScrollPane = new GraphZoomScrollPane(vv);
		JPanel thisPanel  = new JPanel(new BorderLayout());
		thisPanel.add(graphZoomScrollPane,BorderLayout.CENTER);

		slider.setBackground(Color.WHITE);
		slider.setPreferredSize(new Dimension(210, 50));
		slider.setPaintTicks(true);
		slider.setMaximum(100);
		slider.setMinimum(0);
		slider.setValue(0);
		slider.setMajorTickSpacing(50);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setBorder(BorderFactory.createLineBorder(Color.black));
		slider.setEnabled(false);
		sliderLabel.setEnabled(false);
		slider.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(sliderLabel.getText().contains("oltage"))
					ClusterGraph.clusterAndRecolor(ClusterGraph.VoltageClusterer ,slider.getValue());
				else if(sliderLabel.getText().contains("etweenness"))
					ClusterGraph.clusterAndRecolor(ClusterGraph.BetweennessClusterer ,slider.getValue());
			}
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
		});
		JPanel p = new JPanel(new BorderLayout());
		p.add(sliderLabel,BorderLayout.PAGE_START);
		p.add(slider,BorderLayout.CENTER);


		sliderEdge.setBackground(Color.WHITE);
		sliderEdge.setPreferredSize(new Dimension(210, 50));
		sliderEdge.setPaintTicks(true);
		sliderEdge.setValue(0);
		sliderEdge.setMajorTickSpacing(5);
		sliderEdge.setPaintLabels(true);
		sliderEdge.setPaintTicks(true);
		sliderEdge.setBorder(BorderFactory.createLineBorder(Color.black));
		sliderEdge.setEnabled(false);
		sliderLabelEdge.setEnabled(false);
		sliderEdge.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				double v =  Double.parseDouble(
						((JLabel)sliderEdge.getLabelTable().get(sliderEdge.getValue())).getText());
				CSNGraph.addEdgesBasedOnKL(v);
			}
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
		});
		JPanel pEdge = new JPanel(new BorderLayout());
		pEdge.add(sliderLabelEdge,BorderLayout.PAGE_START);
		pEdge.add(sliderEdge,BorderLayout.CENTER);

		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(pEdge,BorderLayout.PAGE_START);
		p2.add(p,BorderLayout.PAGE_END);

		thisPanel.add(p2,BorderLayout.PAGE_END);

		return thisPanel;
	}

	/**
	 * 
	 */
	private static Transformer<MyNode,Shape> vertexSize = new Transformer<MyNode,Shape>(){
		public Shape transform(MyNode i){
			Ellipse2D circle = new Ellipse2D.Double(-10, -10, 20, 20);
			if(i.isBig()) 
				return AffineTransform.getScaleInstance(2, 2).createTransformedShape(circle);
			else return circle;
		}
	};


	/**
	 * 
	 */
	private static void setUpVV() {
		vv.setBackground( Color.white );
		vv.getRenderContext().setVertexFillPaintTransformer(MapTransformer.<MyNode,Paint>getInstance(vertexPaints));
		vv.getRenderContext().setVertexShapeTransformer(vertexSize);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyNode>());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<MyLink>());
		vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<MyNode,Paint>() {
			public Paint transform(MyNode v) {
				if(vv.getPickedVertexState().isPicked(v)) {
					return Color.cyan;
				} else {
					return Color.BLACK;
				}
			}
		});
		vv.getRenderContext().setEdgeDrawPaintTransformer(MapTransformer.<MyLink,Paint>getInstance(edgePaints));
		vv.getRenderContext().setEdgeStrokeTransformer(new Transformer<MyLink,Stroke>() {
			protected final Stroke THIN = new BasicStroke((float)0.5);
			protected final Stroke THICK= new BasicStroke(1);
			public Stroke transform(MyLink e)
			{
				Paint c = edgePaints.get(e);
				if (c == Color.LIGHT_GRAY) {
					return THIN;
				}
				else { 
					return THICK;
				}
			}
		});
		vv.setGraphMouse(gm);
		vv.getPickedVertexState().addItemListener(new VertexPickListener());

	}

	/**
	 *
	 */
	public static class VertexPickListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			for(int j=Stats.getTableModelSelected().getRowCount()-1;j>=0;j--)
				Stats.getTableModelSelected().removeRow(j);
			@SuppressWarnings("unchecked")
			MultiPickedState<Object> state = (MultiPickedState<Object>)arg0.getSource();
			Vector<InstallationInfo> installationInfoVec = new Vector<InstallationInfo>();
			for(int i=0;i<state.getSelectedObjects().length;i++) {
				MyNode node = (MyNode)state.getSelectedObjects()[i];
				InstallationInfo installationInfo = MongoQueries.getInstallationInfo(node.getId());
				installationInfoVec.add(installationInfo);
			}
			Stats.updateTableModelSelected(installationInfoVec);
		}
	}



	/**
	 * 
	 * @param type
	 */
	public static void setModalGraphMouse(int type) {
		if(type == 0)
			gm.setMode(edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode.TRANSFORMING);
		else if(type == 1)
			gm.setMode(edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode.PICKING);
		else if(type == 2)
			gm.setMode(edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode.ANNOTATING);
		else if(type == 3)
			gm.setMode(edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode.EDITING);
		vv.setGraphMouse(gm);
	}


	private static Set<Set<MyNode>> clusters = null;

	public static Set<Set<MyNode>>  getClusters() {
		return clusters;
	}

	public static void setClusters(Set<Set<MyNode>> clusters) {
		CSN.clusters = clusters;
	}

	/**
	 * 
	 * @param clusters
	 */
	public static void groupCluster() { //Set<Set<MyNode>> clusters
		if(clusters == null)
			return;
		aggrLayout.removeAll();
		Iterator<Set<MyNode>> iter = 	clusters.iterator();
		while(iter.hasNext()) {
			Set<MyNode> vertices = iter.next();

			Point2D center =  aggrLayout.transform(vertices.iterator().next());
			Graph<MyNode,MyLink> subGraph = SparseMultigraph.<MyNode,MyLink>getFactory().create();
			for(MyNode v : vertices) {
				subGraph.addVertex(v);
			}
			//			Layout<MyNode,MyLink> subLayout = new  CircleLayout<MyNode,MyLink>(subGraph);//CircleLayout
			//			subLayout.setInitializer(vv.getGraphLayout());
			//			subLayout.setSize(new Dimension(80,80));
			//			CSN.getAggregateLayout().put(subLayout,center);
			//			vv.repaint();
			//			vv.revalidate();

			Layout<MyNode,MyLink> subLayout = new CircleLayout<MyNode,MyLink>(subGraph); //Menu.getSelectedLayout(subGraph);
			subLayout.setInitializer(vv.getGraphLayout());
			if(subGraph.getVertexCount() > 15)
				subLayout.setSize(new Dimension(160,160));
			else
				subLayout.setSize(new Dimension(80,80));

			aggrLayout.put(subLayout, center);

			vv.setGraphLayout(aggrLayout);
			vv.repaint();
		}
		vv.repaint();
	}


	public static final int CIRCLELAYOUT = 0;
	public static final int FRLAYOUT = 2;
	public static final int FRLAYOUT2 = 3;
	public static final int ISOMLAYOUT2 = 4;
	public static final int KKLAYOUT = 5;
	public static final int SPRINGLAYOUT = 6;

	/**
	 * 
	 * @param layoutType
	 */
	public static void rearrange(int layoutType) {
		//Point2D center = aggregateLayout.transform(CSNGraph.getGraph().getVertices().iterator().next());
		Layout<MyNode,MyLink> subLayout= null; 

		if(layoutType == CIRCLELAYOUT)
			subLayout = new CircleLayout<MyNode,MyLink>(CSNGraph.getGraph());
		else if(layoutType == FRLAYOUT)
			subLayout = new FRLayout<MyNode,MyLink>(CSNGraph.getGraph());
		else if(layoutType == FRLAYOUT2)
			subLayout = new FRLayout2<MyNode,MyLink>(CSNGraph.getGraph());
		else if(layoutType == ISOMLAYOUT2)
			subLayout = new ISOMLayout<MyNode,MyLink>(CSNGraph.getGraph());
		else if(layoutType == KKLAYOUT)
			subLayout = new KKLayout<MyNode,MyLink>(CSNGraph.getGraph());
		else if(layoutType == SPRINGLAYOUT)
			subLayout = new SpringLayout<MyNode,MyLink>(CSNGraph.getGraph());

		if(subLayout != null) {
			if(Main.MainFrame.isVisible()) {
				subLayout.setInitializer(vv.getGraphLayout());
				subLayout.setSize(vv.getSize());

				LayoutTransition<MyNode,MyLink> lt =
						new LayoutTransition<MyNode,MyLink>(vv, vv.getGraphLayout(),subLayout);
				Animator animator = new Animator(lt);
				animator.start();
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.repaint();
			}
		}
	}

	public static final Color[] similarColors =
		{
		new Color(216, 134, 134),
		new Color(135, 137, 211),
		new Color(134, 206, 189),
		new Color(206, 176, 134),
		new Color(194, 204, 134),
		new Color(145, 214, 134),
		new Color(133, 178, 209),
		new Color(103, 148, 255),
		new Color(60, 220, 220),
		new Color(30, 250, 100)
		};

	/**
	 * 
	 * @param vertices
	 * @param c
	 */
	public static void colorGraph(Collection<MyNode> vertices, Color c) {
		for (MyNode v : vertices) {
			vertexPaints.put(v, c);
		}
	}

	public static JLabel getSliderEdgeLabel() {
		return sliderLabelEdge;
	}

	public static JSlider getEdgeSlider() {
		return sliderEdge;
	}

	public static JLabel getSliderLabel() {
		return sliderLabel;
	}

	public static JSlider getSlider() {
		return slider;
	}

	public static  Map<MyLink,Paint>  getEdgePaints() {
		return  edgePaints;
	}

	public static  VisualizationViewer<MyNode,MyLink> getVisualizationViewer(){
		return vv;
	}
}

