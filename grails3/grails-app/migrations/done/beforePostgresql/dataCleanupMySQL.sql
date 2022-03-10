
-- fix invalid/missing data
-- fix invalid/missing data

update user_folder set uf_dateCreated = MAKEDATE(1970, 01) where uf_dateCreated is null;
update user_folder set uf_lastUpdated = MAKEDATE(1970, 01) where uf_lastUpdated is null;

update folder_item set fi_dateCreated = MAKEDATE(1970, 01) where fi_dateCreated is null;
update folder_item set fi_lastUpdated = MAKEDATE(1970, 01) where fi_lastUpdated is null;

ALTER TABLE fact MODIFY fact_value INTEGER;

-- rename tables
-- rename tables

RENAME TABLE kb_comment to `comment`;
RENAME TABLE kbplus_ord to ordering;

-- fix duplicate doc_docstore_uuids
-- fix duplicate doc_docstore_uuids

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
