
-- checking identifier namespace usages

SELECT * FROM (
      SELECT idns.idns_ns,
             idns.idns_id,
             sum(CASE WHEN i.id_lic_fk is null THEN 0 ELSE 1 END)  lic,
             sum(CASE WHEN i.id_org_fk is null THEN 0 ELSE 1 END)  org,
             sum(CASE WHEN i.id_pkg_fk is null THEN 0 ELSE 1 END)  pkg,
             sum(CASE WHEN i.id_sub_fk is null THEN 0 ELSE 1 END)  sub,
             sum(CASE WHEN i.id_tipp_fk is null THEN 0 ELSE 1 END) tipp
      FROM identifier i
               JOIN identifier_namespace idns ON idns.idns_id = i.id_ns_fk
      GROUP BY idns.idns_ns, idns.idns_id
      order by idns.idns_ns
) sq WHERE (
    CASE WHEN sq.lic > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.org > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.pkg > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.sub > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.tipp > 0 THEN 1 ELSE 0 END
    ) > 1;