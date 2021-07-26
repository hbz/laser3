package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class WfWorkflowBase {

    @RefdataAnnotation(cat = RDConstants.WF_WORKFLOW_STATE)
    RefdataValue state

    WfTaskBase child

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
