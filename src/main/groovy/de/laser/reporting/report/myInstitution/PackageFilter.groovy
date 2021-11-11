package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.Package
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.EsIndexHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.config.PackageXCfg
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class PackageFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (pkg.id) from Package pkg']
        List<String> whereParts         = [ 'where pkg.id in (:packageIdList)']
        Map<String, Object> queryParams = [ packageIdList: [] ]

        ApplicationContext mainContext = Holders.grailsApplication.mainContext
        ContextService contextService  = mainContext.getBean('contextService')

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'package' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: BaseConfig.getMessage(BaseConfig.KEY_PACKAGE + '.source.' + filterSource)])

        switch (filterSource) {
            case 'all-pkg':
                queryParams.packageIdList = Package.executeQuery( 'select pkg.id from Package pkg where pkg.packageStatus != :pkgStatus',
                        [pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
                )
                break
            case 'my-pkg':
                List<Long> subIdList = Subscription.executeQuery(
                        "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx)) and s.status.value != 'Deleted'",
                        [roleTypes: [
                                RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN
                        ], ctx: contextService.getOrg()])

                queryParams.packageIdList = Package.executeQuery(
                        'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:idList) and subPkg.pkg.packageStatus != :pkgStatus',
                        [idList: subIdList, pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'package_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'pkg.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES) {
                            whereParts.add( 'pkg.' + p + ' is true' )
                        }
                        else if (rdv == RDStore.YN_NO) {
                            whereParts.add( 'pkg.' + p + ' is false' )
                        }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'pkg.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation != es_data
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    if ( ! PackageXCfg.ES_DATA.contains( p )) {
                        println ' ------------ not implemented ------------ '
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'PackageFilter.filter() -->'
//
//        println query
//        println queryParams
//        println whereParts

        List<Long> idList = queryParams.packageIdList ? Package.executeQuery( query, queryParams ) : []
        Map<String, Object> esRecords = [:]
        if (idList) {
            esRecords = EsIndexHelper.getEsRecords( idList ).records as Map<String, Object>
        }
        boolean esFilter = false

        getCurrentFilterKeys(params, cmbKey).each { key ->
            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p)

                String filterLabelValue

                // --> es_data
                if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL && PackageXCfg.ES_DATA.contains( p )) {

                    RefdataValue rdv = RefdataValue.get(params.long(key))

                    if (p == BaseConfig.CUSTOM_IMPL_KEY_PKG_BREAKABLE) {
                        esRecords = esRecords.findAll{ it.value.breakable == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PKG_CONSISTENT) {
                        esRecords = esRecords.findAll{ it.value.consistent == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PKG_OPENACCESS) {
                        esRecords = esRecords.findAll{ it.value.openAccess == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PKG_PAYMENTTYPE) {
                        esRecords = esRecords.findAll{ it.value.paymentType == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PKG_SCOPE) {
                        esRecords = esRecords.findAll{ it.value.scope == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p), value: filterLabelValue])
                    esFilter = true
                }
            }
        }

        if (esFilter) {
            idList = esRecords.keySet()?.collect{ Long.parseLong(it) }
        }
        filterResult.data.put('packageIdList', idList)
        filterResult.data.put('packageEsRecords', esRecords)

//        println 'packages (raw) >> ' + queryParams.packageIdList.size()
//        println 'packages (queried) >> ' + filterResult.data.packageIdList.size() + ' - ' + filterResult.data.packageEsRecords.size()

        filterResult
    }
}
