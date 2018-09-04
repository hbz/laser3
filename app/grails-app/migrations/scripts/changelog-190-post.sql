
/* renaming child subscriptions */

DELIMITER //
CREATE PROCEDURE renameChildSubs ()
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

CALL renameChildSubs();
DROP PROCEDURE renameChildSubs;

/* convert org.addresses['functional contact billing adress'] ! TYPO */

DELIMITER //
CREATE PROCEDURE rebuildAddresses1 ()
  BEGIN

    DECLARE new_person_fk LONG;
    DECLARE given_adr_fk LONG;
    DECLARE given_org_fk LONG;

    DECLARE adr_cursor CURSOR FOR (
      SELECT adr.adr_id, org.org_id FROM address adr JOIN org org ON (adr_org_fk = org_id)
      WHERE
        adr.adr_type_rv_fk IN (
          SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) IN (
            'billing address', 'functional contact billing adress'
          )
        )
        AND
        org_sector_rv_fk IN (
          SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'higher education'
        )
    );

    OPEN adr_cursor;
    adr_loop: LOOP

      FETCH adr_cursor INTO given_adr_fk, given_org_fk;

      INSERT INTO person (
        prs_version,
        prs_last_name,
        prs_contact_type_rv_fk,
        prs_is_public_rv_fk,
        prs_tenant_fk
      ) VALUES (
        99999, /* undo value */
        "Rechnungsadresse",
        (SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'functional contact'),
        (SELECT rdv_id FROM refdata_value WHERE rdv_owner = 1 AND LOWER(rdv_value) = 'no'),
        (SELECT org_id FROM org WHERE LOWER(org_shortname) = 'hbz')
      );

      SET new_person_fk = LAST_INSERT_ID();

      INSERT INTO person_role (
        pr_version,
        pr_function_type_rv_fk,
        pr_org_fk,
        pr_prs_fk
      ) VALUES (
        99999, /* undo value */
        (SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'functional contact billing adress'),
        given_org_fk,
        new_person_fk
      );

      UPDATE address SET
        adr_org_fk = NULL, adr_prs_fk = new_person_fk
      WHERE adr_id = given_adr_fk;

    END LOOP adr_loop;
    CLOSE adr_cursor;

  END //
DELIMITER ;

CALL rebuildAddresses1();
DROP PROCEDURE rebuildAddresses1;

/* convert org.addresses['functional contact legal patron address'] */

DELIMITER //
CREATE PROCEDURE rebuildAddresses2 ()
  BEGIN

    DECLARE new_person_fk LONG;
    DECLARE given_adr_fk LONG;
    DECLARE given_org_fk LONG;

    DECLARE adr_cursor CURSOR FOR (
      SELECT adr.adr_id, org.org_id FROM address adr JOIN org org ON (adr_org_fk = org_id)
        WHERE
          adr.adr_type_rv_fk IN (
            SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) IN (
              'functional contact legal patron address', 'legal patron address'
            )
          )
        AND
          org_sector_rv_fk IN (
            SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'higher education'
          )
        );

    OPEN adr_cursor;
    adr_loop: LOOP

      FETCH adr_cursor INTO given_adr_fk, given_org_fk;

      INSERT INTO person (
        prs_version,
        prs_last_name,
        prs_contact_type_rv_fk,
        prs_is_public_rv_fk,
        prs_tenant_fk
      ) VALUES (
        99999, /* undo value */
        "Rechtlicher Träger",
        (SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'functional contact'),
        (SELECT rdv_id FROM refdata_value WHERE rdv_owner = 1 AND LOWER(rdv_value) = 'no'),
        (SELECT org_id FROM org WHERE LOWER(org_shortname) = 'hbz')
      );

      SET new_person_fk = LAST_INSERT_ID();

      INSERT INTO person_role (
        pr_version,
        pr_function_type_rv_fk,
        pr_org_fk,
        pr_prs_fk
      ) VALUES (
        99999, /* undo value */
        (SELECT rdv_id FROM refdata_value WHERE LOWER(rdv_value) = 'functional contact legal patron address'),
        given_org_fk,
        new_person_fk
      );

      UPDATE address SET
        adr_org_fk = NULL, adr_prs_fk = new_person_fk
      WHERE adr_id = given_adr_fk;

    END LOOP adr_loop;
    CLOSE adr_cursor;

  END //
DELIMITER ;

CALL rebuildAddresses2();
DROP PROCEDURE rebuildAddresses2;
