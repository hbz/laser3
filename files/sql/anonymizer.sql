--
-- laser.anonymizer (1. requirements, examples, tests)
--

-- la functions: helper

CREATE OR REPLACE FUNCTION pg_temp.anon_log_mask(text TEXT, count INT) RETURNS TEXT AS $$
BEGIN
    RETURN concat('--- ', lpad(concat(' ', count::TEXT), 8, '-'), ' --- masked @ ', text);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_log_delete(text TEXT, count INT) RETURNS TEXT AS $$
BEGIN
    RETURN concat('--- ', lpad(concat(' ', count::TEXT), 8, '-'), ' --- deleted @ ', text);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_get_sample(data TEXT[] DEFAULT NULL) RETURNS TEXT AS $$
BEGIN
    IF data IS NOT NULL THEN
        RETURN data[floor(random() * cardinality(data) + 1)];
    ELSE
        RETURN data;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

-- la functions: generation

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
    RETURN pg_temp.anon_get_sample(data);
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
        'Achim', 'Adam', 'Alexander', 'Alfred', 'Anton', 'Arnold',
        'Balduin', 'Bernd', 'Björn', 'Boris',
        'Carsten', 'Christoph', 'Cem', 'Claus',
        'Dagobert', 'Daniel', 'Denis', 'Didi', 'Dorian',
        'Eckhart', 'Edmund', 'Elvis', 'Erich', 'Ersun', 'Erwin',
        'Falko', 'Felix', 'Florian', 'Franz',
        'Georg', 'Gerd', 'Gisbert', 'Günther',
        'Hannes', 'Hans', 'Helmut', 'Herbert', 'Horst',
        'Ian', 'Igor', 'Ingolf', 'Ivan',
        'Jakob', 'Jens', 'Jochen', 'Jörg', 'Jürgen',
        'Karl', 'Karsten', 'Kevin', 'Klaus', 'Knut',
        'Lars', 'Lawrence', 'Leif', 'Louis',
        'Markus', 'Mario', 'Max', 'Michael', 'Moritz',
        'Nicolas', 'Nils', 'Nino', 'Norbert',
        'Olaf', 'Oliver', 'Otto',
        'Paul', 'Peter', 'Philipp', 'Piet'
        ];
    data_fn = ARRAY [
        'Agathe', 'Alina', 'Andrea', 'Anja', 'Anna', 'Astrid',
        'Barbara', 'Berta', 'Bettina', 'Birgit',
        'Carolin', 'Celine', 'Christine', 'Claudia',
        'Dagmar', 'Daniela', 'Diane', 'Doris', 'Dörthe',
        'Edda', 'Edith', 'Eike', 'Elke', 'Emma', 'Ester',
        'Fenja', 'Flora', 'Frauke', 'Friederike',
        'Gabi', 'Gerda', 'Greta', 'Gudrun',
        'Hanna', 'Heidi', 'Heike', 'Helene', 'Hildegard',
        'Ilka', 'Inge', 'Iris', 'Isabel',
        'Jana', 'Jennifer', 'Jessica', 'Judy', 'Julia',
        'Karin', 'Karla', 'Katja', 'Klaudia', 'Kristina',
        'Laura', 'Lena', 'Linda', 'Louise',
        'Maike', 'Marie', 'Margot', 'Mia', 'Monika',
        'Nadja', 'Naomi', 'Nicole', 'Nina',
        'Oksana', 'Olga', 'Olivia',
        'Paula', 'Petra', 'Pia', 'Pina'
        ];
    data_nn = ARRAY [
        'Alex', 'Dido', 'Kim', 'Mika', 'Robin'
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
        'Oberhuber', 'Ohlig', 'Orlowski', 'Ostwald', 'Overkamp',
        'Pampel', 'Pellnitz', 'Pirner', 'Posewitz', 'Püttmann'
        ];

    IF lower(gender) = 'male' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_get_sample(data_mn), pg_temp.anon_get_sample(data_ln)]);
    ELSEIF lower(gender) = 'female' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_get_sample(data_fn), pg_temp.anon_get_sample(data_ln)]);
    ELSEIF lower(gender) = 'third gender' THEN
        RETURN (SELECT ARRAY[pg_temp.anon_get_sample(data_nn), pg_temp.anon_get_sample(data_ln)]);
    ELSE
        RETURN (SELECT ARRAY[null, concat('Bibo', floor(random() * 1000))]);
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
        RETURN concat('01234 - ', round(random() * 1000), ' 0', round(random() * 1000));
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

    RETURN concat('https://', pg_temp.anon_get_sample(part1), pg_temp.anon_get_sample(part2), 'example');
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_test_generation() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE '--- laser.anonymizer';
    RAISE NOTICE '--- ---> testing generation';

    RAISE NOTICE 'anon_log_mask(test, 123) > %', pg_temp.anon_log_mask('test', 123);

    RAISE NOTICE 'anon_get_sample([a, b, c) > %', (SELECT pg_temp.anon_get_sample(ARRAY['a', 'b', 'c']));
