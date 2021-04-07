package de.laser.exporting

import de.laser.Org
import de.laser.OrgSetting
import de.laser.OrgSubjectGroup
import de.laser.helper.DateUtils
import de.laser.helper.RDStore

class OrgExport extends GenericExportConfig {

    static String KEY = 'organisation'

    static Map<String, Object> FIELDS = [

            'country'           : GenericExportConfig.FIELD_TYPE_REFDATA,
            'customerType'      : GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL,
            'eInvoice'          : GenericExportConfig.FIELD_TYPE_PROPERTY,
            'funderHskType'     : GenericExportConfig.FIELD_TYPE_REFDATA,
            'funderType'        : GenericExportConfig.FIELD_TYPE_REFDATA,
            'legalInfo'         : GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL,
            'libraryNetwork'    : GenericExportConfig.FIELD_TYPE_REFDATA,
            'libraryType'       : GenericExportConfig.FIELD_TYPE_REFDATA,
            'orgType'           : GenericExportConfig.FIELD_TYPE_REFDATA_JOINTABLE,
            'subjectGroup'      : GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL
    ]

    Map<String, Object> getCurrentConfig() {

        Map<String, Object> fields = [
                'name' : GenericExportConfig.FIELD_TYPE_PROPERTY
        ]
        return fields + FIELDS
    }

    List<String> exportOrganisation(Long id, Map<String, Object> fields) {

        Org org = Org.get(id)
        List<String> content = [org.id as String]

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == GenericExportConfig.FIELD_TYPE_PROPERTY) {

                if (Org.getDeclaredField(key).getType() == Date) {
                    content.add( DateUtils.parseDateGeneric( org.getProperty(key) as String ) as String )
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
                    content.add( org.getProperty(key) )
                }
            }
            // --> generic refdata
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA) {
                String value = org.getProperty(key)?.getI10n('value')
                content.add( value ?: '')
            }
            // --> refdata join tables
            else if (type == GenericExportConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                Set refdata = org.getProperty(key)
                content.add( refdata.collect{ it.getI10n('value') }.join('; '))
            }
            // --> custom filter implementation
            else if (type == GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL) {

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
                            ( org.createdBy != null ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value') )
                            + '/' +
                            ( org.legallyObligedBy != null? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value') )
                    )
                }
                else if (key == 'subjectGroup') {
                    List osg = OrgSubjectGroup.findAllByOrg(org)
                    if (osg) {
                        content.add( osg.collect{it.subjectGroup.getI10n('value')}.join('; '))
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( '* ' + GenericExportConfig.FIELD_TYPE_CUSTOM_IMPL )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
