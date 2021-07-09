package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class WfTaskBase {

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_PRIORITY)
    RefdataValue priority

    @RefdataAnnotation(cat = RDConstants.WORKFLOW_TASK_TYPE)
    RefdataValue type

    WfTaskBase head
    WfTaskBase next

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
