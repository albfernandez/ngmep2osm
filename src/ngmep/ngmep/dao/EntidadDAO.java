package ngmep.ngmep.dao;

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
import java.util.List;

import ngmep.ngmep.datamodel.Entidad;
import ngmep.osm.dao.Database;

public final class EntidadDAO {
    
    public static final String QUERY_BASE = 
        "SELECT cod_ine, cod_ine_mun, nombre_mun, name, altura, origen_alturas, " + 
        "       st_x(the_geom) as lon, st_y(the_geom) as lat, osmid, place, poblacion, admin_level, cod_prov, poblacion_muni, " + 
        "       official_name, alt_name, old_name, name1, name2, lan1, lan2, " + 
        "       fecha_cambio_nom, decreto_boletin_oficial, loc_name, estado_manual, " + 
        "       estado_robot, decision_nombre " + 
        "  FROM ngmep ";
    public static final String QUERY_OSMID = QUERY_BASE + " where osmid = ?";
    public static final String QUERY_INE   = QUERY_BASE + " where cod_ine = ?";
        
    
    private static EntidadDAO instance = new EntidadDAO();
    public static EntidadDAO getInstance(){
        return instance;
    }
    private EntidadDAO(){
        
    }
    
	public Entidad getEntidadFromOsmId(final long osmid) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Entidad entidad = null;
		
		try {
			statement = Database.getConnection().prepareStatement(QUERY_OSMID);
			statement.setLong(1, osmid);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				entidad = getEntidad(resultSet);
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
				statement.close();
			}
		}

		return entidad;
	}

	public Entidad getEntidadFromCodIne(final String codIne) throws SQLException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Entidad entidad = null;
		try {
			statement = Database.getConnection().prepareStatement(QUERY_INE);
			statement.setString(1, codIne);
			resultSet = statement.executeQuery();

			if (resultSet.next()) {
				entidad = getEntidad(resultSet);
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
				statement.close();
			}
		}

		return entidad;
	}

	private Entidad getEntidad(final ResultSet resultSet) throws SQLException {
		final Entidad entidad = new Entidad();
		entidad.setCodine(resultSet.getString("cod_ine"));
		entidad.setCodineMun(resultSet.getString("cod_ine_mun"));
		entidad.setNombreMun(resultSet.getString("nombre_mun"));
		entidad.setName(resultSet.getString("name"));
		entidad.setAltura(resultSet.getDouble("altura"));
		entidad.setSourceAltura(resultSet.getString("origen_alturas"));
		entidad.setLat(resultSet.getDouble("lat"));
		entidad.setLon(resultSet.getDouble("lon"));
		entidad.setOsmid(resultSet.getLong("osmid"));
		entidad.setPlace(resultSet.getString("place"));
		entidad.setPoblacion(resultSet.getDouble("poblacion"));
		entidad.setAdministrativeLevel(resultSet.getInt("admin_level"));
		entidad.setPoblacionMuni(resultSet.getDouble("poblacion_muni"));
		entidad.setNombreOficial(resultSet.getString("official_name"));
		entidad.setNombreAlternativo(resultSet.getString("alt_name"));
		entidad.setNombreAntiguo(resultSet.getString("old_name"));
		entidad.setName1(resultSet.getString("name1"));
		entidad.setName2(resultSet.getString("name2"));
		entidad.setLan1(resultSet.getString("lan1"));
		entidad.setLan2(resultSet.getString("lan2"));
		entidad.setFechaCambioNombre(resultSet.getString("fecha_cambio_nom"));
		entidad.setDecreto(resultSet.getString("decreto_boletin_oficial"));
		entidad.setLocName(resultSet.getString("loc_name"));
		entidad.setEstadoManual(resultSet.getInt("estado_manual"));
		entidad.setEstadoRobot(resultSet.getInt("estado_robot"));
		entidad.setCodigoProvincia(resultSet.getString("cod_prov"));
		entidad.setDecisionNombre(resultSet.getString("decision_nombre"));

		return entidad;
	}

	public List<Entidad> getListFromRs(final ResultSet resultSet) throws SQLException {
		final List<Entidad> lista = new ArrayList<Entidad>();
		while (resultSet.next()) {
			lista.add(getEntidad(resultSet));
		}
		return lista;
	}
    public void updateOsmId(final Entidad entidad) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = Database.getConnection().prepareStatement(
					"update ngmep set osmid=? where cod_ine = ?");
			statement.setLong(1, entidad.getOsmid());
			statement.setString(2, entidad.getCodine());
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
    }

}
/*



 poblacion_muni           double precision , 
 official_name            text             , 
 alt_name                 text             , 
 old_name                 text             , 
 name1                    text             ,
 name2                    text             ,
 lan1                     character(3)     , 
 lan2                     character(3)     , 
 fecha_cambio_nom         character(50)    , 
 decreto_boletin_oficial  character(100)   , 
 loc_name                 text,
 estado_manual                   integer default 0,         
 estado_robot                    integer default 0,

*/