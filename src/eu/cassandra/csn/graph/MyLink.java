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

public class MyLink {
	private double capacity; // should be private
	private double weight; // should be private for good practice
	private String id;
	private String label;
	private MyNode node1;
	private MyNode node2;

	/**
	 * 
	 * @param id
	 * @param weight
	 * @param nodeID1
	 * @param nodeID2
	 */
	public MyLink(String id,double weight, MyNode nodeID1, MyNode nodeID2) {
		this.setId(id);
		this.weight = weight;
		this.node1 = nodeID1;
		this.node2 = nodeID2;
	}

	public MyNode getNode1() {
		return node1;
	}

	public MyNode getNode2() {
		return node2;
	}

	public double getCapacity() {
		return capacity;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toString() { 
		return String.valueOf(weight);//"E"+id;
	}
}
