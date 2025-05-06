
--
-- Pseudonymization (requirements and tests)
--

-- Anon functions - base

CREATE OR REPLACE FUNCTION pg_temp.anon_gen_rnd_lorem(length INT DEFAULT 1) RETURNS TEXT AS $$
DECLARE
    data TEXT[];
BEGIN
    data = ARRAY [
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor ..',
        'Ligula facilisis neque velit id ut varius tempus per diam quis sodales curae dolor et.',
        'Orci aliquam litora et bibendum urna lectus massa.',
        'Lectus tempus cras tristique mollis varius ligula vitae hac ad quis.',
        'Vitae suspendisse nec hendrerit nibh netus dictumst habitasse vehicula praesent turpis urna.',
        'Nec orci viverra pharetra fames aliquam varius integer rutrum mauris.',
        'Ut tristique eleifend a habitant varius lacinia ad velit turpis.',
        'Lacinia metus varius dolor mi consequat diam sagittis leo accumsan himenaeos velit ex.',
        'Fringilla non lacus eros potenti varius lobortis feugiat luctus.',
        'Nunc nisl pulvinar duis hendrerit quisque sollicitudin tempor faucibus integer gravida orci.',
        'Eros elit nibh cursus ante vestibulum pharetra ligula suspendisse maximus eros commodo sed.',
        'Mattis accumsan consequat imperdiet vulputate tempor lectus ut nunc justo.',
        'Ipsum praesent tellus nam curabitur dolor amet elit.',
        'Etiam taciti nulla varius suspendisse suscipit cubilia rutrum eleifend leo euismod fames.',
        'Id justo etiam fames fringilla eu placerat phasellus aenean lacus sit eget.',
        'Lacinia tellus quis fringilla condimentum sollicitudin tempus est aenean id.',
        'Cubilia praesent facilisis faucibus augue nisl risus tempus habitasse luctus pretium.',
        'Nam praesent per interdum interdum sed.',
        'Justo neque venenatis rhoncus sit aliquam faucibus litora volutpat fringilla sem ..',
        'Arcu nulla metus ipsum adipiscing donec habitasse tincidunt.',
        'Platea vulputate ut mauris conubia lacinia per vehicula vulputate imperdiet.',
        'Nisi dolor odio suspendisse dictumst maecenas donec porta nam.',
        'Dapibus sagittis libero neque pharetra aptent vulputate vestibulum fusce ..',
        'Quisque sodales inceptos proin eu vulputate hendrerit tortor facilisis bibendum.',
        'Taciti risus nec finibus feugiat torquent ligula malesuada netus nulla et volutpat nam.',
        'Pharetra lobortis senectus potenti felis cubilia eu aenean sem sodales euismod morbi ante pharetra metus.',
        'Placerat sem mattis lorem malesuada class et magna egestas mauris fusce mauris.',
        'Platea est elementum sociosqu egestas conubia egestas.',
        'Ultricies mauris nunc orci elit suspendisse suspendisse.',
        'Nullam non tempor justo urna donec habitant sociosqu litora praesent turpis ..',
        'Quisque facilisis facilisis sociosqu habitasse fusce.',
        'Mi dictum condimentum luctus ultrices nec maecenas lectus rutrum fermentum mattis.',
        'Massa hac dictum purus vitae mollis himenaeos vivamus dapibus.',
        'Semper tristique velit urna viverra porttitor ad posuere dui dignissim hac sed ultrices ..',
        'Feugiat eleifend ultrices finibus laoreet enim.',
        'At ipsum tortor ultrices volutpat rhoncus congue.',
        'Mauris tristique id posuere netus dictum convallis curae sagittis erat etiam donec ..',
        'Finibus urna lacus dapibus nisi aliquam ullamcorper.',
        'Urna finibus praesent cras bibendum ad ac nullam fermentum maximus.',
        'Dapibus ligula maximus torquent quis volutpat finibus.'
        ];
    RETURN array_to_string(array_sample(data, length), ' ', '*');
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_gen_rnd_phrase() RETURNS TEXT AS $$
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
    RETURN data[floor(random() *  cardinality(data) + 1)];
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_gen_contact(type TEXT) RETURNS TEXT AS $$
DECLARE
BEGIN
    IF type = 'email' THEN
        RETURN 'to@do.invalid';
    ELSEIF type = 'phone' THEN
        RETURN '01234 - 56 78 90';
    ELSEIF type = 'url' THEN
        RETURN 'https://to.do.invalid';
    ELSE
        RETURN null;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_gen_person(gender TEXT) RETURNS TEXT[] AS $$
