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

import eu.cassandra.csn.gui.CSN;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

public class SaveGraph {

	/**
	 * 	
	 * @param fileName
	 */
	public static void writeCSV(File fileName) {
		saveGraphForPajek(fileName);
	}

	/**
	 * 
	 * @param fileName
	 */
	public static void writeJPG(File fileName) {
		writeJPEGImage(fileName);
	}

	/**
	 * 
	 * @param file
	 */
	private static void saveGraphForPajek(File file) {
		try {
			if(!file.toString().contains("."))
				file = new File(file.toString() + ".pajek");
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fileWriter);

			HashMap<String,Integer> nodeIDHashMap = new HashMap<String,Integer>();
			Collection<MyNode> nodes = CSNGraph.getGraph().getVertices();
			bw.write("*Vertices " + nodes.size());
			bw.newLine();
			Iterator<MyNode> nodesIter = nodes.iterator();
			int nodeCounter = 1;
			while(nodesIter.hasNext()) {
				MyNode node = nodesIter.next();
				bw.write(nodeCounter + " \"" + node.toString() + "\"");
				bw.newLine();
				nodeIDHashMap.put(node.getId(), nodeCounter);
				nodeCounter++;
			}
			bw.write("*Arcs");
			bw.newLine();

			Collection<MyLink> edges = CSNGraph.getGraph().getEdges();
			Iterator<MyLink> edgesIter = edges.iterator();
			while(edgesIter.hasNext()) {
				MyLink edge = edgesIter.next();
				MyNode node1 = edge.getNode1();
				MyNode node2 = edge.getNode2();
				int id1 = nodeIDHashMap.get(node1.getId());
				int id2 = nodeIDHashMap.get(node2.getId());
				bw.write(id1 + " " + id2);
				bw.newLine();
			}

			bw.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param filename
	 */
	private static void writeJPEGImage(File filename) {
		try{
			if(!filename.toString().contains("."))
				filename = new File(filename.toString() + ".jpg");
			int width = CSN.getVisualizationViewer().getSize().width;
			int height = CSN.getVisualizationViewer().getSize().height;
			Color bg = CSN.getVisualizationViewer().getBackground();
			BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
			Graphics2D graphics = bi.createGraphics();
			graphics.setColor(bg);
			graphics.fillRect(0,0, width, height);
			CSN.getVisualizationViewer().paint(graphics);
			ImageIO.write(bi,"jpeg",filename);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
