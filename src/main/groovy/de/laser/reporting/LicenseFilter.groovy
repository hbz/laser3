package de.laser.reporting

import de.laser.License
import de.laser.Org
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.auth.Role
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class LicenseFilter extends GenericFilter {

    def contextService
    def filterService
    def licenseService

    LicenseFilter() {
        ApplicationContext mainContext  = Holders.grailsApplication.mainContext
        contextService                  = mainContext.getBean('contextService')
        filterService                   = mainContext.getBean('filterService')
        licenseService                  = mainContext.getBean('licenseService')
    }

    Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> result      = [ filterLabels : [:] ]

        List<String> queryParts         = [ 'select distinct (lic.id) from License lic']
        List<String> whereParts         = [ 'where lic.id in (:licenseIdList)']
        Map<String, Object> queryParams = [ licenseIdList : [] ]

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + 'license' + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put('base', [source: getFilterSourceLabel(LicenseConfig.CONFIG.base, filterSource)])

        switch (filterSource) {
            case 'all-lic':
                queryParams.licenseIdList = License.executeQuery( 'select l.id from License l' )
                break
            case 'my-lic':
                List tmp1 = licenseService.getLicensesConsortiaQuery( [:] )
                List tmp2 = licenseService.getLicensesConsortialLicenseQuery( [:] )
                List tmp3 = licenseService.getLicensesLocalLicenseQuery( [:] )

                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp1[0], tmp1[1]) )
                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp2[0], tmp2[1]) )
                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp3[0], tmp3[1]) )

                queryParams.licenseIdList.unique()
                break
        }

        String cmbKey = GenericConfig.FILTER_PREFIX + 'license_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each{ key ->
            if (params.get(key)) {
                //println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = getFieldType(LicenseConfig.CONFIG.base, p)

                def filterLabelValue

                // --> generic properties
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {
                    if (License.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'lic.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (License.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'lic.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'lic.' + p + ' is false' )
                        }
                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> generic refdata
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'lic.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == GenericConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    println ' ------------ not implemented ------------ '
                }

                if (filterLabelValue) {
                    result.filterLabels.get('base').put(p, [label: getFieldLabel(LicenseConfig.CONFIG.base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'LicenseFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        result.licenseIdList = License.executeQuery( query, queryParams )

        handleInternalOrgFilter(params, 'member', result)
        handleInternalOrgFilter(params, 'licensor', result)

//        println 'licenses >> ' + result.licenseIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'licensor >> ' + result.licensorIdList.size()

        result
    }

    private void handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> result) {

        String filterSource = params.get(GenericConfig.FILTER_PREFIX + partKey + GenericConfig.FILTER_SOURCE_POSTFIX)
        result.filterLabels.put(partKey, [source: getFilterSourceLabel(LicenseConfig.CONFIG.get(partKey), filterSource)])

        //println 'internalOrgFilter() ' + params + ' >>>>>>>>>>>>>>>< ' + partKey
        if (! result.licenseIdList) {
            result.put( partKey + 'IdList', [] )
            return
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.lic.id in (:licenseIdList)' ]
        Map<String, Object> queryParams = [ 'licenseIdList': result.licenseIdList ]

        if (partKey == 'member') {
            queryParams.put( 'roleTypes', [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS] ) // TODO <- RDStore.OR_SUBSCRIBER
            // check ONLY members
            queryParams.licenseIdList = License.executeQuery(
                    'select distinct (lic.id) from License lic where lic.instanceOf.id in (:licenseIdList)',
                    [ licenseIdList: queryParams.licenseIdList ]
            )
        }
        if (partKey == 'licensor') {
            queryParams.put( 'roleTypes', [RDStore.OR_LICENSOR] )
        }

        String cmbKey = GenericConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType
                if (partKey == 'member') {
                    pType = getFieldType(LicenseConfig.CONFIG.member, p)
                }
                else if (partKey == 'licensor') {
                    pType = getFieldType(LicenseConfig.CONFIG.licensor, p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == GenericConfig.FIELD_TYPE_PROPERTY) {

                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'org.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'org.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'org.' + p + ' is false' )
                        }
                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else {
                        whereParts.add( 'org.' + p + ' = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.get(key) )

                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata relation tables
                else if (pType == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {

                    if (p == GenericConfig.CUSTOM_KEY_SUBJECT_GROUP) {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                }
                // --> custom filter implementation
                else if (pType == GenericConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == GenericConfig.CUSTOM_KEY_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = GenericConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == GenericConfig.CUSTOM_KEY_CUSTOMER_TYPE) {
                        queryBase = queryBase + ' , OrgSetting oss'

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.get(key)))

                        Map<String, Object> customRdv = GenericConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                }

                if (filterLabelValue) {
                    if (partKey == 'member') {
                        result.filterLabels.get(partKey).put(p, [label: getFieldLabel(LicenseConfig.CONFIG.member, p), value: filterLabelValue])
                    }
                    else if (partKey == 'licensor') {
                        result.filterLabels.get(partKey).put(p, [label: getFieldLabel(LicenseConfig.CONFIG.licensor, p), value: filterLabelValue])
                    }
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

//        println 'LicenseFilter.internalOrgFilter() -->'
//        println query

        result.put( partKey + 'IdList', Org.executeQuery(query, queryParams) )
    }
}
