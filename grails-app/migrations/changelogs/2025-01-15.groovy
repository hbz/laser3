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
                Calendar cal = GregorianCalendar.getInstance()
                int count = 0
                Set<ReaderNumber> numbersWithDueDate = ReaderNumber.findAllByDueDateIsNotNull()
                numbersWithDueDate.each { ReaderNumber rn ->
                    cal.setTime(rn.dueDate)
                    int year
                    if(cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
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
