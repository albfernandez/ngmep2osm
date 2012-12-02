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

public class Node extends Entity{
    private double lat;
    private double lon;
    
    public Node () {
        super();
    }
    public double getLat() {
        return lat;
    }
    public void setLat(final double lat) {
        this.lat = lat;
        setModified(true);
    }
    public double getLon() {
        return lon;
    }
    public void setLon(final double lon) {
        this.lon = lon;
        setModified(true);
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
