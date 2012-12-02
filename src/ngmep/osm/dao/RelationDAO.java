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

public final class RelationDAO extends AbstractEntityDAO{
    public static final String QUERY_BASE = 
        "select id, version, user_id, tstamp, changeset_id from relations r ";
    public static final String QUERY_RELATION = 
        QUERY_BASE + " where id=?";   
    public static final String RELATION_TAGS_A =
        "select relation_id, k, v from relation_tags where relation_id = ?";
    public static final String RELATION_TAGS =
        "select id relation_id, (each(tags)).key k, (each(tags)).value v from relations where id = ?";
    
    public static final String RELATION_MEMBERS = 
        "SELECT relation_id, member_id, member_type, member_role, sequence_id " + 
    		"  FROM relation_members where relation_id = ? order by sequence_id";
    
    private static RelationDAO instance = new RelationDAO();
    
    public static RelationDAO getInstance() {
        return instance;
    }
    
	public Relation getRelation(final long relationId) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Relation relation = null;
		try {
			statement = Database.getConnection().prepareStatement(QUERY_RELATION);
			statement.setLong(1, relationId);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				relation = getRelation(resultSet);
			}
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
			if (statement != null) {
				try {
					statement.close();
				}
				catch (Exception e){
					// Ignore
				}
				
			}
		}
		return relation;
	}
    public List<Relation> getRelationsByTag (final String tagName, final String value) throws SQLException {
        
        
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
        
        

        
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<Relation> resultado = null;
		try {
			statement = Database.getConnection().prepareStatement(query);
			statement.setString(1, tagName);
			if (!StringUtils.isBlank(value)) {
				statement.setString(2, value);
			}
			resultSet = statement.executeQuery();
			resultado = getRelations(resultSet);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
			if (statement != null) {
				try {
					statement.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
		}
     
        return resultado;
        
    }
    
    public List<Relation> getRelations(final ResultSet resultSet) throws SQLException {
        final List<Relation> lista = new ArrayList<Relation>();
        while (resultSet.next()) {
            lista.add(getRelation(resultSet));
        }
        return lista;
    }
    public Relation getRelation(final ResultSet resultSet) throws SQLException {
        final Relation relation = new Relation();
        relation.setId(resultSet.getLong("id"));
        relation.setUser(UserDAO.getInstance().getUser(resultSet.getInt("user_id")));
        relation.setVersion(resultSet.getInt("version"));
        final Calendar calendario = new GregorianCalendar();
        relation.setTimestamp(resultSet.getTimestamp("tstamp", calendario).getTime());
        relation.setChangeset(resultSet.getLong("changeset_id"));
        
		PreparedStatement statement = null;
		ResultSet resultSet2 = null;
		try {
			statement = Database.getConnection().prepareStatement(
					getQueryRelationTags());
			statement.setLong(1, relation.getId());
			resultSet2 = statement.executeQuery();
			initTags(relation, resultSet2);
		} finally {
			if (resultSet2 != null) {
				try {
					resultSet2.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
			if (statement != null) {
				try {
					statement.close();
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
    public void loadMembers(final Relation relation) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = Database.getConnection().prepareStatement(
					RELATION_MEMBERS);
			statement.setLong(1, relation.getId());
			resultSet = statement.executeQuery();
			while (resultSet.next()) {

				final String tipo = resultSet.getString("member_type");
				final String role = resultSet.getString("member_role");
				final long memberId = resultSet.getLong("member_id");
				final RelationMember member = new RelationMember();
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
			if (resultSet != null) {
				try {
					resultSet.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
			if (statement != null) {
				try {
					statement.close();
				}
				catch (Exception e) {
					// Ignore
				}
			}
		}
    }
    private String getQueryRelationTags() {
        if (Database.isSimpleSchema()){
            return RELATION_TAGS_A;
        }
        return RELATION_TAGS;

    }
    
}
