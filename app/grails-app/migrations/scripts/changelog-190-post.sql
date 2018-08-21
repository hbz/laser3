
/* renaming child subscriptions */

DELIMITER //
CREATE PROCEDURE getStruct ()
BEGIN

  DECLARE child_id LONG;
  DECLARE child_name VARCHAR(1024);
  DECLARE parent_id LONG;
  DECLARE parent_name VARCHAR(1024);

  DECLARE sub_cursor CURSOR FOR
    SELECT
      child.sub_id    child_id,
      child.sub_name  child_name,
      parent.sub_id   parent_id,
      parent.sub_name parent_name
    FROM subscription child
      JOIN subscription parent ON (child.sub_parent_sub_fk = parent.sub_id)
  ;

  OPEN sub_cursor;
  sub_loop: LOOP
    FETCH sub_cursor INTO child_id, child_name, parent_id, parent_name;
    UPDATE subscription SET sub_name = parent_name WHERE sub_id = child_id;

  END LOOP sub_loop;
  CLOSE sub_cursor;

END //
DELIMITER ;

CALL getStruct();
DROP PROCEDURE getStruct;