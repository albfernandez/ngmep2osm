package ngmep.osm.dao;

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

import java.sql.ResultSet;
import java.sql.SQLException;

import ngmep.osm.datamodel.Entity;

public class AbstractEntityDAO {

    public void initTags (final Entity entity, final ResultSet resultSet) throws SQLException {
        while (resultSet.next()){
            final String key = resultSet.getString("k");
            final String value = resultSet.getString("v");
            entity.setTag(key, value);
        }
    }
}