--     RAISE NOTICE 'anon_get_sample([]) > %', (SELECT pg_temp.anon_get_sample(ARRAY[])); -- FAIL

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

-- SELECT pg_temp.anon_test_generation();

-- la functions: overwrite

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
DECLARE
    digits TEXT = '1234567890123456789012345';
    size INT;
BEGIN
    IF oldValue IS NOT NULL THEN
        size = length(round(oldValue)::TEXT);
        RETURN concat(substr(digits, 1, size), '.', substr(digits, size + 1, 2))::NUMERIC(19, 2);
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
            RETURN concat( 'X', substr(md5(random()::TEXT), 1, 8), ' (', debugInfo, ')' );
        ELSE
            RETURN concat( 'X', substr(md5(random()::TEXT), 1, 8));
        END IF;
    ELSE
        RETURN oldValue;
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_test_overwrite() RETURNS VOID AS $$
BEGIN
    RAISE NOTICE '--- laser.anonymizer';
    RAISE NOTICE '--- ---> testing overwrite';

    RAISE NOTICE '--- anon_get_sample: 3';
    ASSERT length( pg_temp.anon_get_sample(ARRAY['a', 'b', 'c'])) = 1,  'anon_get_sample 1 failed';
    ASSERT length( pg_temp.anon_get_sample(ARRAY[]::TEXT[])) IS NULL,   'anon_get_sample 2 failed';
    ASSERT length( pg_temp.anon_get_sample() ) IS NULL,                 'anon_get_sample 3 failed';

    RAISE NOTICE '--- anon_lorem: 6';
    ASSERT length( pg_temp.anon_lorem('old') ) > 0,                 'anon_lorem 1 failed';
    ASSERT length( pg_temp.anon_lorem('old',10 ) ) > 300,           'anon_lorem 2 failed';
    ASSERT length( pg_temp.anon_lorem('') ) = 0,                    'anon_lorem 3 failed';
    ASSERT length( pg_temp.anon_lorem('   ',1 ) ) = 3,              'anon_lorem 4 failed';
    ASSERT         pg_temp.anon_lorem(null) IS null,                'anon_lorem 5 failed';
    ASSERT         pg_temp.anon_lorem(null, 1) IS null,             'anon_lorem 6 failed';

    RAISE NOTICE '--- anon_bigint: 2';
    ASSERT         pg_temp.anon_bigint(5) > 0,          'anon_bigint 1 failed';
    ASSERT         pg_temp.anon_bigint(null) IS NULL,   'anon_bigint 2 failed';

    RAISE NOTICE '--- anon_numeric: 7';
    ASSERT         pg_temp.anon_numeric(5)::TEXT = '1.23',                  'anon_numeric 1 failed';
    ASSERT         pg_temp.anon_numeric(3.3)::TEXT = '1.23',                'anon_numeric 2 failed';
    ASSERT         pg_temp.anon_numeric(333.333)::TEXT = '123.45',          'anon_numeric 3 failed';
    ASSERT         pg_temp.anon_numeric(12345678)::TEXT = '12345678.90',    'anon_numeric 4 failed';
    ASSERT         pg_temp.anon_numeric(12345678.9)::TEXT = '12345678.90',  'anon_numeric 5 failed';
    ASSERT         pg_temp.anon_numeric(12345678.12)::TEXT = '12345678.90', 'anon_numeric 6 failed';
    ASSERT         pg_temp.anon_numeric(null) IS NULL,                      'anon_numeric 7 failed';

    RAISE NOTICE '--- anon_phrase: 6';
    ASSERT length( pg_temp.anon_phrase('old') ) > 0,            'anon_phrase 1 failed';
    ASSERT length( pg_temp.anon_phrase('old', 'debug') ) > 0,   'anon_phrase 2 failed';
    ASSERT length( pg_temp.anon_phrase('') ) = 0,               'anon_phrase 3 failed';
    ASSERT length( pg_temp.anon_phrase('   ', 'debug') ) = 3,   'anon_phrase 4 failed';
    ASSERT         pg_temp.anon_phrase(null, 'debug') IS null,  'anon_phrase 5 failed';
    ASSERT         pg_temp.anon_phrase(null, null) IS null,     'anon_phrase 6 failed';

    RAISE NOTICE '--- anon_url: 4';
    ASSERT length( pg_temp.anon_url('old') ) > 0,       'anon_url 1 failed';
    ASSERT         pg_temp.anon_url('') = '',           'anon_url 2 failed';
    ASSERT         pg_temp.anon_url('   ') = '   ',     'anon_url 3 failed';
    ASSERT         pg_temp.anon_url(null) IS NULL,      'anon_url 4 failed';

    RAISE NOTICE '--- anon_xval: 6';
    ASSERT length( pg_temp.anon_xval('old') ) > 0,              'anon_xval 1 failed';
    ASSERT length( pg_temp.anon_xval('old', 'debug') ) > 0,     'anon_xval 2 failed';
    ASSERT length( pg_temp.anon_xval('') ) = 0,                 'anon_xval 3 failed';
    ASSERT length( pg_temp.anon_xval('   ', 'debug') ) = 3,     'anon_xval 4 failed';
    ASSERT         pg_temp.anon_xval(null, 'debug') IS null,    'anon_xval 5 failed';
    ASSERT         pg_temp.anon_xval(null, null) IS null,       'anon_xval 6 failed';

    RAISE NOTICE '--- ---> NO errors found';
