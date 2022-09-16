package de.laser.workflow

import de.laser.DocContext
import de.laser.RefdataCategory
import de.laser.RefdataValue
import grails.gorm.dirty.checking.DirtyCheck

/**
 * This class represents the different condition types for a workflow task condition
 */
@DirtyCheck
abstract class WfConditionBase {

    public static final String[] TYPES = [
            '1_0_0',
            '2_0_0',
            '3_0_0',
            '4_0_0',
            '1_1_0',
            '2_2_0',
            '3_3_0',
            '4_4_0',
            '1_0_1',
            '2_0_2',
            '1_1_1', // Checkboxes_Dates_Files
            '2_2_2',
            '0_0_1',
            '0_0_2'
    ]


    public final static String FIELD_STRUCT_FORM    = 'FIELD_STRUCT_FORM'    // tmp layout workaround - will be removed
    public final static String FIELD_STRUCT_TAGLIB  = 'FIELD_STRUCT_TAGLIB'  // tmp layout workaround - will be removed

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
    DocContext  file2

    String      file1_title
    String      file2_title

    // --

    /**
     * Returns the list of fields depending on the condition type
     * @return a {@link List} of fields to display
     */
    List<String> getFields(String struct) {
        List<String> fields = []
        int[] types = type.split('_').collect{ Integer.parseInt(it)}

        if (struct == WfConditionBase.FIELD_STRUCT_FORM) {
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
        }
        else if (struct == WfConditionBase.FIELD_STRUCT_TAGLIB) {
            for (int i = 0; i < 4; i++) {
                if (types[0] > i) {
                    fields.add('checkbox' + (i + 1))
                }
                if (types[1] > i) {
                    fields.add('date' + (i + 1))
                }
                if (types[2] > i) {
                    fields.add('file' + (i + 1))
                }
            }
        }
        else {
            println 'WfConditionBase.getFields( ' + struct + ' ) failed' // - will be removed
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
