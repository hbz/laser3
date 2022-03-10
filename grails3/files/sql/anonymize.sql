
-- partially overwrites critical data

UPDATE "user" SET
    username = CONCAT('User ', id),
    display = CONCAT('User ', id),
    email = 'local@localhost.local',
    password = 'paradoxon'
    enabled = false,
    account_locked = true
WHERE id NOT IN (
    SELECT DISTINCT u.id
    FROM "user" u
             JOIN user_org uo ON u.id = uo.user_id
             JOIN org o ON o.org_id = uo.org_id
    WHERE o.org_name ILIKE 'hbz%' OR org_name ILIKE '%backoffice'
);

UPDATE user_setting SET
    us_string_value = 'cc@localhost.local'
WHERE
        us_string_value != ''
    AND us_key_enum IN ('REMIND_CC_EMAILADDRESS', 'NOTIFICATION_CC_EMAILADDRESS')
    AND us_user_fk NOT IN (
        SELECT DISTINCT u.id
        FROM "user" u
            JOIN user_org uo ON u.id = uo.user_id
            JOIN org o ON o.org_id = uo.org_id
        WHERE o.org_name ILIKE 'hbz%' OR org_name ILIKE '%backoffice'
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