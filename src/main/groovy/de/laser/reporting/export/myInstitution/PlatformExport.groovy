package de.laser.reporting.export.myInstitution

import de.laser.ApiSource
import de.laser.ContextService
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.Platform
import de.laser.RefdataValue
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
                                    'gokbId'                : FIELD_TYPE_PROPERTY,
                                    'name'                  : FIELD_TYPE_PROPERTY,
                                    'altname'               : FIELD_TYPE_ELASTICSEARCH,
                                    'org'                   : FIELD_TYPE_CUSTOM_IMPL,
                                    'primaryUrl'            : FIELD_TYPE_PROPERTY,
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
                    content.add( g.createLink( controller: 'platform', action: 'show', absolute: true ) + '/' + plt.getProperty(key) + '@' + plt.getProperty(key) )
                }
                else if (key == 'gokbId') {
                    String prop = ''
                    if (plt.getProperty(key)) {
                        Map<String, Object> fCache = GlobalExportHelper.getFilterCache(token)
                        List<Long> esRecordIdList = fCache.data.platformESRecords.keySet().collect{ Long.parseLong(it) }

                        if (esRecordIdList.contains(plt.id)) {
                            ApiSource wekb = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                            if (wekb?.baseUrl) {
                                prop = wekb.baseUrl + '/public/platformContent/' + plt.getProperty(key) + '@' + plt.getProperty(key)
                            }
                        }
                    }
                    content.add( prop )
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

                /* if (key == 'x-identifier') { // not used ?
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.plt = :plt and i.ns.id in (:idnsList)",
                                [plt: plt, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: it.ns.ns + ' *') + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else */
                if (key == 'org') {
                    if (plt.org) {
                        content.add( plt.org.name )
                    }
                    else {
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
                String esDataKey = BaseConfig.KEY_PLATFORM + '-' + key
                Map<String, Object> esData = PlatformXCfg.ES_DATA.get( esDataKey )

                if (esData?.export) {
                    Map<String, Object> record = GlobalExportHelper.getFilterCache(token).data.platformESRecords.get(obj.id.toString())

                    if (key == 'altname') {
                        List<String> altNames = record?.get( key )?.collect { an ->
                            an.toString()
                        }
                        content.add (altNames ? altNames.join( CSV_VALUE_SEPARATOR ) : '')
                    }
                    else {
                        String value = record?.get( esData.mapping ?: key )
                        if (value) {
                            String rdc = esData.rdc
                            RefdataValue rdv = rdc ? RefdataValue.getByValueAndCategory(value, rdc) : null

                            if (rdv) {
                                content.add(rdv.getI10n('value'))
                            } else if (rdc) {
                                content.add( '(' + value + ')' )
                            } else {
                                content.add( value )
                            }
                        }
                        else {
                            content.add( '' )
                        }
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
