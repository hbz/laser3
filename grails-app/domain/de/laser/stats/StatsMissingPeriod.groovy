package de.laser.stats

import de.laser.exceptions.CreationException

class StatsMissingPeriod implements Comparable {

    Date from
    Date to

    static belongsTo = [cursor: LaserStatsCursor]

    static mapping = {
        id              column: 'smp_id'
        version         column: 'smp_version'
        cursor          column: 'smp_cursor_fk'
        from            column: 'smp_from_date', index: 'smp_from_idx'
        to              column: 'smp_to_date', index: 'smp_to_idx'
    }

    static constraints = {

    }

    static StatsMissingPeriod construct(Map<String, Object> configMap) throws CreationException {
        StatsMissingPeriod result = StatsMissingPeriod.findByFromAndToAndCursor(configMap.from, configMap.to, configMap.cursor)
        if(!result)
            result = new StatsMissingPeriod(configMap)
        if(!result.save())
            throw new CreationException(result.getErrors().getAllErrors().toListString())
        result
    }

    @Override
    int compareTo(Object o) {
        StatsMissingPeriod period2 = (StatsMissingPeriod) o
        int result
        result = from.compareTo(period2.from)
        if(result == 0)
            result = to.compareTo(period2.to)
        if(result == 0)
            result = cursor.reportID.compareTo(period2.cursor.reportID)
        result
    }
}