END;
$$ LANGUAGE PLPGSQL;

-- SELECT pg_temp.anon_test_overwrite();

-- la functions: masking

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_dummy() RETURNS VOID AS $$
    DECLARE
        count_x INT;
BEGIN
    RAISE NOTICE '----- dummy -----';
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_users() RETURNS VOID AS $$
    DECLARE
        count_user INT;
        count_user_setting INT;
BEGIN
    UPDATE "user" SET
        usr_username    = concat('user', usr_id),
        usr_display     = concat('User #', usr_id),
        usr_email       = concat('user', usr_id, '@anon.local'),
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
    RAISE NOTICE '%', pg_temp.anon_log_mask('user (usr_username, usr_display, usr_email, usr_password, usr_enabled)', count_user);

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
    RAISE NOTICE '%', pg_temp.anon_log_mask('user_setting (us_string_value)', count_user_setting);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_orgs(ignoreCustomerIdentifier BOOL DEFAULT FALSE) RETURNS VOID AS $$
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
    RAISE NOTICE '%', pg_temp.anon_log_mask('org_setting (os_string_value)', count_org_setting);

    IF ignoreCustomerIdentifier IS FALSE THEN
        UPDATE customer_identifier SET
            cid_value           = pg_temp.anon_xval(cid_value),
            cid_requestor_key   = pg_temp.anon_xval(cid_requestor_key),
            cid_note            = pg_temp.anon_lorem(cid_note)
        WHERE cid_customer_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

        GET DIAGNOSTICS count_customer_identifier = ROW_COUNT;
        RAISE NOTICE '%', pg_temp.anon_log_mask('customer_identifier (cid_value, cid_requestor_key, cid_note)', count_customer_identifier);
    END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_addressbook() RETURNS VOID AS $$
DECLARE
    count_person INT;
    count_contact INT;
    count_tmp INT;
    prs RECORD;
    prs_person TEXT[];
