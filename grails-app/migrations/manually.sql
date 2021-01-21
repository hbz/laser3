-- add relevant migrations here (for local and/or remote environments)

-- yyyy-mm-dd
-- <short description>


-- 2021-01-21
-- ONLY for current local dev dbs
update databasechangelog set filename = replace(filename, 'changelog', 'done/pre2.0/changelog') where filename like 'changelog-2020%';
update databasechangelog set filename = replace(filename, 'done/pre1.7/', 'done/pre2.0/') where filename like 'done/pre1.7%';


-- 2020-10-15
-- clean up files ( all changesets before 2021)

update databasechangelog set filename = replace(filename, 'done/changelog', 'done/pre2.0/changelog') where filename like 'done/changelog%';
update databasechangelog set filename = concat('done/pre2.0/', filename) where filename like 'changelog-2020%';

-- 2020-06-26
-- clean up files

--update databasechangelog set filename = concat('done/', filename) where filename like 'pre1.0/%';
--update databasechangelog set filename = concat('done/', filename) where filename like 'changelog-2019%';
--update databasechangelog set filename = concat('done/', filename) where filename like 'changelog-2020-01%' or filename like 'changelog-2020-02%' or filename like 'changelog-2020-03%';

