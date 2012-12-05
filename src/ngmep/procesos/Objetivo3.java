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

import static ngmep.ngmep.datamodel.Constants.IGN;
import static ngmep.ngmep.datamodel.Constants.KEY_ADMIN_LEVEL;
import static ngmep.ngmep.datamodel.Constants.KEY_BAD_INE_REF;
import static ngmep.ngmep.datamodel.Constants.KEY_CAPITAL;
import static ngmep.ngmep.datamodel.Constants.KEY_ELE;
import static ngmep.ngmep.datamodel.Constants.KEY_FIXME;
import static ngmep.ngmep.datamodel.Constants.KEY_IS_IN_PROVINCE_CODE;
import static ngmep.ngmep.datamodel.Constants.KEY_LOC_NAME;
import static ngmep.ngmep.datamodel.Constants.KEY_NAME;
import static ngmep.ngmep.datamodel.Constants.KEY_OFFICIAL_NAME;
import static ngmep.ngmep.datamodel.Constants.KEY_OLD_NAME;
import static ngmep.ngmep.datamodel.Constants.KEY_PLACE;
import static ngmep.ngmep.datamodel.Constants.KEY_POPULATION;
import static ngmep.ngmep.datamodel.Constants.KEY_POPULATION_DATE;
import static ngmep.ngmep.datamodel.Constants.KEY_REF_INE;
import static ngmep.ngmep.datamodel.Constants.KEY_SOURCE;
import static ngmep.ngmep.datamodel.Constants.KEY_SOURCE_DATE;
import static ngmep.ngmep.datamodel.Constants.KEY_SOURCE_ELE;
import static ngmep.ngmep.datamodel.Constants.KEY_SOURCE_FILE;
import static ngmep.ngmep.datamodel.Constants.KEY_SOURCE_NAME;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
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
import ngmep.osm.dao.WayDAO;
import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.datamodel.Way;
import ngmep.osm.log.Log;
import ngmep.xml.XMLExporter;

import org.apache.commons.lang3.StringUtils;

public final class Objetivo3 {
    



	

    private static int iderror = 0;
    
    private Objetivo3(){
    	//No instance
    }
    
    private static String doubleToString(final double valor){
        return Long.toString((long) valor);
    }
    
    
    public static Entity entidad2node(final Entidad ine, final String error) {
        final Node osm = new Node();
        osm.setId(--iderror);
        osm.setLat(ine.getLat());
        osm.setLon(ine.getLon());
        osm.setTag(KEY_REF_INE, ine.getCodine());
        osm.setTag(KEY_PLACE, ine.getPlace());
        if (ine.getAltura() > 0){
            osm.setTag(KEY_ELE, doubleToString(ine.getAltura()));
            osm.setTag(KEY_SOURCE_ELE, ine.getSourceAltura());
        }
        if (ine.getPoblacion() >= 0){
            osm.setTag(KEY_POPULATION, doubleToString(ine.getPoblacion()));
            osm.setTag(KEY_POPULATION_DATE, "2009");
        }
        osm.setTag(KEY_SOURCE, "Instituto Geográfico Nacional");
        osm.setTag(KEY_SOURCE_DATE, "2011-06");
        osm.setTag(KEY_SOURCE_FILE, "http://centrodedescargas.cnig.es/CentroDescargas/equipamiento/BD_Municipios-Entidades.zip");
        osm.setTag(KEY_SOURCE_NAME, "Nomenclátor Geográfico de Municipios y Entidades de Población");
        if (ine.getAdministrativeLevel() > 0){
            osm.setTag(KEY_ADMIN_LEVEL, Integer.toString(ine.getAdministrativeLevel()));
            osm.setTag(KEY_CAPITAL, Integer.toString(ine.getAdministrativeLevel()));               
        }
        
        //if (!StringUtils.isBlank(ine.getNombreMun())){
        //    osm.setTag("is_in:municipality", ine.getNombreMun());
        //}
        osm.setTag(KEY_IS_IN_PROVINCE_CODE, ine.getCodigoProvincia());
        estableceNombresAlternativos(osm, ine);
        osm.setTag(KEY_NAME, ine.getName());
        if (!StringUtils.isBlank(error)) {
                osm.setTag(KEY_FIXME, error);
        }
        return osm;
    }
    
