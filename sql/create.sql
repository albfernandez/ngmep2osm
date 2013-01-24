CREATE TABLE poblaciones_osm (
    id bigint NOT NULL,
    version int NOT NULL,
    user_id int NOT NULL,
    tstamp timestamp without time zone NOT NULL,
    changeset_id bigint NOT NULL,
    tipo char(5),
    cod_ine char(64)
);

SELECT AddGeometryColumn('poblaciones_osm', 'geom', 4326, 'POINT', 2);


ALTER TABLE ONLY poblaciones_osm ADD CONSTRAINT pk_poblaciones_osm PRIMARY KEY (id);

CREATE INDEX idx_poblaciones_osm_geom ON poblaciones_osm USING gist (geom);
