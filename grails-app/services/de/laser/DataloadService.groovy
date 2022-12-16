package de.laser

import de.laser.remote.FTControl
import de.laser.storage.RDConstants
import de.laser.properties.LicenseProperty
import de.laser.properties.SubscriptionProperty
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyOrg
import de.laser.system.SystemEvent
import de.laser.storage.RDStore
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.interfaces.CalculatedType
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import grails.converters.JSON
import org.apache.commons.lang3.ClassUtils
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.admin.indices.flush.FlushRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.core.CountResponse
import org.elasticsearch.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.grails.web.json.JSONElement

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * This service handles the app's ElasticSearch connection and the app's data indexing
 */
//@Transactional
class DataloadService {

    ESWrapperService ESWrapperService
    ExecutorService executorService

    static final int BULK_LIMIT         = 3000000

    static final int BULK_SIZE_LARGE    = 10000
    static final int BULK_SIZE_MEDIUM   = 5000
    static final int BULK_SIZE_SMALL    = 100

    boolean update_running = false
    Future activeFuture

    def updateFTIndex(String indexName) {
        if (indexName) {
            updateFTIndices(indexName)
        }
    }

    /**
     * Cronjob- or Yoda-triggered.
     * Starts the update of the ElasticSearch indices and initialises a parallel thread for the update
     * @return false if the job is already running, true otherwise
     */
    def updateFTIndices(String indexName = null) {
        boolean update = false

        if (! update_running) {
            if (!(activeFuture) || activeFuture.isDone()) {

                if (indexName) {
                    activeFuture = executorService.submit({
                        Thread.currentThread().setName("DataloadServiceUpdateFTIndex")
                        update = doFTUpdate(indexName)
                    })
                    log.debug("updateFTIndices(${indexName}) returning")
                } else {
                    activeFuture = executorService.submit({
                        Thread.currentThread().setName("DataloadServiceUpdateFTIndices")
                        update = doFTUpdate()
                    })
                    log.debug("updateFTIndices returning")
                }
            } else {
                log.debug("doFTUpdate already running")
            }
        } else {
            log.debug("doFTUpdate already running")
        }
        update
    }

    /**
     * Performs the index update and sets a lock to prevent multiple execution.
     * See the aggr_es_indices (taken at ESWrapperService.es_indices) configuration for
     * the domains being indexed and the fields recorded for each index
     * @return true if the update was successful, false otherwise
     * @see ESWrapperService#ES_Indices
     */
    boolean doFTUpdate(String indexName = null) {

        SystemEvent sysEvent
        if (indexName) { sysEvent = SystemEvent.createEvent('FT_INDEX_UPDATE_START', [index: indexName]) }
        else           { sysEvent = SystemEvent.createEvent('FT_INDEX_UPDATE_START') }

        synchronized(this) {
            if ( update_running ) {
                log.debug("doFTUpdate ---> exiting - one already running")
                return false
            }
            else {
                update_running = true
            }
        }
        long start_time = System.currentTimeMillis()
        log.debug("doFTUpdate ---> Starting at ${new Date()}")

        if (indexName) { _doFTUpdateUpdateESCalls(ESWrapperService.getDomainClassByIndex(indexName)) }
        else           { _doFTUpdateUpdateESCalls() }

        double elapsed = ((System.currentTimeMillis() - start_time) / 1000).round(2)
        log.debug("doFTUpdate ---> Completed in ${elapsed}s")

        if (indexName) { sysEvent.changeTo('FT_INDEX_UPDATE_COMPLETE', [index: indexName, s: elapsed]) }
        else           { sysEvent.changeTo('FT_INDEX_UPDATE_COMPLETE', [s: elapsed]) }

        update_running = false
        true
    }

