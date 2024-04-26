package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.Vendor
import de.laser.auth.Role
import de.laser.properties.PropertyDefinition
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap

class ProviderFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (ven.id) from Vendor ven']
        List<String> whereParts         = [ 'where ven.id in (:providerIdList)']
        Map<String, Object> queryParams = [ providerIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, 'provider')
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_VENDOR, filterSource)])

        switch (filterSource) {
            case 'all-provider':
                queryParams.providerIdList = _getAllProviderIdList()
                break
            case 'my-provider':
                queryParams.providerIdList = _getMyProviderIdList()
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'provider_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_VENDOR ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Vendor.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'ven.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Vendor.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'ven.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'ven.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'ven.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {

                    if (p == BaseConfig.RDJT_GENERIC_ORG_TYPE) {
                        whereParts.add('exists (select ot from org.orgType ot where ot = :p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, RefdataValue.get(params.long(key)))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CI_GENERIC_SUBJECT_GROUP) {
                        queryParts.add('OrgSubjectGroup osg')
                        whereParts.add('osg.org = org and osg.subjectGroup.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CI_GENERIC_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == BaseConfig.CI_GENERIC_CUSTOMER_TYPE) {
                        queryParts.add('OrgSetting oss')

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.long(key)))

                        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                    else if (p == BaseConfig.CI_CTX_PROPERTY_KEY) {
                        Long pValue = params.long('filter:org_propertyValue')

                        String pq = getPropertyFilterSubQuery(
                                'OrgProperty', 'org',
                                params.long(key),
                                pValue,
                                queryParams
                        )
                        whereParts.add( '(exists (' + pq + '))' )
                        filterLabelValue = PropertyDefinition.get(params.long(key)).getI10n('name') + ( pValue ? ': ' + RefdataValue.get( pValue ).getI10n('value') : '')
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_VENDOR ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'VendorFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('providerIdList', queryParams.providerIdList ? Vendor.executeQuery( query, queryParams ) : [])

//        println 'providers >> ' + result.providerIdList.size()

        filterResult
    }

    static List<Long> _getAllProviderIdList() {

        List<Long> idList = Vendor.executeQuery(
                'select ven.id from Vendor ven where (ven.status is null or ven.status != :vendorStatus)',
                [vendorStatus: RDStore.VENDOR_STATUS_DELETED]
        )

        idList
    }

    static List<Long> _getMyProviderIdList() { // TODO TODO TODO

        ContextService contextService = BeanStore.getContextService()

        List<Long> idList = Vendor.executeQuery(
                'select ven.id from Vendor ven where (ven.status is null or ven.status != :vendorStatus)',
                [vendorStatus: RDStore.VENDOR_STATUS_DELETED]
        )

//        List<Long> idList = Org.executeQuery( '''
//            select distinct(ven.vendor.id) from VendorRole ven
//                join ven.sub sub
//                join sub.orgRelations subOr
//            where (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))
//                and (prov.org.status is null or prov.org.status != :orgStatus)
//            ''',
//            [
//                    org: contextService.getOrg(), vendorStatus: RDStore.VENDOR_STATUS_DELETED,
//                    subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]
//            ]
//        )
        idList
    }
}
