CREATE OR REPLACE FUNCTION GRANTS_FOR_BACKUP()
    RETURNS void
    LANGUAGE plpgsql
    AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

    bck_user RECORD;

BEGIN

    SELECT * into bck_user FROM pg_catalog.pg_user where usename = 'backup';

    IF NOT found THEN
        RAISE EXCEPTION 'no backup user found';
    END IF;

    RAISE NOTICE 'backup user found: %', bck_user;

    EXECUTE 'GRANT usage ON SCHEMA public TO backup';
    EXECUTE 'GRANT select ON ALL TABLES IN SCHEMA public TO backup';
    EXECUTE 'GRANT select, usage ON ALL SEQUENCES IN SCHEMA public TO backup';
    EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO backup';

END;
$$;