DECLARE
    data_mn TEXT[];
    data_fn TEXT[];
    data_ln TEXT[];
BEGIN
    data_mn = ARRAY [
        'Achim', 'Adam', 'Anton',
        'Balduin', 'Bert', 'Björn',
        'Carsten', 'Christoph', 'Cuno',
        'Dagobert', 'Didi', 'Dorian',
        'Eckard', 'Erich', 'Erwin',
        'Felix', 'Florian', 'Franz',
        'Gerd', 'Gisbert', 'Günther'
        ];
    data_fn = ARRAY [
        'Ada', 'Antonia', 'Astrid',
        'Barbara', 'Berta', 'Birgit',
        'Carolin', 'Christine', 'Cuna',
        'Dagmar', 'Diane', 'Doris',
        'Edda', 'Elke', 'Emma',
        'Fenja', 'Flora', 'Frauke',
        'Gabi', 'Gerda', 'Greta'
        ];
    data_ln = ARRAY [
        'Aberle', 'Acker', 'Althaus', 'Andersen', 'Auerbach',
        'Bachmeier', 'Birken', 'Beckmann', 'Bluhme', 'Böhmer',
        'Calmund', 'Conrad', 'Claasen', 'Cremer', 'Czajkowski',
        'Dahlberg', 'Daubner', 'Diemert', 'Dörfler', 'Dyck',
        'Ebertz', 'Edinger', 'Erdmann', 'Eschke', 'Eulert',
        'Faber', 'Fechtheim', 'Feldkamp', 'Förster', 'Fuchs',
        'Gabel', 'Geppert', 'Giesen', 'Goller', 'Gottschalk'
        ];

    IF gender = 'male' THEN
        RETURN (SELECT ARRAY[data_mn[floor(random() *  cardinality(data_mn) + 1)], data_ln[floor(random() *  cardinality(data_ln) + 1)]]);
    ELSEIF gender = 'female' THEN
        RETURN (SELECT ARRAY[data_fn[floor(random() *  cardinality(data_fn) + 1)], data_ln[floor(random() *  cardinality(data_ln) + 1)]]);
    ELSE
        RETURN (SELECT ARRAY[null, null]);
    END IF;
END;
$$ LANGUAGE PLPGSQL;

-- Anon functions - for use

CREATE OR REPLACE FUNCTION pg_temp.anon_lorem(oldValue TEXT, length INT DEFAULT 1) RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        RETURN pg_temp.anon_gen_rnd_lorem(length);
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_phrase(oldValue TEXT, debugInfo TEXT DEFAULT '') RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        IF debugInfo != '' THEN
            RETURN concat( pg_temp.anon_gen_rnd_phrase(), ' (', debugInfo, ')' );
        ELSE
            RETURN pg_temp.anon_gen_rnd_phrase();
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
    part2 = ARRAY ['anon.', 'bit.', 'test.'];

    IF trim(coalesce(oldValue, '')) != '' THEN
        RETURN 'https://' ||
               part1[floor(random() *  cardinality(part1) + 1)] ||
               part2[floor(random() *  cardinality(part2) + 1)] ||
               'example';
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

-- Anon function tests

