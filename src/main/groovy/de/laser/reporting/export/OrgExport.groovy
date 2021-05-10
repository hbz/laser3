package de.laser.reporting.export

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Org
import de.laser.OrgSetting
import de.laser.OrgSubjectGroup
import de.laser.OrganisationService
import de.laser.Person
import de.laser.ReaderNumber
import de.laser.RefdataValue
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

class OrgExport extends AbstractExport {

    static String KEY = 'organisation'

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: Org
                    ],
                    fields : [
                            default: [
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
                                    '@ae-org-contact'       : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-identifier'          : FIELD_TYPE_CUSTOM_IMPL,
                                    '@ae-org-readerNumber'  : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-property'            : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                                    'subjectGroup'          : FIELD_TYPE_CUSTOM_IMPL
                            ],
                            provider: [
                                    'globalUID'         : FIELD_TYPE_PROPERTY,
                                    'sortname'          : FIELD_TYPE_PROPERTY,
                                    'name'              : FIELD_TYPE_PROPERTY,
                                    'orgType'           : FIELD_TYPE_REFDATA_JOINTABLE,
                                    'country'           : FIELD_TYPE_REFDATA,
                                    'legalInfo'         : FIELD_TYPE_CUSTOM_IMPL,
                                    '@ae-org-contact'   : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-identifier'      : FIELD_TYPE_CUSTOM_IMPL,
                                    'x-property'        : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                            ],
                            agency: [
                                    'globalUID'         : FIELD_TYPE_PROPERTY,
                                    'sortname'          : FIELD_TYPE_PROPERTY,
                                    'name'              : FIELD_TYPE_PROPERTY,
                                    'orgType'           : FIELD_TYPE_REFDATA_JOINTABLE,
                                    'country'           : FIELD_TYPE_REFDATA,
                                    'legalInfo'         : FIELD_TYPE_CUSTOM_IMPL,
                                    '@ae-org-contact'   : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-identifier'      : FIELD_TYPE_CUSTOM_IMPL,
                                    'x-property'        : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                            ]
                    ]
            ]
    ]

    OrgExport (String token, Map<String, Object> fields) {
        this.token = token
        selectedExportFields = getAllFields().findAll{ it.key in fields.keySet() }
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( token, getCurrentConfig( KEY ).base as Map<String, Object>, fieldName )
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
                else if (key == 'x-identifier') {
                    List<Identifier> ids = Identifier.executeQuery(
                            "select i from Identifier i where i.value != null and i.value != '' and i.org = :org", [org: org]
                    )
                    content.add( ids.collect{ it.ns.ns + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@ae-org-contact') {

                    List personList = []
                    List<RefdataValue> funcTypes = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT]

                    funcTypes.each{ ft ->
                        List<Person> persons = org.getContactPersonsByFunctionType(true, ft)
                        persons.each {p ->
                            String p1 = [
                                    ft.getI10n('value') + ':',
                                    p.title,
                                    p.first_name,
                                    p.middle_name,
                                    p.last_name
                            ].findAll().join(' ')

                            String p2 = p.contacts.toSorted().collect{
                                it.contentType.getI10n('value')  + ': ' + it.content
                            }.join(', ')

                            personList.add( p1 + (p2 ? ', ' + p2 : ''))
                        }
                    }

                    content.add( personList.join( CSV_VALUE_SEPARATOR ) )
                }
                else if (key == '@ae-org-readerNumber') {
                    
                    OrganisationService organisationService = (OrganisationService) Holders.grailsApplication.mainContext.getBean('organisationService')
                    Map<String,Map<String, ReaderNumber>> semesterMap = organisationService.groupReaderNumbersByProperty(
                            ReaderNumber.findAllByOrgAndSemesterIsNotNull( org ), "semester"
                    )

                    String all = semesterMap.collect { sem ->
                        sem.key.getI10n('value') + ': ' + sem.value.collect { rn ->
                            rn.key + ' ' + rn.value.value
                        }.join(', ')
                    }.join( CSV_VALUE_SEPARATOR )

                    content.add( all )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = BaseDetails.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(org, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
