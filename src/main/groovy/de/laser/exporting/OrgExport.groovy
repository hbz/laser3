package de.laser.exporting

import de.laser.Org
import de.laser.OrgSetting
import de.laser.OrgSubjectGroup
import de.laser.helper.DateUtils
import de.laser.helper.RDStore

import java.text.SimpleDateFormat

class OrgExport extends AbstractExport {

    static String KEY = 'organisation'

    static Map<String, Object> FIELDS = [

            'country'           : [type: FIELD_TYPE_REFDATA, text: 'Land' ],
            'customerType'      : [type: FIELD_TYPE_CUSTOM_IMPL, text: 'Kundentyp' ],
            'eInvoice'          : [type: FIELD_TYPE_PROPERTY, text: 'Elektronische Rechnungsstellung (XRechnung)' ],
            'funderHskType'     : [type: FIELD_TYPE_REFDATA, text: 'Trägerschaft' ],
            'funderType'        : [type: FIELD_TYPE_REFDATA, text: 'Unterhaltsträger' ],
            'legalInfo'         : [type: FIELD_TYPE_CUSTOM_IMPL, text: '?/?' ],
            'libraryNetwork'    : [type: FIELD_TYPE_REFDATA, text: 'Verbundzugehörigkeit' ],
            'libraryType'       : [type: FIELD_TYPE_REFDATA, text:  'Bibliothekstyp' ],
            'orgType'           : [type: FIELD_TYPE_REFDATA_JOINTABLE, text: 'Organisationstyp' ],
            'subjectGroup'      : [type: FIELD_TYPE_CUSTOM_IMPL, text: 'Fächergruppen' ]
    ]

    OrgExport (Map<String, Object> fields) {
        selectedExport = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getAllFields() {
        Map<String, Object> fields = [
                'sortname'     : [type: FIELD_TYPE_PROPERTY, text: 'Sortiername' ],
                'name'          : [type: FIELD_TYPE_PROPERTY, text: 'Name' ],
                'globalUID'     : [type: FIELD_TYPE_PROPERTY, text: 'globalUID' ]
        ]
        return fields + FIELDS
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExport
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        Org org = Org.get(id)
        List<String> content = []

        fields.each{ f ->
            String key = f.key
            String type = f.value.type

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( org.getProperty(key) as String )
                }
                else if (Org.getDeclaredField(key).getType() == Date) {
                    if (org.getProperty(key)) {
                        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                        content.add( sdf.format( org.getProperty(key) ) as String )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else if (Org.getDeclaredField(key).getType() in [boolean, Boolean]) {
                    if (org.getProperty(key) == true) {
                        content.add( RDStore.YN_YES.getI10n('value') )
                    }
                    else if (org.getProperty(key) == false) {
                        content.add( RDStore.YN_NO.getI10n('value') )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( org.getProperty(key) as String )
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                String value = org.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = org.getProperty(key) as Set
                content.add( refdata.collect{ it.getI10n('value') }.join( CSV_VALUE_SEPARATOR ))
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'customerType') {
                    def ct = OrgSetting.get(org, OrgSetting.KEYS.CUSTOMER_TYPE)
                    if (ct != OrgSetting.SETTING_NOT_FOUND) {
                        content.add( ct.getValue()?.getI10n('authority') )
                    }
                    else {
                        content.add( '' )
                    }
                }
                else if (key == 'legalInfo') {
                    content.add(
                            ( org.createdBy != null ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value') ) + '/' +
                            ( org.legallyObligedBy != null? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value') )
                    )
                }
                else if (key == 'subjectGroup') {
                    List osg = OrgSubjectGroup.findAllByOrg(org)
                    if (osg) {
                        content.add( osg.collect{it.subjectGroup.getI10n('value')}.join( CSV_VALUE_SEPARATOR ))
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( '* ' + FIELD_TYPE_CUSTOM_IMPL )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
