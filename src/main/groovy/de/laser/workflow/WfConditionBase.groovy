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

    List<String> getFields() {
        List<String> fields = []

        if (type == 1) {
            fields.add( 'checkbox1' )
        }
        else if (type == 2) {
            fields.addAll( 'checkbox1', 'date1' )
        }
        else if (type == 3) {
            fields.addAll( 'checkbox1', 'checkbox2' )
        }
        else if (type == 4 || type == 0) {
            fields.addAll( 'checkbox1', 'date1', 'checkbox2', 'date2' )
        }
        fields
    }

    String getFieldLabel(String key) {

        if (key.startsWith('checkbox')) {
            'Checkbox'
        }
        else if (key.startsWith('date')) {
            'Datum'
        }
        else {
            'Feld'
        }
    }

    RefdataValue getTypeAsRefdataValue() {
        RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + type)
    }
}
