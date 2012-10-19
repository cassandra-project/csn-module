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


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Charts
{

	/**
	 * 
	 * @param title
	 * @param x
	 * @param y
	 * @param data
	 * @return
	 */
	public static ChartPanel createGraph (String title, String x,
			String y, Double[] data) {
		XYSeries series1 = new XYSeries("First");
		for (int i = 0; i < data.length; i++) {
			series1.add(i, data[i]);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);

		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart =
				ChartFactory.createXYLineChart(title, x, y, dataset, orientation, show,
						toolTips, urls);
		//XYPlot plot = (XYPlot) chart.getPlot();  

		ChartPanel cp = new ChartPanel(chart);
		return cp;
	}
}