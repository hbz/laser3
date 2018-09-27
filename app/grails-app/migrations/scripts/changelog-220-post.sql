
/* Copy OrgTypes Into OrgRoleTypes */


INSERT into org_roletype (org_id, refdata_value_id)
SELECT org_id, rdv_id from org
left join refdata_value v on org.org_type_rv_fk = v.rdv_id
where rdv_id is not null
