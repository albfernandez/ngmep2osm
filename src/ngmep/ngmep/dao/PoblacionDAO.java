package ngmep.ngmep.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ngmep.osm.dao.Database;
import ngmep.osm.dao.NodeDAO;
import ngmep.osm.dao.WayDAO;
import ngmep.osm.datamodel.Entity;

public class PoblacionDAO {
	
	private static PoblacionDAO instance = new PoblacionDAO();
	
	public static PoblacionDAO getInstance() {
		return instance;
	}

	private PoblacionDAO(){
		super();
	}
	
    public List<Entity> getPoblaciones(final double lon, final double lat, final double distance) throws SQLException {
    	String query = "select id, version, user_id, tstamp, changeset_id, st_x(geom) as lon, st_y(geom) as lat, tipo from poblaciones_osm  "+
    	" where 1=1 "+
    	" and st_distance(st_setsrid(st_point(?,?),4326), geom) < ?";
    	
		List<Entity> lista = null;
		try (PreparedStatement statement = Database.getConnection().prepareStatement(query);){			
			statement.setDouble(1, lon);
			statement.setDouble(2, lat);
			statement.setDouble(3, distance);
			try (ResultSet resultSet = statement.executeQuery();){
				lista = getResults(resultSet);	
			}			
		}
		return lista;
    }
    
    public List<Entity> getResults(final ResultSet resultSet) throws SQLException{
    	List<Entity> lista = new ArrayList<Entity>();
    	while (resultSet.next()){
    		String tipo = StringUtils.trim(resultSet.getString("tipo"));
    		if ("node".equals(tipo)){
    			lista.add(NodeDAO.getInstance().getNode(resultSet));
    		}
    		else if ("way".equals(tipo)){
    			lista.add(WayDAO.getInstance().getWay(resultSet));
    		}
    	}
    	
    	
    	return lista;
    }
}
