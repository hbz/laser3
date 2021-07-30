package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class WfWorkflowBase {

    WfTaskBase child

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
