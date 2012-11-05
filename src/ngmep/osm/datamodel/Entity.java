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

public class Entity {

    private long id;
    private User user;
    private int version;
    private long changeset;
    private long timestamp;
    private Map<String, String> tags = new HashMap<String, String>();
    private boolean modified = false;
    
    protected Entity(){
        super();
    }
    
    
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }


    public long getId() {
        return id;
    }
    public void setId(long id) {
        if (this.id != id) {
            this.id = id;
            setModified(true);
        }
    }
    public String getTag(String tagName) {
        return tags.get(tagName);
    }
    public void setTag(String tagName, String value) {
        if (value == null) {
            removeTag(tagName);
            return;
        }
        String valtrim =value.trim();
        if (!tags.containsKey(tagName) || !valtrim.equals(tags.get(tagName))){
            tags.put(tagName, valtrim);
            setModified(true);
        }
    }
    public void removeTag(String tagName){
        if (tags.containsKey(tagName)){
            tags.remove(tagName);
            setModified(true);
        }
    }
    
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
        setModified(true);
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        if (this.version != version){
            this.version = version;
            setModified(true);
        }
    }
    public long getChangeset() {
        return changeset;
    }
    public void setChangeset(long changeset) {
        if (this.changeset != changeset) {
            this.changeset = changeset;
            setModified(true);
        }
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        if (this.timestamp != timestamp){
            this.timestamp = timestamp;
            setModified(true);
        }
    }
    public int getNumTags(){
        return tags.size();
    }
    public Set<String> getTagKeys () {
        return  tags.keySet();
    }
    public boolean containsTag (String key){
        return tags.containsKey(key);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Entity other = (Entity) obj;
        if (id != other.id){
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