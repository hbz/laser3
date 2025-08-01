package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

/**
 * This service retrieves data for task retrieval and creation
 */
@Transactional
class TaskService {

    ContextService contextService
    MessageSource messageSource

    /**
     * Loads the user's tasks for the given object
     * @param user the user whose tasks should be retrieved
     * @param object the object to which the tasks are attached
     * @return a list of accessible tasks
     */
    Map<String, Object> getTasks(User user, Object object) {
        Map<String, Object> result = [:]
        result.taskInstanceList   = getTasksByResponsibilityAndObject(user, object)
        result.myTaskInstanceList = getTasksByCreatorAndObject(user,  object)
        result.cmbTaskInstanceList = (result.taskInstanceList + result.myTaskInstanceList).unique()
        //println result
        result
    }

    /**
     * Loads the user's tasks for the given object; the output is for a PDF export
     * @param user the user whose tasks should be retrieved
     * @param object the object to which the tasks are attached
     * @return a list of accessible tasks
     */
    Set<Task> getTasksForExport(User user, Object object) {
        Set<Task> result = []
        result.addAll(getTasksByResponsibilityAndObject(user, object))
        result.addAll(getTasksByCreatorAndObject(user,  object))
        result
    }

    /**
     * Gets the tasks for the given object
     * @param obj the object whose tasks should be retrieved
     * @return a list of tasks
     */
    List<Task> getTasksByObject(Object obj) {
        List<Task> tasks = []
        Map pparams = [order: "endDate"]

        switch (obj.getClass().getSimpleName()) {
            case 'License':
                tasks.addAll(Task.findAllByLicense(obj as License, pparams))
                break
            case 'Org':
                tasks.addAll(Task.findAllByOrg(obj as Org, pparams))
                break
            case 'Provider':
                tasks.addAll(Task.findAllByProvider(obj as Provider, pparams))
                break
            case 'Subscription':
                tasks.addAll(Task.findAllBySubscription(obj as Subscription, pparams))
                break
            case 'SurveyConfig':
                tasks.addAll(Task.findAllBySurveyConfig(obj as SurveyConfig, pparams))
                break
            case 'TitleInstancePackagePlatform':
                tasks.addAll(Task.findAllByTipp(obj as TitleInstancePackagePlatform, pparams))
                break
            case 'Vendor':
                tasks.addAll(Task.findAllByVendor(obj as Vendor, pparams))
                break
        }
        tasks
    }

