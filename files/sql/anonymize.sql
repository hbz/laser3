
--
-- Pseudonymization (A: requirements, examples, tests)
--

-- Anon base functions

CREATE OR REPLACE FUNCTION pg_temp.anon_sample(data TEXT[] DEFAULT NULL) RETURNS TEXT AS $$
BEGIN
    IF data IS NOT NULL THEN
        RETURN data[floor(random() * cardinality(data) + 1)];
    ELSE
        RETURN data;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_generate_lorem(length INT DEFAULT 1) RETURNS TEXT AS $$
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

CREATE OR REPLACE FUNCTION pg_temp.anon_generate_phrase() RETURNS TEXT AS $$
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
    RETURN pg_temp.anon_sample(data);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_generate_person(gender TEXT DEFAULT NULL) RETURNS TEXT[] AS $$
DECLARE
    data_mn TEXT[];
    data_fn TEXT[];
    data_nn TEXT[];
    data_ln TEXT[];
BEGIN
    data_mn = ARRAY [
        'Achim', 'Adam', 'Alexander', 'Alfred', 'Anton',
        'Balduin', 'Bernd', 'Björn', 'Boris',
        'Carsten', 'Christoph', 'Cem', 'Claus',
        'Dagobert', 'Daniel', 'Didi', 'Dorian',
        'Eckard', 'Erich', 'Erwin',
        'Falko', 'Felix', 'Florian', 'Franz',
        'Georg', 'Gerd', 'Gisbert', 'Günther',
        'Hannes', 'Hans', 'Helmut', 'Herbert', 'Horst',
        'Ian', 'Igor', 'Ingolf', 'Ivan',
        'Jakob', 'Jens', 'Jochen', 'Jörg', 'Jürgen',
        'Karl', 'Kevin', 'Kim', 'Klaus', 'Knut'
        ];
    data_fn = ARRAY [
        'Agathe', 'Alina', 'Andrea', 'Anja', 'Astrid',
        'Barbara', 'Berta', 'Bettina', 'Birgit',
        'Carolin', 'Celine', 'Christine', 'Claudia',
        'Dagmar', 'Diane', 'Doris', 'Dörthe',
        'Edda', 'Elke', 'Emma',
        'Fenja', 'Flora', 'Frauke', 'Friederike',
        'Gabi', 'Gerda', 'Greta', 'Gudrun',
        'Hanna', 'Heidi', 'Heike', 'Helene', 'Hildegard',
        'Ilka', 'Inge', 'Iris', 'Isabel',
        'Jana', 'Jennifer', 'Jessica', 'Judy', 'Julia',
        'Karin', 'Karla', 'Katja', 'Kim', 'Kristina'
        ];
    data_nn = ARRAY [
        'Alex', 'Kim', 'Mika', 'Robin'
        ];
    data_ln = ARRAY [
        'Aberle', 'Ackermann', 'Althaus', 'Andersen', 'Auerbach',
        'Bachmeier', 'Birken', 'Beckmann', 'Bluhme', 'Böhmer',
        'Calmund', 'Conrad', 'Claasen', 'Cremer', 'Czajkowski',
        'Dahlberg', 'Daubner', 'Diemert', 'Dörfler', 'Dyck',
        'Ebertz', 'Edinger', 'Erdmann', 'Eschke', 'Eulert',
        'Faber', 'Fechtheim', 'Feldkamp', 'Förster', 'Fuchs',
        'Gabel', 'Geppert', 'Giesen', 'Goller', 'Gottschalk',
        'Hagedorn', 'Herschmann', 'Höfer', 'Hummel', 'Huppertz',
        'Ickert', 'Imbusch', 'Ingholt', 'Itzinger', 'Iwanowski',
        'Jacobs', 'Jenke', 'Johmann', 'Junker', 'Jürgensen',
        'Kaiser', 'Kemper', 'Kluge', 'Krause', 'Kurz',
        'Lambertz', 'Lehmann', 'Leppert', 'Lorenz', 'Lücke',
        'Maier', 'Mertens', 'Mielke', 'Morgenstern', 'Müller',
        'Nagel', 'Neuberger', 'Nitsche', 'Nordmeyer', 'Nuschke',
        'Oberhuber', 'Ohlig', 'Orlowski', 'Ostwald', 'Overkamp'
        ];

    IF lower(gender) = 'male' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_sample(data_mn), pg_temp.anon_sample(data_ln)]);
    ELSEIF lower(gender) = 'female' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_sample(data_fn), pg_temp.anon_sample(data_ln)]);
    ELSEIF lower(gender) = 'third gender' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_sample(data_nn), pg_temp.anon_sample(data_ln)]);
    ELSE
        RETURN (SELECT ARRAY[null, 'Bibo']);
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_generate_contact(type TEXT, person TEXT[] DEFAULT ARRAY['Max', 'Mustermann']) RETURNS TEXT AS $$
DECLARE
BEGIN
    IF array_position(ARRAY['email', 'e-mail', 'mail'], lower(type)) > 0 THEN
        IF trim(coalesce(person[1], '')) = '' THEN
            RETURN concat(lower(person[2]), '@anon.example');
        ELSE
            RETURN concat(lower(person[1]), '.', lower(person[2]), '@anon.example');
        END IF;
    ELSEIF array_position(ARRAY['fax', 'phone'], lower(type)) > 0  THEN
        RETURN concat('01234 - ', round(random() * 1000), ' ', round(random() * 1000), '0');
    ELSEIF array_position(ARRAY['url'], lower(type)) > 0 THEN
        RETURN pg_temp.anon_generate_url();
    ELSE
        RETURN null;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_generate_url() RETURNS TEXT AS $$
