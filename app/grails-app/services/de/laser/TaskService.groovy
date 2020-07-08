package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class TaskService {

    final static WITHOUT_TENANT_ONLY = "WITHOUT_TENANT_ONLY"

    def springSecurityService
    def accessService
    def filterService
    def messageSource

    private static final String select_with_join = 'select t from Task t LEFT JOIN t.responsibleUser ru '

    def getTasksByCreator(User user, Map queryMap, flag) {
        def tasks = []
        if (user) {
            def query
            if (flag == WITHOUT_TENANT_ONLY) {
                query = select_with_join + 'where t.creator = :user and ru is null and t.responsibleOrg is null'
            } else {
                query = select_with_join + 'where t.creator = :user'
            }

            def params = [user : user]
            if (queryMap){
                query += queryMap.query
                query = addDefaultOrder("t", query)
                params << queryMap.queryParams
            }
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }
    def getTasksByCreatorAndObject(User user, License obj,  Object params) {
        (user && obj)? Task.findAllByCreatorAndLicense(user, obj, params) : []
    }
    def getTasksByCreatorAndObject(User user, Org obj,  Object params) {
        (user && obj) ?  Task.findAllByCreatorAndOrg(user, obj, params) : []
    }
    def getTasksByCreatorAndObject(User user, Package obj,  Object params) {
        (user && obj) ?  Task.findAllByCreatorAndPkg(user, obj, params) : []
    }
    def getTasksByCreatorAndObject(User user, Subscription obj,  Object params) {
        (user && obj) ?  Task.findAllByCreatorAndSubscription(user, obj, params) : []
    }
    def getTasksByCreatorAndObject(User user, SurveyConfig obj,  Object params) {
        (user && obj) ?  Task.findAllByCreatorAndSurveyConfig(user, obj, params) : []
    }
    def getTasksByCreatorAndObject(User user, License obj ) {
        (user && obj)? Task.findAllByCreatorAndLicense(user, obj) : []
    }
    def getTasksByCreatorAndObject(User user, Org obj ) {
        (user && obj) ?  Task.findAllByCreatorAndOrg(user, obj) : []
    }
    def getTasksByCreatorAndObject(User user, Package obj ) {
        (user && obj) ?  Task.findAllByCreatorAndPkg(user, obj) : []
    }
    def getTasksByCreatorAndObject(User user, Subscription obj) {
        (user && obj) ?  Task.findAllByCreatorAndSubscription(user, obj) : []
    }
    def getTasksByCreatorAndObject(User user, SurveyConfig obj) {
        (user && obj) ?  Task.findAllByCreatorAndSurveyConfig(user, obj) : []
    }

    List chopOffForPageSize(List taskInstanceList, User user, int offset){
        //chop everything off beyond the user's pagination limit
        int taskInstanceCount = taskInstanceList?.size() ?: 0
        if (taskInstanceCount > user.getDefaultPageSizeTMP()) {
            try {
                taskInstanceList = taskInstanceList.subList(offset, offset + Math.toIntExact(user.getDefaultPageSizeTMP()))
            }
            catch (IndexOutOfBoundsException e) {
                taskInstanceList = taskInstanceList.subList(offset, taskInstanceCount)
            }
        }
        taskInstanceList
    }
    def getTasksByResponsible(User user, Map queryMap) {
        def tasks = []
        if (user) {
            def query  = select_with_join + 'where t.responsibleUser = :user' + queryMap.query
            query = addDefaultOrder("t", query)
            def params = [user : user] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    def getTasksByResponsible(Org org, Map queryMap) {
        def tasks = []
        if (org) {
            def query  = select_with_join + 'where t.responsibleOrg = :org' + queryMap.query
            query = addDefaultOrder("t", query)
            def params = [org : org] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    def getTasksByResponsibles(User user, Org org, Map queryMap) {
        def tasks = []

        if (user && org) {
            def query = select_with_join + 'where ( ru = :user or t.responsibleOrg = :org ) ' + queryMap.query
            query = addDefaultOrder("t", query)

            def params = [user : user, org: org] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        } else if (user) {
            tasks = getTasksByResponsible(user, queryMap)
        } else if (org) {
            tasks = getTasksByResponsible(org, queryMap)
        }
        tasks
    }

    def getTasksByResponsibleAndObject(User user, Object obj) {
        def tasks = getTasksByResponsibleAndObject(user, obj, [sort: 'endDate', order: 'asc'])
        tasks
    }

    def getTasksByResponsibleAndObject(Org org, Object obj) {
        def tasks = getTasksByResponsibleAndObject(org, obj, [sort: 'endDate', order: 'asc'])
        tasks
    }

    List<Task> getTasksByResponsiblesAndObject(User user, Org org, Object obj) {
        def tasks = []
        String tableName = ''
        if (user && org && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tableName = 'license'
                    break
                case 'Org':
                    tableName = 'org'
                    break
                case 'Package':
                    tableName = 'pkg'
                    break
                case 'Subscription':
                    tableName = 'subscription'
                    break
                case 'SurveyConfig':
                    tableName = 'surveyConfig'
                    break
            }
            tasks = Task.executeQuery("""select distinct(t) from Task t where ${tableName}=:obj and (responsibleUser=:user or responsibleOrg=:org) order by endDate""",
                [user: user, org: org, obj: obj])
        }
        tasks
    }

    def getTasksByResponsibleAndObject(User user, Object obj,  Map params) {
        def tasks = []
        params = addDefaultOrder(null, params)
        if (user && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByResponsibleUserAndLicense(user, obj, params)
                    break
                case 'Org':
                    tasks = Task.findAllByResponsibleUserAndOrg(user, obj, params)
                    break
                case 'Package':
                    tasks = Task.findAllByResponsibleUserAndPkg(user, obj, params)
                    break
                case 'Subscription':
                    tasks = Task.findAllByResponsibleUserAndSubscription(user, obj, params)
                    break
                case 'SurveyConfig':
                    tasks = Task.findAllByResponsibleUserAndSurveyConfig(user, obj, params)
                    break
            }
        }
        tasks
    }

    def getTasksByResponsibleAndObject(Org org, Object obj,  Object params) {
        def tasks = []
        params = addDefaultOrder(null, params)
        if (org && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByResponsibleOrgAndLicense(org, obj, params)
                    break
                case 'Org':
                    tasks = Task.findAllByResponsibleOrgAndOrg(org, obj, params)
                    break
                case 'Package':
                    tasks = Task.findAllByResponsibleOrgAndPkg(org, obj, params)
                    break
                case 'Subscription':
                    tasks = Task.findAllByResponsibleOrgAndSubscription(org, obj, params)
                    break
                case 'SurveyConfig':
                    tasks = Task.findAllByResponsibleOrgAndSurveyConfig(org, obj, params)
                    break
            }
        }
        tasks
    }

    def getTasksByResponsiblesAndObject(User user, Org org, Object obj,  Object params) {
        def tasks = []
        def a = getTasksByResponsibleAndObject(user, obj, params)
        def b = getTasksByResponsibleAndObject(org, obj, params)

        tasks = a.plus(b).unique()
        tasks
    }

    Map<String, Object> getPreconditions(Org contextOrg) {
        Map<String, Object> result = [:]

        result.taskCreator                  = springSecurityService.getCurrentUser()
        result.validResponsibleOrgs         = contextOrg ? [contextOrg] : []
        result.validResponsibleUsers        = getUserDropdown(contextOrg)
        result.validPackages                = getPackagesDropdown(contextOrg)
        result.validOrgsDropdown            = getOrgsDropdown(contextOrg)
        result.validSubscriptionsDropdown   = getSubscriptionsDropdown(contextOrg, false)
        result.validLicensesDropdown        = getLicensesDropdown(contextOrg, false)

        result
    }

    private List<Package> getPackagesDropdown(Org contextOrg) {
        List<Package> validPackages        = Package.findAll("from Package p where p.name != '' and p.name != null order by lower(p.sortName) asc") // TODO
        validPackages
    }

    private List<User> getUserDropdown(Org contextOrg) {
        List<User> validResponsibleUsers   = contextOrg ? User.executeQuery(
                "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3)) order by lower(u.display)",
                [contextOrg]) : []

        validResponsibleUsers
    }

    private List<Map> getOrgsDropdown(Org contextOrg) {
        List validOrgs = []
        List<Map> validOrgsDropdown = []
        if (contextOrg) {
            boolean isInstitution = (contextOrg.getCustomerType() in ['ORG_BASIC_MEMBER','ORG_INST','ORG_INST_COLLECTIVE'])
            boolean isConsortium  = (contextOrg.getCustomerType() == 'ORG_CONSORTIUM')
            def params       = [:]
            params.sort      = isInstitution ? " LOWER(o.name), LOWER(o.shortname)" : " LOWER(o.sortname), LOWER(o.name)"
            def fsq          = filterService.getOrgQuery(params)
            //validOrgs = Org.executeQuery('select o.id, o.name, o.shortname, o.sortname from Org o where (o.status is null or o.status != :orgStatus) order by  LOWER(o.sortname), LOWER(o.name) asc', fsq.queryParams)

            String comboQuery = 'select o.id, o.name, o.shortname, o.sortname from Org o join o.outgoingCombos c where c.toOrg = :toOrg and c.type = :type order by '+params.sort
            if (isConsortium){
                validOrgs = Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_CONSORTIUM])
            } else if (isInstitution){
                validOrgs = Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_DEPARTMENT])
            }
            validOrgs.each {
                Long optionKey = it[0]
                if (isConsortium) {
                    validOrgsDropdown << [optionKey: optionKey, optionValue: (it[1]?:'') + (it[2]?' (':'') + (it[2]?:'') + (it[2]?')':'')]
                } else {
                    validOrgsDropdown << [optionKey: optionKey, optionValue: (it[3]?:'')  + (it[1]?' (':'') + (it[1]?:'')  + (it[1]?')':'')]
                }
            }
        }
        validOrgsDropdown.unique().sort{it.optionValue}
    }


    private List<Map> getSubscriptionsDropdown(Org contextOrg, boolean isWithInstanceOf) {
        List validSubscriptionsMitInstanceOf = []
        List validSubscriptionsOhneInstanceOf = []
        List<Map> validSubscriptionsDropdown = []
        boolean binKonsortium = contextOrg.getCustomerType()  == 'ORG_CONSORTIUM'

        if (contextOrg) {
            if (binKonsortium) {

                def qry_params_for_sub = [
                        'roleTypes' : [
                                RDStore.OR_SUBSCRIBER,
                                RDStore.OR_SUBSCRIBER_CONS,
                                RDStore.OR_SUBSCRIPTION_CONSORTIA,
                                RDStore.OR_SUBSCRIPTION_COLLECTIVE
                        ],
                        'activeInst': contextOrg
                ]
                String i10value = LocaleContextHolder.getLocale().getLanguage() == Locale.GERMAN.getLanguage() ? 'valueDe' : 'valueEn'

                validSubscriptionsOhneInstanceOf = Subscription.executeQuery("""
select s.id, s.name, s.startDate, s.endDate, i10.valueDe from Subscription s, I10nTranslation i10 
where s.status.id = i10.referenceId 
    and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) 
    and s.instanceOf is null 
    and i10.referenceField=:referenceField 
order by lower(s.name), s.endDate"""
                        , qry_params_for_sub << [referenceField: 'value'])

                if (isWithInstanceOf) {
                    validSubscriptionsMitInstanceOf = Subscription.executeQuery("""
select s.id, s.name, s.startDate, s.endDate, i10.valueDe, oo.sortname from Subscription s, Org oo, OrgRole rr, I10nTranslation i10
where s.status.id = i10.referenceId
    and rr.org = oo and rr.sub = s and rr.roleType in (:roleTypesOO)
    and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) )
    and s.instanceOf is not null and i10.referenceField=:referenceField
order by lower(s.name), s.endDate""", qry_params_for_sub << [referenceField: 'value', roleTypesOO: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
                }

            }
        }

        String NO_STATUS = RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        validSubscriptionsMitInstanceOf?.each {

            Long optionKey = it[0]
            String optionValue = (
                    it[1]
                            + ' - '
                            + (it[4] ?: NO_STATUS)
                            + ((it[2] || it[3]) ? ' (' : ' ')
                            + (it[2] ? (it[2]?.format('dd.MM.yy')) : '')
                            + '-'
                            + (it[3] ? (it[3]?.format('dd.MM.yy')) : '')
                            + ((it[2] || it[3]) ? ') ' : ' ')
            )
            if (binKonsortium) {
                optionValue += " - " + it[5]

            } else {
                optionValue += ' - Konsortiallizenz'
            }
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        validSubscriptionsOhneInstanceOf?.each {

            Long optionKey = it[0]
            String optionValue = (
                    it[1]
                            + ' - '
                            + (it[4] ?: NO_STATUS)
                            + ((it[2] || it[3]) ? ' (' : ' ')
                            + (it[2] ? (it[2]?.format('dd.MM.yy')) : '')
                            + '-'
                            + (it[3] ? (it[3]?.format('dd.MM.yy')) : '')
                            + ((it[2] || it[3]) ? ') ' : ' ')
            )
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        if (isWithInstanceOf) {
            validSubscriptionsDropdown.sort { it.optionValue.toLowerCase() }
        }
        validSubscriptionsDropdown
    }

    private List<Map> getLicensesDropdown(Org contextOrg, boolean isWithInstanceOf) {
        List<License> validLicensesOhneInstanceOf = []
        List<License> validLicensesMitInstanceOf = []
        List<Map> validLicensesDropdown = []

        if (contextOrg) {
            String licensesQueryMitInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate, licinstanceof.type from License lic left join lic.orgLinks o left join lic.instanceOf licinstanceof WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is not null order by lic.sortableReference asc'

            String licensesQueryOhneInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate from License lic left join lic.orgLinks o WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is null order by lic.sortableReference asc'

            if(accessService.checkPerm("ORG_CONSORTIUM")){
                Map<String, Object> qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE.id,
                            RDStore.OR_LICENSING_CONSORTIUM.id
                    ]
                ]
                validLicensesOhneInstanceOf = License.executeQuery(licensesQueryOhneInstanceOf, qry_params_for_lic)
                if (isWithInstanceOf) {
                    validLicensesMitInstanceOf = License.executeQuery(licensesQueryMitInstanceOf, qry_params_for_lic)
                }

            }
            else if (accessService.checkPerm("ORG_INST")) {
                Map<String, Object> qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE.id,
                            RDStore.OR_LICENSEE_CONS.id,
                            RDStore.OR_LICENSEE_COLL.id
                    ]
                ]
                validLicensesOhneInstanceOf = License.executeQuery(licensesQueryOhneInstanceOf, qry_params_for_lic)
                if (isWithInstanceOf) {
                    validLicensesMitInstanceOf = License.executeQuery(licensesQueryMitInstanceOf, qry_params_for_lic)
                }

            }
            else {
                validLicensesOhneInstanceOf = []
                validLicensesMitInstanceOf = []
            }
        }

        String member = ' - ' +messageSource.getMessage('license.member', null, LocaleContextHolder.getLocale())
        validLicensesDropdown = validLicensesMitInstanceOf?.collect{

            def optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? it[3]?.format('dd.MM.yy') : '') + ('-') + (it[4] ? it[4]?.format('dd.MM.yy') : '') + ')'
            boolean isLicensingConsortium = 'Licensing Consortium' == it[5]?.value

            if (isLicensingConsortium) {
                optionValue += member
            }
            return [optionKey: optionKey, optionValue: optionValue]
        }
        validLicensesOhneInstanceOf?.collect{

            Long optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? it[3]?.format('dd.MM.yy') : '') + ('-') + (it[4] ? it[4]?.format('dd.MM.yy') : '') + ')'
            validLicensesDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        if (isWithInstanceOf) {
            validLicensesDropdown.sort { it.optionValue.toLowerCase() }
        }
        validLicensesDropdown
    }

    Map<String, Object> getPreconditionsWithoutTargets(Org contextOrg) {
        Map<String, Object> result = [:]
        def validResponsibleUsers   = contextOrg ? User.executeQuery(
                "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3)) order by lower(u.display)",
                [contextOrg]) : []
        result.taskCreator          = springSecurityService.getCurrentUser()
        result.validResponsibleUsers = validResponsibleUsers
        result
    }

    private String addDefaultOrder(String tableAlias, String query){
        if (query && ( ! query.toLowerCase().contains('order by'))){
            if (tableAlias) {
                query += ' order by '+tableAlias+'.endDate asc'
            } else {
                query += ' order by endDate asc'
            }
        }
        query
    }

    private Map addDefaultOrder(String tableAlias, Map params){
        if (params) {
            if (tableAlias){
                if ( ! params.sort) {
                    params << [sort: tableAlias+'.endDate', order: 'asc']
                }
            } else {
                if ( ! params.sort) {
                    params << [sort: 'endDate', order: 'asc']
                }
            }
        } else {
            if (tableAlias) {
                params = [sort: tableAlias+'.endDate', order: 'asc']
            } else {
                params = [sort: 'endDate', order: 'asc']
            }
        }
        params
    }
}
