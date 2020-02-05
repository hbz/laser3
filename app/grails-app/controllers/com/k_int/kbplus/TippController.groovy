package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class TippController extends AbstractDebugController {

  def springSecurityService
  def contextService

  @Secured(['ROLE_USER'])
  def show() { 
    Map<String, Object> result = [:]

    result.user = User.get(springSecurityService.principal.id)
    result.editable = SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

    result.tipp = TitleInstancePackagePlatform.get(params.id)
    result.titleInstanceInstance = result.tipp.title

    if (!result.titleInstanceInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label'), params.id])
      redirect action: 'list'
      return
    }

    params.max = Math.min(params.max ? params.int('max') : 10, 100)
    def paginate_after = params.paginate_after ?: 19;
    result.max = params.max
    result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

    def base_qry = "from TitleInstancePackagePlatform as tipp where tipp.title = ? and tipp.status != ? "
    def qry_params = [result.titleInstanceInstance, RDStore.TIPP_STATUS_DELETED]

    if ( params.filter ) {
      base_qry += " and lower(tipp.pkg.name) like ? "
      qry_params.add("%${params.filter.trim().toLowerCase()}%")
    }

    if ( params.endsAfter && params.endsAfter.length() > 0 ) {
      SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
      def d = sdf.parse(params.endsAfter)
      base_qry += " and (select max(tc.endDate) from TIPPCoverage tc where tc.tipp = tipp) >= ?"
      qry_params.add(d)
    }

    if ( params.startsBefore && params.startsBefore.length() > 0 ) {
      SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
      def d = sdf.parse(params.startsBefore)
      base_qry += " and (select min(tc.startDate) from TIPPCoverage tc where tc.tipp = tipp) <= ?"
      qry_params.add(d)
    }

    if ( ( params.sort != null ) && ( params.sort.length() > 0 ) ) {
      base_qry += " order by lower(${params.sort}) ${params.order}"
    }
    else {
      base_qry += " order by lower(tipp.title.title) asc"
    }

    log.debug("Base qry: ${base_qry}, params: ${qry_params}, result:${result}");
    // result.tippList = TitleInstancePackagePlatform.executeQuery("select tipp "+base_qry, qry_params, [max:result.max, offset:result.offset]);
    result.tippList = TitleInstancePackagePlatform.executeQuery("select tipp "+base_qry, qry_params)
    result.num_tipp_rows = TitleInstancePackagePlatform.executeQuery("select tipp.id "+base_qry, qry_params ).size()

    result

  }
}
