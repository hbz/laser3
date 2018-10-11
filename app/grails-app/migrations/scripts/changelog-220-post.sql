
/* copy settings from user into user_settings */

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_string_value)
    SELECT 'PAGE_SIZE', 0, id, default_page_size FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_org_fk)
  SELECT 'DASHBOARD', 0, id, default_dash_id FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_rv_fk)
  SELECT 'SHOW_SIMPLE_VIEWS', 0, id, show_simple_views_id FROM user;

INSERT INTO user_settings (us_key_enum, us_version, us_user_fk, us_rv_fk)
  SELECT 'SHOW_INFO_ICON', 0, id, show_info_icon_id FROM user;

  
/* TODO: alter user table -> removing old data */

