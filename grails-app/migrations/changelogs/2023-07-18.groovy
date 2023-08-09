package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1689666718159-1") {
        grailsChange {
            change {
                sql.execute("update license set lic_ref = replace(lic_ref, '(Teilnehmervertrag)', '') where lic_ref like '%(Teilnehmervertrag)%'")
                int countUpdate = sql.updateCount
                confirm("update license set lic_ref = replace(lic_ref, 'Teilnehmervertrag', 'Einrichtungsvertrag') where lic_ref like '%(Teilnehmervertrag)%': ${countUpdate}")
                changeSet.setComments("update license set lic_ref = replace(lic_ref, 'Teilnehmervertrag', 'Einrichtungsvertrag') where lic_ref like '%(Teilnehmervertrag)%': ${countUpdate}")
            }
            rollback {}
        }
    }

}
