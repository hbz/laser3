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
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointEzproxy
import de.laser.oap.OrgAccessPointLink
import de.laser.oap.OrgAccessPointOA
import de.laser.oap.OrgAccessPointProxy
import de.laser.oap.OrgAccessPointShibboleth
import de.laser.oap.OrgAccessPointVpn
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
                                    '@ae-org-contact'   : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    'x-property'        : FIELD_TYPE_CUSTOM_IMPL_QDP,   // qdp
                                    'x-identifier'      : FIELD_TYPE_CUSTOM_IMPL,
                            ]
                    ]
            ]
    ]

    OrgExport (String token, Map<String, Object> fields) {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                selectedExportFields.put(k, fields.get(k))
            }
        }
        ExportHelper.normalizeSelectedMultipleFields( this )
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
                            ( org.createdBy != null ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value') ) +
                            CSV_VALUE_SEPARATOR +
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
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: it.ns.ns + ' *') + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
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

                    List entries = []
                    List<Long> semIdList = f.value.findAll{ it.startsWith('sem-') }.collect{ Long.parseLong( it.replace('sem-', '') ) }
                    List<Integer> ddList = f.value.findAll{ it.startsWith('dd-') }.collect{ Integer.parseInt( it.replace('dd-', '') ) }

                    if (semIdList) {

                        Map<String,Map<String, ReaderNumber>> semesterMap = organisationService.groupReaderNumbersByProperty(
                                ReaderNumber.executeQuery(
                                        'select rn from ReaderNumber rn where rn.org = :org and rn.semester.id in (:semIdList)',
                                        [org: org, semIdList: semIdList]
                                )
                                , "semester"
                        )
                        entries.addAll( semesterMap.collect { sem ->
                            sem.key.getI10n('value') + ': ' + sem.value.collect { rn ->
                                rn.value.value ? (rn.key + ' ' + rn.value.value) : null
                            }.findAll().join(', ')
                        } )
                    }
                    if (ddList) {

                        Map<String,Map<String, ReaderNumber>> dueDateMap = organisationService.groupReaderNumbersByProperty(
                                ReaderNumber.executeQuery(
                                        'select rn from ReaderNumber rn where rn.org = :org and YEAR(rn.dueDate) in (:ddList)',
                                        [org: org, ddList: ddList]
                                ), "dueDate"
                        )

                        entries.addAll( dueDateMap.collect { sem ->
                            DateUtils.getSDF_NoTime().format( sem.key ) + ': ' + sem.value.collect { rn ->
                                rn.value.value ? (rn.key + ' ' + rn.value.value) : null
                            }.findAll().join(', ')
                        } )
                    }

                    content.add( entries.join( CSV_VALUE_SEPARATOR ) )
                }
                else if (key == '@ae-org-accessPoint') {

                    List oapList = []

                    f.value.each { amId ->
                        RefdataValue am = RefdataValue.get( amId )
                        List entry = []

                        OrgAccessPoint.findAllByOrgAndAccessMethod(org, am, [sort: [name: 'asc']] ).each {oap ->
                            Map<String, Object> ipRanges = oap.getAccessPointIpRanges()

                            ipRanges['ipv4Ranges'].each { ipv4 ->
                                entry.add(ipv4['ipInput'])
                            }
                            ipRanges['ipv6Ranges'].each { ipv6 ->
                                entry.add(ipv6['ipInput'])
                            }

                            if (oap instanceof OrgAccessPointEzproxy || org instanceof OrgAccessPointProxy || org instanceof OrgAccessPointVpn) {
                                entry.add( oap.url )
                            }
                            else if (oap instanceof OrgAccessPointOA || oap instanceof OrgAccessPointShibboleth) {
                                entry.add( oap.entityId )
                            }
                            // ignored: OrgAccessPointLink
                        }
                        if (! entry.isEmpty()) {
                            oapList.add( am.getI10n('value') + ': ' + entry.join(', ') )
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
