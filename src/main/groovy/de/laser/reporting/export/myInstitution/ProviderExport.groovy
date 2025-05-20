package de.laser.reporting.export.myInstitution

import de.laser.*
import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.wekb.Provider
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource

class ProviderExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_PROVIDER

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: Provider
                    ],
                    fields : [
                            default: [
                                    'globalUID'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '+sortname+name'    : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    'homepage'          : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'status'            : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-provider-contact': [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'x-property'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ],
                                    'x-identifier'      : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                            ]
                    ]
            ]
    ]

    ProviderExport(String token, Map<String, Object> fields) {
        init(token, fields)
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        GlobalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        ContextService contextService = BeanStore.getContextService()
        ProviderService providerService = BeanStore.getProviderService()
        MessageSource messageSource = BeanStore.getMessageSource()

        Provider pro = obj as Provider
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'provider', action: 'show', absolute: true ) + '/' + pro.getProperty(key) + '@' + pro.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(pro, key, Provider.getDeclaredField(key).getType()) )
                }
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(pro, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(pro, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.provider = :pro and i.ns.id in (:idnsList)",
                                [pro: pro, idnsList: f.value]
                        )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@-provider-contact') {
                    List coList = []

                    if (RDStore.REPORTING_CONTACT_TYPE_CONTACTS.id in f.value) {
                        List<RefdataValue> functionTypes = Person.executeQuery(
                                "select distinct pr.functionType from Person p inner join p.roleLinks pr where p.isPublic = true and pr.provider = :pro", [pro: pro]
                        )
                        List personList = []

                        functionTypes.each{ ft ->
                            List<Person> persons = providerService.getContactPersonsByFunctionType(pro, true, ft as RefdataValue)
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
                        String sql = "select distinct type from Address addr join addr.type type join addr.provider pro where pro = :pro and (addr.tenant is null or addr.tenant = :ctxOrg) order by type.value_" + LocaleUtils.getCurrentLang()
                        List<RefdataValue> addressTypes = Address.executeQuery( sql, [pro: pro, ctxOrg: contextService.getOrg()] )
                        List addressList = []

                        String pob = messageSource.getMessage('address.pob.label',null, LocaleUtils.getCurrentLocale())

                        addressTypes.each { at ->
                            String sql2 = "select distinct addr from Address addr join addr.provider pro join addr.type addrType where pro = :pro and addrType = :at and (addr.tenant is null or addr.tenant = :ctxOrg)"
                            List<Address> addresses = Address.executeQuery(sql2, [pro: pro, at: at, ctxOrg: contextService.getOrg()])
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
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(pro, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> combined properties : TODO
            else if (key in ['sortname', 'name']) {
                content.add( getPropertyContent(pro, key, Provider.getDeclaredField(key).getType()) )
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
