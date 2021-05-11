package de.laser.reporting.export

import de.laser.AccessPointService
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
                                    'x-property'            : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                                    'x-identifier'          : FIELD_TYPE_CUSTOM_IMPL,
                                    '@ae-org-accessPoint'   : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    '@ae-org-readerNumber'  : FIELD_TYPE_CUSTOM_IMPL,       // virtual
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
                                    'x-property'        : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp,
                                    'x-identifier'      : FIELD_TYPE_CUSTOM_IMPL,
                            ],
                            agency: [
                                    'globalUID'         : FIELD_TYPE_PROPERTY,
                                    'sortname'          : FIELD_TYPE_PROPERTY,
                                    'name'              : FIELD_TYPE_PROPERTY,
                                    'orgType'           : FIELD_TYPE_REFDATA_JOINTABLE,
                                    'country'           : FIELD_TYPE_REFDATA,
                                    'legalInfo'         : FIELD_TYPE_CUSTOM_IMPL,
                                    '@ae-org-contact'   : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-property'        : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                                    'x-identifier'      : FIELD_TYPE_CUSTOM_IMPL,
                            ]
                    ]
            ]
    ]

    OrgExport (String token, Map<String, Object> fields) {
        this.token = token
        selectedExportFields = fields.findAll { it.key in getAllFields().keySet() }

        selectedExportFields.each {it ->
            if ( it.key in ['x-identifier'] ) {
                it.value = it.value instanceof String ? [ Long.parseLong(it.value) ] : it.value.collect{ Long.parseLong(it) }
            }
        }
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        ExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<String> getObject(Long id, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Org org = Org.get(id)
        List<String> content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)

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
                String rdv = org.getProperty(key)?.getI10n('value')
                content.add( rdv ?: '')
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
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.org = :org and i.ns.id in (:idnsList)",
                                [org: org, idnsList: f.value] )
                    }
//                    else {
//                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.org = :org",
//                                [org: org] )
//                    }

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
                else if (key == '@ae-org-accessPoint') {

                    List oapList = []

                    AccessPointService accessPointService = (AccessPointService) Holders.grailsApplication.mainContext.getBean('accessPointService')
                    accessPointService.getOapListWithLinkCounts( org ).each {oa ->

                        List entry = []
                        Map<String, Object> ipRanges = oa['oap'].getAccessPointIpRanges()

                        ipRanges['ipv4Ranges'].each { ipv4 ->
                            entry.add( ipv4['ipInput'] )
//                            //entry.add( ipv4['name'] + ' - ' + ipv4['ipRange'] + ' - ' + ipv4['ipCidr'] + ' - ' + ipv4['ipInput'] )
//                            String t = ipv4['ipRange'].split('-')
//                            if ( t.size() == 2 && (t[0] != t[1]) ) {
//                                entry.add( ipv4['ipRange'] )
//                            }
//                            else {
//                                entry.add( ipv4['ipInput'] )
//                            }
                        }
                        ipRanges['ipv6Ranges'].each { ipv6 ->
                            entry.add( ipv6['ipInput'] )
//                            //entry.add( ipv6['name'] + ' - ' +  ipv6['ipRange'] + ' - ' + ipv6['ipCidr'] + ' - ' + ipv6['ipInput'] )
//                            String t = ipv6['ipRange'].split('-')
//                            if ( t.size() == 2 && (t[0] != t[1]) ) {
//                                entry.add( ipv6['ipRange'] )
//                            }
//                            else {
//                                entry.add( ipv6['ipInput'] )
//                            }
                        }
                        if (! entry.isEmpty()) {
                            oapList.add( oa['oap'].accessMethod.getI10n('value') + ': ' + entry.join(' / ') )
                        }
                    }

                    content.add( oapList.join( CSV_VALUE_SEPARATOR ) )
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
