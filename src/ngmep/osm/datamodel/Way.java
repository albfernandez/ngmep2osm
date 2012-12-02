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

import java.util.ArrayList;
import java.util.List;

public class Way extends Entity{
    private final transient List<Node> nodos = new ArrayList<Node>();
    public Way(){
        super();
    }
    public void addNode(final Node node) {
        this.nodos.add(node);
        setModified(true);
    }
    public List<Node> getNodes(){
        return this.nodos;
    }
    @Override
    public boolean equals(final Object obj) {
    	return super.equals(obj);
    }
    @Override
    public int hashCode() {
       	return super.hashCode();
    }
}