DECLARE
    part1 TEXT[];
    part2 TEXT[];
BEGIN
    part1 = ARRAY ['abc.', 'bibo.', 'data.', 'help.', 'www.', ''];
    part2 = ARRAY ['anon.', 'bit.', 'test.'];

    RETURN concat('https://', pg_temp.anon_sample(part1), pg_temp.anon_sample(part2), 'example');
END;
$$ LANGUAGE PLPGSQL;

-- Anon base functions -> examples

CREATE OR REPLACE FUNCTION pg_temp.anon_run_examples() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE 'ANON -> base functions -> EXAMPLES';

    RAISE NOTICE 'anon_sample([a, b, c) > %', (SELECT pg_temp.anon_sample(ARRAY['a', 'b', 'c']));

    RAISE NOTICE 'anon_generate_lorem() > %', (SELECT pg_temp.anon_generate_lorem());
    RAISE NOTICE 'anon_generate_lorem(3) > %', (SELECT pg_temp.anon_generate_lorem(3));

    RAISE NOTICE 'anon_generate_phrase() > %', (SELECT pg_temp.anon_generate_phrase());

    RAISE NOTICE 'anon_generate_person(male) > %', (SELECT pg_temp.anon_generate_person('male'));
    RAISE NOTICE 'anon_generate_person(female) > %', (SELECT pg_temp.anon_generate_person('female'));
    RAISE NOTICE 'anon_generate_person(third gender) > %', (SELECT pg_temp.anon_generate_person('third gender'));
    RAISE NOTICE 'anon_generate_person() > %', (SELECT pg_temp.anon_generate_person());

    RAISE NOTICE 'anon_generate_contact(mail, anon_generate_person(male)) > %', (SELECT pg_temp.anon_generate_contact('mail', pg_temp.anon_generate_person('male')));
    RAISE NOTICE 'anon_generate_contact(mail, anon_generate_person(female)) > %', (SELECT pg_temp.anon_generate_contact('mail', pg_temp.anon_generate_person('female')));
    RAISE NOTICE 'anon_generate_contact(mail, anon_generate_person(third gender)) > %', (SELECT pg_temp.anon_generate_contact('mail', pg_temp.anon_generate_person('third gender')));
    RAISE NOTICE 'anon_generate_contact(mail, anon_generate_person()) > %', (SELECT pg_temp.anon_generate_contact('mail', pg_temp.anon_generate_person()));

    RAISE NOTICE 'anon_generate_contact(phone) > %', (SELECT pg_temp.anon_generate_contact('phone'));
    RAISE NOTICE 'anon_generate_contact(url) > %', (SELECT pg_temp.anon_generate_contact('url'));

    RAISE NOTICE 'anon_generate_url(url) > %', (SELECT pg_temp.anon_generate_url());
END;
$$ LANGUAGE PLPGSQL;

SELECT pg_temp.anon_run_examples();

-- Anon use functions

