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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ngmep.osm.datamodel.Way;

public class WayDAO extends AbstractEntityDAO{
    public static final String QUERY_WAY = 
        "select id, version, user_id, tstamp, changeset_id from ways where id=?";
    public static final String QUERY_WAY_TAGS_A =
        "select way_id, k, v from way_tags where way_id = ?";
    public static final String QUERY_WAY_TAGS =
        "select id way_id, (each(tags)).key k, (each(tags)).value v from ways where id = ?";
    public static final String QUERY_WAY_NODES = 
        "select way_id, node_id from way_nodes where way_id = ? order by sequence_id";
    private static  WayDAO instance = new WayDAO();
    
    public static WayDAO getInstance() {
        return instance;
    }
    private WayDAO(){
        super();
    }
    public Way getWay(long id) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Way way = null;
		try {
			ps = Database.getConnection().prepareStatement(QUERY_WAY);
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				way = getWay(rs);
			}
		} finally {
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
				catch (Exception e) {
					// Ignore
				}
			}
		}
     
        return way;
    }
    private Way getWay(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        Way way = new Way();
        way.setId(id);
        way.setUser(UserDAO.getInstance().getUser(rs.getInt("user_id")));
        way.setVersion(rs.getInt("version"));
        Calendar calendario = new GregorianCalendar();
        way.setTimestamp(rs.getTimestamp("tstamp", calendario).getTime());
        way.setChangeset(rs.getLong("changeset_id"));

        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try {
	        ps2 = Database.getConnection().prepareStatement(get_QUERY_WAY_TAGS());
	        ps2.setLong(1,id);
	        rs2 = ps2.executeQuery();
	        initTags(way, rs2);
        }
        finally {
        	if (rs2 != null) {
        		try {
        			rs2.close();
        		}
        		catch (Exception e) {
        			// Ignore
        		}
        	}
        	if (ps2 != null) {
        		try {
        			ps2.close();
        		}
        		catch (Exception e) {
        			// Ignore
        		}
        	}
        }
        loadPoints(way);
        way.setModified(false);
        return way;
    } 
    private String get_QUERY_WAY_TAGS() {
        if (Database.isSimpleSchema()){
            return QUERY_WAY_TAGS_A;
        }
        return QUERY_WAY_TAGS;

    }
    public List<Way> getWays(ResultSet rs) throws SQLException {
        List<Way> lista = new ArrayList<Way>();
        while (rs.next()) {
            lista.add(getWay(rs));
        }
        return lista;
    }

	private void loadPoints(Way way) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Long> idNodos = new ArrayList<Long>();

		try {
			ps = Database.getConnection().prepareStatement(QUERY_WAY_NODES);
			ps.setLong(1, way.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				idNodos.add(rs.getLong("node_id"));
			}
		} finally {
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
				catch (Exception e) {
					// Ignore
				}
			}
		}
		for (long id : idNodos) {
			way.addNode(NodeDAO.getInstance().getNode(id));
		}
	}

}
