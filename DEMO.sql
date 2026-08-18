-- Setup in IntelliJ as:
-- name: `DEMO@localhost`
-- host: `localhost`
-- port: `5432` (default)
-- database: `postgres` (default)
-- user: `postgres` (default)
-- password: `demopass`
-- NOTE: Under `properties`->`Schemas` enable all databases and all schemas!
SET SESSION search_path TO demo_map, "naksha~admin", topology, hint_plan, public;
SET enable_seqscan TO off;




-- demo_01
select id, fn, version, naksha_2d(geo) as geo, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from random_data;
select id, fn, version, naksha_2d(geo) as geo, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from "random_data$hst";


-- demo_02
select id, fn, version, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from random_data;
select id, fn, version, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from "random_data$hst";


-- demo_03
select id, fn, version, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from random_data
  WHERE version <= 4456397936787628
  AND ST_Intersects(naksha_2d(geo), ST_MakeEnvelope(0.5, 0.5, 2.5, 2.5, 4326))
union all
select id, fn, version, naksha_2d(geo) as geo, naksha_feature(feature) as feature, feature as binary from "random_data$hst"
  WHERE version <= 4456397936787628 AND nv > 4456397936787628
  AND ST_Intersects(naksha_2d(geo), ST_MakeEnvelope(0.5, 0.5, 2.5, 2.5, 4326))


-- demo_04
select id, fn, version, naksha_feature(feature) as feature, feature as binary from random_data;
select id, fn, version, naksha_feature(feature) as feature, feature as binary from "random_data$hst";


-- demo_04
select id, fn, naksha_feature(feature) as feature, feature as binary from delta;


select 'delta' as layer, id, fn, version, naksha_feature(feature) as feature, feature as binary from delta
union all
select 'base' as layer, id, fn, version, naksha_feature(feature) as feature, feature as binary from random_data;


















select * from wikvaya_log where type = 'STREAM' limit 100;


-- Raw select all data.
select naksha_txn('2025-06-19'::timestamptz, 29040284);
select i, geo, jsondata->'properties'->'@ns:com:here:xyz' as xyz
FROM topology_delta
LIMIT 10
;



(SELECT 'READ',
naksha_feature_id(jsondata),
naksha_feature_uuid(jsondata),
naksha_feature_type(jsondata),
naksha_feature_ptype(jsondata),
jsondata,
ST_AsEWKB(geo),
null
FROM topology_delta
WHERE (jsondata->E'properties'->E'@ns:com:here:xyz'->E'txn')::bigint = naksha_txn('2025-06-19'::timestamptz, 29040284)::bigint
LIMIT 1000000
) UNION ALL (
SELECT 'READ',
naksha_feature_id(jsondata),
naksha_feature_uuid(jsondata),
naksha_feature_type(jsondata),
naksha_feature_ptype(jsondata),
jsondata,
ST_AsEWKB(geo),
null 
FROM topology_delta_hst 
WHERE (jsondata->E'properties'->E'@ns:com:here:xyz'->E'txn')::bigint = naksha_txn('2025-06-19'::timestamptz, 29040284)::bigint
LIMIT 1000000
);


-- SQLite
PRAGMA compile_options;
SELECT count(*) as fcount, round(SUM(length(binary))/1024.0/1024.0,2) mib FROM ec_geometries;
SELECT count(*) FROM ec_geo_point order by id LIMIT 10;
SELECT * FROM ec_const LIMIT 10;
SELECT * FROM ec_tag LIMIT 10;
WITH key_ids AS (
  SELECT key_id FROM ec_attr
), c AS (
  SELECT id, value FROM ec_const WHERE id IN key_ids
) SELECT c.value as "key", a.value as v, a.* FROM ec_attr a, c WHERE a.key_id = c.id;
SELECT * FROM ec_geo_point where next_version > 10809738 and version <= 10809738 order by id LIMIT 1000;
SELECT * FROM ec_boundary_element order by id LIMIT 1000;
SELECT count(*) FROM ec_param_point order by id LIMIT 10;
SELECT count(*) FROM ec_edge order by id LIMIT 10;
SELECT * FROM ec_surface LIMIT 10;
-- http://localhost:1234/here/api/feature/51200261134



