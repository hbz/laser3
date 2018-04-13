/*
TODO
*/

/* convert role Subscriber (fom subscriptions with instanceOf != null) to Subscriber_Consortial */

UPDATE org_role AS target SET target.or_roletype_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Subscriber_Consortial')
WHERE target.or_id IN ( SELECT * FROM (
  SELECT ogr.or_id
    FROM org_role as ogr
      join subscription as sub on ogr.or_sub_fk = sub.sub_id
    WHERE ogr.or_roletype_fk = (SELECT rdv_id
                                  FROM refdata_value
                                  WHERE rdv_value = 'Subscriber')
      AND sub.sub_parent_sub_fk IS NOT null
) AS source );

/* convert existing title_instances to new type 'Journal' */

UPDATE title_instance SET class = 'com.k_int.kbplus.JournalInstance';
UPDATE title_instance SET ti_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Journal');