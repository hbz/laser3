
-- fixing problems
-- fixing problems

update "public".user_folder set uf_dateCreated = MAKEDATE(1970, 01) where uf_dateCreated is null;
update "public".user_folder set uf_lastUpdated = MAKEDATE(1970, 01) where uf_lastUpdated is null;

update "public".folder_item set fi_dateCreated = MAKEDATE(1970, 01) where fi_dateCreated is null;
update "public".folder_item set fi_lastUpdated = MAKEDATE(1970, 01) where fi_lastUpdated is null;


-- fixing duplicate doc_docstore_uuids
-- fixing duplicate doc_docstore_uuids

DROP FUNCTION funky_uuid;

DELIMITER //
CREATE FUNCTION funky_uuid()
  RETURNS CHAR(36)
  BEGIN
    SET @NOW = NOW();
    SET @h1 = LEFT( SHA1(@NOW + RAND()), 8 );
    SET @h2 = RIGHT( SHA1(@NOW + RAND()), 4 );
    SET @h3 = LEFT( SHA1(@NOW + RAND()), 4 );
    SET @h4 = RIGHT( SHA1(@NOW + RAND()), 4 );
    SET @h5 = LEFT( SHA1(@NOW + RAND()), 12 );

    RETURN CONCAT(@h1, '-', @h2, '-', @h3, '-', @h4, '-', @h5);
  END //
DELIMITER ;

-- finding duplicates
select * from (
  select
    doc_docstore_uuid,
    count(*) as count
  from doc
  where doc_content_type = 3
  group by doc_docstore_uuid
) x where x.count > 1;

-- setting unique doc_docstore_uuids
UPDATE doc set doc_docstore_uuid = funky_uuid() WHERE doc_docstore_uuid in (
  SELECT doc_docstore_uuid
  FROM (
         select
           doc_docstore_uuid,
           count(*) as count
         from doc
         where doc_content_type = 3
         group by doc_docstore_uuid
       ) x
  where x.count > 1
);

-- migrate string to bigint
ALTER TABLE "public".fact ALTER COLUMN fact_value TYPE bigint USING fact_value::bigint;