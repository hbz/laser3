
/* copy settings from user into user_settings */

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_string_value)
  SELECT 'PAGE_SIZE', 0, id, default_page_size FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_org_fk)
  SELECT 'DASHBOARD', 0, id, default_dash_id FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_rv_fk)
  SELECT 'SHOW_SIMPLE_VIEWS', 0, id, show_simple_views_id FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_rv_fk)
  SELECT 'SHOW_INFO_ICON', 0, id, show_info_icon_id FROM user;


/* Copy OrgTypes Into OrgRoleTypes */

INSERT into org_roletype (org_id, refdata_value_id)
SELECT org_id, (SELECT r.rdv_id from refdata_value as r left join refdata_category category on r.rdv_owner = category.rdc_id where rdc_description = "OrgRoleType" and r.rdv_value = v.rdv_value )
from org  left join refdata_value v on org.org_type_rv_fk = v.rdv_id
where rdv_id is not null;


/* remove deprecated refdataValues and foreign keys */

DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id
  FROM refdata_category
  WHERE rdc_description = 'FactType' and rdv_value like 'STATS%' );


/* TODO: alter user table -> removing old data */

