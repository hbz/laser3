package changelogs

import de.laser.GlobalService
import de.laser.system.SystemEvent
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1671434535123-1") {
        addColumn(tableName: "doc") {
            column(name: "doc_server", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (modified)", id: "1671434535123-2") {
        grailsChange {
            change {
                Sql sql = GlobalService.obtainSqlConnection()

                String query = """
select si.surin_id as survey_id, si.surin_name as survey_name,
       siOrg.org_id as survey_owner_id, siOrg.org_name as survey_owner_name,
       dOrg.org_id as doc_owner_id, dOrg.org_name as doc_owner_name,
       d.doc_id as doc_id, d.doc_filename as doc_filename, d.doc_date_created as doc_created
from survey_info si
         join survey_config sc on si.surin_id = sc.surconf_surinfo_fk
         join doc_context dc on sc.surconf_id = dc.dc_survey_config_fk
         join doc d on d.doc_id = dc.dc_doc_fk
         join org siOrg on siOrg.org_id = si.surin_owner_org_fk
         join org dOrg on dOrg.org_id = d.doc_owner_fk
where
        si.surin_owner_org_fk != d.doc_owner_fk
order by
    d.doc_date_created;
"""
                List<String> changesList = []
                sql.eachRow( query, { grs ->
                    String change = "DOC:${grs[6]} (${grs[7]}) => [ OWNER from ORG:${grs[4]} (${grs[5]}) to ORG:${grs[2]} (${grs[3]}) ] @ SURVEY:${grs[0]} (${grs[1]})"

                    if (grs[2] && grs[4] && grs[6]) {
                        changesList.add( change )
                        sql.executeUpdate('update doc set doc_owner_fk = ' + grs[2] + ' where doc_id = ' + grs[6] + ' and doc_owner_fk = ' + grs[4])
                    }
                } )

                SystemEvent.createEvent( 'DBM_SCRIPT_INFO', [
                        msg: 'Data migration - Survey doc owners',
                        changes: changesList,
                        changes_count: changesList.size()
                ])
            }
            rollback {}
        }
    }
}
