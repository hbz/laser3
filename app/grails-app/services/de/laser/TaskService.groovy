package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
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
        def responsibleUsersQuery   = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3)) order by lower(u.display)"
        def validResponsibleOrgs    = contextOrg ? [contextOrg] : []
        def validResponsibleUsers   = contextOrg ? User.executeQuery(responsibleUsersQuery, [contextOrg]) : []

        if (contextOrg) {
            boolean isInstitution = (RDStore.OT_INSTITUTION == contextOrg.getCustomerType())
                // Anbieter und Lieferanten
            def params       = [:]
            params.sort      = isInstitution ? " LOWER(o.name), LOWER(o.shortname)" : " LOWER(o.sortname), LOWER(o.name)"
            def fsq          = filterService.getOrgQuery(params)
            result.validOrgsDropdown = Org.executeQuery('select o.id, o.name, o.shortname, o.sortname from Org o where (o.status is null or o.status != :orgStatus) order by  LOWER(o.sortname), LOWER(o.name) asc', fsq.queryParams)

            String licensesQuery = 'select lic.id, lic.reference, lic.status, lic.startDate, lic.endDate from License lic where exists ( select o.id from OrgRole o where o.lic = lic.id AND o.org = :lic_org and o.roleType IN (:org_roles) ) order by lic.sortableReference asc'
            if(accessService.checkPerm("ORG_CONSORTIUM") || accessService.checkPerm("ORG_CONSORTIUM_SURVEY")){
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE,
                            RDStore.OR_LICENSING_CONSORTIUM
                    ]
                ]
                result.validLicensesDropdown = License.executeQuery(licensesQuery, qry_params_for_lic)//, maxOffset)

            } else if (accessService.checkPerm("ORG_INST")) {
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE,
                            RDStore.OR_LICENSEE_CONS,
                            RDStore.OR_LICENSEE_COLL
                    ]
                ]
                result.validLicensesDropdown = License.executeQuery(licensesQuery, qry_params_for_lic)

            } else {
                result.validLicenses = []
            }
            String comboQuery = 'select o.id, o.name, o.shortname, o.sortname, c.id from Org o, Combo c join org o on c.fromOrg = o.org_id where c.toOrg = :toOrg and c.type = :type order by '+params.sort
            if (contextOrg.orgType == RDStore.OT_CONSORTIUM){
                result.validOrgsDropdown << Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_CONSORTIUM])
//                result.validOrgs.unique().sort{it.sortname.toLowerCase() + it.name}
            } else if (contextOrg.orgType == RDStore.OT_INSTITUTION){
                result.validOrgsDropdown << Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_DEPARTMENT])
//                result.validOrgs.unique().sort{it.name.toLowerCase() + it.shortname}
            }

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

            result.validSubscriptionDropdown = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, i10."+i10value+", s.instanceOf from Subscription s,  I10nTranslation i10 where s.status.id = i10.referenceId and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) )  and i10.referenceField=:referenceField order by lower(s.name), s.endDate", qry_params_for_sub << [referenceField: 'value'])


        } else { // TODO: admin and datamanager without contextOrg possible ?
            result.validLicenses      = License.list()
            result.validSubscriptions = Subscription.list()
//            result.validOrgs          = Org.list().sort(it.name+it.shortname)
        }

        result.validPackages        = Package.findAll("from Package p where p.name != '' and p.name != null order by lower(p.sortName) asc") // TODO

        result.taskCreator          = springSecurityService.getCurrentUser()
        result.validResponsibleOrgs = validResponsibleOrgs
        result.validResponsibleUsers = validResponsibleUsers
        long ende = System.currentTimeMillis()
        long dauer = ende-start
        print "Dauer in Millis: " + dauer
        result
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