BEGIN
    count_person = 0;
    count_contact = 0;

    FOR prs IN
        SELECT * FROM person WHERE
            prs_is_public = false AND
            prs_tenant_fk NOT IN (
                SELECT org_id FROM org WHERE org_is_beta_tester = true
            )
    LOOP
        SELECT pg_temp.anon_generate_person(CASE
            WHEN prs.prs_gender_rv_fk = 62    THEN 'female'
            WHEN prs.prs_gender_rv_fk = 63    THEN 'male'
            WHEN prs.prs_gender_rv_fk = 64    THEN 'third gender'
            ELSE NULL END) INTO prs_person;

        UPDATE person SET
            prs_first_name = prs_person[1],
            prs_last_name = prs_person[2]
        WHERE prs_id = prs.prs_id;

        GET DIAGNOSTICS count_tmp = ROW_COUNT;
        count_person = count_person + count_tmp;

        UPDATE contact SET
            ct_content = pg_temp.anon_generate_contact(CASE
                WHEN ct_content_type_rv_fk = 16  THEN 'mail'
                WHEN ct_content_type_rv_fk = 17  THEN 'phone'
                WHEN ct_content_type_rv_fk = 18  THEN 'fax'
                WHEN ct_content_type_rv_fk = 718 THEN 'url'
                WHEN ct_content_type_rv_fk = 1563 THEN 'phone'
                WHEN ct_content_type_rv_fk = 1564 THEN 'url'
                ELSE NULL END, prs_person
            )
        WHERE ct_prs_fk = prs.prs_id;

        GET DIAGNOSTICS count_tmp = ROW_COUNT;
        count_contact = count_contact + count_tmp;
    END LOOP;

    RAISE NOTICE '%', pg_temp.anon_log_mask('person (prs_first_name, prs_last_name)', count_person);
    RAISE NOTICE '%', pg_temp.anon_log_mask('contact (ct_content)', count_contact);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_properties1() RETURNS VOID AS $$
    DECLARE
        count_org_property INT;
        count_person_property INT;
        count_platform_property INT;
        count_provider_property INT;
        count_vendor_property INT;
BEGIN
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
    RAISE NOTICE '%', pg_temp.anon_log_mask('org_property (op_note, op_string_value, op_url_value, op_long_value, op_dec_value)', count_org_property);

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
    RAISE NOTICE '%', pg_temp.anon_log_mask('person_property (pp_note, pp_string_value, pp_url_value, pp_long_value, pp_dec_value)', count_person_property);

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
    RAISE NOTICE '%', pg_temp.anon_log_mask('platform_property (plp_note, plp_string_value, plp_url_value, plp_long_value, plp_dec_value)', count_platform_property);

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
    RAISE NOTICE '%', pg_temp.anon_log_mask('provider_property (prp_note, prp_string_value, prp_url_value, prp_long_value, prp_dec_value)', count_provider_property);

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
    RAISE NOTICE '%', pg_temp.anon_log_mask('vendor_property (vp_note, vp_string_value, vp_url_value, vp_long_value, vp_dec_value)', count_vendor_property);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_properties2() RETURNS VOID AS $$
    DECLARE
        count_sub_prop INT;
        count_sub_prop_child INT;
        count_lic_prop INT;
        count_lic_prop_child INT;
BEGIN
    UPDATE subscription_property SET
        sp_note             = pg_temp.anon_lorem(sp_note),
        sp_string_value     = pg_temp.anon_phrase(sp_string_value, concat(sp_id, ', ', to_char(sp_date_created, 'DD.MM.YYYY'), ', ', sp_is_public)),
        sp_url_value        = pg_temp.anon_url(sp_url_value),
        sp_long_value       = pg_temp.anon_bigint(sp_long_value),
        sp_dec_value        = pg_temp.anon_numeric(sp_dec_value)
    WHERE
        sp_instance_of_fk IS NULL AND
        sp_tenant_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_sub_prop = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('subscription_property (sp_note, sp_string_value, sp_url_value, sp_long_value, sp_dec_value)', count_sub_prop);

    UPDATE subscription_property child SET
        sp_note             = parent.sp_note,
        sp_string_value     = parent.sp_string_value,
        sp_url_value        = parent.sp_url_value,
        sp_long_value       = parent.sp_long_value,
        sp_dec_value        = parent.sp_dec_value
    FROM subscription_property parent
    WHERE
        parent.sp_id = child.sp_instance_of_fk AND
        parent.sp_instance_of_fk IS NULL AND
        parent.sp_tenant_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_sub_prop_child = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('subscription_property ~ children', count_sub_prop_child);

    UPDATE license_property SET
        lp_note             = pg_temp.anon_lorem(lp_note),
        lp_string_value     = pg_temp.anon_phrase(lp_string_value, concat(lp_id, ', ', to_char(lp_date_created, 'DD.MM.YYYY'), ', ', lp_is_public)),
        lp_url_value        = pg_temp.anon_url(lp_url_value),
        lp_long_value       = pg_temp.anon_bigint(lp_long_value),
        lp_dec_value        = pg_temp.anon_numeric(lp_dec_value),
        lp_paragraph        = pg_temp.anon_lorem(lp_paragraph, 3),
        lp_paragraph_number = pg_temp.anon_xval(lp_paragraph_number)
    WHERE
        lp_instance_of_fk IS NULL AND
        lp_tenant_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_lic_prop = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('license_property (lp_note, lp_string_value, lp_url_value, lp_long_value, lp_dec_value, lp_paragraph, lp_paragraph_number)', count_lic_prop);

    UPDATE license_property child SET
        lp_note             = parent.lp_note,
        lp_string_value     = parent.lp_string_value,
        lp_url_value        = parent.lp_url_value,
        lp_long_value       = parent.lp_long_value,
        lp_dec_value        = parent.lp_dec_value,
        lp_paragraph        = parent.lp_paragraph,
        lp_paragraph_number = parent.lp_paragraph_number
    FROM license_property parent
    WHERE
        parent.lp_id = child.lp_instance_of_fk AND
        parent.lp_instance_of_fk IS NULL AND
        parent.lp_tenant_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_lic_prop_child = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('license_property ~ children', count_lic_prop_child);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_finance() RETURNS VOID AS $$
    DECLARE
        count_cost_item_1 INT;
        count_cost_item_2 INT;
