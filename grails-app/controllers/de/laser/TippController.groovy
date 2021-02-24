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

    result.currentTippsCounts = TitleInstancePackagePlatform.findAllByPkgAndStatus(result.tipp.pkg, RDStore.TIPP_STATUS_CURRENT).size()
    result.plannedTippsCounts = TitleInstancePackagePlatform.findAllByPkgAndStatusNotEqualAndAccessEndDateGreaterThan(result.tipp.pkg, RDStore.TIPP_STATUS_DELETED, new Date()).size()
    result.expiredTippsCounts = TitleInstancePackagePlatform.findAllByPkgAndStatusNotEqualAndAccessEndDateLessThan(result.tipp.pkg, RDStore.TIPP_STATUS_DELETED, new Date()).size()


    if (!result.tipp) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label'), params.id])
      redirect action: 'list'
      return
    }
    result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = :participant", [participant: result.tipp] )

    result
  }
}
