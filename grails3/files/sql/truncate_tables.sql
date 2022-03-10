
-- truncate all tables (except dbm info), reset sequences

SELECT * FROM information_schema.tables
  WHERE table_catalog = 'laser' AND table_schema = 'public' AND table_type = 'BASE TABLE'
  AND table_name NOT IN ('databasechangelog', 'databasechangeloglock');

SELECT 'TRUNCATE ' || table_name || ' RESTART IDENTITY CASCADE;'
  FROM information_schema.tables
    WHERE table_catalog = 'laser'
          AND table_schema = 'public'
          AND table_type = 'BASE TABLE'
          AND table_name NOT IN ('databasechangelog', 'databasechangeloglock');
