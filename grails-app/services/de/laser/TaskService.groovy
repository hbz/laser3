package de.laser

import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

/**
 * This service retrieves data for task retrieval and creation
 */
@Transactional
class TaskService {

    AccessService accessService
    ContextService contextService
    MessageSource messageSource

    static final String SELECT_WITH_JOIN = 'select t from Task t LEFT JOIN t.responsibleUser ru '
    static final String WITHOUT_TENANT_ONLY = "WITHOUT_TENANT_ONLY"

    /**
     * Called from view for edit override
     * Checks if the given task may be edited by the given user
     * @param task the task to be checked
     * @param user the user accessing the task
     * @param org the context institution of the user
     * @return true if the user is creator or responsible of the task or the user belongs to the institution responsible for the task
     */
    boolean isTaskEditableBy(Task task, User user, Org org) {
        task.creator == user || task.responsibleUser?.id == user.id || task.responsibleOrg?.id == org.id
    }

    /**
     * Loads the user's tasks for the given object
     * @param offset the pagination offset from which data should be loaded
     * @param user the user whose tasks should be retrieved
     * @param contextOrg the user's context institution
     * @param object the object to which the tasks are attached
     * @return a list of accessible tasks
     */
    Map<String, Object> getTasks(int offset, User user, Org contextOrg, Object object) {
        Map<String, Object> result = [:]
        result.taskInstanceList = getTasksByResponsiblesAndObject(user, contextOrg, object)
        result.taskInstanceList = chopOffForPageSize(result.taskInstanceList, user, offset)
        result.myTaskInstanceList = getTasksByCreatorAndObject(user,  object)
        result.myTaskInstanceList = chopOffForPageSize(result.myTaskInstanceList, user, offset)
        result
    }

