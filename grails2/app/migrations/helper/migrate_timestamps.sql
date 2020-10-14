
-- migrate existing 'timestamp with timezone' to 'timestamp'

CREATE OR REPLACE FUNCTION MIGRATE_TIMESTAMPS()
    RETURNS void
    LANGUAGE plpgsql
AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

    match RECORD;

    curr_matches CURSOR
        FOR SELECT table_name, column_name, data_type
            FROM information_schema.columns
            WHERE table_schema = 'public' AND data_type LIKE 'timestamp with time zone';

BEGIN

    OPEN curr_matches;

    LOOP
        FETCH curr_matches INTO match;
        EXIT WHEN NOT FOUND;

        RAISE NOTICE 'alter table % alter column % type timestamp using %::timestamp;', match.table_name, match.column_name, match.column_name;
        RAISE NOTICE 'update % set % = (% + ''1 hour''::interval);', match.table_name, match.column_name, match.column_name;
    END LOOP;

    CLOSE curr_matches;

END;
$$;

SELECT MIGRATE_TIMESTAMPS();