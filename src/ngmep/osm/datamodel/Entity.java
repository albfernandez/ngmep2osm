package ngmep.osm.datamodel;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class Entity {

    private long id; //NOPMD
    private User user;
    private int version;
    private long changeset;
    private long timestamp;
    private final transient Map<String, String> tags = new HashMap<String, String>();
    private boolean modified = false;
    private static final int PRIME = 31;
    protected Entity(){
        super();
    }
    
    
    public boolean isModified() {
        return this.modified;
    }

    public void setModified(final boolean modified) {
        this.modified = modified;
    }


    public long getId() {
        return this.id;
    }
    public void setId(final long identifier) {
        if (this.id != identifier) {
            this.id = identifier;
            setModified(true);
        }
    }
    public String getTag(final String tagName) {
        return this.tags.get(tagName);
    }
    public void setTag(final String tagName, final String value) {
    	final String realTagName = StringUtils.trim(tagName);
    	final String valtrim =StringUtils.trim(value);
    	if (valtrim == null) {
            removeTag(realTagName);
            return;
        }
        
    	// Si el tag no existe o si existe pero es distinto
    	// Lo modificamos
        if (!this.tags.containsKey(realTagName) || !valtrim.equals(this.tags.get(realTagName))){
            this.tags.put(realTagName, valtrim);
            setModified(true);
        }
    }
    public void removeTag(final String tagName){
    	final String realTagName = StringUtils.trim(tagName);
    	if (StringUtils.isBlank(realTagName)){
    		return;
    	}
        if (this.tags.containsKey(realTagName)){
            this.tags.remove(realTagName);
            setModified(true);
        }
    }
    
    public User getUser() {
        return this.user;
    }
    public void setUser(final User user) {
        this.user = user;
        setModified(true);
    }
    public int getVersion() {
        return this.version;
    }
    public void setVersion(final int version) {
        if (this.version != version){
            this.version = version;
            setModified(true);
        }
    }
    public long getChangeset() {
        return this.changeset;
    }
    public void setChangeset(final long changeset) {
        if (this.changeset != changeset) {
            this.changeset = changeset;
            setModified(true);
        }
    }
    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(final long timestamp) {
        if (this.timestamp != timestamp){
            this.timestamp = timestamp;
            setModified(true);
        }
    }
    public int getNumTags(){
        return this.tags.size();
    }
    public Set<String> getTagKeys () {
        return  this.tags.keySet();
    }
    public boolean containsTag (final String key){
        return this.tags.containsKey(key);
    }


    @Override
    public int hashCode() {

        int result = 1;
        result = PRIME * result + (int) (this.id ^ (this.id >>> 32));
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        final Entity other = (Entity) obj;
        if (this.id != other.id){
            return false;
        }
        return true;
    }

    
    
    

}

/*

CREATE TABLE nodes
(
  id bigint NOT NULL,
  version integer NOT NULL,
  user_id integer NOT NULL,
  tstamp timestamp without time zone NOT NULL,
  changeset_id bigint NOT NULL,
  geom geometry,
  CONSTRAINT pk_nodes PRIMARY KEY (id ),
  CONSTRAINT enforce_dims_geom CHECK (st_ndims(geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(geom) = 'POINT'::text OR geom IS NULL),
  CONSTRAINT enforce_srid_geom CHECK (st_srid(geom) = 4326)
)


CREATE TABLE ways
(
  id bigint NOT NULL,
  version integer NOT NULL,
  user_id integer NOT NULL,
  tstamp timestamp without time zone NOT NULL,
  changeset_id bigint NOT NULL,
  CONSTRAINT pk_ways PRIMARY KEY (id )
)

CREATE TABLE relations
(
  id bigint NOT NULL,
  version integer NOT NULL,
  user_id integer NOT NULL,
  tstamp timestamp without time zone NOT NULL,
  changeset_id bigint NOT NULL,
  CONSTRAINT pk_relations PRIMARY KEY (id )
)




CREATE TABLE node_tags
(
  node_id bigint NOT NULL,
  k text NOT NULL,
  v text NOT NULL
)

CREATE TABLE way_tags
(
  way_id bigint NOT NULL,
  k text NOT NULL,
  v text
)

CREATE TABLE relation_tags
(
  relation_id bigint NOT NULL,
  k text NOT NULL,
  v text NOT NULL
)

CREATE TABLE way_nodes
(
  way_id bigint NOT NULL,
  node_id bigint NOT NULL,
  sequence_id integer NOT NULL,
  CONSTRAINT pk_way_nodes PRIMARY KEY (way_id , sequence_id )
)

CREATE TABLE relation_members
(
  relation_id bigint NOT NULL,
  member_id bigint NOT NULL,
  member_type character(1) NOT NULL,
  member_role text NOT NULL,
  sequence_id integer NOT NULL
)
*/