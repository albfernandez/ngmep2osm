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

import ngmep.osm.datamodel.Relation;
import ngmep.osm.datamodel.RelationMember;

import org.apache.commons.lang3.StringUtils;

public class RelationDAO extends AbstractEntityDAO{
    public static final String QUERY_BASE = 
        "select id, version, user_id, tstamp, changeset_id from relations r ";
    public static final String QUERY_RELATION = 
        QUERY_BASE + " where id=?";   
    public static final String QUERY_RELATION_TAGS_A =
        "select relation_id, k, v from relation_tags where relation_id = ?";
    public static final String QUERY_RELATION_TAGS =
        "select id relation_id, (each(tags)).key k, (each(tags)).value v from relations where id = ?";
    
    public static final String QUERY_RELATION_MEMBERS = 
        "SELECT relation_id, member_id, member_type, member_role, sequence_id " + 
    		"  FROM relation_members where relation_id = ? order by sequence_id";
    
    private static RelationDAO instance = new RelationDAO();
    
    public static RelationDAO getInstance() {
        return instance;
    }
    
	public Relation getRelation(long id) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Relation relation = null;
		try {
			ps = Database.getConnection().prepareStatement(QUERY_RELATION);
			ps.setLong(1, id);
			rs = ps.executeQuery();

			if (rs.next()) {
				relation = getRelation(rs);
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
				catch (Exception e){
					// Ignore
				}
				
			}
		}
		return relation;
	}
    public List<Relation> getRelationsByTag (String tagName, String value) throws SQLException {
        
        
        String query = QUERY_BASE;
        if (Database.isSimpleSchema()){
            query += " where exists (select 1 from relation_tags t where t.relation_id = r.id and t.k = ?";
            if (!StringUtils.isBlank(value)) {
                query += " and t.v = ?";
            }
            query +=")";
        }
        else {
            query += " where tags->? = ?";
        }
        
        

        
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Relation> resultado = null;
		try {
			ps = Database.getConnection().prepareStatement(query);
			ps.setString(1, tagName);
			if (!StringUtils.isBlank(value)) {
				ps.setString(2, value);
			}
			rs = ps.executeQuery();
			resultado = getRelations(rs);
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
     
        return resultado;
        
    }
    
    public List<Relation> getRelations(ResultSet rs) throws SQLException {
        List<Relation> lista = new ArrayList<Relation>();
        while (rs.next()) {
            lista.add(getRelation(rs));
        }
        return lista;
    }
    public Relation getRelation(ResultSet rs) throws SQLException {
        Relation relation = new Relation();
        relation.setId(rs.getLong("id"));
        relation.setUser(UserDAO.getInstance().getUser(rs.getInt("user_id")));
        relation.setVersion(rs.getInt("version"));
        Calendar calendario = new GregorianCalendar();
        relation.setTimestamp(rs.getTimestamp("tstamp", calendario).getTime());
        relation.setChangeset(rs.getLong("changeset_id"));
        
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try {
			ps2 = Database.getConnection().prepareStatement(
					getQueryRelationTags());
			ps2.setLong(1, relation.getId());
			rs2 = ps2.executeQuery();
			initTags(relation, rs2);
		} finally {
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
        
        loadMembers(relation);
        relation.setModified(false);
        return relation;
    }
    public void loadMembers(Relation relation) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = Database.getConnection().prepareStatement(
					QUERY_RELATION_MEMBERS);
			ps.setLong(1, relation.getId());
			rs = ps.executeQuery();
			while (rs.next()) {

				String tipo = rs.getString("member_type");
				String role = rs.getString("member_role");
				long memberId = rs.getLong("member_id");
				RelationMember member = new RelationMember();
				member.setRole(role);
				if ("N".equals(tipo)) {
					member.setEntity(NodeDAO.getInstance().getNode(memberId));
				} else if ("W".equals(tipo)) {
					member.setEntity(WayDAO.getInstance().getWay(memberId));
				} else if ("R".equals(tipo)) {
					member.setEntity(RelationDAO.getInstance().getRelation(
							memberId));
				}
				relation.addMember(member);

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
    }
    private String getQueryRelationTags() {
        if (Database.isSimpleSchema()){
            return QUERY_RELATION_TAGS_A;
        }
        return QUERY_RELATION_TAGS;

    }
    
}
