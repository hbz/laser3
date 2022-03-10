CREATE OR REPLACE FUNCTION COUNT_ROWS(schema TEXT, table_name TEXT)
    RETURNS integer
    LANGUAGE plpgsql
AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

    result INTEGER;

BEGIN
    execute 'SELECT count(1) FROM ' || schema || '.' || table_name into result;
    return result;
END;
$$;