CREATE OR REPLACE FUNCTION pg_temp.anon_lorem(oldValue TEXT, length INT DEFAULT 1) RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        RETURN pg_temp.anon_generate_lorem(length);
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_bigint(oldValue BIGINT) RETURNS BIGINT AS $$
BEGIN
    IF oldValue IS NOT NULL THEN
        RETURN round(oldValue * random() * 3);
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_numeric(oldValue NUMERIC) RETURNS NUMERIC AS $$
BEGIN
    IF oldValue IS NOT NULL THEN
        RETURN (oldValue * random() * 3)::numeric(19, 2);
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_phrase(oldValue TEXT, debugInfo TEXT DEFAULT '') RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        IF debugInfo != '' THEN
            RETURN concat( pg_temp.anon_generate_phrase(), ' (', debugInfo, ')' );
        ELSE
            RETURN pg_temp.anon_generate_phrase();
        END IF;
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_url(oldValue TEXT) RETURNS TEXT AS $$
BEGIN
    IF trim(coalesce(oldValue, '')) != '' THEN
        RETURN pg_temp.anon_generate_url();
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

-- Anon use functions -> tests

CREATE OR REPLACE FUNCTION pg_temp.anon_run_tests() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE 'ANON -> use functions -> TESTS';

    RAISE NOTICE 'ANON ---> anon_sample';
    ASSERT length( pg_temp.anon_sample(ARRAY['a', 'b', 'c'])) = 1,   'anon_sample 1 failed';
    ASSERT length( pg_temp.anon_sample(ARRAY[]::text[])) IS NULL,   'anon_sample 2 failed';
    ASSERT length( pg_temp.anon_sample() ) IS NULL,   'anon_sample 3 failed';

    RAISE NOTICE 'ANON ---> anon_lorem';
    ASSERT length( pg_temp.anon_lorem('old') ) > 0,   'anon_lorem 1 failed';
    ASSERT length( pg_temp.anon_lorem('old',10 ) ) > 300,   'anon_lorem 2 failed';
    ASSERT length( pg_temp.anon_lorem('') ) = 0,   'anon_lorem 3 failed';
    ASSERT length( pg_temp.anon_lorem('   ',1 ) ) = 3,   'anon_lorem 4 failed';
    ASSERT         pg_temp.anon_lorem(null) IS null,   'anon_lorem 5 failed';
    ASSERT         pg_temp.anon_lorem(null, 1) IS null,   'anon_lorem 6 failed';

    RAISE NOTICE 'ANON ---> anon_bigint';
    ASSERT         pg_temp.anon_bigint(5) > 0,   'anon_bigint 1 failed';
    ASSERT         pg_temp.anon_bigint(null) IS NULL,   'anon_bigint 2 failed';

    RAISE NOTICE 'ANON ---> anon_numeric';
    ASSERT         pg_temp.anon_numeric(5) > 0,   'anon_numeric 1 failed';
    ASSERT         pg_temp.anon_numeric(33.33) > 0,   'anon_numeric 2 failed';
    ASSERT         pg_temp.anon_numeric(null) IS NULL,   'anon_numeric 3 failed';

    RAISE NOTICE 'ANON ---> anon_phrase';
    ASSERT length( pg_temp.anon_phrase('old') ) > 0,   'anon_phrase 1 failed';
    ASSERT length( pg_temp.anon_phrase('old', 'debug') ) > 0,   'anon_phrase 2 failed';
    ASSERT length( pg_temp.anon_phrase('') ) = 0,   'anon_phrase 3 failed';
    ASSERT length( pg_temp.anon_phrase('   ', 'debug') ) = 3,   'anon_phrase 4 failed';
    ASSERT         pg_temp.anon_phrase(null, 'debug') IS null,   'anon_phrase 5 failed';
    ASSERT         pg_temp.anon_phrase(null, null) IS null,   'anon_phrase 6 failed';

    RAISE NOTICE 'ANON ---> anon_url';
    ASSERT length( pg_temp.anon_url('old') ) > 0,   'anon_url 1 failed';
    ASSERT         pg_temp.anon_url('') = '',   'anon_url 2 failed';
    ASSERT         pg_temp.anon_url('   ') = '   ',   'anon_url 3 failed';
    ASSERT         pg_temp.anon_url(null) IS NULL,   'anon_url 4 failed';

    RAISE NOTICE 'ANON ---> anon_xval';
    ASSERT length( pg_temp.anon_xval('old') ) > 0,   'anon_xval 1 failed';
    ASSERT length( pg_temp.anon_xval('old', 'debug') ) > 0,   'anon_xval 2 failed';
    ASSERT length( pg_temp.anon_xval('') ) = 0,   'anon_xval 3 failed';
    ASSERT length( pg_temp.anon_xval('   ', 'debug') ) = 3,   'anon_xval 4 failed';
    ASSERT         pg_temp.anon_xval(null, 'debug') IS null,   'anon_xval 5 failed';
    ASSERT         pg_temp.anon_xval(null, null) IS null,   'anon_xval 6 failed';

    RAISE NOTICE 'ANON -> NO errors found';
