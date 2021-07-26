package de.laser.workflow

import de.laser.RefdataCategory
import de.laser.RefdataValue
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class WfConditionBase {

    final static TYPES = [
            0, //: 'TEST',
            1, //: '1 Checkbox',
            2, //: '1 Checkbox + Date',
            3, //: '2 Checkboxes',
            4, //: '2 Checkboxes + 2 Dates',
    ]

    int type

    String title
    String description

    Date dateCreated
    Date lastUpdated

    // -- type specific --

    Boolean checkbox1
    String  checkbox1_title
    Boolean checkbox1_isTrigger

    Boolean checkbox2
    String  checkbox2_title
    Boolean checkbox2_isTrigger

    Date    date1
    String  date1_title

    Date    date2
    String  date2_title

    // --

    RefdataValue getTypeAsRefdataValue() {
        RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + type)
    }
}
