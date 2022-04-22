package de.laser

import de.laser.helper.DateUtils
import de.laser.annotations.DebugInfo
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.transaction.TransactionStatus

import java.text.SimpleDateFormat

/**
 * This controller manages library reader number related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class ReaderNumberController  {

	/**
	 * Creates a new reader number for the given institution
	 * @return redirect to the updated reader number table
	 */
	@DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
			Map<String, Object> rnData = params.clone()
			if (params.dueDate)
				rnData.dueDate = sdf.parse(params.dueDate)

			rnData.org = Org.get(params.orgid)
			rnData.referenceGroup = RefdataValue.get(params.referenceGroup)
			rnData.value = new BigDecimal(params.value)
			ReaderNumber numbersInstance = new ReaderNumber(rnData)
			if (! numbersInstance.save()) {
				flash.error = message(code: 'default.not.created.message', args: [message(code: 'readerNumber.number.label')])
				log.error(numbersInstance.errors.toString())
			}
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id: params.orgid, tableA: params.tableA, tableB: params.tableB, sort: params.sort, order: params.order]
    }

	/**
	 * Takes the submitted parameters and updates the given reader number with the given parameter map
	 * @return the updated reader number table
	 */
	@DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			ReaderNumber numbersInstance = ReaderNumber.get(params.id)
			Map<String, Object> rnData = params.clone()
			if (! numbersInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id])
			}
			SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
			rnData.referenceGroup = RefdataValue.get(params.referenceGroup)
			if(params.dueDate)
				rnData.dueDate = sdf.parse(params.dueDate)
			rnData.value = new BigDecimal(params.value)
			numbersInstance.properties = rnData
			if (! numbersInstance.save()) {
				flash.error = message(code:'default.not.updated.message', args: [message(code: 'readerNumber.label'), numbersInstance.id])
				log.error(numbersInstance.errors.toString())
			}
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id: params.orgid, tableA: params.tableA, tableB: params.tableB, sort: params.sort, order: params.order]
    }

	/**
	 * Deletes the given reader numbers, specified by their grouping unit
	 * @return the updated reader number table
	 */
	@DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			List<Long> numbersToDelete = []
			Org org = Org.get(params.org)
			if(params.number) {
				ReaderNumber rn = ReaderNumber.get(params.number)
				rn.delete()
			}
			else if(params.dueDate) {
				Date dueDate = DateUtils.parseDateGeneric(params.dueDate)
				numbersToDelete.addAll(ReaderNumber.findAllByDueDateAndOrg(dueDate,org).collect{ ReaderNumber rn -> rn.id })
			}
			else if(params.semester) {
				RefdataValue semester = RefdataValue.get(params.semester)
				numbersToDelete.addAll(ReaderNumber.findAllBySemesterAndOrg(semester,org).collect{ ReaderNumber rn -> rn.id })
			}
			else if(params.referenceGroup) {
				numbersToDelete.addAll(ReaderNumber.findAllByReferenceGroupAndOrg(RefdataValue.get(params.referenceGroup),org).collect{ ReaderNumber rn -> rn.id })
			}
			if (numbersToDelete) {
				RefdataValue.executeUpdate('delete from ReaderNumber rn where rn.id in (:ids)',[ids:numbersToDelete])
			}
		}
		redirect(url: request.getHeader('referer'))
    }
}
