-- 1.
-- fix duplicate doc_docstore_uuids
--

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

-- function needed
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

SELECT funky_uuid();


-- 2.
-- exporting BLOBs as files
--

DROP PROCEDURE exportFiles;

DELIMITER //
CREATE PROCEDURE exportFiles()
BEGIN
  DECLARE ccId INTEGER;
  DECLARE ccPath TEXT;

  DECLARE cc CURSOR FOR (select
    doc_id as id,
    concat('/tmp/', doc_docstore_uuid) as path
  from doc
  where doc_content_type = 3);

  OPEN cc;
  snoopy: LOOP
    FETCH cc INTO ccId, ccPath;
    SET @sql = concat('SELECT doc_blob_content INTO DUMPFILE \'', ccPath, '\' FROM doc WHERE doc_id = ', ccId);

    PREPARE cmd from @sql;
    EXECUTE cmd;
    DEALLOCATE PREPARE cmd;
    SET @sql = NULL;

  END LOOP snoopy;

  CLOSE cc;
END //
DELIMITER ;

CALL exportFiles();