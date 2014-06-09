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

public final class WayDAO extends AbstractEntityDAO{
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
    public Way getWay(final long wayId) throws SQLException {
		Way way = null;
		try (PreparedStatement statement = Database.getConnection().prepareStatement(QUERY_WAY);) {
			statement.setLong(1, wayId);
			try (ResultSet resultSet = statement.executeQuery();){
				if (resultSet.next()) {
					way = getWay(resultSet);
				}
			}
		} 
     
        return way;
    }
    public Way getWay(final ResultSet resultSetWay) throws SQLException {
        final long wayId = resultSetWay.getLong("id");
        final Way way = new Way();
        way.setId(wayId);
        way.setUser(UserDAO.getInstance().getUser(resultSetWay.getInt("user_id")));
        way.setVersion(resultSetWay.getInt("version"));
        final Calendar calendario = new GregorianCalendar();
        way.setTimestamp(resultSetWay.getTimestamp("tstamp", calendario).getTime());
        way.setChangeset(resultSetWay.getLong("changeset_id"));


        try (PreparedStatement statement = Database.getConnection().prepareStatement(getQueryWayTags())){	        
	        statement.setLong(1,wayId);
	        try (ResultSet resultSet =  statement.executeQuery();){
	        	initTags(way, resultSet);
	        }
        }
        
        loadPoints(way);
        way.setModified(false);
        return way;
    } 
    private String getQueryWayTags() {
        if (Database.isSimpleSchema()){
            return QUERY_WAY_TAGS_A;
        }
        return QUERY_WAY_TAGS;

    }
    public List<Way> getWays(final ResultSet resultSet) throws SQLException {
        final List<Way> lista = new ArrayList<Way>();
        while (resultSet.next()) {
            lista.add(getWay(resultSet));
        }
        return lista;
    }

	private void loadPoints(final Way way) throws SQLException {
		final List<Long> idNodos = new ArrayList<Long>();

		try (PreparedStatement statement = Database.getConnection().prepareStatement(QUERY_WAY_NODES);){
			statement.setLong(1, way.getId());
			try (ResultSet resultSet = statement.executeQuery();){
				while (resultSet.next()) {
					idNodos.add(resultSet.getLong("node_id"));
				}
			}
		} 
		for (long id : idNodos) {
			way.addNode(NodeDAO.getInstance().getNode(id));
		}
	}

}