END;
$$ LANGUAGE PLPGSQL;

SELECT pg_temp.anon_run_tests();

-- Anon masking functions

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_dummy() RETURNS VOID AS $$
    DECLARE
        count_x INT;
BEGIN
    RAISE NOTICE '----- dummy -----';
    RAISE NOTICE '----- masking table x > %', count_x;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_users() RETURNS VOID AS $$
    DECLARE
        count_user INT;
        count_user_setting INT;
BEGIN
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

    GET DIAGNOSTICS count_user = ROW_COUNT;
    RAISE NOTICE '----- masking table         user (usr_username, usr_display, usr_email, usr_password, usr_enabled) > %', count_user;

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

    GET DIAGNOSTICS count_user_setting = ROW_COUNT;
    RAISE NOTICE '----- masking table user_setting (us_string_value) > %', count_user_setting;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_orgs() RETURNS VOID AS $$
    DECLARE
        count_org_setting INT;
        count_customer_identifier INT;
BEGIN
    UPDATE org_setting SET
        os_string_value     = pg_temp.anon_xval(os_string_value)
    WHERE os_key_enum IN ('API_PASSWORD', 'NATSTAT_SERVER_API_KEY', 'NATSTAT_SERVER_REQUESTOR_ID', 'LASERSTAT_SERVER_KEY') AND
        os_org_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_org_setting = ROW_COUNT;
    RAISE NOTICE '----- masking table         org_setting (os_string_value) > %', count_org_setting;

    UPDATE customer_identifier SET
        cid_value           = pg_temp.anon_xval(cid_value),
        cid_requestor_key   = pg_temp.anon_xval(cid_requestor_key),
        cid_note            = pg_temp.anon_lorem(cid_note)
    WHERE cid_customer_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_customer_identifier = ROW_COUNT;
    RAISE NOTICE '----- masking table customer_identifier (cid_value, cid_requestor_key, cid_note) > %', count_customer_identifier;
END;
$$ LANGUAGE PLPGSQL;

-- TODO
CREATE OR REPLACE FUNCTION pg_temp.anon_mask_person_and_contact() RETURNS VOID AS $$
DECLARE
    count_person INT;
    count_contact INT;
    pp RECORD;
    pp_person TEXT[];
    co RECORD;
    co_contact TEXT;
BEGIN
    RAISE NOTICE '----- TODO: SELECT ONLY /  -----';

    FOR pp IN
        SELECT * FROM person WHERE
            prs_is_public = false AND
            prs_tenant_fk NOT IN (
                SELECT org_id FROM org WHERE org_is_beta_tester = true
            )
    LOOP
        SELECT pg_temp.anon_generate_person(CASE
            WHEN pp.prs_gender_rv_fk = 62    THEN 'female'
            WHEN pp.prs_gender_rv_fk = 63    THEN 'male'
            WHEN pp.prs_gender_rv_fk = 64    THEN 'third gender'
            ELSE NULL END) INTO pp_person;

--         RAISE NOTICE '%s %s', pp_person;

        FOR co IN
            SELECT * FROM contact WHERE
                ct_prs_fk = pp.prs_id
        LOOP
            SELECT pg_temp.anon_generate_contact(CASE
                WHEN co.ct_content_type_rv_fk = 16  THEN 'mail'
                WHEN co.ct_content_type_rv_fk = 17  THEN 'phone'
                WHEN co.ct_content_type_rv_fk = 18  THEN 'fax'
                WHEN co.ct_content_type_rv_fk = 718 THEN 'url'
                WHEN co.ct_content_type_rv_fk = 1563 THEN 'phone'
                WHEN co.ct_content_type_rv_fk = 1564 THEN 'url'
                ELSE NULL END, pp_person) INTO co_contact;

            RAISE NOTICE '(%, %, %, %) + (% % %) > [% %]',
                pp.prs_id, pp.prs_first_name, pp.prs_last_name, pp.prs_gender_rv_fk,
                co.ct_id, co.ct_content_type_rv_fk, co.ct_content,
                pp_person, co_contact;
        END LOOP;
    END LOOP;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_properties() RETURNS VOID AS $$
    DECLARE
        count_org_property INT;
        count_person_property INT;
        count_platform_property INT;
        count_provider_property INT;
        count_vendor_property INT;
