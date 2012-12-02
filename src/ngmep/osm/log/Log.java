package ngmep.osm.log;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import ngmep.config.Config;

public class Log {

	public static void log(String mensaje) {
		if (Config.getInstance().isLogConsola() && System.out != null){
			System.out.println(mensaje); //NOPMD
		}
		logToFile (mensaje);
	}
	public static void logToFile (String mensaje) {
		try {
			writeToFile(mensaje, Config.getInstance().getLogFile());
		}
		catch (IOException ioe) {
			// Ignorar
		}
	}
	private static void writeToFile(String mensaje, String ruta) throws IOException {
		String mensajeDisco = "["+new Timestamp(new Date().getTime())+"] " + mensaje;
		try (FileWriter fichero =  new FileWriter(ruta, true)){
			fichero.write(mensajeDisco);
			fichero.write(System.lineSeparator());			
		} 
	}
}
