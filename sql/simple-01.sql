-- Borramos el indice para que las inserciones sean más rapidas
DROP INDEX  idx_poblaciones_osm_geom;

-- Borramos los datos
delete from poblaciones_osm;


-- Rellenamos los datos
insert into poblaciones_osm(id, version, user_id, tstamp, changeset_id, geom)
select id, version, user_id, tstamp, changeset_id, geom from nodes n
where exists (select 1 from node_tags t where t.node_id = n.id and t.k = 'place' and t.v in ('city','town', 'village', 'hamlet', 'suburb','isolated_dwelling'));

-- Recreamos el indice
CREATE INDEX idx_poblaciones_osm_geom ON poblaciones_osm USING gist (geom);
