package com.k_int.kbplus

import de.laser.SystemEvent
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.interfaces.CalculatedType
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonOutput
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.flush.FlushRequest
import org.elasticsearch.action.admin.indices.flush.FlushResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.support.replication.ReplicationResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.core.CountResponse
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.hibernate.ScrollMode

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

//@Transactional
class DataloadService {

    def stats = [:]

    def update_stages = [
        'Organisations Data',
        'Subscriptions Offered Data',
        'Subscriptions Taken Data',
        'License Data'
    ]

    ExecutorService executorService
    def ESWrapperService
    def sessionFactory
    def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def grailsApplication

    def es_index
    def dataload_running=false
    def dataload_stage=-1
    def dataload_message=''
    boolean update_running = false
    def lastIndexUpdate = null
    Future activeFuture

    @javax.annotation.PostConstruct
    def init () {
        es_index = ESWrapperService.getESSettings().indexName
    }

    def updateFTIndexes() {
        //log.debug("updateFTIndexes ${this.hashCode()}")
        if(update_running == false) {

            if(!(activeFuture) || activeFuture.isDone()) {

                activeFuture = executorService.submit({
                    Thread.currentThread().setName("DataloadService UpdateFTIndexes")
                    doFTUpdate()
                } as java.util.concurrent.Callable)
                //log.debug("updateFTIndexes returning")
            }else{
                log.debug("FT update already running")
                return false
            }
        } else {
            return false
        log.debug("FT update already running")
        }
    }