    /**
     * Gets the tasks which the given user has created
     * @param user the user whose tasks should be retrieved
     * @param queryMap an eventual filter restricting the output
     * @param flag should only tasks without tenant appear?
     * @return a list of tasks matching the given criteria
     */
    List<Task> getTasksByCreator(User user, Map queryMap) {
        List<Task> tasks = []
        if (user) {
            String query = 'select t from Task t where t.creator = :user'

            Map<String, Object> params = [user : user]
            if (queryMap){
                query += queryMap.query
                query = _addDefaultOrder("t", query)
                params << queryMap.queryParams
            }
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    /**
     * Retrieves all tasks the given user created for the given license
     * @param user the user whose tasks should be retrieved
     * @param obj the license to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, License obj) {
        (user && obj) ? Task.findAllByCreatorAndLicense(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given organisation
     * @param user the user whose tasks should be retrieved
     * @param obj the organisation to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Org obj) {
        (user && obj) ? Task.findAllByCreatorAndOrg(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given organisation
     * @param user the user whose tasks should be retrieved
     * @param obj the organisation to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Provider obj) {
        (user && obj) ? Task.findAllByCreatorAndProvider(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given subscription
     * @param user the user whose tasks should be retrieved
     * @param obj the subscription to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Subscription obj) {
        (user && obj) ? Task.findAllByCreatorAndSubscription(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given survey
     * @param user the user whose tasks should be retrieved
     * @param obj the survey to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, SurveyConfig obj) {
        (user && obj) ? Task.findAllByCreatorAndSurveyConfig(user, obj) : []
    }

    List<Task> getTasksByCreatorAndObject(User user, TitleInstancePackagePlatform obj) {
        (user && obj) ? Task.findAllByCreatorAndTipp(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given organisation
     * @param user the user whose tasks should be retrieved
     * @param obj the organisation to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Vendor obj) {
        (user && obj) ?  Task.findAllByCreatorAndVendor(user, obj) : []
    }

    /**
     * Gets the tasks for which the given user or institution is responsible, restricted by an optional query
     * @param user the user responsible for those tasks which should be retrieved
     * @param org the institution responsible for those tasks which should be retrieved
     * @param queryMap eventual filter parameters
     * @return a (filtered) list of tasks
     */
    List<Task> getTasksByResponsibility(User user, Map queryMap) {
        List<Task> tasks = []

        if (user) {
            String query = 't.responsibleUser = :user'
            Map params   = [user: user]

            if (user.formalOrg) {
                query  = '( t.responsibleUser = :user or t.responsibleOrg = :org )'
                params = [user: user, org: user.formalOrg]
            }

            query = 'select distinct(t) from Task t where ' + query + ' ' + queryMap.query
            query = _addDefaultOrder("t", query)

            params = params << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    /**
     * Gets the tasks for the given object for which the given user or institution is responsible
     * @param user the user responsible for those tasks which should be retrieved
     * @param org the institution responsible for those tasks which should be retrieved
     * @param obj the object to which the tasks are related
     * @return a list of tasks
     */
    List<Task> getTasksByResponsibilityAndObject(User user, Object obj) {
        List<Task> tasks = []
        String tableName = ''
        if (user && obj) {
            switch (GrailsHibernateUtil.unwrapIfProxy(obj).getClass().getSimpleName()) {
                case 'License':
                    tableName = 'license'
                    break
                case 'Org':
                    tableName = 'org'
                    break
                case 'Provider':
                    tableName = 'provider'
                    break
                case 'Subscription':
                    tableName = 'subscription'
                    break
                case 'SurveyConfig':
                    tableName = 'surveyConfig'
                    break
                case 'TitleInstancePackagePlatform':
                    tableName = 'tipp'
                    break
                case 'Vendor':
                    tableName = 'vendor'
                    break
            }

            String query = "select distinct(t) from Task t where ${tableName}=:obj and (responsibleUser=:user or responsibleOrg=:org) order by endDate"
            tasks = Task.executeQuery( query, [user: user, org: user.formalOrg, obj: obj] )
        }
        tasks
    }

    /**
     * Gets the possible selection values for the current institution to create a new task
     * @return a map containing prefilled lists for dropdowns
     */
    Map<String, Object> getPreconditions() {
        Map<String, Object> result = [:]
        Org contextOrg = contextService.getOrg()

        result.validResponsibleOrgs         = contextOrg ? [contextOrg] : []
        result.validResponsibleUsers        = getUserDropdown()
        result.validOrgsDropdown            = _getOrgsDropdown()
        result.validProvidersDropdown       = _getProvidersDropdown()
        result.validVendorsDropdown         = _getVendorsDropdown()
        result.validSubscriptionsDropdown   = _getSubscriptionsDropdown(false)
        result.validLicensesDropdown        = _getLicensesDropdown(false)

        result
    }

    /**
     * Gets a list of all users for dropdown output
     * @return a list of users
     */
    List<User> getUserDropdown() { // modal_create
        User.executeQuery( 'select u from User as u where u.formalOrg = :org order by lower(u.display)', [org: contextService.getOrg()])
    }

    /**
     * Gets a list of all institutions for dropdown output
     * @return a list of organisation
     */
    private Set<Map> _getOrgsDropdown() {
        Set<Map> validOrgs = [], validOrgsDropdown = []

        boolean isInstitution = contextService.getOrg().isCustomerType_Inst()
        boolean isConsortium  = contextService.getOrg().isCustomerType_Consortium()

            GrailsParameterMap params = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
            params.sort      = " LOWER(o.sortname), LOWER(o.name)"
            //def fsq          = filterService.getOrgQuery(params)
            //validOrgs = Org.executeQuery('select o.id, o.name, o.sortname from Org o order by LOWER(o.sortname), LOWER(o.name) asc', fsq.queryParams)

            if (isConsortium) {
                String comboQuery = 'select new map(o.id as id, o.name as name, o.sortname as sortname) from Org o join o.outgoingCombos c where c.toOrg = :toOrg and c.type = :type order by '+params.sort
                validOrgs = Combo.executeQuery(comboQuery,
                        [toOrg: contextService.getOrg(),
                        type:  RDStore.COMBO_TYPE_CONSORTIUM])
            }
            else if (isInstitution) {
                String consQuery = 'select new map(o.id as id, o.name as name, o.sortname as sortname) from OrgSetting os join os.org o where os.key = :customerType and os.roleValue in (:consortium) order by '+params.sort
                validOrgs.addAll(Org.executeQuery(consQuery, [customerType: OrgSetting.KEYS.CUSTOMER_TYPE, consortium: Role.findAllByAuthorityInList([CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_CONSORTIUM_BASIC])]))
            }
            validOrgs = validOrgs.sort { a, b -> !a.sortname ? !b.sortname ? 0 : 1 : !b.sortname ? -1 : a.sortname <=> b.sortname }
            validOrgs.each { row ->
                Long optionKey = row.id
                String optionValue
                if(row.sortname)
                    optionValue = "${row.name} (${row.sortname})"
                else optionValue = "${row.name}"
                validOrgsDropdown << [optionKey: optionKey, optionValue: optionValue]
            }

        validOrgsDropdown
    }

    /**
     * Gets a list of all providers for dropdown output
     * @return a list of organisation
     */
    private Set<Map> _getProvidersDropdown() {
        Set<Map> validProviders = [], validProvidersDropdown = []

        boolean isInstitution = contextService.getOrg().isCustomerType_Inst()
        boolean isConsortium  = contextService.getOrg().isCustomerType_Consortium()

            GrailsParameterMap params = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
            params.sort      = " LOWER(p.name)"

            if (isConsortium) {
                String query = 'select new map(p.id as id, p.name as name, p.abbreviatedName as abbreviatedName) from ProviderRole pvr join pvr.provider p, OrgRole oo join oo.sub s where pvr.subscription = s and oo.org = :context and s.instanceOf = null order by '+params.sort
                validProviders = Provider.executeQuery(query, [context: contextService.getOrg()])
            }
            else if (isInstitution) {
                String query = 'select new map(p.id as id, p.name as name, p.abbreviatedName as abbreviatedName) from ProviderRole pvr join pvr.provider p, OrgRole oo where oo.sub = pvr.subscription and oo.org = :context order by '+params.sort
                validProviders.addAll(Provider.executeQuery(query, [context: contextService.getOrg()]))
            }
            validProviders.each { row ->
                Long optionKey = row.id
                String optionValue
                if(row.abbreviatedName)
                    optionValue = "${row.name} (${row.abbreviatedName})"
                else optionValue = "${row.name}"
                validProvidersDropdown << [optionKey: optionKey, optionValue: optionValue]
            }

        validProvidersDropdown
    }

    /**
     * Gets a list of all organisations for dropdown output
     * @return a list of organisation
     */
    private Set<Map> _getVendorsDropdown() {
        Set<Map> validVendors = [], validVendorsDropdown = []

        boolean isInstitution = contextService.getOrg().isCustomerType_Inst()
        boolean isConsortium  = contextService.getOrg().isCustomerType_Consortium()

            GrailsParameterMap params = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
            params.sort      = " LOWER(v.name)"

            if (isConsortium) {
                String query = 'select new map(v.id as id, v.name as name, v.abbreviatedName as abbreviatedName) from VendorRole vr join vr.vendor v, OrgRole oo join oo.sub s where vr.subscription = s and oo.org = :context and s.instanceOf = null order by '+params.sort
                validVendors = Combo.executeQuery(query, [context: contextService.getOrg()])
            }
            else if (isInstitution) {
                String query = 'select new map(v.id as id, v.name as name, v.abbreviatedName as abbreviatedName) from VendorRole vr join vr.vendor v, OrgRole oo where vr.subscription = oo.sub and oo.org = :context order by '+params.sort
                validVendors.addAll(Org.executeQuery(query, [context: contextService.getOrg()]))
            }
            validVendors.each { row ->
                Long optionKey = row.id
                String optionValue
                if(row.abbreviatedName)
                    optionValue = "${row.name} (${row.abbreviatedName})"
                else optionValue = "${row.name}"
                validVendorsDropdown << [optionKey: optionKey, optionValue: optionValue]
            }

        validVendorsDropdown
    }

    /**
     * Gets a list of all subscriptions for dropdown output
     * @param isWithInstanceOf should member subscriptions being retrieved as well?
     * @return a list of subscriptions
     */
    private List<Map> _getSubscriptionsDropdown(boolean isWithInstanceOf) {
        List validSubscriptionsWithInstanceOf = []
        List validSubscriptionsWithoutInstanceOf = []
        List<Map> validSubscriptionsDropdown = []

        boolean isConsortium = contextService.getOrg().isCustomerType_Consortium()

            if (isConsortium) {

                Map<String, Object> qry_params_for_sub = [
                        'roleTypes' : [
                                RDStore.OR_SUBSCRIBER_CONS,
                                RDStore.OR_SUBSCRIPTION_CONSORTIUM
                        ],
                        'activeInst': contextService.getOrg()
                ]

                validSubscriptionsWithoutInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status from OrgRole oo join oo.sub s where oo.roleType IN (:roleTypes) AND oo.org = :activeInst and s.instanceOf is null order by lower(s.name), s.endDate", qry_params_for_sub)

                if (isWithInstanceOf) {
                    validSubscriptionsWithInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status, oo.org.sortname from OrgRole oo join oo.sub s where oo.roleType in (:memberRoleTypes) and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:consRoleTypes) AND o.org = :activeInst ) ) ) ) and s.instanceOf is not null order by lower(s.name), s.endDate", qry_params_for_sub << [memberRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN],consRoleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM]])
                }

            }
            else {
                Map<String, Object> qry_params_for_sub = [
                        'roleTypes' : [
                                RDStore.OR_SUBSCRIBER,
                                RDStore.OR_SUBSCRIBER_CONS
                        ],
                        'activeInst': contextService.getOrg()
                ]
                validSubscriptionsWithoutInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status from OrgRole oo join oo.sub s where oo.roleType IN (:roleTypes) AND oo.org = :activeInst order by lower(s.name), s.endDate", qry_params_for_sub)
            }

