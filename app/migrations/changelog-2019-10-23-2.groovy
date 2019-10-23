databaseChangeLog = {

    changeSet(author: "jaegle (hand-coded)", id: "1571847838001-1") {
        grailsChange {
            change {

                def propDefId = sql.rows("select pd_id from property_definition where pd_name='NatStat Supplier ID'")

                def mapping = [:] // LAS:eR Platform -> NatStat Platform
                mapping['ACS Publications'] = 'American Chemical Society (ACS)'
                mapping['American Physical Society'] = 'American Physical Society (APS)'
                mapping['Annual Reviews'] = 'Annual Reviews'
                mapping['Brepolis'] = 'Brepols Online'
                mapping['de Gruyter'] = 'De Gruyter'
                mapping['EBSCOhost'] = 'EBSCOhost'
                mapping['Emerald Insight'] = 'Emerald Insight'
                mapping['Gale Databases'] = 'Gale'
                mapping['JAMA Network'] = 'JAMA Network'
                mapping['Nexis'] = 'LexisNexis'
                mapping['Mary Ann Liebert, Inc. Publishers'] = 'Mary Ann Liebert'
                mapping['Microbiology Society'] = 'Microbiology Society'
                mapping['MIT Press Journals'] = 'MIT Press'
                mapping['Ovid SP'] = 'OvidSP'
                mapping['ProQuest'] = 'ProQuest'
                mapping['ProQuest: Ebook Central'] = 'ProQuest Ebook Central'
                mapping['Scitation'] = 'Scitation'
                mapping['epubs SIAM'] = 'Society for Industrial and Applied Mathematics (SIAM)'
                mapping['Thieme Connect'] = 'Thieme Connect'
                mapping['Web of Science'] = 'Web of Science'
                mapping['Cochrane Library'] = 'Wiley Online Library'

                mapping.each() {
                    def owner = sql.rows("select plat_id from platform where plat_name='${it.key}'")
                    def existingProperty = sql.firstRow("select * from platform_custom_property where string_value='${it.value}'")
                    if (owner.plat_id[0] && !existingProperty) {
                        sql.execute("insert into platform_custom_property (version, owner_id, string_value, type_id) values (1,${owner.plat_id[0]},${it.value},${propDefId.pd_id[0]})")
                    }
                }

                // special treatment for cambridge core/university press
                // since there are 3 Cambridge platforms and we need to get the right one (id: 86 / GOKB: bbb92434-8e54-48f7-83ef-979e590b8565 on prod)
                def owner = sql.firstRow("select * from platform where plat_id=86 and plat_name='Cambridge University Press'")
                def existingProperty = sql.firstRow("select * from platform_custom_property where string_value='Cambridge University Press'")
                if (owner && !existingProperty){
                    sql.execute("insert into platform_custom_property (version, owner_id, string_value, type_id) values (1,86,'Cambridge Core',${propDefId.pd_id[0]})")
                }

            }
        }
    }

}