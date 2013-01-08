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

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


public class DBConn {
	private static String DB_HOST = "localhost";
	private static Mongo m;
	private static HashMap<String,DB> dbs = new  HashMap<String,DB>();
	private static DB db;

	/**
	 * 
	 * @param dbHost
	 */
	public static void initDBConn(String dbHost) {
		try {
			if(dbHost != null)
				DB_HOST = dbHost;
			m = new Mongo(DB_HOST);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} 
	}

	public static DB openDB(String dbName) {
		db = getConn(dbName) ;
		return db;
	}

	public static DB getConn() {
		return db;
	}
	
	/**
	 * 
	 * @return
	 */
	public static DB getConn(String dbName) {
		if(dbs.containsKey(dbName)) {
			return dbs.get(dbName);
		}
		else {
			DB aDB = m.getDB(dbName);
			dbs.put(dbName, aDB);
		}
		return dbs.get(dbName);
	}

	public static Mongo getMongo() {
		if(m == null)
			initDBConn(null);
		return m;
	}

	public static List<String> getDBs() {
		List<String> collections = m.getDatabaseNames();
		System.out.println(collections);
		return collections;
	}
}
