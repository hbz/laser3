
/* remove deprecated refdataValues and foreign keys */

UPDATE cost_item SET ci_cat_rv_fk = null WHERE ci_cat_rv_fk IN (
  SELECT rdv_id
  FROM refdata_value
  WHERE rdv_owner = (SELECT rdc_id
                     FROM refdata_category
                     WHERE rdc_description = 'CostItemCategory')
);

DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id
  FROM refdata_category
  WHERE rdc_description = 'CostItemCategory');