        String NO_STATUS = RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTimeShort()

        validSubscriptionsWithInstanceOf.each {

            Long optionKey = it[0]
            String optionValue = (
                    it[1]
                            + ' - '
                            + (it[4] ? it[4].getI10n('value') : NO_STATUS)
                            + ((it[2] || it[3]) ? ' (' : ' ')
                            + (it[2] ? sdf.format(it[2]) : '')
                            + '-'
                            + (it[3] ? sdf.format(it[3]) : '')
                            + ((it[2] || it[3]) ? ') ' : ' ')
            )
            if (isConsortium) {
                optionValue += " - " + it[5]

            } else {
                optionValue += ' - Konsortiallizenz'
            }
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        validSubscriptionsWithoutInstanceOf.each {

            Long optionKey = it[0]
            String optionValue = (
                    it[1]
                            + ' - '
                            + (it[4] ? it[4].getI10n('value') : NO_STATUS)
                            + ((it[2] || it[3]) ? ' (' : ' ')
                            + (it[2] ? sdf.format(it[2]) : '')
                            + '-'
                            + (it[3] ? sdf.format(it[3]) : '')
                            + ((it[2] || it[3]) ? ') ' : ' ')
            )
            validSubscriptionsDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        if (isWithInstanceOf) {
            validSubscriptionsDropdown.sort { it.optionValue.toLowerCase() }
        }
        validSubscriptionsDropdown
    }

    /**
     * Gets a list of all licenses for dropdown output
     * @param isWithInstanceOf should member licenses being retrieved as well?
     * @return a list of subscriptions
     */
    private List<Map> _getLicensesDropdown(boolean isWithInstanceOf) {
        List<License> validLicensesOhneInstanceOf = []
        List<License> validLicensesMitInstanceOf = []
        List<Map> validLicensesDropdown = []

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTimeShort()

            String licensesQueryMitInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate, licinstanceof.type from License lic left join lic.orgRelations o left join lic.instanceOf licinstanceof WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is not null order by lic.sortableReference asc'

            String licensesQueryOhneInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate from License lic left join lic.orgRelations o WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is null order by lic.sortableReference asc'

            if (contextService.getOrg().isCustomerType_Consortium()){
                Map<String, Object> qry_params_for_lic = [
                    lic_org:    contextService.getOrg(),
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
            else if (contextService.getOrg().isCustomerType_Inst_Pro()) {
                Map<String, Object> qry_params_for_lic = [
                    lic_org:    contextService.getOrg(),
                    org_roles:  [
                            RDStore.OR_LICENSEE.id,
                            RDStore.OR_LICENSEE_CONS.id
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

        String member = ' - ' +messageSource.getMessage('license.member', null, LocaleUtils.getCurrentLocale())
        validLicensesDropdown = validLicensesMitInstanceOf?.collect{

            def optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? sdf.format(it[3]) : '') + ('-') + (it[4] ? sdf.format(it[4]) : '') + ')'
            boolean isLicensingConsortium = 'Licensing Consortium' == it[5]?.value

            if (isLicensingConsortium) {
                optionValue += member
            }
            return [optionKey: optionKey, optionValue: optionValue]
        }
        validLicensesOhneInstanceOf?.collect{

            Long optionKey = it[0]
            String optionValue = it[1] + ' ' + (it[2].getI10n('value')) + ' (' + (it[3] ? sdf.format(it[3]) : '') + ('-') + (it[4] ? sdf.format(it[4]) : '') + ')'
            validLicensesDropdown << [optionKey: optionKey, optionValue: optionValue]
        }
        if (isWithInstanceOf) {
            validLicensesDropdown.sort { it.optionValue.toLowerCase() }
        }
        validLicensesDropdown
    }

    /**
     * Adds an order to the task query if not specified
     * @param tableAlias the joined table name
     * @param query the base task query
     * @return the query string with the order clause
     */
    private String _addDefaultOrder(String tableAlias, String query){
        if (query && ( ! query.toLowerCase().contains('order by'))){
            if (tableAlias) {
                query += ' order by '+tableAlias+'.endDate asc'
            } else {
                query += ' order by endDate asc'
            }
        }
        query
    }

    /**
     * Checks if the current user has *potential* reading rights
     * @return true if the context user belongs to a PRO customer, false otherwise
     * @see CustomerTypeService#PERMS_PRO
     * @see ContextService#isInstUser()
     */
    boolean hasREAD() {
        contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    }

    /**
     * Checks if the current user has *potential* editing rights
     * @return true if the context user is at least {@link de.laser.auth.Role#INST_EDITOR} at a PRO customer, false otherwise
     * @see CustomerTypeService#PERMS_PRO
     * @see ContextService#isInstEditor()
     */
    boolean hasWRITE() {
        contextService.isInstEditor(CustomerTypeService.PERMS_PRO)
    }
}
