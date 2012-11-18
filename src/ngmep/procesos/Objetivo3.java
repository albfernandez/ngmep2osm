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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import ngmep.config.Config;
import ngmep.ngmep.dao.EntidadDAO;
import ngmep.ngmep.datamodel.Entidad;
import ngmep.osm.dao.Database;
import ngmep.osm.dao.NodeDAO;
import ngmep.osm.dao.WayDAO;
import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.datamodel.Way;
import ngmep.osm.log.Log;
import ngmep.xml.XMLExporter;

import org.apache.commons.lang3.StringUtils;

public class Objetivo3 {
    
	public static final String INSTITUTO_GEGORAFICO = "Instituto Geográfico Nacional";
	
	private static final Locale LOCALE_ES = new Locale("ES", "es");
    private static int iderror = 0;
    private static String doubleToString(double d){
        return Long.toString(new Double(d).longValue());
    }
    
    
    public static Entity entidad2node(Entidad ine, String error) {
        Node osm = new Node();
        osm.setId(--iderror);
        osm.setLat(ine.getLat());
        osm.setLon(ine.getLon());
        osm.setTag("ref:ine", ine.getCodine());
        osm.setTag("place", ine.getPlace());
        if (ine.getAltura() > 0){
            osm.setTag("ele", doubleToString(ine.getAltura()));
            osm.setTag("source:ele", ine.getSourceAltura());
        }
        if (ine.getPoblacion() >= 0){
            osm.setTag("population", doubleToString(ine.getPoblacion()));
            osm.setTag("population:date", "2009");
        }
        osm.setTag("source", "Instituto Geográfico Nacional");
        osm.setTag("source:date", "2011-06");
        osm.setTag("source:file", "http://centrodedescargas.cnig.es/CentroDescargas/equipamiento/BD_Municipios-Entidades.zip");
        osm.setTag("source:name", "Nomenclátor Geográfico de Municipios y Entidades de Población");
        if (ine.getAdministrativeLevel() > 0){
            osm.setTag("admin_level", Integer.toString(ine.getAdministrativeLevel()));
            osm.setTag("capital", Integer.toString(ine.getAdministrativeLevel()));               
        }
        
        //if (!StringUtils.isBlank(ine.getNombreMun())){
        //    osm.setTag("is_in:municipality", ine.getNombreMun());
        //}
        osm.setTag("is_in:province_code", ine.getCodigoProvincia());
        estableceNombresAlternativos(osm, ine);
        osm.setTag("name", ine.getName());
        if (!StringUtils.isBlank(error)) {
                osm.setTag("fixme", error);
        }
        return osm;
    }
    
