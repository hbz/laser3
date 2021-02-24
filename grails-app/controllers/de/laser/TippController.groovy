package de.laser

import de.laser.auth.User
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.helper.SwissKnife
import de.laser.titles.TitleHistoryEvent
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class TippController  {

  ContextService contextService

  @Secured(['ROLE_USER'])
  def show() { 
    Map<String, Object> result = [:]

    result.user = contextService.getUser()
    result.editable = false

    result.tipp = TitleInstancePackagePlatform.get(params.id)

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
