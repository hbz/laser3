package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1671015645548-1") {
        grailsChange {
            change {
                sql.execute("update doc set doc_content = regexp_replace( doc_content, '(\r)', '', 'g' ) where doc_content_type = 0 and doc_content is not null")
                sql.execute("update doc set doc_content = regexp_replace( doc_content, '(\n){2,}', '</p><p>', 'g' ) where doc_content_type = 0 and doc_content is not null")
                sql.execute("update doc set doc_content = concat( '<p>', regexp_replace( doc_content, '(\n)', ' ', 'g' ), '</p>' ) where doc_content_type = 0 and doc_content is not null")
            }
            rollback {}
        }
    }
}