BEGIN
    RAISE NOTICE '----- TODO: license_property -----';
    RAISE NOTICE '----- TODO: license_property -----';

    UPDATE org_property SET
        op_note             = pg_temp.anon_lorem(op_note),
        op_string_value     = pg_temp.anon_phrase(op_string_value, concat(to_char(op_date_created, 'DD.MM.YYYY'), ', ', op_is_public)),
        op_url_value        = pg_temp.anon_url(op_url_value),
        op_long_value       = pg_temp.anon_bigint(op_long_value),
        op_dec_value        = pg_temp.anon_numeric(op_dec_value)
    WHERE op_tenant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_org_property = ROW_COUNT;
    RAISE NOTICE '----- masking table      org_property (op_note,  op_string_value,  op_url_value,  op_long_value,  op_dec_value) > %', count_org_property;

    UPDATE person_property SET
        pp_note             = pg_temp.anon_lorem(pp_note),
        pp_string_value     = pg_temp.anon_phrase(pp_string_value, concat(to_char(pp_date_created, 'DD.MM.YYYY'), ', ', pp_is_public)),
        pp_url_value        = pg_temp.anon_url(pp_url_value),
        pp_long_value       = pg_temp.anon_bigint(pp_long_value),
        pp_dec_value        = pg_temp.anon_numeric(pp_dec_value)
    WHERE pp_tenant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_person_property = ROW_COUNT;
    RAISE NOTICE '----- masking table   person_property (pp_note,  pp_string_value,  pp_url_value,  pp_long_value,  pp_dec_value) > %', count_person_property;

    UPDATE platform_property SET
        plp_note            = pg_temp.anon_lorem(plp_note),
        plp_string_value    = pg_temp.anon_phrase(plp_string_value, concat(to_char(plp_date_created, 'DD.MM.YYYY'), ', ', plp_is_public)),
        plp_url_value       = pg_temp.anon_url(plp_url_value),
        plp_long_value      = pg_temp.anon_bigint(plp_long_value),
        plp_dec_value       = pg_temp.anon_numeric(plp_dec_value)
    WHERE plp_tenant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_platform_property = ROW_COUNT;
    RAISE NOTICE '----- masking table platform_property (plp_note, plp_string_value, plp_url_value, plp_long_value, plp_dec_value) > %', count_platform_property;

    UPDATE provider_property SET
        prp_note            = pg_temp.anon_lorem(prp_note),
        prp_string_value    = pg_temp.anon_phrase(prp_string_value, concat(to_char(prp_date_created, 'DD.MM.YYYY'), ', ', prp_is_public)),
        prp_url_value       = pg_temp.anon_url(prp_url_value),
        prp_long_value      = pg_temp.anon_bigint(prp_long_value),
        prp_dec_value       = pg_temp.anon_numeric(prp_dec_value)
    WHERE prp_tenant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_provider_property = ROW_COUNT;
    RAISE NOTICE '----- masking table provider_property (prp_note, prp_string_value, prp_url_value, prp_long_value, prp_dec_value) > %', count_provider_property;

    UPDATE vendor_property SET
        vp_note             = pg_temp.anon_lorem(vp_note),
        vp_string_value     = pg_temp.anon_phrase(vp_string_value, concat(to_char(vp_date_created, 'DD.MM.YYYY'), ', ', vp_is_public)),
        vp_url_value        = pg_temp.anon_url(vp_url_value),
        vp_long_value       = pg_temp.anon_bigint(vp_long_value),
        vp_dec_value        = pg_temp.anon_numeric(vp_dec_value)
    WHERE vp_tenant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_vendor_property = ROW_COUNT;
    RAISE NOTICE '----- masking table   vendor_property (vp_note,  vp_string_value,  vp_url_value,  vp_long_value,  vp_dec_value) > %', count_vendor_property;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_finance() RETURNS VOID AS $$
    DECLARE
        count_cost_item_1 INT;
        count_cost_item_2 INT;
