package de.laser.workflow

import de.laser.DocContext
import de.laser.RefdataCategory
import de.laser.RefdataValue
import grails.gorm.dirty.checking.DirtyCheck

/**
 * This class represents the different condition types for a workflow task condition
 */
@DirtyCheck
class WfConditionBase {

    final static TYPES = [
            1, //: '1 Checkbox',
            2, //: '1 Checkbox + 1 Date',
            3, //: '2 Checkboxes',
            4, //: '2 Checkboxes + 2 Dates',
            5, //: '1 Checkbox + 1 File',
            6, //: '1 Checkbox + 1 File + 1 Date',
            7, //: '2 Checkboxes + 1 File + 1 Date',
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

    DocContext  file1
    String      file1_title

    // --

    /**
     * Returns the list of fields depending on the condition type
     * @return a {@link List} of fields to display
     */
    List<String> getFields() {
        List<String> fields = []

        if (type == 0) {
            fields.addAll( 'checkbox1', 'date1', 'checkbox2', 'date2', 'file1' )
        }
        else if (type == 1) {
            fields.add( 'checkbox1' )
        }
        else if (type == 2) {
            fields.addAll( 'checkbox1', 'date1' )
        }
        else if (type == 3) {
            fields.addAll( 'checkbox1', 'checkbox2' )
        }
        else if (type == 4) {
            fields.addAll( 'checkbox1', 'date1', 'checkbox2', 'date2' )
        }
        else if (type == 5) {
            fields.addAll( 'checkbox1', 'file1' )
        }
        else if (type == 6) {
            fields.addAll( 'checkbox1', 'file1', 'date1' )
        }
        else if (type == 7) {
            fields.addAll( 'checkbox1', 'file1', 'checkbox2', 'date1' )
        }
        fields
    }

    /**
     * Retrieves the label for the given field key
     * @param key the key to which the field label should be get
     * @return the (internationalised) field label
     */
    String getFieldLabel(String key) {

        if (key.startsWith('checkbox')) {
            'Checkbox'
        }
        else if (key.startsWith('date')) {
            'Datum'
        }
        else if (key.startsWith('file')) {
            'Datei'
        }
        else {
            'Feld'
        }
    }

    /**
     * Returns the condition type as a {@link RefdataValue}
     * @return the corresponding {@link RefdataValue}
     */
    RefdataValue getTypeAsRefdataValue() {
        RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + type)
    }
}
