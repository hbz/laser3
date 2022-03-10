package de.laser.reporting.export.myInstitution

import de.laser.ApiSource
import de.laser.ContextService
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
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
                                    'globalUID'                     : [ type: FIELD_TYPE_PROPERTY ],
                                    'gokbId'                        : [ type: FIELD_TYPE_PROPERTY ],
                                    'name'                          : [ type: FIELD_TYPE_PROPERTY ],
                                    'altname'                       : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'org+sortname+name'             : [ type: FIELD_TYPE_COMBINATION ], // 'platform/org+sortname+name'
                                    'primaryUrl'                    : [ type: FIELD_TYPE_PROPERTY ],
                                    'serviceProvider'               : [ type: FIELD_TYPE_REFDATA ],
                                    'softwareProvider'              : [ type: FIELD_TYPE_REFDATA ],
                                    'status'                        : [ type: FIELD_TYPE_REFDATA ],
                                    'ipAuthentication'              : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'shibbolethAuthentication'      : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'passwordAuthentication'        : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'proxySupported'                : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'statisticsFormat'              : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'statisticsUpdate'              : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterCertified'              : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR3Supported'            : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR4Supported'            : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR4SushiApiSupported'    : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR5Supported'            : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR5SushiApiSupported'    : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-property'                    : [ type: FIELD_TYPE_CUSTOM_IMPL_QDP ]
                            ]
                    ]
            ]
    ]

    static List<String> ES_SOURCE_FIELDS = [

            "uuid", "providerUuid",
            "altname",
            "ipAuthentication", "shibbolethAuthentication", "passwordAuthentication", "proxySupported",
            "statisticsFormat", "statisticsUpdate", "counterCertified",
            "counterR3Supported", "counterR4Supported", "counterR4SushiApiSupported", "counterR5Supported", "counterR5SushiApiSupported",
            "lastUpdatedDisplay"
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

        Map<String, Map> esdConfig  = BaseConfig.getCurrentConfigElasticsearchData( KEY )

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

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
                            ApiSource wekb = ElasticSearchHelper.getCurrentApiSource()
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

                content.add( '- ' + key + ' not implemented -' )
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(plt, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> elastic search
            else if (type == FIELD_TYPE_ELASTICSEARCH) {
                String esDataKey = BaseConfig.KEY_PLATFORM + '-' + key
                Map<String, Object> esData = esdConfig.get( esDataKey )

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
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> combined properties : TODO
            else if (key in ['org+sortname', 'org+name']) {
                String prop = key.split('\\+')[1]
                content.add( plt.org?.getProperty(prop) ?: '' )
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
