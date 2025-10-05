package ru.ai.assistant.db

object SqlScript {

    val QUERY_ALL_DATA = """
        SELECT jsonb_agg(x.table_json ORDER BY x.schema_name, x.table_name) AS tables
        FROM (
          SELECT 
            t.table_schema AS schema_name,
            t.table_name,
            jsonb_build_object(
              'schema', t.table_schema,
              'table',  t.table_name,
              'table_comment',
                (
                  SELECT obj_description(c.oid)
                  FROM pg_class c
                  JOIN pg_namespace n ON n.oid = c.relnamespace
                  WHERE c.relname = t.table_name
                    AND n.nspname = t.table_schema
                ),
              'columns',
                (
                  SELECT jsonb_agg(
                           jsonb_build_object(
                             'name',              c.column_name,
                             'data_type',         c.data_type,
                             'is_nullable',       (c.is_nullable = 'YES'),
                             'default',           c.column_default,
                             'ordinal_position',  c.ordinal_position,
                             'column_comment',
                               (
                                 SELECT pgd.description
                                 FROM pg_description pgd
                                 JOIN pg_class cls     ON cls.oid = pgd.objoid
                                 JOIN pg_namespace ns  ON ns.oid = cls.relnamespace
                                 JOIN pg_attribute attr ON attr.attrelid = cls.oid AND attr.attnum = pgd.objsubid
                                 WHERE cls.relname = c.table_name
                                   AND ns.nspname = c.table_schema
                                   AND attr.attname = c.column_name
                               )
                           )
                           ORDER BY c.ordinal_position
                         )
                  FROM information_schema.columns c
                  WHERE c.table_schema = t.table_schema
                    AND c.table_name   = t.table_name
                ),
              'primary_key',
                (
                  SELECT COALESCE(jsonb_agg(a.attname ORDER BY a.attnum), '[]'::jsonb)
                  FROM pg_index i
                  JOIN pg_class     ct ON ct.oid = i.indrelid
                  JOIN pg_namespace ns ON ns.oid = ct.relnamespace
                  JOIN pg_attribute a  ON a.attrelid = ct.oid AND a.attnum = ANY(i.indkey)
                  WHERE i.indisprimary
                    AND ns.nspname = t.table_schema
                    AND ct.relname = t.table_name
                )
            ) AS table_json
          FROM information_schema.tables t
          WHERE t.table_type = 'BASE TABLE'
            AND t.table_schema NOT IN ('pg_catalog', 'information_schema')
            AND NOT (
              t.table_schema = 'public'
              AND t.table_name IN ('databasechangelog', 'databasechangeloglock')
            )
        ) x;

    """.trimIndent()
}