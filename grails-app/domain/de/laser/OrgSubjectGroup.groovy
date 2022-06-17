package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

/**
 * This is a linking class between a subject group and an organisation
 */
class OrgSubjectGroup implements Comparable {

    @RefdataInfo(cat = RDConstants.SUBJECT_GROUP)
    RefdataValue subjectGroup

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            org:            Org,
            subjectGroup:   RefdataValue
    ]

    static mapping = {
        id           column: 'osg_id'
        version      column: 'osg_version'
        org          column: 'osg_org'
        subjectGroup column: 'osg_subject_group'
        dateCreated  column: 'osg_date_created'
        lastUpdated  column: 'osg_last_updated'
    }
    static constraints = {
        lastUpdated  (nullable: true)
        dateCreated  (nullable: true)
    }

    /**
     * Compares two subject group assignals on the base of their internationalised value
     * @param o the other assignal to compare with
     * @return the comparison result (-1, 0, 1)
     * @see I10nTranslation
     */
    @Override
    int compareTo(Object o) {
        OrgSubjectGroup b = (OrgSubjectGroup) o
        return subjectGroup.getI10n('value') <=> b.subjectGroup.getI10n('value')
    }
}
