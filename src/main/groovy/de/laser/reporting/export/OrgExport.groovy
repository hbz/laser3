package de.laser.reporting.export

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Org
import de.laser.OrgSetting
import de.laser.OrgSubjectGroup
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.properties.OrgProperty
import de.laser.reporting.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class OrgExport extends AbstractExport {

    static String KEY = 'organisation'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Org
                    ],
                    fields : [
                            'globalUID'         : FIELD_TYPE_PROPERTY,
                            'sortname'          : FIELD_TYPE_PROPERTY,
                            'name'              : FIELD_TYPE_PROPERTY,
                            'customerType'      : FIELD_TYPE_CUSTOM_IMPL,
                            'orgType'           : FIELD_TYPE_REFDATA_JOINTABLE,
                            'libraryType'       : FIELD_TYPE_REFDATA,
                            'libraryNetwork'    : FIELD_TYPE_REFDATA,
                            'funderHskType'     : FIELD_TYPE_REFDATA,
                            'funderType'        : FIELD_TYPE_REFDATA,
                            'country'           : FIELD_TYPE_REFDATA,
                            'legalInfo'         : FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : FIELD_TYPE_PROPERTY,
                            'identifier-assignment' : FIELD_TYPE_CUSTOM_IMPL,       // <- no BaseConfig.getCustomRefdata(fieldName)
                            'property-assignment'   : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                            'subjectGroup'      : FIELD_TYPE_CUSTOM_IMPL
                    ]
            ]
    ]

    OrgExport (String token, Map<String, Object> fields) {
        this.token = token
        selectedExportFields = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getAllFields() {
        String suffix = ExportHelper.getCachedQuerySuffix(token)

        CONFIG.base.fields.findAll {
            (it.value != FIELD_TYPE_CUSTOM_IMPL_QDP) || (it.key == suffix)
        }
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( token, CONFIG.base, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Org org = Org.get(id)
        List<String> content = []

        fields.each{ f ->
            String key = f.key
            String type = f.value

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'org', action: 'show', absolute: true ) + '/' + org.getProperty(key) as String )
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
                else if (key == 'identifier-assignment') {
                    List<Identifier> ids = Identifier.executeQuery(
                            "select i from Identifier i where i.value != null and i.value != '' and i.org = :org", [org: org]
                    )
                    content.add( ids.collect{ it.ns.ns + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'property-assignment') {
                    Long pdId = BaseDetails.getDetailsCache(token).id as Long

                    List<OrgProperty> properties = OrgProperty.executeQuery(
                            "select op from OrgProperty op join op.type pd where op.owner = :org and pd.id = :pdId " +
                                    "and (op.isPublic = true or op.tenant = :ctxOrg) and pd.descr like '%Property' ",
                            [org: org, pdId: pdId, ctxOrg: contextService.getOrg()]
                    )
                    content.add(
                            properties.collect { op ->
                                if (op.getType().isRefdataValueType()) {
                                    op.getRefValue()?.getI10n('value')
                                } else {
                                    op.getValue()
                                }
                            }.findAll().join( CSV_VALUE_SEPARATOR ) // removing empty and null values
                    )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
