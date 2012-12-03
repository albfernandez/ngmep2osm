package ngmep.main;

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
import java.io.IOException;
import java.sql.SQLException;

import ngmep.osm.dao.Database;
import ngmep.osm.log.Log;

import org.apache.commons.lang3.time.StopWatch;

public final class Main {

	private Main() {
		// No instances of this class are allowed
	}
    /**
     * @param args
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws SQLException 
     */
    public static void main(final String[] args) throws SQLException, ClassNotFoundException, IOException {
    	final boolean objetivo2 = isProcessEnabled(args, "2");
    	final boolean objetivo3 = isProcessEnabled(args, "3");
    	final boolean objetivo4 = isProcessEnabled(args, "4");

        final StopWatch cronometro = new StopWatch();
        cronometro.start();
        if (objetivo2) {
        	ngmep.procesos.BuscaAsignaIds.buscaOsmId();
        }
        if (objetivo3) {
        	ngmep.procesos.Objetivo3.ejecutaObjetivo3();
        }
        if (objetivo4) {
        	ngmep.procesos.Objetivo4.ejecutaObjetivo4();
        }
        
        Database.closeConnection();
        cronometro.stop();
        Log.log("Tiempo empleado:" + cronometro);

    }
    private static boolean isProcessEnabled(final String[] args, final String testValue) {
    	if (args == null || args.length == 0) {
    		return true;    		
    	}
    	else {    		
    		for (String argument: args) {
    			if (testValue.equals(argument)){
    				return true;
    			}
    		}	
    	}
    	return false;
    }
}


