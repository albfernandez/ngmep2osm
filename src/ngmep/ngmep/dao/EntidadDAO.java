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

public class EntidadDAO {
    
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
    
	public Entidad getEntidadFromOsmId(long osmid) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Entidad entidad = null;
		
		try {
			ps = Database.getConnection().prepareStatement(QUERY_OSMID);
			ps.setLong(1, osmid);
			rs = ps.executeQuery();

			if (rs.next()) {
				entidad = getEntidad(rs);
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
				ps.close();
			}
		}

		return entidad;
	}

	public Entidad getEntidadFromCodIne(String codIne) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Entidad entidad = null;
		try {
			ps = Database.getConnection().prepareStatement(QUERY_INE);
			ps.setString(1, codIne);
			rs = ps.executeQuery();

			if (rs.next()) {
				entidad = getEntidad(rs);
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
				ps.close();
			}
		}

		return entidad;
	}

	private Entidad getEntidad(ResultSet rs) throws SQLException {
		Entidad entidad = new Entidad();
		entidad.setCodine(rs.getString("cod_ine"));
		entidad.setCodineMun(rs.getString("cod_ine_mun"));
		entidad.setNombreMun(rs.getString("nombre_mun"));
		entidad.setName(rs.getString("name"));
		entidad.setAltura(rs.getDouble("altura"));
		entidad.setSourceAltura(rs.getString("origen_alturas"));
		entidad.setLat(rs.getDouble("lat"));
		entidad.setLon(rs.getDouble("lon"));
		entidad.setOsmid(rs.getLong("osmid"));
		entidad.setPlace(rs.getString("place"));
		entidad.setPoblacion(rs.getDouble("poblacion"));
		entidad.setAdministrativeLevel(rs.getInt("admin_level"));
		entidad.setPoblacionMuni(rs.getDouble("poblacion_muni"));
		entidad.setNombreOficial(rs.getString("official_name"));
		entidad.setNombreAlternativo(rs.getString("alt_name"));
		entidad.setNombreAntiguo(rs.getString("old_name"));
		entidad.setName1(rs.getString("name1"));
		entidad.setName2(rs.getString("name2"));
		entidad.setLan1(rs.getString("lan1"));
		entidad.setLan2(rs.getString("lan2"));
		entidad.setFechaCambioNombre(rs.getString("fecha_cambio_nom"));
		entidad.setDecreto(rs.getString("decreto_boletin_oficial"));
		entidad.setLocName(rs.getString("loc_name"));
		entidad.setEstadoManual(rs.getInt("estado_manual"));
		entidad.setEstadoRobot(rs.getInt("estado_robot"));
		entidad.setCodigoProvincia(rs.getString("cod_prov"));
		entidad.setDecisionNombre(rs.getString("decision_nombre"));

		return entidad;
	}

	public List<Entidad> getListFromRs(ResultSet rs) throws SQLException {
		List<Entidad> lista = new ArrayList<Entidad>();
		while (rs.next()) {
			lista.add(getEntidad(rs));
		}
		return lista;
	}
    public void updateOsmId(Entidad entidad) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = Database.getConnection().prepareStatement(
					"update ngmep set osmid=? where cod_ine = ?");
			ps.setLong(1, entidad.getOsmid());
			ps.setString(2, entidad.getCodine());
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
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