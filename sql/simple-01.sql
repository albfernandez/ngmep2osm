-- Borramos el indice para que las inserciones sean más rapidas
DROP INDEX  idx_poblaciones_osm_geom;

-- Borramos los datos
delete from poblaciones_osm;


-- Rellenamos los datos de nodos
insert into poblaciones_osm(id, version, user_id, tstamp, changeset_id, tipo, geom)
select id, version, user_id, tstamp, changeset_id, 'node', geom from nodes n
where exists (select 1 from node_tags t where t.node_id = n.id and t.k = 'place' and t.v in ('city','town', 'village', 'hamlet', 'suburb','isolated_dwelling'));

insert into poblaciones_osm(id, version, user_id, tstamp, changeset_id, tipo, geom)
select id, version, user_id, tstamp, changeset_id, 'way', 
 (select geom from nodes n, way_nodes nw 
	where nw.way_id = w.id and nw.node_id = n.id and nw.sequence_id = (
		select min(sequence_id) from way_nodes x where x.way_id = nw.way_id
	)
 )
from ways w
where exists (select 1 from way_tags t where t.way_id = w.id and t.k = 'place' and t.v in ('city','town', 'village', 'hamlet', 'suburb','isolated_dwelling'));

-- way_nodes way_id, node_id , sequence_id

-- Recreamos el indice
CREATE INDEX idx_poblaciones_osm_geom ON poblaciones_osm USING gist (geom);
