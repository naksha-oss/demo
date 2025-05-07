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



-- Raw select all data.
SELECT *
FROM demo_collection
LIMIT 10
;



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
WHERE naksha_tn_version(tn) = 4453739352033117
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
