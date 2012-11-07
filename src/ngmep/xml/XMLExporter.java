package ngmep.xml;

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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ngmep.osm.datamodel.Entity;
import ngmep.osm.datamodel.Node;
import ngmep.osm.datamodel.Relation;
import ngmep.osm.datamodel.RelationMember;
import ngmep.osm.datamodel.Way;

import org.apache.commons.lang3.StringEscapeUtils;

public class XMLExporter {
    
    public static void export (Entity entity, OutputStream os) throws IOException {
        List<Entity> lista = new ArrayList<Entity>();
        lista.add(entity);
        export(lista, os, true);
    }


    public static void export (List<Entity> list, OutputStream os, boolean force) throws IOException{
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(os), "UTF-8"));
        writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
        writer.write("<osm version='0.6' generator='JOSM'>\n");
        for (Entity entity: list) {
            _export(entity, writer, force);
        }        
        writer.write("</osm>\n");       
        writer.close();
    }
    private static void _export (Entity entity, BufferedWriter writer, boolean force)throws IOException{
        if (entity instanceof Node){
            exportNode((Node)entity, writer,force);
        }
        if (entity instanceof Way) {
            exportWay((Way) entity, writer, force);
        }
        if (entity instanceof Relation) {
            exportRelation((Relation) entity, writer, force);
        }
    }
    private static void exportRelation(Relation relation, BufferedWriter writer, boolean force) throws IOException {
        if (!relation.isModified() && !force) {
            return;
        }
        for (RelationMember member: relation.getMembers()){
            _export (member.getEntity(), writer, true);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<relation");
        sb.append(" id='").append(relation.getId()).append("'");
        if (relation.isModified()){
            sb.append(" action='").append("modify").append("'");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sb.append(" timestamp='").append(sdf.format(new Date(relation.getTimestamp()))).append("'");
        if (relation.getUser() != null) {
            sb.append(" uid='").append(relation.getUser().getId()).append("'");
            sb.append(" user='").append(StringEscapeUtils.escapeXml(relation.getUser().getName())).append("'");
        }
        sb.append(" visible='true'");
        sb.append(" version='").append(relation.getVersion()).append("'");
        sb.append(" changeset='").append(relation.getChangeset()).append("'");
        if (relation.getMembers().size() > 0 || relation.getNumTags() > 0){
            sb.append(">\n");
            
            for (RelationMember member: relation.getMembers()){
                String tipo = "node";;
                if (member.getEntity() instanceof Node){
                    tipo = "node";
                }
                else if (member.getEntity() instanceof Way){
                    tipo = "way";
                }
                sb.append("    ");
                sb.append("<member type='").append(tipo).append("'");
                sb.append(" ref='").append(member.getEntity().getId()).append("'");
                sb.append(" role='").append(StringEscapeUtils.escapeXml(member.getRole())).append("'");
                sb.append(" />\n");
            }
            Set<String> tags = relation.getTagKeys();            
            for (String key: tags){
                sb.append("    ");
                sb.append("<tag");
                sb.append(" k='").append(StringEscapeUtils.escapeXml(key)).append("'");
                sb.append(" v='").append(StringEscapeUtils.escapeXml(relation.getTag(key))).append("'");
                sb.append(" />\n");
            }
            sb.append("</relation>\n");
        }
        else {
            sb.append("/>\n");
        }
        writer.write(sb.toString());
        /*
         <osm version="0.6" generator="OpenStreetMap server">
  <relation id="347824" visible="true" timestamp="2011-09-11T16:04:29Z" version="2" changeset="9272105" user="afernandez" uid="41263">
    <member type="node" ref="268521663" role="admin_centre"/>
    <member type="way" ref="45320004" role=""/>
    <member type="way" ref="45357913" role=""/>
    <member type="way" ref="45377654" role=""/>
    <member type="way" ref="45382632" role=""/>
    <member type="way" ref="45369688" role=""/>
    <tag k="admin_level" v="8"/>
    <tag k="boundary" v="administrative"/>
    <tag k="idee:name" v="Moratinos"/>
    <tag k="ine:municipio" v="34109"/>
    <tag k="is_in" v="Palencia, Castilla y León, Spain"/>
    <tag k="is_in:country" v="Spain"/>
    <tag k="is_in:province" v="Palencia"/>
    <tag k="is_in:region" v="Castilla y León"/>
    <tag k="name" v="Moratinos"/>
    <tag k="source" v="BDLL25, EGRN, Instituto Geográfico Nacional"/>
    <tag k="type" v="boundary"/>
    <tag k="wikipedia" v="es:Moratinos"/>
  </relation>

         */
    }
    private static void exportWay(Way way, BufferedWriter writer, boolean force) throws IOException {
        if (!way.isModified() && !force){
            return;
        }
        for (Node node : way.getNodes()){
            exportNode(node, writer,true);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        sb.append("<way");
        sb.append(" id='").append(way.getId()).append("'");
        if (way.isModified()){
            sb.append(" action='").append("modify").append("'");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sb.append(" timestamp='").append(sdf.format(new Date(way.getTimestamp()))).append("'");
        if (way.getUser() != null) {
            sb.append(" uid='").append(way.getUser().getId()).append("'");
            sb.append(" user='").append(StringEscapeUtils.escapeXml(way.getUser().getName())).append("'");
        }
        sb.append(" visible='true'");
        sb.append(" version='").append(way.getVersion()).append("'");
        sb.append(" changeset='").append(way.getChangeset()).append("'");
        if (way.getNodes().size() > 0 || way.getNumTags() > 0){
            sb.append(">\n");
            for (Node node: way.getNodes()){
                sb.append("    ");
                sb.append("<nd");
                sb.append(" ref='").append(node.getId()).append("'");
                sb.append(" />\n");
            }
            Set<String> tags = way.getTagKeys();
            for (String key: tags){
                sb.append("    ");
                sb.append("<tag");
                sb.append(" k='").append(StringEscapeUtils.escapeXml(key)).append("'");
                sb.append(" v='").append(StringEscapeUtils.escapeXml(way.getTag(key))).append("'");
                sb.append(" />\n");
            }
            sb.append("  </way>\n");
        }
        else {
            sb.append("/>\n");
        }
        writer.write(sb.toString());

    }
    private static void exportNode(Node node, BufferedWriter writer, boolean force) throws IOException{
        if (!node.isModified() && !force) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        sb.append("<node");
        sb.append(" id='").append(node.getId()).append("'");
        if (node.isModified()){
            sb.append(" action='").append("modify").append("'");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sb.append(" timestamp='").append(sdf.format(new Date(node.getTimestamp()))).append("'");
        if (node.getUser() != null) {
            sb.append(" uid='").append(node.getUser().getId()).append("'");
            sb.append(" user='").append(StringEscapeUtils.escapeXml(node.getUser().getName())).append("'");
        }
        sb.append(" visble='true'");
        sb.append(" version='").append(node.getVersion()).append("'");
        sb.append(" changeset='").append(node.getChangeset()).append("'");
        sb.append(" lat='").append(node.getLat()).append("'");
        sb.append(" lon='").append(node.getLon()).append("'");
        
        if (node.getNumTags() > 0){
            sb.append(">\n");
            
            Set<String> tags = node.getTagKeys();
            for (String key: tags){
                sb.append("    ");
                sb.append("<tag");
                sb.append(" k='").append(StringEscapeUtils.escapeXml(key)).append("'");
                sb.append(" v='").append(StringEscapeUtils.escapeXml(node.getTag(key))).append("'");
                sb.append("/>\n");
            }
            sb.append("  </node>\n");
            
        }
        else {
            sb.append("/>\n");
        }
        
        
       
        
        writer.write(sb.toString());
    }
}

/*
  <way id='45975724' action='modify' timestamp='2009-12-11T00:03:23Z' uid='5265' user='ivansanchez' visible='true' version='1' changeset='3345956'>
    <nd ref='585478760' />
    <nd ref='585478780' />
    <nd ref='585478785' />
    <nd ref='585478782' />
    <nd ref='585478787' />
    <nd ref='585478801' />
    <nd ref='585478771' />
    <nd ref='585478773' />
    <nd ref='585478775' />
    <nd ref='585478776' />
    <nd ref='585478832' />
    <nd ref='585478834' />
    <nd ref='585478836' />
    <nd ref='585478838' />
    <nd ref='585478842' />
    <nd ref='585478844' />
    <nd ref='585479100' />
    <nd ref='585478846' />
    <nd ref='585478895' />
    <nd ref='585478903' />
    <nd ref='585478905' />
    <nd ref='585479025' />
    <nd ref='585479113' />
    <nd ref='585479115' />
    <nd ref='585479122' />
    <nd ref='585479128' />
    <nd ref='585479129' />
    <nd ref='585479136' />
    <nd ref='585479137' />
    <nd ref='585479074' />
    <nd ref='585479076' />
    <nd ref='585479077' />
    <nd ref='585479078' />
    <nd ref='585479065' />
    <nd ref='585479069' />
    <nd ref='585479070' />
    <nd ref='585479059' />
    <nd ref='585479050' />
    <nd ref='585479051' />
    <nd ref='585478980' />
    <nd ref='585478967' />
    <nd ref='585478969' />
    <nd ref='585478971' />
    <nd ref='585478924' />
    <nd ref='585478928' />
    <nd ref='585478930' />
    <nd ref='585478932' />
    <nd ref='585478934' />
    <nd ref='585478760' />
    <tag k='landuse' v='residential' />
    <tag k='name' v='Blascosancho' />
    <tag k='place' v='village' />
    <tag k='source' v='ITACyL' />
  </way>

 */
/*
<osm version='0.6' generator='JOSM'>
  <bounds minlat='42.345504999999996' minlon='-4.9165589' maxlat='42.3457072' maxlon='-4.9158883' origin='CGImap 0.0.2' />
  <node id='271241537' action='modify' timestamp='2011-08-09T21:00:44Z' uid='101355' user='ottokar55' visible='true' version='3' changeset='8971469' lat='42.3456062' lon='-4.9162025'>
    <tag k='highway' v='stop' />
    <tag k='is_in' v='Palencia, Castilla y León, Spain, Europe' />
    <tag k='name' v='San Martín de la Fuente' />
    <tag k='place' v='hamlet' />
  </node>
</osm>
*/