    public static String actualiza (Entity osm, Entidad ine) {
        /*
         * Para los casos que tienen ine:ref
         */
        if (osm.containsTag("ine:ref")) {
            osm.setTag("ref:ine", osm.getTag("ine:ref"));
            osm.removeTag("ine:ref");
        }

        /*
         *  Si encontramos que ya tiene ref:ine
         *  si es igual continuamos importando campos.
         *  Si no es igual, seguimos con otro
         */
        if (osm.containsTag("ref:ine")){                
            if (!ine.getCodine().equals(osm.getTag("ref:ine"))){
            	Log.log("El elemento ya tiene REF_INE y NO COINCIDE:" + ine.getCodine() + "|" + osm.getTag("ref:ine"));
                return "El elemento ya tiene REF_INE y NO COINCIDE";
            }
            /*
            if (osm.getUser() != null && "egrn".equals(osm.getUser().getName())){
                //Log.log("localidad ya actualizada por egrn:" + osm.getId());
                return false;
            }*/
            
        }
        // Ponemos el ref:ine siempre
        osm.setTag("ref:ine", ine.getCodine());
        
        // Si no tiene place se lo ponemos 
        // Si tiene place, solo sobreescribimos si es village
        // (village es el que se pone siempre en caso de duda)
        if (!osm.containsTag("place")){
            osm.setTag("place", ine.getPlace());
        }
        // FIXME Desactivado en segundas pasadas
        //else if ("village".equals(osm.getTag("place"))){
         //   osm.setTag("place", ine.getPlace());
        //}

        // Si tenemos dato de altura, pero el nodo osm no, se lo rellenamos
        if (!osm.containsTag("ele") && ine.getAltura() > 0){
            osm.setTag("ele", doubleToString(ine.getAltura()));
            osm.setTag("source:ele", ine.getSourceAltura());
        }
        
        /*
         * Si no tenemos poblacion se lo rellenamos.
         * Si tenemos poblacion y fecha, comprobamos la fecha y actualizamos 
         * solo si es anterior a 2009
         */
        if (ine.getPoblacion() >= 0){
            if (!osm.containsTag("population")){
                osm.setTag("population", doubleToString(ine.getPoblacion()));
                osm.setTag("population:date", "2009");
            }
            else if (osm.containsTag("population:date") && osm.getTag("population:date").matches("^[0-9]+$")){
                if (Integer.parseInt(osm.getTag("population:date")) < 2009){
                    osm.setTag("population", doubleToString(ine.getPoblacion()));
                    osm.setTag("population:date", "2009");
                }                
            }
        }
        
        // Rellenamos source,
        if (!osm.containsTag("source")){
            osm.setTag("source", INSTITUTO_GEGORAFICO);
        }
        else if (!osm.getTag("source").contains(INSTITUTO_GEGORAFICO) ){        
            osm.setTag("source", osm.getTag("source")+";"+ "Instituto Geográfico Nacional");
        }
        osm.setTag("source:date", "2011-06");
        osm.setTag("source:file", "http://centrodedescargas.cnig.es/CentroDescargas/equipamiento/BD_Municipios-Entidades.zip");
        osm.setTag("source:name", "Nomenclátor Geográfico de Municipios y Entidades de Población");
        
        
        // Si es capital, rellenamos esos datos siempre
        // FIXME Antes se rellenaba siempre, ahora solo si no están ya rellenos
        if (ine.getAdministrativeLevel() > 0 && !osm.containsTag("admin_level") && !osm.containsTag("capital")){
            osm.setTag("admin_level", Integer.toString(ine.getAdministrativeLevel()));
            osm.setTag("capital", Integer.toString(ine.getAdministrativeLevel()));               
        }
        // Si tenemos el nombre del muncipio al que pertenece rellenamos
//        if (!StringUtils.isBlank(ine.getNombreMun())){
//            osm.setTag("is_in:municipality", ine.getNombreMun());
//        }
        // Rellenamos la provincia a la que pertenece siempre.
        osm.setTag("is_in:province_code", ine.getCodigoProvincia());

        // Con esto establecemos los nombres en otros idiomas, nombres antiguos
        // nombre alternativos, etc
        estableceNombresAlternativos(osm, ine);
        
        // Establecemos el nombre "name"
        String osmName = osm.getTag("name");
        String ineName = ine.getName();
        if (ajustarNombre(osm, ine)){
            return "";
        }
        Log.log("INE:" + ine.getCodine() + " No se que hacer con la decision [" + ine.getDecisionNombre() +"]");
        Log.log("osm:" + osm.getTag("name") + " ine:" + ine.getName());
        return "No se que hacer con la decision [" + ine.getDecisionNombre() +"]";
        
    }

    
    public static void ejecutaObjetivo3 () throws SQLException, ClassNotFoundException, IOException{
        String query  = EntidadDAO.QUERY_BASE + " where  estado_manual >= 0 and estado_robot = 0";
        ResultSet rs = Database.getConnection().createStatement().executeQuery(query);
        List<Entidad> entidades = EntidadDAO.getInstance().getListFromRs(rs);
        rs.close();
        List<Entity> nodos = new ArrayList<Entity>();
        List<Entity> errores = new ArrayList<Entity>();
        String error = "";
        for (Entidad ine: entidades) {
            Entity osm = null;
            if (ine.getLat() < 1){
            	continue;
            }
            if (ine.getOsmid() > 0){
                osm = getLocalidadOsm(ine.getOsmid());

            }
            if (ine.getOsmid() <= 0){
                error = "No tiene asignado osmid:" + ine.getCodine();
            }
            else if (osm == null) {
                error = "El osmid asignado no existe: " + ine.getOsmid();
            }
            else if (ine.getEstadoManual() != 0) {
                error = "Estado manual " + ine.getEstadoManual();
            }
            else if (distanciaExcesiva (ine, osm)){
                error ="Distancia excesiva entre los nodos";
            }            
            else {
                error = actualiza(osm, ine); 
                if (StringUtils.isBlank(error)) {        
                    if (osm.isModified()) {
                        nodos.add(osm);
                    }
                    marcarProcesado(ine);
                }
            }
            if (!StringUtils.isBlank(error)) {
            	Log.log(error);
                errores.add(entidad2node(ine, error));
            }

        
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fecha = sdf.format(new Date());
        if (nodos.size() > 0){
            String nombreArchivo = Config.getOsmDir() + "/ine/subir.objetivo3." +fecha+  ".osm.gz";
            OutputStream salida = new GZIPOutputStream(new FileOutputStream(nombreArchivo)); 
            XMLExporter.export(nodos, salida, false);
            salida.close();
            Log.log("Generado el archivo (" + nodos.size() + "):"+nombreArchivo);
        }
        else {
        	Log.log("No se ha generado el archivo.");
        }
        if (errores.size() > 0) {
            String nombreArchivo = Config.getOsmDir() + "/ine/errores.objetivo3." +fecha+  ".osm.gz";
            OutputStream salida = new GZIPOutputStream(new FileOutputStream(nombreArchivo)); 
            XMLExporter.export(errores, salida, true);
            salida.close();
            Log.log("Generado el archivo(" + errores.size() + "):"+nombreArchivo);
        }
        
    }
    private static boolean distanciaExcesiva(Entidad ine, Entity osm) {
        try {
            Node nodo = null;
            if (osm instanceof Node){
                nodo  = (Node) osm;                
            }
            else if (osm instanceof Way){
                nodo = ((Way) osm).getNodes().get(0);
            }
            double difLatitud = Math.abs(ine.getLat() - nodo.getLat());
            double difLongitud = Math.abs(ine.getLon() - nodo.getLon());
            
            return (difLatitud > 0.5 || difLongitud > 0.5);
        }
        catch (Exception e){
            return false;
        }

    }

    private static Entity getLocalidadOsm(long osmid) throws SQLException {
        Entity osm = NodeDAO.getInstance().getNode(osmid);
        if (osm == null || !esLocalidad(osm)){
            osm = WayDAO.getInstance().getWay(osmid);
            if (osm != null && !esLocalidad(osm)){
                osm = null;
            }
        }
        return osm;
    }
    private static boolean esLocalidad(Entity osm) {
        return osm.containsTag("place") && osm.containsTag("name"); 
    }

    private static void marcarProcesado(Entidad ine) throws SQLException {
        String query = "update ngmep set estado_robot = 1 where cod_ine = ?";
        PreparedStatement ps = Database.getConnection().prepareStatement(query);
        ps.setString(1, ine.getCodine());
        ps.executeUpdate(); 
        ps.close();
    }

    private static void estableceNombresAlternativos (Entity osm, Entidad ine){
        if (!StringUtils.isBlank(ine.getLan1()) && !StringUtils.isBlank(ine.getName1())) {
            String lan = "name:" + ine.getLan1();
            if (!osm.containsTag(lan)) {
                osm.setTag(lan, ine.getName1());
            }
        }
        if (!StringUtils.isBlank(ine.getLan2()) && !StringUtils.isBlank(ine.getName2())) {
            String lan = "name:" + ine.getLan2();
            if (!osm.containsTag(lan)) {
                osm.setTag(lan, ine.getName2());
            }
        }

//        if (!osm.containsTag("alt_name") && !StringUtils.isBlank(ine.getNombreAlternativo())) {
//            osm.setTag("alt_name", ine.getNombreAlternativo());
//        }
        if (!osm.containsTag("loc_name") && !StringUtils.isBlank(ine.getLocName())) {
            osm.setTag("loc_name", ine.getLocName());
        }
        if (!osm.containsTag("official_name") && !StringUtils.isBlank(ine.getNombreOficial())) {
            osm.setTag("official_name", ine.getNombreOficial());
        }

        if (!osm.containsTag("old_name") && !StringUtils.isBlank(ine.getNombreAntiguo())) {
            osm.setTag("old_name", ine.getNombreAntiguo());
        }
    }

    
    private static boolean ajustarNombre(Entity osm, Entidad ine) {
        String nombre = osm.getTag("name");        
        if (StringUtils.isBlank(nombre)) {
        	Log.log("osm sin nombre " + osm.getId());
            return false;
        }
        
        if ("OSM".equals(ine.getDecisionNombre())){
            return true;
        }
        if ("INE".equals(ine.getDecisionNombre())){
            osm.setTag("name",ine.getName());
            return true;
        }
        if ("AUTO".equals(ine.getDecisionNombre())){
        	if (nombre.equalsIgnoreCase(ine.getName())){
        		return true;
        	}
            if (ComparaCadenas.igualesObjetivo3(ine.getName(), nombre)){
                osm.setTag("name",ine.getName());
                return true;
            }
            if (ComparaCadenas.prefijoDeN2(ine.getName(), nombre)){
                osm.setTag("name", nombre);
                return true;
            }
            if (ComparaCadenas.prefijoDeN2(nombre, ine.getName())){
                osm.setTag("name", ine.getName());
                return true;
            }
                
            return false;            
        }
        osm.setTag("name",ine.getDecisionNombre());
        return true;            
    }

}
