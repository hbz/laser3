package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

/**
 * This is the base class for a workflow task
 */
@DirtyCheck
abstract class WfTaskBase {

    @RefdataInfo(cat = RDConstants.WF_TASK_PRIORITY)
    RefdataValue priority

//    WfConditionBase condition

//    WfTaskBase child
//    WfTaskBase next

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
