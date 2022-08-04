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
import de.laser.titles.TitleInstance
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
import org.elasticsearch.action.admin.indices.flush.FlushResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.core.CountResponse
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.hibernate.Session
import org.springframework.transaction.TransactionStatus

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * This service handles the app's ElasticSearch connection and the app's data indexing
 */
//@Transactional
class DataloadService {

    ESWrapperService ESWrapperService
    ExecutorService executorService
    GlobalService globalService

    final static int BULK_SIZE = 5000

    boolean update_running = false
    Future activeFuture

    /**
     * Cronjob- or Yoda-triggered.
     * Starts the update of the ElasticSearch indices and initialises a parallel thread for the update
     * @return false if the job is already running, true otherwise
     */
    def updateFTIndexes() {
        //log.debug("updateFTIndexes ${this.hashCode()}")
        if(! update_running) {

            if(!(activeFuture) || activeFuture.isDone()) {

                activeFuture = executorService.submit({
                    Thread.currentThread().setName("DataloadServiceUpdateFTIndexes")
                    doFTUpdate()
                })
                 log.debug("updateFTIndexes returning")
            }else{
                log.debug("FT update already running #2")
                return false
            }
        } else {
            log.debug("FT update already running #1")
            return false
        }
    }

    /**
     * Performs the index update and sets a lock to prevent multiple execution.
     * See the aggr_es_indices (taken at ESWrapperService.es_indices) configuration for
     * the domains being indexed and the fields recorded for each index
     * @return true if the update was successful, false otherwise
     * @see ESWrapperService#ES_Indices
     */
    boolean doFTUpdate() {

        SystemEvent.createEvent('FT_INDEX_UPDATE_START')
        synchronized(this) {
            if ( update_running ) {
                log.debug("Exiting FT update - one already running");
                return false
            }
            else {
                update_running = true;
            }
        }
        log.debug("doFTUpdate: Execute IndexUpdateJob starting at ${new Date()}");

        long start_time = System.currentTimeMillis()

        updateES( Org.class ) { Org org ->
            def result = [:]

                result._id = org.globalUID
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
                        if(ident.value) {
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

        updateES( TitleInstancePackagePlatform.class ) { TitleInstancePackagePlatform tipp ->

            def result = [:]

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
                            if(ident.value) {
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

        updateES( Package.class ) { Package pkg ->
            def result = [:]

                result._id = pkg.globalUID
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

                result.titleCountCurrent = pkg.getCurrentTipps().size()?:0

                result.identifiers = []
                pkg.ids?.each { ident ->
                    try {
                        if(ident.value) {
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

        updateES( Platform.class ) { Platform plat ->
            def result = [:]

                result._id = plat.globalUID
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
                result.titleCountCurrent = plat.getCurrentTipps().size()?:0

                result.dateCreated = plat.dateCreated
                result.lastUpdated = plat.lastUpdated

            result
        }

        updateES( License.class ) { License lic ->
            def result = [:]

            result._id = lic.globalUID
            result.priority = 50
            result.dbId = lic.id
            result.guid = lic.globalUID ?:''
            result.name = lic.reference
            result.visible = 'Private'
            result.rectype = lic.getClass().getSimpleName()

            switch(lic._getCalculatedType()) {
                case CalculatedType.TYPE_CONSORTIAL:
                    result.availableToOrgs = lic.orgRelations.findAll{ OrgRole oo ->oo.roleType.value in [RDStore.OR_LICENSING_CONSORTIUM.value]}?.org?.id
                    result.membersCount = License.findAllByInstanceOf(lic).size()?:0
                    break
                case CalculatedType.TYPE_PARTICIPATION:
                    List orgs = lic.orgRelations.findAll{ OrgRole oo -> oo.roleType.value in [RDStore.OR_LICENSEE_CONS.value]}?.org
                    result.availableToOrgs = orgs.collect{ Org org -> org.id }
                    result.consortiaID = lic.getLicensingConsortium()?.id
                    result.consortiaName = lic.getLicensingConsortium()?.name

                    result.members = []
                    orgs.each{ Org org ->
                        result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                    }
                    break
                case CalculatedType.TYPE_LOCAL:
                    result.availableToOrgs = lic.orgRelations.findAll{ OrgRole oo -> oo.roleType.value in [RDStore.OR_LICENSEE.value]}?.org?.id
                    break
            }

            result.identifiers = []
            lic.ids?.each { ident ->
                try {
                    if(ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                } catch (Exception e) {
                    log.error( e.toString() )
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

        updateES( Subscription.class ) { Subscription sub ->
            def result = [:]

                result._id = sub.globalUID
                result.priority = 70
                result.dbId = sub.id
                result.guid = sub.globalUID ?: ''
                result.name = sub.name
                result.status = sub.status?.getMapForES()
                result.visible = 'Private'
                result.rectype = sub.getClass().getSimpleName()

                switch (sub._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                        result.membersCount = Subscription.findAllByInstanceOf(sub).size() ?:0
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        List orgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org
                        result.availableToOrgs = orgs?.id
                        result.consortiaID = sub.getConsortia()?.id
                        result.consortiaName = sub.getConsortia()?.name

                        result.members = []
                        orgs.each{ org ->
                            result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                        }
                        break
                /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER.value]}?.org?.id
                        break
                }

                result.identifiers = []
                sub.ids?.each { ident ->
                        try {
                            if(ident.value) {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        }
                        } catch (Exception e) {
                            log.error( e.toString() )
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
                        result.packages.add(pgkinfo);
                    }
                }

                result.dateCreated = sub.dateCreated
                result.lastUpdated = sub.lastUpdated

            result
        }

        updateES( SurveyConfig.class ) { SurveyConfig surveyConfig ->
            def result = [:]

            result._id = surveyConfig.getClass().getSimpleName().toLowerCase()+":"+surveyConfig.id
            result.priority = 60
            result.dbId = surveyConfig.id
            result.name = surveyConfig.getSurveyName()
            result.status= surveyConfig.surveyInfo.status?.getMapForES()
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

        updateES( SurveyOrg.class ) { SurveyOrg surOrg ->
            def result = [:]

            result._id = surOrg.getClass().getSimpleName().toLowerCase()+":"+surOrg.id
            result.priority = 60
            result.dbId = surOrg.surveyConfig.id
            result.name = surOrg.surveyConfig.getSurveyName()
            result.status= surOrg.surveyConfig.surveyInfo.status?.getMapForES()
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

        updateES( Task.class ) { Task task ->
            def result = [:]

            result._id = task.getClass().getSimpleName().toLowerCase()+":"+task.id
            result.priority = 40
            result.dbId = task.id
            result.name = task.title
            result.status= task.status?.getMapForES()
            result.visible = 'Private'
            result.rectype = task.getClass().getSimpleName()

            result.availableToOrgs = [task.responsibleOrg?.id ?: 0]
            result.availableToUser = [task.responsibleUser?.id]

            result.description = task.description
            result.endDate= task.endDate

            if(task.subscription){
                result.objectId = task.subscription.id
                result.objectName = task.subscription.name
                result.objectTypeId = task.subscription.type?.id
                result.objectClassName = task.subscription.getClass().getSimpleName().toLowerCase()
            }

            if(task.org){
                result.objectId = task.org.id
                result.objectName = task.org.name
                result.objectClassName = task.org.getClass().getSimpleName().toLowerCase()
            }

            if(task.license){
                result.objectId = task.license.id
                result.objectName = task.license.reference
                result.objectClassName = task.license.getClass().getSimpleName().toLowerCase()
            }

            if(task.surveyConfig){
                result.objectId = task.surveyConfig.id
                result.objectName = task.surveyConfig.getSurveyName()
                result.objectClassName = task.surveyConfig.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = task.dateCreated
            result.lastUpdated = task.lastUpdated

            result
        }

        updateES( DocContext.class ) { DocContext docCon ->
            def result = [:]

            result._id = docCon.getClass().getSimpleName().toLowerCase()+":"+docCon.id
            result.priority = 40
            result.dbId = docCon.id
            result.name = docCon.owner?.title ?: ''
            result.status= docCon.status?.getMapForES()
            result.visible = 'Private'
            result.rectype = (docCon.owner?.contentType == Doc.CONTENT_TYPE_STRING) ? 'Note' : 'Document'

            result.availableToOrgs = [docCon.owner?.owner?.id ?: 0]

            result.description = docCon.owner?.content ?: ''

            if(docCon.subscription){
                result.objectId = docCon.subscription.id
                result.objectName = docCon.subscription.name
                result.objectTypeId = docCon.subscription.type?.id
                result.objectClassName = docCon.subscription.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.org){
                result.objectId = docCon.org.id
                result.objectName = docCon.org.name
                result.objectClassName = docCon.org.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.license){
                result.objectId = docCon.license.id
                result.objectName = docCon.license.reference
                result.objectClassName = docCon.license.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.surveyConfig){
                result.objectId = docCon.surveyConfig.id
                result.objectName = docCon.surveyConfig.getSurveyName()
                result.objectClassName = docCon.surveyConfig.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = docCon.dateCreated
            result.lastUpdated = docCon.lastUpdated

            result
        }

        updateES( IssueEntitlement.class ) { IssueEntitlement ie ->
            def result = [:]

            result._id = ie.globalUID
            result.priority = 45
            result.dbId = ie.id
            result.name = ie.tipp?.name
            result.status= ie.status?.getMapForES()
            result.visible = 'Private'
            result.rectype = ie.getClass().getSimpleName()

            switch (ie.subscription._getCalculatedType()) {
                case CalculatedType.TYPE_CONSORTIAL:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                    break
                case CalculatedType.TYPE_PARTICIPATION:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                    break
            /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                              result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                              break*/
                case CalculatedType.TYPE_LOCAL:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER.value]}?.org?.id
                    break
            }

            if(ie.subscription){
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

        updateES( SubscriptionProperty.class ) { SubscriptionProperty subProp ->
            def result = [:]

            result._id = subProp.getClass().getSimpleName().toLowerCase()+":"+subProp.id
            result.priority = 45
            result.dbId = subProp.id
            result.name = subProp.type?.name

            result.visible = 'Private'
            result.rectype = subProp.getClass().getSimpleName()

            if(subProp.type.isIntegerType()){
                result.description = subProp.intValue
            }
            else if(subProp.type.isStringType()){
                result.description = subProp.stringValue
            }
            else if(subProp.type.isBigDecimalType()){
                result.description = subProp.decValue
            }
            else if(subProp.type.isDateType()){
                result.description = subProp.dateValue
            }
            else if(subProp.type.isURLType()){
                result.description = subProp.urlValue
            }
            else if(subProp.type.isRefdataValueType()){
                //result.description = subProp.refValue?.getMapForES()
                result.description = subProp.refValue?.value
            }

            if(subProp.isPublic) {
                switch (subProp.owner._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                        break
                /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER.value]}?.org?.id
                        break
                }
            }
            else result.availableToOrgs = [subProp.type.tenant?.id ?: 0]

            if(subProp.owner){
                result.objectId = subProp.owner.id
                result.objectName = subProp.owner.name
                result.objectTypeId = subProp.owner.type?.id
                result.objectClassName = subProp.owner.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = subProp.dateCreated
            result.lastUpdated = subProp.lastUpdated

            result
        }

        updateES( LicenseProperty.class ) { LicenseProperty licProp ->
            def result = [:]

            result._id = licProp.getClass().getSimpleName().toLowerCase()+":"+licProp.id
            result.priority = 45
            result.dbId = licProp.id
            result.name = licProp.type?.name

            result.visible = 'Private'
            result.rectype = licProp.getClass().getSimpleName()

            if(licProp.type.isIntegerType()){
                result.description = licProp.intValue
            }
            else if(licProp.type.isStringType()){
                result.description = licProp.stringValue
            }
            else if(licProp.type.isBigDecimalType()){
                result.description = licProp.decValue
            }
            else if(licProp.type.isDateType()){
                result.description = licProp.dateValue
            }
            else if(licProp.type.isURLType()){
                result.description = licProp.urlValue
            }
            else if(licProp.type.isRefdataValueType()){
                //result.description = licProp.refValue?.getMapForES()
                result.description = licProp.refValue?.value
            }

            if(licProp.isPublic) {
                switch(licProp.owner._getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = licProp.owner.orgRelations.findAll{it.roleType?.value in [RDStore.OR_LICENSING_CONSORTIUM.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        result.availableToOrgs = licProp.owner.orgRelations.findAll{it.roleType?.value in [RDStore.OR_LICENSEE_CONS.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = licProp.owner.orgRelations.findAll{it.roleType?.value in [RDStore.OR_LICENSEE.value]}?.org?.id
                        break
                }
            }
            else result.availableToOrgs = [licProp.type.tenant?.id ?: 0]

            if(licProp.owner){
                result.objectId = licProp.owner.id
                result.objectName = licProp.owner.reference
                result.objectClassName = licProp.owner.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = licProp.dateCreated
            result.lastUpdated = licProp.lastUpdated

            result
        }

        update_running = false
        long elapsed = System.currentTimeMillis() - start_time

        log.debug("IndexUpdateJob completed in ${elapsed}ms at ${new Date()} ")
        SystemEvent.createEvent('FT_INDEX_UPDATE_END', [ms: elapsed])

        return true
    }

    /**
     * Updates the given domain index with the given record generating closure.
     * This bulk operation is being flushed at every 100 records
     * @param domainClass the domain class whose index should be updated
     * @param recgen_closure the closure to be used for record generation
     * @see ESWrapperService#ES_Indices
     */
    void updateES(Class domainClass, Closure recgen_closure) {

        RestHighLevelClient esclient = ESWrapperService.getClient()
        Map es_indices = ESWrapperService.ES_Indices

        int count = 0
        long total = 0
        long startingTimestamp = 0

        //FTControl.withTransaction { TransactionStatus ts ->

            FTControl latest_ft_record = FTControl.findByDomainClassNameAndActivity(domainClass.name, 'ESIndex')
            if (!latest_ft_record) {
                latest_ft_record = new FTControl(domainClassName: domainClass.name, activity: 'ESIndex', lastTimestamp: 0, active: true, esElements: 0, dbElements: 0)
            }

            try {
                if (latest_ft_record.active) {

                    if (ESWrapperService.testConnection() && es_indices && es_indices.get(domainClass.simpleName)) {

                        log.debug("updateES ( ${domainClass.name} ) for changes since ${new Date(latest_ft_record.lastTimestamp)}")
                        Date from = new Date(latest_ft_record.lastTimestamp)

                        List<Long> idList = []

                        if (ClassUtils.getAllInterfaces(domainClass).contains(CalculatedLastUpdated)) {
                            idList = domainClass.executeQuery(
                                    "select d.id from " + domainClass.name + " as d where (d.lastUpdatedCascading is not null and d.lastUpdatedCascading > :from) or (d.lastUpdated > :from) or (d.dateCreated > :from and d.lastUpdated is null) order by d.lastUpdated asc, d.id",
                                    [from: from], [readonly: true]
                            )
                        } else {
                            idList = domainClass.executeQuery(
                                    "select d.id from " + domainClass.name + " as d where (d.lastUpdated > :from) or (d.dateCreated > :from and d.lastUpdated is null) order by d.lastUpdated asc, d.id",
                                    [from: from], [readonly: true]
                            )
                        }

                        startingTimestamp = System.currentTimeMillis()
                        BulkRequest bulkRequest = new BulkRequest();

                        FTControl.withNewSession { Session session ->
                            for (domain_id in idList) {
                                Object r = domainClass.get(domain_id)
                                Map idx_record = recgen_closure(r) as Map
                                if (idx_record['_id'] == null) {
                                    // log.error("******** Record without an ID: ${idx_record} Obj:${r} ******** ")
                                    log.warn("+++++ Record without an ID for: ${r} +++++")
                                    continue
                                }

                                String recid = idx_record['_id'].toString()
                                idx_record.remove('_id');

                                IndexRequest request = new IndexRequest(es_indices[domainClass.simpleName])
                                request.id(recid);
                                String jsonString = idx_record as JSON
                                //String jsonString = JsonOutput.toJson(idx_record)
                                //println(jsonString)
                                request.source(jsonString, XContentType.JSON)

                                bulkRequest.add(request)

                                count++
                                total++
                                if (count >= BULK_SIZE) {
                                    count = 0;
                                    // log.debug("noa ---> ${bulkRequest.numberOfActions()} : esib ---> ${bulkRequest.estimatedSizeInBytes()}")

                                    BulkResponse bulkResponse = esclient.bulk(bulkRequest, RequestOptions.DEFAULT)

                                    if (bulkResponse.hasFailures()) {
                                        for (BulkItemResponse bulkItemResponse : bulkResponse) {
                                            if (bulkItemResponse.isFailed()) {
                                                BulkItemResponse.Failure failure = bulkItemResponse.getFailure()
                                                log.warn("- updateES ${domainClass.name}: #1 bulk operation failure -> ${failure}")
                                            }
                                        }
                                    }

                                    log.debug("- processed ${total} of ${idList.size()} records ( ${domainClass.name} )")
                                    bulkRequest = new BulkRequest()
                                }
                            }

                            if (count > 0) {
                                BulkResponse bulkResponse = esclient.bulk(bulkRequest, RequestOptions.DEFAULT)

                                if (bulkResponse.hasFailures()) {
                                    for (BulkItemResponse bulkItemResponse : bulkResponse) {
                                        if (bulkItemResponse.isFailed()) {
                                            BulkItemResponse.Failure failure = bulkItemResponse.getFailure()
                                            log.warn("- updateES ${domainClass.name}: #2 bulk operation failure -> ${failure}")
                                        }
                                    }
                                }
                            }

                            log.debug("- finally processed ${total} records ( ${domainClass.name} )")

                            latest_ft_record.lastTimestamp = startingTimestamp
                            latest_ft_record.save()
                            session.flush()
                            session.clear()

                        } // withNewSession
                    } else {
                        latest_ft_record.save()
                        log.debug("updateES ${domainClass.name}: Failed -> ESWrapperService.testConnection() && es_indices && es_indices.get(domain.simpleName)")
                    }
                } else {
                    latest_ft_record.save()
                    log.debug("updateES ${domainClass.name}: FTControl is not active")
                }

            }
            catch (Exception e) {
                log.error("Problem with FT index", e)

                SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["index": domainClass.name])
            }
            finally {
                log.debug("Completed processing on ${domainClass.name} - saved ${total} records")
                try {
                    if (ESWrapperService.testConnection()) {
                        if (latest_ft_record.active) {
                            FlushRequest request = new FlushRequest(es_indices.get(domainClass.simpleName));
                            FlushResponse flushResponse = esclient.indices().flush(request, RequestOptions.DEFAULT)
                        }

                        esclient.close()
                    }
                    checkESElementswithDBElements(domainClass.name)
                }
                catch (Exception e) {
                    log.error(e.toString())
                }
            }
        //}
    }

    @Deprecated
    def dataCleanse() {
        log.debug("dataCleanse")
        executorService.execute({
            //doDataCleanse()
            log.debug("dataCleanse deactived")
        })
        log.debug("dataCleanse returning")
    }

    @Deprecated
  def doDataCleanse() {
    log.debug("dataCleansing");
    // 1. Find all packages that do not have a nominal platform
    Package.findAllByNominalPlatformIsNull().each { p ->
      Map platforms = [:]
      p.tipps.each{ tipp ->
        if ( !platforms.keySet().contains(tipp.platform.id) ) {
          platforms[tipp.platform.id] = [count:1, platform:tipp.platform]
        }
        else {
          platforms[tipp.platform.id].count++
        }
      }

      def selected_platform = null;
      def largest = 0;
      platforms.values().each { pl ->
        log.debug("Processing ${pl}");
        if ( pl['count'] > largest ) {
          selected_platform = pl['platform']
        }
      }

      log.debug("Nominal platform is ${selected_platform} for ${p.id}");
      p.nominalPlatform = selected_platform
      p.save()
    }

    // Fill out any missing sort keys on titles, packages or licenses
    long num_rows_updated = 0
    long sort_str_start_time = System.currentTimeMillis()
    boolean rows_updated = true

    while ( rows_updated ) {
      rows_updated = false

      TitleInstance.findAllBySortTitle(null,[max:100]).each {
        log.debug("Normalise Title ${it.title}");
        it.sortTitle = it.generateSortTitle(it.title) ?: 'AAA_Error'
        if ( it.sortTitle != null ) {
          it.save(failOnError:true)
          num_rows_updated++
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sort Package Names Rows_updated:: ${rows_updated} ${num_rows_updated}");
      Package.findAllBySortName(null,[max:100]).each {
        log.debug("Normalise Package Name ${it.name}");
        it.sortname = it.generateSortName(it.name) ?: 'AAA_Error'
        if ( it.sortname != null ) {
          it.save(failOnError:true)
          num_rows_updated++
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sortable License References Rows_updated:: ${rows_updated} ${num_rows_updated}");
      License.findAllBySortableReference(null,[max:100]).each {
        log.debug("Normalise License Reference Name ${it.reference}");
        it.sortableReference = it.generateSortableReference(it.reference) ?: 'AAA_Error'
        if( it.sortableReference != null ) {
          it.save(failOnError:true)
          num_rows_updated++
          rows_updated = true
        }
      }
      
      log.debug("Rows_updated:: ${rows_updated} ${num_rows_updated}");

      globalService.cleanUpGorm();
    }

    log.debug("Completed normalisation step... updated ${rows_updated} rows in ${System.currentTimeMillis()-sort_str_start_time}ms");
  }

    /**
     * Drops an old domain index and reinitialises it. An eventually running job is being cancelled; execution
     * is done if the job could be cancelled.
     * The new index is being rebuilt right after resetting
     */
    def clearDownAndInitES() {
        log.debug("Clear down and init ES");

        RestHighLevelClient client = ESWrapperService.getClient()
        SystemEvent.createEvent('YODA_ES_RESET_START')

        if(ESWrapperService.testConnection()) {

            if (!(activeFuture) || (activeFuture && activeFuture.cancel(true))) {
                Collection esIndicesNames = ESWrapperService.ES_Indices.values() ?: []

                esIndicesNames.each { String indexName ->
                    try {
                        boolean isDeletedIndex = ESWrapperService.deleteIndex(indexName)
                        if (isDeletedIndex) {
                            log.debug("Drop old ES index completed OK")
                            SystemEvent.createEvent('YODA_ES_RESET_DROP_OK')
                        } else {
                            log.error("Index wasn't deleted")
                        }
                    }
                    catch (ElasticsearchException e) {
                        if (e.status() == RestStatus.NOT_FOUND) {
                            log.warn("index does not exist ..")
                        } else {
                            log.warn("Problem deleting index ..", e)
                        }

                        SystemEvent.createEvent('FT_INDEX_CLEANUP_ERROR', ["index": indexName])
                    }

                    boolean isCreatedIndex = ESWrapperService.createIndex(indexName)

                    if (isCreatedIndex) {
                        SystemEvent.createEvent('YODA_ES_RESET_CREATE_OK')
                        log.debug("Create ES index completed OK")

                    } else {
                        log.error("Index wasn't created")
                    }
                }

                try {
                    client.close()
                }
                catch (Exception e) {
                    log.error(e.toString())
                }

                log.debug("Do updateFTIndexes");
                updateFTIndexes()

            } else {
                log.debug("!!!!Clear down and init ES is not possible because updateFTIndexes is currently in process!!!!");
            }
        }
        SystemEvent.createEvent('YODA_ES_RESET_END')
    }

    /**
     * Compares the count of database entries for the given domain class with the count of ElasticSearch index entries for the
     * given domain class. The counts are being retained in the FTControl entry for the given domain
     * @param domainClassName the domain class to check the entry counts of
     * @return true if successful, false otherwise
     * @see FTControl
     */
    boolean checkESElementswithDBElements(String domainClassName) {

        RestHighLevelClient esclient = ESWrapperService.getClient()
        Map es_indices = ESWrapperService.ES_Indices

        try {
            if(ESWrapperService.testConnection()) {
                log.debug("Element comparison: ES <-> DB ( ${domainClassName} )")

                FTControl ftControl = FTControl.findByDomainClassName(domainClassName)

                if (ftControl && ftControl.active) {

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
                            ftControl.dbElements = domainClass.findAll().size()
                            ftControl.esElements = countIndex
                            //println(ft.dbElements +' , '+ ft.esElements)

                            if (ftControl.dbElements != ftControl.esElements) {
                                log.debug("+++++ ES NOT COMPLETE FOR ${ftControl.domainClassName}: ES Results = ${ftControl.esElements}, DB Results = ${ftControl.dbElements} +++++")
                                //ft.lastTimestamp = 0
                            }

                            ftControl.save()
                        }
                    }
                log.debug("Completed element comparison: ES <-> DB ( ${domainClassName} )")
            }
        }
        finally {
            try {
                esclient.close()
            }
            catch (Exception e) {
                log.error(e.toString())
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

        RestHighLevelClient esclient = ESWrapperService.getClient()
        Map es_indices = ESWrapperService.ES_Indices

        try {
            if(ESWrapperService.testConnection()) {
                log.debug("Element comparison: ES <-> DB")

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
                            ft.dbElements = domainClass.findAll().size()
                            ft.esElements = countIndex
                            //println(ft.dbElements +' , '+ ft.esElements)

                            if (ft.dbElements != ft.esElements) {
                                log.debug("+++++ ES NOT COMPLETE FOR ${ft.domainClassName}: ES Results = ${ft.esElements}, DB Results = ${ft.dbElements} +++++")
                                //ft.lastTimestamp = 0
                            }

                            ft.save()
                        }
                    }
                }
                log.debug("Completed element comparison: ES <-> DB")
            }
        }
        finally {
            try {
                esclient.close()
            }
            catch (Exception e) {
                log.error(e.toString())
            }
        }
        return true
    }

    String getLastFTIndexUpdateInfo() {
        String info = '?'

        SystemEvent se = SystemEvent.getLastByToken('FT_INDEX_UPDATE_END')
        if (se) {
            info = DateUtils.getLocalizedSDF_noZ().format(se.created)
            if (se.payload) {
                long ms = JSON.parse(se.payload).ms
                if (ms) {
                    info += ' (' + (ms/1000/60).round(2) + ' min.)'
                }
            }
        }
        info
    }

    /**
     * Kills an eventually running process
     */
    public synchronized void killDataloadService() {
        if (activeFuture != null) {
            SystemEvent.createEvent('FT_INDEX_UPDATE_KILLED')
            activeFuture.cancel(true)
            log.debug("killed DataloadService!")
        }
    }
}