BEGIN
    UPDATE cost_item SET
        ci_cost_in_billing_currency = pg_temp.anon_numeric(ci_cost_in_billing_currency::NUMERIC)
    WHERE
        ci_cost_in_billing_currency IS NOT NULL AND
        ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_cost_item_1 = ROW_COUNT;

    UPDATE cost_item SET
        ci_cost_in_local_currency = round(cast(ci_cost_in_billing_currency * ci_currency_rate AS NUMERIC), 2)::NUMERIC
    WHERE
        ci_cost_in_billing_currency IS NOT NULL AND
        ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_cost_item_2 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('cost_item (ci_cost_in_billing_currency)', count_cost_item_1);
    RAISE NOTICE '%', pg_temp.anon_log_mask('cost_item (ci_cost_in_local_currency)', count_cost_item_2);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_docs() RETURNS VOID AS $$
DECLARE
    count_doc INT;
BEGIN
    UPDATE doc SET
        doc_title       = pg_temp.anon_phrase(doc_title, to_char(doc_date_created, 'DD.MM.YYYY')),
        doc_content     = pg_temp.anon_lorem(doc_content, 4)
    WHERE doc_content_type = 0 AND
        doc_owner_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );

    GET DIAGNOSTICS count_doc = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('doc (doc_title, doc_content)', count_doc);
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
    RAISE NOTICE '%', pg_temp.anon_log_mask('task (tsk_title, tsk_description)', count_task);
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
    RAISE NOTICE '%', pg_temp.anon_log_mask('wf_checklist (wfcl_title, wfcl_description, wfcl_comment)', count_wf_checklist);

    UPDATE wf_checkpoint SET
        wfcp_title          = pg_temp.anon_phrase(wfcp_title, wfcp_position::TEXT),
        wfcp_description    = pg_temp.anon_lorem(wfcp_description, 2),
        wfcp_comment        = pg_temp.anon_lorem(wfcp_comment)
    WHERE wfcp_checklist_fk IN (
        SELECT wfcl_id FROM wf_checklist WHERE wfcl_owner_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        )
    );

    GET DIAGNOSTICS count_wf_checkpoint = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('wf_checkpoint (wfcp_title, wfcp_description, wfcp_comment)', count_wf_checkpoint);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION pg_temp.anon_mask_surveys() RETURNS VOID AS $$
DECLARE
    count_survey_info INT;
    count_survey_config INT;
    count_survey_org INT;
    count_survey_url INT;
    count_survey_result1 INT;
    count_survey_result2 INT;
    count_survey_result3 INT;
    count_survey_package_result1 INT;
    count_survey_package_result2 INT;
    count_survey_subscription_result1 INT;
    count_survey_subscription_result2 INT;
    count_survey_vendor_result1 INT;
    count_survey_vendor_result2 INT;
