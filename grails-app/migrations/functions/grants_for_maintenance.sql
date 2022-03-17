CREATE OR REPLACE FUNCTION GRANTS_FOR_MAINTENANCE()
    RETURNS void
    LANGUAGE plpgsql
    AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 3;

    bck_user RECORD;
    readonly_user RECORD;

BEGIN

    SELECT * into bck_user FROM pg_catalog.pg_user where usename = 'backup';

    IF NOT found THEN
        RAISE NOTICE 'no backup user found';

    ELSE
        RAISE NOTICE 'backup user found: %', bck_user;

        EXECUTE 'GRANT usage ON SCHEMA public TO backup';
        EXECUTE 'GRANT select ON ALL TABLES IN SCHEMA public TO backup';
        EXECUTE 'GRANT select ON ALL SEQUENCES IN SCHEMA public TO backup';

        EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO backup';
        EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO backup';
    END IF;

    SELECT * into readonly_user FROM pg_catalog.pg_user where usename = 'readonly';

    IF NOT found THEN
        RAISE NOTICE 'no readonly user found';

    ELSE
        RAISE NOTICE 'readonly user found: %', readonly_user;

        EXECUTE 'GRANT usage ON SCHEMA public TO readonly';
        EXECUTE 'GRANT select ON ALL TABLES IN SCHEMA public TO readonly';
        EXECUTE 'GRANT select ON ALL SEQUENCES IN SCHEMA public TO readonly';

        EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly';
        EXECUTE 'ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO readonly';
    END IF;

END;
$$;
