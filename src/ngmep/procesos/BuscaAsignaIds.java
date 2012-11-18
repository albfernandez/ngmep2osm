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

public class BuscaAsignaIds {
//    private static final String[] PLACES = new String[] { "city","town", "village", "hamlet", "suburb","isolated_dwelling"};


    private static int contador = -1;

    public static void buscaOsmId () throws SQLException, ClassNotFoundException, IOException{
        String query  = EntidadDAO.QUERY_BASE + " where osmid is  null and estado_robot = 0 and estado_manual = 0  " ;

        ResultSet rs = null;
        Statement stmt = null;
        List<Entidad> entidades = new ArrayList<Entidad>(); 
        try {
        	stmt = Database.getConnection().createStatement();
        	rs = stmt.executeQuery(query);
        	entidades = EntidadDAO.getInstance().getListFromRs(rs);
        }
        finally {
        	if (rs != null) {
        		rs.close();
        	}
        	if (stmt != null) {
        		stmt.close();
        	}
        }
        List<Entity> entidadesOsm = new ArrayList<Entity>();
        List<Entity> entidadesIne = new ArrayList<Entity>();
        List<Entity> entidadesActualizadas = new ArrayList<Entity>();
        for (Entidad entidad: entidades) {            
            List<Node> nodos = NodeDAO.getInstance().getPoblaciones(entidad.getLon(), entidad.getLat(), 0.02);
            for (Node nodo : nodos){
                String nombreOsm = null;
                if (nodo.containsTag( "name")){
                    nombreOsm = nodo.getTag("name");
                }
                else if (nodo.containsTag("name:es")){
                    nombreOsm = nodo.getTag("name:es");
                }
                Log.log("INE:" + entidad.getCodine() + ":" + entidad.getName());
                Log.log("OSM:" + nodo.getId() + ":" + nombreOsm);
                
                if (entidad.getCodine().equals(nodo.getTag("ref:ine"))){
                	actualizarIguales(entidad, nodo);
                }                
                if (ComparaCadenas.iguales(entidad.getName(), nombreOsm)){
                   actualizarIguales(entidad, nodo); 
                }
                else {
                    if (!entidadesOsm.contains(nodo)){
                        entidadesOsm.add(nodo);
                        Node ine = new Node();
                        ine.setId(contador--);
                        entidad.setDecisionNombre("OSM");
                        Objetivo3.actualiza(ine, entidad);           
                        ine.setModified(true);
                        if (!entidadesIne.contains(ine)){
                            entidadesIne.add(ine);
                        }
                        Node x = NodeDAO.getInstance().getNode(nodo.getId());
                        Objetivo3.actualiza(x, entidad);
                        if (!entidadesActualizadas.contains(x)){
                            entidadesActualizadas.add(x);
                        }
                    }
                }
            }            
        }
        
        Log.log("Exportando entidades pendientes osm:" + entidadesOsm.size());
        OutputStream salida = new GZIPOutputStream(new FileOutputStream(Config.getOsmOutputFile("objetivo2.pendientes_osm"))); 
        XMLExporter.export(entidadesOsm, salida,true);
        salida.close();
        Log.log("Exportando entidades pendientes ine:" + entidadesIne.size());
        salida = new GZIPOutputStream(new FileOutputStream(Config.getOsmOutputFile("objetivo2.pendientes_ine"))); 
        XMLExporter.export(entidadesIne, salida,true);
        salida.close();
        salida = new GZIPOutputStream(new FileOutputStream(Config.getOsmOutputFile("objetivo2.subir"))); 
        XMLExporter.export(entidadesActualizadas, salida,true);
        salida.close();
    }
    
    private static void actualizarIguales(Entidad entidad, Node nodo) throws SQLException{
    	Log.log("Actualizando:" + entidad.getCodine() + ":" + nodo.getId()); 
        entidad.setOsmid(nodo.getId());
        EntidadDAO.getInstance().updateOsmId(entidad);
    }

}
