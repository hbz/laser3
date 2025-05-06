
--
-- Pseudonymization (requirements and tests)
--

-- Anon functions

CREATE OR REPLACE FUNCTION pg_temp.anon_get_rnd_text() RETURNS TEXT AS $$
DECLARE
    data TEXT[];
BEGIN
    data = ARRAY [
        'Aller guten Dinge sind drei',
        'Besser spät als nie',
        'Der Ton macht die Musik',
        'Es ist nicht alles Gold, was glänzt',
        'Gut Ding will Weile haben',
        'In der Ruhe liegt die Kraft',
        'Kommt Zeit, kommt Rat',
        'Lange Rede, kurzer Sinn',
        'Man muss das Eisen schmieden, solange es heiß ist',
        'Not macht erfinderisch',
        'Ordnung ist das halbe Leben',
        'Probieren geht über studieren',
        'Scherben bringen Glück',
        'Von nichts kommt nichts',
        'Wer die Wahl hat, hat die Qual',
        'Zahlen lügen nicht'
        ];
    RETURN (SELECT data[floor(random() *  cardinality(data) + 1)]);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_text(oldValue TEXT, debugInfo TEXT DEFAULT '') RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        IF debugInfo != '' THEN
            RETURN concat( pg_temp.anon_get_rnd_text(), ' (', debugInfo, ')' );
        ELSE
            RETURN pg_temp.anon_get_rnd_text();
        END IF;
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_xval(oldValue TEXT, debugInfo TEXT DEFAULT '') RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        IF debugInfo != '' THEN
            RETURN concat( 'X', substr(md5(random()::text), 1, 8), ' (', debugInfo, ')' );
        ELSE
            RETURN concat( 'X', substr(md5(random()::text), 1, 8));
        END IF;
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_url(oldValue TEXT) RETURNS TEXT AS $$
DECLARE
    part1 TEXT[];
    part2 TEXT[];
BEGIN
    part1 = ARRAY ['abc.', 'bibo.', 'data.', 'help.', 'www.', ''];
    part2 = ARRAY ['anon.', 'example.', 'test.'];

    IF trim(coalesce(oldValue, '')) != '' THEN
        RETURN 'https://' ||
               part1[floor(random() *  cardinality(part1) + 1)] ||
               part2[floor(random() *  cardinality(part2) + 1)] ||
               'local';
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

select pg_temp.anon_url('#');

CREATE OR REPLACE FUNCTION pg_temp.anon_set_text(nullValueChance INT DEFAULT 0) RETURNS TEXT AS $$
BEGIN
    IF nullValueChance >= floor(random() * 100) + 1 THEN
        RETURN null;
    ELSE
        RETURN pg_temp.anon_get_rnd_text();
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_set_xval(nullValueChance INT DEFAULT 0) RETURNS TEXT AS $$
BEGIN
    IF nullValueChance >= floor(random() * 100) + 1 THEN
        RETURN null;
    ELSE
        RETURN concat('X', substr(md5(random()::text), 1, 8));
    END IF;
END;
$$ LANGUAGE PLPGSQL;

-- Anon function tests

CREATE OR REPLACE FUNCTION pg_temp.anon_test() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE 'anon -> running tests';

    ASSERT length( pg_temp.anon_text('old') ) > 0,   'anon_text 1 failed';
    ASSERT length( pg_temp.anon_text('old', 'debug') ) > 0,   'anon_text 2 failed';
    ASSERT length( pg_temp.anon_text('') ) = 0,   'anon_text 3 failed';
    ASSERT length( pg_temp.anon_text('   ', 'debug') ) = 3,   'anon_text 4 failed';
    ASSERT         pg_temp.anon_text(null, 'debug') IS null,   'anon_text 5 failed';
    ASSERT         pg_temp.anon_text(null, null) IS null,   'anon_text 6 failed';

    ASSERT length( pg_temp.anon_xval('old') ) > 0,   'anon_xval 1 failed';
    ASSERT length( pg_temp.anon_xval('old', 'debug') ) > 0,   'anon_xval 2 failed';
    ASSERT length( pg_temp.anon_xval('') ) = 0,   'anon_xval 3 failed';
    ASSERT length( pg_temp.anon_xval('   ', 'debug') ) = 3,   'anon_xval 4 failed';
    ASSERT         pg_temp.anon_xval(null, 'debug') IS null,   'anon_xval 5 failed';
    ASSERT         pg_temp.anon_xval(null, null) IS null,   'anon_xval 6 failed';

    RAISE NOTICE 'anon -> done .. NO errors found';
END;
$$ LANGUAGE PLPGSQL;

SELECT pg_temp.anon_test();

--
-- Pseudonymization (data manipulation)
--

-- for LOCAL only:

UPDATE org SET
    org_is_beta_tester = false
WHERE org_guid NOT IN ('org:e6be24ff-98e4-474d-9ef8-f0eafd843d17', 'org:1d72afe7-67cb-4676-add0-51d3ae66b1b3');

-- User

