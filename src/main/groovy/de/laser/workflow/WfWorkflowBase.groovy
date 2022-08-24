package de.laser.workflow

import grails.gorm.dirty.checking.DirtyCheck

/**
 * The base class for workflows and workflow prototypes
 */
@DirtyCheck
abstract class WfWorkflowBase {

//    WfTaskBase task

    String prototypeVersion

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
