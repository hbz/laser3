package de.laser

import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.transaction.TransactionStatus

import java.text.SimpleDateFormat
import java.time.Year

/**
 * This controller manages library reader number related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class ReaderNumberController  {

	EscapeService escapeService

	/**
	 * Creates a new reader number for the given institution
	 * @return redirect to the updated reader number table
	 */
	@DebugInfo(isInstEditor = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
    def create() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			Map<String, Object> configMap = params.clone()
			configMap.value = escapeService.parseFinancialValue(params.value)
			ReaderNumber numbersInstance = ReaderNumber.construct(configMap)
			if (! numbersInstance) {
				flash.error = message(code: 'default.not.created.message', args: [message(code: 'readerNumber.number.label')]) as String
			}
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id: params.orgid, tableA: params.tableA, tableB: params.tableB, sort: params.sort, order: params.order]
    }

	/**
	 * Takes the submitted parameters and updates the given reader number with the given parameter map
	 * @return the updated reader number table
	 */
	@DebugInfo(isInstEditor = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
    def edit() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			ReaderNumber numbersInstance = ReaderNumber.get(params.id)
			Map<String, Object> rnData = params.clone()
			if (! numbersInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id]) as String
			}
			SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
			rnData.referenceGroup = RefdataValue.get(params.referenceGroup)
			if(params.year)
				rnData.year = sdf.parse(params.year)
			rnData.value = new BigDecimal(params.value)
			numbersInstance.properties = rnData
			if (! numbersInstance.save()) {
				flash.error = message(code:'default.not.updated.message', args: [message(code: 'readerNumber.label'), numbersInstance.id]) as String
				log.error(numbersInstance.errors.toString())
			}
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id: params.orgid, tableA: params.tableA, tableB: params.tableB, sort: params.sort, order: params.order]
    }

	/**
	 * Deletes the given reader numbers, specified by their grouping unit
	 * @return the updated reader number table
	 */
	@DebugInfo(isInstEditor = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
    def delete() {
		ReaderNumber.withTransaction { TransactionStatus ts ->
			List<Long> numbersToDelete = []
			Org org = Org.get(params.org)
			if(params.number) {
				ReaderNumber rn = ReaderNumber.get(params.number)
				rn.delete()
			}
			else if(params.year) {
				Year year = Year.parse(params.year)
				numbersToDelete.addAll(ReaderNumber.findAllByYearAndOrg(year,org).collect{ ReaderNumber rn -> rn.id })
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
