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
import ngmep.ngmep.datamodel.Entidad;
import ngmep.osm.dao.Database;
import ngmep.osm.dao.NodeDAO;
import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.log.Log;
import ngmep.xml.XMLExporter;

public final class BuscaAsignaIds {
//    private static final String[] PLACES = new String[] { "city","town", "village", "hamlet", "suburb","isolated_dwelling"};


    private static int contador = -1;

    private BuscaAsignaIds() {
    	// No instances
    }

    public static void buscaOsmId () throws SQLException, ClassNotFoundException, IOException{
        final String query  = EntidadDAO.QUERY_BASE + " where osmid is  null and estado_robot = 0 and estado_manual = 0  " ;

        
       
        List<Entidad> entidades = null; 
        try (Statement  stmt = Database.getConnection().createStatement();
        	ResultSet resultSet = stmt.executeQuery(query);){        	
        	entidades = EntidadDAO.getInstance().getListFromRs(resultSet);
        }
        
        final List<Entity> entidadesOsm = new ArrayList<Entity>();
        final List<Entity> entidadesIne = new ArrayList<Entity>();
        final List<Entity> actualizadas = new ArrayList<Entity>();
        for (Entidad entidad: entidades) {            
            final List<Node> nodos = NodeDAO.getInstance().getPoblaciones(entidad.getLon(), entidad.getLat(), 0.02);
            for (Node nodo : nodos){
                String nombreOsm = null;
                if (nodo.containsTag(KEY_NAME)){
                    nombreOsm = nodo.getTag(KEY_NAME);
                }
                else if (nodo.containsTag(KEY_NAME_ES)){
                    nombreOsm = nodo.getTag(KEY_NAME_ES);
                }
                //Log.log("INE:" + entidad.getCodine() + ":" + entidad.getName());
                //Log.log("OSM:" + nodo.getId() + ":" + nombreOsm);
                
                if (entidad.getCodine().equals(nodo.getTag(KEY_REF_INE))){
                	actualizarIguales(entidad, nodo);
                }                
                if (ComparaCadenas.iguales(entidad.getName(), nombreOsm)){
                   actualizarIguales(entidad, nodo); 
                }
                else {
                    if (!entidadesOsm.contains(nodo)){
                        entidadesOsm.add(nodo);
                        final Node ine = new Node();
                        ine.setId(contador--);
                        entidad.setDecisionNombre("OSM");
                        Objetivo3.actualiza(ine, entidad, false);           
                        ine.setModified(true);
                        if (!entidadesIne.contains(ine)){
                            entidadesIne.add(ine);
                        }
                        final Node nodo2 = NodeDAO.getInstance().getNode(nodo.getId());
                        Objetivo3.actualiza(nodo2, entidad, false);
                        if (!actualizadas.contains(nodo2) && nodo2.isModified()){
                            actualizadas.add(nodo2);
                        }
                    }
                }
            }            
        }
        
        Log.log("Exportando entidades pendientes osm:" + entidadesOsm.size());
        try (OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getInstance().getOsmOutputFile("objetivo2.pendientes_osm")));){ 
        	XMLExporter.export(entidadesOsm, salida,true);
        }
        Log.log("Exportando entidades pendientes ine:" + entidadesIne.size());
        try (OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getInstance().getOsmOutputFile("objetivo2.pendientes_ine"))); ){
             XMLExporter.export(entidadesIne, salida,true);
        }
        Log.log("Exportando entidades a actualizar:" + actualizadas.size());
        try (OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getInstance().getOsmOutputFile("objetivo2.subir")));){ 
        	XMLExporter.export(actualizadas, salida,true);
        }
    }
    
    private static void actualizarIguales(final Entidad entidad, final Node nodo) throws SQLException{
    	//Log.log("Actualizando:" + entidad.getCodine() + ":" + nodo.getId()); 
        entidad.setOsmid(nodo.getId());
        EntidadDAO.getInstance().updateOsmId(entidad);
    }

}
