-- Ausgabe aller Organisationen (hbz Consorten/Teilnehmer), die bei elektronischer Rechnung No gewählt haben.
-- ids sind von PROD!!!

SELECT * from org
                left join combo c on org.org_id = c.combo_from_org_fk
                left join org_private_property o2 on org.org_id = o2.opp_owner_fk
where combo_to_org_fk = 1   -- 1=hbz
  and combo_type_rv_fk = 83
  and o2.opp_type_fk = 156  -- elektronische Rechnung
  and o2.ref_value_id = 2   -- No (el Rg)