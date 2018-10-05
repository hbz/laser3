
/* remove deprecated refdataValues and foreign keys */


DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id
  FROM refdata_category
  WHERE rdc_description = 'FactType' and rdv_value != 'STATS:JR1' );
