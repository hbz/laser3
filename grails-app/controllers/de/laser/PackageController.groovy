package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.properties.PropertyDefinition
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.context.MessageSource

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

/**
 * This controller manages display calls to packages
 * @see Package
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PackageController {

    AccessService accessService
    AddressbookService addressbookService
    ContextService contextService
    EscapeService escapeService
    ExecutorService executorService
    ExecutorWrapperService executorWrapperService
    ExportService exportService
    FilterService filterService
    GenericOIDService genericOIDService
    GokbService gokbService
    MessageSource messageSource
    SubscriptionService subscriptionService
    ExportClickMeService exportClickMeService
    //TaskService taskService
    YodaService yodaService

    //-----

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'index' : 'package.show.all',
            'list' : 'myinst.packages',
            'myInstitution/currentPackages' : 'menu.my.packages'
    ]

    //-----

    /**
     * Lists current packages in the we:kb ElasticSearch index.
     * @return Data from we:kb ES
     */
    @Secured(['ROLE_USER'])
    def index() {

        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        if (!apiSource) {
            redirect controller: 'package', action: 'list'
            return
        }
        Map<String, Object> result = [
                flagContentGokb : true // gokbService.queryElasticsearch
        ]
        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, result.user)

        result.editUrl = apiSource.editUrl

        String esQuery = "?componentType=Package"
        if (params.q) {
            result.filterSet = true
            esQuery += "&name=${params.q}"
            esQuery += "&ids=Anbieter_Produkt_ID,*${params.q}*"
            esQuery += "&ids=isil,*${params.q}*"
        }

        if (params.provider) {
            result.filterSet = true
            esQuery += "&provider=${params.provider.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')}"
        }

        if (params.curatoryGroup) {
            result.filterSet = true
            esQuery += "&curatoryGroupExact=${params.curatoryGroup.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')}"
        }

        if (params.ddc) {
            result.filterSet = true
            params.list("ddc").each { String key ->
                esQuery += "&ddc=${RefdataValue.get(key).value}"
            }
        }

        //you rarely encounter it; ^ is the XOR operator in Java - if both options are set, we mean all curatory group types
        if (params.containsKey('curatoryGroupProvider') ^ params.containsKey('curatoryGroupOther')) {
            result.filterSet = true
            if(params.curatoryGroupProvider)
                esQuery += "&curatoryGroupType=provider"
            else if(params.curatoryGroupOther)
                esQuery += "&curatoryGroupType=other" //setting to this includes also missing ones, this is already implemented in we:kb
        }

        Map queryCuratoryGroups = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + '/groups')
        if(queryCuratoryGroups.error == 404) {
            result.error = message(code:'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups.warning) {
                List recordsCuratoryGroups = queryCuratoryGroups.warning.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)

            result.putAll(gokbService.doQuery(result, params.clone(), esQuery))
        }

        result
    }

    /**
     * Is a fallback to list packages which are in the local LAS:eR database
     */
    @Secured(['ROLE_ADMIN'])
    def list() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.editable = true

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        RefdataValue deleted_package_status = RefdataValue.getByValueAndCategory('Deleted', RDConstants.PACKAGE_STATUS)
        //def qry_params = [deleted_package_status]
        def qry_params = []

        // TODO: filter by status in frontend
        // TODO: use elastic search
        String base_qry = " from Package as p where ( (p.packageStatus is null ) OR ( p.packageStatus is not null ) ) "
        //def base_qry = " from Package as p where ( (p.packageStatus is null ) OR ( p.packageStatus = ? ) ) "

        if (params.q?.length() > 0) {
            base_qry += " and ( ( lower(p.name) like ? ) or ( lower(p.identifier) like ? ) )"
            qry_params.add("%${params.q.trim().toLowerCase()}%");
            qry_params.add("%${params.q.trim().toLowerCase()}%");
        }

        if (params.updateStartDate?.length() > 0) {
            base_qry += " and ( p.lastUpdated > ? )"
            qry_params.add(params.date('updateStartDate', message(code: 'default.date.format.notime')));
        }

        if (params.updateEndDate?.length() > 0) {
            base_qry += " and ( p.lastUpdated < ? )"
            qry_params.add(params.date('updateEndDate', message(code: 'default.date.format.notime')));
        }

        if (params.createStartDate?.length() > 0) {
            base_qry += " and ( p.dateCreated > ? )"
            qry_params.add(params.date('createStartDate', message(code: 'default.date.format.notime')));
        }

        if (params.createEndDate?.length() > 0) {
            base_qry += " and ( p.dateCreated < ? )"
            qry_params.add(params.date('createEndDate', message(code: 'default.date.format.notime')));
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by p.${params.sort} ${params.order}"
        } else {
            base_qry += " order by lower(p.name) asc"
        }


        log.debug(base_qry + ' <<< ' + qry_params)
        result.packageInstanceTotal = Subscription.executeQuery("select p.id " + base_qry, qry_params).size()


        withFormat {
            html {
                result.packageInstanceList = Subscription.executeQuery("select p " + base_qry, qry_params, [max: result.max, offset: result.offset])
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"packages.csv\"")
                response.contentType = "text/csv"
                List packages = Subscription.executeQuery("select p " + base_qry, qry_params)
                ServletOutputStream out = response.outputStream
                log.debug('colheads');
                out.withWriter { writer ->
                    writer.write('Package Name, Creation Date, Last Modified, Identifier\n');
                    packages.each {
                        log.debug(it);
                        writer.write("${it.name},${it.dateCreated},${it.lastUpdated},${it.identifier}\n")
                    }
                    writer.write("END");
                    writer.flush();
                    writer.close();
                }
                out.close()
            }
        }
    }

    /**
     * Shows the details of the package. Consider that an active connection to a we:kb ElasticSearch index has to exist
     * because some data will not be mirrored to the app
     */
    @Secured(['ROLE_USER'])
    @Check404()
    def show() {
        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        Package packageInstance
        if(params.id instanceof Long || params.id.isLong())
            packageInstance = Package.get(params.id)
        else if(params.id ==~ /[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}/)
            packageInstance = Package.findByGokbId(params.id)
        else packageInstance = Package.findByGlobalUID(params.id)

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]
        result.contextOrg = contextService.getOrg()
        result.contextCustomerType = result.contextOrg.getCustomerType()

        // tasks
        /*
        result.tasks = taskService.getTasksByResponsiblesAndObject(contextService.getUser(), result.contextOrg, packageInstance)
        Map<String,Object> preCon = taskService.getPreconditionsWithoutTargets(result.contextOrg)
        result << preCon*/

        result.modalPrsLinkRole = RefdataValue.getByValueAndCategory('Specific package editor', RDConstants.PERSON_RESPONSIBILITY)
        result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.contextOrg)

        // restrict visible for templates/links/orgLinksAsList
        result.visibleOrgs = packageInstance.orgs
        //result.visibleOrgs.sort { it.org.sortname }

        List<RefdataValue> roleTypes = [RDStore.OR_SUBSCRIBER]
        if (accessService.checkPerm('ORG_CONSORTIUM_BASIC')) {
            roleTypes.addAll([RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS])
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        params.max = result.max

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Date today = new Date()
        if (!params.asAt) {
            if (packageInstance.startDate > today) {
                params.asAt = sdf.format(packageInstance.startDate)
            } else if (packageInstance.endDate < today && packageInstance.endDate) {
                params.asAt = sdf.format(packageInstance.endDate)
            }
        }

        result.lasttipp = result.offset + result.max > result.num_tipp_rows ? result.num_tipp_rows : result.offset + result.max

        if (OrgSetting.get(result.contextOrg, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID) instanceof OrgSetting) {
            result.statsWibid = result.contextOrg.getIdentifierByType('wibid')?.value
            result.usageMode = accessService.checkPerm("ORG_CONSORTIUM_BASIC") ? 'package' : 'institution'
            result.packageIdentifier = packageInstance.getIdentifierByType('isil')?.value
        }

        Set<Subscription> gascoSubscriptions = Subscription.executeQuery('select s from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.propertySet prop where pkg = :pkg and prop.type = :gasco and prop.refValue = :yes', [pkg: packageInstance, gasco: PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP), yes: RDStore.YN_YES])
        Map<Org, Map<String, Object>> gascoContacts = [:]
        PropertyDefinition gascoDisplayName = PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP)
        gascoSubscriptions.each { Subscription s ->
            Org gascoNegotiator = s.getConsortia()
            if(gascoNegotiator) {
                Map<String, Object> gascoContactData = gascoContacts.get(gascoNegotiator)
                if(!gascoContactData) {
                    gascoContactData = [:]
                    String gascoDisplay = s.propertySet.find{ it.type == gascoDisplayName}?.stringValue
                    gascoContactData.orgDisplay = gascoDisplay ?: gascoNegotiator.name
                    gascoContactData.personRoles = PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, gascoNegotiator)
                    gascoContacts.put(gascoNegotiator, gascoContactData)
                }
            }
        }
        result.gascoContacts = gascoContacts

        result.packageInstance = packageInstance

        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'

        Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${packageInstance.gokbId}")
        if (queryResult.error && queryResult.error == 404) {
            flash.error = message(code:'wekb.error.404') as String
        }
        else if (queryResult.warning) {
            List records = queryResult.warning.records
            result.packageInstanceRecord = records ? records[0] : [:]
        }
        if(packageInstance.nominalPlatform) {
            //record filled with LAS:eR and we:kb data
            Map<String, Object> platformInstanceRecord = [:]
            queryResult = gokbService.queryElasticsearch(apiSource.baseUrl+apiSource.fixToken+"/find?uuid=${packageInstance.nominalPlatform.gokbId}")
            if(queryResult.warning) {
                List records = queryResult.warning.records
                if(records)
                    platformInstanceRecord.putAll(records[0])
                platformInstanceRecord.name = packageInstance.nominalPlatform.name
                platformInstanceRecord.status = packageInstance.nominalPlatform.status
                platformInstanceRecord.org = packageInstance.nominalPlatform.org
                platformInstanceRecord.id = packageInstance.nominalPlatform.id
                platformInstanceRecord.primaryUrl = packageInstance.nominalPlatform.primaryUrl
            }
            result.platformInstanceRecord = platformInstanceRecord
            result.platformInstance = packageInstance.nominalPlatform
        }

        result.flagContentGokb = true // gokbService.queryElasticsearch
        result
    }

    /**
     * Call to show all current titles in the package. The entitlement holding may be shown directly as HTML
     * or exported as KBART (<a href="https://www.niso.org/standards-committees/kbart">Knowledge Base and related tools</a>) file, CSV file or Excel worksheet
     * @return a HTML table showing the holding or the holding rendered as KBART or Excel worksheet
     * @see TitleInstancePackagePlatform
     */
    @Secured(['ROLE_USER'])
    @Check404()
    def current() {
        log.debug("current ${params}");
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        result.contextOrg = contextService.getOrg()
        result.contextCustomerType = result.contextOrg.getCustomerType()

        Package packageInstance = Package.get(params.id)
        result.packageInstance = packageInstance

        if (executorWrapperService.hasRunningProcess(packageInstance)) {
            result.processingpc = true
        }
        /*result.pendingChanges = PendingChange.executeQuery(
                "select pc from PendingChange as pc where pc.pkg = :pkg and ( pc.status is null or pc.status = :status ) order by ts, payload",
                [pkg: packageInstance, status: RDStore.PENDING_CHANGE_PENDING]
        )*/

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Map<String, Object> query = filterService.getTippQuery(params, [packageInstance])
        result.filterSet = query.filterSet

        List<Long> titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)

        String filename = "${escapeService.escapeString(packageInstance.name + '_' + message(code: 'package.show.nav.current'))}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"

        result.filename = filename

        if (params.exportKBart) {
            response.setHeader( "Content-Disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.pkgIds = [params.id]
            Map<String, List> tableData = titlesList ? exportService.generateTitleExportKBART(configMap, TitleInstancePackagePlatform.class.name) : []
            out.withWriter { writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
            return
        } else if (params.exportXLSX) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.pkgIds = [params.id]
            Map<String, List> export = titlesList ? exportService.generateTitleExportCustom(configMap, TitleInstancePackagePlatform.class.name) : [] //no subscription needed
            Map sheetData = [:]
            sheetData[message(code: 'title.plural')] = [titleRow: export.titles, columnData: export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            ArrayList<TitleInstancePackagePlatform> tipps = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList,[sort:'sortname']) : [:]

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(tipps, selectedFields)
            response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        withFormat {
            html {
                result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
                result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
                result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
                result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]
                //we can be sure that no one will request more than 32768 entries ...
                result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList.drop(result.offset).take(result.max), [sort: 'sortname']) : []
                result.num_tipp_rows = titlesList.size()

                result.lasttipp = result.offset + result.max > result.num_tipp_rows ? result.num_tipp_rows : result.offset + result.max
                result
            }
            csv {
                response.setHeader( "Content-Disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"

                ServletOutputStream out = response.outputStream
                Map<String, List> tableData = titlesList ? exportService.generateTitleExportCSV(titlesList,TitleInstancePackagePlatform.class.name) : []
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.rows, '|'))
                }
                out.flush()
                out.close()
                return
            }
        }
    }

    /**
     * Call to see planned titles of the package
     * @return {@link #planned_expired_deleted(java.lang.Object, java.lang.Object)}
     */
    @Secured(['ROLE_USER'])
    def planned() {
        planned_expired_deleted(params, "planned")
    }

    /**
     * Call to see expired titles of the package
     * @return {@link #planned_expired_deleted(java.lang.Object, java.lang.Object)}
     */
    @Secured(['ROLE_USER'])
    def expired() {
        planned_expired_deleted(params, "expired")
    }

    /**
     * Call to see deleted titles of the package
     * @return {@link #planned_expired_deleted(java.lang.Object, java.lang.Object)}
     */
    @Secured(['ROLE_USER'])
    def deleted() {
        planned_expired_deleted(params, "deleted")
    }

    /**
     * Call to show all titles matching the given status in the package. The entitlement holding may be shown directly as HTML
     * or exported as KBART (<a href="https://www.niso.org/standards-committees/kbart">Knowledge Base and related tools</a>) file, CSV file or Excel worksheet
     * @param params filter parameters
     * @param func the status key to filter
     * @return a HTML table showing the holding or the holding rendered as KBART or Excel worksheet
     * @see TitleInstancePackagePlatform
     */
    @Secured(['ROLE_USER'])
    def planned_expired_deleted(params, func) {
        log.debug("planned_expired_deleted ${params}");
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        result.contextOrg = contextService.getOrg()
        result.contextCustomerType = result.contextOrg.getCustomerType()

        Package packageInstance = Package.get(params.id)
        if (!packageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'package.label'), params.id]) as String
            redirect action: 'index'
            return
        }
        result.packageInstance = packageInstance

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        String filename

        if (func == "planned") {
            params.status = RDStore.TIPP_STATUS_EXPECTED.id
            filename = "${escapeService.escapeString(packageInstance.name + '_' + message(code: 'package.show.nav.planned'))}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
        } else if (func == "expired") {
            params.status = RDStore.TIPP_STATUS_RETIRED.id
            filename = "${escapeService.escapeString(packageInstance.name + '_' + message(code: 'package.show.nav.expired'))}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
        } else if (func == "deleted") {
            params.status = RDStore.TIPP_STATUS_DELETED.id
            filename = "${escapeService.escapeString(packageInstance.name + '_' + message(code: 'package.show.nav.deleted'))}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
        }

        Map<String, Object> query = filterService.getTippQuery(params, [packageInstance])
        result.filterSet = query.filterSet

        List<Long> titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
        result.filename = filename

        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.pkgIds = [params.id]
            Map<String, List> tableData = exportService.generateTitleExportKBART(configMap,TitleInstancePackagePlatform.class.name)
            out.withWriter { writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        } else if (params.exportXLSX) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.pkgIds = [params.id]
            Map<String, List> export = exportService.generateTitleExportCustom(params, TitleInstancePackagePlatform.class.name) //no subscription needed
            Map sheetData = [:]
            sheetData[message(code: 'title.plural')] = [titleRow: export.titles, columnData: export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            ArrayList<TitleInstancePackagePlatform> tipps = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList,[sort:'sortname']) : [:]

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(tipps, selectedFields)
            response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        withFormat {
            html {

                result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList,[sort:'sortname']).drop(result.offset).take(result.max) : []
                result.num_tipp_rows = titlesList.size()

                result.lasttipp = result.offset + result.max > result.num_tipp_rows ? result.num_tipp_rows : result.offset + result.max
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"

                ServletOutputStream out = response.outputStream
                Map<String, List> tableData = exportService.generateTitleExportCSV(titlesList,TitleInstancePackagePlatform.class.name)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.rows, '|'))
                }
                out.flush()
                out.close()
            }
        }
    }

    /**
     * Shows the title changes done in the package
     * @see PendingChange
     */
    @Secured(['ROLE_USER'])
    @Check404()
    def tippChanges() {
        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        Package packageInstance = Package.get(params.id)

        result.packageInstance = packageInstance

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]

        Set<Long> packageHistory = []

        String query = 'select pc.id from PendingChange pc where pc.pkg = :pkg and pc.oid = null and pc.status = :history ',
               query1 = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :pkg and pc.oid = null and pc.status = :history ',
               query2 = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :pkg and pc.oid = null and pc.status = :history ',
               query3 = 'select pc.id from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :pkg and pc.oid = null and pc.status = :history '

        packageHistory.addAll(PendingChange.executeQuery(query, [pkg: packageInstance, history: RDStore.PENDING_CHANGE_HISTORY]))
        packageHistory.addAll(PendingChange.executeQuery(query1, [pkg: packageInstance, history: RDStore.PENDING_CHANGE_HISTORY]))
        packageHistory.addAll(PendingChange.executeQuery(query2, [pkg: packageInstance, history: RDStore.PENDING_CHANGE_HISTORY]))
        packageHistory.addAll(PendingChange.executeQuery(query3, [pkg: packageInstance, history: RDStore.PENDING_CHANGE_HISTORY]))

        params.sort = params.sort ?: 'ts'
        params.order = params.order ?: 'desc'
        params.max = result.max
        params.offset = result.offset

        List changes = packageHistory ? PendingChange.findAllByIdInList(packageHistory.drop(result.max).take(result.max), params) : []
        result.countPendingChanges = packageHistory.size()

        result.num_change_rows = result.countPendingChanges
        result.changes = changes

        result.apisources = ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        result
    }

    /**
     * Links the given package to the given subscription and creates issue entitlements
     * of the current package holding. If the package was not available in the app,
     * the we:kb data will be fetched and data mirrored prior to linking the package
     * to the subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processLinkToSub() {
        Map<String, Object> result = [:]
        result.pkg = Package.get(params.id)
        result.subscription = genericOIDService.resolveOID(params.targetObjectId)

        if (result.subscription) {
            Locale locale = LocaleUtils.getCurrentLocale()
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            boolean bulkProcessRunning = false
            threadArray.each { Thread thread ->
                if (thread.name == 'PackageSync_' + result.subscription.id && !SubscriptionPackage.findBySubscriptionAndPkg(result.subscription, result.pkg)) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running', null, locale)
                    bulkProcessRunning = true
                }
            }
            //to be deployed in parallel thread
            if (result.pkg) {
                if(!bulkProcessRunning) {
                    executorService.execute({
                        Thread.currentThread().setName('PackageSync_' + result.subscription.id)
                        String addType = params.addType
                        log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                        if (addType == 'With') {
                            subscriptionService.addToSubscription(result.subscription, result.pkg, true)
                        } else if (addType == 'Without') {
                            subscriptionService.addToSubscription(result.subscription, result.pkg, false)
                        }

                        if (addType != null && addType != '') {
                            subscriptionService.addPendingChangeConfiguration(result.subscription, result.pkg, params.clone())
                        }
                    })
                }
            }
            switch (params.addType) {
                case "With": flash.message = message(code: 'subscription.details.link.processingWithEntitlements') as String
                    redirect controller: 'subscription', action: 'index', params: [id: result.subscription.id, gokbId: result.pkg.gokbId]
                    return
                    break
                case "Without": flash.message = message(code: 'subscription.details.link.processingWithoutEntitlements') as String
                    redirect controller: 'subscription', action: 'addEntitlements', params: [id: result.subscription.id, packageLinkPreselect: result.pkg.gokbId, preselectedName: result.pkg.name]
                    return
                    break
            }
        } else {
            flash.error = message(code: 'package.show.linkToSub.noSubSelection') as String
            redirect controller: 'package', action: 'show', params: [id: params.id]
            return
        }

        redirect(url: request.getHeader("referer"))
    }

    /**
     * For that no accidental call may occur ... ROLE_YODA is correct!
     * Lists duplicates package in the database
     */
    @Secured(['ROLE_YODA'])
    Map getDuplicatePackages() {
        yodaService.listDuplicatePackages()
    }

    /**
     * Executes package deduplication and merges duplicate issue entitlements
     */
    @Secured(['ROLE_YODA'])
    def purgeDuplicatePackages() {
        List<Long> toDelete = (List<Long>) JSON.parse(params.toDelete)
        if (params.doIt == "true") {
            yodaService.executePackageCleanup(toDelete)
            redirect action: 'index'
            return
        } else {
            flash.message = "Betroffene Paket-IDs wären gelöscht worden: ${toDelete.join(", ")}"
            redirect action: 'getDuplicatePackages'
            return
        }
    }
}
