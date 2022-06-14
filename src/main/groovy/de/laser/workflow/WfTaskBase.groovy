package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

/**
 * This is the base class for a workflow task
 */
@DirtyCheck
class WfTaskBase {

    @RefdataAnnotation(cat = RDConstants.WF_TASK_PRIORITY)
    RefdataValue priority

    //@RefdataAnnotation(cat = RDConstants.WF_TASK_TYPE)
    //RefdataValue type

    WfConditionBase condition

    WfTaskBase child
    WfTaskBase next

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
