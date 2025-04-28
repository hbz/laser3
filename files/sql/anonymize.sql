
-- partially overwrites critical data

UPDATE "user" SET
    usr_username = CONCAT('User ', usr_id),
    usr_display = CONCAT('User ', usr_id),
    usr_email = 'local@localhost.local',
    usr_password = 'you_shall_not_pass',
    usr_enabled = false
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
WHERE us_string_value != ''
    AND us_key_enum IN ('REMIND_CC_EMAILADDRESS', 'NOTIFICATION_CC_EMAILADDRESS')
    AND us_user_fk NOT IN (
        SELECT u.usr_id
        FROM "user" u
             JOIN org o ON o.org_id = u.usr_formal_org_fk
        WHERE o.org_is_beta_tester = true
        UNION
        SELECT u.usr_id from "user" u where u.usr_username = 'anonymous'
    );

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

UPDATE doc SET
    doc_content = (SELECT (ARRAY [
            'Aller guten Dinge sind drei',
            'Besser spät als nie',
            'Der Ton macht die Musik',
            'Es ist nicht alles Gold, was glänzt',
            'Gut Ding will Weile haben',
            'In der Ruhe liegt die Kraft',
            'Lange Rede, kurzer Sinn',
            'Not macht erfinderisch',
            'Ordnung ist das halbe Leben'
        ])[FLOOR(RANDOM() * 9 + 1)] || ' (' || to_char(doc_date_created, 'DD.MM.YYYY') || ')'
    )
    WHERE doc_content_type = 0 AND
        doc_owner_fk NOT IN (
            SELECT org_id FROM org WHERE org_is_beta_tester = true
        );