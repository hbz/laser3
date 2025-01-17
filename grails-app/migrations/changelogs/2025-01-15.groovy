package changelogs

import de.laser.ReaderNumber

import java.time.Year

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1736932770420-1") {
        addColumn(tableName: "reader_number") {
            column(name: "num_year", type: "bytea")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-2") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_due_date is not null and num_reference_group_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'number.type' and rdv_value = 'Students')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-3") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_semester_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'semester' and rdv_value = 'semester.not.applicable')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-4") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'semester') and rdv_value = 'semester.not.applicable'"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-5") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_semester_rv_fk is not null" +
                        " and num_org_fk in (select org_id from org where org_library_type_rv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value in ('Forschungseinrichtung', 'Wissenschaftliche Spezialbibliothek') and rdc_description = 'library.type'))" +
                        " and num_reference_group_rv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'number.type' and rdv_value in ('FTE', 'Scientific staff'))"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-6") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_due_date is not null" +
                        " and num_org_fk in (select org_id from org where org_library_type_rv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value in ('Fachhochschule', 'Kunst- und Musikhochschule', 'Universität') and rdc_description = 'library.type'))"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-7") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_semester_rv_fk is not null" +
                        " and num_reference_group_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'number.type' and rdv_value = 'User')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1736932770420-8") {
        grailsChange {
            change {
                Calendar cal = GregorianCalendar.getInstance()
                int count = 0
                Set<ReaderNumber> numbersWithDueDate = ReaderNumber.findAllByDueDateIsNotNull()
                numbersWithDueDate.each { ReaderNumber rn ->
                    cal.setTime(rn.dueDate)
                    int year
                    if(cal.get(Calendar.MONTH) > Calendar.JUNE) {
                        year = cal.get(Calendar.YEAR)
                    }
                    else year = cal.get(Calendar.YEAR) - 1
                    rn.year = Year.of(year)
                    if(rn.dateGroupNote?.isBlank())
                        rn.dateGroupNote = null
                    if(!rn.save())
                        println rn.errors.getAllErrors().toListString()
                    else count++
                }
                String info = "update due date to year: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
}
