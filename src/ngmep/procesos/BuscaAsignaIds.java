package ngmep.procesos;

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

import static ngmep.ngmep.datamodel.Constants.KEY_CAPITAL;
import static ngmep.ngmep.datamodel.Constants.KEY_NAME;
import static ngmep.ngmep.datamodel.Constants.KEY_NAME_ES;
import static ngmep.ngmep.datamodel.Constants.KEY_REF_INE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import ngmep.config.Config;
import ngmep.ngmep.dao.EntidadDAO;
import ngmep.ngmep.dao.PoblacionDAO;
import ngmep.ngmep.datamodel.Entidad;
import ngmep.osm.dao.Database;
import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.log.Log;
import ngmep.xml.XMLExporter;

public final class BuscaAsignaIds {
//    private static final String[] PLACES = new String[] { "city","town", "village", "hamlet", "suburb","isolated_dwelling"};




    private BuscaAsignaIds() {
    	// No instances
    }

    /**
     * En la lista de entidades ine, busca su correspondiente osm (si existe).
     * 
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void buscaOsmId () throws SQLException, ClassNotFoundException, IOException{
        final String query  = 
        		EntidadDAO.QUERY_BASE + 
        		" where " +
        		"osmid is null " +
        		"and estado_robot = 0 " +
        		"and estado_manual = 0 " +
        		"and st_y(the_geom) > 1 " ;

        
       
        List<Entidad> entidades = null; 
        try (Statement  stmt = Database.getConnection().createStatement();
        	ResultSet resultSet = stmt.executeQuery(query);){
        	entidades = EntidadDAO.getInstance().getListFromRs(resultSet);
        }
        
        final List<Entity> entidadesIne = new ArrayList<Entity>();
        final List<Entity> entidadesCapitalesIne = new ArrayList<Entity>();
        for (Entidad entidad: entidades) {            
            final List<Entity> poblaciones = PoblacionDAO.getInstance().getPoblaciones(entidad.getLon(), entidad.getLat(), 0.03);
			boolean encontrado = false;
			for (Entity entity : poblaciones) {
				String nombreOsm = null;
				if (entity.containsTag(KEY_NAME)) {
					nombreOsm = entity.getTag(KEY_NAME);
				} else if (entity.containsTag(KEY_NAME_ES)) {
					nombreOsm = entity.getTag(KEY_NAME_ES);
				}

				if (entity.containsTag(KEY_REF_INE) && entidad.getCodine().equals(entity.getTag(KEY_REF_INE))) {
					actualizarIguales(entidad, entity);
					encontrado = true;
					break;
				} else if (!entity.containsTag(KEY_REF_INE) && ComparaCadenas.iguales(entidad.getName(), nombreOsm)) {
					actualizarIguales(entidad, entity);
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				Node ine = Objetivo3.entidad2node(entidad);
				ine.setModified(true);
				if (ine.containsTag(KEY_CAPITAL)){				
					entidadesCapitalesIne.add(ine);
				}
				else {
					entidadesIne.add(ine);
				}
			}
		}
        Log.log("Exportando entidades pendientes ine (objetivo 1) :" + entidadesCapitalesIne.size());
        if (!entidadesCapitalesIne.isEmpty()){
	        try (OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getInstance().getOsmOutputFile("objetivo1.pendientes_ine"))); ){
	             XMLExporter.export(entidadesCapitalesIne, salida,true);
	        }
        }
        Log.log("Exportando entidades pendientes ine (objetivo 2) :" + entidadesIne.size());
        if (!entidadesIne.isEmpty()){
	        try (OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getInstance().getOsmOutputFile("objetivo2.pendientes_ine"))); ){
	             XMLExporter.export(entidadesIne, salida,true);
	        }
        }
    }
    
    private static void actualizarIguales(final Entidad entidad, final Entity entity) throws SQLException{
        entidad.setOsmid(entity.getId());
        EntidadDAO.getInstance().updateOsmId(entidad);
    }

}
