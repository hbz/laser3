/*
TODO
*/

/* convert existing title_instances to new type 'Journal' */

UPDATE title_instance SET class = 'com.k_int.kbplus.JournalInstance';
UPDATE title_instance SET ti_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Journal');