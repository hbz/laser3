
-- Pseudonymization

-- for LOCAL only:

UPDATE org SET
    org_is_beta_tester = false
WHERE org_guid NOT IN ('org:e6be24ff-98e4-474d-9ef8-f0eafd843d17', 'org:1d72afe7-67cb-4676-add0-51d3ae66b1b3');

-- for ALL:

-- TODO: add NULL values
CREATE FUNCTION pg_temp.laser_rnd_text() RETURNS TEXT AS $$
    (SELECT (ARRAY [
        'Aller guten Dinge sind drei',
        'Besser spät als nie',
        'Der Ton macht die Musik',
        'Es ist nicht alles Gold, was glänzt',
        'Gut Ding will Weile haben',
        'In der Ruhe liegt die Kraft',
        'Kommt Zeit, kommt Rat',
        'Lange Rede, kurzer Sinn',
        'Not macht erfinderisch',
        'Ordnung ist das halbe Leben',
        'Von nichts kommt nichts',
        'Wer die Wahl hat, hat die Qual',
        'Zahlen lügen nicht'
        ])[floor(random() * 13 + 1)])
$$ LANGUAGE SQL;

-- TODO: add NULL values
CREATE FUNCTION pg_temp.laser_rnd_xval() RETURNS TEXT AS $$
    (SELECT concat('X', substr(md5(random()::text), 1, 8)))
$$ LANGUAGE SQL;

-- User

UPDATE "user" SET
    usr_username    = concat('User ', usr_id),
    usr_display     = concat('User ', usr_id),
    usr_email       = 'xy@localhost.local',
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
    us_string_value = 'cc@localhost.local'
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
    os_string_value     = pg_temp.laser_rnd_xval()
WHERE os_key_enum IN ('API_PASSWORD', 'NATSTAT_SERVER_API_KEY', 'NATSTAT_SERVER_REQUESTOR_ID', 'LASERSTAT_SERVER_KEY') AND
    os_org_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

UPDATE customer_identifier SET
    cid_value           = pg_temp.laser_rnd_xval(),
    cid_requestor_key   = pg_temp.laser_rnd_xval(),
    cid_note            = concat(pg_temp.laser_rnd_text(), ' (', cid_customer_fk, ', ', cid_owner_fk, ', ', cid_is_public, ')')
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

-- Various

UPDATE doc SET
    doc_content     = concat(pg_temp.laser_rnd_text(), ' (', to_char(doc_date_created, 'DD.MM.YYYY'), ')')
WHERE doc_content_type = 0 AND
    doc_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    );

UPDATE task SET
    tsk_title       = concat(pg_temp.laser_rnd_text(), ' (', to_char(tsk_date_created, 'DD.MM.YYYY'), ')'),
    tsk_description = ''
WHERE tsk_creator_fk NOT IN (
    SELECT usr_id FROM "user" WHERE usr_formal_org_fk IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);

UPDATE wf_checklist SET
    wfcl_title          = concat(pg_temp.laser_rnd_text(), ' (', to_char(wfcl_date_created, 'DD.MM.YYYY'), ')'),
    wfcl_description    = '',
    wfcl_comment        = ''
WHERE wfcl_owner_fk NOT IN (
    SELECT org_id FROM org WHERE org_is_beta_tester = true
);

UPDATE wf_checkpoint SET
    wfcp_title          = concat('Aufgabe ', wfcp_position, ' (', wfcp_checklist_fk , ')'),
    wfcp_description    = '',
    wfcp_comment        = ''
WHERE wfcp_checklist_fk IN (
    SELECT wfcl_id FROM wf_checklist WHERE wfcl_owner_fk NOT IN (
        SELECT org_id FROM org WHERE org_is_beta_tester = true
    )
);