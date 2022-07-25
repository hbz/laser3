
-- partially overwrites critical data

UPDATE "user" SET
    usr_username = CONCAT('User ', usr_id),
    usr_display = CONCAT('User ', usr_id),
    usr_email = 'local@localhost.local',
    usr_password = 'you_shall_not_pass',
    usr_enabled = false,
    usr_account_locked = true
WHERE usr_id NOT IN (
    SELECT DISTINCT u.usr_id
    FROM "user" u
             JOIN user_org uo ON u.usr_id = uo.uo_user_fk
             JOIN org o ON o.org_id = uo.uo_org_fk
    WHERE o.org_name ILIKE 'hbz%' OR o.org_name ILIKE '%backoffice' OR u.usr_username = 'anonymous'
);

UPDATE user_setting SET
    us_string_value = 'cc@localhost.local'
WHERE us_string_value != ''
    AND us_key_enum IN ('REMIND_CC_EMAILADDRESS', 'NOTIFICATION_CC_EMAILADDRESS')
    AND us_user_fk NOT IN (
        SELECT DISTINCT u.usr_id
        FROM "user" u
            JOIN user_org uo ON u.usr_id = uo.uo_user_fk
            JOIN org o ON o.org_id = uo.uo_org_fk
        WHERE o.org_name ILIKE 'hbz%' OR o.org_name ILIKE '%backoffice' OR u.usr_username = 'anonymous'
    );

UPDATE cost_item SET
    ci_cost_in_billing_currency = round(cast(random() * 5 * ci_cost_in_billing_currency AS NUMERIC), 2)
WHERE
    ci_cost_in_billing_currency IS NOT NULL AND
    ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_name ILIKE 'hbz%' OR org_name ILIKE '%backoffice'
        );

UPDATE cost_item SET
    ci_cost_in_local_currency = round(cast(ci_cost_in_billing_currency * ci_currency_rate AS NUMERIC), 2)
WHERE
    ci_cost_in_billing_currency IS NOT NULL AND
    ci_currency_rate IS NOT NULL AND
        ci_owner NOT IN (
            SELECT org_id FROM org WHERE org_name ILIKE 'hbz%' OR org_name ILIKE '%backoffice'
        );