    /**
     * Loads the user's tasks for the given object; the output is for a PDF export
     * @param user the user whose tasks should be retrieved
     * @param contextOrg the user's context institution
     * @param object the object to which the tasks are attached
     * @return a list of accessible tasks
     */
    Set<Task> getTasksForExport(User user, Org contextOrg, Object object) {
        Set<Task> result = []
        result.addAll(getTasksByResponsiblesAndObject(user, contextOrg, object))
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
            case 'Package':
                tasks.addAll(Task.findAllByPkg(obj as Package, pparams))
                break
            case 'Subscription':
                tasks.addAll(Task.findAllBySubscription(obj as Subscription, pparams))
                break
            case 'SurveyConfig':
                tasks.addAll(Task.findAllBySurveyConfig(obj as SurveyConfig, pparams))
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
    List<Task> getTasksByCreator(User user, Map queryMap, String flag) {
        List<Task> tasks = []
        if (user) {
            String query
            if (flag == WITHOUT_TENANT_ONLY) {
                query = SELECT_WITH_JOIN + 'where t.creator = :user and ru is null and t.responsibleOrg is null'
            } else {
                query = SELECT_WITH_JOIN + 'where t.creator = :user'
            }

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
    List<Task> getTasksByCreatorAndObject(User user, License obj ) {
        (user && obj)? Task.findAllByCreatorAndLicense(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given organisation
     * @param user the user whose tasks should be retrieved
     * @param obj the organisation to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Org obj ) {
        (user && obj) ?  Task.findAllByCreatorAndOrg(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given package
     * @param user the user whose tasks should be retrieved
     * @param obj the package to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Package obj ) {
        (user && obj) ?  Task.findAllByCreatorAndPkg(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given subscription
     * @param user the user whose tasks should be retrieved
     * @param obj the subscription to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, Subscription obj) {
        (user && obj) ?  Task.findAllByCreatorAndSubscription(user, obj) : []
    }

    /**
     * Retrieves all tasks the given user created for the given survey
     * @param user the user whose tasks should be retrieved
     * @param obj the survey to which the tasks are attached
     * @return a complete list of tasks
     */
    List<Task> getTasksByCreatorAndObject(User user, SurveyConfig obj) {
        (user && obj) ?  Task.findAllByCreatorAndSurveyConfig(user, obj) : []
    }

    /**
     * Chop everything off beyond the user's pagination limit
     * @param taskInstanceList the complete list of tasks
     * @param user the user whose default page size should be taken
     * @param offset the offset of entries
     * @return the reduced list of tasks
     */
    List<Task> chopOffForPageSize(List taskInstanceList, User user, int offset){
        int taskInstanceCount = taskInstanceList.size() ?: 0
        if (taskInstanceCount > user.getPageSizeOrDefault()) {
            try {
                taskInstanceList = taskInstanceList.subList(offset, offset + user.getPageSizeOrDefault())
            }
            catch (IndexOutOfBoundsException e) {
                taskInstanceList = taskInstanceList.subList(offset, taskInstanceCount)
            }
        }
        taskInstanceList
    }

    /**
     * Gets the tasks for which the given user is responsible, restricted by an optional query
     * @param user the user responsible for those tasks which should be retrieved
     * @param queryMap eventual filter parameters
     * @return a (filtered) list of tasks
     */
    List<Task> getTasksByResponsible(User user, Map queryMap) {
        List<Task> tasks = []
        if (user) {
            String query  = SELECT_WITH_JOIN + 'where t.responsibleUser = :user' + queryMap.query
            query = _addDefaultOrder("t", query)

            Map params = [user : user] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    /**
     * Gets the tasks for which the given institution is responsible, restricted by an optional query
     * @param org the institution responsible for those tasks which should be retrieved
     * @param queryMap eventual filter parameters
     * @return a (filtered) list of tasks
     */
    List<Task> getTasksByResponsible(Org org, Map queryMap) {
        List<Task> tasks = []
        if (org) {
            String query  = SELECT_WITH_JOIN + 'where t.responsibleOrg = :org' + queryMap.query
            query = _addDefaultOrder("t", query)

            Map params = [org : org] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        }
        tasks
    }

    /**
     * Gets the tasks for which the given user or institution is responsible, restricted by an optional query
     * @param user the user responsible for those tasks which should be retrieved
     * @param org the institution responsible for those tasks which should be retrieved
     * @param queryMap eventual filter parameters
     * @return a (filtered) list of tasks
     */
    List<Task> getTasksByResponsibles(User user, Org org, Map queryMap) {
        List<Task> tasks = []

        if (user && org) {
            String query = SELECT_WITH_JOIN + 'where ( ru = :user or t.responsibleOrg = :org ) ' + queryMap.query
            query = _addDefaultOrder("t", query)

            Map<String, Object>  params = [user : user, org: org] << queryMap.queryParams
            tasks = Task.executeQuery(query, params)
        } else if (user) {
            tasks = getTasksByResponsible(user, queryMap)
        } else if (org) {
            tasks = getTasksByResponsible(org, queryMap)
        }
        tasks
    }

    List<Task> getTasksByResponsiblesAndObject(User user, Org org, Object obj) {
        List<Task> tasks = []
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
            String query = "select distinct(t) from Task t where ${tableName}=:obj and (responsibleUser=:user or responsibleOrg=:org) order by endDate"
            tasks = Task.executeQuery( query, [user: user, org: org, obj: obj] )
        }
        tasks
    }

    /**
     * Gets the possible selection values for the given institution to create a new task
     * @param contextOrg the institution whose perspective is going to be taken
     * @return a map containing prefilled lists for dropdowns
     */
    Map<String, Object> getPreconditions(Org contextOrg) {
        Map<String, Object> result = [:]

        result.validResponsibleOrgs         = contextOrg ? [contextOrg] : []
        result.validResponsibleUsers        = getUserDropdown(contextOrg)
        result.validPackages                = _getPackagesDropdown(contextOrg)
        result.validOrgsDropdown            = _getOrgsDropdown(contextOrg)
        result.validSubscriptionsDropdown   = _getSubscriptionsDropdown(contextOrg, false)
        result.validLicensesDropdown        = _getLicensesDropdown(contextOrg, false)

        result
    }

    /**
     * Gets a list of all packages for dropdown output
     * @param contextOrg unused
     * @return a list of packages
     */
    private List<Package> _getPackagesDropdown(Org contextOrg) {
        List<Package> validPackages        = Package.findAll("from Package p where p.name != '' and p.name != null order by lower(p.sortname) asc") // TODO
        validPackages
    }

    /**
     * Gets a list of all users for dropdown output
     * @param contextOrg the institution whose affiliated users should be retrieved
     * @return a list of users
     */
    List<User> getUserDropdown(Org org) { // modal_create
        org ? User.executeQuery( 'select u from User as u where u.formalOrg = :org order by lower(u.display)', [org: org]) : []
    }

    /**
     * Gets a list of all organisations for dropdown output
     * @param contextOrg the institution whose accessible organisations should be retrieved
     * @return a list of organisation
     */
    private Set<Map> _getOrgsDropdown(Org contextOrg) {
        Set validOrgs = [], validOrgsDropdown = []
        if (contextOrg) {
            boolean isInstitution = (contextOrg.isCustomerType_Inst())
            boolean isConsortium  = (contextOrg.isCustomerType_Consortium())

            GrailsParameterMap params = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
            params.sort      = " LOWER(o.sortname), LOWER(o.name)"
            //def fsq          = filterService.getOrgQuery(params)
            //validOrgs = Org.executeQuery('select o.id, o.name, o.sortname from Org o where (o.status is null or o.status != :orgStatus) order by  LOWER(o.sortname), LOWER(o.name) asc', fsq.queryParams)

            String comboQuery = 'select new map(o.id as id, o.name as name, o.sortname as sortname) from Org o join o.outgoingCombos c where c.toOrg = :toOrg and c.type = :type order by '+params.sort
            if (isConsortium){
                validOrgs = Combo.executeQuery(comboQuery,
                        [toOrg: contextOrg,
                        type:  RDStore.COMBO_TYPE_CONSORTIUM])
            }
            validOrgs.each { row ->
                Long optionKey = row.id
                if (isConsortium) {
                    String optionValue
                    if(row.sortname)
                        optionValue = "${row.name} (${row.sortname})"
                    else optionValue = "${row.name}"
                    validOrgsDropdown << [optionKey: optionKey, optionValue: optionValue]
                }
            }
        }
        validOrgsDropdown
    }

    /**
     * Gets a list of all subscriptions for dropdown output
     * @param contextOrg the institution whose subscriptions should be retrieved
     * @param isWithInstanceOf should member subscriptions being retrieved as well?
     * @return a list of subscriptions
     */
    private List<Map> _getSubscriptionsDropdown(Org contextOrg, boolean isWithInstanceOf) {
        List validSubscriptionsWithInstanceOf = []
        List validSubscriptionsWithoutInstanceOf = []
        List<Map> validSubscriptionsDropdown = []
        boolean isConsortium = contextOrg.isCustomerType_Consortium()

        if (contextOrg) {
            if (isConsortium) {

                Map<String, Object> qry_params_for_sub = [
                        'roleTypes' : [
                                RDStore.OR_SUBSCRIBER_CONS,
                                RDStore.OR_SUBSCRIPTION_CONSORTIA
                        ],
                        'activeInst': contextOrg
                ]

                validSubscriptionsWithoutInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status from OrgRole oo join oo.sub s where oo.roleType IN (:roleTypes) AND oo.org = :activeInst and s.instanceOf is null order by lower(s.name), s.endDate", qry_params_for_sub)

                if (isWithInstanceOf) {
                    validSubscriptionsWithInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status, oo.org.sortname from OrgRole oo join oo.sub s where oo.roleType in (:memberRoleTypes) and ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:consRoleTypes) AND o.org = :activeInst ) ) ) ) and s.instanceOf is not null order by lower(s.name), s.endDate", qry_params_for_sub << [memberRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN],consRoleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA]])
                }

            }
            else {
                Map<String, Object> qry_params_for_sub = [
                        'roleTypes' : [
                                RDStore.OR_SUBSCRIBER,
                                RDStore.OR_SUBSCRIBER_CONS
                        ],
                        'activeInst': contextOrg
                ]
                validSubscriptionsWithoutInstanceOf = Subscription.executeQuery("select s.id, s.name, s.startDate, s.endDate, s.status from OrgRole oo join oo.sub s where oo.roleType IN (:roleTypes) AND oo.org = :activeInst order by lower(s.name), s.endDate", qry_params_for_sub)
            }
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
     * @param contextOrg the institution whose licenses should be retrieved
     * @param isWithInstanceOf should member licenses being retrieved as well?
     * @return a list of subscriptions
     */
    private List<Map> _getLicensesDropdown(Org contextOrg, boolean isWithInstanceOf) {
        List<License> validLicensesOhneInstanceOf = []
        List<License> validLicensesMitInstanceOf = []
        List<Map> validLicensesDropdown = []

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTimeShort()

        if (contextOrg) {
            String licensesQueryMitInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate, licinstanceof.type from License lic left join lic.orgRelations o left join lic.instanceOf licinstanceof WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is not null order by lic.sortableReference asc'

            String licensesQueryOhneInstanceOf =
                    'SELECT lic.id, lic.reference, o.roleType, lic.startDate, lic.endDate from License lic left join lic.orgRelations o WHERE  o.org = :lic_org AND o.roleType.id IN (:org_roles) and lic.instanceOf is null order by lic.sortableReference asc'

            if (accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)){
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
            else if (accessService.ctxPerm(CustomerTypeService.ORG_INST_PRO)) {
                Map<String, Object> qry_params_for_lic = [
                    lic_org:    contextOrg,
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
     * Adds an order to the task query if not specified
     * @param tableAlias the joined table name
     * @param params the sorting parameter map
     * @return the query string with the order clause
     */
    private Map _addDefaultOrder(String tableAlias, Map params){
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
