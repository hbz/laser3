package de.laser

import com.k_int.kbplus.ChangeNotificationService
import com.k_int.kbplus.CreatorTitle
import com.k_int.kbplus.Fact
import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.OrgAccessPointLink
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Package
import com.k_int.kbplus.PersonRole
import com.k_int.kbplus.Platform
import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.TitleHistoryEventParticipant
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.GlobalRecordSource
import com.k_int.kbplus.GlobalSourceSyncService
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.exceptions.CleanupException
import de.laser.helper.ConfigUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional
import grails.util.Holders
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.transaction.TransactionStatus

//@CompileStatic
//@Transactional
class YodaService {

    GrailsApplication grailsApplication
    def sessionRegistry = Holders.grailsApplication.mainContext.getBean('sessionRegistry')
    def contextService = Holders.grailsApplication.mainContext.getBean('contextService')
    GlobalSourceSyncService globalSourceSyncService = Holders.grailsApplication.mainContext.getBean('globalSourceSyncService')
    DeletionService deletionService
    GOKbService gokbService = Holders.grailsApplication.mainContext.getBean('GOKbService')
    GenericOIDService genericOIDService = Holders.grailsApplication.mainContext.getBean('genericOIDService')
    ChangeNotificationService changeNotificationService = Holders.grailsApplication.mainContext.getBean('changeNotificationService')
    LinkGenerator grailsLinkGenerator = Holders.grailsApplication.mainContext.getBean(LinkGenerator)

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigUtils.getShowDebugInfo() )
    }

    int getNumberOfActiveUsers() {
        getActiveUsers( (1000 * 60 * 10) ).size() // 10 minutes
    }

    List getActiveUsers(long ms) {
        List result = []

        sessionRegistry.getAllPrincipals().each { user ->
            List lastAccessTimes = []

            sessionRegistry.getAllSessions(user, false).each { userSession ->
                if (user.username == contextService.getUser()?.username) {
                    userSession.refreshLastRequest()
                }
                lastAccessTimes << userSession.getLastRequest().getTime()
            }
            if (lastAccessTimes.max() > System.currentTimeMillis() - ms) {
                result.add(user)
            }
        }
        result
    }

    Map<String,Object> listDuplicatePackages() {
        List<Package> pkgDuplicates = Package.executeQuery('select pkg from Package pkg where pkg.gokbId in (select p.gokbId from Package p group by p.gokbId having count(p.gokbId) > 1)')
        pkgDuplicates.addAll(Package.findAllByGokbIdIsNullOrGokbIdLike(RDStore.GENERIC_NULL_VALUE.value))
        Map<String,List<Package>> result = [pkgDuplicates: pkgDuplicates]
        if(pkgDuplicates) {
            log.debug("located package duplicates")
            List<Package> pkgDupsWithTipps = Package.executeQuery('select distinct(tipp.pkg) from TitleInstancePackagePlatform tipp where tipp.pkg in (:pkg) and tipp.status != :deleted',[pkg:pkgDuplicates,deleted:RDStore.TIPP_STATUS_DELETED])
            List<Package> pkgDupsWithoutTipps = []
            pkgDuplicates.each { pkg ->
                if(!pkgDupsWithTipps.contains(pkg))
                    pkgDupsWithoutTipps << pkg
            }
            result.pkgDupsWithTipps = pkgDupsWithTipps
            result.pkgDupsWithoutTipps = pkgDupsWithoutTipps
        }
        result
    }

    void executePackageCleanup(List<Long> toDelete) {
        toDelete.each { pkgId ->
            Package pkg = Package.get(pkgId)
            DeletionService.deletePackage(pkg)
        }
    }

    Map<String,Object> listDuplicateTitles() {
        Map<String,Object> result = [:]
        List rows = TitleInstance.executeQuery('select ti.gokbId,count(ti.gokbId) from TitleInstance ti group by ti.gokbId having count(ti.gokbId) > 1')
        Map<String,List<TitleInstance>> duplicateRows = [:]
        rows.each { row ->
            duplicateRows << ["${row[0]}":TitleInstance.findAllByGokbId(row[0])]
        }
        result = checkTitleData(duplicateRows)
        int phase = 2
        if(result.nextPhase.size() > 0) {
            println("----------- passing on to phase ${phase} -----------")
            println("Titles in phase ${phase}: ${result.nextPhase}")
            duplicateRows.putAll(result.nextPhase)
            Map<String,Object> nextPhase = checkTitleData(result.nextPhase)
            result.missingTitles.addAll(nextPhase.missingTitles)
            result.mergingTitles.addAll(nextPhase.mergingTitles)
            result.tippMergers.addAll(nextPhase.tippMergers)
            result.remappingTitles.addAll(nextPhase.remappingTitles)
            result.titlesWithoutTIPPs.addAll(nextPhase.titlesWithoutTIPPs)
            while(nextPhase.nextPhase.size() > 0){
                phase++
                println("----------- passing on to phase ${phase} -----------")
                println("Titles in phase ${phase}: ${nextPhase.nextPhase}")
                duplicateRows.putAll(result.nextPhase)
                nextPhase = checkTitleData(nextPhase.nextPhase)
                result.missingTitles.addAll(nextPhase.missingTitles)
                result.mergingTitles.addAll(nextPhase.mergingTitles)
                result.tippMergers.addAll(nextPhase.tippMergers)
                result.remappingTitles.addAll(nextPhase.remappingTitles)
                result.titlesWithoutTIPPs.addAll(nextPhase.titlesWithoutTIPPs)
            }
        }
        result.duplicateRows = duplicateRows
        result
    }

    Map<String,Object> checkTitleData(duplicateRows) {
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        globalSourceSyncService.setSource(grs)
        Set<String> missingTitles = []
        List<String> titlesWithoutTIPPs = [], considered = []
        List<Map<String,String>> mergingTitles = [], remappingTitles = [], tippMergers = []
        Map<String,List<TitleInstance>> nextPhase = [:]
        Map<String,GPathResult> oaiRecords = [:]
        duplicateRows.eachWithIndex { String tiKey, List<TitleInstance> titleInstances, int ctr ->
            boolean crossed = false
            titleInstances.each { TitleInstance titleA ->
                println("attempt get with link ${grs.uri}?verb=GetRecord&metadataPrefix=${grs.fullPrefix}&identifier=${titleA.gokbId} ...")
                GPathResult titleB
                GPathResult oaiRecord = oaiRecords.get(titleA.gokbId)
                if(!oaiRecord) {
                    oaiRecord = globalSourceSyncService.fetchRecord(grs.uri,'titles',[verb:'GetRecord',metadataPrefix:grs.fullPrefix,identifier:titleA.gokbId])
                    if(oaiRecord)
                        oaiRecords.put(titleA.gokbId,oaiRecord)
                }
                if(oaiRecord && oaiRecord.record.metadata.gokb.title) {
                    titleB = oaiRecord.record.metadata.gokb.title
                    println("processing record for #${ctr} (${grailsLinkGenerator.link(controller:'title',action:'show',id:titleA.id)}), we crossed it already: ${crossed}, GOKb record name is ${titleB.name.text()}")
                    if(titleA.title != titleB.name.text()) {
                        crossed = true
                        println("Title mismatch! ${titleA.title} vs. ${titleB.name.text()}! Get correct key for LAS:eR title!")
                        if(titleA.tipps.size() > 0) {
                            TitleInstancePackagePlatform referenceTIPP = titleA.tipps[0]
                            GPathResult packageOAI = oaiRecords.get(referenceTIPP.pkg.gokbId)
                            if(!packageOAI) {
                                packageOAI = globalSourceSyncService.fetchRecord(grs.uri,'packages',[verb:'GetRecord',metadataPrefix:grs.fullPrefix,identifier:referenceTIPP.pkg.gokbId])
                                if(packageOAI)
                                    oaiRecords.put(referenceTIPP.pkg.gokbId,packageOAI)
                            }
                            if(packageOAI && packageOAI.record.metadata.gokb.package) {
                                GPathResult referenceGOKbTIPP = packageOAI.record.metadata.gokb.package.TIPPs.TIPP.find { tipp ->
                                    tipp.@uuid.text() == referenceTIPP.gokbId
                                }
                                if(referenceGOKbTIPP) {
                                    String guessedCorrectTitleKey = referenceGOKbTIPP.title.@uuid.text()
                                    //check if titleB's key (NOT NECESSARILY the correct instance itself!) is not already existing in LAS:eR
                                    TitleInstance titleC = TitleInstance.findByGokbId(guessedCorrectTitleKey)
                                    //the KEY is already taken in LAS:eR! Do further check!
                                    if(titleC) {
                                        println("GOKb key ${guessedCorrectTitleKey} already taken! Check if merge or remapping is necessary!")
                                        boolean nameCriteria = titleB.name.text() == titleC.title
                                        boolean idCriteria = false
                                        titleB.identifiers.identifier.each { idA ->
                                            println("processing check of identifier: ${idA.@namespace.text()}:${idA.@value.text()}")
                                            if(titleC.ids.find { idC -> idC.ns.ns == idA.@namespace.text() && idC.value == idA.@value.text() }) {
                                                idCriteria = true
                                            }
                                        }
                                        if(nameCriteria && idCriteria) {
                                            println("name and at least one identifier is matching --> merge!")
                                            mergingTitles << [from:titleA.globalUID,to:titleC.globalUID]
                                        }
                                        else {
                                            println("the GOKb key may be mistaken, repeat checkup!")
                                            nextPhase.put(titleC.gokbId,TitleInstance.findAllByGokbId(titleC.gokbId))
                                        }
                                    }
                                    remappingTitles << [target:titleA.globalUID,from:titleA.gokbId,to:guessedCorrectTitleKey]
                                }
                                else {
                                    println("package lacks GOKb ID")
                                    missingTitles << titleA.gokbId
                                }
                            }
                            else {
                                println("package lacks title, probably")
                                missingTitles << titleA.gokbId
                            }
                        }
                        else {
                            println("${titleA.title} is without TIPPs, try other solutions")
                            if(titleA.historyEvents) {
                                Set<TitleInstance> otherTitlesConcerned = []
                                titleA.historyEvents.each { thep ->
                                    if(thep.event.fromTitles().contains(titleA))
                                        otherTitlesConcerned.addAll(thep.event.toTitles())
                                    else if(thep.event.toTitles().contains(titleA))
                                        otherTitlesConcerned.addAll(thep.event.fromTitles())
                                }
                                GPathResult guessedTitle
                                String guessedCorrectTitleKey
                                otherTitlesConcerned.each { titleCandidate ->
                                    GPathResult candidateRecord = oaiRecords.get(titleCandidate.gokbId)
                                    println("attempt get with link ${grs.uri}?verb=GetRecord&metadataPrefix=${grs.fullPrefix}&identifier=${titleCandidate.gokbId} ...")
                                    if(!candidateRecord) {
                                        candidateRecord = globalSourceSyncService.fetchRecord(grs.uri,'titles',[verb:'GetRecord',metadataPrefix:grs.fullPrefix,identifier:titleCandidate.gokbId])
                                        if(candidateRecord)
                                            oaiRecords.put(titleCandidate.gokbId,candidateRecord)
                                    }
                                    if(candidateRecord && candidateRecord.record.metadata.gokb.title.size() > 0) {
                                        GPathResult gokbTitleHistory = candidateRecord.record.metadata.gokb.title.history
                                        guessedTitle = gokbTitleHistory.historyEvent.'**'.find { thep ->
                                            thep.title.text() == titleA.title
                                        }
                                    }
                                    else {
                                        println("Title history participant not retrievable in GOKb!")
                                    }
                                }
                                if(guessedTitle) {
                                    guessedCorrectTitleKey = guessedTitle.uuid.text()
                                    if(!guessedCorrectTitleKey) {
                                        //check if titleCandidate's key (NOT NECESSARILY the correct instance itself!) is not already existing in LAS:eR
                                        TitleInstance titleC = TitleInstance.findByGokbId(guessedCorrectTitleKey)
                                        //the KEY is already taken in LAS:eR! Do further check!
                                        if(titleC) {
                                            println("GOKb key ${guessedCorrectTitleKey} already taken! Check if merge or remapping is necessary!")
                                            boolean nameCriteria = guessedTitle.name.text() == titleC.title
                                            boolean idCriteria = false
                                            guessedTitle.identifiers.identifier.each { idA ->
                                                println("processing check of identifier: ${idA.@namespace.text()}:${idA.@value.text()}")
                                                if(titleC.ids.find { idC -> idC.ns.ns == idA.@namespace.text() && idC.value == idA.@value.text() }) {
                                                    idCriteria = true
                                                }
                                            }
                                            if(nameCriteria && idCriteria) {
                                                println("name and at least one identifier is matching --> merge!")
                                                mergingTitles << [from:titleA.globalUID,to:titleC.globalUID]
                                            }
                                            else {
                                                println("the GOKb key may be mistaken, repeat checkup!")
                                                nextPhase.put(titleC.gokbId,TitleInstance.findAllByGokbId(titleC.gokbId))
                                            }
                                        }
                                        remappingTitles << [target:titleA.globalUID,from:titleA.gokbId,to:guessedCorrectTitleKey]
                                    }
                                    else {
                                        println("Title history event not retrievable in GOKb! Someone has deleted an entry?!")
                                        titlesWithoutTIPPs << titleA.globalUID
                                    }
                                }
                                else
                                    titlesWithoutTIPPs << titleA.globalUID
                            }
                            else {
                                titlesWithoutTIPPs << titleA.globalUID
                            }
                        }
                    }
                    else if(!crossed){
                        println("${titleA.title} and ${titleB.name.text()} are matching! Check GOKb record and TIPP data! Maybe a merger has to be done ...")
                        List<TitleInstance> dupsWithSameName = titleInstances.findAll { instance -> instance.globalUID != titleA.globalUID && instance.title == titleA.title}
                        if(!considered.contains(titleB.@uuid.text())) {
                            considered << titleB.@uuid.text()
                            Set<String> tippSetA = []
                            tippSetA.addAll(titleA.tipps.collect{tipp->tipp.gokbId})
                            dupsWithSameName.each { duplicate ->
                                if(duplicate.tipps.size() > 0)
                                    tippSetA.addAll(duplicate.tipps.collect{tipp->tipp.gokbId})
                                else titlesWithoutTIPPs << duplicate.globalUID
                            }
                            Set<String> tippSetB = []
                            titleB.TIPPs.TIPP.findAll().each { tipp ->
                                if(Package.findByGokbId(tipp.package.@uuid.text()))
                                    tippSetB << tipp.@uuid.text()
                            }
                            if(tippSetA.size() == 0) {
                                println("${titleA.title} is without TIPPs")
                                titlesWithoutTIPPs << titleA.globalUID
                            }
                            else if(tippSetB.containsAll(tippSetA)) {
                                println("full match, unite TIPP sets")
                                dupsWithSameName.remove(titleA)
                                tippMergers << [from:titleA.globalUID,to:titleB,others:dupsWithSameName.collect {it.globalUID}]
                            }
                            else {
                                println("partial match; map each TIPP to GOKb title")
                                tippMergers << [gokbLink:"${grs.uri}?verb=GetRecord&metadataPrefix=${grs.fullPrefix}&identifier=${titleA.gokbId}",mergeTarget:titleA.globalUID,gokbId:titleA.gokbId]
                            }
                        }
                        else if(considered.contains(titleB.@uuid.text())) {
                            println("Merger already done with gokbId ${titleB.@uuid.text()}!")
                        }
                        else {
                            titlesWithoutTIPPs.addAll(titleInstances.findAll { instance -> instance.title == titleA.title && instance.tipps.size() == 0 }.collect { instance -> instance.globalUID })
                        }
                    }
                }
                else {
                    println("UUID ${titleA.gokbId} does not exist in GOKb, mark everything dependent as deleted!")
                    missingTitles << titleA.gokbId
                }
            }
        }
        [missingTitles:missingTitles,mergingTitles:mergingTitles,remappingTitles:remappingTitles,titlesWithoutTIPPs:titlesWithoutTIPPs,nextPhase:nextPhase,tippMergers:tippMergers]
    }

    void executeTiCleanup(Map result) throws CleanupException {
        List<TitleInstance> toDelete = []
        toDelete.addAll(TitleInstance.findAllByGlobalUIDInList(result.titlesWithoutTIPPs))
        toDelete.addAll(TitleInstance.findAllByGokbIdIsNull())
        TitleInstance.withTransaction { TransactionStatus status ->
            try {
                result.missingTitles.each { entry ->
                    TitleInstance mergeTarget = TitleInstance.findByGokbId(entry) //take first
                    Set<TitleInstance> others = TitleInstance.findAllByGokbIdAndGlobalUIDNotEqual(entry,mergeTarget.globalUID)
                    TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.title = :to where tipp.title in :from',[to:mergeTarget,from:others])
                    Fact.executeUpdate('update Fact f set f.relatedTitle = :to where f.relatedTitle in :from',[to:mergeTarget, from:others])
                    Identifier.executeUpdate('update Identifier id set id.ti = :to where id.ti in :from',[to:mergeTarget,from:others])
                    //TitleInstitutionProvider.executeUpdate('update TitleInstitutionProvider tip set tip.title = :to where tip.title in :from',[to:mergeTarget,from:others])
                    OrgRole.executeUpdate('update OrgRole oo set oo.title = :to where oo.title in :from',[to:mergeTarget, from:others])
                    TitleHistoryEventParticipant.executeUpdate('update TitleHistoryEventParticipant thep set thep.participant = :to where thep.participant in :from',[to:mergeTarget, from:others])
                    PersonRole.executeUpdate('update PersonRole pr set pr.title = :to where pr.title in :from',[to:mergeTarget, from:others])
                    CreatorTitle.executeUpdate('update CreatorTitle ct set ct.title = :to where ct.title in :from',[to:mergeTarget, from:others])
                    toDelete.addAll(others)
                }
                result.remappingTitles.each { entry ->
                    TitleInstance.executeUpdate('update TitleInstance ti set ti.gokbId = :to where ti.globalUID = :from',[from:entry.target,to:entry.to])
                }
                result.tippMergers.each { entry ->
                    if(entry.to) {
                        TitleInstance mergeTarget = TitleInstance.findByGlobalUID(entry.from)
                        //List tippsB = entry.to.TIPPs.TIPP.collect { tippB -> tippB.@uuid.text() }
                        entry.others.each { otherKey ->
                            TitleInstance other = TitleInstance.findByGlobalUID(otherKey)
                            TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.title = :to where tipp.title = :from',[to:mergeTarget,from:other])
                            Fact.executeUpdate('update Fact f set f.relatedTitle = :to where f.relatedTitle = :from',[to:mergeTarget, from:other])
                            Identifier.executeUpdate('update Identifier id set id.ti = :to where id.ti = :from',[to:mergeTarget,from:other])
                            //TitleInstitutionProvider.executeUpdate('update TitleInstitutionProvider tip set tip.title = :to where tip.title = :from',[to:mergeTarget,from:other])
                            OrgRole.executeUpdate('update OrgRole oo set oo.title = :to where oo.title = :from',[to:mergeTarget, from:other])
                            TitleHistoryEventParticipant.executeUpdate('update TitleHistoryEventParticipant thep set thep.participant = :to where thep.participant = :from',[to:mergeTarget, from:other])
                            PersonRole.executeUpdate('update PersonRole pr set pr.title = :to where pr.title = :from',[to:mergeTarget, from:other])
                            CreatorTitle.executeUpdate('update CreatorTitle ct set ct.title = :to where ct.title = :from',[to:mergeTarget, from:other])
                            toDelete << other
                        }
                    }
                    else if(entry.mergeTarget) {
                        TitleInstance mergeTarget = TitleInstance.findByGlobalUID(entry.mergeTarget)
                        Set<TitleInstance> others = TitleInstance.findAllByGokbIdAndGlobalUIDNotEqual(entry.gokbId,entry.mergeTarget)
                        TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.title = :to where tipp.title in :from',[to:mergeTarget,from:others])
                        Fact.executeUpdate('update Fact f set f.relatedTitle = :to where f.relatedTitle in :from',[to:mergeTarget, from:others])
                        Identifier.executeUpdate('update Identifier id set id.ti = :to where id.ti in :from',[to:mergeTarget,from:others])
                        //TitleInstitutionProvider.executeUpdate('update TitleInstitutionProvider tip set tip.title = :to where tip.title in :from',[to:mergeTarget,from:others])
                        OrgRole.executeUpdate('update OrgRole oo set oo.title = :to where oo.title in :from',[to:mergeTarget, from:others])
                        TitleHistoryEventParticipant.executeUpdate('update TitleHistoryEventParticipant thep set thep.participant = :to where thep.participant in :from',[to:mergeTarget, from:others])
                        PersonRole.executeUpdate('update PersonRole pr set pr.title = :to where pr.title in :from',[to:mergeTarget, from:others])
                        CreatorTitle.executeUpdate('update CreatorTitle ct set ct.title = :to where ct.title in :from',[to:mergeTarget, from:others])
                        toDelete.addAll(others)
                    }
                    else {
                        toDelete.addAll(TitleInstance.executeQuery('select ti from TitleInstance ti where ti.gokbId = :titleAKey and ti.tipps.size = 0',[titleAKey:entry.gokbId]))
                    }
                }
                globalSourceSyncService.cleanUpGorm()
                if(toDelete) {
                    Identifier.executeUpdate('delete from Identifier id where id.ti in :toDelete',[toDelete:toDelete])
                    //TitleInstitutionProvider.executeUpdate('delete from TitleInstitutionProvider tip where tip.title in :toDelete',[toDelete:toDelete])
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.title in :toDelete',[toDelete:toDelete])
                    TitleHistoryEventParticipant.executeUpdate('delete from TitleHistoryEventParticipant thep where thep.participant in :toDelete',[toDelete:toDelete])
                    PersonRole.executeUpdate('delete from PersonRole pr where pr.title in :toDelete',[toDelete:toDelete])
                    CreatorTitle.executeUpdate('delete from CreatorTitle ct where ct.title in :toDelete',[toDelete:toDelete])
                    Fact.executeUpdate('delete from Fact f where f.relatedTitle in :toDelete',[toDelete:toDelete])
                    TitleInstance.executeUpdate('delete from TitleInstance ti where ti in :toDelete',[toDelete:toDelete])
                }
            }
            catch (Exception e) {
                status.setRollbackOnly()
                throw new CleanupException(e.message)
            }
        }
    }

    Map<String,Object> listDeletedTIPPs() {
        globalSourceSyncService.cleanUpGorm()
        //merge duplicate tipps
        List<String,Integer> duplicateTIPPRows = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId,count(tipp.gokbId) from TitleInstancePackagePlatform tipp group by tipp.gokbId having count(tipp.gokbId) > 1')
        List<String> duplicateTIPPKeys = []
        List<Long> excludes = []
        List<Map<String,Object>> mergingTIPPs = []
        duplicateTIPPRows.eachWithIndex { row, int ctr ->
            println("Processing entry ${ctr}. TIPP UUID ${row[0]} occurs ${row[1]} times in DB. Merging!")
            duplicateTIPPKeys << row[0]
            TitleInstancePackagePlatform mergeTarget = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(row[0], RDStore.TIPP_STATUS_DELETED)
            if(!mergeTarget) {
                println("no equivalent found, taking first ...")
                mergeTarget = TitleInstancePackagePlatform.findByGokbId(row[0])
            }
            excludes << mergeTarget.id
            println("merge target with LAS:eR object ${mergeTarget} located")
            List<Long> iesToMerge = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.tipp.gokbId = :gokbId and ie.tipp != :mergeTarget',[gokbId:row[0], mergeTarget:mergeTarget])
            if(iesToMerge) {
                println("found IEs to merge: ${iesToMerge}")
                mergingTIPPs << [mergeTarget:mergeTarget.id,iesToMerge:iesToMerge]
            }
        }
        Map<String,RefdataValue> refdatas = [:]
        RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS).each { tippStatus ->
            refdatas[tippStatus.value] = tippStatus
        }
        //get to deleted tipps
        globalSourceSyncService.cleanUpGorm()
        println("move to TIPPs marked as deleted")
        //aim is to exclude resp. update those which has been erroneously marked as deleted (duplicate etc.)
        List<TitleInstancePackagePlatform> deletedTIPPs = TitleInstancePackagePlatform.findAllByStatus(RDStore.TIPP_STATUS_DELETED,[sort:'pkg.name',order:'asc'])
        deletedTIPPs.addAll(TitleInstancePackagePlatform.findAllByGokbIdIsNull())
        println "deleted TIPPs located: ${deletedTIPPs.size()}"
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        HTTPBuilder http = new HTTPBuilder(grs.uri)
        Map<String, NodeChildren> oaiRecords = [:]
        List<Map<TitleInstancePackagePlatform,Map<String,Object>>> deletedWithoutGOKbRecord = []
        List<Map<String,Map<String,Object>>> deletedWithGOKbRecord = []
        /*
            processing list of deleted TIPPs, doing the following checks:
            - is there a remote GOKb record? Load remote package for that
         */
        deletedTIPPs.each { delTIPP ->
            println("now processing entry #${delTIPP.id} ${delTIPP.gokbId} of package ${delTIPP.pkg} with uuid ${delTIPP.pkg.gokbId}")
            if(!duplicateTIPPKeys.contains(delTIPP.gokbId)) {
                NodeChildren oaiRecord = oaiRecords.get(delTIPP.pkg.gokbId)
                if(!oaiRecord) {
                    /*
                        case: there is a TIPP in LAS:eR with an invalid GOKb package UUID, thus no record.
                        If we have IssueEntitlements depending on it: check subscription state
                            if deleted: mark IE as deleted
                            else check if there is an equivalent GOKb record -> load package, check if there is an equivalent TitleInstance-Package-Platform entry (so a TIPP entry!)
                            if so: remap to new UUID
                            else show subscriber
                    */
                    def packageRecord = http.get(path:'packages',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.pkg.gokbId],contentType:'xml') { resp, xml ->
                        GPathResult record = new XmlSlurper().parseText(xml.text)
                        if(record.error.@code == 'idDoesNotExist')
                            return "package ${delTIPP.pkg.gokbId} inexistent"
                        else return record.'GetRecord'.record.metadata.gokb.package
                    }
                    //case one: GOKb package does not exist
                    if(packageRecord instanceof GString) {
                        println(packageRecord)
                        List<Map<String,Object>> issueEntitlements = []
                        //check eventually depending issue entitlements
                        IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED).each { ie ->
                            Map<String,Object> ieDetails = [ie:ie]
                            if(ie.subscription.status == RDStore.TIPP_STATUS_DELETED) {
                                println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                                ieDetails.action = "deleteCascade"
                            }
                            else {
                                println("associated subscription is not deleted, report ...")
                                ieDetails.action = "report"
                                Map<String,Object> report = [subscriber:ie.subscription.getSubscriber().shortname,subscription:ie.subscription.name,title:delTIPP.title.title,package:delTIPP.pkg.name]
                                if(ie.subscription.getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE, CalculatedType.TYPE_PARTICIPATION]) {
                                    report.consortium = ie.subscription.getConsortia().shortname
                                }
                                else {
                                    report.consortium = ""
                                }
                                ieDetails.report = report+[cause:"Paket ${delTIPP.pkg.gokbId} existiert nicht"]
                            }
                            issueEntitlements << ieDetails
                        }
                        Map<TitleInstancePackagePlatform,List<Map<String,Object>>> result = [:]
                        result[delTIPP] = issueEntitlements
                        deletedWithoutGOKbRecord << result
                    }
                    //case two: GOKb package does exist
                    else if(packageRecord instanceof NodeChildren) {
                        oaiRecords[delTIPP.pkg.gokbId] = packageRecord
                        oaiRecord = packageRecord
                    }
                }
                //case two continued: there is a GOKb record (preloaded by map or meanwhile fetched by OAI request)
                //do NOT set to else if because the variable may be set in structure above
                if(oaiRecord) {
                    //find TIPP in remote record
                    def gokbTIPP = oaiRecord.'**'.find { tipp ->
                        tipp.@uuid == delTIPP.gokbId && tipp.status.text() != RDStore.TIPP_STATUS_DELETED.value
                    }
                    if(!gokbTIPP) {
                        /*
                        case: there is a TIPP in LAS:eR with an invalid GOKb UUID, thus no record.
                        If we have IssueEntitlements depending on it: check subscription state
                            if deleted: mark IE as deleted
                            else check if there is an equivalent GOKb record -> load package, check if there is an equivalent TitleInstance-Package-Platform entry (so a TIPP entry!)
                            if so: remap to new UUID
                            else show subscriber
                         */
                        NodeChildren oaiTitleRecord = oaiRecords.get(delTIPP.title.gokbId)
                        List<Map<String,Object>> issueEntitlements = []
                        def equivalentTIPP
                        boolean titleRecordExists
                        boolean equivalentTIPPExists
                        //load remote title record in order to determine equivalent TitleInstance-Package-Platform link
                        if(!oaiTitleRecord) {
                            def titleRecord = http.get(path:'titles',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.title.gokbId],contentType:'xml') { resp, xml ->
                                GPathResult record = new XmlSlurper().parseText(xml.text)
                                if(record.error.@code == 'idDoesNotExist')
                                    return "title ${delTIPP.title.gokbId} inexistent, name is ${delTIPP.title.title}"
                                else if(record.'GetRecord'.record.header.status == 'deleted')
                                    return "title ${delTIPP.title.gokbId} is marked as deleted, name is ${delTIPP.title.title}"
                                else
                                    return record.'GetRecord'.record.metadata.gokb.title
                            }
                            //no title record
                            if(titleRecord instanceof GString) {
                                println(titleRecord)
                                titleRecordExists = false
                            }
                            //title record exists
                            else if (titleRecord instanceof NodeChildren) {
                                println("title instance ${delTIPP.title.gokbId} found, reconcile UUID by retrieving package and platform")
                                titleRecordExists = true
                                oaiTitleRecord = (NodeChildren) titleRecord
                                oaiRecords.put(delTIPP.title.gokbId,oaiTitleRecord)
                            }
                        }
                        //title record exists (by OAI PMH request or by preload in map)
                        if(oaiTitleRecord) {
                            //match package and platform
                            equivalentTIPP = oaiTitleRecord.TIPPs.TIPP.find { node ->
                                node.package.'@uuid' == delTIPP.pkg.gokbId && node.platform.'@uuid' == delTIPP.platform.gokbId
                            }
                            if(equivalentTIPP) {
                                equivalentTIPPExists = true
                                println("TIPP found: should remapped to UUID ${equivalentTIPP.@uuid}")
                            }
                            else {
                                equivalentTIPPExists = false
                                println("no equivalent TIPP found")
                            }
                        }
                        IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED).each { ie ->
                            Map<String,Object> ieDetails = [ie:ie]
                            if(ie.subscription.status == RDStore.TIPP_STATUS_DELETED) {
                                println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                                ieDetails.action = "deleteCascade"
                            }
                            else {
                                println("${ie.subscription} is current, check if action needs to be taken ...")
                                Map<String,Object> report = [subscriber:ie.subscription.getSubscriber().shortname,subscription:ie.subscription.name,title:delTIPP.title.title,package:delTIPP.pkg.name]
                                if(ie.subscription.getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE, CalculatedType.TYPE_PARTICIPATION]) {
                                    report.consortium = ie.subscription.getConsortia().shortname
                                }
                                else {
                                    report.consortium = ""
                                }
                                //does the title exist? If not, issue entitlement is void!
                                if(!titleRecordExists){
                                    ieDetails.action = "report"
                                    ieDetails.report = report+[cause:"Titel ${delTIPP.title.gokbId} existiert nicht"]
                                    println(ieDetails.report)
                                }
                                else if(titleRecordExists) {
                                    //does the TIPP exist? If so: check if it is already existing in package; if not, create it.
                                    if(equivalentTIPPExists) {
                                        if(!ie.tipp.pkg.tipps.find {it.gokbId == equivalentTIPP.@uuid}){
                                            ieDetails.action = "remap"
                                            ieDetails.target = equivalentTIPP.@uuid
                                        }
                                        else println("no remapping necessary!")
                                    }
                                    //If not, report because it is void!
                                    else {
                                        ieDetails.action = "report"
                                        ieDetails.report = report+[cause:"Kein äquivalentes TIPP gefunden"]
                                        println(ieDetails.report)
                                    }
                                }
                            }
                            if(ieDetails.action)
                                issueEntitlements << ieDetails
                        }
                        Map<TitleInstancePackagePlatform,List<Map<String,Object>>> result = [:]
                        result[delTIPP] = issueEntitlements
                        deletedWithoutGOKbRecord << result
                    }
                    else {
                        /*
                            case: there is a TIPP marked deleted with GOKb entry
                            do further checks as follows:
                            set TIPP and IssueEntitlement (by pending change) to that status
                            otherwise do nothing
                         */
                        Map<String,Map<String,Object>> result = [:]
                        RefdataValue currTippStatus = refdatas[gokbTIPP.status.text()]
                        Map<String,Object> tippDetails = [issueEntitlements: IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED), action: 'updateStatus', status: currTippStatus]
                        //storing key is needed in order to prevent LazyInitializationException when executing cleanup
                        result[delTIPP.globalUID] = tippDetails
                        deletedWithGOKbRecord << result
                    }
                }
            }
            else {
                println("TIPP marked as deleted is a duplicate, so already considered")
            }
        }
        http.shutdown()
        [deletedWithoutGOKbRecord:deletedWithoutGOKbRecord,deletedWithGOKbRecord:deletedWithGOKbRecord,mergingTIPPs:mergingTIPPs,duplicateTIPPKeys:duplicateTIPPKeys,excludes:excludes]
    }

    List<List<String>> executeTIPPCleanup(Map result) {
        //first: merge duplicate entries
        result.mergingTIPPs.each { mergingTIPP ->
            IssueEntitlement.withTransaction { status ->
                try {
                    IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp.id = :mergeTarget where ie.id in (:iesToMerge)',[mergeTarget:mergingTIPP.mergeTarget,iesToMerge:mergingTIPP.iesToMerge])
                    status.flush()
                }
                catch (Exception e) {
                    log.error("failure on merging TIPPs ... rollback!")
                    status.setRollbackOnly()
                }
            }
        }
        println("remapping done, purge now duplicate entries ...")
        globalSourceSyncService.cleanUpGorm()
        List<List<String>> reportRows = []
        Map<RefdataValue,Set<String>> pendingChangeSetupMap = [:]
        Set<String> alreadyProcessed = []

        result.deletedWithoutGOKbRecord.each { entry ->
            entry.each { delTIPP,issueEntitlements ->
                issueEntitlements.each { ieDetails ->
                    IssueEntitlement ie = (IssueEntitlement) ieDetails.ie
                    switch(ieDetails.action) {
                        case "deleteCascade":
                            //mark as deleted!
                            println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                            deletionService.deleteSubscription(ie.subscription,false)
                            break
                        case "report": reportRows << [ieDetails.report.consortium,ieDetails.report.subscriber,ieDetails.report.subscription,ieDetails.report.package,ieDetails.report.title,ieDetails.report.cause]
                            break
                        case "remap": if(!alreadyProcessed.contains(delTIPP.gokbId)){
                            //mark obsolete ones as deleted!
                            deletionService.deleteTIPP(delTIPP,TitleInstancePackagePlatform.findByGokbId(ieDetails.target))
                            alreadyProcessed << delTIPP.gokbId
                        }
                            break
                    }
                }
            }
        }
        result.deletedWithGOKbRecord.each { row ->
            row.each { delTIPP, tippDetails ->
                Set<Long> tippsToUpdate = pendingChangeSetupMap[tippDetails.status]
                if(!tippsToUpdate)
                    tippsToUpdate = []
                tippsToUpdate << delTIPP
                pendingChangeSetupMap[tippDetails.status] = tippsToUpdate
            }
        }
        pendingChangeSetupMap.each { RefdataValue status, Set<String> tippsToUpdate ->
            println("updating ${tippsToUpdate} to status ${status}")
            TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :status where tipp.globalUID in :tippsToUpdate',[status:status,tippsToUpdate:tippsToUpdate])
            //hook up pending changes
            tippsToUpdate.each { tippKey ->
                List<IssueEntitlement> iesToNotify = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.globalUID = :tippKey',[tippKey:tippKey])
                if(iesToNotify) {
                    TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGlobalUID(tippKey)
                    iesToNotify.each { IssueEntitlement ie ->
                        println("notifying subscription ${ie.subscription}")
                        Map<String, Object> changeMap = [target:ie.subscription,oid:"${ie.class.name}:${ie.id}",prop:'status',newValue:status.id,oldValue:ie.status.id]
                        changeNotificationService.determinePendingChangeBehavior(changeMap,PendingChangeConfiguration.TITLE_UPDATED,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,tipp.pkg))
                        //changeNotificationService.registerPendingChange(PendingChange.PROP_SUBSCRIPTION,ie.subscription,ie.subscription.getSubscriber(),changeMap,null,null,changeDesc)
                    }
                }
                else println("no issue entitlements depending!")
            }
        }
        Set<TitleInstancePackagePlatform> tippsToDelete = TitleInstancePackagePlatform.findAllByGokbIdInListAndIdNotInList(result.duplicateTIPPKeys,result.excludes)
        //this is correct; only the duplicates should be deleted!
        if(tippsToDelete)
            deletionService.deleteTIPPsCascaded(tippsToDelete)
        println("Cleanup finished!")
        reportRows
    }

    Map<String,Object> getTIPPsWithoutGOKBId() {
        List<TitleInstancePackagePlatform> tippsWithoutGOKbID = TitleInstancePackagePlatform.findAllByGokbIdIsNullOrGokbIdLike(RDStore.GENERIC_NULL_VALUE.value)
        List<IssueEntitlement> issueEntitlementsAffected = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp in :tipps',[tipps:tippsWithoutGOKbID])
        Map<TitleInstancePackagePlatform,Set<IssueEntitlement>> ieTippMap = [:]
        List<Map<String,Object>> tippsWithAlternate = []
        Map<Long,Long> toDelete = [:]
        Set<Long> toUUIDfy = []
        tippsWithoutGOKbID.each { tipp ->
            TitleInstancePackagePlatform altTIPP = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.title = :title and tipp.status = :current and tipp.gokbId != null",[pkg:tipp.pkg,title:tipp.title,current:RDStore.TIPP_STATUS_CURRENT])[0]
            if(altTIPP) {
                toDelete[tipp.id] = altTIPP.id
                tippsWithAlternate << [tipp:tipp,altTIPP:altTIPP]
            }
            else toUUIDfy << tipp.id
        }
        issueEntitlementsAffected.each { IssueEntitlement ie ->
            if (ieTippMap.get(ie.tipp)) {
                ieTippMap[ie.tipp] << ie
            } else {
                Set<IssueEntitlement> ies = new TreeSet<IssueEntitlement>()
                ies.add(ie)
                ieTippMap[ie.tipp] = ies
            }
        }
        [tipps: tippsWithAlternate, issueEntitlements: ieTippMap, toDelete: toDelete, toUUIDfy: toUUIDfy]
    }

    void purgeTIPPsWihtoutGOKBId(toDelete,toUUIDfy) {
        toDelete.each { oldTippId, newTippId ->
            TitleInstancePackagePlatform oldTipp = TitleInstancePackagePlatform.get(oldTippId)
            TitleInstancePackagePlatform newTipp = TitleInstancePackagePlatform.get(newTippId)
            deletionService.deleteTIPP(oldTipp,newTipp)
        }
        toUUIDfy.each { tippId ->
            TitleInstancePackagePlatform.executeUpdate("update TitleInstancePackagePlatform tipp set tipp.gokbId = :missing where tipp.id = :id",[missing:"${RDStore.GENERIC_NULL_VALUE}.${tippId}",id:tippId])
        }
    }

    Map<String, Object> listPlatformDuplicates() {
        Map<String,Object> result = [:]
        Map<String, GPathResult> oaiRecords = [:]
        List<Platform> platformsWithoutTIPPs = Platform.executeQuery('select plat from Platform plat where plat.tipps.size = 0')
        result.platformDupsWithoutTIPPs = []
        result.platformsToUpdate = []
        result.platformsWithoutGOKb = []
        List<Platform> platformsWithoutGOKb = Platform.findAllByGokbIdIsNull()
        result.inexistentPlatforms = []
        result.incorrectPlatformDups = []
        result.database = []
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        List duplicateKeys = Platform.executeQuery('select plat.gokbId,count(plat.gokbId) from Platform plat where plat.gokbId is not null group by plat.gokbId having count(plat.gokbId) > 1')
        duplicateKeys.each { row ->
            //get platform, get eventual TIPPs of platform, determine from package which platform key is correct, if it is correct: ignore, otherwise, add to result
            List<Platform> platformDuplicates = Platform.findAllByGokbId(row[0])
            platformDuplicates.each { Platform platform ->
                println("processing platform ${platform} with duplicate GOKb ID ${platform.gokbId}")
                //it ran too often into null pointer exceptions ... we set a tighter check!
                if(platform.tipps.size() > 0) {
                    TitleInstancePackagePlatform referenceTIPP = platform.tipps[0]
                    GPathResult packageRecord = oaiRecords.get(referenceTIPP.pkg.gokbId)
                    if(!packageRecord) {
                        packageRecord = globalSourceSyncService.fetchRecord(grs.uri,'packages',[verb:'GetRecord',metadataPrefix:grs.fullPrefix,identifier:referenceTIPP.pkg.gokbId])
                        oaiRecords.put(referenceTIPP.pkg.gokbId,packageRecord)
                    }
                    if(packageRecord.record.metadata.gokb.package) {
                        GPathResult referenceGOKbTIPP = packageRecord.record.metadata.gokb.package.TIPPs.TIPP.find { tipp ->
                            tipp.@uuid.text() == referenceTIPP.gokbId
                        }
                        if(referenceGOKbTIPP) {
                            String guessedCorrectPlatformKey = referenceGOKbTIPP.platform.@uuid.text()
                            if(platform.gokbId != guessedCorrectPlatformKey) {
                                result.incorrectPlatformDups << platform
                            }
                        }
                    }
                }
                else {
                    result.platformDupsWithoutTIPPs << platform
                }
            }
        }
        platformsWithoutGOKb.each { Platform platform ->
            if(!OrgAccessPointLink.findByPlatform(platform) && !TitleInstancePackagePlatform.findByPlatform(platform) && platform.customProperties.empty) {
                result.platformsWithoutGOKb << platform
            }
            else {
                result.database << [id:platform.id,name:platform.name]
            }
        }
        platformsWithoutTIPPs.each { Platform platform ->
            println("processing platform ${platform} without TIPP ${platform.gokbId} to check correctness ...")
            Map esQuery = gokbService.queryElasticsearch('https://gokb.org/gokb/api/find?uuid='+platform.gokbId)
            List esResult
            //is a consequent error of GOKbService's copy-paste-mess ...
            if(esQuery.warning)
                esResult = esQuery.warning.records
            else if(esQuery.info)
                esResult = esQuery.info.records
            if(esResult) {
                Map gokbPlatformRecord = esResult[0]
                if(gokbPlatformRecord.name == platform.name)
                    println("Name ${platform.name} is correct")
                else {
                    println("Name ${platform.name} is not correct, should actually be ${gokbPlatformRecord.name}")
                    result.platformsToUpdate << [old:platform.globalUID,correct:gokbPlatformRecord]
                }
            }
            else {
                if(!OrgAccessPointLink.findByPlatform(platform) && !TitleInstancePackagePlatform.findByPlatform(platform) && platform.customProperties.empty) {
                    result.inexistentPlatforms << platform
                }
                else {
                    if(!result.database.find{ entry -> entry.id == platform.id})
                        result.database << [id:platform.id,name:platform.name]
                }
            }
        }
        result
    }

    void executePlatformCleanup(Map result) {
        List<String> toDelete = []
        toDelete.addAll(result.platformDupsWithoutTIPPs.collect{ plat -> plat.globalUID })
        toDelete.addAll(result.platformsWithoutGOKb.collect{plat -> plat.globalUID })
        toDelete.addAll(result.inexistentPlatforms.collect{plat -> plat.globalUID })
        result.platformsToUpdate.each { entry ->
            Platform oldRecord = Platform.findByGlobalUID(entry.old)
            Map newRecord = entry.correct
            oldRecord.name = newRecord.name
            oldRecord.primaryUrl = newRecord.primaryUrl
            oldRecord.status = RefdataValue.getByValueAndCategory(newRecord.status,RDConstants.PLATFORM_STATUS)
            oldRecord.save(flush:true) //here, it is necessary since object is being reused
        }
        Platform.executeUpdate('delete from Platform plat where plat.globalUID in :toDelete',[toDelete:toDelete])
    }

}