SELECT count(*) FROM ec_surface;
SELECT * FROM ec_surface WHERE surface_id = 200001006;
SELECT * FROM ec_lane; -- WHERE surface_id = 15393362789870;

SELECT * FROM ec_surface LIMIT 1000;
SELECT * FROM ec_surface WHERE surface_id = 247601848;
SELECT * FROM ec_lane_group WHERE surface_id = ;
SELECT * FROM ec_lane WHERE surface_id = 15393362799755 LIMIT 1000;
SELECT * FROM ec_tag WHERE surface_id = 15393362799755 LIMIT 1000;










-- Select all versions
SELECT * FROM ec_edge WHERE id = 5499900748689 ORDER BY version DESC;


-- Select HEAD (latest version)
SELECT * FROM ec_edge WHERE id = 5499900748689 AND next_version = 9223372036854775807;

-- Select version 12,000,000 (second version)
SELECT * FROM ec_edge WHERE id = 5499900748689 AND version <= 12000000 AND next_version > 12000000;

-- Select version 11,003,583 (second version)
SELECT * FROM ec_edge WHERE id = 5499900748689 AND version <= 11003583 AND next_version > 11003583;

-- Select version 11,003,582 (first version)
SELECT * FROM ec_edge WHERE id = 5499900748689 AND version <= 11003582 AND next_version > 11003582;



-- Select all keys for surface
WITH k AS (SELECT key_id FROM ec_tag WHERE surface_id = 15393372935812),
     c AS (SELECT id, value FROM ec_const, k WHERE id = k.key_id)
SELECT c.value as external_key, t.key_id as key_id, t.value as value FROM ec_tag t, c WHERE t.key_id = c.id AND t.surface_id = 15393372935812;



-- Find all surfaces with key ELEVATION = DEM
WITH c AS (SELECT id, value FROM ec_const WHERE value = 'ELEVATION'),
	 tags AS (SELECT t.surface_id as surface_id, t.key_id as key_id, t.value as name, c.value as value FROM ec_tag t, c WHERE t.key_id = c.id AND t.value = 'DEM')
SELECT tags.name as name, tags.value as value, s.* FROM ec_surface s, tags WHERE s.id = tags.surface_id;






















SELECT * FROM ec_geo_point WHERE latitude >= 48.20698039185197;
SELECT * FROM ec_edge;

-- 1029873
-- Find all surfaces with key ELEVATION = DEM
SELECT * FROM ec_surface WHERE version <= 1029873 AND next_version > 1029873;

SELECT * FROM ec_surface WHERE surface_id = 15393597881484 AND version <= 1029873 AND next_version > 1029873;
SELECT * FROM ec_surface WHERE surface_id = 15393623041178;






