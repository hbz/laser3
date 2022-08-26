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

//    WfTaskBase task

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
