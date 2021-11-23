package de.laser.reporting.export.myInstitution

import de.laser.ContextService
import de.laser.Identifier
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.helper.RDConstants
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import de.laser.reporting.report.myInstitution.config.PlatformXCfg
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class PlatformExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_PLATFORM

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: Platform
                    ],
                    fields : [
                            default: [
                                    'globalUID'             : FIELD_TYPE_PROPERTY,
                                    'name'                  : FIELD_TYPE_PROPERTY,
                                    'org'                   : FIELD_TYPE_CUSTOM_IMPL,
                                    'serviceProvider'       : FIELD_TYPE_CUSTOM_IMPL,
                                    'softwareProvider'      : FIELD_TYPE_CUSTOM_IMPL,
                                    'status'                : FIELD_TYPE_REFDATA,
                                    'ipAuthentication'             : FIELD_TYPE_ELASTICSEARCH,
                                    'shibbolethAuthentication'     : FIELD_TYPE_ELASTICSEARCH,
                                    'passwordAuthentication'       : FIELD_TYPE_ELASTICSEARCH,
                                    'proxySupported'               : FIELD_TYPE_ELASTICSEARCH,
                                    'statisticsFormat'             : FIELD_TYPE_ELASTICSEARCH,
                                    'statisticsUpdate'             : FIELD_TYPE_ELASTICSEARCH,
                                    'counterCertified'             : FIELD_TYPE_ELASTICSEARCH,
                                    'counterR3Supported'           : FIELD_TYPE_ELASTICSEARCH,
                                    'counterR4Supported'           : FIELD_TYPE_ELASTICSEARCH,
                                    'counterR4SushiApiSupported'   : FIELD_TYPE_ELASTICSEARCH,
                                    'counterR5Supported'           : FIELD_TYPE_ELASTICSEARCH,
                                    'counterR5SushiApiSupported'   : FIELD_TYPE_ELASTICSEARCH,
                            ]
                    ]
            ]
    ]

    PlatformExport(String token, Map<String, Object> fields) {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                selectedExportFields.put(k, fields.get(k))
            }
        }
        normalizeSelectedMultipleFields( this )
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

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Platform plt = obj as Platform
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'platform', action: 'show', absolute: true ) + '/' + plt.getProperty(key) as String )
                }
                else {
                    content.add( getPropertyContent(plt, key, Platform.getDeclaredField(key).getType()))
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(plt, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(plt, key) )
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == 'x-identifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.plt = :plt and i.ns.id in (:idnsList)",
                                [plt: plt, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: it.ns.ns + ' *') + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else if (key == 'org') {
                    if (plt.org) {
                        content.add( plt.org.name )
                    } else {
                        content.add('')
                    }
                }
                else if (key == 'serviceProvider') {
                    content.add( getRefdataContent(plt, key) )
                }
                else if (key == 'softwareProvider') {
                    content.add( getRefdataContent(plt, key) )
                }
                else {
                    content.add( '- not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(plt, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- not implemented -' )
                }
            }
            // --> elastic search
            else if (type == FIELD_TYPE_ELASTICSEARCH) {

                if (key in [
                        BaseConfig.ELASTICSEARCH_KEY_PLT_IP_AUTHENTICATION,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_SHIBBOLETH_AUTHENTICATION,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_PASSWORD_AUTHENTICATION,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_PROXY_SUPPORTED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_STATISTICS_FORMAT,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_STATISTICS_UPDATE,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTER_CERTIFIED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTERR3_SUPPORTED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTERR4_SUPPORTED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTERR5_SUPPORTED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTERR4_SUSHI_SUPPORTED,
                        BaseConfig.ELASTICSEARCH_KEY_PLT_COUNTERR5_SUSHI_SUPPORTED
                ]) {
                    Map<String, Object> record = GlobalExportHelper.getFilterCache(token).data.platformESRecords.get(obj.id.toString())

                    String value = record?.get( key )
                    if (value) {
                        String rdc = PlatformXCfg.ES_DATA.get( BaseConfig.KEY_PLATFORM + '-' + key )
                        RefdataValue rdv = rdc ? RefdataValue.getByValueAndCategory(value, rdc) : null

                        if (rdv) {
                            content.add(rdv.getI10n('value'))
                        } else {
                            content.add( '(' + value + ')' )
                        }
                    }
                    else {
                        content.add( '' )
                    }
                }
                else {
                    content.add( '- not implemented -' )
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