SELECT * FROM ec_tiles LIMIT 1000;
SELECT * FROM ec_tiles WHERE tile_id = 192239229322999;
SELECT * FROM ec_surface WHERE surface_id = 210146948;
SELECT * FROM ec_surface WHERE surface_id IN (SELECT id FROM json_each('[ 237664997, 240006460, 247601848, 250204752, 267543213, 322965333, 362663580, 410354894, 455455106, 467964681, 490351459, 562642084, 590353551, 645393168, 690463049, 827972646, 832708172, 862971464, 937694654, 1035468656, 1067549556, 1085261062, 1112773301, 1150268606, 1157571102, 1165479843, 1232524044, 1232777290, 1280316667, 1430476237, 1467504644, 1475226357, 1492974725, 1515460510, 1527908383, 1627970509, 1702972007, 1722974572, 1737607715, 1742960816, 1767976044, 1862957869, 1915051236, 2052968437, 2155471213, 2177975138, 2260149403, 2260482212, 2350316961, 2385010201, 2425262435, 2432759435, 2432972591, 2465405862, 2470471739, 2582826762, 2590068498, 2625461852, 2692981079, 2735457006, 2767970829, 2780481087, 2842886693, 2845464273, 2870458843, 2877953794, 2902642714, 2972975662, 3032582141, 3047921379, 3075346458, 3102954639, 3107959005, 3110173779, 3162757842, 3175291500, 3182969577, 15997504784, 16290006136, 17305005540, 17417507051, 27960486545, 27970486542, 28105478363, 28192983383, 28605027361, 28612538917, 28615026991, 28615489906, 28620032914, 28647518177, 28653039362, 28672529405, 28692542289, 28695023343, 28700022684, 28700467159, 28705633184, 28718035821, 28725041352, 28727523933, 28735039810, 28745475070, 28750031450, 28760023648, 28762920061, 28772537119, 28780043365, 28785042520, 47291917502, 59676910660, 63594402679, 64381926128, 64479405913, 91925084408, 91962576450, 92000086544, 92002582567, 92002582568, 92002582574, 92005079974, 92005079976, 92007581153, 92007581154, 92007581161, 92010087218, 92010087222, 92010087226, 92012582769, 92012582773, 92012582780, 92015083595, 92015083598, 92015083599, 92015083602, 92015083606, 92017578830, 92017578834, 92017578835, 92017578836, 92020076654, 92020076655, 92022583734, 92022583735, 92022583736, 92025076854, 92027583990, 92030081406, 92030081408, 92030081409, 92030081411, 92032575146, 92032575149, 92037587865, 92037587867, 92037587873, 92040085829, 92040085830, 92040085833, 92042582317, 92042582323, 92045084801, 92045084804, 92045084807, 92047579857, 92047579859, 92047579861, 92047579863, 92047579865, 92050084905, 92050084910, 92050084912, 92052584822, 92052584832, 92055081376, 92055081378, 92055081379, 92057580767, 92060083197, 92060083202, 92062579492, 92062579494, 92062579497, 92062579499, 92067576045, 92067576047, 92067576049, 92067576052, 92070089500, 92070089502, 92070089507, 92070089508, 92070089511, 92072581891, 92072581895, 92075083233, 92075083236, 92077584826, 92077584832, 92080081125, 92080081130, 92080081136, 92082583970, 92082583973, 92082583975, 92082583977, 92082583979, 92085077354, 92085077364, 92087583323, 92087583326, 92087583333, 92090083049, 92090083051, 92090083052, 92090083053, 92090083055, 92092584911, 92095086902, 92095086903, 92095086904, 92095086905, 92095086911, 92097577402, 92097577403, 92097577406, 92097577408, 92097577410, 92100081361, 92100081362, 92100081364, 92100081365, 92100081366, 128736930934, 128814430450, 128874430318, 129066931183, 129079432073, 149454442972, 149901940304, 170560105718, 177107516038, 177155017250, 177155017269, 177230018139, 177252518870, 177257516123, 177500017760, 177810016551, 178140018755, 178892513908, 178977515049, 179412503349, 180110003955, 183095095362, 185902509859, 186367511778, 186422514638, 186542514533, 188632623170, 189142580465, 189900045244, 190427504142, 190545005330 ]'));
SELECT * FROM ec_surface LIMIT 100;
SELECT * FROM ec_const LIMIT 1000;
SELECT * FROM ec_lane LIMIT 1000;
SELECT * FROM ec_tag LIMIT 10;
SELECT * FROM ec_attr LIMIT 10;
SELECT * FROM ec_lane LIMIT 10;

WITH geo_points AS (
  SELECT * 
  FROM ec_geo_point
  WHERE latitude >= 0 AND latitude <= 100 AND longitude >= 0 AND longitude <= 100
)
SELECT * FROM ec_param_points WHERE 


SELECT count(*) FROM ec_feature;

SELECT count(feature_id) as features,
	   sum(octet_length(feature_id)) as feature_id_bytes, 
	   sum(octet_length(feature_type)) as feature_type_bytes, 
	   sum(octet_length(feature_binary)) as feature_binary_bytes
FROM ec_feature;


SET SESSION search_path TO poc, "naksha~admin", public, topology, hint_plan, public;
select
  (naksha_tn_feature_number(tn) >> 40) & 0xf as type,
  naksha_tn_feature_number(tn) & 0x00ff_ffff_ffff::bigint as id_int,
  id as id,
  naksha_feature(feature, flags) as f
from ec_geometries
where id = '1505009993'
limit 1000;
select count(*) from ec_geometries;


-- DEVSUP
SET SESSION search_path TO public, topology, hint_plan, public;
SET enable_seqscan TO off;

