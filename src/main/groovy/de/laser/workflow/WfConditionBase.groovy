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

    public static final String[] TYPES = [
            '1_0_0',    // 1 Checkbox,1 Checkbox
            '2_0_0',    // 2 Checkboxen,2 Checkboxes
            '3_0_0',    // 3 Checkboxen,3 Checkboxes
            '4_0_0',    // 4 Checkboxen,4 Checkboxes
            '1_1_0',    // 1 Checkbox und 1 Datum,1 Checkbox and 1 Date
            '2_2_0',    // 2 Checkboxen und 2 Daten,2 Checkboxes and 2 Dates
            '3_3_0',    // 3 Checkboxen und 3 Daten,3 Checkboxes and 3 Dates
            '4_4_0',    // 4 Checkboxen und 4 Daten,4 Checkboxes and 4 Dates
            '1_0_1',    // 1 Checkbox und 1 Datei,1 Checkbox and 1 File
            '1_1_1',    // 1 Checkbox und 1 Datum und 1 Datei,1 Checkbox and 1 Date and 1 File
            '2_2_1',    // 2 Checkboxen und 2 Daten und 1 Datei,2 Checkboxes and 2 Dates and 1 File
            '0_0_1',    // 1 Datei,1 File
    ]

    String type

    String title
    String description

    Date dateCreated
    Date lastUpdated

    // -- type specific --

    Boolean checkbox1
    Boolean checkbox2
    Boolean checkbox3
    Boolean checkbox4

    String  checkbox1_title
    String  checkbox2_title
    String  checkbox3_title
    String  checkbox4_title

    Boolean checkbox1_isTrigger
    Boolean checkbox2_isTrigger
    Boolean checkbox3_isTrigger
    Boolean checkbox4_isTrigger

    Date    date1
    Date    date2
    Date    date3
    Date    date4

    String  date1_title
    String  date2_title
    String  date3_title
    String  date4_title

    DocContext  file1
//    DocContext  file2

    String      file1_title
//    String      file2_title

    // --

    /**
     * Returns the list of fields depending on the condition type
     * @return a {@link List} of fields to display
     */
    List<String> getFields() {
        List<String> fields = []
        int[] types = type.split('_').collect{ Integer.parseInt(it)}

        types.eachWithIndex{v, i ->
            for(int j=1; j<=v; j++) {
                if (i == 0) {
                    fields.add('checkbox' + j)
                }
                else if (i == 1) {
                    fields.add('date' + j)
                }
                else if (i == 2) {
                    fields.add('file' + j)
                }
            }
        }
        // println type + ' >> ' + fields
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
