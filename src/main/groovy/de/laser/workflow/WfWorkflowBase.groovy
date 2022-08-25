package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

/**
 * The base class for workflows and workflow prototypes
 */
@DirtyCheck
abstract class WfWorkflowBase {

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_TARGET_TYPE)
    RefdataValue targetType

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_TARGET_ROLE)
    RefdataValue targetRole

//    WfTaskBase task

    String prototypeVersion

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
