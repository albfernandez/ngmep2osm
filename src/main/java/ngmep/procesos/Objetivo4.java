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

import static ngmep.ngmep.datamodel.Constants.KEY_INE_MUNICIPIO;
import static ngmep.ngmep.datamodel.Constants.KEY_REF_INE;
import static ngmep.ngmep.datamodel.Constants.ROLE_ADMIN_CENTRE;
import static ngmep.ngmep.datamodel.Constants.ROLE_OUTER;

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
import ngmep.osm.dao.RelationDAO;
import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.datamodel.Relation;
import ngmep.osm.datamodel.RelationMember;
import ngmep.osm.log.Log;
import ngmep.xml.XMLExporter;

import org.apache.commons.lang3.StringUtils;

public final class Objetivo4 {


    
    
    private Objetivo4(){
    	// No instances
    }
    
    
    public static void ejecutaObjetivo4 () throws SQLException, ClassNotFoundException, IOException{
        String query  =  EntidadDAO.QUERY_BASE + " where osmid is not  null and admin_level in (4,6,7,8) and estado_4 = 0 ";
        query = query + "and cod_prov in ('35', '38')";

		List<Entidad> entidades = null;
		try (Statement statement = Database.getConnection().createStatement();
			ResultSet resultSet = statement.executeQuery(query);){			
			entidades = EntidadDAO.getInstance().getListFromRs(resultSet);
		} 
        final List<Entity> municipios = new ArrayList<Entity>();
        for (Entidad ine: entidades) {
            final String ineCapital = ine.getCodine();
            final String ineMunicipio = ine.getCodineMun();
            final String ineRelacion = ineMunicipio.substring(0, 5);
            final List<Relation> relaciones = RelationDAO.getInstance().getRelationsByTag(KEY_INE_MUNICIPIO, ineRelacion);
            if (relaciones.size() == 1){
                final Relation municipio = relaciones.get(0);
                final Entity localidad = getLocalidad(ine.getCodine());
                if (localidad != null) {
                    if (!municipio.containsMemberWithRole(ROLE_ADMIN_CENTRE)){
                        final RelationMember capital = new RelationMember();
                        capital.setEntity(localidad);
                        capital.setRole(ROLE_ADMIN_CENTRE);
                        municipio.addMember(capital);
                        municipios.add(municipio);
                        for (RelationMember miembro : municipio.getMembers()){
                            if (StringUtils.isBlank(miembro.getRole())){
                                miembro.setRole(ROLE_OUTER);                                
                            }
                        }
                        //marcarProcesado(ine);
                    }
                    else {
                        // Si ya tiene admin_centre no lo tocamos y lo dejamos como procesado
                        //marcarProcesado(ine);
                    }
                }
            }
            else {
            	Log.log( " se encontraron " + relaciones.size() + " limites para el municipio " + ineRelacion + "/" + ineCapital);
            }            
        }
        if (!municipios.isEmpty()){
            final String nombreArchivo = Config.getInstance().getOsmOutputFile("objetivo4.subir");
            
            try (final OutputStream salida = new GZIPOutputStream(new FileOutputStream(nombreArchivo));){ 
            	XMLExporter.export(municipios, salida, false);
            }
            Log.log("Generado el archivo objetivo4 (" + municipios.size() + "):"+nombreArchivo);
        }
        else {
        	Log.log("No se ha generado el archivo de objetivo4.");
        }
        
    }
    private static Entity getLocalidad(final String codine) throws SQLException {
        //Entity osm = NodeDAO.getInstance().getNode(id);
        
        final List<Node> localidades = NodeDAO.getInstance().getNodesByTag(KEY_REF_INE, codine);
        /*
         El validador de JOSM dice que no se puede poner admin_centre a un way,
         Asi que de momento desactivamos esto.
         
         if (osm == null){
            osm = WayDAO.getInstance().getWay(id);
        }*/
        Node osm = null;
        if (!localidades.isEmpty()) {
                if (localidades.size() > 1) {
                	Log.log("Nodo duplicado: " + codine);
                }
                osm = localidades.get(0);
        }
        return osm;
    }

//	private static void marcarProcesado(Entidad ine) throws SQLException {
//		String query = "update ngmep set estado_4 = 1 where cod_ine = ?";
//		PreparedStatement ps = null;
//		try {
//			ps = Database.getConnection().prepareStatement(query);
//			ps.setString(1, ine.getCodine());
//			ps.executeUpdate();
//		} finally {
//			if (ps != null) {
//				ps.close();
//			}
//		}
//	}
}