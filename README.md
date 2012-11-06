ngmep2osm
=========

Importardor de datos NGMEP para OSM

Este proyecto se desarrolló en la última parte de 2011 y principios de 2012 para llevar a cabo la importación
de los datos del NGMEP en openstreetmap.

Puedes hacer un seguimiento del estado de la importación en http://wiki.openstreetmap.org/wiki/ES:NGMEP

Para ejecutar el proyecto se requiere cargar los datos de openstreetmap en una base de datos postgresql local, 
así como los datos del NGMEP cargados de forma adecuada para ello.

La finalidad de publicar este código no es tanto que se pueda ejecutar el programa, sino que pueda servir a otros 
que desarrollen herramientas similares en Java.

Para compilar es necesario :
- el driver jdbc de postgresql,
- commons-lang