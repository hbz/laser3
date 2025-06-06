package de.laser.reporting.export.local

import de.laser.*
import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.utils.LocaleUtils
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.oap.*
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource

/**
 * Contains configurations for the local organisation report
 */
class OrgExport extends BaseDetailsExport {

    static String KEY = 'organisation'

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: Org
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'customerType'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'libraryType'       : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'libraryNetwork'    : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'funderHskType'     : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'funderType'        : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'country'           : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'legalInfo'         : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'eInvoice'          : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '@-org-contact'     : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-org-accessPoint' : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-org-readerNumber': [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'subjectGroup'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ]   // TODO custom_impl
                            ],
                            provider: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'country'           : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-org-contact'     : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ]
                            ],
                            agency: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'country'           : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-org-contact'     : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ]
                            ]
                    ]
            ]
    ]

    /**
     * Constructor call for a new organisation report
     * @param token the token under which the queried data is going to be stored
     * @param fields the {@link Map} with the fields selected for the export
     */
    OrgExport(String token, Map<String, Object> fields) {
        init(token, fields)
    }

    /**
     * Gets the fields selected for the current report export
     * @return the class field map containing the selected report fields
     */
    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    /**
     * Builds the label for the selected field key
     * @param fieldName the field key to which the export label should be built
     * @return the label which will appear in the report export
     */
    @Override
    String getFieldLabel(String fieldName) {
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    /**
     * Collects the details of the given organisation and outputs the field values human-readably
     * @param obj the organisation to export
     * @param fields the selected fields which should appear in the report
     * @return a {@link List} with the organisation's human-readable field values
     */
    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        ContextService contextService = BeanStore.getContextService()
        MessageSource messageSource = BeanStore.getMessageSource()

        Org org = obj as Org
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'org', action: 'show', absolute: true ) + '/' + org.getProperty(key) + '@' + org.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(org, key, Org.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(org, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(org, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

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
                        content.add( osg.collect{it.subjectGroup.getI10n('value')}.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
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
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@-org-contact') {

                    List coList = []

                    if (RDStore.REPORTING_CONTACT_TYPE_CONTACTS.id in f.value) {
                        List<RefdataValue> functionTypes = Person.executeQuery(
                                "select distinct pr.functionType from Person p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org", [org: org]
                        )
                        List personList = []
                        // List<RefdataValue> funcTypes = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_INVOICING_CONTACT, RDStore.PRS_FUNC_TECHNICAL_SUPPORT]

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
//                        String sql = "select distinct type from Address addr join addr.type type join addr.org org where org = :org order by type.value_" + LocaleUtils.getCurrentLang()
//                        List<RefdataValue> addressTypes = Address.executeQuery( sql, [org: org] )

                        String sql = "select distinct type from Address addr join addr.type type join addr.org org where org = :org and (addr.tenant is null or addr.tenant = :ctxOrg) order by type.value_" + LocaleUtils.getCurrentLang()
                        List<RefdataValue> addressTypes = Address.executeQuery( sql, [org: org, ctxOrg: contextService.getOrg()] )
                        List addressList = []

                        String pob = messageSource.getMessage('address.pob.label',null, LocaleUtils.getCurrentLocale())

                        addressTypes.each { at ->
//                            List<Address> addresses = Address.executeQuery(
//                                    "select distinct addr from Address addr join addr.org org join addr.type addrType where org = :org and addrType = :at", [org: org, at: at]
//                            )

                            String sql2 = "select distinct addr from Address addr join addr.org org join addr.type addrType where org = :org and addrType = :at and (addr.tenant is null or addr.tenant = :ctxOrg)"
                            List<Address> addresses = Address.executeQuery(sql2, [org: org, at: at, ctxOrg: contextService.getOrg()])
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

                    content.add( coList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) )
                }
                else if (key == '@-org-readerNumber') {

                    OrganisationService organisationService = BeanStore.getOrganisationService()

                    List entries = []
                    List<Long> semIdList = f.value.findAll{ it.startsWith('sem-') }.collect{ Long.parseLong( it.replace('sem-', '') ) }
                    List<Integer> yearList = f.value.findAll{ it.startsWith('yr-') }.collect{ Integer.parseInt( it.replace('yr-', '') ) } // integer - hql

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
                    if (yearList) {

                        Map<String,Map<String, ReaderNumber>> yearMap = organisationService.groupReaderNumbersByProperty(
                                ReaderNumber.executeQuery(
                                        'select rn from ReaderNumber rn where rn.org = :org and rn.year in (:yearList)',
                                        [org: org, yearList: yearList]
                                ), "year"
                        )

                        entries.addAll( yearMap.collect { sem ->
                            DateUtils.getLocalizedSDF_noTime().format( sem.key ) + ': ' + sem.value.collect { rn ->
                                rn.value.value ? (rn.key + ' ' + rn.value.value) : null
                            }.findAll().join(', ')
                        } )
                    }

                    content.add( entries.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) )
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

                            if (oap instanceof OrgAccessPointEzproxy || org instanceof OrgAccessPointProxy) {
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

                    content.add( oapList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) )
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
