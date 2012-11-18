package ngmep.osm.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import ngmep.config.Config;

public class Log {

	public static void log(String mensaje) {
		System.out.println(mensaje);
		logToFile (mensaje);
	}
	public static void logToFile (String mensaje) {
		try {
			writeToFile(mensaje, Config.getOsmDir() +"ine" + File.separator + "log"+File.separator + "log.log");
		}
		catch (IOException ioe) {
			// Ignorar
		}
	}
	private static void writeToFile(String mensaje, String ruta) throws IOException {
		String mensajeDisco = "["+new Timestamp(new Date().getTime())+"] " + mensaje;
		FileWriter fichero = null;
		try {
			fichero = new FileWriter(ruta, true);
			fichero.write(mensajeDisco);
			fichero.write("\n");
			
		} finally {						
			if (fichero != null){
				fichero.close();
			}
		}
	}
}