BEGIN
    UPDATE survey_info SET
        surin_comment = pg_temp.anon_lorem(surin_comment)
    WHERE survey_info.surin_owner_org_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_info = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_info (surin_comment)', count_survey_info);

    UPDATE survey_config SET
        surconf_comment                         = pg_temp.anon_lorem(surconf_comment, 2),
        surconf_internal_comment                = pg_temp.anon_lorem(surconf_internal_comment),
        surconf_comment_for_new_participants    = pg_temp.anon_lorem(surconf_comment_for_new_participants, 3)
    WHERE surconf_surinfo_fk NOT IN (
        SELECT surin_id FROM survey_info
            JOIN org ON surin_owner_org_fk = org_id
        WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_config = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_config (surconf_comment, surconf_internal_comment, surconf_comment_for_new_participants)', count_survey_config);

    UPDATE survey_org SET
        surorg_owner_comment    = pg_temp.anon_lorem(surorg_owner_comment),
        surorg_pricecomment     = pg_temp.anon_lorem(surorg_pricecomment)
    WHERE surorg_surveyconfig_fk NOT IN (
        SELECT surconf_id FROM survey_config
           JOIN survey_info ON surconf_surinfo_fk = surin_id
           JOIN org ON surin_owner_org_fk = org_id
        WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_org = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_org (surorg_owner_comment, surorg_pricecomment)', count_survey_org);

    UPDATE survey_url SET
        surur_url_comment = pg_temp.anon_lorem(surur_url_comment)
    WHERE surur_survey_config_fk NOT IN (
        SELECT surconf_id FROM survey_config
            JOIN survey_info ON surconf_surinfo_fk = surin_id
            JOIN org ON surin_owner_org_fk = org_id
        WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_url = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_url (surur_url_comment)', count_survey_url);

    UPDATE survey_result SET
        surre_note                  = pg_temp.anon_lorem(surre_note),
        surre_string_value          = pg_temp.anon_phrase(surre_string_value, concat(surre_id, ', ', to_char(surre_date_created, 'DD.MM.YYYY'), ', ', surre_is_public)),
        surre_url_value             = pg_temp.anon_url(surre_url_value),
        surre_long_value            = pg_temp.anon_bigint(surre_long_value),
        surre_dec_value             = pg_temp.anon_numeric(surre_dec_value)
    WHERE surre_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    ) OR surre_participant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_result1 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_result (surre_note, surre_string_value, surre_url_value, surre_long_value, surre_dec_value)', count_survey_result1);

    UPDATE survey_result SET
         surre_comment          = pg_temp.anon_lorem(surre_comment),
         surre_owner_comment    = pg_temp.anon_lorem(surre_owner_comment)
    WHERE surre_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_result2 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_result (surre_comment, surre_owner_comment)', count_survey_result2);

    UPDATE survey_result SET
         surre_participant_comment  = pg_temp.anon_lorem(surre_participant_comment)
    WHERE surre_participant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_result3 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_result (surre_participant_comment)', count_survey_result3);

    UPDATE survey_package_result SET
        surpkgre_comment        = pg_temp.anon_lorem(surpkgre_comment),
        surpkgre_owner_comment  = pg_temp.anon_lorem(surpkgre_owner_comment)
    WHERE surpkgre_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_package_result1 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_package_result (surpkgre_comment, surpkgre_owner_comment)', count_survey_package_result1);

    UPDATE survey_package_result SET
        surpkgre_participant_comment = pg_temp.anon_lorem(surpkgre_participant_comment)
    WHERE surpkgre_participant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_package_result2 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_package_result (surpkgre_participant_comment)', count_survey_package_result2);

    UPDATE survey_subscription_result SET
        sursubre_comment        = pg_temp.anon_lorem(sursubre_comment),
        sursubre_owner_comment  = pg_temp.anon_lorem(sursubre_owner_comment)
    WHERE sursubre_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_subscription_result1 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_subscription_result (sursubre_comment, sursubre_owner_comment)', count_survey_subscription_result1);

    UPDATE survey_subscription_result SET
        sursubre_participant_comment = pg_temp.anon_lorem(sursubre_participant_comment)
    WHERE sursubre_participant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_subscription_result2 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_subscription_result (sursubre_participant_comment)', count_survey_subscription_result2);

    UPDATE survey_vendor_result SET
        survenre_comment        = pg_temp.anon_lorem(survenre_comment),
        survenre_owner_comment  = pg_temp.anon_lorem(survenre_owner_comment)
    WHERE survenre_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_vendor_result1 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_vendor_result (survenre_comment, survenre_owner_comment)', count_survey_vendor_result1);

    UPDATE survey_vendor_result SET
        survenre_participant_comment = pg_temp.anon_lorem(survenre_participant_comment)
    WHERE survenre_participant_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

    GET DIAGNOSTICS count_survey_vendor_result2 = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_mask('survey_vendor_result (survenre_participant_comment)', count_survey_vendor_result2);

END;
$$ LANGUAGE PLPGSQL;

-- la functions: deletion

CREATE OR REPLACE FUNCTION pg_temp.anon_delete_mails() RETURNS VOID AS $$
DECLARE
    count_tmp INT;
BEGIN
    DELETE FROM async_mail_attachment;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_attachment', count_tmp);

    DELETE FROM async_mail_header;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_header', count_tmp);

    DELETE FROM async_mail_bcc;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_bcc', count_tmp);

    DELETE FROM async_mail_cc;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_cc', count_tmp);

    DELETE FROM async_mail_to;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_to', count_tmp);

    DELETE FROM async_mail_mess;
    GET DIAGNOSTICS count_tmp = ROW_COUNT;
    RAISE NOTICE '%', pg_temp.anon_log_delete('async_mail_mess', count_tmp);
END;
$$ LANGUAGE PLPGSQL;

--
-- laser.anonymizer (2. usage)
--

CREATE OR REPLACE FUNCTION pg_temp.anonymize(acceptBetaTester BOOL DEFAULT FALSE) RETURNS VOID AS $$
    DECLARE
        VERSION CONSTANT TEXT = '01/07/2025';
        count_tmp INT;
BEGIN

    RAISE NOTICE '--- ---------------------------------------- ---';
    RAISE NOTICE '--- laser.anonymizer';
    RAISE NOTICE '--- version %', version;
    RAISE NOTICE '--- now %', to_char(now(), 'YYYY-MM-DD HH24:MI');
    RAISE NOTICE '--- ---------------------------------------- ---';
    RAISE NOTICE '--- acceptBetaTester: %', acceptBetaTester;
    RAISE NOTICE '--- ignoreCustomerIdentifier: %', acceptBetaTester;

    IF acceptBetaTester IS FALSE THEN
        UPDATE org SET
            org_is_beta_tester = false
        WHERE org_guid NOT IN ('org:e6be24ff-98e4-474d-9ef8-f0eafd843d17', 'org:1d72afe7-67cb-4676-add0-51d3ae66b1b3');

        GET DIAGNOSTICS count_tmp = ROW_COUNT;
        RAISE NOTICE '%', pg_temp.anon_log_mask('org (org_is_beta_tester)', count_tmp);
    ELSE
        SELECT count(*) FROM org WHERE org_is_beta_tester = true INTO count_tmp;
        RAISE NOTICE '---            found: %', count_tmp;
    END IF;

    RAISE NOTICE '--- ---------------------------------------- ---';
    RAISE NOTICE '--- masking data';

    PERFORM pg_temp.anon_mask_users();              -- Users
    PERFORM pg_temp.anon_mask_orgs(acceptBetaTester);   -- Orgs (OrgSetting, CustomerIdentifier)

    PERFORM pg_temp.anon_mask_addressbook();         -- Person, Contact
    PERFORM pg_temp.anon_mask_finance();             -- Finance
    PERFORM pg_temp.anon_mask_docs();                -- Docs (notes)

    PERFORM pg_temp.anon_mask_properties1();         -- Properties (Org, Person, Platform, Provider, Vendor)
    PERFORM pg_temp.anon_mask_properties2();         -- Properties (License, Subscription)

    PERFORM pg_temp.anon_mask_tasks();               -- Tasks
    PERFORM pg_temp.anon_mask_workflows();           -- Workflows
    PERFORM pg_temp.anon_mask_surveys();             -- Surveys

    RAISE NOTICE '--- ---------------------------------------- ---';
    RAISE NOTICE '--- deleting data';

    PERFORM pg_temp.anon_delete_mails();             -- Mails (Grails Asynchronous Mail Plugin)

    RAISE NOTICE '--- ---------------------------------------- ---';
END;
$$ LANGUAGE PLPGSQL;

-- LOCAL  : acceptBetaTester -> ignoreCustomerIdentifier = FALSE
-- DEV/QA : acceptBetaTester -> ignoreCustomerIdentifier = TRUE

SELECT pg_temp.anonymize(TRUE);

