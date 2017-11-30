package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.MyInstitutionsController
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.Task
import com.k_int.kbplus.auth.User
import grails.transaction.Transactional

@Transactional
class TaskService {

    final static WITHOUT_TENANT_ONLY = "WITHOUT_TENANT_ONLY"

    def springSecurityService

    def getTasksByCreator(User user, flag) {
        def tasks = []
        if (user) {
            if (flag == WITHOUT_TENANT_ONLY) {
                tasks = Task.findAllByCreatorAndResponsibleOrgAndResponsibleUser(user, null, null)
            }
            else {
                tasks = Task.findAllByCreator(user)
            }
        }
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsible(User user) {
        def tasks = []
        if (user) {
            tasks = Task.findAllByResponsibleUser(user)
        }
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsible(Org org) {
        def tasks = []
        if (org) {
            tasks = Task.findAllByResponsibleOrg(org)
        }
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsibles(User user, Org org) {
        def tasks = []
        def a = getTasksByResponsible(user)
        def b = getTasksByResponsible(org)

        tasks = a.plus(b).unique()
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsibleAndObject(User user, Object obj) {
        def tasks = []
        if (user && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByResponsibleUserAndLicense(user, obj)
                    break
                case 'Org':
                    tasks = Task.findAllByResponsibleUserAndOrg(user, obj)
                    break
                case 'Package':
                    tasks = Task.findAllByResponsibleUserAndPkg(user, obj)
                    break
                case 'Subscription':
                    tasks = Task.findAllByResponsibleUserAndSubscription(user, obj)
                    break
            }
        }
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsibleAndObject(Org org, Object obj) {
        def tasks = []
        if (org && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByResponsibleOrgAndLicense(org, obj)
                    break
                case 'Org':
                    tasks = Task.findAllByResponsibleOrgAndOrg(org, obj)
                    break
                case 'Package':
                    tasks = Task.findAllByResponsibleOrgAndPkg(org, obj)
                    break
                case 'Subscription':
                    tasks = Task.findAllByResponsibleOrgAndSubscription(org, obj)
                    break
            }
        }
        tasks.sort{ it.endDate }
    }

    def getTasksByResponsiblesAndObject(User user, Org org, Object obj) {
        def tasks = []
        def a = getTasksByResponsibleAndObject(user, obj)
        def b = getTasksByResponsibleAndObject(org, obj)

        tasks = a.plus(b).unique()
        tasks.sort{ it.endDate }
    }

    def getPreconditions(Org contextOrg) {
        def result = [:]

        def qry_params1 = [
            lic_org:    contextOrg,
            org_role:   RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee'),
            lic_status: RefdataCategory.lookupOrCreate('License Status', 'Deleted')
        ]
        def qry_params2 = [
            'roleTypes' : [
                RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber'),
                RefdataCategory.lookupOrCreate('Organisational Role', 'Subscription Consortia')
            ],
            'activeInst': contextOrg
        ]

        def responsibleUsersQuery   = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3))"
        def validResponsibleOrgs    = [contextOrg]
        def validResponsibleUsers   = User.executeQuery(responsibleUsersQuery, [contextOrg])

        result.validLicenses        = License.executeQuery('select l ' + MyInstitutionsController.INSTITUTIONAL_LICENSES_QUERY, qry_params1, [max: 100, offset: 0])
        result.validOrgs            = Org.list()
        result.validPackages        = Package.list() // TODO
        result.validSubscriptions   = Subscription.executeQuery("select s " + MyInstitutionsController.INSTITUTIONAL_SUBSCRIPTION_QUERY, qry_params2,  [max: 100, offset: 0])

        result.taskCreator          = springSecurityService.getCurrentUser()
        result.validResponsibleOrgs = validResponsibleOrgs
        result.validResponsibleUsers = validResponsibleUsers

        result
    }
}
