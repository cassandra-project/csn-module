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
package eu.cassandra.csn.gui.util;

import cc.mallet.util.Maths;


public class ConsumptionDetector {
	private static final int MINUTES_PER_DAY = 1440;

	/**
	 * 
	 * @param resultMatrix
	 * @return
	 */
	public static double[][] estimateKLD(double[][] resultMatrix) {
		int index = 0;
		int days = resultMatrix[0].length / MINUTES_PER_DAY;
		double[][] distanceMatrix = new double[resultMatrix.length][resultMatrix.length];
		double[][] tempMatrix = new double[resultMatrix.length][MINUTES_PER_DAY];
		for (int i = 0; i < resultMatrix.length; i++) {
			for (int j = 0; j < resultMatrix[0].length; j++) {
				index = j % 1440;
				tempMatrix[i][index] += resultMatrix[i][j];
			}
			double sumConsumption = 0;
			for (int j = 0; j < MINUTES_PER_DAY; j++) {
				tempMatrix[i][j] /= days;
				sumConsumption += tempMatrix[i][j];
			}
			for (int j = 0; j < MINUTES_PER_DAY; j++) {
				tempMatrix[i][j] /= sumConsumption;
			}
		}
		for (int i = 0; i < resultMatrix.length; i++) {
			for (int j = i; j < resultMatrix.length; j++) {
				distanceMatrix[i][j] = (Maths.klDivergence(resultMatrix[i], resultMatrix[j]) + Maths
						.klDivergence(resultMatrix[j], resultMatrix[i])) / 10000000;
				distanceMatrix[j][i] = distanceMatrix[i][j];
			}
		}
		return distanceMatrix;
	}
}