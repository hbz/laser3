package de.laser.workflow

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class WfWorkflowBase {

    WfTaskBase task

    String title
    String description

    Date dateCreated
    Date lastUpdated
}
