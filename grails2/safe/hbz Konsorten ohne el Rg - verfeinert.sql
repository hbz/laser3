-- Ausgabe aller Organisationen (hbz Consorten/Teilnehmer),
-- Emailadressen des Rechnungskontakt und Hauptkontakts, die bei elektronischer Rechnung No gewählt haben.
-- ids sind von PROD!!!

SELECT distinct org_id, org_name, prs_first_name, prs_last_name, ct_content, rdv_value  from org
                left join person_role r on org.org_id = r.pr_org_fk
                left join combo c on org.org_id = c.combo_from_org_fk
                left join org_private_property o2 on org.org_id = o2.opp_owner_fk
                left join person p on r.pr_prs_fk = p.prs_id
                left join contact c2 on p.prs_id = c2.ct_prs_fk
                left join refdata_value v on r.pr_function_type_rv_fk = v.rdv_id
where combo_to_org_fk = 1
  and combo_type_rv_fk = 83
  and o2.opp_type_fk = 156
  and o2.ref_value_id = 2
  and (r.pr_function_type_rv_fk = 816 or r.pr_function_type_rv_fk = 91) and ct_content_type_rv_fk = 16
order by org_name asc