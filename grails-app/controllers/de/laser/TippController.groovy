package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.titles.TitleHistoryEvent
import de.laser.wekb.TitleInstancePackagePlatform
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls to title instances in platform-package contexts. Not to
 * confound with {@link TitleController}
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TippController  {

  ContextService contextService
  SurveyService surveyService

  //-----

  /**
   * Map containing menu alternatives if an unexisting object has been called
   */
  public static final Map<String, String> CHECK404_ALTERNATIVES = [
          'title/list': 'menu.public.all_titles',
          'myInstitution/currentTitles': 'myinst.currentTitles.label'
  ]

  //-----

  /**
   * Shows the given title. The title may be called by database ID, we:kb UUID or Laser-ID
   * @return the details view of the title
   */
  @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
  @Check404(domain= TitleInstancePackagePlatform)
  def show() { 
    Map<String, Object> result = [:]

    result.user = contextService.getUser()
    result.editable = false

    TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.id)
    result.tipp = tipp

    if(!result.tipp){
      response.sendError(404)
      return
    }
    result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_CURRENT])[0]
    result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_EXPECTED])[0]
    result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_RETIRED])[0]
    result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.tipp.pkg, status: RDStore.TIPP_STATUS_DELETED])[0]

    result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = :participant", [participant: result.tipp] )

    result.participantPerpetualAccessToTitle = []

    result.subscription = params.sub ? Subscription.get(params.sub) : null
    Org org = contextService.getOrg()
    if (result.subscription) {
      org = result.subscription.getSubscriberRespConsortia()
    }

    result.participantPerpetualAccessToTitle = surveyService.listParticipantPerpetualAccessToTitle(org, tipp)

    result
  }
}
