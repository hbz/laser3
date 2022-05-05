package de.laser.reporting.export.myInstitution

import de.laser.ApiSource
import de.laser.ContextService
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.storage.BeanStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
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
                                    'globalUID'                     : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'gokbId'                        : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'name'                          : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'altname'                       : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'org+sortname+name'             : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ], // 'platform/org+sortname+name'
                                    'primaryUrl'                    : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'serviceProvider'               : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'softwareProvider'              : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'status'                        : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'ipAuthentication'              : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'shibbolethAuthentication'      : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'passwordAuthentication'        : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'proxySupported'                : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'statisticsFormat'              : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'statisticsUpdate'              : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterCertified'              : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR3Supported'            : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR4Supported'            : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR4SushiApiSupported'    : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR5Supported'            : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'counterR5SushiApiSupported'    : [ type: BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH ],
                                    'x-property'                    : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP ]
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

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        ContextService contextService = BeanStore.getContextService()

        Platform plt = obj as Platform
        List content = []

        Map<String, Map> esdConfig  = BaseConfig.getCurrentConfigElasticsearchData( KEY )

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

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
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(plt, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(plt, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

                content.add( '- ' + key + ' not implemented -' )
            }
            // --> custom query depending filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(plt, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> elastic search
            else if (type == BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH) {
                String esDataKey = BaseConfig.KEY_PLATFORM + '-' + key
                Map<String, Object> esData = esdConfig.get( esDataKey )

                if (esData?.export) {
                    Map<String, Object> record = GlobalExportHelper.getFilterCache(token).data.platformESRecords.get(obj.id.toString())

                    if (key == 'altname') {
                        List<String> altNames = record?.get( key )?.collect { an ->
                            an.toString()
                        }
                        content.add (altNames ? altNames.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) : '')
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
