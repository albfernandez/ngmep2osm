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

public class NodeDAO extends AbstractEntityDAO {
    
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
    
    public List<Node> getNodesByTag(String tagName) throws SQLException {
        return getNodesByTag(tagName, null);
    }

	public List<Node> getNodesByTag(String tagName, String value)
			throws SQLException {

		String query = QUERY_BASE
				+ "where exists (select 1 from node_tags t where t.node_id = n.id and t.k = ?";
		if (!StringUtils.isBlank(value)) {
			query += " and t.v = ?";
		}
		query += ")";
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Node> resultado = new ArrayList<Node>();
		try {
			ps = Database.getConnection().prepareStatement(query);
			ps.setString(1, tagName);
			if (!StringUtils.isBlank(value)) {
				ps.setString(2, value);
			}
			rs = ps.executeQuery();
			resultado = getNodes(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return resultado;

    }

	public List<Node> getNodes(ResultSet rs) throws SQLException {
		List<Node> lista = new ArrayList<Node>();
		while (rs.next()) {
			lista.add(getNode(rs));
		}
		return lista;
	}

	private Node getNode(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		Node node = new Node();
		node.setId(id);
		node.setUser(UserDAO.getInstance().getUser(rs.getInt("user_id")));
		node.setVersion(rs.getInt("version"));
		Calendar calendario = new GregorianCalendar();
		node.setTimestamp(rs.getTimestamp("tstamp", calendario).getTime());
		node.setChangeset(rs.getLong("changeset_id"));
		node.setLat(rs.getDouble("lat"));
		node.setLon(rs.getDouble("lon"));

		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = Database.getConnection().prepareStatement(
					get_QUERY_NODE_TAGS());
			ps2.setLong(1, id);
			rs2 = ps2.executeQuery();
			initTags(node, rs2);
		} finally {
			if (rs2 != null) {
				rs2.close();
			}
			if (ps2 != null) {
				ps2.close();
			}
		}
		node.setModified(false);
		return node;
	}

	public Node getNode(long id) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Node node = null;
		try {
			ps = Database.getConnection().prepareStatement(QUERY_NODE);
			ps.setLong(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				node = getNode(rs);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return node;
	}
    
    public List<Node> getNode(double lon, double lat, double distance, String key, String[] values) throws SQLException {
        String query = "select id, version, user_id, tstamp, changeset_id, st_x(geom) as lon, st_y(geom) as lat from nodes n ";
        query += " where 1=1 and n.id > 0 ";
        if (!StringUtils.isBlank(key)){
            query += " and  exists (select 1 from node_tags t where t.node_id = n.id and t.k= ? ";
            if (values != null && values.length > 0){
                query += " and t.v in ('___dummy'";
                for (int i = 0; i < values.length; i++){
                    query += ", ? ";
                }
                query+=")";
            }
            query += ")";
        }
        query += " and st_distance(st_setsrid(st_point(?,?),4326), geom) < ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Node> lista = new ArrayList<Node>();
		try {
			ps = Database.getConnection().prepareStatement(query);
			int indice = 1;

			if (!StringUtils.isBlank(key)) {
				ps.setString(indice++, key);
				if (values != null) {
					for (String valor : values) {
						ps.setString(indice++, valor);
					}
				}
			}
			ps.setDouble(indice++, lon);
			ps.setDouble(indice++, lat);
			ps.setDouble(indice++, distance);
			rs = ps.executeQuery();

			lista = getNodes(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return lista;
	}
    
    public List<Node> getPoblaciones(double lon, double lat, double distance) throws SQLException {
    	String query = "select id, version, user_id, tstamp, changeset_id, st_x(geom) as lon, st_y(geom) as lat from poblaciones_osm  ";
    	query += " where 1=1 ";
    	query += " and st_distance(st_setsrid(st_point(?,?),4326), geom) < ?";
    	
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Node> lista = new ArrayList<Node>();
		try {
			ps = Database.getConnection().prepareStatement(query);
			ps.setDouble(1, lon);
			ps.setDouble(2, lat);
			ps.setDouble(3, distance);
			rs = ps.executeQuery();

			lista = getNodes(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return lista;
    }
    
    

    private String get_QUERY_NODE_TAGS() {
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