select * from ec_feature limit 100;
select * from ec_feature where feature_id > 400000000000 limit 100;
-- 1099511627776
--  100000000000
select * from ec_feature_connector_ref LIMIT 100;
select * from ec_feature_connector_ref where connector_id = 36701909 LIMIT 100;
select * from ec_feature where feature_id = 91389434799;
-- feature_id=2235467220
-- SURF_2235467220
-- SBE_2200316785
-- BOUNDARY_1303044505
-- SURF_2235467220
-- PORTAL_2200316785_0

-- feature_id=1490467080


-- Create reference table with 200,000,000 entries
CREATE TABLE refs(
  src_ns int2,
  target_ns int2,
  src_id text,
  target_id text
);
CREATE INDEX refs_src_i ON refs USING btree (src_ns, src_id) INCLUDE (target_ns, target_id);
CREATE INDEX refs_target_i ON refs USING btree (target_ns, target_id) INCLUDE (src_ns, src_id);
-- WARNING: Takes around 2h 0m on a cold Aurora !
INSERT INTO refs (src_ns, target_ns, src_id, target_id)
SELECT trunc(random()*99+1) AS src_ns, 
       trunc(random()*99+1) AS target_ns, 
       trunc(random()*10000000+1) AS src_id, 
       trunc(random()*10000000+1) AS target_id
FROM generate_series(1, 5000000);

-- Peek.
SELECT * FROM refs LIMIT 100;

-- Efficiently query data with index only scan.
EXPLAIN (ANALYZE, BUFFERS)
SELECT src_ns, src_id, target_ns, target_id 
FROM refs 
WHERE target_ns = 68 AND target_id = ANY(ARRAY[
	'87449848','15358263','98403171','4150064','45031903','37878901','71905123','47189497','54716979','94448966',
	'39729590','86847602','59198913','20943438','85449860','54601879','39462878','16285727','84876201','72884982',
	'51251187','85928009','96618828','50978970','58510410','31374284','94490977','83861836','81574096','10395425',
	'30144938','27266520','65041224','43595847','29277646','15786497','12402637','47032140','28624796','44833340',
	'16446126','9770127','44904941','97909054','25955585','21564637','82189762','19332377','12334970','89439896',
	'20257873','80187503','11408677','49979540','68749842','81764766','80547497','89759460','22290697','40722176',
	'69362500','87156892','66190458','31751666','76337438','95457335','33195736','43432527','71739379','51566565',
	'54241641','84829952','55984629','91444469','38048227','46945092','79923121','94166348','1790213','57826342',
	'39095341','20296201','59843203','4318259','90184672','13235356','62578601','26591560','30994528','41140584',
	'74546261','37557602','98734079','34275385','77317058','9536381','47097772','86532714','85230540','46018565'
]);


-- How many features do we have?
SELECT count(*)
FROM demo_collection
;



-- Raw select id, geometry, reference-point, tile-id, tags, and feature.
SELECT
  id,
  geo,
  ref_point,
  here_tile,
  tags,
  feature
FROM demo_collection
ORDER BY naksha_tn_feature_number(tn)
LIMIT 1000;



-- Decode select id, action, geometry, reference-point, tile-id, tags, and feature.
SELECT
  id,
  naksha_flags_action_text(flags) as action,
  naksha_tn_feature_number(tn) as feature_number,
  naksha_geometry(geo, flags) as geo,
  naksha_ref_point(ref_point) as ref_point,
  here_tile,
  naksha_tags(tags, flags) as tags,
  naksha_feature(feature, flags) as feature
FROM "demo_collection"
--WHERE id = 'o9E6kdUbRLYo'
ORDER BY naksha_tn_feature_number(tn)
LIMIT 1000;



-- Decode select id, action, geometry, reference-point, tile-id, tags, and feature.
SELECT
  id,
  naksha_flags_action_text(flags) as action,
  naksha_tn_feature_number(tn) as feature_number,
  naksha_geometry(geo, flags) as geo,
  naksha_ref_point(ref_point) as ref_point,
  here_tile,
  naksha_tags(tags, flags) as tags,
  naksha_feature(feature, flags) as feature
FROM demo_collection
WHERE (naksha_tags(tags, flags)->>'group')::float8 = 1.0
ORDER BY naksha_tn_feature_number(tn)
LIMIT 2000;