    boolean doFTUpdate() {

        SystemEvent.createEvent('FT_INDEX_UPDATE_START')
        synchronized(this) {
            if ( update_running ) {
                return false
                log.debug("Exiting FT update - one already running");
            }
            else {
                update_running = true;
            }
        }
        log.debug("doFTUpdate: Execute IndexUpdateJob starting at ${new Date()}");

        def start_time = System.currentTimeMillis();

        updateES(com.k_int.kbplus.Org.class) { org ->
            def result = [:]

                result._id = org.globalUID
                result.priority = 30
                result.dbId = org.id

                result.gokbId = org.gokbId
                result.guid = org.globalUID ?: ''

                result.name = org.name
                result.status = org.status?.value
                result.statusId = org.status?.id
                result.visible = 'Public'
                result.rectype = org.getClass().getSimpleName()

                result.sector = org.sector?.value

                result.shortname = org.shortname
                result.sortname = org.sortname

                result.identifiers = []
                org.ids?.each { ident ->
                    try {
                        result.identifiers.add([type: ident.ns.ns, value: ident.value])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }

                result.platforms = []
                org.platforms?.each { platform ->
                    try {
                        result.platforms.add([dbId: platform.id, name: platform.name])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }

                result.dateCreated = org.dateCreated
                result.lastUpdated = org.lastUpdated

            result
        }

        updateES(com.k_int.kbplus.TitleInstance.class) { ti ->

            def result = [:]

                if (ti.title != null) {
                    def new_key_title = com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                    if (ti.keyTitle != new_key_title) {
                        ti.normTitle = com.k_int.kbplus.TitleInstance.generateNormTitle(ti.title)
                        ti.keyTitle = com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                        //
                        // This alone should trigger before update to do the necessary...
                        //
                        ti.save()
                    } else {
                    }

                    result._id = ti.globalUID
                    result.priority = 20
                    result.dbId = ti.id

                    result.gokbId = ti.gokbId
                    result.guid = ti.globalUID ?: ''
                    result.name = ti.title
                    result.status = ti.status?.value
                    result.statusId = ti.status?.id
                    result.visible = 'Public'
                    result.rectype = ti.getClass().getSimpleName()

                    //result.keyTitle = ti.keyTitle
                    //result.normTitle = ti.normTitle
                    result.publisher = ti.getPublisher()?.name ?: ''
                    result.sortTitle = ti.sortTitle

                    result.typTitle = ti.medium?.value

                    result.identifiers = []
                    ti.ids?.each { ident ->
                        try {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        } catch (Exception e) {
                            log.error(e)
                        }
                    }
                    //result.keyTitle = ti.keyTitle
                    //result.normTitle = ti.normTitle

                    result.dateCreated = ti.dateCreated
                    result.lastUpdated = ti.lastUpdated

                } else {
                    log.warn("Title with no title string - ${ti.id}")
                }

            result
        }

        updateES(com.k_int.kbplus.Package.class) { pkg ->
            def result = [:]

                result._id = pkg.globalUID
                result.priority = 30
                result.dbId = pkg.id
                result.gokbId = pkg.gokbId
                result.guid = pkg.globalUID ?: ''
                result.name = "${pkg.name}"
                result.status = pkg.packageStatus?.value
                result.statusId = pkg.packageStatus?.id
                result.visible = 'Public'
                result.rectype = pkg.getClass().getSimpleName()

                result.consortiaGUID = pkg.getConsortia()?.globalUID
                result.consortiaName = pkg.getConsortia()?.name
                result.providerId = pkg.getContentProvider()?.id
                result.providerName = pkg.getContentProvider()?.name

                result.nominalPlatformId = pkg.nominalPlatform?.id
                result.nominalPlatformName = pkg.nominalPlatform?.name

                result.isPublic = (pkg?.isPublic) ? 'Yes' : 'No'

                result.sortname = pkg.sortName
                result.startDate = pkg.startDate
                result.endDate = pkg.endDate
                def lastmod = pkg.lastUpdated ?: pkg.dateCreated
                if (lastmod != null) {
                    result.lastModified = lastmod
                }

                result.titleCount = pkg.tipps.size()?:0
                result.titleCountCurrent = pkg.getCurrentTipps().size()?:0

                result.identifiers = []
                pkg.ids?.each { ident ->
                    try {
                        result.identifiers.add([type: ident.ns.ns, value: ident.value])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }
                result.dateCreated = pkg.dateCreated
                result.lastUpdated = pkg.lastUpdated

/*                if (pkg.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(pkg.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                    result.startYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH)) + 1}"
                }

                if (pkg.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(pkg.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                    result.endYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH)) + 1}"
                }*/
            result
        }

        updateES(com.k_int.kbplus.Platform.class) { plat ->
            def result = [:]

                result._id = plat.globalUID
                result.priority = 30
                result.dbId = plat.id
                result.gokbId = plat.gokbId
                result.guid = plat.globalUID ?: ''
                result.name = plat.name
                result.status = plat.status?.value
                result.statusId = plat.status?.id
                result.visible = 'Public'
                result.rectype = plat.getClass().getSimpleName()

                result.normname = plat.normname
                result.primaryUrl = plat.primaryUrl
                result.orgId = plat.org?.id
                result.orgName = plat.org?.name
                result.titleCountCurrent = plat.getCurrentTipps().size()?:0

                result.dateCreated = plat.dateCreated
                result.lastUpdated = plat.lastUpdated

            result
        }

        updateES(com.k_int.kbplus.License.class) { lic ->
            def result = [:]

            result._id = lic.globalUID
            result.priority = 50
            result.dbId = lic.id
            result.guid = lic.globalUID ?:''
            result.name = lic.reference
            result.visible = 'Private'
            result.rectype = lic.getClass().getSimpleName()

            switch(lic.getCalculatedType()) {
                case CalculatedType.TYPE_CONSORTIAL:
                    //result.availableToOrgs = lic.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSING_CONSORTIUM.value]}?.org?.id
                    result.availableToOrgs = Org.executeQuery("select oo.org.id from OrgRole oo where concat('"+Subscription.class.name+":',oo.sub.id) in (select li.destination from Links li where li.source = :lic and li.linkType = :linkType) and oo.roleType = :roleType",[lic:GenericOIDService.getOID(lic),linkType:RDStore.LINKTYPE_LICENSE,roleType:RDStore.OR_SUBSCRIPTION_CONSORTIA])
                    result.membersCount = License.findAllByInstanceOf(lic).size()?:0
                    break
                case CalculatedType.TYPE_PARTICIPATION:
                    //List orgs = lic.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSEE_CONS.value]}?.org
                    List orgs = Org.executeQuery("select oo.org from OrgRole oo where concat('"+Subscription.class.name+":',oo.sub.id) in (select li.destination from Links li where li.source = :lic and li.linkType = :linkType) and oo.roleType in (:roleType)",[lic:GenericOIDService.getOID(lic),linkType:RDStore.LINKTYPE_LICENSE,roleType:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
                    result.availableToOrgs = orgs.collect{ Org org -> org.id }
                    result.consortiaGUID = lic.getLicensingConsortium()?.globalUID
                    result.consortiaName = lic.getLicensingConsortium()?.name

                    result.members = []
                    orgs.each{ org ->
                        result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                    }
                    break
                case CalculatedType.TYPE_LOCAL:
                    //result.availableToOrgs = lic.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSEE.value]}?.org?.id
                    result.availableToOrgs = Org.executeQuery("select oo.org.id from OrgRole oo where concat('"+Subscription.class.name+":',oo.sub.id) in (select li.destination from Links li where li.source = :lic and li.linkType = :linkType) and oo.roleType = :roleType",[lic:GenericOIDService.getOID(lic),linkType:RDStore.LINKTYPE_LICENSE,roleType:RDStore.OR_SUBSCRIBER])
                    break
            }

            result.typeId = lic.type?.id

            result.identifiers = []
            lic.ids?.each { ident ->
                try {
                    result.identifiers.add([type: ident.ns.ns, value: ident.value])
                } catch (Exception e) {
                    log.error(e)
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

        updateES( com.k_int.kbplus.Subscription.class) { sub ->
            def result = [:]

                result._id = sub.globalUID
                result.priority = 70
                result.dbId = sub.id
                result.guid = sub.globalUID ?: ''
                result.name = sub.name
                result.status = sub.status?.value
                result.statusId = sub.status?.id
                result.visible = 'Private'
                result.rectype = sub.getClass().getSimpleName()

                switch (sub.getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                        result.membersCount = Subscription.findAllByInstanceOf(sub).size() ?:0
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        List orgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value, RDStore.OR_SUBSCRIBER_COLLECTIVE.value]}?.org
                        result.availableToOrgs = orgs?.id
                        result.consortiaGUID = sub.getConsortia()?.globalUID
                        result.consortiaName = sub.getConsortia()?.name

                        result.members = []
                        orgs.each{ org ->
                            result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                        }

                        break
                    case CalculatedType.TYPE_COLLECTIVE:
                        result.availableToOrgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value]}?.org?.id
                        break
                /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                    case CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE:
                        List orgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value, RDStore.OR_SUBSCRIBER_CONS.value]}?.org
                        result.availableToOrgs = orgs?.id
                        result.consortiaGUID = sub.getConsortia()?.globalUID
                        result.consortiaName = sub.getConsortia()?.name

                        result.members = []
                        orgs.each{ org ->
                            result.members.add([dbId: org.id, name: org.name, shortname: org.shortname, sortname: org.sortname])
                        }
                        break
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = sub.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER.value]}?.org?.id
                        break
                }

                result.typeId = sub.type?.id

                result.identifiers = []
                sub.ids?.each { ident ->
                        try {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        } catch (Exception e) {
                            log.error(e)
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

        updateES(com.k_int.kbplus.SurveyConfig.class) { surveyConfig ->
            def result = [:]

            result._id = surveyConfig.getClass().getSimpleName().toLowerCase()+":"+surveyConfig.id
            result.priority = 60
            result.dbId = surveyConfig.id
            result.name = surveyConfig.getSurveyName()
            result.status= surveyConfig.surveyInfo.status?.value
            result.statusId= surveyConfig.surveyInfo.status?.id
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

        updateES(com.k_int.kbplus.SurveyOrg.class) { surOrg ->
            def result = [:]

            result._id = surOrg.getClass().getSimpleName().toLowerCase()+":"+surOrg.id
            result.priority = 60
            result.dbId = surOrg.surveyConfig.id
            result.name = surOrg.surveyConfig.getSurveyName()
            result.status= surOrg.surveyConfig.surveyInfo.status?.value
            result.statusId= surOrg.surveyConfig.surveyInfo.status?.id
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

        updateES(com.k_int.kbplus.Task.class) { task ->
            def result = [:]

            result._id = task.getClass().getSimpleName().toLowerCase()+":"+task.id
            result.priority = 40
            result.dbId = task.id
            result.name = task.title
            result.status= task.status?.value
            result.statusId= task.status?.id
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
                result.objectTypeId = task.license.type?.id
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

        updateES(com.k_int.kbplus.DocContext.class) { docCon ->
            def result = [:]

            result._id = docCon.getClass().getSimpleName().toLowerCase()+":"+docCon.id
            result.priority = 40
            result.dbId = docCon.id
            result.name = docCon.owner?.title ?: ''
            result.status= docCon.status?.value ?: ''
            result.statusId= docCon.status?.id ?: ''
            result.visible = 'Private'
            result.rectype = (docCon.owner?.contentType == 0) ? 'Note' : 'Document'

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
                result.objectTypeId = docCon.license.type?.id
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

        updateES(com.k_int.kbplus.IssueEntitlement.class) { ie ->
            def result = [:]

            result._id = ie.globalUID
            result.priority = 45
            result.dbId = ie.id
            result.name = ie.tipp?.title?.title
            result.status= ie.status?.value
            result.statusId= ie.status?.id
            result.visible = 'Private'
            result.rectype = ie.getClass().getSimpleName()

            switch (ie.subscription.getCalculatedType()) {
                case CalculatedType.TYPE_CONSORTIAL:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                    break
                case CalculatedType.TYPE_PARTICIPATION:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value, RDStore.OR_SUBSCRIBER_COLLECTIVE.value]}?.org?.id
                    break
                case CalculatedType.TYPE_COLLECTIVE:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value]}?.org?.id
                    break
            /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                              result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                              break*/
                case CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE:
                    result.availableToOrgs = ie.subscription.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value, RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                    break
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

        updateES(SubscriptionProperty.class) { SubscriptionProperty subProp ->
            def result = [:]

            result._id = subProp.getClass().getSimpleName().toLowerCase()+":"+subProp.id
            result.priority = 45
            result.dbId = subProp.id
            result.name = subProp.type?.name

            result.visible = 'Private'
            result.rectype = subProp.getClass().getSimpleName()

            if(subProp.type.type == Integer.toString()){
                result.description = subProp.intValue
            }
            else if(subProp.type.type == String.toString()){
                result.description = subProp.stringValue
            }
            else if(subProp.type.type == BigDecimal.toString()){
                result.description = subProp.decValue
            }
            else if(subProp.type.type == Date.toString()){
                result.description = subProp.dateValue
            }
            else if(subProp.type.type == URL.toString()){
                result.description = subProp.urlValue
            }
            else if(subProp.type.type == RefdataValue.toString()){
                result.description = subProp.refValue?.value
            }

            if(subProp.isPublic) {
                switch (subProp.owner.getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_CONSORTIA.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value, RDStore.OR_SUBSCRIBER_COLLECTIVE.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_COLLECTIVE:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value]}?.org?.id
                        break
                /*              case CalculatedType.TYPE_ADMINISTRATIVE:
                                  result.availableToOrgs = sub.orgRelations.findAll {it.roleType.value in [RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                                  break*/
                    case CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE:
                        result.availableToOrgs = subProp.owner.orgRelations.findAll{it.roleType.value in [RDStore.OR_SUBSCRIPTION_COLLECTIVE.value, RDStore.OR_SUBSCRIBER_CONS.value]}?.org?.id
                        break
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

        /*
        updateES(SubscriptionPrivateProperty.class) { subPrivProp ->
            def result = [:]

            result._id = subPrivProp.getClass().getSimpleName().toLowerCase()+":"+subPrivProp.id
            result.priority = 45
            result.dbId = subPrivProp.id
            result.name = subPrivProp.type?.name

            result.visible = 'Private'
            result.rectype = subPrivProp.getClass().getSimpleName()

            if(subPrivProp.type.type == Integer.toString()){
                result.description = subPrivProp.intValue
            }
            else if(subPrivProp.type.type == String.toString()){
                result.description = subPrivProp.stringValue
            }
            else if(subPrivProp.type.type == BigDecimal.toString()){
                result.description = subPrivProp.decValue
            }
            else if(subPrivProp.type.type == Date.toString()){
                result.description = subPrivProp.dateValue
            }
            else if(subPrivProp.type.type == URL.toString()){
                result.description = subPrivProp.urlValue
            }
            else if(subPrivProp.type.type == RefdataValue.toString()){
                result.description = subPrivProp.refValue?.value
            }



            if(subPrivProp.owner){
                result.objectId = subPrivProp.owner.id
                result.objectName = subPrivProp.owner.name
                result.objectTypeId = subPrivProp.owner.type?.id
                result.objectClassName = subPrivProp.owner.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = subPrivProp.dateCreated
            result.lastUpdated = subPrivProp.lastUpdated

            result
        }
         */

        updateES(LicenseProperty.class) { LicenseProperty licProp ->
            def result = [:]

            result._id = licProp.getClass().getSimpleName().toLowerCase()+":"+licProp.id
            result.priority = 45
            result.dbId = licProp.id
            result.name = licProp.type?.name

            result.visible = 'Private'
            result.rectype = licProp.getClass().getSimpleName()

            if(licProp.type.type == Integer.toString()){
                result.description = licProp.intValue
            }
            else if(licProp.type.type == String.toString()){
                result.description = licProp.stringValue
            }
            else if(licProp.type.type == BigDecimal.toString()){
                result.description = licProp.decValue
            }
            else if(licProp.type.type == Date.toString()){
                result.description = licProp.dateValue
            }
            else if(licProp.type.type == URL.toString()){
                result.description = licProp.urlValue
            }
            else if(licProp.type.type == RefdataValue.toString()){
                result.description = licProp.refValue?.value
            }

            if(licProp.isPublic) {
                switch(licProp.owner.getCalculatedType()) {
                    case CalculatedType.TYPE_CONSORTIAL:
                        result.availableToOrgs = licProp.owner.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSING_CONSORTIUM.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_PARTICIPATION:
                        result.availableToOrgs = licProp.owner.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSEE_CONS.value]}?.org?.id
                        break
                    case CalculatedType.TYPE_LOCAL:
                        result.availableToOrgs = licProp.owner.orgLinks.findAll{it.roleType?.value in [RDStore.OR_LICENSEE.value]}?.org?.id
                        break
                }
            }
            else result.availableToOrgs = [licProp.type.tenant?.id ?: 0]

            if(licProp.owner){
                result.objectId = licProp.owner.id
                result.objectName = licProp.owner.reference
                result.objectTypeId = licProp.owner.type?.id
                result.objectClassName = licProp.owner.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = licProp.dateCreated
            result.lastUpdated = licProp.lastUpdated

            result
        }

        /*
        updateES( LicensePrivateProperty.class) { licPrivProp ->
            def result = [:]

            result._id = licPrivProp.getClass().getSimpleName().toLowerCase()+":"+licPrivProp.id
            result.priority = 45
            result.dbId = licPrivProp.id
            result.name = licPrivProp.type?.name

            result.visible = 'Private'
            result.rectype = licPrivProp.getClass().getSimpleName()

            if(licPrivProp.type.type == Integer.toString()){
                result.description = licPrivProp.intValue
            }
            else if(licPrivProp.type.type == String.toString()){
                result.description = licPrivProp.stringValue
            }
            else if(licPrivProp.type.type == BigDecimal.toString()){
                result.description = licPrivProp.decValue
            }
            else if(licPrivProp.type.type == Date.toString()){
                result.description = licPrivProp.dateValue
            }
            else if(licPrivProp.type.type == URL.toString()){
                result.description = licPrivProp.urlValue
            }
            else if(licPrivProp.type.type == RefdataValue.toString()){
                result.description = licPrivProp.refValue?.value
            }

            result.availableToOrgs = [licPrivProp.type.tenant?.id ?: 0]


            if(licPrivProp.owner){
                result.objectId = licPrivProp.owner.id
                result.objectName = licPrivProp.owner.reference
                result.objectTypeId = licPrivProp.owner.type?.id
                result.objectClassName = licPrivProp.owner.getClass().getSimpleName().toLowerCase()
            }

            result.dateCreated = licPrivProp.dateCreated
            result.lastUpdated = licPrivProp.lastUpdated

            result
        }
        */

        RestHighLevelClient esclient = ESWrapperService.getClient()
        update_running = false
        def elapsed = System.currentTimeMillis() - start_time;
        lastIndexUpdate = new Date(System.currentTimeMillis())
        FlushRequest request = new FlushRequest(es_index);
        FlushResponse flushResponse = esclient.indices().flush(request, RequestOptions.DEFAULT)

        log.debug("IndexUpdateJob completed in ${elapsed}ms at ${new Date()} ")
        SystemEvent.createEvent('FT_INDEX_UPDATE_END')

        esclient.close()

        checkESElementswithDBElements()

        return true
    }

    def updateES( domain, recgen_closure) {

    RestHighLevelClient esclient = ESWrapperService.getClient()

    def count = 0;
    def total = 0;
    try {
        //log.debug("updateES - ${domain.name}")

        def highest_timestamp = 0;
        def highest_id = 0;

        def latest_ft_record = FTControl.findByDomainClassNameAndActivity(domain.name,'ESIndex')


        if (! latest_ft_record) {
            latest_ft_record=new FTControl(domainClassName:domain.name,activity:'ESIndex', lastTimestamp:0)
        } else {
            highest_timestamp = latest_ft_record.lastTimestamp
            //log.debug("Got existing ftcontrol record for ${domain.name} max timestamp is ${highest_timestamp} which is ${new Date(highest_timestamp)}");
        }
        if(latest_ft_record.active) {

            //log.debug("result of findByDomain: ${latest_ft_record}")

            log.debug("updateES ${domain.name} since ${new Date(latest_ft_record.lastTimestamp)}")
            Date from = new Date(latest_ft_record.lastTimestamp)
            // def qry = domain.findAllByLastUpdatedGreaterThan(from,[sort:'lastUpdated'])

            def c = domain.createCriteria()
            c.setReadOnly(true)
            c.setCacheable(false)
            c.setFetchSize(Integer.MIN_VALUE)


            Class domainClass = grailsApplication.getDomainClass(domain.name).clazz
            if (org.apache.commons.lang.ClassUtils.getAllInterfaces(domainClass).contains(CalculatedLastUpdated)) {
                c.buildCriteria {
                    or {
                        and {
                            isNotNull('lastUpdatedCascading')
                            gt('lastUpdatedCascading', from)
                        }
                        gt('lastUpdated', from)
                        and {
                            gt('dateCreated', from)
                            isNull('lastUpdated')
                        }
                    }
                    order("lastUpdated", "asc")
                }
            } else {
                c.buildCriteria {
                    or {
                        gt('lastUpdated', from)
                        and {
                            gt('dateCreated', from)
                            isNull('lastUpdated')
                        }
                    }
                    order("lastUpdated", "asc")
                }
            }

            def results = c.scroll(ScrollMode.FORWARD_ONLY)

            //log.debug("Query completed .. processing rows ..")

            String rectype
            while (results.next()) {
                Object r = results.get(0);
                def idx_record = recgen_closure(r)
                def future
                if (idx_record['_id'] == null) {
                    log.error("******** Record without an ID: ${idx_record} Obj:${r} ******** ")
                    continue
                }

                def recid = idx_record['_id'].toString()
                idx_record.remove('_id');

                IndexRequest request = new IndexRequest(es_index);
                request.id(recid);
                String jsonString = idx_record as JSON
                //String jsonString = JsonOutput.toJson(idx_record)
                //println(jsonString)
                request.source(jsonString, XContentType.JSON)

                IndexResponse indexResponse = esclient.index(request, RequestOptions.DEFAULT);

                String index = indexResponse.getIndex();
                String id = indexResponse.getId();
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    //log.debug("CREATED ${domain.name}")
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    //log.debug("UPDATED ${domain.name}")
                } else {
                    log.debug("ELSE ${domain.name}: ${indexResponse.getResult()}")
                }
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure :
                            shardInfo.getFailures()) {
                        String reason = failure.reason();
                        println(reason)
                    }
                }

                //latest_ft_record.lastTimestamp = r.lastUpdated?.getTime()
                if (r.lastUpdated?.getTime() > highest_timestamp) {
                    highest_timestamp = r.lastUpdated?.getTime();
                }

                count++
                total++
                if (count == 100) {
                    count = 0;
                    log.debug("processed ${total} records (${domain.name})")
                    latest_ft_record.lastTimestamp = highest_timestamp
                    latest_ft_record.esElements = latest_ft_record.esElements ?: 0
                    latest_ft_record.dbElements = latest_ft_record.dbElements ?: 0
                    latest_ft_record.save(flush: true);
                    cleanUpGorm();
                }
            }
            results.close();

            log.debug("Processed ${total} records for ${domain.name}")

            // update timestamp
            latest_ft_record.lastTimestamp = highest_timestamp

            latest_ft_record.esElements = latest_ft_record.esElements ?: 0
            latest_ft_record.dbElements = latest_ft_record.dbElements ?: 0
            latest_ft_record.save(flush: true);
        }else{
            log.debug("updateES ${domain.name}: FTControle is not active")
        }

    }
    catch ( Exception e ) {
      log.error("Problem with FT index", e)

        SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["index": domain.name])
    }
    finally {
      log.debug("Completed processing on ${domain.name} - saved ${total} records")
        try {
            esclient.close()
        }
        catch ( Exception e ) {
            log.error("Problem by Close ES Client",e);
        }
    }

  }

    def lookupOrCreateCanonicalIdentifier(ns, value) {
        // TODO [ticket=1789]
        log.debug("lookupOrCreateCanonicalIdentifier(${ns},${value})");
        //def namespace = IdentifierNamespace.findByNs(ns) ?: new IdentifierNamespace(ns:ns).save();
        //Identifier.findByNsAndValue(namespace,value) ?: new Identifier(ns:namespace, value:value).save();
        Identifier.construct([value:value, reference:null, namespace:ns])
  }

  def dataCleanse() {
    log.debug("dataCleanse");
    def future = executorService.submit({
      doDataCleanse()
    } as java.util.concurrent.Callable)
    log.debug("dataCleanse returning");
  }

  def doDataCleanse() {
    log.debug("dataCleansing");
    // 1. Find all packages that do not have a nominal platform
    Package.findAllByNominalPlatformIsNull().each { p ->
      def platforms = [:]
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
      p.save(flush:true)


    }

    // Fill out any missing sort keys on titles, packages or licenses
    def num_rows_updated = 0
    def sort_str_start_time = System.currentTimeMillis()
    def rows_updated = true

    while ( rows_updated ) {
      rows_updated = false

      TitleInstance.findAllBySortTitle(null,[max:100]).each {
        log.debug("Normalise Title ${it.title}");
        it.sortTitle = it.generateSortTitle(it.title) ?: 'AAA_Error'
        if ( it.sortTitle != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sort Package Names Rows_updated:: ${rows_updated} ${num_rows_updated}");
      Package.findAllBySortName(null,[max:100]).each {
        log.debug("Normalise Package Name ${it.name}");
        it.sortName = it.generateSortName(it.name) ?: 'AAA_Error'
        if ( it.sortName != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sortable License References Rows_updated:: ${rows_updated} ${num_rows_updated}");
      License.findAllBySortableReference(null,[max:100]).each {
        log.debug("Normalise License Reference Name ${it.reference}");
        it.sortableReference = it.generateSortableReference(it.reference) ?: 'AAA_Error'
        if( it.sortableReference != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }
      
      log.debug("Rows_updated:: ${rows_updated} ${num_rows_updated}");

      cleanUpGorm()
    }

    log.debug("Completed normalisation step... updated ${rows_updated} rows in ${System.currentTimeMillis()-sort_str_start_time}ms");

  }
    def cleanUpGorm() {
        log.debug("Clean up GORM")

        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def clearDownAndInitES() {
        log.debug("Clear down and init ES");

        RestHighLevelClient client = ESWrapperService.getClient()
        SystemEvent.createEvent('YODA_ES_RESET_START')

        if(!(activeFuture) || (activeFuture && activeFuture.cancel(false))) {
            try {
                // Drop any existing kbplus index
                log.debug("Dropping old ES index ..")
                DeleteIndexRequest deleteRequest = new DeleteIndexRequest(es_index)
                def deleteIndexResponse = client.indices().delete(deleteRequest, RequestOptions.DEFAULT)
                boolean acknowledged = deleteIndexResponse.isAcknowledged()
                if (acknowledged) {
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

                SystemEvent.createEvent('FT_INDEX_CLEANUP_ERROR', ["index": es_index])
            }

            log.debug("Create new ES index ..")
            //def createResponse = client.admin().indices().prepareCreate(es_index).get()
            CreateIndexRequest createRequest = new CreateIndexRequest(es_index)

            def es_mapping = ESWrapperService.getESMapping()
            //println(es_mapping)

            createRequest.mapping(JsonOutput.toJson(es_mapping), XContentType.JSON)

            CreateIndexResponse createIndexResponse = client.indices().create(createRequest, RequestOptions.DEFAULT)
            boolean acknowledgedCreate = createIndexResponse.isAcknowledged()
            if (acknowledgedCreate) {
                SystemEvent.createEvent('YODA_ES_RESET_CREATE_OK')
                log.debug("Create ES index completed OK")
                log.debug("manual start full text index")

                log.debug("Delete all existing FT Control entries");
                FTControl.withTransaction {
                    FTControl.executeUpdate("delete FTControl c")
                }

                updateFTIndexes()
            } else {
                log.error("Index wasn't created")
            }

            //log.debug("Clear down and init ES completed...")
            client.close()
        }else{
            log.debug("!!!!Clear down and init ES is not possible because updateFTIndexes is currently in process!!!!");
        }
        SystemEvent.createEvent('YODA_ES_RESET_END')
    }

    boolean checkESElementswithDBElements() {

        log.debug("Begin to check ES Elements with DB Elements")

        RestHighLevelClient esclient = ESWrapperService.getClient()

        try {
            FTControl.list().each { ft ->

                if (ft.active) {

                    Class domainClass = grailsApplication.getDomainClass(ft.domainClassName).clazz

                    String query_str = "rectype: '${ft.domainClassName.replaceAll("com.k_int.kbplus.", "")}'"

                    if (ft.domainClassName == DocContext.name) {
                        query_str = "rectype:'Note' OR rectype:'Document'"
                    }

                    if (ft.domainClassName == TitleInstance.name) {
                        query_str = "rectype:'TitleInstance' OR rectype:'BookInstance' OR rectype:'JournalInstance' OR rectype:'DatabaseInstance'"
                    }

                    //println(query_str)

                    String index = ESWrapperService.getESSettings().indexName

                    CountRequest countRequest = new CountRequest(index);
                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
                    countRequest.source(searchSourceBuilder);

                    CountResponse countResponse = esclient.count(countRequest, RequestOptions.DEFAULT)

                    ft.dbElements = domainClass.findAll().size()
                    ft.esElements = countResponse ? countResponse.getCount().toInteger() : 0

                    //println(ft.dbElements +' , '+ ft.esElements)

                    if (ft.dbElements != ft.esElements) {
                        log.debug("****ES NOT COMPLETE FOR ${ft.domainClassName}: ES Results = ${ft.esElements}, DB Results = ${ft.dbElements} -> RESET lastTimestamp****")
                        //ft.lastTimestamp = 0
                    }

                    ft.save(flush: true)


                }
            }
        }
            finally {
                try {
                    esclient.close()
                }
                catch (Exception e) {
                    log.error("Problem by Close ES Client", e);
                }
            }

        log.debug("End to check ES Elements with DB Elements")

        return true

    }

    public synchronized void killDataloadService() {
        if (activeFuture != null) {
            activeFuture.cancel(true)
            log.debug("kill DataloadService done!")
        }
    }

}
