package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.OrgRole
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import static com.k_int.kbplus.MyInstitutionController.INSTITUTIONAL_LICENSES_QUERY
import static com.k_int.kbplus.MyInstitutionController.INSTITUTIONAL_SUBSCRIPTION_QUERY

@Transactional
class TaskService {

    final static WITHOUT_TENANT_ONLY = "WITHOUT_TENANT_ONLY"

    def springSecurityService
    def accessService
    def filterService
    def messageSource

    def getTasksByCreator(User user, Map queryMap, flag) {
        def tasks = []
        if (user) {
            def query
            if (flag == WITHOUT_TENANT_ONLY) {
                query = "select t from Task t where t.creator=:user and t.responsibleUser is null and t.responsibleOrg is null "
            } else {
                query = "select t from Task t where t.creator=:user "
            }
            def params = [user : user]
            if (queryMap){
                query += queryMap.query
                params << queryMap.queryParams
            }
            tasks = Task.executeQuery(query, params)
        }
        if ( ! queryMap || ! queryMap?.query?.toLowerCase()?.contains('order by')){
            tasks.sort{ it.endDate }
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
            def query  = "select t from Task t where t.responsibleUser = :user" + queryMap.query
            def params = [user : user] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    def getTasksByResponsible(Org org, Map queryMap) {
        def tasks = []
        if (org) {
            def query  = "select t from Task t where t.responsibleOrg = :org" + queryMap.query
            def params = [org : org] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    def getTasksByResponsibles(User user, Org org, Map queryMap) {
        def tasks = []

        if (user && org) {
            def query = "select t from Task t where ( t.responsibleUser = :user or t.responsibleOrg = :org ) " + queryMap.query
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
        def tasks = getTasksByResponsibleAndObject(user, obj, [])
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsibleAndObject(Org org, Object obj) {
        def tasks = getTasksByResponsibleAndObject(org, obj, [])
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsiblesAndObject(User user, Org org, Object obj) {
        def tasks = []
        def a = getTasksByResponsibleAndObject(user, obj)
        def b = getTasksByResponsibleAndObject(org, obj)

        tasks = a.plus(b).unique()
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsibleAndObject(User user, Object obj,  Object params) {
        def tasks = []
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

    def getPreconditions(Org contextOrg) {
        long start = System.currentTimeMillis()
        def result = [:]

        result.taskCreator                  = springSecurityService.getCurrentUser()
        result.validResponsibleOrgs         = contextOrg ? [contextOrg] : []
        result.validResponsibleUsers        = getUserDropdown(contextOrg)
        result.validPackages                = getPackagesDropdown(contextOrg)
        result.validOrgsDropdown            = getOrgsDropdown(contextOrg)
//        result.validSubscriptionsDropdown   = getSubscriptionsDropdown(contextOrg)
        result.validSubscriptionsDropdown   = getSubscriptionsDropdown_unfinished(contextOrg)
        result.validLicensesDropdown        = getLicensesDropdown(contextOrg)

        long ende = System.currentTimeMillis()
        long dauer = ende-start
        print "Dauer in Millis: " + dauer
        result
    }

    def getPackagesDropdown(Org contextOrg) {
        def validPackages        = Package.findAll("from Package p where p.name != '' and p.name != null order by lower(p.sortName) asc") // TODO
        validPackages
    }

    def getUserDropdown(Org contextOrg) {
        def responsibleUsersQuery   = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3)) order by lower(u.display)"
        def validResponsibleUsers   = contextOrg ? User.executeQuery(responsibleUsersQuery, [contextOrg]) : []

        validResponsibleUsers
    }
    def getOrgsDropdown(Org contextOrg) {
        List validOrgsDropdown = []
        if (contextOrg) {
            boolean isInstitution = (RDStore.OT_INSTITUTION == contextOrg.getCustomerType())
                // Anbieter und Lieferanten
            def params       = [:]
            params.sort      = isInstitution ? " LOWER(o.name), LOWER(o.shortname)" : " LOWER(o.sortname), LOWER(o.name)"
            def fsq          = filterService.getOrgQuery(params)
            validOrgsDropdown = Org.executeQuery('select o.id, o.name, o.shortname, o.sortname from Org o where (o.status is null or o.status != :orgStatus) order by  LOWER(o.sortname), LOWER(o.name) asc', fsq.queryParams)

            String comboQuery = 'select o.id, o.name, o.shortname, o.sortname, c.id from Org o, Combo c join org o on c.fromOrg = o.org_id where c.toOrg = :toOrg and c.type = :type order by '+params.sort
            if (contextOrg.orgType == RDStore.OT_CONSORTIUM){
                validOrgsDropdown << Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_CONSORTIUM])
//                result.validOrgs.unique().sort{it.sortname.toLowerCase() + it.name}
            } else if (contextOrg.orgType == RDStore.OT_INSTITUTION){
                validOrgsDropdown << Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_DEPARTMENT])
//                result.validOrgs.unique().sort{it.name.toLowerCase() + it.shortname}
            }
        }
        validOrgsDropdown
    }
    List<Map> getSubscriptionsDropdown(Org contextOrg) {
        List validSubscriptions = []
        List<Map> validSubscriptionsDropdown = []
        if (contextOrg) {
            def qry_params_for_sub = [
                    'roleTypes' : [
                            RDStore.OR_SUBSCRIBER,
                            RDStore.OR_SUBSCRIBER_CONS,
                            RDStore.OR_SUBSCRIPTION_CONSORTIA
                    ],
                    'activeInst': contextOrg
            ]
            validSubscriptions = Subscription.executeQuery("select s " + INSTITUTIONAL_SUBSCRIPTION_QUERY + ' order by s.name asc', qry_params_for_sub)
        }
        validSubscriptions.each { Subscription sub ->
            validSubscriptionsDropdown << [optionKey: sub.id, optionValue: sub.dropdownNamingConvention()]
        }

        validSubscriptionsDropdown
    }

    List<Map> getSubscriptionsDropdown_unfinished(Org contextOrg) {
        List validSubscriptionsMitInstanceOf = []
        List validSubscriptionsOhneInstanceOf = []
        List<Map> validSubscriptionsDropdown = []
        boolean binKonsortium = contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']

        if (contextOrg) {
            if (binKonsortium) {

            def qry_params_for_sub_MitInstanceOf = [
                'roleTypes' : [
                        RDStore.OR_SUBSCRIBER,
                        RDStore.OR_SUBSCRIBER_CONS,
                        RDStore.OR_SUBSCRIPTION_CONSORTIA,
                        RDStore.OR_SUBSCRIPTION_COLLECTIVE
                ],
                'roleTypes1' : [
                        RDStore.OR_SUBSCRIBER,
                        RDStore.OR_SUBSCRIBER_CONS,
                        RDStore.OR_SUBSCRIBER_CONS_HIDDEN,
                        RDStore.OR_SUBSCRIBER_COLLECTIVE
                ],
                'OR_SUBSCRIPTION_CONSORTIA' : RDStore.OR_SUBSCRIPTION_CONSORTIA,
                'OR_SUBSCRIPTION_COLLECTIVE' : RDStore.OR_SUBSCRIPTION_COLLECTIVE,
                'activeInst': contextOrg
            ]

            def qry_params_for_sub = [
                'roleTypes' : [
                        RDStore.OR_SUBSCRIBER,
                        RDStore.OR_SUBSCRIBER_CONS,
                        RDStore.OR_SUBSCRIPTION_CONSORTIA,
                        RDStore.OR_SUBSCRIPTION_COLLECTIVE
                ],
                'activeInst': contextOrg
            ]
            String i10value = LocaleContextHolder.getLocale().getLanguage()== Locale.GERMAN.getLanguage() ? 'valueDe' : 'valueEn'

            //Kindlizenzen
            validSubscriptionsMitInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, i10."+i10value+", orel1.org, orel2.org, orel3.org from Subscription s, I10nTranslation i10 left join s.orgRelations as orel left join s.orgRelations as orel1 left join s.orgRelations as orel2 left join s.orgRelations as orel3 where s.status.id = i10.referenceId and orel.roleType IN (:roleTypes) AND (orel1.roleType is null or orel1.roleType IN (:roleTypes1)) AND (orel2.roleType is null or orel2.roleType = :OR_SUBSCRIPTION_CONSORTIA) AND (orel3.roleType is null or orel3.roleType = :OR_SUBSCRIPTION_COLLECTIVE)  AND orel.org = :activeInst and s.instanceOf is not null and i10.referenceField=:referenceField order by lower(s.name), s.endDate", qry_params_for_sub_MitInstanceOf << [referenceField: 'value'])

//            validSubscriptionsMitInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, i10."+i10value+" from Subscription s,  I10nTranslation i10 where s.status.id = i10.referenceId and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) and s.instanceOf is not null and i10.referenceField=:referenceField order by lower(s.name), s.endDate", qry_params_for_sub << [referenceField: 'value'])

            validSubscriptionsOhneInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, i10."+i10value+" from Subscription s, I10nTranslation i10 where s.status.id = i10.referenceId and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) and s.instanceOf is null and i10.referenceField=:referenceField order by lower(s.name), s.endDate", qry_params_for_sub << [referenceField: 'value'])
            }
        }

        String NO_STATUS = RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')
        validSubscriptionsMitInstanceOf?.each{

            Long optionKey = it[0]
            String optionValue = (it[1]
                    + ' - '
                    + (it[4]?: NO_STATUS)
                    + ((it[2]||it[3]) ? ' (' : ' ')
                    + (it[2] ? (it[2]?.format('dd.MM.yy')) : '')
                    +  '-'
                    + (it[3] ? (it[3]?.format('dd.MM.yy')) : '')
                    + ((it[2]||it[3]) ? ') ' : ' ')
            )
            if (binKonsortium){
                optionValue += ' - (1)' + it[5] + ' (2)' + it[6] + ' (3)' + it[7]
//                optionValue += com.k_int.kbplus.Subscription.get(it[0]).getSubscriber().sortname
            } else {
                optionValue += ' - Konsortiallizenz'
            }
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        validSubscriptionsOhneInstanceOf?.each{

            Long optionKey = it[0]
            String optionValue = (it[1]
                    + ' - '
                    + (it[4]?: NO_STATUS)
                    + ((it[2]||it[3]) ? ' (' : ' ')
                    + (it[2] ? (it[2]?.format('dd.MM.yy')) : '')
                    +  '-'
                    + (it[3] ? (it[3]?.format('dd.MM.yy')) : '')
                    + ((it[2]||it[3]) ? ') ' : ' ')
            )
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        validSubscriptionsDropdown.sort{it.optionValue.toLowerCase()}
    }

    List<Map> getLicensesDropdown(Org contextOrg) {
        List validLicensesOhneInstanceOf = []
        List validLicensesMitInstanceOf = []
        List<Map> validLicensesDropdown = []

        if (contextOrg) {
            String licensesQueryMitInstanceOf = 'SELECT lic.id, lic.reference, lic.status, lic.startDate, lic.endDate, o.roleType, licinstanceof.type from License lic left join lic.orgLinks o left join lic.instanceOf licinstanceof WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is not null order by lic.sortableReference asc'

            String licensesQueryOhneInstanceOf = 'SELECT lic.id, lic.reference, lic.status, lic.startDate, lic.endDate, o.roleType from License lic left join lic.orgLinks o WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is null order by lic.sortableReference asc'

            if(accessService.checkPerm("ORG_CONSORTIUM") || accessService.checkPerm("ORG_CONSORTIUM_SURVEY")){
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE?.id,
                            RDStore.OR_LICENSING_CONSORTIUM?.id
                    ]
                ]
                validLicensesOhneInstanceOf = License.executeQuery(licensesQueryOhneInstanceOf, qry_params_for_lic)
                validLicensesMitInstanceOf = License.executeQuery(licensesQueryMitInstanceOf, qry_params_for_lic)

            } else if (accessService.checkPerm("ORG_INST")) {
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE,
                            RDStore.OR_LICENSEE_CONS,
                            RDStore.OR_LICENSEE_COLL
                    ]
                ]
                validLicensesOhneInstanceOf = License.executeQuery(licensesQueryOhneInstanceOf, qry_params_for_lic)
                validLicensesMitInstanceOf = License.executeQuery(licensesQueryMitInstanceOf, qry_params_for_lic)

            } else {
                validLicensesOhneInstanceOf = []
                validLicensesMitInstanceOf = []
            }
        }

        String member = ' - ' +messageSource.getMessage('license.member', null, LocaleContextHolder.getLocale())
        validLicensesDropdown = validLicensesMitInstanceOf?.collect{

            def optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? it[3]?.format('dd.MM.yy') : '') + ('-') + (it[4] ? it[4]?.format('dd.MM.yy') : '') + ')'
            boolean isLicensingConsortium = 'Licensing Consortium' == it[5]?.value
            boolean hasTemplate2 = (it[6] != null) && (it[6] == RDStore.LICENSE_TYPE_TEMPLATE)
            if (isLicensingConsortium && ! hasTemplate2) {
                optionValue += member
            }
            return [optionKey: optionKey, optionValue: optionValue]
        }
        validLicensesOhneInstanceOf?.collect{

            Long optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? it[3]?.format('dd.MM.yy') : '') + ('-') + (it[4] ? it[4]?.format('dd.MM.yy') : '') + ')'
            validLicensesDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        validLicensesDropdown.sort{it.optionValue.toLowerCase()}
    }

    def getPreconditionsWithoutTargets(Org contextOrg) {
        def result = [:]
        def responsibleUsersQuery   = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3)) order by lower(u.display)"
        def validResponsibleUsers   = contextOrg ? User.executeQuery(responsibleUsersQuery, [contextOrg]) : []
        result.taskCreator          = springSecurityService.getCurrentUser()
        result.validResponsibleUsers = validResponsibleUsers
        result
    }
}
