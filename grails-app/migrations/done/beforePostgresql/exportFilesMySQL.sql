
-- exporting BLOBs as files
-- exporting BLOBs as files

DROP PROCEDURE exportFiles;

DELIMITER //
CREATE PROCEDURE exportFiles()
BEGIN
  DECLARE ccId INTEGER;
  DECLARE ccPath TEXT;

  DECLARE cc CURSOR FOR (select
    doc_id as id,
    concat('/tmp/migrate2postgresql/', doc_docstore_uuid) as path
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

-- CAUTION: deleting BLOBs after export
-- CAUTION: deleting BLOBs after export

UPDATE doc set doc.doc_blob_content = null;