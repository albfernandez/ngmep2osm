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

import ngmep.osm.datamodel.Node;

import org.apache.commons.lang3.StringUtils;

public final class NodeDAO extends AbstractEntityDAO {
    
    public static final String QUERY_BASE = 
        "select id, version, user_id, tstamp, changeset_id, st_x(geom) as lon, st_y(geom) as lat from nodes n ";
    public static final String QUERY_NODE = 
        QUERY_BASE + " where id=?";
    public static final String QUERY_NODE_TAGS_A =
        "select node_id, k, v from node_tags where node_id = ?";
    public static final String QUERY_NODE_TAGS =
        "select id node_id, (each(tags)).key k, (each(tags)).value v from nodes where id = ?";
    
    private static NodeDAO instance = new NodeDAO();
    
    public static NodeDAO getInstance() {
        return instance;
    }
    private NodeDAO(){
        super();
    }
    
    public List<Node> getNodesByTag(final String tagName) throws SQLException {
        return getNodesByTag(tagName, null);
    }

	public List<Node> getNodesByTag(final String tagName, final String value)
			throws SQLException {

		String query = QUERY_BASE
				+ "where exists (select 1 from node_tags t where t.node_id = n.id and t.k = ?";
		if (!StringUtils.isBlank(value)) {
			query += " and t.v = ?";
		}
		query += ")";
		List<Node> resultado = null;
		try (PreparedStatement statement = Database.getConnection().prepareStatement(query);){
			statement.setString(1, tagName);
			if (!StringUtils.isBlank(value)) {
				statement.setString(2, value);
			}
			try (ResultSet resultSet = statement.executeQuery();){
				resultado = getNodes(resultSet);
			}			
		} 

		return resultado;

    }

	public List<Node> getNodes(final ResultSet resultSet) throws SQLException {
		final List<Node> lista = new ArrayList<Node>();
		while (resultSet.next()) {
			lista.add(getNode(resultSet));
		}
		return lista;
	}

	public Node getNode(final ResultSet resultSet) throws SQLException {
		final long identifier = resultSet.getLong("id");
		final Node node = new Node();
		node.setId(identifier);
		node.setUser(UserDAO.getInstance().getUser(resultSet.getInt("user_id")));
		node.setVersion(resultSet.getInt("version"));
		final Calendar calendario = new GregorianCalendar();
		node.setTimestamp(resultSet.getTimestamp("tstamp", calendario).getTime());
		node.setChangeset(resultSet.getLong("changeset_id"));
		node.setLat(resultSet.getDouble("lat"));
		node.setLon(resultSet.getDouble("lon"));
				
		try (PreparedStatement statement = Database.getConnection().prepareStatement(getQueryNodeTags());){			
			statement.setLong(1, identifier);
			try (ResultSet resultSet2 = statement.executeQuery();){
				initTags(node, resultSet2);
			}
		}
		node.setModified(false);
		return node;
	}

	public Node getNode(final long nodeId) throws SQLException {		
		Node node = null;
		try (PreparedStatement statement = Database.getConnection().prepareStatement(QUERY_NODE); ){			
			statement.setLong(1, nodeId);
			try (ResultSet resultSet = statement.executeQuery();) {
				if (resultSet.next()) {
					node = getNode(resultSet);
				}
			}
		}
		return node;
	}
    
    public List<Node> getNode(final double lon, final double lat, final double distance, final String key, final String[] values) throws SQLException {
        final StringBuilder query = new StringBuilder(); 
        
        query.append("select id, version, user_id, tstamp, changeset_id, st_x(geom) as lon, st_y(geom) as lat from nodes n ");
        query.append(" where 1=1 and n.id > 0 ");
        if (!StringUtils.isBlank(key)){
            query.append(" and  exists (select 1 from node_tags t where t.node_id = n.id and t.k= ? ");
            if (values != null && values.length > 0){
                query.append(" and t.v in ('___dummy'");
                for (int i = 0; i < values.length; i++){
                    query.append(", ? ");
                }
                query.append(")");
            }
            query.append(")");
        }
        query.append(" and st_distance(st_setsrid(st_point(?,?),4326), geom) < ?");
        
        
        List<Node> lista = null;
		try (PreparedStatement statement = Database.getConnection().prepareStatement(query.toString());) {
			
			int indice = 1;

			if (!StringUtils.isBlank(key)) {
				statement.setString(indice++, key);
				if (values != null) {
					for (String valor : values) {
						statement.setString(indice++, valor);
					}
				}
			}
			statement.setDouble(indice++, lon);
			statement.setDouble(indice++, lat);
			statement.setDouble(indice++, distance);
			try (ResultSet resultSet = statement.executeQuery();){
				lista = getNodes(resultSet);
			}
		} 
		return lista;
	}
    

    
    

    private String getQueryNodeTags() {
        if (Database.isSimpleSchema()){
            return QUERY_NODE_TAGS_A;
        }
        return QUERY_NODE_TAGS;

    }

}

/*

CREATE TABLE nodes
(
  id bigint NOT NULL,
  version integer NOT NULL,
  user_id integer NOT NULL,
  tstamp timestamp without time zone NOT NULL,
  changeset_id bigint NOT NULL,
  geom geometry,
  CONSTRAINT pk_nodes PRIMARY KEY (id ),
  CONSTRAINT enforce_dims_geom CHECK (st_ndims(geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(geom) = 'POINT'::text OR geom IS NULL),
  CONSTRAINT enforce_srid_geom CHECK (st_srid(geom) = 4326)
)

*/