CREATE OR REPLACE FUNCTION COUNT_ROWS_FOR_ALL_TABLES(schema TEXT)
    RETURNS table(x information_schema.sql_identifier, y information_schema.sql_identifier, z INTEGER)
    LANGUAGE plpgsql
AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

BEGIN
    RETURN QUERY
        select table_schema as x, table_name as y, count_rows(table_schema, table_name) as z
        from information_schema.tables
        where table_schema=schema
          and table_type='BASE TABLE'
        order by 3 desc;

END;
$$;