    public static String actualiza(final Entity osm, final Entidad ine) {
    	return actualiza(osm, ine, true);
    }
    
    public static String actualiza (final Entity osm, final Entidad ine, final boolean doLog) {
        /*
         * Para los casos que tienen ine:ref
         */
        if (osm.containsTag(KEY_BAD_INE_REF)) {
            osm.setTag(KEY_REF_INE, osm.getTag(KEY_BAD_INE_REF));
            osm.removeTag(KEY_BAD_INE_REF);
        }

        /*
         *  Si encontramos que ya tiene ref:ine
         *  si es igual continuamos importando campos.
         *  Si no es igual, seguimos con otro
         */
        if (osm.containsTag(KEY_REF_INE)){                
            if (!ine.getCodine().equals(osm.getTag(KEY_REF_INE))){
            	if (doLog){
            		Log.log("El elemento ya tiene REF_INE y NO COINCIDE:" + ine.getCodine() + "|" + osm.getTag(KEY_REF_INE));
            	}
                return "El elemento ya tiene REF_INE y NO COINCIDE";
            }
            /*
            if (osm.getUser() != null && "egrn".equals(osm.getUser().getName())){
                //Log.log("localidad ya actualizada por egrn:" + osm.getId());
                return false;
            }*/
            
        }
        // Ponemos el ref:ine siempre
        osm.setTag(KEY_REF_INE, ine.getCodine());
        
        // Si no tiene place se lo ponemos 
        // Si tiene place, solo sobreescribimos si es village
        // (village es el que se pone siempre en caso de duda)
        if (!osm.containsTag(KEY_PLACE)){
            osm.setTag(KEY_PLACE, ine.getPlace());
        }
        // FIXME Desactivado en segundas pasadas
        //else if ("village".equals(osm.getTag("place"))){
         //   osm.setTag("place", ine.getPlace());
        //}

        // Si tenemos dato de altura, pero el nodo osm no, se lo rellenamos
        if (!osm.containsTag(KEY_ELE) && ine.getAltura() > 0){
            osm.setTag(KEY_ELE, doubleToString(ine.getAltura()));
            osm.setTag(KEY_SOURCE_ELE, ine.getSourceAltura());
        }
        
        /*
         * Si no tenemos poblacion se lo rellenamos.
         * Si tenemos poblacion y fecha, comprobamos la fecha y actualizamos 
         * solo si es anterior a 2009
         */
        if (ine.getPoblacion() >= 0){
            if (!osm.containsTag(KEY_POPULATION)){
                osm.setTag(KEY_POPULATION, doubleToString(ine.getPoblacion()));
                osm.setTag(KEY_POPULATION_DATE, "2009");
            }
            else if (osm.containsTag(KEY_POPULATION_DATE) && osm.getTag(KEY_POPULATION_DATE).matches("^[0-9]+$")){
                if (Integer.parseInt(osm.getTag(KEY_POPULATION_DATE)) < 2009){
                    osm.setTag("population", doubleToString(ine.getPoblacion()));
                    osm.setTag(KEY_POPULATION_DATE, "2009");
                }                
            }
        }
        
        // Rellenamos source,
        if (!osm.containsTag(KEY_SOURCE)){
            osm.setTag(KEY_SOURCE, IGN);
        }
        else if (!osm.getTag(KEY_SOURCE).contains(IGN) ){
            osm.setTag(KEY_SOURCE, osm.getTag(KEY_SOURCE)+";"+ "Instituto Geográfico Nacional");
        }
        osm.setTag(KEY_SOURCE_DATE, "2011-06");
        osm.setTag(KEY_SOURCE_FILE, "http://centrodedescargas.cnig.es/CentroDescargas/equipamiento/BD_Municipios-Entidades.zip");
        osm.setTag(KEY_SOURCE_NAME, "Nomenclátor Geográfico de Municipios y Entidades de Población");
        
        
        // Si es capital, rellenamos esos datos siempre
        // FIXME Antes se rellenaba siempre, ahora solo si no están ya rellenos
        if (ine.getAdministrativeLevel() > 0 && !osm.containsTag(KEY_ADMIN_LEVEL) && !osm.containsTag(KEY_CAPITAL)){
            osm.setTag(KEY_ADMIN_LEVEL, Integer.toString(ine.getAdministrativeLevel()));
            osm.setTag(KEY_CAPITAL, Integer.toString(ine.getAdministrativeLevel()));               
        }
        // Si tenemos el nombre del muncipio al que pertenece rellenamos
//        if (!StringUtils.isBlank(ine.getNombreMun())){
//            osm.setTag("is_in:municipality", ine.getNombreMun());
//        }
        // Rellenamos la provincia a la que pertenece siempre.
        osm.setTag(KEY_IS_IN_PROVINCE_CODE, ine.getCodigoProvincia());

        // Con esto establecemos los nombres en otros idiomas, nombres antiguos
        // nombre alternativos, etc
        estableceNombresAlternativos(osm, ine);
        
        // Establecemos el nombre "name"
        //String osmName = osm.getTag("name");
        //String ineName = ine.getName();
        if (ajustarNombre(osm, ine)){
            return "";
        }
        if (doLog) {
        	Log.log("INE:" + ine.getCodine() + " No se que hacer con la decision de nombre [" + ine.getDecisionNombre() +"]"+"osm:" + osm.getTag(KEY_NAME) + " ine:" + ine.getName());
        }
        return "No se que hacer con la decision de nombre [" + ine.getDecisionNombre() +"]";
        
    }

    
    public static void ejecutaObjetivo3 () throws SQLException, ClassNotFoundException, IOException{
        final String query  = EntidadDAO.QUERY_BASE + " where  estado_manual >= 0 and estado_robot = 0";
		List<Entidad> entidades = null;
		try (Statement statement = Database.getConnection().createStatement();
			ResultSet resultSet = statement.executeQuery(query);
				){
			entidades = EntidadDAO.getInstance().getListFromRs(resultSet);
		} 
        final List<Entity> nodos = new ArrayList<Entity>();
        final List<Entity> errores = new ArrayList<Entity>();
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
                //Log.log(error);
            }
            else if (osm == null) {
                error = "El osmid asignado no existe: " + ine.getOsmid();
                Log.log(error);
            }
            else if (ine.getEstadoManual() != 0) {
                error = "Estado manual " + ine.getEstadoManual();
            }
            else if (distanciaExcesiva (ine, osm)){
                error ="Distancia excesiva entre los nodos";
                Log.log(error);
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
            	//Log.log(error);
                errores.add(entidad2node(ine, error));
            }

        
        }
        
        if (!nodos.isEmpty()){
            final String nombreArchivo = Config.getInstance().getOsmOutputFile("objetivo3.subir");
            try (final OutputStream salida = new GZIPOutputStream(new FileOutputStream(nombreArchivo));){ 
            	XMLExporter.export(nodos, salida, false);
            }
            Log.log("Generado el archivo (" + nodos.size() + "):"+nombreArchivo);
        }
        else {
        	Log.log("No se ha generado el archivo.");
        }
        if (!errores.isEmpty()) {
            final String nombreArchivo = Config.getInstance().getOsmOutputFile("objetivo3.errores");
            try (final OutputStream salida = new GZIPOutputStream(new FileOutputStream(nombreArchivo));){ 
            	XMLExporter.export(errores, salida, true);
            }
            Log.log("Generado el archivo(" + errores.size() + "):"+nombreArchivo);
        }
        
    }
    private static boolean distanciaExcesiva(final Entidad ine, final Entity osm) {
        try {
            Node nodo = null;
            if (osm instanceof Node){
                nodo  = (Node) osm;                
            }
            else if (osm instanceof Way){
                nodo = ((Way) osm).getNodes().get(0);
            }
            else {
            	throw new IllegalArgumentException("osm must be a way or a node");
            }
            final double difLatitud = Math.abs(ine.getLat() - nodo.getLat());
            final double difLongitud = Math.abs(ine.getLon() - nodo.getLon());
            
            return (difLatitud > 0.5 || difLongitud > 0.5);
        }
        catch (Exception e){
            return false;
        }

    }

    private static Entity getLocalidadOsm(final long osmid) throws SQLException {
        Entity osm = NodeDAO.getInstance().getNode(osmid);
        if (osm == null || !esLocalidad(osm)){
            osm = WayDAO.getInstance().getWay(osmid);
            if (osm != null && !esLocalidad(osm)){
                return null;
            }
        }
        return osm;
    }
    private static boolean esLocalidad(final Entity osm) {
        return osm.containsTag(KEY_PLACE) && osm.containsTag(KEY_NAME); 
    }

	private static void marcarProcesado(final Entidad ine) throws SQLException {
		String query = "update ngmep set estado_robot = 1 where cod_ine = ?";
		try (PreparedStatement statement = Database.getConnection().prepareStatement(query); ){			
			statement.setString(1, ine.getCodine());
			statement.executeUpdate();
		} 
	}

    private static void estableceNombresAlternativos (final Entity osm, final Entidad ine){
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
        if (!osm.containsTag(KEY_LOC_NAME) && !StringUtils.isBlank(ine.getLocName())) {
            osm.setTag(KEY_LOC_NAME, ine.getLocName());
        }
        if (!osm.containsTag(KEY_OFFICIAL_NAME) && !StringUtils.isBlank(ine.getNombreOficial())) {
            osm.setTag(KEY_OFFICIAL_NAME, ine.getNombreOficial());
        }

        if (!osm.containsTag(KEY_OLD_NAME) && !StringUtils.isBlank(ine.getNombreAntiguo())) {
            osm.setTag(KEY_OLD_NAME, ine.getNombreAntiguo());
        }
    }

    
    private static boolean ajustarNombre(final Entity osm, final Entidad ine) {
        final String nombre = StringUtils.trim(osm.getTag(KEY_NAME));        
        if (StringUtils.isBlank(nombre)) {
        	/*if (osm.getId() > 0){
        		Log.log("osm sin nombre " + osm.getId());
        	}
        	*/
        	osm.setTag(KEY_NAME, ine.getName());
            return true;
        }
        
        if ("OSM".equals(ine.getDecisionNombre())){
            return true;
        }
        if ("INE".equals(ine.getDecisionNombre())){
            osm.setTag(KEY_NAME,ine.getName());
            return true;
        }
        if ("AUTO".equals(ine.getDecisionNombre())){
        	if (nombre.equalsIgnoreCase(ine.getName())){
        		return true;
        	}
            if (ComparaCadenas.igualesObjetivo3(ine.getName(), nombre)){
                osm.setTag(KEY_NAME,ine.getName());
                return true;
            }
            if (ComparaCadenas.prefijoDeN2(ine.getName(), nombre)){
                osm.setTag(KEY_NAME, nombre);
                return true;
            }
            if (ComparaCadenas.prefijoDeN2(nombre, ine.getName())){
                osm.setTag(KEY_NAME, ine.getName());
                return true;
            }
            
            if (nombre.indexOf('/') > 0){
            	String[] partes = nombre.split("/");
            	for (String parte: partes) {
            		if (!StringUtils.isBlank(parte) && ComparaCadenas.igualesObjetivo3(ine.getName(), parte) ){
            			osm.setTag(KEY_NAME, nombre);
            			return true;
            		}
            	}
            }
                
            return false;            
        }
        
        // Puesto el nombre en la tabla (valor != INE, OSM, AUTO)
        osm.setTag(KEY_NAME,ine.getDecisionNombre());
        return true;            
    }

}
