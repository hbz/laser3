package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import grails.transaction.Transactional
import static com.k_int.kbplus.MyInstitutionController.INSTITUTIONAL_LICENSES_QUERY
import static com.k_int.kbplus.MyInstitutionController.INSTITUTIONAL_SUBSCRIPTION_QUERY

@Transactional
class TaskService {

    final static WITHOUT_TENANT_ONLY = "WITHOUT_TENANT_ONLY"

    def springSecurityService
    def accessService

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
        def result = [:]
        def responsibleUsersQuery   = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3))"
        def validResponsibleOrgs    = contextOrg ? [contextOrg] : []
        def validResponsibleUsers   = contextOrg ? User.executeQuery(responsibleUsersQuery, [contextOrg]) : []

        //TODO: MAX und OFFSET anders festlegen
        Map maxOffset = [max: 1000, offset: 0]
        if (contextOrg) {
            if(accessService.checkPerm("ORG_CONSORTIUM") || accessService.checkPerm("ORG_CONSORTIUM_SURVEY")){
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE,
                            RDStore.OR_LICENSING_CONSORTIUM
                    ]
                ]
                result.validLicenses = License.executeQuery('select l ' + INSTITUTIONAL_LICENSES_QUERY +' order by l.sortableReference asc', qry_params_for_lic)//, maxOffset)

            } else if (accessService.checkPerm("ORG_INST")) {
                def qry_params_for_lic = [
                    lic_org:    contextOrg,
                    org_roles:  [
                            RDStore.OR_LICENSEE,
                            RDStore.OR_LICENSEE_CONS,
                            RDStore.OR_LICENSEE_COLL
                    ]
                ]
                result.validLicenses = License.executeQuery('select l ' + INSTITUTIONAL_LICENSES_QUERY +' order by l.sortableReference asc', qry_params_for_lic)//, maxOffset)

            } else {
                result.validLicenses = []
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
            result.validSubscriptions = Subscription.executeQuery("select s " + INSTITUTIONAL_SUBSCRIPTION_QUERY + ' order by s.name asc', qry_params_for_sub)//,  maxOffset)
        }
        else { // TODO: admin and datamanager without contextOrg possible ?
            result.validLicenses = License.list()
            result.validSubscriptions = Subscription.list()
        }

        result.validOrgs            = Org.list() // TODO
        result.validPackages        = Package.findAll("from Package p where p.name != '' and p.name != null order by p.sortName asc") // TODO

        result.taskCreator          = springSecurityService.getCurrentUser()
        result.validResponsibleOrgs = validResponsibleOrgs
        result.validResponsibleUsers = validResponsibleUsers

        result
    }
}