-- Show transaction logs, their are just features.
SELECT id, naksha_tn_version(tn) as version, naksha_feature(feature, flags) as "tx"
FROM "naksha~admin"."naksha~transactions"
ORDER BY naksha_tn_version(tn) DESC;



-- Decode select id, action, geometry, reference-point, tile-id, tags, and feature.
--EXPLAIN
SELECT
  id,
  naksha_flags_action_text(flags) as action,
  naksha_tn_feature_number(tn) as feature_number,
  naksha_geometry(geo, flags) as geo,
  naksha_ref_point(ref_point) as ref_point,
  here_tile,
  naksha_tags(tags, flags) as tags,
  naksha_feature(feature, flags) as feature
FROM demo_collection
WHERE naksha_tn_version(tn) = 4453739352031258
ORDER BY naksha_tn_feature_number(tn);


-- Search for ref-quad-id in tile
-- $1 = 1476395008
-- $2 = 1476460543
--EXPLAIN
WITH query AS (
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p000" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p001" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p002" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p003" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p004" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p005" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p006" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p007" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p008" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p009" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p010" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p011" WHERE  ((here_tile >= $1 AND here_tile <= $2)))
), limited AS (
  SELECT col_num, tn
  FROM query
  LIMIT 16777216
)
SELECT tn FROM limited;



-- Search for group
-- $2 = 'group'
-- $1 = 0.0
--EXPLAIN
WITH query AS (
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p000" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p001" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p002" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p003" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p004" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p005" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p006" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p007" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p008" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p009" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p010" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
 UNION ALL
	(SELECT -1606573927 AS col_num, tn FROM "demo_collection$p011" WHERE  ((naksha_tags(tags, flags)->$2)::float8 = $1))
), limited AS (
  SELECT col_num, tn
  FROM query
  LIMIT 16777216
)
SELECT tn FROM limited;



-- Select from admin_db
SET SESSION search_path TO naksha_admin_v3, "naksha~admin", topology, hint_plan, public;
SET enable_seqscan TO off;
select id, naksha_feature(feature, flags) as feature from "hub_internal:configs";
select id, naksha_feature(feature, flags) as feature from "hub_internal:event_handlers";
select id, naksha_feature(feature, flags) as feature from "hub_internal:extensions";
select id, naksha_feature(feature, flags) as feature from "hub_internal:spaces";
select id, naksha_feature(feature, flags) as feature from "hub_internal:storages";
select id, naksha_feature(feature, flags) as feature from "hub_internal:subscriptions";


-- Select from data_db
SET SESSION search_path TO temp_store, "naksha~admin", topology, hint_plan, public;
SET enable_seqscan TO off;
select id, naksha_feature(feature, flags) as feature from topology;
select id, naksha_feature(feature, flags) as feature from "um-mod-dev:tc_280_auto_delete_on";
select id, naksha_feature(feature, flags) as feature from "um-mod-dev:tc_281";


SET SESSION search_path TO cons_store, "naksha~admin", topology, hint_plan, public;
SET SESSION search_path TO temp_store, "naksha~admin", topology, hint_plan, public;
SET enable_seqscan TO off;
select id, naksha_feature(feature, flags) as feature from "logicalroadsign" limit 1;

SET SESSION search_path TO temp_store, "naksha~admin", topology, hint_plan, public;
SELECT id FROM "logicalroadsign";
select naksha_flags_action(flags) as action, count(*) as counts from "logicalroadsign" group by action;

select naksha_flags_action(flags) as action, count(*) as counts from "logicalroadsign" group by action
UNION ALL (select 2 as action, count(*) as counts from "logicalroadsign$del");


SET SESSION search_path TO naksha_test_temp, "naksha~admin", topology, hint_plan, public;
SELECT naksha_flags_action(flags) AS action, count(*) AS counts FROM "topology" GROUP BY action
UNION ALL (SELECT 2 AS action, count(*) AS counts FROM "topology$del");


SET SESSION search_path TO temp_store, "naksha~admin", topology, hint_plan, public;
SET enable_seqscan TO off;
SELECT id, naksha_feature(feature, flags) AS feature FROM "topology"
UNION ALL
SELECT id, naksha_feature(feature, flags) AS feature FROM "topology$del";


