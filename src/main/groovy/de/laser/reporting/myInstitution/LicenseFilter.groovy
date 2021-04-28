package de.laser.reporting.myInstitution

import de.laser.License
import de.laser.Org
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.auth.Role
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseFilter
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class LicenseFilter extends BaseFilter {

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
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (lic.id) from License lic']
        List<String> whereParts         = [ 'where lic.id in (:licenseIdList)']
        Map<String, Object> queryParams = [ licenseIdList: [] ]

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'license' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: getFilterSourceLabel(LicenseConfig.getCurrentConfig().base, filterSource)])

        switch (filterSource) {
            case 'all-lic':
                queryParams.licenseIdList = License.executeQuery( 'select l.id from License l' )
                break
            case 'consortia-lic':
                List tmp1 = licenseService.getLicensesConsortiaQuery( [:] )         // roleType:Licensing Consortium
                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp1[0], tmp1[1]) )
                queryParams.licenseIdList.unique()
                break
            case 'my-lic':
                List tmp2 = licenseService.getLicensesConsortialLicenseQuery( [:] ) // roleType:Licensee_Consortial
                List tmp3 = licenseService.getLicensesLocalLicenseQuery( [:] )      // roleType:Licensee
                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp2[0], tmp2[1]) )
                queryParams.licenseIdList.addAll( License.executeQuery( 'select l.id ' + tmp3[0], tmp3[1]) )
                queryParams.licenseIdList.unique()
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'license_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each{ key ->
            if (params.get(key)) {
                //println key + " >> " + params.get(key)

                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(LicenseConfig.getCurrentConfig().base, p)

                def filterLabelValue

                // --> generic properties
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
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
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'lic.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    println ' ------------ not implemented ------------ '
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(LicenseConfig.getCurrentConfig().base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'LicenseFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put( 'licenseIdList', queryParams.licenseIdList ? License.executeQuery( query, queryParams ) : [] )

        //if (LicenseConfig.getCurrentConfig().member) {
            //handleInternalOrgFilter(params, 'member', result)
        //}
        if (LicenseConfig.getCurrentConfig().licensor) {
            handleInternalOrgFilter(params, 'licensor', filterResult)
        }

//        println 'licenses >> ' + result.licenseIdList.size()
//        println 'member >> ' + result.memberIdList.size()
//        println 'licensor >> ' + result.licensorIdList.size()

        filterResult
    }

    private void handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> filterResult) {

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + partKey + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put(partKey, [source: getFilterSourceLabel(LicenseConfig.getCurrentConfig().get(partKey), filterSource)])

        //println 'internalOrgFilter() ' + params + ' >>>>>>>>>>>>>>>< ' + partKey
        if (! filterResult.data.get('licenseIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
            return
        }

        String queryBase = 'select distinct (org.id) from Org org join org.links orgLink'
        List<String> whereParts = [ 'orgLink.roleType in (:roleTypes)', 'orgLink.lic.id in (:licenseIdList)' ]
        Map<String, Object> queryParams = [ 'licenseIdList': filterResult.data.licenseIdList ]

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

        String cmbKey = BaseConfig.FILTER_PREFIX + partKey + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType
                if (partKey == 'member') {
                    pType = GenericHelper.getFieldType(LicenseConfig.getCurrentConfig().member, p)
                }
                else if (partKey == 'licensor') {
                    pType = GenericHelper.getFieldType(LicenseConfig.getCurrentConfig().licensor, p)
                }

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {

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
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {

                    if (p == BaseConfig.CUSTOM_KEY_SUBJECT_GROUP) {
                        queryBase = queryBase + ' join org.subjectGroup osg join osg.subjectGroup rdvsg'
                        whereParts.add('rdvsg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CUSTOM_KEY_LEGAL_INFO) {
                        long li = params.long(key)
                        whereParts.add( getLegalInfoQueryWhereParts(li) )

                        Map<String, Object> customRdv = BaseConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == li }.value_de
                    }
                    else if (p == BaseConfig.CUSTOM_KEY_CUSTOMER_TYPE) {
                        queryBase = queryBase + ' , OrgSetting oss'

                        whereParts.add('oss.org = org and oss.key = :p' + (++pCount))
                        queryParams.put('p' + pCount, OrgSetting.KEYS.CUSTOMER_TYPE)

                        whereParts.add('oss.roleValue = :p' + (++pCount))
                        queryParams.put('p' + pCount, Role.get(params.get(key)))

                        Map<String, Object> customRdv = BaseConfig.getCustomRefdata(p)
                        filterLabelValue = customRdv.get('from').find{ it.id == params.long(key) }.value_de
                    }
                }

                if (filterLabelValue) {
                    if (partKey == 'member') {
                        filterResult.labels.get(partKey).put(p, [label: GenericHelper.getFieldLabel(LicenseConfig.getCurrentConfig().member, p), value: filterLabelValue])
                    }
                    else if (partKey == 'licensor') {
                        filterResult.labels.get(partKey).put(p, [label: GenericHelper.getFieldLabel(LicenseConfig.getCurrentConfig().licensor, p), value: filterLabelValue])
                    }
                }
            }
        }

        String query = queryBase + ' where ' + whereParts.join(' and ')

//        println 'LicenseFilter.internalOrgFilter() -->'
//        println query
//        println queryParams

        filterResult.data.put( partKey + 'IdList', queryParams.licenseIdList ? Org.executeQuery(query, queryParams) : [] )
    }
}
