package de.laser.reporting.export.local

import de.laser.*
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.oap.*
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class OrgExport extends BaseDetailsExport {

    static String KEY = 'organisation'

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: Org
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : [ type: FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: FIELD_TYPE_COMBINATION ],
                                    'customerType'      : [ type: FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'orgType'           : [ type: FIELD_TYPE_REFDATA_JOINTABLE ],
                                    'libraryType'       : [ type: FIELD_TYPE_REFDATA ],
                                    'libraryNetwork'    : [ type: FIELD_TYPE_REFDATA ],
                                    'funderHskType'     : [ type: FIELD_TYPE_REFDATA ],
                                    'funderType'        : [ type: FIELD_TYPE_REFDATA ],
                                    'country'           : [ type: FIELD_TYPE_REFDATA ],
                                    'legalInfo'         : [ type: FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'eInvoice'          : [ type: FIELD_TYPE_PROPERTY ],
                                    '@-org-contact'     : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-identifier'      : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-org-accessPoint' : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-org-readerNumber': [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'subjectGroup'      : [ type: FIELD_TYPE_CUSTOM_IMPL ]   // TODO custom_impl
                            ],
                            provider: [
                                    'globalUID'         : [ type: FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: FIELD_TYPE_COMBINATION ],
                                    'orgType'           : [ type: FIELD_TYPE_REFDATA_JOINTABLE ],
                                    'country'           : [ type: FIELD_TYPE_REFDATA ],
                                    '@-org-contact'     : [ type: FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'x-identifier'      : [ type: FIELD_TYPE_CUSTOM_IMPL ]
                            ],
                            agency: [
                                    'globalUID'         : [ type: FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: FIELD_TYPE_COMBINATION ],
                                    'orgType'           : [ type: FIELD_TYPE_REFDATA_JOINTABLE ],
                                    'country'           : [ type: FIELD_TYPE_REFDATA ],
                                    '@-org-contact'     : [ type: FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'x-identifier'      : [ type: FIELD_TYPE_CUSTOM_IMPL ]
                            ]
                    ]
            ]
    ]

    OrgExport(String token, Map<String, Object> fields) {
        init(token, fields)
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')

        Org org = obj as Org
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'org', action: 'show', absolute: true ) + '/' + org.getProperty(key) + '@' + org.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(org, key, Org.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(org, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(org, key) )
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
                            ' / ' +
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
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@-org-contact') {

                    List coList = []

                    if (RDStore.REPORTING_CONTACT_TYPE_CONTACTS.id in f.value) {
                        List<RefdataValue> functionTypes = Person.executeQuery(
                                "select distinct pr.functionType from Person p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org", [org: org]
                        )
                        List personList = []
                        // List<RefdataValue> funcTypes = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, RDStore.PRS_FUNC_TECHNICAL_SUPPORT]

                        functionTypes.each{ ft ->
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
                        coList.addAll( personList )
                    }
                    if (RDStore.REPORTING_CONTACT_TYPE_ADDRESSES.id in f.value) {
                        String sql = "select distinct type from Address addr join addr.type type join addr.org org where org = :org order by type.value_" + I10nTranslation.decodeLocale( LocaleContextHolder.getLocale() )
                        List<RefdataValue> addressTypes = Address.executeQuery( sql, [org: org] )
                        List addressList = []

                        String pob = messageSource.getMessage('address.pob.label',null, LocaleContextHolder.getLocale())

                        addressTypes.each { at ->
                            List<Address> addresses = Address.executeQuery(
                                    "select distinct addr from Address addr join addr.org org join addr.type addrType where org = :org and addrType = :at", [org: org, at: at]
                            )
                            addresses.each{ addr ->
                                String a1 = [
                                        addr.name,
                                        [addr.street_1, addr.street_2].findAll().join(' '),
                                        [addr.zipcode, addr.city].findAll().join(' '),
                                        addr.country ? addr.country.getI10n('value') + (addr.region ? ' (' + addr.region.getI10n('value') + ')' : '') : addr.region?.getI10n('value'),
                                        [addr.pob ? (pob + ' ' + addr.pob + ' -') : null, addr.pobZipcode, addr.pobCity].findAll().join(' ')
                                ].findAll().join(', ')

                                String a2 = addr.type.collect{ it.getI10n('value') }.join(', ') + ': ' + a1
                                addressList.add( a2 )
                            }
                        }
                        coList.addAll( addressList )
                    }

                    content.add( coList.join( CSV_VALUE_SEPARATOR ) )
                }
                else if (key == '@-org-readerNumber') {

                    OrganisationService organisationService = (OrganisationService) Holders.grailsApplication.mainContext.getBean('organisationService')

                    List entries = []
                    List<Long> semIdList = f.value.findAll{ it.startsWith('sem-') }.collect{ Long.parseLong( it.replace('sem-', '') ) }
                    List<Integer> ddList = f.value.findAll{ it.startsWith('dd-') }.collect{ Integer.parseInt( it.replace('dd-', '') ) } // integer - hql

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
                else if (key == '@-org-accessPoint') {

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
            // --> combined properties : TODO
            else if (key in ['sortname', 'name']) {
                content.add( getPropertyContent(org, key, Org.getDeclaredField(key).getType()) )
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
