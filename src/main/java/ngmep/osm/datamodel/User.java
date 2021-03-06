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

public class User {

    private int id; //NOPMD
    private String name;
    
    
    public int getId() {
        return this.id;
    }
    public void setId(final int identifier) {
        this.id = identifier;
    }
    public String getName() {
        return this.name;
    }
    public void setName(final String name) {
        this.name = name;
    }
    
    
}


/*
CREATE TABLE users
(
  id integer NOT NULL,
  name text NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id )
)

*/