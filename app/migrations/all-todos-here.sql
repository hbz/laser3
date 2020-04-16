-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>
-- changesets in changelog-yyyy-mm-dd.groovy

-- 2020-04-16
-- ERMS-2382
-- issue entitlement tipp_fk and subscription_fk set not null
-- changesets in changelog-2020-04-16.groovy
alter table issue_entitlement alter column ie_tipp_fk set not null;
alter table issue_entitlement alter column ie_subscription_fk set not null;