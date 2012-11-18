package ngmep.config;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Config {
    
	private static String fecha="";
	static {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        fecha = sdf.format(new Date());
        String home = System.getProperty("user.home");
        File osm = new File(home + File.separator + "osm");
        if (!osm.exists()){
        	osm.mkdir();
        }
        File ine = new File(osm, "ine");
        if (!ine.exists()){
        	ine.mkdir();
        }
        File log = new File (ine, "log");
        if (!log.exists()){
        	log.mkdir();
        }
	}
    public static Properties getConfigProperties () throws IOException{
        Properties config = new Properties();
        FileInputStream fis = new FileInputStream(
                 getOsmDir() + "osmc.auth");
        config.load(fis);
        fis.close();
        return config;
    }
    public static String getOsmDir () {
        return System.getProperty("user.home")+ File.separator + "osm" + File.separator;
    }
    public static String getOsmOutputFile (String baseName) {
    	return getOsmDir() + "ine" + File.separator + baseName + "."+ fecha + ".osm.gz";
    }

}
