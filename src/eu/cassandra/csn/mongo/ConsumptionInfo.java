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
package eu.cassandra.csn.mongo;

public class ConsumptionInfo {
	private double totalComnsumption;
	private double avgComnsumption;
	private double maxComnsumption;
	private int daysRun;
	
	public ConsumptionInfo(int daysRun, double totalComnsumption, double avgComnsumption, double maxComnsumption) {
		this.daysRun = daysRun;
		this.totalComnsumption = totalComnsumption;
		this.avgComnsumption = avgComnsumption;
		this.maxComnsumption = maxComnsumption;
	}

	public int getDaysRun() {
		return daysRun;
	}
	
	public double getTotalComnsumption() {
		return totalComnsumption;
	}

	public double getAvgComnsumption() {
		return avgComnsumption;
	}

	public double getMaxComnsumption() {
		return maxComnsumption;
	}
}