    private void _doFTUpdateUpdateESCalls(Class domainClass = null) {

        if (!domainClass || domainClass == Org.class) {

            _updateES(Org.class, BULK_SIZE_MEDIUM) { Org org ->
                Map result = [:]

                result._id = org.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 30
                result.dbId = org.id

                result.gokbId = org.gokbId
                result.guid = org.globalUID ?: ''

                result.name = org.name

                result.status = org.status?.getMapForES()
                result.visible = 'Public'
                result.rectype = org.getClass().getSimpleName()

                result.shortname = org.shortname
                result.sortname = org.sortname

                result.type = []
                org.orgType?.each { type ->
                    try {
                        result.type.add(type.getMapForES())
                    } catch (Exception e) {
                        log.error( e.toString() )
                    }
                }

                result.identifiers = []
                org.ids?.each { ident ->
                    try {
                        if (ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                    } catch (Exception e) {
                        log.error( e.toString() )
                    }
                }

                result.platforms = []
                org.platforms?.each { platform ->
                    try {
                        result.platforms.add([dbId: platform.id, name: platform.name])
                    } catch (Exception e) {
                        log.error( e.toString() )
                    }
                }

                result.dateCreated = org.dateCreated
                result.lastUpdated = org.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == TitleInstancePackagePlatform.class) {

            _updateES(TitleInstancePackagePlatform.class, BULK_SIZE_LARGE) { TitleInstancePackagePlatform tipp ->
                Map result = [:]

                if (tipp.name != null && tipp.titleType != null) {
                    if (!tipp.sortname) {
                        tipp.generateNormTitle()
                        tipp.generateSortTitle()
                        tipp.save()
                        //
                        // This alone should trigger before update to do the necessary...
                        //
                    }

                    result._id = tipp.globalUID
                    if (!result._id) {
                        return result
                    }

                    result.priority = 20
                    result.dbId = tipp.id

                    result.gokbId = tipp.gokbId
                    result.guid = tipp.globalUID ?: ''
                    result.name = tipp.name
                    result.status = tipp.status?.getMapForES()
                    result.visible = 'Public'
                    result.rectype = tipp.getClass().getSimpleName()

                    result.sortname = tipp.sortname

                    result.medium = tipp.medium?.getMapForES()
                    RefdataValue titleType = RefdataValue.getByValueAndCategory(tipp.titleType, RDConstants.TITLE_MEDIUM)
                    result.type = titleType ? titleType.getMapForES() : []

                    List<Org> publishers = tipp.getPublishers()
                    result.publishers = []
                    publishers.each { publisher ->
                        result.publishers.add([id: publisher.id, name: publisher.name])
                    }

                    result.identifiers = []
                    tipp.ids.each { Identifier ident ->
                        try {
                            if (ident.value) {
                                result.identifiers.add([type: ident.ns.ns, value: ident.value])
                            }
                        } catch (Exception e) {
                            log.error( e.toString() )
                        }
                    }
                    //result.keyTitle = ti.keyTitle
                    //result.normTitle = ti.normTitle

                    result.dateCreated = tipp.dateCreated
                    result.lastUpdated = tipp.lastUpdated

                } else {
                    log.warn("Title with no title string - ${tipp.id}")
                }

                result
            }
        }

        if (!domainClass || domainClass == Package.class) {

            _updateES(Package.class, BULK_SIZE_MEDIUM) { Package pkg ->
                Map result = [:]

                result._id = pkg.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 30
                result.dbId = pkg.id
                result.gokbId = pkg.gokbId
                result.guid = pkg.globalUID ?: ''
                result.name = "${pkg.name}"
                result.status = pkg.packageStatus?.getMapForES()
                result.visible = 'Public'
                result.rectype = pkg.getClass().getSimpleName()

                //result.consortiaID = pkg.getConsortia()?.id
                //result.consortiaName = pkg.getConsortia()?.name
                result.providerId = pkg.getContentProvider()?.id
                result.providerName = pkg.getContentProvider()?.name

                result.isPublic = (pkg?.isPublic) ? 'Yes' : 'No'

                result.sortname = pkg.sortname
                result.startDate = pkg.startDate
                result.endDate = pkg.endDate

                //result.titleCountCurrent = pkg.getCurrentTipps().size() ?: 0
                result.titleCountCurrent = TitleInstancePackagePlatform.executeQuery(
                        'select count(id) from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status = :status',
                        [pkg: pkg, status: RDStore.TIPP_STATUS_CURRENT]
                )

                result.identifiers = []
                pkg.ids?.each { ident ->
                    try {
                        if (ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                    } catch (Exception e) {
                        log.error( e.toString() )
                    }
                }
                result.dateCreated = pkg.dateCreated
                result.lastUpdated = pkg.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == Platform.class) {

            _updateES(Platform.class, BULK_SIZE_MEDIUM) { Platform plat ->
                Map result = [:]

                result._id = plat.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 30
                result.dbId = plat.id
                result.gokbId = plat.gokbId
                result.guid = plat.globalUID ?: ''
                result.name = plat.name
                result.status = plat.status?.getMapForES()
                result.visible = 'Public'
                result.rectype = plat.getClass().getSimpleName()

                result.primaryUrl = plat.primaryUrl
                result.orgId = plat.org?.id
                result.orgName = plat.org?.name

                //result.titleCountCurrent = plat.getCurrentTipps().size() ?: 0
                result.titleCountCurrent = TitleInstancePackagePlatform.executeQuery(
                        'select count(id) from TitleInstancePackagePlatform tipp where tipp.platform = :plat and tipp.status = :status',
                        [plat: plat, status: RDStore.TIPP_STATUS_CURRENT]
                )

                result.dateCreated = plat.dateCreated
                result.lastUpdated = plat.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == License.class) {

            _updateES(License.class, BULK_SIZE_MEDIUM) { License lic ->
                Map result = [:]

                result._id = lic.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 50
                result.dbId = lic.id
                result.guid = lic.globalUID ?: ''
                result.name = lic.reference
                result.visible = 'Private'
                result.rectype = lic.getClass().getSimpleName()

                switch (lic._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = lic.orgRelations.findAll { OrgRole oo ->oo.roleType.value in [RDStore.OR_LICENSING_CONSORTIUM.value] }?.org?.id
                        result.membersCount = License.findAllByInstanceOf(lic).size()?:0
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        List orgs = lic.orgRelations.findAll { OrgRole oo -> oo.roleType.value in [RDStore.OR_LICENSEE_CONS.value] }?.org
                        result.availableToOrgs = orgs.collect { Org org -> org.id }
                        result.consortiaID = lic.getLicensingConsortium()?.id
                        result.consortiaName = lic.getLicensingConsortium()?.name

                        result.members = []
                        orgs.each { Org org ->
                            result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                        }
                        break
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = lic.orgRelations.findAll { OrgRole oo -> oo.roleType.value in [RDStore.OR_LICENSEE.value] }?.org?.id
                        break
                }

                result.identifiers = []
                lic.ids?.each { ident ->
                    try {
                        if (ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                    } catch (Exception e) {
                        log.error(e.toString())
                    }
                }

                result.endDate = lic.endDate
                result.startDate = lic.startDate

                if (lic.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(lic.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }
                if (lic.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(lic.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }

                result.dateCreated = lic.dateCreated
                result.lastUpdated = lic.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == Subscription.class) {

            _updateES(Subscription.class, BULK_SIZE_MEDIUM) { Subscription sub ->
                Map result = [:]

                result._id = sub.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 70
                result.dbId = sub.id
                result.guid = sub.globalUID ?: ''
                result.name = sub.name
                result.status = sub.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = sub.getClass().getSimpleName()

                switch (sub._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = sub.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value] }?.org?.id
                        result.membersCount = Subscription.findAllByInstanceOf(sub).size() ?: 0
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        List orgs = sub.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value] }?.org
                        result.availableToOrgs = orgs?.id
                        result.consortiaID = sub.getConsortia()?.id
                        result.consortiaName = sub.getConsortia()?.name

                        result.members = []
                        orgs.each { org ->
                            result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                        }
                        break
                        /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = sub.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER.value] }?.org?.id
                        break
                }

                result.identifiers = []
                sub.ids?.each { ident ->
                    try {
                        if (ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                    } catch (Exception e) {
                        log.error(e.toString())
                    }
                }

                result.endDate = sub.endDate
                result.startDate = sub.startDate
                if (sub.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(sub.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }

                if (sub.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(sub.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }
                result.packages = []
                sub.packages.each { sp ->
                    def pgkinfo = [:]
                    if (sp.pkg != null) {
                        // Defensive - it appears that there can be a SP without a package.
                        pgkinfo.pkgname = sp.pkg.name
                        pgkinfo.pkgid = sp.pkg.id
                        pgkinfo.providerName = sp.pkg.contentProvider?.name
                        pgkinfo.providerId = sp.pkg.contentProvider?.id
                        result.packages.add(pgkinfo)
                    }
                }

                result.dateCreated = sub.dateCreated
                result.lastUpdated = sub.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == SurveyConfig.class) {

            _updateES(SurveyConfig.class, BULK_SIZE_MEDIUM) { SurveyConfig surveyConfig ->
                Map result = [:]

                result._id = surveyConfig.getClass().getSimpleName().toLowerCase() + ":" + surveyConfig.id
                if (!result._id) {
                    return result
                }

                result.priority = 60
                result.dbId = surveyConfig.id
                result.name = surveyConfig.getSurveyName()
                result.status = surveyConfig.surveyInfo.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = surveyConfig.getClass().getSimpleName()

                result.availableToOrgs = [surveyConfig.surveyInfo.owner?.id]

                result.membersCount = surveyConfig.orgs?.size() ?: 0

                result.endDate = surveyConfig.surveyInfo.endDate
                result.startDate = surveyConfig.surveyInfo.startDate

                if (surveyConfig.surveyInfo.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(surveyConfig.surveyInfo.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }

                if (surveyConfig.surveyInfo.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(surveyConfig.surveyInfo.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }

                result.dateCreated = surveyConfig.dateCreated
                result.lastUpdated = surveyConfig.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == SurveyOrg.class) {

            _updateES(SurveyOrg.class, BULK_SIZE_MEDIUM) { SurveyOrg surOrg ->
                Map result = [:]

                result._id = surOrg.getClass().getSimpleName().toLowerCase() + ":" + surOrg.id
                if (!result._id) {
                    return result
                }

                result.priority = 60
                result.dbId = surOrg.surveyConfig.id
                result.name = surOrg.surveyConfig.getSurveyName()
                result.status = surOrg.surveyConfig.surveyInfo.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = surOrg.getClass().getSimpleName()

                result.availableToOrgs = (surOrg.surveyConfig.surveyInfo.status.value != RDStore.SURVEY_IN_PROCESSING.value) ? [surOrg.org.id] : [0]

                result.endDate = surOrg.surveyConfig.surveyInfo.endDate
                result.startDate = surOrg.surveyConfig.surveyInfo.startDate

                if (surOrg.surveyConfig.surveyInfo.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(surOrg.surveyConfig.surveyInfo.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }

                if (surOrg.surveyConfig.surveyInfo.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(surOrg.surveyConfig.surveyInfo.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }

                result.dateCreated = surOrg.dateCreated
                result.lastUpdated = surOrg.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == Task.class) {

            _updateES(Task.class, BULK_SIZE_MEDIUM) { Task task ->
                Map result = [:]

                result._id = task.getClass().getSimpleName().toLowerCase() + ":" + task.id
                if (!result._id) {
                    return result
                }

                result.priority = 40
                result.dbId = task.id
                result.name = task.title
                result.status = task.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = task.getClass().getSimpleName()

                result.availableToOrgs = [task.responsibleOrg?.id ?: 0]
                result.availableToUser = [task.responsibleUser?.id]

                result.description = task.description
                result.endDate = task.endDate

                if (task.subscription) {
                    result.objectId = task.subscription.id
                    result.objectName = task.subscription.name
                    result.objectTypeId = task.subscription.type?.id
                    result.objectClassName = task.subscription.getClass().getSimpleName().toLowerCase()
                }

                if (task.org) {
                    result.objectId = task.org.id
                    result.objectName = task.org.name
                    result.objectClassName = task.org.getClass().getSimpleName().toLowerCase()
                }

                if (task.license) {
                    result.objectId = task.license.id
                    result.objectName = task.license.reference
                    result.objectClassName = task.license.getClass().getSimpleName().toLowerCase()
                }

                if (task.surveyConfig) {
                    result.objectId = task.surveyConfig.id
                    result.objectName = task.surveyConfig.getSurveyName()
                    result.objectClassName = task.surveyConfig.getClass().getSimpleName().toLowerCase()
                }

                result.dateCreated = task.dateCreated
                result.lastUpdated = task.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == DocContext.class) {

            _updateES(DocContext.class, BULK_SIZE_LARGE) { DocContext docCon ->
                Map result = [:]

                result._id = docCon.getClass().getSimpleName().toLowerCase() + ":" + docCon.id
                if (!result._id) {
                    return result
                }

                result.priority = 40
                result.dbId = docCon.id
                result.name = docCon.owner?.title ?: ''
                result.status = docCon.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = (docCon.isDocANote()) ? 'Note' : 'Document'

                result.availableToOrgs = [docCon.owner?.owner?.id ?: 0]

                result.description = docCon.owner?.content ?: ''

                if (docCon.subscription) {
                    result.objectId = docCon.subscription.id
                    result.objectName = docCon.subscription.name
                    result.objectTypeId = docCon.subscription.type?.id
                    result.objectClassName = docCon.subscription.getClass().getSimpleName().toLowerCase()
                }

                if (docCon.org) {
                    result.objectId = docCon.org.id
                    result.objectName = docCon.org.name
                    result.objectClassName = docCon.org.getClass().getSimpleName().toLowerCase()
                }

                if (docCon.license) {
                    result.objectId = docCon.license.id
                    result.objectName = docCon.license.reference
                    result.objectClassName = docCon.license.getClass().getSimpleName().toLowerCase()
                }

                if (docCon.surveyConfig) {
                    result.objectId = docCon.surveyConfig.id
                    result.objectName = docCon.surveyConfig.getSurveyName()
                    result.objectClassName = docCon.surveyConfig.getClass().getSimpleName().toLowerCase()
                }

                result.dateCreated = docCon.dateCreated
                result.lastUpdated = docCon.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == IssueEntitlement.class) {

            _updateES(IssueEntitlement.class, BULK_SIZE_LARGE) { IssueEntitlement ie ->
                Map result = [:]

                result._id = ie.globalUID
                if (!result._id) {
                    return result
                }

                result.priority = 45
                result.dbId = ie.id
                result.name = ie.tipp?.name
                result.status = ie.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = ie.getClass().getSimpleName()

                switch (ie.subscription._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = ie.subscription.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value] }?.org?.id
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        result.availableToOrgs = ie.subscription.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value] }?.org?.id
                        break
                        /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                              result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                              break*/
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = ie.subscription.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER.value] }?.org?.id
                        break
                }

                if (ie.subscription) {
                    result.objectId = ie.subscription.id
                    result.objectName = ie.subscription.name
                    result.objectTypeId = ie.subscription.type?.id
                    result.objectClassName = ie.subscription.getClass().getSimpleName().toLowerCase()
                }

                if (ie.accessStartDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(ie.accessStartDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }

                if (ie.accessEndDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(ie.accessEndDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }

                result.dateCreated = ie.dateCreated
                result.lastUpdated = ie.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == SubscriptionProperty.class) {

            _updateES(SubscriptionProperty.class, BULK_SIZE_LARGE) { SubscriptionProperty subProp ->
                Map result = [:]

                result._id = subProp.getClass().getSimpleName().toLowerCase() + ":" + subProp.id
                if (!result._id) {
                    return result
                }

                result.priority = 45
                result.dbId = subProp.id
                result.name = subProp.type?.name

                result.visible = 'Private'
                result.rectype = subProp.getClass().getSimpleName()

                if (subProp.type.isIntegerType()) {
                    result.description = subProp.intValue
                } else if (subProp.type.isStringType()) {
                    result.description = subProp.stringValue
                } else if (subProp.type.isBigDecimalType()) {
                    result.description = subProp.decValue
                } else if (subProp.type.isDateType()) {
                    result.description = subProp.dateValue
                } else if (subProp.type.isURLType()) {
                    result.description = subProp.urlValue
                } else if (subProp.type.isRefdataValueType()) {
                    //result.description = subProp.refValue?.getMapForES()
                    result.description = subProp.refValue?.value
                }

                if (subProp.isPublic) {
                    switch (subProp.owner._getCalculatedType()) {
                        case CalculatedType.TYPE_CONSORTIAL:
                            result.availableToOrgs = subProp.owner.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value] }?.org?.id
                            break
                        case CalculatedType.TYPE_PARTICIPATION:
                            result.availableToOrgs = subProp.owner.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value] }?.org?.id
                            break
                            /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                        case CalculatedType.TYPE_LOCAL:
                            result.availableToOrgs = subProp.owner.orgRelations.findAll { it.roleType.value in [RDStore.OR_SUBSCRIBER.value] }?.org?.id
                            break
                    }
                } else result.availableToOrgs = [subProp.type.tenant?.id ?: 0]

                if (subProp.owner) {
                    result.objectId = subProp.owner.id
                    result.objectName = subProp.owner.name
                    result.objectTypeId = subProp.owner.type?.id
                    result.objectClassName = subProp.owner.getClass().getSimpleName().toLowerCase()
                }

                result.dateCreated = subProp.dateCreated
                result.lastUpdated = subProp.lastUpdated

                result
            }
        }

        if (!domainClass || domainClass == LicenseProperty.class) {

            _updateES(LicenseProperty.class, BULK_SIZE_MEDIUM) { LicenseProperty licProp ->
                Map result = [:]

                result._id = licProp.getClass().getSimpleName().toLowerCase() + ":" + licProp.id
                if (!result._id) {
                    return result
                }

                result.priority = 45
                result.dbId = licProp.id
                result.name = licProp.type?.name

                result.visible = 'Private'
                result.rectype = licProp.getClass().getSimpleName()

                if (licProp.type.isIntegerType()) {
                    result.description = licProp.intValue
                } else if (licProp.type.isStringType()) {
                    result.description = licProp.stringValue
                } else if (licProp.type.isBigDecimalType()) {
                    result.description = licProp.decValue
                } else if (licProp.type.isDateType()) {
                    result.description = licProp.dateValue
                } else if (licProp.type.isURLType()) {
                    result.description = licProp.urlValue
                } else if (licProp.type.isRefdataValueType()) {
                    //result.description = licProp.refValue?.getMapForES()
                    result.description = licProp.refValue?.value
                }

                if (licProp.isPublic) {
                    switch (licProp.owner._getCalculatedType()) {
                        case CalculatedType.TYPE_CONSORTIAL:
                            result.availableToOrgs = licProp.owner.orgRelations.findAll { it.roleType?.value in [RDStore.OR_LICENSING_CONSORTIUM.value] }?.org?.id
                            break
                        case CalculatedType.TYPE_PARTICIPATION:
                            result.availableToOrgs = licProp.owner.orgRelations.findAll { it.roleType?.value in [RDStore.OR_LICENSEE_CONS.value] }?.org?.id
                            break
                        case CalculatedType.TYPE_LOCAL:
                            result.availableToOrgs = licProp.owner.orgRelations.findAll { it.roleType?.value in [RDStore.OR_LICENSEE.value] }?.org?.id
                            break
                    }
                } else result.availableToOrgs = [licProp.type.tenant?.id ?: 0]

                if (licProp.owner) {
                    result.objectId = licProp.owner.id
                    result.objectName = licProp.owner.reference
                    result.objectClassName = licProp.owner.getClass().getSimpleName().toLowerCase()
                }

                result.dateCreated = licProp.dateCreated
                result.lastUpdated = licProp.lastUpdated

                result
            }
        }
    }

    /**
     * Updates the given domain index with the given record generating closure.
     * This bulk operation is being flushed at every 100 records
     * @param domainClass the domain class whose index should be updated
     * @param recgen_closure the closure to be used for record generation
     * @see ESWrapperService#ES_Indices
     */
    private void _updateES(Class domainClass, int bulkSize, Closure recgen_closure) {
        String logPrefix = "updateES ( ${domainClass.name} )"

        log.info ( "${logPrefix}")

        RestHighLevelClient esclient = ESWrapperService.getNewClient(true)
        Map es_indices = ESWrapperService.ES_Indices

        long total = 0
        long startingTimestamp = 0
        long bulkMaxTimestamp = 0
        BigDecimal mb = 0
        BigDecimal totalMb = 0

        if (! FTControl.findByDomainClassNameAndActivity(domainClass.name, 'ESIndex')) {
            (new FTControl(domainClassName: domainClass.name, activity: 'ESIndex', lastTimestamp: 0, active: true, esElements: 0, dbElements: 0)).save()
        }

        FTControl.withTransaction {

            FTControl ftControl = FTControl.findByDomainClassNameAndActivity(domainClass.name, 'ESIndex')
            try {
                if (ftControl.active) {

                    if (esclient && es_indices && es_indices.get(domainClass.simpleName)) {
                        Date from = new Date(ftControl.lastTimestamp)
                        List<Long> idList = []
                        List<Long> ignoredObjectIdList = []

                        if (ClassUtils.getAllInterfaces(domainClass).contains(CalculatedLastUpdated)) {
                            idList = domainClass.executeQuery(
                                    // "select d.id from " + domainClass.name + " as d where (d.lastUpdatedCascading is not null and d.lastUpdatedCascading > :from) or (d.lastUpdated > :from) or (d.dateCreated > :from and d.lastUpdated is null) order by d.lastUpdated asc, d.id",
                                    "select d.id from " + domainClass.name + " as d where (d.dateCreated > :from or d.lastUpdated > :from or d.lastUpdatedCascading > :from) order by d.dateCreated asc, d.id",
                                    [from: from], [readonly: true]
                            )
                        } else {
                            idList = domainClass.executeQuery(
                                    // "select d.id from " + domainClass.name + " as d where (d.lastUpdated > :from) or (d.dateCreated > :from and d.lastUpdated is null) order by d.lastUpdated asc, d.id",
                                    "select d.id from " + domainClass.name + " as d where (d.dateCreated > :from or d.lastUpdated > :from) order by d.dateCreated asc, d.id",
                                    [from: from], [readonly: true]
                            )
                        }

                        BulkRequest bulkRequest = new BulkRequest()
                        startingTimestamp = System.currentTimeMillis()

                            List<Long> todoList = idList.take(BULK_LIMIT)
                            List<List<Long>> bulks = todoList.collate(bulkSize)

                            if (bulks) {
                                if (idList.size() > BULK_LIMIT) {
                                    log.debug("${logPrefix} - ${idList.size()} changes since [${from}] - processing is limited to ${BULK_LIMIT}; bulks todo: ${bulks.size()}")
                                }
                                else {
                                    log.debug("${logPrefix} - ${idList.size()} changes since [${from}]; bulks todo: ${bulks.size()}")
                                }
                            }

                            bulks.eachWithIndex { List<Long> bulk, int i ->
                                for (domain_id in bulk) {
                                    Object r = domainClass.get(domain_id)
                                    Map idx_record = recgen_closure(r) as Map
                                    if (idx_record['_id'] == null) {
                                        ignoredObjectIdList.add(domain_id)
                                        continue
                                    }

                                    String recid = idx_record['_id'].toString()
                                    idx_record.remove('_id')

                                    IndexRequest request = new IndexRequest(es_indices[domainClass.simpleName])
                                    request.id(recid)
                                    String jsonString = idx_record as JSON
                                    request.source(jsonString, XContentType.JSON)

                                    bulkRequest.add(request)
                                    total++

                                    if (r.dateCreated) {
                                        bulkMaxTimestamp = Math.max(bulkMaxTimestamp, ((Date) r.dateCreated).getTime())
                                    }
                                } // for

                                mb = (bulkRequest.estimatedSizeInBytes()/1024/1024)
                                totalMb = totalMb + mb

                                if (bulkRequest.numberOfActions()) {
                                    BulkResponse bulkResponse = esclient.bulk(bulkRequest, RequestOptions.DEFAULT)

                                    if (bulkResponse.hasFailures()) {
                                        for (BulkItemResponse bulkItemResponse : bulkResponse) {
                                            if (bulkItemResponse.isFailed()) {
                                                BulkItemResponse.Failure failure = bulkItemResponse.getFailure()
                                                log.warn("${logPrefix} - (#1) bulk operation failure -> ${failure}")
                                            }
                                        }
                                    }
                                    log.debug("${logPrefix} - processed ${total} / ignored ${ignoredObjectIdList.size()} / todo ${todoList.size() - (total + ignoredObjectIdList.size())} records; bulkSize ${mb.round(2)}MB")
                                }
                                else {
                                    log.debug( "${logPrefix} - ignored empty bulk")
                                }

                                bulkRequest = new BulkRequest()
                            } // each

                            if (total) {
                                log.debug("${logPrefix} - totally processed ${total} records; ${totalMb.round(2)}MB")
                            }
                            if (ignoredObjectIdList) {
                                log.info("${logPrefix} - but ignored ${ignoredObjectIdList.size()} records because of missing _id")
                            }

                            if (idList.size() > BULK_LIMIT) {
                                log.debug("${logPrefix} - increasing last_timestamp to [${new Date(bulkMaxTimestamp)}]")
                                ftControl.lastTimestamp = bulkMaxTimestamp
                            } else {
                                ftControl.lastTimestamp = startingTimestamp
                            }
                            ftControl.save()

                    } else {
                        log.debug("${logPrefix} - failed. Preconditions not met!")
                    }
                } else {
                    log.debug("${logPrefix} - ignored. FTControl is not active")
                }
            }
            catch (Exception e) {
                log.error("${logPrefix} - Error with Rollback!", e)

                SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["index": domainClass.name])
            }
            finally {
                try {
                    if (esclient) {
                        if (ftControl.active) {
                            FlushRequest request = new FlushRequest(es_indices.get(domainClass.simpleName))
                            esclient.indices().flush(request, RequestOptions.DEFAULT)
                        }
                        esclient.close()
                    }
                    checkESElementswithDBElements(ftControl)
                }
                catch (Exception e) {
                    log.error("${logPrefix} - finally error: " + e.toString())
                }
            }
        }

        // log.info ( "${logPrefix} - End")
    }

    /**
     * Drops an old domain index and reinitialises it. An eventually running job is being cancelled; execution
     * is done if the job could be cancelled.
     * The new index is being rebuilt right after resetting
     */
    def resetESIndices() {
        log.debug("resetESIndices")

        RestHighLevelClient client = ESWrapperService.getNewClient(true)

        if (client) {
            if (!(activeFuture) || (activeFuture && activeFuture.cancel(true))) {

                SystemEvent.createEvent('YODA_ES_RESET_START')

                List<String> deleted = [], deletedFailed = []
                List<String> created = [], createdFailed = []

                Collection esIndicesNames = ESWrapperService.ES_Indices.values() ?: []
                esIndicesNames.each { String indexName ->
                    try {
                        boolean isDeletedIndex = ESWrapperService.deleteIndex(indexName)
                        if (isDeletedIndex) {
                            log.debug("Deleted ES index: ${indexName}")
                            deleted.add(indexName)
                        } else {
                            log.error("Failed to delete ES index: ${indexName}")
                            deletedFailed.add(indexName)
                        }
                    }
                    catch (ElasticsearchException e) {
                        if (e.status() == RestStatus.NOT_FOUND) {
                            log.warn("index does not exist ..")
                        } else {
                            log.warn("Problem deleting index ..", e)
                        }

//                        SystemEvent.createEvent('FT_INDEX_CLEANUP_ERROR', [index: indexName])
                        deletedFailed.add(indexName)
                    }

                    boolean isCreatedIndex = ESWrapperService.createIndex(indexName)
                    if (isCreatedIndex) {
                        log.debug("Created ES index: ${indexName}")
                        created.add(indexName)

                    } else {
                        log.debug("Failed to create ES index: ${indexName}")
                        createdFailed.add(indexName)
                    }
                }

                SystemEvent.createEvent('YODA_ES_RESET_DELETED',   [deleted: deleted, failed: deletedFailed])
                SystemEvent.createEvent('YODA_ES_RESET_CREATED', [created: created, failed: createdFailed])

                try {
                    client.close()
                }
                catch (Exception e) {
                    log.error(e.toString())
                }

                log.debug("Call updateFTIndices")
                updateFTIndices()

                // SystemEvent.createEvent('YODA_ES_RESET_END')
            }
            else {
                log.debug("!!!! resetESIndices is not possible !!!!")
            }
        }
    }

    /**
     * Compares the count of database entries for the given domain class with the count of ElasticSearch index entries for the
     * given domain class. The counts are being retained in the FTControl entry for the given domain
     * @param domainClassName the domain class to check the entry counts of
     * @return true if successful, false otherwise
     * @see FTControl
     */
    boolean checkESElementswithDBElements(FTControl ftControl) {

        RestHighLevelClient esclient = ESWrapperService.getNewClient(true)
        Map es_indices = ESWrapperService.ES_Indices

        if (esclient) {
            try {
                if (ftControl.active) {

                        Class domainClass = CodeUtils.getDomainClass(ftControl.domainClassName)
                        String indexName =  es_indices.get(domainClass.simpleName)
                        Integer countIndex = 0

                        GetIndexRequest request = new GetIndexRequest(indexName)

                        if (esclient.indices().exists(request, RequestOptions.DEFAULT)) {
                            CountRequest countRequest = new CountRequest(indexName)
                            CountResponse countResponse = esclient.count(countRequest, RequestOptions.DEFAULT)
                            countIndex = countResponse ? countResponse.getCount().toInteger() : 0
                        }

                        FTControl.withTransaction {
                            int countDB = domainClass.count()

                            if (countDB != countIndex) {
                                log.debug("Element comparison: DB <-> ES ( ${ftControl.domainClassName} / ${indexName} ) : +++++ DB = ${countDB}, ES = ${countIndex} +++++")
                                //ft.lastTimestamp = 0
                            }
                            else {
                                //log.debug("Element comparison: DB <-> ES ( ${ftControl.domainClassName} )")
                            }
                            ftControl.dbElements = countDB
                            ftControl.esElements = countIndex
                            ftControl.save()
                        }
                }
                else {
                    log.debug("Element comparison ignored, because ftControl is not active")
                }
            }
            finally {
                esclient.close()
            }
        }
        return true
    }

    /**
     * Compares the count of database entries for each domain class with the count of ElasticSearch index entries for the
     * respective domain class. The counts are being retained in the FTControl entries for each domain
     * @return true if successful, false otherwise
     * @see FTControl
     */
    boolean checkESElementswithDBElements() {

        RestHighLevelClient esclient = ESWrapperService.getNewClient(true)
        Map es_indices = ESWrapperService.ES_Indices

        if (esclient) {
            try {

                FTControl.list().each { ft ->

                    if (ft.active) {
                        Class domainClass = CodeUtils.getDomainClass(ft.domainClassName)
                        String indexName = es_indices.get(domainClass.simpleName)
                        Integer countIndex = 0

                        GetIndexRequest request = new GetIndexRequest(indexName)

                        if (esclient.indices().exists(request, RequestOptions.DEFAULT)) {
                            CountRequest countRequest = new CountRequest(indexName)
                            CountResponse countResponse = esclient.count(countRequest, RequestOptions.DEFAULT)
                            countIndex = countResponse ? countResponse.getCount().toInteger() : 0
                        }

                        FTControl.withTransaction {
                            int countDB = domainClass.count()

                            if (countDB != countIndex) {
                                log.debug("Element comparison: DB <-> ES  ( ${indexName} ): +++++ DB = ${countDB}, ES = ${countIndex} +++++")
                                //ft.lastTimestamp = 0
                            }
                            else {
                                log.debug("Element comparison: DB <-> ES")
                            }

                            ft.dbElements = countDB
                            ft.esElements = countIndex
                            ft.save()
                        }
                    } else {
                        log.debug("Element comparison ignored, because ftControl is not active")
                    }
                }
            }
            finally {
                esclient.close()
            }
        }
        return true
    }

    String getLastFTIndexUpdateInfo() {
        String info = '?'

        SystemEvent se = SystemEvent.getLastByToken('FT_INDEX_UPDATE_COMPLETE')
        if (!se) {
            se = SystemEvent.getLastByToken('FT_INDEX_UPDATE_START')
        }
        if (se) {
            info = DateUtils.getLocalizedSDF_noZ().format(se.created)
            if (se.payload) {
                JSONElement je = JSON.parse(se.payload)

                if (je.ms) {
                    long ms = JSON.parse(se.payload).ms ?: 0
                    if (ms) {
                        info += ' (' + (ms/1000).round(2) + 's)'
                    }
                }
                if (je.s) {
                    double s = JSON.parse(se.payload).s ?: 0
                    if (s) {
                        info += ' (' + s + 's)'
                    }
                }
            }
        }
        info
    }

    /**
     * Kills an eventually running process
     */
    synchronized void killDataloadService() {
        if (activeFuture != null) {
            SystemEvent.createEvent('FT_INDEX_UPDATE_KILLED')

            activeFuture.cancel(true)
            if (update_running) {
                update_running = false
                log.debug("Killed DataloadService! Set DataloadService.update_running to false")
            }
            else {
                log.debug("Killed DataloadService!")
            }
        }
    }
}
