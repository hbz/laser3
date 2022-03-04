package de.laser.reporting.export.myInstitution

import de.laser.ApiSource
import de.laser.ContextService
import de.laser.IdentifierNamespace
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseDetails
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class PackageExport extends BaseDetailsExport {

    static String KEY = BaseConfig.KEY_PACKAGE

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: de.laser.Package
                    ],
                    fields : [
                            default: [
                                    'globalUID'                     : [ type: FIELD_TYPE_PROPERTY ],
                                    'gokbId'                        : [ type: FIELD_TYPE_PROPERTY ],
                                    'name'                          : [ type: FIELD_TYPE_PROPERTY ],
                                    'altname'                       : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-id'                          : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-provider+sortname+name'      : [ type: FIELD_TYPE_COMBINATION ],
                                    'x-platform+name+primaryUrl'    : [ type: FIELD_TYPE_COMBINATION ],
                                    'contentType'                   : [ type: FIELD_TYPE_REFDATA ],
                                    'file'                          : [ type: FIELD_TYPE_REFDATA ],
                                    'packageStatus'                 : [ type: FIELD_TYPE_REFDATA ],
                                    '@-package-titleCount'          : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                                    'scope'                         : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'consistent'                    : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'paymentType'                   : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'openAccess'                    : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'breakable'                     : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-ddc'                         : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-curatoryGroup'               : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'x-archivingAgency'             : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'description'                   : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                    'descriptionURL'                : [ type: FIELD_TYPE_ELASTICSEARCH ]
                            ]
                    ]
            ]
    ]

    static List<String> ES_SOURCE_FIELDS = [

            "uuid",
            "openAccess", "paymentType", "scope",
            "altname", "description", "descriptionURL",
            "curatoryGroups.*", "packageArchivingAgencies.*", "ddcs.*", "identifiers.*", "nationalRanges.*", "regionalRanges.*",
            "lastUpdatedDisplay"
    ]

    PackageExport(String token, Map<String, Object> fields) {
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

        Package pkg = obj as Package
        List content = []

        Map<String, Map> esdConfig  = BaseConfig.getCurrentConfigElasticsearchData( KEY )

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'package', action: 'show', absolute: true ) + '/' + pkg.getProperty(key) + '@' + pkg.getProperty(key) )
                }
                else if (key == 'gokbId') {
                    String prop = ''
                    if (pkg.getProperty(key)) {
                        Map<String, Object> fCache = GlobalExportHelper.getFilterCache(token)
                        List<Long> esRecordIdList = fCache.data.packageESRecords.keySet().collect{ Long.parseLong(it) }

                        if (esRecordIdList.contains(pkg.id)) {
                            ApiSource wekb = ElasticSearchHelper.getCurrentApiSource()
                            if (wekb?.baseUrl) {
                                prop = wekb.baseUrl + '/public/packageContent/' + pkg.getProperty(key) + '@' + pkg.getProperty(key)
                            }
                        }
                    }
                    content.add( prop )
                }
                else {
                    content.add( getPropertyContent(pkg, key, Package.getDeclaredField(key).getType()))
                }
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(pkg, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(pkg, key) )
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                /* if (key == 'x-identifier') { // not used ?
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.pkg = :pkg and i.ns.id in (:idnsList)",
                                [pkg: pkg, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( CSV_VALUE_SEPARATOR ))
                }
                else */
                if (key == '@-package-titleCount') {
                    int titles = TitleInstancePackagePlatform.executeQuery( 'select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status',
                            [pkg: pkg, status: RDStore.TIPP_STATUS_CURRENT]
                    )[0]
                    content.add( titles )
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> custom query depending filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL_QDP) {

                if (key == 'x-property') {
                    Long pdId = GlobalExportHelper.getDetailsCache(token).id as Long

                    List<String> properties = BaseDetails.resolvePropertiesGeneric(pkg, pdId, contextService.getOrg())
                    content.add( properties.findAll().join( CSV_VALUE_SEPARATOR ) ) // removing empty and null values
                }
                else {
                    content.add( '- ' + key + ' not implemented -' )
                }
            }
            // --> elastic search
            else if (type == FIELD_TYPE_ELASTICSEARCH) {
                String esDataKey = BaseConfig.KEY_PACKAGE + '-' + key
                Map<String, Object> esData = esdConfig.get( esDataKey )

                if (esData?.export) {
                    Map<String, Object> record = GlobalExportHelper.getFilterCache(token).data.packageESRecords.get(obj.id.toString())

                    if (key == 'altname') {
                        List<String> altNames = record?.get( key )?.collect { an ->
                            an.toString()
                        }
                        content.add (altNames ? altNames.join( CSV_VALUE_SEPARATOR ) : '')
                    }
                    else if (key == 'x-curatoryGroup') {
                        List<String> cgList = record?.get( esData.mapping )?.collect{ cg ->
                            String cgType = RefdataValue.getByValueAndCategory(cg.type as String, RDConstants.ORG_TYPE)?.getI10n('value') ?: '(' + cg.type + ')'
                            cg.name + ( cgType ? ' - ' + cgType : '')
                        }
                        content.add (cgList ? cgList.join( CSV_VALUE_SEPARATOR ) : '')
                    }
                    else if (key == 'x-archivingAgency') {
                        List<String> aaList = record?.get( esData.mapping )?.collect{ aa ->
                            aa.archivingAgency
                        }
                        content.add (aaList ? aaList.join( CSV_VALUE_SEPARATOR ) : '')
                    }
                    else if (key == 'x-ddc') {
                        List<String> ddcList = record?.get( esData.mapping )?.collect{ ddc ->
                            RefdataValue rdv = RefdataValue.getByValueAndCategory(ddc.value as String, esData.rdc as String)
                            if (rdv) {
                                rdv.getI10n('value')
                            } else {
                                '(' + value + ')'
                            }
                        }
                        content.add (ddcList ? ddcList.join( CSV_VALUE_SEPARATOR ) : '')
                    }
                    else if (key == 'x-id') {
                        List<String> idList = record?.get( esData.mapping )?.collect{ id ->
                            IdentifierNamespace ns = IdentifierNamespace.findByNsAndNsType(id.namespace, 'de.laser.Package')
                            ns ? ((ns.getI10n('name') ?: ns.ns) + ':' + id.value) : GenericHelper.flagUnmatched( id.namespaceName ?: id.namespace ) + ':' + id.value
                        }
                        content.add (idList ? idList.join( CSV_VALUE_SEPARATOR ) : '')
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
            else if (key in ['x-provider+sortname', 'x-provider+name']) {
                List<Org> prvds = Org.executeQuery(
                        'select o from Package pkg join pkg.orgs ro join ro.org o where ro.roleType in (:prov) and pkg.id = :id order by o.sortname, o.name',
                        [id: pkg.id, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER]]
                )
                String prop = key.split('\\+')[1]
                content.add( prvds.collect{ it.getProperty(prop) ?: '' }.join( CSV_VALUE_SEPARATOR ))
            }
            // --> combined properties : TODO
            else if (key in ['x-platform+name', 'x-platform+primaryUrl']) {
                List<Platform> plts = Platform.executeQuery(
                        'select p from Package pkg join pkg.nominalPlatform p where pkg.id = :id order by p.name',
                        [id: pkg.id]
                )
                String prop = key.split('\\+')[1]
                content.add( plts.collect{ it.getProperty(prop) ?: '' }.join( CSV_VALUE_SEPARATOR ))

            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
