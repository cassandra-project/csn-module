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

public class InstallationInfo {
	private String id;
	private String installationName;
	private String installationType;
	private String personType;
	
	double totalConsumption = 0.0;
	double peakComsumption = 0.0;
	double avgConsumption = 0.0;
	
	double[] klValuesWithOtherInsts;

	public InstallationInfo(String id,String installationName, 
			String installationType, String personType) {
		this.id = id;
		this.installationName = installationName;
		this.installationType = installationType;
		this.personType = personType;
	}
	
	public String getID() {
		return id;
	}
	
	public String getInstallationName() {
		return installationName;
	}
	
	public String getInstallationType() {
		return installationType;
	}
	
	public String getPersonType() {
		return personType;
	}
	
	public double getTotalConsumption() {
		return totalConsumption;
	}

	public double getPeakComsumption() {
		return peakComsumption;
	}

	public double getAvgConsumption() {
		return avgConsumption;
	}

	public void setTotalConsumption(double totalConsumption) {
		this.totalConsumption = totalConsumption;
	}

	public void setPeakComsumption(double peakComsumption) {
		this.peakComsumption = peakComsumption;
	}

	public void setAvgConsumption(double avgConsumption) {
		this.avgConsumption = avgConsumption;
	}
	
	public double[] getKlValuesWithOtherInsts() {
		return klValuesWithOtherInsts;
	}

	public void setKlValuesWithOtherInsts(double[] klValuesWithOtherInsts) {
		this.klValuesWithOtherInsts = klValuesWithOtherInsts;
	}
}
