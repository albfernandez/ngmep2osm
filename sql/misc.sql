

-- upgrade osm-names in ngmep table
update ngmep n set
osm_name = (select t.v from node_tags t where t.k = 'name' and t.node_id = n.osmid),
autor = (select u.name from users u, nodes x where u.id = x.user_id and n.osmid = x.id)
where 1=1
and n.osmid is not null
and exists (select 1 from poblaciones_osm p where p.id = n.osmid)