package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.CustomerTypeService
import de.laser.Org
import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.auth.Role
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap

class OrganisationFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (org.id) from Org org']
        List<String> whereParts         = [ 'where org.id in (:orgIdList)']
        Map<String, Object> queryParams = [ orgIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, 'org')
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_ORGANISATION, filterSource)])

        switch (filterSource) {
            case 'all-org':
                queryParams.orgIdList = Org.executeQuery('select o.id from Org o')
                break
            case 'all-consortium':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o, OrgSetting os where os.org = o and os.key = :ct and os.roleValue.authority in (:roles)',
                        [ct: OrgSetting.KEYS.CUSTOMER_TYPE, roles: [CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO]]
                )
                break
            case 'all-inst':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org o, OrgSetting os where os.org = o and os.key = :ct and os.roleValue.authority in (:roles)',
                        [ct: OrgSetting.KEYS.CUSTOMER_TYPE, roles: [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]]
                )
                break
            case 'my-inst':
                queryParams.orgIdList = Org.executeQuery(
                        'select o.id from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :org and c.type = :comboType',
                        [org: contextService.getOrg(), comboType: RDStore.COMBO_TYPE_CONSORTIUM]
                )
                break
            case 'my-consortium':
                queryParams.orgIdList = Org.executeQuery( '''
select distinct(consOr.org.id) from OrgRole consOr 
    join consOr.sub sub join sub.orgRelations subOr 
where (consOr.roleType = :consRoleType) 
    and (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))  ''',
                        [
                                org: contextService.getOrg(),
                                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                                consRoleType: RDStore.OR_SUBSCRIPTION_CONSORTIUM
                        ]
                )

                queryParams.orgIdList.addAll( Org.executeQuery( '''
select distinct(consOr.org.id) from OrgRole consOr 
    join consOr.lic lic join lic.orgRelations licOr 
where (consOr.roleType = :consRoleType) 
    and (lic = licOr.lic and licOr.org = :org and licOr.roleType in (:licRoleTypes))  ''',
                        [
                                org: contextService.getOrg(),
                                licRoleTypes: [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS],
                                consRoleType: RDStore.OR_LICENSING_CONSORTIUM
                        ]
                ) )
                queryParams.orgIdList = queryParams.orgIdList.unique()
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'org_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base, p)

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
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'org.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'org.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'org.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
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
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'OrganisationFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('orgIdList', queryParams.orgIdList ? Org.executeQuery( query, queryParams ) : [])

//        println 'orgs >> ' + result.orgIdList.size()

        filterResult
    }
}
