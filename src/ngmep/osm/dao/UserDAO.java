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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ngmep.osm.datamodel.User;

public class UserDAO {
    
    private Map<Integer, User> cache = new HashMap<Integer, User>();

    private static UserDAO instance = new UserDAO();
    
    public static UserDAO getInstance() {
        return instance;
    }
    private UserDAO() {
        super();
    }
    public static final String QUERY = "select id, name from users where id = ?";
    public User getUser(int id) throws SQLException{
        if (cache.get(id) == null){
            User u = getUserFromBD(id);
            cache.put(id, u);
        }
        return cache.get(id);
    }
    public User getUserFromBD(int id) throws SQLException{
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	User user = null;
    	try {
	        ps = Database.getConnection().prepareStatement(QUERY);
	        ps.setInt(1, id);
	        rs  = ps.executeQuery();
	       
	        if (rs.next()){
	            user = new User();
	            user.setId(id);
	            user.setName(rs.getString("name"));
	        }
    	}
    	finally {
    		if (rs != null) {
    			try {
    				rs.close();
    			}
    			catch (Exception e) {
    				// Ignore
    			}
    		}
    		if (ps != null) {
    			try {
    				ps.close();
    			}
    			catch (Exception e ) {
    				// Ignore
    			}
    		}
    			
    	}
        return user;
    }
}
