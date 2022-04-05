package de.laser

import de.laser.storage.RDStore
import de.laser.titles.TitleHistoryEvent
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls to title instances in platform-package contexts. Not to
 * confound with {@link TitleController}
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TippController  {

  ContextService contextService

  /**
   * Shows the given title. The title may be called by database ID, we:kb UUID or globalUID
   * @return the details view of the title
   */
  @Secured(['ROLE_USER'])
  def show() { 
    Map<String, Object> result = [:]

    result.user = contextService.getUser()
    result.editable = false

    TitleInstancePackagePlatform tipp
    if(params.id instanceof Long || params.id.isLong())
      tipp = TitleInstancePackagePlatform.get(params.id)
    else if(params.id ==~ /[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}/)
      tipp = TitleInstancePackagePlatform.findByGokbId(params.id)
    else tipp = Package.findByGlobalUID(params.id)
    result.tipp = tipp

    result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_CURRENT])[0]
    result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_EXPECTED])[0]
    result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_RETIRED])[0]
    result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_DELETED])[0]


    if (!result.tipp) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label'), params.id])
      redirect action: 'list'
      return
    }
    result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = :participant", [participant: result.tipp] )

    result
  }
}