CREATE OR REPLACE FUNCTION pg_temp.anon_test() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE 'ANON -> running tests';

    RAISE NOTICE 'ANON ---> anon_lorem()';
    ASSERT length( pg_temp.anon_lorem('old') ) > 0,   'anon_lorem 1 failed';
    ASSERT length( pg_temp.anon_lorem('old',10 ) ) > 300,   'anon_lorem 2 failed';
    ASSERT length( pg_temp.anon_lorem('') ) = 0,   'anon_lorem 3 failed';
    ASSERT length( pg_temp.anon_lorem('   ',1 ) ) = 3,   'anon_lorem 4 failed';
    ASSERT         pg_temp.anon_lorem(null) IS null,   'anon_lorem 5 failed';
    ASSERT         pg_temp.anon_lorem(null, 1) IS null,   'anon_lorem 6 failed';

    RAISE NOTICE 'ANON ---> anon_phrase()';
    ASSERT length( pg_temp.anon_phrase('old') ) > 0,   'anon_phrase 1 failed';
    ASSERT length( pg_temp.anon_phrase('old', 'debug') ) > 0,   'anon_phrase 2 failed';
    ASSERT length( pg_temp.anon_phrase('') ) = 0,   'anon_phrase 3 failed';
    ASSERT length( pg_temp.anon_phrase('   ', 'debug') ) = 3,   'anon_phrase 4 failed';
    ASSERT         pg_temp.anon_phrase(null, 'debug') IS null,   'anon_phrase 5 failed';
    ASSERT         pg_temp.anon_phrase(null, null) IS null,   'anon_phrase 6 failed';

    RAISE NOTICE 'ANON ---> anon_xval()';
    ASSERT length( pg_temp.anon_xval('old') ) > 0,   'anon_xval 1 failed';
    ASSERT length( pg_temp.anon_xval('old', 'debug') ) > 0,   'anon_xval 2 failed';
    ASSERT length( pg_temp.anon_xval('') ) = 0,   'anon_xval 3 failed';
    ASSERT length( pg_temp.anon_xval('   ', 'debug') ) = 3,   'anon_xval 4 failed';
    ASSERT         pg_temp.anon_xval(null, 'debug') IS null,   'anon_xval 5 failed';
    ASSERT         pg_temp.anon_xval(null, null) IS null,   'anon_xval 6 failed';

    RAISE NOTICE 'ANON -> NO errors found';
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
    cid_note            = pg_temp.anon_lorem(cid_note)
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
    pp_note             = pg_temp.anon_lorem(pp_note),
    pp_string_value     = pg_temp.anon_phrase(pp_string_value, concat(to_char(pp_date_created, 'DD.MM.YYYY'), ', ', pp_is_public)),
    pp_url_value        = pg_temp.anon_url(pp_url_value)
WHERE pp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE platform_property SET
    plp_note            = pg_temp.anon_lorem(plp_note),
    plp_string_value    = pg_temp.anon_phrase(plp_string_value, concat(to_char(plp_date_created, 'DD.MM.YYYY'), ', ', plp_is_public)),
    plp_url_value       = pg_temp.anon_url(plp_url_value)
WHERE plp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE provider_property SET
    prp_note            = pg_temp.anon_lorem(prp_note),
    prp_string_value    = pg_temp.anon_phrase(prp_string_value, concat(to_char(prp_date_created, 'DD.MM.YYYY'), ', ', prp_is_public)),
    prp_url_value       = pg_temp.anon_url(prp_url_value)
WHERE prp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE vendor_property SET
    vp_note             = pg_temp.anon_lorem(vp_note),
    vp_string_value     = pg_temp.anon_phrase(vp_string_value, concat(to_char(vp_date_created, 'DD.MM.YYYY'), ', ', vp_is_public)),
    vp_url_value        = pg_temp.anon_url(vp_url_value)
WHERE vp_tenant_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

-- Various

-- UPDATE doc SET
--     doc_title       = pg_temp.anon_phrase(title, to_char(doc_date_created, 'DD.MM.YYYY')),
--     doc_content     = pg_temp.anon_lorem(doc_content, 5)
-- WHERE doc_content_type = 0 AND
--     doc_owner_fk NOT IN (
--         SELECT org_id FROM org WHERE org_is_beta_tester = true
--     );

UPDATE task SET
    tsk_title       = pg_temp.anon_phrase(tsk_title, to_char(tsk_date_created, 'DD.MM.YYYY')),
    tsk_description = pg_temp.anon_lorem(tsk_description, 2)
WHERE tsk_creator_fk NOT IN (
    SELECT usr_id FROM "user" WHERE usr_formal_org_fk IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);

UPDATE wf_checklist SET
    wfcl_title          = pg_temp.anon_phrase(wfcl_title, to_char(wfcl_date_created, 'DD.MM.YYYY')),
    wfcl_description    = pg_temp.anon_lorem(wfcl_description, 2),
    wfcl_comment        = pg_temp.anon_lorem(wfcl_comment)
WHERE wfcl_owner_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE wf_checkpoint SET
    wfcp_title          = pg_temp.anon_phrase(wfcp_title, wfcp_position::text),
    wfcp_description    = pg_temp.anon_lorem(wfcp_description, 2),
    wfcp_comment        = pg_temp.anon_lorem(wfcp_comment)
WHERE wfcp_checklist_fk IN (
    SELECT wfcl_id FROM wf_checklist WHERE wfcl_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);