BEGIN
    UPDATE cost_item SET
        ci_cost_in_billing_currency = pg_temp.anon_numeric(ci_cost_in_billing_currency::numeric)
    WHERE
        ci_cost_in_billing_currency IS NOT NULL AND
        ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_cost_item_1 = ROW_COUNT;

    UPDATE cost_item SET
        ci_cost_in_local_currency = round(cast(ci_cost_in_billing_currency * ci_currency_rate AS NUMERIC), 2)
    WHERE
        ci_cost_in_billing_currency IS NOT NULL AND
        ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_cost_item_2 = ROW_COUNT;
    RAISE NOTICE '----- masking table cost_item (ci_cost_in_billing_currency, ci_cost_in_local_currency) > %, %', count_cost_item_1, count_cost_item_2;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_tasks() RETURNS VOID AS $$
    DECLARE
        count_task INT;
BEGIN
    UPDATE task SET
        tsk_title       = pg_temp.anon_phrase(tsk_title, to_char(tsk_date_created, 'DD.MM.YYYY')),
        tsk_description = pg_temp.anon_lorem(tsk_description, 2)
    WHERE tsk_creator_fk NOT IN (
        SELECT usr_id FROM "user" WHERE usr_formal_org_fk IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        )
    );

    GET DIAGNOSTICS count_task = ROW_COUNT;
    RAISE NOTICE '----- masking table task (tsk_title, tsk_description) > %', count_task;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_workflows() RETURNS VOID AS $$
    DECLARE
        count_wf_checklist INT;
        count_wf_checkpoint INT;
BEGIN
    UPDATE wf_checklist SET
        wfcl_title          = pg_temp.anon_phrase(wfcl_title, to_char(wfcl_date_created, 'DD.MM.YYYY')),
        wfcl_description    = pg_temp.anon_lorem(wfcl_description, 2),
        wfcl_comment        = pg_temp.anon_lorem(wfcl_comment)
    WHERE wfcl_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_wf_checklist = ROW_COUNT;
    RAISE NOTICE '----- masking table  wf_checklist (wfcl_title, wfcl_description, wfcl_comment) > %', count_wf_checklist;

    UPDATE wf_checkpoint SET
        wfcp_title          = pg_temp.anon_phrase(wfcp_title, wfcp_position::text),
        wfcp_description    = pg_temp.anon_lorem(wfcp_description, 2),
        wfcp_comment        = pg_temp.anon_lorem(wfcp_comment)
    WHERE wfcp_checklist_fk IN (
        SELECT wfcl_id FROM wf_checklist WHERE wfcl_owner_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        )
    );

    GET DIAGNOSTICS count_wf_checkpoint = ROW_COUNT;
    RAISE NOTICE '----- masking table wf_checkpoint (wfcp_title, wfcp_description, wfcp_comment) > %', count_wf_checkpoint;
END;
$$ LANGUAGE PLPGSQL;

--
-- Pseudonymization (B: data manipulation)
--

-- for LOCAL only

UPDATE org SET
    org_is_beta_tester = false
WHERE org_guid NOT IN ('org:e6be24ff-98e4-474d-9ef8-f0eafd843d17', 'org:1d72afe7-67cb-4676-add0-51d3ae66b1b3');

-- masking

SELECT pg_temp.anon_mask_users();               -- Users

SELECT pg_temp.anon_mask_orgs();                -- Orgs

SELECT pg_temp.anon_mask_person_and_contact();  -- Addressbook -- TODO

SELECT pg_temp.anon_mask_finance();             -- Finance

SELECT pg_temp.anon_mask_properties();          -- Properties -- TODO

SELECT pg_temp.anon_mask_tasks();               -- Tasks

SELECT pg_temp.anon_mask_workflows();           -- Workflows

-- Various

-- UPDATE doc SET
--     doc_title       = pg_temp.anon_phrase(title, to_char(doc_date_created, 'DD.MM.YYYY')),
--     doc_content     = pg_temp.anon_lorem(doc_content, 5)
-- WHERE doc_content_type = 0 AND
--     doc_owner_fk NOT IN (
--         SELECT org_id FROM org WHERE org_is_beta_tester = true
--     );
