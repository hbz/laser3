
/* Copy OrgTypes Into OrgRoleTypes */


INSERT into org_roletype (org_id, refdata_value_id)
SELECT org_id, (SELECT r.rdv_id from refdata_value as r left join refdata_category category on r.rdv_owner = category.rdc_id where rdc_description = "OrgRoleType" and r.rdv_value = v.rdv_value )
from org  left join refdata_value v on org.org_type_rv_fk = v.rdv_id
where rdv_id is not null;