UPDATE "user" SET
    usr_username    = concat('User ', usr_id),
    usr_display     = concat('User ', usr_id),
    usr_email       = concat('user',  usr_id, '@anon.local'),
    usr_password    = 'you_shall_not_pass',
    usr_enabled     = false
WHERE usr_id NOT IN (
    SELECT u.usr_id
    FROM "user" u
         JOIN org o ON o.org_id = u.usr_formal_org_fk
    WHERE o.org_is_beta_tester = true
    UNION
    SELECT u.usr_id from "user" u where u.usr_username = 'anonymous'
);

UPDATE user_setting SET
    us_string_value = 'cc@anon.local'
WHERE us_string_value != '' AND
    us_key_enum IN ('REMIND_CC_EMAILADDRESS', 'NOTIFICATION_CC_EMAILADDRESS') AND
    us_user_fk NOT IN (
        SELECT u.usr_id
        FROM "user" u
             JOIN org o ON o.org_id = u.usr_formal_org_fk
        WHERE o.org_is_beta_tester = true
        UNION
        SELECT u.usr_id from "user" u where u.usr_username = 'anonymous'
    );

-- Org

UPDATE org_setting SET
    os_string_value     = pg_temp.anon_xval(os_string_value)
WHERE os_key_enum IN ('API_PASSWORD', 'NATSTAT_SERVER_API_KEY', 'NATSTAT_SERVER_REQUESTOR_ID', 'LASERSTAT_SERVER_KEY') AND
    os_org_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

UPDATE customer_identifier SET
    cid_value           = pg_temp.anon_xval(cid_value),
    cid_requestor_key   = pg_temp.anon_xval(cid_requestor_key),
    cid_note            = pg_temp.anon_text(cid_note, concat('c:', cid_customer_fk, ', o:', cid_owner_fk))
WHERE cid_customer_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

-- Finance

UPDATE cost_item SET
    ci_cost_in_billing_currency = round(cast(random() * 5 * ci_cost_in_billing_currency AS NUMERIC), 2)
WHERE
    ci_cost_in_billing_currency IS NOT NULL AND
    ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

UPDATE cost_item SET
    ci_cost_in_local_currency = round(cast(ci_cost_in_billing_currency * ci_currency_rate AS NUMERIC), 2)
WHERE
    ci_cost_in_billing_currency IS NOT NULL AND
    ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

-- Properties

UPDATE person_property SET
    pp_note             = pg_temp.anon_text(pp_note),
    pp_string_value     = pg_temp.anon_text(pp_string_value, concat(to_char(pp_date_created, 'DD.MM.YYYY'), ', ', pp_is_public)),
    pp_url_value        = pg_temp.anon_url(pp_url_value)
WHERE pp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE platform_property SET
    plp_note            = pg_temp.anon_text(plp_note),
    plp_string_value    = pg_temp.anon_text(plp_string_value, concat(to_char(plp_date_created, 'DD.MM.YYYY'), ', ', plp_is_public)),
    plp_url_value       = pg_temp.anon_url(plp_url_value)
WHERE plp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE provider_property SET
    prp_note            = pg_temp.anon_text(prp_note),
    prp_string_value    = pg_temp.anon_text(prp_string_value, concat(to_char(prp_date_created, 'DD.MM.YYYY'), ', ', prp_is_public)),
    prp_url_value       = pg_temp.anon_url(prp_url_value)
WHERE prp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE vendor_property SET
    vp_note             = pg_temp.anon_text(vp_note),
    vp_string_value     = pg_temp.anon_text(vp_string_value, concat(to_char(vp_date_created, 'DD.MM.YYYY'), ', ', vp_is_public)),
    vp_url_value        = pg_temp.anon_url(vp_url_value)
WHERE vp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

-- Various

-- UPDATE doc SET
--     doc_title       = pg_temp.anon_text(title, to_char(doc_date_created, 'DD.MM.YYYY')),
--     doc_content     = pg_temp.anon_text(doc_content)
-- WHERE doc_content_type = 0 AND
--     doc_owner_fk NOT IN (
--         SELECT org_id FROM org WHERE org_is_beta_tester = true
--     );

UPDATE task SET
    tsk_title       = pg_temp.anon_text(tsk_title, to_char(tsk_date_created, 'DD.MM.YYYY')),
    tsk_description = null
WHERE tsk_creator_fk NOT IN (
    SELECT usr_id FROM "user" WHERE usr_formal_org_fk IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);

UPDATE wf_checklist SET
    wfcl_title          = pg_temp.anon_text(wfcl_title, to_char(wfcl_date_created, 'DD.MM.YYYY')),
    wfcl_description    = null,
    wfcl_comment        = null
WHERE wfcl_owner_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE wf_checkpoint SET
    wfcp_title          = pg_temp.anon_text(wfcp_title, wfcp_position::text),
    wfcp_description    = null,
    wfcp_comment        = null
WHERE wfcp_checklist_fk IN (
    SELECT wfcl_id FROM wf_checklist WHERE wfcl_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);