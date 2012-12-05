

-- upgrade osm-names in ngmep table
update ngmep n set
osm_name = (select t.v from node_tags t where t.k = 'name' and t.node_id = n.osmid),
autor = (select u.name from users u, nodes x where u.id = x.user_id and n.osmid = x.id)
where 1=1
and n.osmid is not null
and exists (select 1 from poblaciones_osm p where p.id = n.osmid);


-- nulls deleted nodes or nodes with different ref-ine for reprocessing
update ngmep n 
set osmid = null
where osmid is not null
and not exists (select 1 from node_tags t where t.node_id = n.osmid and t.k = 'ref:ine' and t.v = n.cod_ine)
and not exists (select 1 from way_tags  t where t.way_id  = n.osmid and t.k = 'ref:ine' and t.v = n.cod_ine);