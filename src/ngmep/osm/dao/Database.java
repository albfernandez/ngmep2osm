package ngmep.osm.dao;

/*
	ngmep2osm - importador de datos de ngmep a openstreetmap
	
	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import ngmep.config.Config;

public class Database {

    
    private static Connection connection;
    private static boolean simpleSchema = false;
    
    public static Connection getConnection () throws   SQLException {
        if (connection == null){
            try {
            Properties config = Config.getInstance().getDatabaseCredentials();
            
            String url = "jdbc:postgresql://" + config.getProperty("host") + "/" + config.getProperty("database");
            
            Connection tmpConnection = DriverManager.getConnection(url, config);
            checkSimpleSchema(tmpConnection);
            connection = tmpConnection;
            }
            catch (IOException io){
                throw new SQLException("Error leyendo configuracion", io);
            }
                  
        }
        return connection;
    }
    public static void closeConnection() throws SQLException{
        if (connection != null) {
            connection.close();
        }
    }
    
    private static void checkSimpleSchema(Connection connection) {
    	DatabaseMetaData md;
		try {
			md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, "node_tags", null);
	        simpleSchema = rs.next();
	        rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}        
    }
    public static boolean isSimpleSchema () {
    	return simpleSchema;
    }
    
    
}
