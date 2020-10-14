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

-- 2020-04-20
-- ERMS-1914

insert into due_date_object (
    ddo_version,
    ddo_attribute_name,
    ddo_attribute_value_de,
    ddo_attribute_value_en,
    ddo_date,
    ddo_oid,
    ddo_date_created,
    ddo_last_updated,
    ddo_is_done)
select distinct
    das_version,
    das_attribute_name,
    das_attribute_value_de,
    das_attribute_value_en,
    das_date,
    das_oid,
    das_date_created,
    das_last_updated,
    das_is_done
from dashboard_due_date;

update dashboard_due_date ddd set das_ddobj_fk = ddo_id
from dashboard_due_date, due_date_object
where
    due_date_object.ddo_oid = ddd.das_oid and
    due_date_object.ddo_attribute_name = ddd.das_attribute_name;

-- Überprüfung der Daten:
-- select das_ddobj_fk, ddo_id, das_attribute_name, ddo_attribute_name, das_oid, ddo_oid
-- from dashboard_due_date, due_date_object
-- where ddo_oid = das_oid and
--       ddo_attribute_name = das_attribute_name;

ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_name;
ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_value_de;
ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_value_en;
ALTER TABLE dashboard_due_date DROP COLUMN das_date;
ALTER TABLE dashboard_due_date DROP COLUMN das_is_done;
ALTER TABLE dashboard_due_date DROP COLUMN das_oid;

-- 2020-05-08
-- ERMS-2407
-- in changelog-2020-05-07.groovy

update org set org_region_rv_fk = null where org_region_rv_fk in (
    select rdv_id
    from refdata_value
    where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
);

delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de');

update refdata_value
    set rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
    where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'federal.state');

delete from refdata_category where rdc_description = 'federal.state';

ALTER TABLE address RENAME COLUMN adr_state_rv_fk TO adr_region_rv_fk;

--ERMS-2407 Bugfix Verlorene Regionen
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- DARF NUR AUF PROD AUSGEFÜHRT WERDEN, da nur hier die ids stimmen!!
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 4 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 7 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 111 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 113 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 115 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 127 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 129 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 130 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 132 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 133 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 134 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 135 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 136 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 167 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 198 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 210 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 212 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 213 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 220 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 224 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 225 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 235 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 236 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 237 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 252 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 259 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 260 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 264 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 265 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 266 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 275 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 279 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 283 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 284 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 291 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 295 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 297 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 302 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 303 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 304 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 309 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 318 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 319 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 326 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 329 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 330 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 336 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 337 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 339 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 346 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 350 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 353 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 378 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 380 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 381 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 389 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 391 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 407 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 410 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 505 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 585 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 595 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 596 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 597 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 600 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 604 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 605 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 606 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 607 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 608 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 609 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 610 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 611 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 616 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 631 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 639 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 673 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 711 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 773 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 774 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 775 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 33, org_last_updated = now() WHERE org_id = 839 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 8 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 110 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 116 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 117 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 126 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 166 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 169 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 171 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 172 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 173 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 174 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 175 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 178 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 191 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 194 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 199 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 206 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 207 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 215 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 217 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 221 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 249 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 253 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 256 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 272 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 273 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 274 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 277 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 294 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 298 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 305 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 310 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 312 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 317 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 323 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 324 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 328 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 340 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 345 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 369 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 375 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 376 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 377 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 386 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 393 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 397 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 494 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 534 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 540 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 553 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 599 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 619 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 824 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 34, org_last_updated = now() WHERE org_id = 842 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 120 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 121 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 125 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 137 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 208 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 222 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 227 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 232 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 233 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 255 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 306 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 308 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 331 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 332 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 352 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 356 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 358 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 385 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 387 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 402 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 506 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 508 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 509 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 517 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 533 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 583 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 791 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 795 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 800 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 810 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 813 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 826 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 838 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 35, org_last_updated = now() WHERE org_id = 840 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 123 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 147 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 161 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 177 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 234 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 247 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 325 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 409 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 672 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 793 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 817 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 36, org_last_updated = now() WHERE org_id = 822 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 112 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 139 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 158 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 239 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 799 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 37, org_last_updated = now() WHERE org_id = 828 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 51 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 157 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 163 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 197 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 229 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 244 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 276 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 287 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 300 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 349 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 363 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 366 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 371 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 383 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 384 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 398 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 404 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 411 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 804 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 38, org_last_updated = now() WHERE org_id = 818 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 6 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 124 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 152 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 154 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 155 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 160 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 162 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 164 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 170 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 176 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 179 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 209 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 357 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 360 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 367 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 388 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 390 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 396 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 399 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 790 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 792 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 812 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 39, org_last_updated = now() WHERE org_id = 823 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 47 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 131 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 138 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 140 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 141 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 142 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 143 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 153 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 190 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 218 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 241 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 242 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 243 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 245 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 261 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 271 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 286 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 290 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 293 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 307 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 314 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 354 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 368 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 382 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 507 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 670 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 802 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 816 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 825 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 40, org_last_updated = now() WHERE org_id = 827 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 50 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 114 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 189 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 228 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 230 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 246 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 248 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 285 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 41, org_last_updated = now() WHERE org_id = 837 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 1 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 73 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 74 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 75 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 76 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 77 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 78 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 79 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 80 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 81 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 82 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 83 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 84 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 85 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 86 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 87 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 88 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 89 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 90 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 91 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 92 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 93 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 94 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 95 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 96 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 97 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 98 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 99 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 100 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 106 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 108 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 118 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 119 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 122 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 148 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 149 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 150 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 151 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 192 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 193 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 200 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 201 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 202 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 203 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 204 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 205 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 223 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 226 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 250 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 257 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 258 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 262 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 267 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 281 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 289 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 296 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 313 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 316 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 320 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 321 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 322 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 333 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 334 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 335 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 341 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 344 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 362 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 370 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 374 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 379 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 392 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 403 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 423 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 448 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 539 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 548 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 614 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 625 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 642 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 664 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 667 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 786 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 794 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 803 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 806 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 811 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 815 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 819 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 821 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 829 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 925 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 42, org_last_updated = now() WHERE org_id = 947 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 101 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 102 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 103 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 104 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 105 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 107 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 238 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 278 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 280 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 292 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 299 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 315 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 327 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 347 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 361 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 394 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 43, org_last_updated = now() WHERE org_id = 401 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 44, org_last_updated = now() WHERE org_id = 109 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 44, org_last_updated = now() WHERE org_id = 180 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 44, org_last_updated = now() WHERE org_id = 406 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 44, org_last_updated = now() WHERE org_id = 675 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 156 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 181 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 182 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 183 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 184 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 185 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 186 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 187 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 188 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 196 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 211 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 219 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 251 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 282 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 288 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 338 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 364 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 365 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 414 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 594 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 45, org_last_updated = now() WHERE org_id = 820 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 128 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 146 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 342 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 348 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 351 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 355 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 413 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 535 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 805 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 46, org_last_updated = now() WHERE org_id = 807 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 159 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 165 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 168 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 216 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 254 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 263 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 270 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 343 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 359 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 373 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 400 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 504 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 674 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 47, org_last_updated = now() WHERE org_id = 841 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 144 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 145 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 195 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 214 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 231 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 301 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 311 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 372 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 395 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 405 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 412 AND org_region_rv_fk ISNULL;
UPDATE org set org_region_rv_fk = 48, org_last_updated = now() WHERE org_id = 801 AND org_region_rv_fk ISNULL;


