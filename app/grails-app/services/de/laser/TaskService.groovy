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

    def getTasksByOwner(User user, flag) {
        def tasks = []
        if (user) {
            if (flag == WITHOUT_TENANT_ONLY) {
                tasks = Task.findAllByOwnerAndTenantOrgAndTenantUser(user, null, null)
            }
            else {
                tasks = Task.findAllByOwner(user)
            }
        }
        tasks
    }

    def getTasksByTenant(User user) {
        def tasks = []
        if (user) {
            tasks = Task.findAllByTenantUser(user)
        }
        tasks
    }

    def getTasksByTenant(Org org) {
        def tasks = []
        if (org) {
            tasks = Task.findAllByTenantOrg(org)
        }
        tasks
    }

    def getTasksByTenants(User user, Org org) {
        def tasks = []
        def a = getTasksByTenant(user)
        def b = getTasksByTenant(org)

        tasks = a.plus(b).unique()
        tasks
    }

    def getTasksByTenantAndObject(User user, Object obj) {
        def tasks = []
        if (user && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByTenantUserAndLicense(user, obj)
                    break
                case 'Org':
                    tasks = Task.findAllByTenantUserAndOrg(user, obj)
                    break
                case 'Package':
                    tasks = Task.findAllByTenantUserAndPkg(user, obj)
                    break
                case 'Subscription':
                    tasks = Task.findAllByTenantUserAndSubscription(user, obj)
                    break
            }
        }
        tasks
    }

    def getTasksByTenantAndObject(Org org, Object obj) {
        def tasks = []
        if (org && obj) {
            switch (obj.getClass().getSimpleName()) {
                case 'License':
                    tasks = Task.findAllByTenantOrgAndLicense(org, obj)
                    break
                case 'Org':
                    tasks = Task.findAllByTenantOrgAndOrg(org, obj)
                    break
                case 'Package':
                    tasks = Task.findAllByTenantOrgAndPkg(org, obj)
                    break
                case 'Subscription':
                    tasks = Task.findAllByTenantOrgAndSubscription(org, obj)
                    break
            }
        }
        tasks
    }

    def getTasksByTenantsAndObject(User user, Org org, Object obj) {
        def tasks = []
        def a = getTasksByTenantAndObject(user, obj)
        def b = getTasksByTenantAndObject(org, obj)

        tasks = a.plus(b).unique()
        tasks
    }

    def getPreconditions(Org contextOrg) {
        def result = [:]

        def qry_params = [
                lic_org:    contextOrg,
                org_role:   RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee'),
                lic_status: RefdataCategory.lookupOrCreate('License Status', 'Deleted')
        ]

        def validLicenses 	        = License.executeQuery('select l ' + MyInstitutionsController.INSTITUTIONAL_LICENSES_QUERY, qry_params,  [max: 100, offset: 0])
        def validOrgs               = Org.list()
        def validPackages           = Package.list()        // TODO
        def validSubscriptions 	    = Subscription.list()   // TODO

        def tenantUsersQuery        = "select u from User as u where exists (select uo from UserOrg as uo where uo.user = u and uo.org = ? and (uo.status=1 or uo.status=3))"

        def validTenantOrgs         = [contextOrg]
        def validTenantUsers 	    = User.executeQuery(tenantUsersQuery, [contextOrg])

        result.validLicenses        = validLicenses
        result.validOrgs            = validOrgs
        result.validPackages        = validPackages
        result.validSubscriptions   = validSubscriptions

        result.taskOwner            = springSecurityService.getCurrentUser()
        result.validTenantOrgs      = validTenantOrgs
        result.validTenantUsers     = validTenantUsers

        result
    }
}
