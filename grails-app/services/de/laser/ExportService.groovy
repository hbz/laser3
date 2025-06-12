package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractReport
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigMapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.CostInformationDefinition
import de.laser.finance.PriceItem
import de.laser.helper.Profiler
import de.laser.http.BasicHttpClient
import de.laser.remote.Wekb
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.storage.RDConstants
import de.laser.system.SystemEvent
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.stats.CounterCheck
import de.laser.utils.LocaleUtils
import de.laser.wekb.Platform
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.slurpersupport.GPathResult
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFDataFormat
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.MessageSource

import java.awt.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.List

/**
 * This service should contain the methods required to build the different exported files.
 * CSV methods will stream out the content of the file to a given output.
 * XML methods are provided to build the XML document
 * JSON methods build a Map object which can then be converted into Json.
 *
 * To be modified: the now specialised methods should be generalised into one method generating all exports.
 */
@Transactional
class ExportService {

	static final String EXCEL = 'xlsx'
	static final String KBART = 'kbart'

	BatchQueryService batchQueryService
	ContextService contextService
	EscapeService escapeService
	GokbService gokbService
	IssueEntitlementService issueEntitlementService
	MessageSource messageSource
	StatsSyncService statsSyncService
	SubscriptionControllerService subscriptionControllerService
	SubscriptionService subscriptionService

	SimpleDateFormat formatter = DateUtils.getSDF_yyyyMMdd()

	/**
	 * new CSV/TSV export interface - should subsequently replace StreamOutLicenseCSV, StreamOutSubsCSV and StreamOutTitlesCSV
	 * expect data in structure:
	 * @param titleRow {@link Collection} of column headers [header1,header2,...,headerN]
	 * @param columnData {@link Collection} of the rows, each row is itself a {@link Collection}:
	 * [
	 *   [column1, column2, ..., columnN], //for row 1
	 *   [column1, column2, ..., columnN], //for row 2
	 *   ...
	 *   [column1, column2, ..., columnN]  //for row N
	 * ]
	 * @return the table formatted as a character-separated string
	 */
	String generateSeparatorTableString(Collection titleRow, Collection columnData,String separator) {
		List output = []
		if(titleRow)
			output.add(titleRow.join(separator))
		columnData.each { row ->
			if(row instanceof Map || row instanceof GroovyRowResult) {
				output.add(row.values().join(separator).replaceAll('null', ''))
			}
			else {
				if (row instanceof String && row.trim())
					output.add(row)
				else if(row.size() > 0)
					output.add(row.join(separator).replaceAll('null',''))
				else output.add(" ")
			}
		}
		output.join("\n")
	}

	/**
	 * new XSLX export interface - should subsequently collect the Excel export points
	 * @param sheets the Map of sheets to export. Expect data in structure:
	 * [sheet:
	 *   titleRow: [colHeader1, colHeader2, ..., colHeaderN]
	 *   columnData:[
	 *     [field:field1,style:style1], //for row 1
	 *     [field:field2,style:style2], //for row 2
	 *     ...,
	 *     [field:fieldN,style:styleN]  //for row N
	 *   ]
	 * ]
	 * @return an Excel map as SXSSFWorkbook
	 */
    SXSSFWorkbook generateXLSXWorkbook(Map sheets) {
		Locale locale = LocaleUtils.getCurrentLocale()
		XSSFWorkbook wb = new XSSFWorkbook()
		POIXMLProperties xmlProps = wb.getProperties()
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
		coreProps.setCreator(messageSource.getMessage('laser',null, locale))
		XSSFCellStyle csPositive = wb.createCellStyle()
		csPositive.setFillForegroundColor(new XSSFColor(new Color(198,239,206)))
		csPositive.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle csNegative = wb.createCellStyle()
		csNegative.setFillForegroundColor(new XSSFColor(new Color(255,199,206)))
		csNegative.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle csNeutral = wb.createCellStyle()
		csNeutral.setFillForegroundColor(new XSSFColor(new Color(255,235,156)))
		csNeutral.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle csNeutral2 = wb.createCellStyle()
		csNeutral2.setFillForegroundColor(new XSSFColor(new Color(255,205,156)))
		csNeutral2.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle bold = wb.createCellStyle()
		XSSFFont font = wb.createFont()
		font.setBold(true)
		bold.setFont(font)
		XSSFCellStyle lb = wb.createCellStyle()
		lb.setWrapText(true)
		SXSSFWorkbook output = new SXSSFWorkbook(wb,50,true)
		output.setCompressTempFiles(true)
		sheets.entrySet().eachWithIndex { sheetData, index ->
			try {
				String title = sheetData.key
				List titleRow = (List) sheetData.value.titleRow
				List columnData = (List) sheetData.value.columnData

				if (title.length() > 31) {
					title = title.substring(0, 31-3);
					title = title + "_${index+1}"
				}

				Sheet sheet = output.createSheet(title)
				sheet.setAutobreaks(true)
				int rownum = 0
				Row headerRow = sheet.createRow(rownum++)
				headerRow.setHeightInPoints(16.75f)
				titleRow.eachWithIndex{ colHeader, int i ->
					Cell cell = headerRow.createCell(i)

					if(colHeader instanceof String){
						cell.setCellValue(colHeader)
					}

					if(colHeader instanceof Map) {
						cell.setCellValue(colHeader.field)
						switch (colHeader.style) {
							case 'positive': cell.setCellStyle(csPositive)
								break
							case 'neutral': cell.setCellStyle(csNeutral)
								break
							case 'neutral2': cell.setCellStyle(csNeutral2)
								break
							case 'negative': cell.setCellStyle(csNegative)
								break
							case 'bold': cell.setCellStyle(bold)
								break
						}
					}
				}
				sheet.createFreezePane(0,1)
				Row row
				Cell cell
				CellStyle numberStyle = wb.createCellStyle()
				XSSFDataFormat numberFormat = wb.createDataFormat()
				//messageSource.getMessage('default.financial.export.format', null, locale)
				//df.getFormat('0,00')
				//8
				numberStyle.setDataFormat(numberFormat.getFormat("#,##0.00"))
				columnData.each { rowData ->
					int cellnum = 0
					row = sheet.createRow(rownum)
					rowData.each { cellData ->
						if(cellData instanceof String)
							cellData = JSON.parse(cellData)
						cell = row.createCell(cellnum++)
						if (cellData.field instanceof String) {
							try {
								BigDecimal financial = new BigDecimal(cellData.field)
								cell.setCellValue(financial.toDouble())
								cell.setCellStyle(numberStyle)
							}
							catch (NumberFormatException ignored) {
								cell.setCellValue((String) cellData.field)
								if(cellData.field.contains('\n'))
									cell.setCellStyle(lb)
							}
						} else if (cellData.field instanceof Integer) {
							cell.setCellValue((Integer) cellData.field)
						} else if (cellData.field instanceof Double || cellData.field instanceof BigDecimal) {
							cell.setCellValue((Double) cellData.field)
							cell.setCellStyle(numberStyle)
						}
						switch(cellData.style) {
							case 'positive': cell.setCellStyle(csPositive)
								break
							case 'neutral': cell.setCellStyle(csNeutral)
								break
							case 'neutral2': cell.setCellStyle(csNeutral2)
								break
							case 'negative': cell.setCellStyle(csNegative)
								break
							case 'bold': cell.setCellStyle(bold)
								break
						}
					}
					rownum++
				}
				for(int i = 0;i < titleRow.size(); i++) {
					try {
						sheet.trackColumnForAutoSizing(i)
						sheet.autoSizeColumn(i)
					}
					catch (Exception e) {
						log.error("Exception in column ${i}: ${e}")
					}
				}
			}
			catch (ClassCastException e) {
				log.error("Data delivered in inappropriate structure!")
			}
		}
        output
    }

	/**
	 * Retrieves for the given property definition type and organisation of list of headers, containing property definition names. Includes custom and private properties
	 * @param propDefConst a {@link PropertyDefinition} constant which property definition type should be loaded
	 * @param contextOrg the context {@link Org}
	 * @return a {@link List} of headers
	 */
	List<String> loadPropListHeaders(Set<PropertyDefinition> propSet) {
		List<String> titles = []
		propSet.each {
			titles.add(it.getI10n('name'))
			titles.add("${it.getI10n('name')} ${messageSource.getMessage('default.notes.plural', null, LocaleUtils.getCurrentLocale())}")
		}
		titles
	}

	/**
	 * Fetches for the given {@link Set} of {@link PropertyDefinition}s the values and inserts them into the cell of the given format
	 * @param propertyDefinitions the {@link Set} of {@link PropertyDefinition}s to read the values off
	 * @param format the format (Excel or CSV) in which the values should be outputted
	 * @param target the target object whose property set should be consulted
	 * @param childObjects a {@link Map} of dependent objects
	 * @return a {@link List} or a {@link List} of {@link Map}s for the export sheet containing the value
	 */
	List processPropertyListValues(Set<PropertyDefinition> propertyDefinitions, String format, def target, Map childObjects, Map objectNames, Org contextOrg) {
		if(!contextOrg)
			contextOrg = contextService.getOrg()
		List cells = []
		SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
		propertyDefinitions.each { PropertyDefinition pd ->
			//log.debug("now processing ${pd.name_de}")
			List<String> value = [], note = []
			String ownerClassName = target.class.name.substring(target.class.name.lastIndexOf(".") + 1)
			List<AbstractPropertyWithCalculatedLastUpdated> propCheck = (new GroovyClassLoader()).loadClass("de.laser.properties.${ownerClassName}Property").executeQuery('select prop from '+ownerClassName+'Property prop where prop.owner = :owner and prop.type = :pd and (prop.stringValue != null or prop.longValue != null or prop.decValue != null or prop.refValue != null or prop.urlValue != null or prop.dateValue != null)', [pd: pd, owner: target])
			AbstractPropertyWithCalculatedLastUpdated prop = null
			if(propCheck)
				prop = propCheck[0]
			if(prop && prop.getValue()) {
				value << prop.getValueInI10n() ?: ' '
			}
			if(prop && prop.note)
				note << prop.note
			if(childObjects) {
				if(target.hasProperty("propertySet")) {
					//childObjects.get(target).each { childObj ->
						//childObj.propertySet.findAll{ AbstractPropertyWithCalculatedLastUpdated childProp -> childProp.type.descr == pd.descr && childProp.type == pd && childProp.value && !childProp.instanceOf && (childProp.tenant == contextOrg || childProp.isPublic) }
						(new GroovyClassLoader()).loadClass("de.laser.properties.${ownerClassName}Property").executeQuery('select prop from '+ownerClassName+'Property prop where prop.owner.instanceOf = :owner and prop.type = :pd and prop.instanceOf = null and (prop.tenant = :ctx or prop.isPublic = true) and (prop.stringValue != null or prop.longValue != null or prop.decValue != null or prop.refValue != null or prop.urlValue != null or prop.dateValue != null)', [pd: pd, ctx: contextOrg, owner: target]).each { AbstractPropertyWithCalculatedLastUpdated childProp ->
							if(childProp.getValue()) {
								if(childProp.refValue)
									value << "${childProp.refValue.getI10n('value')} (${objectNames.get(childProp.owner)})"
								else
									value << childProp.getValue() ? "${childProp.getValue()} (${objectNames.get(childProp.owner)})" : ' '
							}
							if(childProp.note) {
								note << childProp.note
							}
						}
					//}
				}
			}
			def cell
			switch(format) {
				case [ "xls", "xlsx" ]:
					cell = [field: value.join('\n'), style: null]
					cells.add(cell)
					cell = [field: note.join('\n'), style: null]
					cells.add(cell)
					break
				case "csv":
					cell = value.join('|').replaceAll(',',';')
					cells.add(cell)
					cell = note.join('|').replaceAll(',',';')
					cells.add(cell)
					break
			}
		}
		cells
	}

	/**
	 * Generate a usage report based on the given title data. The export is COUNTER compliant unless it is used for
	 * the title selection survey; then, additional data is needed which is not covered by the COUNTER compliance
	 * @param params the filter parameter map
	 * @param data the retrieved and filtered COUNTER data
	 * @return an Excel worksheet of the usage report, either according to the COUNTER 4 or COUNTER 5 format
	 */
	Map<String, Object> generateReport(GrailsParameterMap params) {
		EhcacheWrapper userCache = contextService.getUserCache("/subscription/stats")
		userCache.put('progress', 0)
		userCache.put('label', 'Bereite Laden vor ...')
		Profiler prf = new Profiler()
		prf.setBenchmark('start export')
		Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
		if(!result)
			return null
		Subscription refSub
		Org customer = result.subscription.getSubscriberRespConsortia()
		boolean allTitles = false
		if(subscriptionService.countCurrentIssueEntitlements(result.subscription) > 0){
			refSub = result.subscription
		}
		else {
			refSub = result.subscription.instanceOf
			allTitles = true
		}
		prf.setBenchmark('get platforms')
		Platform platform = Platform.get(params.platform)
		prf.setBenchmark('get namespaces')
		IdentifierNamespace propIdNamespace = IdentifierNamespace.findByNs(platform.titleNamespace)
		Set<IdentifierNamespace> namespaces = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, TitleInstancePackagePlatform.class.name)] as Set<IdentifierNamespace>
		namespaces.add(propIdNamespace)
		prf.setBenchmark('get date ranges')
		Map<String, Object> dateRangeParams = subscriptionControllerService.getDateRange(params, result.subscription)
		XSSFWorkbook workbook
		Row row
		Cell cell
		SXSSFSheet sheet
        Set<String> metricTypes = params.list('metricType')
		String reportType = params.reportType
		Map<String, Object> titles = [:] //structure: namespace -> value -> tipp
		Set titlesSorted = [] //fallback structure to preserve sorting
		prf.setBenchmark('prepare title identifier map')
        if(reportType in Counter4Report.COUNTER_4_TITLE_REPORTS || reportType in Counter5Report.COUNTER_5_TITLE_REPORTS) {
			userCache.put('label', 'Hole Titel ...')
			Map<String, Object> idSubsetQueryParams = [refSub: refSub, current: RDStore.TIPP_STATUS_CURRENT]
			if(allTitles) {
				titlesSorted = TitleInstancePackagePlatform.executeQuery('select new map(tipp.id as tippID, tipp.name as tippName) from TitleInstancePackagePlatform tipp, SubscriptionPackage sp where tipp.pkg = sp.pkg and sp.subscription = :refSub and tipp.status = :current order by tipp.sortname', idSubsetQueryParams)
			}
			else {
				titlesSorted = IssueEntitlement.executeQuery('select new map(tipp.id as tippID, tipp.name as tippName) from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :refSub and ie.status = :current order by tipp.sortname', idSubsetQueryParams)
			}
			titles = subscriptionControllerService.fetchTitles(titlesSorted['tippID'].toArray())
			userCache.put('progress', 20)
        }
		//reportTypes.each { String reportType ->
		Set<String> columnHeaders = []
		Set<String> monthHeaders = []
		Map<Long, Map> titleRows = [:]
		Map<String, Object> sumsPerYOP
		int rowno = 0
		//revision 4
		if(params.revision == AbstractReport.COUNTER_4) {
			userCache.put('label', 'Hole Daten vom Anbieter ...')
			prf.setBenchmark('before SUSHI call')
			Map<String, Object> queryParams = [reportType: reportType, customer: customer, platform: platform, revision: AbstractReport.COUNTER_4]
			if(params.metricType) {
				queryParams.metricTypes = params.list('metricType')
			}
			else {
				metricTypes.addAll(Counter4Report.METRIC_TYPES.valueOf(reportType).metricTypes)
			}
			queryParams.startDate = dateRangeParams.startDate
			queryParams.endDate = dateRangeParams.endDate
			//the data
			Map<String, Object> requestResponse = getReports(queryParams)
			userCache.put('progress', 40)
			prf.setBenchmark('data fetched from provider')
			if(requestResponse.containsKey('reports')) {
				userCache.put('label', 'Erzeuge Tabelle ...')
				Set<String> availableMetrics = requestResponse.reports.'**'.findAll { node -> node.name() == 'MetricType' }.collect { node -> node.text()}.toSet()
				workbook = new XSSFWorkbook()
				POIXMLProperties xmlProps = workbook.getProperties()
				POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
				coreProps.setCreator(messageSource.getMessage('laser',null,locale))
				SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
				wb.setCompressTempFiles(true)
				metricTypes.each { String metricType ->
					//the data
					if(metricType in availableMetrics) {
						sheet = wb.createSheet(metricType)
						sheet.flushRows(10)
						sheet.setAutobreaks(true)
						//the header
						Row headerRow = sheet.createRow(0)
						cell = headerRow.createCell(0)
						LinkedHashSet<String> header = Counter4Report.EXPORTS.valueOf(reportType).header
						cell.setCellValue(header[0])
						cell = headerRow.createCell(1)
						cell.setCellValue(header[1])
						headerRow = sheet.createRow(1)
						cell = headerRow.createCell(0)
						cell.setCellValue(customer.name)
						headerRow = sheet.createRow(2)
						cell = headerRow.createCell(0)
						cell.setCellValue("") //institutional identifier is never used but must appear according to reference
						headerRow = sheet.createRow(3)
						cell = headerRow.createCell(0)
						cell.setCellValue('Period covered by Report:')
						headerRow = sheet.createRow(4)
						cell = headerRow.createCell(0)
						if(dateRangeParams.containsKey('startDate') && dateRangeParams.containsKey('endDate'))
							cell.setCellValue("${DateUtils.getSDF_yyyyMMdd().format(dateRangeParams.startDate)} to ${DateUtils.getSDF_yyyyMMdd().format(dateRangeParams.endDate)}")
						headerRow = sheet.createRow(5)
						cell = headerRow.createCell(0)
						cell.setCellValue("Date run:")
						headerRow = sheet.createRow(6)
						cell = headerRow.createCell(0)
						cell.setCellValue(DateUtils.getSDF_yyyyMMdd().format(new Date()))
						columnHeaders.addAll(Counter4Report.COLUMN_HEADERS.valueOf(reportType).headers)
						if(reportType == Counter4Report.JOURNAL_REPORT_5) {
							Map<String, Object> data = prepareDataWithTitles(titles, reportType, requestResponse)
							titleRows = data.titleRows
							sumsPerYOP = data.sumsPerYOP
							for(String yop: sumsPerYOP.keySet()) {
								monthHeaders << yop
							}
							columnHeaders.addAll(monthHeaders)
							columnHeaders.add('YOP Pre-2000')
							columnHeaders.add('YOP unknown')
						}
						else {
							if(dateRangeParams.containsKey('startDate') && dateRangeParams.containsKey('endDate')) {
								monthHeaders.addAll(dateRangeParams.monthsInRing.collect { Date month -> DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.localeEN).format(month) })
								columnHeaders.addAll(monthHeaders)
							}
						}
						row = sheet.createRow(7)
						columnHeaders.eachWithIndex { String colHeader, int i ->
							cell = row.createCell(i)
							cell.setCellValue(colHeader)
						}
						row = sheet.createRow(8)
						List sumRows = []
						switch(reportType) {
							case Counter4Report.JOURNAL_REPORT_1:
							case Counter4Report.JOURNAL_REPORT_1_GOA: cell = row.createCell(0)
								cell.setCellValue("Total for all titles")
								for(int i = 1; i < 7; i++) {
									cell = row.createCell(i)
									cell.setCellValue("")
								}
								Cell totalCell = row.createCell(7)
								int totalCount = 0
								Map<String, Object> data = prepareDataWithTitles(titles, reportType, requestResponse)
								titleRows = data.titleRows
								int j = 0
								for(Date month: dateRangeParams.monthsInRing) {
									cell = row.createCell(j+10)
									Integer countPerMonth = data.sumRows.get(month) ?: 0
									totalCount += countPerMonth
									cell.setCellValue(countPerMonth)
									j++
								}
								totalCell.setCellValue(totalCount)
								rowno = 9
								break
							case Counter4Report.BOOK_REPORT_1:
							case Counter4Report.BOOK_REPORT_2: cell = row.createCell(0)
								cell.setCellValue("Total for all titles")
								for(int i = 1; i < 7; i++) {
									cell = row.createCell(i)
									cell.setCellValue("")
								}
								Cell totalCell = row.createCell(7)
								int totalCount = 0
								prf.setBenchmark('prepare title rows')
								//dateRangeParams.dateRange, queryParams,
								Map<String, Object> data = prepareDataWithTitles(titles, reportType, requestResponse)
								titleRows = data.titleRows
								int j = 0
								prf.setBenchmark('prepare sum row')
								for(Date month: dateRangeParams.monthsInRing) {
									cell = row.createCell(j+8)
									Integer countPerMonth = data.sumRows.get(metricType)?.get(month) ?: 0
									totalCount += countPerMonth
									cell.setCellValue(countPerMonth)
									j++
								}
								totalCell.setCellValue(totalCount)
								rowno = 9
								break
							case Counter4Report.JOURNAL_REPORT_2:
							case Counter4Report.BOOK_REPORT_3:
							case Counter4Report.BOOK_REPORT_4:
								int totalCount = 0
								cell = row.createCell(8)
								cell.setCellValue(totalCount)
								rowno++
								row = sheet.createRow(rowno)
								Map<String, Object> data = prepareDataWithTitles(titles, reportType, requestResponse)
								titleRows = data.titleRows
								rowno = 9
								break
							case Counter4Report.JOURNAL_REPORT_5: cell = row.createCell(0)
								cell.setCellValue("Total for all journals")
								for(int i = 1; i < 8; i++) {
									cell = row.createCell(i)
									cell.setCellValue("")
								}
								int j = 0
								for(String yop: monthHeaders) {
									cell = row.createCell(j+8)
									Integer countPerMonth = sumsPerYOP.get(yop)
									cell.setCellValue(countPerMonth)
									j++
								}
								rowno = 9
								break
							case Counter4Report.BOOK_REPORT_5:
								Map<String, Object> data = prepareDataWithTitles(titles, reportType, requestResponse)
								titleRows = data.titleRows
								if(titleRows) {
									rowno = 8
									cell = row.createCell(0)
									cell.setCellValue(params.metricType == 'search_reg' ? "Total searches" : "Total searches: Searches: federated and automated")
									cell = row.createCell(2)
									cell.setCellValue(titleRows.keySet().first().platform.name)
									int i = 0
									int totalCount = 0
									prf.setBenchmark('prepare sum row')
									for(Date month: dateRangeParams.monthsInRing) {
										cell = row.createCell(i+9)
										Integer countPerMonth = data.sumRows.get(month) ?: 0
										totalCount += countPerMonth
										cell.setCellValue(countPerMonth)
										i++
									}
									cell = row.createCell(8)
									cell.setCellValue(totalCount)
									rowno++
									row = sheet.createRow(rowno)
								}
								break
							case Counter4Report.DATABASE_REPORT_1:
								int i = 0
								prf.setBenchmark('loop through assembled rows')
								rowno = 9
								Map<String, Map<String, Object>> data = prepareDataWithDatabases(requestResponse, reportType)
								double pointsPerIteration = 20/data.size()
								data.each { String databaseName, Map<String, Object> databasePublishers ->
									databasePublishers.each { String publisherName, Map<String, Object> databaseMetrics ->
										databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
											columnHeaders.eachWithIndex { String colHeader, int c ->
												cell = row.createCell(c)
												cell.setCellValue(metricRow.get(colHeader))
											}
										}
										row = sheet.createRow(rowno)
										rowno++
										i++
										userCache.put('progress', 80+i*pointsPerIteration)
									}
								}
								break
							case Counter4Report.DATABASE_REPORT_2:
								rowno = 9
								Map<String, Object> data = prepareDataWithDatabases(requestResponse, reportType)
								double pointsPerIteration = 20/data.size()
								int totalSum = 0, i = 0
								row = sheet.createRow(rowno)
								data.each { String databaseName, Map<String, Object> databasePublishers ->
									databasePublishers.each { String publisherName, Map<String, Object> databaseMetrics ->
										databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
											columnHeaders.eachWithIndex { String colHeader, int c ->
												cell = row.createCell(c)
												cell.setCellValue(metricRow.get(colHeader))
											}
										}
										rowno++
										row = sheet.createRow(rowno)
										i++
										userCache.put('progress', 80 + i * pointsPerIteration)
									}
								}
								break
							case Counter4Report.PLATFORM_REPORT_1:
								int i = 0
								rowno = 9
								Map<String, Map<String, Object>> data = prepareDataWithDatabases(requestResponse, reportType)
								double pointsPerIteration = 20/data.size()
								data.each { String databaseName, Map<String, Object> databasePublishers ->
									databasePublishers.each { String publisherName, Map<String, Object> databaseMetrics ->
										databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
											columnHeaders.eachWithIndex { String colHeader, int c ->
												cell = row.createCell(c)
												cell.setCellValue(metricRow.get(colHeader))
											}
										}
										row = sheet.createRow(rowno)
										rowno++
										i++
										userCache.put('progress', 80 + i * pointsPerIteration)
									}
								}
								break
						}
						if(titlesSorted) {
							int i = 0
							prf.setBenchmark('loop through assembled rows')
							double pointsPerIteration = 20/titlesSorted.size()
							for(Map title: titlesSorted) {
								Instant start = Instant.now().truncatedTo(ChronoUnit.MICROS)
								Map metricRow = titleRows.get(title.tippID)
								if(metricRow) {
									Map titleRow = metricRow.get(metricType)
									if(titleRow) {
										row = sheet.createRow(i+rowno)
										cell = row.createCell(0)
										cell.setCellValue(title.tippName)
										for(int c = 1; c < columnHeaders.size(); c++) {
											cell = row.createCell(c)
											def empty = ""
											if(columnHeaders[c].matches('\\w{3}-\\d{4}')) {
												empty = 0
												cell.setCellValue(titleRow.get(columnHeaders[c]) ?: empty)
											}
											else
												cell.setCellValue(titleRow.get(columnHeaders[c]) ?: empty)
										}
										i++
										userCache.put('progress', 80+i*pointsPerIteration)
									}
								}
								Duration diff = Duration.between(start, Instant.now().truncatedTo(ChronoUnit.MICROS))
								//log.debug("cell row generated in ${diff} micros")
							}
						}
					}
				}
				List debug = prf.stopBenchmark()
				debug.eachWithIndex { bm, int c ->
					String debugString = "Step: ${c+1}, comment: ${bm[0]} "
					if (c < debug.size() - 1) {
						debugString += debug[c+1][1] - bm[1]
					} else {
						debugString += '=> ' + ( bm[1] - debug[0][1] ) + ' <='
					}
					log.debug(debugString)
				}
				if(wb.getNumberOfSheets() == 0) {
					userCache.put('progress', 0)
					[error: 'noMetricsAvailable']
				}
				else {
					userCache.put('progress', 100)
					[result: wb]
				}
			}
			else {
				userCache.put('progress', 100)
				requestResponse
			}
		}
		//revision 5
		else if(params.revision == AbstractReport.COUNTER_5) {
			userCache.put('label', 'Hole Daten vom Anbieter ...')
			prf.setBenchmark('before SUSHI call')
			Map<String, Object> queryParams = [reportType: reportType, customer: customer, platform: platform, revision: AbstractReport.COUNTER_5]
			if(params.metricType) {
				queryParams.metricTypes = params.list('metricType').join('%7C')
			}
			if(params.accessType) {
				queryParams.accessTypes = params.list('accessType').join('%7C')
			}
			if(params.accessMethod) {
				queryParams.accessMethods = params.list('accessMethod').join('%7C')
			}
			queryParams.startDate = dateRangeParams.startDate
			queryParams.endDate = dateRangeParams.endDate
			//the data
			Map<String, Object> requestResponse = getReports(queryParams)
			if(requestResponse.containsKey('items')) {
				workbook = new XSSFWorkbook()
				POIXMLProperties xmlProps = workbook.getProperties()
				POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
				coreProps.setCreator(messageSource.getMessage('laser',null,locale))
				SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
				wb.setCompressTempFiles(true)
				sheet = wb.createSheet(reportType)
				sheet.flushRows(10)
				sheet.setAutobreaks(true)
				sheet.trackAllColumnsForAutoSizing()

				prf.setBenchmark('data fetched from provider')
				userCache.put('label', 'Erzeuge Tabelle ...')
				//the header
				columnHeaders.addAll(Counter5Report.COLUMN_HEADERS.valueOf(reportType.toUpperCase()).headers)
				Row headerRow = sheet.createRow(0)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_Name")
				cell = headerRow.createCell(1)
				cell.setCellValue(Counter5Report.EXPORT_HEADERS.valueOf(reportType.toUpperCase()).header)
				headerRow = sheet.createRow(1)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_ID")
				cell = headerRow.createCell(1)
				cell.setCellValue(reportType.toUpperCase())
				headerRow = sheet.createRow(2)
				cell = headerRow.createCell(0)
				cell.setCellValue("Release")
				cell = headerRow.createCell(1)
				cell.setCellValue(requestResponse.subRevision)
				headerRow = sheet.createRow(3)
				cell = headerRow.createCell(0)
				cell.setCellValue("Institution_Name")
				cell = headerRow.createCell(1)
				cell.setCellValue(customer.name)
				headerRow = sheet.createRow(4)
				cell = headerRow.createCell(0)
				cell.setCellValue("Institution_ID")
				cell = headerRow.createCell(1)
				String identifierString = customer.ids.findAll{ Identifier id -> id.ns.ns == IdentifierNamespace.ISIL }.collect { Identifier id -> "${id.ns.ns}:${id.value}"}.join("; ")
				cell.setCellValue(identifierString)
				headerRow = sheet.createRow(5)
				cell = headerRow.createCell(0)
				cell.setCellValue("Metric_Types")
				cell = headerRow.createCell(1)
				cell.setCellValue(params.metricType ? params.list('metricType').join(';'): Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).metricTypes)
				headerRow = sheet.createRow(6)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_Filters")
				cell = headerRow.createCell(1)
				String standardFilters = Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).reportFilters
				if(standardFilters == 'as selected') {
					String reportFilters = ''
					if(params.accessType)
						reportFilters += 'Access_Type='+params.list('accessType').join('|')
					if(params.accessMethod) {
						if(reportFilters)
							reportFilters += '; '
						reportFilters += 'Access_Method=' + params.list('accessMethod').join('|')
					}
					cell.setCellValue(reportFilters)
				}
				else
					cell.setCellValue(standardFilters)
				headerRow = sheet.createRow(7)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_Attributes")
				cell = headerRow.createCell(1)
				cell.setCellValue(Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).reportAttributes)
				headerRow = sheet.createRow(8)
				cell = headerRow.createCell(0)
				cell.setCellValue("Exceptions")
				if(requestResponse.header.Exceptions) {
					cell = headerRow.createCell(1)
					List<String> exceptions = []
					requestResponse.header.Exceptions.each { Map exception ->
						exceptions << "${exception.Code}: ${exception.Message} (${exception.Data})"
					}
					cell.setCellValue(exceptions.join('; '))
				}
				headerRow = sheet.createRow(9)
				cell = headerRow.createCell(0)
				cell.setCellValue("Reporting_Period")
				cell = headerRow.createCell(1)
				if(dateRangeParams.containsKey('startDate') && dateRangeParams.containsKey('endDate'))
					cell.setCellValue("Begin_Date:${DateUtils.getSDF_yyyyMMdd().format(dateRangeParams.startDate)}; End_Date=${DateUtils.getSDF_yyyyMMdd().format(dateRangeParams.endDate)}")
				/*else if(counter5Reports) {
					cell.setCellValue("${DateUtils.getSDF_yyyyMMdd().format(counter5Reports.first().reportFrom)} to ${DateUtils.getSDF_yyyyMMdd().format(counter5Reports.last().reportTo)}")
				}*/
				headerRow = sheet.createRow(10)
				cell = headerRow.createCell(0)
				cell.setCellValue("Created")
				cell = headerRow.createCell(1)
				cell.setCellValue(DateUtils.getSDF_yyyyMMddTHHmmssZ().format(new Date()))
				headerRow = sheet.createRow(11)
				cell = headerRow.createCell(0)
				cell.setCellValue("Created_By")
				cell = headerRow.createCell(1)
				cell.setCellValue(messageSource.getMessage('laser', null, locale))
				headerRow = sheet.createRow(12) //the 13th row is mandatory empty
				cell = headerRow.createCell(0)
				cell.setCellValue("")
				if(dateRangeParams.containsKey('startDate') && dateRangeParams.containsKey('endDate'))
					columnHeaders.addAll(dateRangeParams.monthsInRing.collect { Date month -> DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(month) })

				row = sheet.createRow(13)
				columnHeaders.eachWithIndex { String colHeader, int i ->
					cell = row.createCell(i)
					cell.setCellValue(colHeader)
				}
				rowno = 14
				Map<String, Object> data = [:]
				double pointsPerIteration
				switch(reportType.toLowerCase()) {
					case [Counter5Report.PLATFORM_MASTER_REPORT, Counter5Report.PLATFORM_USAGE]:
						data = [:]
						List reportItems = []
						if(requestResponse.items.size() > 1) {
							String platformDataType
							if(result.subscription.packages[0].pkg.contentType in [RefdataValue.getByValueAndCategory('Book', RDConstants.PACKAGE_CONTENT_TYPE), RefdataValue.getByValueAndCategory('Journal', RDConstants.PACKAGE_CONTENT_TYPE)])
								platformDataType = result.subscription.packages.pkg[0].contentType
							reportItems.addAll(requestResponse.items.findAll{ Map itemCand ->
								platform.name.toLowerCase().contains(itemCand.Platform.toLowerCase()) || itemCand.Platform.toLowerCase().contains(platform.name.toLowerCase())
							})
							if(!reportItems) {
								reportItems.addAll(requestResponse.items.findAll{ Map itemCand ->
									platformDataType == itemCand.Data_Type
								})
							}
						}
						else {
							reportItems.addAll(requestResponse.items)
						}
						if(reportItems.size() > 0) {
							pointsPerIteration = 20/reportItems.size()
							for(def reportItem: reportItems) {
								//counter 5.0 structure
								if(reportItem.containsKey('Performance')) {
									for(Map performance: reportItem.Performance) {
										Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
										for(Map instance: performance.Instance) {
											int periodTotal, reportCount = instance.Count as int
											Map<String, Object> metricRow = data.containsKey(instance.Metric_Type) ? data.get(instance.Metric_Type) : [:]
											Map<String, Object> platformRow = metricRow.containsKey(reportItem.Platform) ? metricRow.get(reportItem.Platform) : [:]
											Map<String, Object> dataTypeRow = platformRow.get(reportItem.Data_Type)
											if(!dataTypeRow) {
												dataTypeRow = ['Data_Type': reportItem.Data_Type, 'Platform': reportItem.Platform, 'Metric_Type': instance.Metric_Type]
												periodTotal = 0
											}
											else periodTotal = dataTypeRow.get('Reporting_Period_Total') as int
											dataTypeRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
											periodTotal += reportCount
											dataTypeRow.put('Reporting_Period_Total', periodTotal)
											platformRow.put(reportItem.Data_Type, dataTypeRow)
											metricRow.put(reportItem.Platform, platformRow)
											data.put(instance.Metric_Type, metricRow)
										}
									}
								}
								//counter 5.1 structure
								else if(reportItem.containsKey('Attribute_Performance')) {
									for(Map struct: reportItem.Attribute_Performance) {
										String dataType = struct.Data_Type
										//continue with implementing structure
										for(Map.Entry performance: struct.Performance) {
											for(Map.Entry instance: performance) {
												String metricType = instance.getKey()
												for(Map.Entry reportRow: instance.getValue()) {
													Date reportMonth = DateUtils.getSDF_yyyyMM().parse(reportRow.getKey())
													int periodTotal, reportCount = reportRow.getValue() as int
													Map<String, Object> metricRow = data.containsKey(metricType) ? data.get(metricType) : [:]
													Map<String, Object> platformRow = metricRow.containsKey(reportItem.Platform) ? metricRow.get(reportItem.Platform) : [:]
													Map<String, Object> dataTypeRow = platformRow.get(dataType)
													if(!dataTypeRow) {
														dataTypeRow = ['Data_Type': dataType, 'Platform': reportItem.Platform, 'Metric_Type': metricType]
														periodTotal = 0
													}
													else periodTotal = dataTypeRow.get('Reporting_Period_Total') as int
													dataTypeRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportMonth), reportCount)
													periodTotal += reportCount
													dataTypeRow.put('Reporting_Period_Total', periodTotal)
													platformRow.put(dataType, dataTypeRow)
													metricRow.put(reportItem.Platform, platformRow)
													data.put(performance.getKey(), metricRow)
												}
											}
										}
									}
								}
								int i = 0
								for (Map.Entry<String, Object> metricRow: data) {
									for (Map.Entry<String, Object> platformRow: metricRow.getValue()) {
										for (Map.Entry<String, Object> dataTypeRow : platformRow.getValue()) {
											row = sheet.createRow(i + rowno)
											for (int c = 0; c < columnHeaders.size(); c++) {
												cell = row.createCell(c)
												cell.setCellValue(dataTypeRow.getValue().get(columnHeaders[c]) ?: "")
											}
											i++
											userCache.put('progress', 80+i*pointsPerIteration)
										}
									}
								}
							}
						}
						break
					case Counter5Report.DATABASE_MASTER_REPORT:
						data = prepareDataWithDatabases(requestResponse, reportType)
						int i = 0
						pointsPerIteration = data.size()
						for(Map.Entry<String, Object> databaseRow: data) {
							for(Map.Entry<String, Object> platformRow: databaseRow.getValue()) {
								for(Map.Entry<String, Object> metricRow: platformRow.getValue()) {
									for(Map.Entry<String, Object> dataTypeRow: metricRow.getValue()) {
										row = sheet.createRow(i + rowno)
										for(int c = 0;c < columnHeaders.size(); c++) {
											cell = row.createCell(c)
											cell.setCellValue(dataTypeRow.getValue().get(columnHeaders[c]) ?: "")
										}
										i++
										userCache.put('progress', 80+i*pointsPerIteration)
									}
								}
							}
						}
						break
					case Counter5Report.DATABASE_ACCESS_DENIED:
					case Counter5Report.DATABASE_SEARCH_AND_ITEM_USAGE:
						data = prepareDataWithDatabases(requestResponse, reportType)
						int i = 0
						pointsPerIteration = data.size()
						for(Map.Entry<String, Object> databaseRow: data) {
							for(Map.Entry<String, Object> platformRow: databaseRow.getValue()) {
								for(Map.Entry<String, Object> metricRow: platformRow.getValue()) {
									row = sheet.createRow(i + rowno)
									for(int c = 0;c < columnHeaders.size(); c++) {
										cell = row.createCell(c)
										cell.setCellValue(metricRow.getValue().get(columnHeaders[c]) ?: "")
									}
									i++
									userCache.put('progress', 80+i*pointsPerIteration)
								}
							}
						}
						break
					default: data = prepareDataWithTitles(titles, reportType, requestResponse)
						titleRows = data.titleRows
						break
				}
				if(reportType in Counter5Report.COUNTER_5_TITLE_REPORTS) {
					if(titleRows.size() > 0) {
						pointsPerIteration = titlesSorted.size()
						prf.setBenchmark('loop through assembled rows')
						for(Map title: titlesSorted) {
							Instant start = Instant.now().truncatedTo(ChronoUnit.MICROS)
							Map titleMetrics = titleRows.get(title.tippID)
							for(Map.Entry titleMetric: titleMetrics) {
								Map titleAccessTypes = titleMetric.getValue()
								for(Map.Entry titleAccessType: titleAccessTypes) {
									int i = 0
									Map titleYop = titleAccessType.getValue()
									for(Map.Entry entry: titleYop) {
										Map titleRow = entry.getValue()
										row = sheet.createRow(i+rowno)
										cell = row.createCell(0)
										cell.setCellValue(title.tippName)
										for(int c = 1; c < columnHeaders.size(); c++) {
											cell = row.createCell(c)
											cell.setCellValue(titleRow.get(columnHeaders[c]) ?: "")
										}
										i++
										userCache.put('progress', 80+i*pointsPerIteration)
									}
									rowno += titleYop.size()
								}
							}
							Duration diff = Duration.between(start, Instant.now().truncatedTo(ChronoUnit.MICROS))
							//log.debug("cell row generated in ${diff} micros")
						}
						List debug = prf.stopBenchmark()
						debug.eachWithIndex { bm, int c ->
							String debugString = "Step: ${c+1}, comment: ${bm[0]} "
							if (c < debug.size() - 1) {
								debugString += debug[c+1][1] - bm[1]
							} else {
								debugString += '=> ' + ( bm[1] - debug[0][1] ) + ' <='
							}
							log.debug(debugString)
						}
						[result: wb]
					}
					else {
						[error: 'noSubscribedTitles']
					}
				}
				else if(data.size() > 0) {
					[result: wb]
				}
				else {
					requestResponse
				}
			}
			else requestResponse
		}
		else [error: 'COUNTER revision missing!']
	}

	/**
	 * Assembles the rows containing the usages for each title according to the given report type.
	 * The reports exports are COUNTER-compliant unless if used for other purposes which make the
	 * display of other data necessary as well, i.e. list prices, for pick-and-choose purposes
	 * @param usages the data to prepare
	 * @param reportType the report type which is about to be exported
	 * @param metricType (for COUNTER 4 exports only) the metric typ which should be exported
	 * @return a map of titles with the row containing the columns as specified for the given report
	 */
	Map<String, Object> prepareDataWithTitles(Map<String, Object> titles, String reportType, requestResponse) {
		EhcacheWrapper userCache = contextService.getUserCache("/subscription/stats")
		double pointsPerIteration
		int processed = 0
		Map<Long, Map<String, Map>> titleRows = [:]
		Map<String, Object> result = [:]
		Calendar limit = GregorianCalendar.getInstance()
		limit.set(2000, 0, 1)
		if(reportType in Counter5Report.COUNTER_5_REPORTS) {
			Map<Date, Integer> countsPerMonth = [:]
			pointsPerIteration = 40/requestResponse.items.size()
			for(def reportItem: requestResponse.items) {
				Map<String, String> identifierMap = buildIdentifierMap(reportItem, AbstractReport.COUNTER_5)
				Long tippID = issueEntitlementService.matchReport(titles, identifierMap)
				if(tippID) {
					TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(tippID)
					String isbn = identifierMap.isbn ?: identifierMap.printIdentifier
					String issn = identifierMap.printIdentifier
					String eissn = identifierMap.onlineIdentifier
					String doi = identifierMap.doi
					String proprietary = identifierMap.proprietaryIdentifier
					//counter 5.0 structure
					if(reportItem.containsKey('Performance')) {
						for(Map performance : reportItem.Performance) {
							Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
							for(Map instance : performance.Instance) {
								int periodTotal = 0
								String yopKey = reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : 'empty'
								Map<String, Map<String, Object>> titleMetrics = titleRows.get(tippID)
								if(!titleMetrics)
									titleMetrics = [:]
								Map<String, Map<String, Object>> titlesByAccessType = titleMetrics.get(instance.Metric_Type)
								if(!titlesByAccessType)
									titlesByAccessType = [:]
								Map<String, Map<String, Object>> titlesByYop = titlesByAccessType.get(reportItem.Access_Type)
								if(!titlesByYop)
									titlesByYop = [:]
								Map<String, Object> titleRow = titlesByYop.get(yopKey)
								if(!titleRow) {
									titleRow = [:]
								}
								else {
									periodTotal = titleRow.get("Reporting_Period_Total") as int
								}
								int reportCount = instance.Count as int
								Integer countPerMonth = countsPerMonth.get(reportFrom)
								if(!countPerMonth) {
									countPerMonth = 0
								}
								countPerMonth += reportCount
								countsPerMonth.put(reportFrom, countPerMonth)
								//key naming identical to column headers
								titleRow.put("Publisher", reportItem.Publisher)
								//publisher ID is usually not available
								titleRow.put("Platform", tipp.platform.name)
								titleRow.put("Proprietary_ID", proprietary)
								titleRow.put("Metric_Type", instance.Metric_Type)
								switch(reportType.toLowerCase()) {
									case Counter5Report.TITLE_MASTER_REPORT:
										titleRow.put("DOI", doi)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										break
									case Counter5Report.BOOK_REQUESTS:
									case Counter5Report.BOOK_ACCESS_DENIED: titleRow.put("DOI", doi)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
										titleRow.put("ISBN", isbn)
										titleRow.put("Access_Type", reportItem.Access_Type)
										break
									case Counter5Report.BOOK_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", doi)
										titleRow.put("ISBN", isbn)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
										titleRow.put("Access_Type", reportItem.Access_Type)
										break
									case Counter5Report.JOURNAL_REQUESTS:
									case Counter5Report.JOURNAL_ACCESS_DENIED:
									case Counter5Report.JOURNAL_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", doi)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
										titleRow.put("Access_Type", reportItem.Access_Type)
										break
									case Counter5Report.JOURNAL_REQUESTS_BY_YOP: titleRow.put("DOI", doi)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
										break
									case Counter5Report.ITEM_MASTER_REPORT:
									case Counter5Report.DATABASE_SEARCH_AND_ITEM_USAGE:
									case Counter5Report.MULTIMEDIA_ITEM_REQUESTS:
										titleRow.put("DOI", doi)
										titleRow.put("ISBN", isbn)
										titleRow.put("Print_ISSN", issn)
										titleRow.put("Online_ISSN", eissn)
										titleRow.put("URI", tipp.hostPlatformURL)
										break
								}
								periodTotal += reportCount
								titleRow.put("Reporting_Period_Total", periodTotal)
								//temp solution for journal reports, especially for DUZ, where issues of a journal are currently counted as individual title instances
								String reportMonth = DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom)
								int currentMonthCount = titleRow.containsKey(reportMonth) ? titleRow.get(reportMonth) : 0
								titleRow.put(reportMonth, reportCount+currentMonthCount)
								titlesByYop.put(yopKey, titleRow)
								titlesByAccessType.put(reportItem.Access_Type, titlesByYop)
								titleMetrics.put(instance.Metric_Type, titlesByAccessType)
								titleRows.put(tippID, titleMetrics)
							}
						}
					}
					//counter 5.1 structure
					else if(reportItem.containsKey('Attribute_Performance')) {
						for (Map struct : reportItem.Attribute_Performance) {
							String dataType = struct.Data_Type, accessType = struct.Access_Type,
							yopKey = struct.containsKey('YOP') && struct.get('YOP') != 'null' && struct.get('YOP') != null ? struct.get('YOP') : 'empty'
							for (Map.Entry performance : struct.Performance) {
								for (Map.Entry instance : performance) {
									String metricType = instance.getKey()
									int periodTotal = 0
									for (Map.Entry reportRow : instance.getValue()) {
										int reportCount = reportRow.getValue() as int
										Date reportMonth = DateUtils.getSDF_yyyyMM().parse(reportRow.getKey())
										Map<String, Map<String, Object>> titleMetrics = titleRows.get(tippID)
										if (!titleMetrics)
											titleMetrics = [:]
										Map<String, Map<String, Object>> titlesByAccessType = titleMetrics.get(metricType)
										if (!titlesByAccessType)
											titlesByAccessType = [:]
										Map<String, Map<String, Object>> titlesByYop = titlesByAccessType.get(accessType)
										if (!titlesByYop)
											titlesByYop = [:]
										Map<String, Object> titleRow = titlesByYop.get(yopKey)
										if (!titleRow) {
											titleRow = [:]
										} else {
											periodTotal = titleRow.get("Reporting_Period_Total") as int
										}
										Integer countPerMonth = countsPerMonth.get(reportMonth)
										if (!countPerMonth) {
											countPerMonth = 0
										}
										countPerMonth += reportCount
										countsPerMonth.put(reportMonth, countPerMonth)
										//key naming identical to column headers
										titleRow.put("Publisher", reportItem.Publisher)
										//publisher ID is usually not available
										titleRow.put("Platform", tipp.platform.name)
										titleRow.put("Proprietary_ID", proprietary)
										titleRow.put("Metric_Type", metricType)
										switch (reportType.toLowerCase()) {
											case Counter5Report.TITLE_MASTER_REPORT:
												titleRow.put("DOI", doi)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												break
											case Counter5Report.BOOK_REQUESTS:
											case Counter5Report.BOOK_ACCESS_DENIED: titleRow.put("DOI", doi)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
												titleRow.put("ISBN", isbn)
												titleRow.put("Access_Type", accessType)
												break
											case Counter5Report.BOOK_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", doi)
												titleRow.put("ISBN", isbn)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
												titleRow.put("Access_Type", accessType)
												break
											case Counter5Report.JOURNAL_REQUESTS:
											case Counter5Report.JOURNAL_ACCESS_DENIED:
											case Counter5Report.JOURNAL_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", doi)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
												titleRow.put("Access_Type", accessType)
												break
											case Counter5Report.JOURNAL_REQUESTS_BY_YOP: titleRow.put("DOI", doi)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												titleRow.put("YOP", reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : '')
												break
											case Counter5Report.ITEM_MASTER_REPORT:
											case Counter5Report.DATABASE_SEARCH_AND_ITEM_USAGE:
											case Counter5Report.MULTIMEDIA_ITEM_REQUESTS:
												titleRow.put("DOI", doi)
												titleRow.put("ISBN", isbn)
												titleRow.put("Print_ISSN", issn)
												titleRow.put("Online_ISSN", eissn)
												titleRow.put("URI", tipp.hostPlatformURL)
												break
										}
										periodTotal += reportCount
										titleRow.put("Reporting_Period_Total", periodTotal)
										//temp solution for journal reports, especially for DUZ, where issues of a journal are currently counted as individual title instances
										String reportMonthKey = DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportMonth)
										int currentMonthCount = titleRow.containsKey(reportMonthKey) ? titleRow.get(reportMonthKey) : 0
										titleRow.put(reportMonthKey, reportCount + currentMonthCount)
										titlesByYop.put(yopKey, titleRow)
										titlesByAccessType.put(accessType, titlesByYop)
										titleMetrics.put(metricType, titlesByAccessType)
										titleRows.put(tippID, titleMetrics)
									}
								}
							}
						}
					}
				}
				processed++
				userCache.put('progress', 40+processed*pointsPerIteration)
			}
			result.sumRows =  countsPerMonth
		}
        else if(reportType in Counter4Report.COUNTER_4_REPORTS) {
			Map<String, Map<Date, Integer>> countsPerMonth = [:], sumsPerYOP = [:]
			Set<String> metricsAvailable = []
			pointsPerIteration = 20/requestResponse.reports.size()
			for (GPathResult reportItem: requestResponse.reports) {
				Map<String, String> identifierMap = buildIdentifierMap(reportItem, AbstractReport.COUNTER_4)
				Long tippID = issueEntitlementService.matchReport(titles, identifierMap)
				if(tippID) {
					TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(tippID)
					String isbn = identifierMap.printIdentifier
					String eisbn = identifierMap.onlineIdentifier
					String issn = identifierMap.printIdentifier
					String eissn = identifierMap.onlineIdentifier
					String doi = identifierMap.doi
					String proprietaryIdentifier = identifierMap.proprietaryIdentifier
					for (GPathResult performance : reportItem.'ns2:ItemPerformance') {
						Date reportFrom = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
						String category = performance.'ns2:Category'.text()
						Date yop = null
						if(performance.'@PubYr'.size() > 0) {
							yop = DateUtils.getSDF_yyyy().parse(performance.'@PubYr'.text())
						}
						for (GPathResult instance : performance.'ns2:Instance') {
							int periodTotal = 0, periodHTML = 0, periodPDF = 0, count = Integer.parseInt(instance.'ns2:Count'.text())
							String metricType = instance.'ns2:MetricType'.text()
							metricsAvailable << metricType
							//certain reports wish that for each metric, too ...
							Map<Date, Integer> metricSums = countsPerMonth.containsKey(metricType) ? countsPerMonth.get(metricType) : [:]
							Integer countPerMonth = metricSums.containsKey(reportFrom) ? metricSums.get(reportFrom) : 0
							countPerMonth += count
							metricSums.put(reportFrom, countPerMonth)
							countsPerMonth.put(metricType, metricSums)
							Map<String, Map<String, Object>> titleMetrics = titleRows.get(tippID)
							if(!titleMetrics)
								titleMetrics = [:]
							Map<String, Object> titleRow = titleMetrics.get(metricType)
							if (!titleRow) {
								titleRow = [:]
								//key naming identical to column headers
								titleRow.put("Publisher", reportItem.'ns2:ItemPublisher'.text())
								titleRow.put("Platform", reportItem.'ns2:ItemPlatform'.text())
								titleRow.put("Proprietary Identifier", proprietaryIdentifier)
								titleRow.put("Journal DOI", doi)
								titleRow.put("Book DOI", doi)
								if (reportType == Counter4Report.JOURNAL_REPORT_5) {
									titleRow.put("Print ISSN", issn)
									titleRow.put("Online ISSN", eissn)
								} else {
									titleRow.put("ISBN", eisbn ?: isbn)
									titleRow.put("ISSN", eissn ?: issn)
								}
								DecimalFormat df = new DecimalFormat("###,##0.00")
								df.decimalFormatSymbols = new DecimalFormatSymbols(LocaleUtils.getCurrentLocale())
								if (showPriceDate && priceItems) {
									//listprice_eur
									PriceItem eur = priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }
									PriceItem gbp = priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }
									PriceItem usd = priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }
									titleRow.put("List Price EUR", eur ? df.format(eur.listPrice) : ' ')
									//listprice_gbp
									titleRow.put("List Price GBP", gbp ? df.format(gbp.listPrice) : ' ')
									//listprice_usd
									titleRow.put("List Price USD", usd ? df.format(usd.listPrice) : ' ')
								}
								if (showOtherData) {
									titleRow.put("Year First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyy().format(tipp.dateFirstOnline) : ' ')
									titleRow.put("Date First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyyMMdd().format(tipp.dateFirstOnline) : ' ')
								}
							}
							else {
								if (reportType == Counter4Report.JOURNAL_REPORT_1) {
									periodHTML = titleRow.containsKey("Reporting Period HTML") ? titleRow.get("Reporting Period HTML") as int : 0
									periodPDF = titleRow.containsKey("Reporting Period PDF") ? titleRow.get("Reporting Period PDF") as int : 0
								}
								if (reportType != Counter4Report.JOURNAL_REPORT_5) {
									periodTotal = titleRow.get("Reporting Period Total") as int
								}
							}
							periodTotal += count
							if (reportType != Counter4Report.JOURNAL_REPORT_5) {
								titleRow.put("Reporting Period Total", periodTotal)
								titleRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), count)
							}
							switch (reportType) {
								case Counter4Report.BOOK_REPORT_5: titleRow.put("User activity", metricType == 'search_reg' ? "Regular Searches" : "Searches: federated and automated")
									break
								case Counter4Report.JOURNAL_REPORT_1:
									if (metricType == 'ft_html') {
										periodHTML += count
										titleRow.put("Reporting Period HTML", periodHTML)
									} else if (metricType == 'ft_pdf') {
										periodPDF += count
										titleRow.put("Reporting Period PDF", periodPDF)
									}
									break
								case Counter4Report.JOURNAL_REPORT_5:
									String key
									if (yop < limit.getTime())
										key = "YOP Pre-2000"
									else if (yop == null)
										key = "YOP unknown"
									else
										key = "YOP ${DateUtils.getSDF_yyyy().format(yop)}"
									Integer currVal = titleRow.get(key)
									if (!currVal)
										currVal = 0
									currVal += count
									titleRow.put(key, currVal)
									Integer countPerYOP = sumsPerYOP.get(key)
									if (!countPerYOP) {
										countPerYOP = 0
									}
									countPerYOP += count
									sumsPerYOP.put(key, countPerYOP)
									break
							}
							titleMetrics.put(metricType, titleRow)
							titleRows.put(tippID, titleMetrics)
						}
					}
				}
				processed++
				userCache.put('progress', 40+processed*pointsPerIteration)
			}
			result.metricsAvailable = metricsAvailable
			result.sumRows = countsPerMonth
			result.sumsPerYOP = sumsPerYOP
		}
		userCache.put('progress', 80)
		result.titleRows = titleRows
		result
	}

	/**
	 * Assembles the rows containing the usages for each database according to the given report type
	 * @param requestResponse the response data from the provider's SUSHI API
	 * @param reportType the report type to be exported
	 * @return a {@link Map} containing the export rows for each database, in structure:
	 * [
	 	databaseName: {
	 		metric1: [date1: count, date2: count ... dateN: count]
	 		metric2: [date1: count, date2: count ... dateN: count]
	 		...
	 		metricN: [date1: count, date2: count ... dateN: count]
	 	  }
	 	]
	 */
	Map<String, Object> prepareDataWithDatabases(Map requestResponse, String reportType) {
		EhcacheWrapper userCache = contextService.getUserCache("/subscription/stats")
		double pointsPerIteration
		int processed = 0
		Map<String, Object> databaseRows = [:]
		//mini example: https://connect.liblynx.com/sushi/r5/reports/dr?customer_id=2246867&requestor_id=facd706b-cf11-42f9-8d92-a874e594a218&begin_date=2023-01&end_date=2023-12
		//take https://laser-dev.hbz-nrw.de/subscription/membersSubscriptionsManagement/59727?tab=customerIdentifiers&isSiteReloaded=false as base for customer identifiers!
		if(reportType in Counter5Report.COUNTER_5_REPORTS) {
			pointsPerIteration = 40/requestResponse.items.size()
			for(def reportItem: requestResponse.items) {
				String propID = reportItem.Item_ID.find { Map itemId -> itemId.Type == 'Proprietary' }?.Value
				//counter 5.0 structure
				if(reportItem.containsKey('Performance')) {
					for(Map performance: reportItem.Performance) {
						Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
						for(Map instance: performance.Instance) {
							int periodTotal = 0, reportCount = instance.Count as int
							if(reportType == Counter5Report.DATABASE_MASTER_REPORT) {
								Map<String, Object> databaseRow = databaseRows.get(reportItem.Database) as Map<String, Object>
								if(!databaseRow)
									databaseRow = [:]
								Map<String, Object> platformRow = databaseRow.get(reportItem.Platform) as Map<String, Object>
								if(!platformRow)
									platformRow = [:]
								Map<String, Object> metricRow = platformRow.get(instance.Metric_Type) as Map<String, Object>
								if(!metricRow) {
									metricRow = [:]
								}
								Map<String, Object> dataTypeRow = metricRow.get(reportItem.Data_Type) as Map<String, Object>
								if(!dataTypeRow) {
									dataTypeRow = ['Database': reportItem.Database, 'Publisher': reportItem.Publisher, 'Platform': reportItem.Platform, 'Data_Type': reportItem.Data_Type, 'Metric_Type': instance.Metric_Type, 'Proprietary_ID': propID]
								}
								else periodTotal = dataTypeRow.get('Reporting_Period_Total')
								periodTotal += reportCount
								dataTypeRow.put('Reporting_Period_Total', periodTotal)
								dataTypeRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
								metricRow.put(reportItem.Data_Type, dataTypeRow)
								platformRow.put(instance.Metric_Type, metricRow)
								databaseRow.put(reportItem.Platform, platformRow)
								databaseRows.put(reportItem.Database, databaseRow)
							}
							else {
								Map<String, Object> databaseRow = databaseRows.get(reportItem.Database) as Map<String, Object>
								if(!databaseRow)
									databaseRow = [:]
								Map<String, Object> platformRow = databaseRow.get(reportItem.Platform) as Map<String, Object>
								if(!platformRow)
									platformRow = [:]
								Map<String, Object> metricRow = platformRow.get(instance.Metric_Type) as Map<String, Object>
								if(!metricRow) {
									metricRow = ['Database': reportItem.Database, 'Publisher': reportItem.Publisher, 'Platform': reportItem.Platform, 'Metric_Type': instance.Metric_Type, 'Proprietary_ID': propID]
								}
								else periodTotal = metricRow.get('Reporting_Period_Total')
								periodTotal += reportCount
								metricRow.put('Reporting_Period_Total', periodTotal)
								metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
								platformRow.put(instance.Metric_Type, metricRow)
								databaseRow.put(reportItem.Platform, platformRow)
								databaseRows.put(reportItem.Database, databaseRow)
							}
						}
					}
				}
				//counter 5.1 structure
				else if(reportItem.containsKey('Attribute_Performance')) {
					for(Map struct : reportItem.Attribute_Performance) {
						String dataType = struct.Data_Type
						for (Map.Entry performance : struct.Performance) {
							for (Map.Entry instance : performance) {
								String metricType = instance.getKey()
								int periodTotal = 0
								for (Map.Entry reportRow : instance.getValue()) {
									int reportCount = reportRow.getValue() as int
									Date reportMonth = DateUtils.getSDF_yyyyMM().parse(reportRow.getKey())
									if(reportType == Counter5Report.DATABASE_MASTER_REPORT) {
										Map<String, Object> databaseRow = databaseRows.get(reportItem.Database) as Map<String, Object>
										if(!databaseRow)
											databaseRow = [:]
										Map<String, Object> platformRow = databaseRow.get(reportItem.Platform) as Map<String, Object>
										if(!platformRow)
											platformRow = [:]
										Map<String, Object> metricRow = platformRow.get(metricType) as Map<String, Object>
										if(!metricRow) {
											metricRow = [:]
										}
										Map<String, Object> dataTypeRow = metricRow.get(dataType) as Map<String, Object>
										if(!dataTypeRow) {
											dataTypeRow = ['Database': reportItem.Database, 'Publisher': reportItem.Publisher, 'Platform': reportItem.Platform, 'Data_Type': dataType, 'Metric_Type': metricType, 'Proprietary_ID': propID]
										}
										else periodTotal = dataTypeRow.get('Reporting_Period_Total')
										periodTotal += reportCount
										dataTypeRow.put('Reporting_Period_Total', periodTotal)
										dataTypeRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportMonth), reportCount)
										metricRow.put(dataType, dataTypeRow)
										platformRow.put(metricType, metricRow)
										databaseRow.put(reportItem.Platform, platformRow)
										databaseRows.put(reportItem.Database, databaseRow)
									}
									else {
										Map<String, Object> databaseRow = databaseRows.get(reportItem.Database) as Map<String, Object>
										if(!databaseRow)
											databaseRow = [:]
										Map<String, Object> platformRow = databaseRow.get(reportItem.Platform) as Map<String, Object>
										if(!platformRow)
											platformRow = [:]
										Map<String, Object> metricRow = platformRow.get(metricType) as Map<String, Object>
										if(!metricRow) {
											metricRow = ['Database': reportItem.Database, 'Publisher': reportItem.Publisher, 'Platform': reportItem.Platform, 'Metric_Type': metricType, 'Proprietary_ID': propID]
										}
										else periodTotal = metricRow.get('Reporting_Period_Total')
										periodTotal += reportCount
										metricRow.put('Reporting_Period_Total', periodTotal)
										metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportMonth), reportCount)
										platformRow.put(metricType, metricRow)
										databaseRow.put(reportItem.Platform, platformRow)
										databaseRows.put(reportItem.Database, databaseRow)
									}
								}
							}
						}
					}
				}
				processed++
				userCache.put('progress', 40+processed*pointsPerIteration)
			}
		}
		else if(reportType in Counter4Report.COUNTER_4_REPORTS) {
			pointsPerIteration = 20/requestResponse.reports.size()
			for (GPathResult reportItem: requestResponse.reports) {
				String databaseName = reportItem.'ns2:ItemName'.text()
				for (GPathResult performance: reportItem.'ns2:ItemPerformance') {
					Date reportMonth = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
					for (GPathResult instance: performance.'ns2:Instance') {
						String metricType = instance.'ns2:MetricType'.text(), publisherName = reportItem.'ns2:ItemPublisher'.text()
						int periodTotal = 0, count = Integer.parseInt(instance.'ns2:Count'.text())
						Map<String, Object> databaseRow = databaseRows.get(databaseName) as Map<String, Object>
						if(!databaseRow)
							databaseRow = [:]
						Map<String, Object> publisherRow = databaseRow.get(publisherName) as Map<String, Object>
						if(!publisherRow)
							publisherRow = [:]
						Map<String, Object> metricRow = publisherRow.get(metricType) as Map<String, Object>
						String metricHumanReadable
						switch(metricType) {
							case 'search_reg': metricHumanReadable = 'Regular Searches'
								break
							case 'search_fed': metricHumanReadable = 'Searches-federated and automated'
								break
							case 'record_view': metricHumanReadable = 'Record Views'
								break
							case 'ft_html': metricHumanReadable = 'Record Views HTML'
								break
							case 'ft_total': metricHumanReadable = 'Record Views Total'
								break
							case 'result_click': metricHumanReadable = 'Result Clicks'
								break
							default: metricHumanReadable = metricType
								break
						}
						if(!metricRow) {
							metricRow = ['Database': databaseName, 'Publisher': publisherName, 'Platform': reportItem.'ns2:ItemPlatform'.text()]
						}
						else periodTotal = metricRow.get('Reporting Period Total')
						if(reportType == Counter4Report.DATABASE_REPORT_2)
							metricRow.put('Access denied category', metricHumanReadable)
						else
							metricRow.put('User Activity', metricHumanReadable)
						periodTotal += count
						metricRow.put('Reporting Period Total', periodTotal)
						metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.localeEN).format(reportMonth), count)
						publisherRow.put(metricType, metricRow)
						databaseRow.put(publisherName, publisherRow)
						databaseRows.put(databaseName, databaseRow)
					}
				}
				processed++
				userCache.put('progress', 40+processed*pointsPerIteration)
			}
			/*
            for(AbstractReport report: reports) {
                int periodTotal = 0
                Map<String, Object> databaseRow = databaseRows.get(report.databaseName) as Map<String, Object>
                if(!databaseRow)
                    databaseRow = [:]
                Map<String, Object> metricRow = databaseRow.get(report.metricType) as Map<String, Object>
                String metricHumanReadable
                if(report instanceof Counter4Report) {
                    switch(report.metricType) {
                        case 'search_reg': metricHumanReadable = 'Regular Searches'
                            break
                        case 'search_fed': metricHumanReadable = 'Searches-federated and automated'
                            break
                        case 'record_view': metricHumanReadable = 'Record Views'
                            break
                        case 'ft_html': metricHumanReadable = 'Record Views HTML'
                            break
                        case 'ft_total': metricHumanReadable = 'Record Views Total'
                            break
                        case 'result_click': metricHumanReadable = 'Result Clicks'
                            break
                        default: metricHumanReadable = report.metricType
                            break
                    }
                    if(!metricRow) {
                        metricRow = ['Database': report.databaseName, 'Publisher': report.publisher, 'Platform': Platform.findByGlobalUID(report.platformUID).name]
                    }
                    else periodTotal = metricRow.get('Reporting Period Total')
                    if(report.reportType == Counter4Report.DATABASE_REPORT_1)
                        metricRow.put('User Activity', metricHumanReadable)
                    else if(report.reportType == Counter4Report.DATABASE_REPORT_2)
                        metricRow.put('Access denied category', metricHumanReadable)
                    periodTotal += report.reportCount
                    metricRow.put('Reporting Period Total', periodTotal)
                }
				databaseRow.put(report.metricType, metricRow)
				databaseRows.put(report.databaseName, databaseRow)
            }
			*/
		}
		userCache.put('progress', 80)
		databaseRows
	}

	/**
	 * Sets generic parameters for the upcoming call: revision and SUSHI URL to use.
     * If the suffix "reports" is not contained by the root URL coming from we:kb, it will be suffixed
	 * @param platformRecord the we:kb platform record map containing the SUSHI API data
	 * @return a {@link Map} containing the revision (either counter4 or counter5) and statsUrl wth the URL to call
	 */
	Map<String, String> prepareSushiCall(Map platformRecord, String connectionMethod = 'reports') {
		String revision = null, statsUrl = null, sushiApiAuthenticationMethod = null
		if(platformRecord.counterRegistryApiUuid) {
			String url = "${ConfigMapper.getSushiCounterRegistryUrl()}/${platformRecord.counterRegistryApiUuid}${ConfigMapper.getSushiCounterRegistryDataSuffix()}"
			BasicHttpClient counterApiRegistry = new BasicHttpClient(url)
			try {
				Closure success = { resp, json ->
					if(resp.code() == 200) {
						if(json.containsKey('sushi_services')) {
							Map counterApiConfig = json.sushi_services[0]
							if(counterApiConfig.counter_release in ['5', '5.1'])
								revision = "counter5"
							statsUrl = counterApiConfig.url
							if (!counterApiConfig.url.contains(connectionMethod)) {
								if (counterApiConfig.url.endsWith('/'))
									statsUrl = counterApiConfig.url + connectionMethod
								else statsUrl = counterApiConfig.url + '/'+connectionMethod
							}
							boolean withRequestorId = Boolean.valueOf(counterApiConfig.requestor_id_required), withApiKey = Boolean.valueOf(counterApiConfig.api_key_required), withIpWhitelisting = Boolean.valueOf(counterApiConfig.ip_address_authorization)
							if(withRequestorId) {
								if(withApiKey) {
									sushiApiAuthenticationMethod = AbstractReport.API_AUTH_CUSTOMER_REQUESTOR_API
								}
								else sushiApiAuthenticationMethod = AbstractReport.API_AUTH_CUSTOMER_REQUESTOR
							}
							else if(withApiKey) {
								sushiApiAuthenticationMethod = AbstractReport.API_AUTH_CUSTOMER_API
							}
							else if(withIpWhitelisting) {
								sushiApiAuthenticationMethod = AbstractReport.API_IP_WHITELISTING
							}
						}
						else {
							Map sysEventPayload = [errToken: "noCounterApiConfiguration", errMess: "platform has no COUNTER API configuration", platform: platformRecord.uuid, url: url, callError: true]
							CounterCheck.construct(sysEventPayload)
							SystemEvent.createEvent('STATS_CALL_ERROR', sysEventPayload)
						}
					}
				}
				Closure failure = { resp, reader ->
					Map sysEventPayload = [errToken: "counterRegistryError", errMess: "error on call at COUNTER registry", platform: platformRecord.uuid, url: url, callError: true]
					CounterCheck.construct(sysEventPayload)
					SystemEvent.createEvent('STATS_CALL_ERROR', sysEventPayload)
				}
				counterApiRegistry.get(BasicHttpClient.ResponseType.JSON, success, failure)
			}
			catch (Exception e) {
				Map sysEventPayload = [errToken: "invalidResponse", errMess: "invalid response returned for ${url} - ${e.getMessage()}!", platform: platformRecord.uuid, url: url, callError: true]
				CounterCheck.construct(sysEventPayload)
				SystemEvent.createEvent('STATS_CALL_ERROR', sysEventPayload)
				log.error("stack trace: ", e)
			}
			finally {
				counterApiRegistry.close()
			}
		}
		//fallback configuration
		if(!revision) {
			if(platformRecord.counterR5SushiApiSupported == "Yes") {
				revision = 'counter5'
				statsUrl = platformRecord.counterR5SushiServerUrl
				if (!platformRecord.counterR5SushiServerUrl.contains('reports')) {
					if (platformRecord.counterR5SushiServerUrl.endsWith('/'))
						statsUrl = platformRecord.counterR5SushiServerUrl + 'reports'
					else statsUrl = platformRecord.counterR5SushiServerUrl + '/reports'
				}
				sushiApiAuthenticationMethod = platformRecord.sushiApiAuthenticationMethod
			}
			else if(platformRecord.counterR4SushiApiSupported == "Yes") {
				revision = 'counter4'
				statsUrl = platformRecord.counterR4SushiServerUrl
				/*
                if (!platformRecord.counterR4SushiServerUrl.contains('reports')) {
                    if (platformRecord.counterR4SushiServerUrl.endsWith('/'))
                        statsUrl = platformRecord.counterR4SushiServerUrl + 'reports'
                    else statsUrl = platformRecord.counterR4SushiServerUrl + '/reports'
                }
                */
			}
		}
		[revision: revision, statsUrl: statsUrl, sushiApiAuthenticationMethod: sushiApiAuthenticationMethod]
	}

	String buildQueryArguments(Map configMap, Map platformRecord, CustomerIdentifier ci) {
		String queryArguments = null
		if(configMap.revision == AbstractReport.COUNTER_5) {
			String apiKey = platformRecord.centralApiKey ?: ci.requestorKey
			queryArguments = "?customer_id=${ci.value}"
			String sushiApiAuthenticationMethod = configMap.sushiApiAuthenticationMethod
			if(!sushiApiAuthenticationMethod)
				sushiApiAuthenticationMethod = platformRecord.sushiApiAuthenticationMethod
			switch(sushiApiAuthenticationMethod) {
				case AbstractReport.API_AUTH_CUSTOMER_REQUESTOR:
					if(ci.requestorKey) {
						queryArguments += "&requestor_id=${ci.requestorKey}"
					}
					break
				case AbstractReport.API_AUTH_CUSTOMER_API:
				case AbstractReport.API_AUTH_REQUESTOR_API:
					if(ci.requestorKey) {
						queryArguments += "&api_key=${ci.requestorKey}"
					}
					break
				case AbstractReport.API_AUTH_CUSTOMER_REQUESTOR_API:
					if(ci.requestorKey && platformRecord.centralApiKey) {
						queryArguments += "&requestor_id=${ci.requestorKey}&api_key=${platformRecord.centralApiKey}"
					}
					else if(ci.requestorKey && !platformRecord.centralApiKey) {
						//the next fancy solution ... this time: Statista!
						queryArguments += "&requestor_id=${ci.value}&api_key=${ci.requestorKey}"
					}
					break
				case AbstractReport.API_IP_WHITELISTING:
					break
				default:
					if(ci.requestorKey || apiKey) {
						queryArguments += "&requestor_id=${ci.requestorKey}&api_key=${apiKey}"
					}
					break
			}
			if(platformRecord.counterR5SushiPlatform)
				queryArguments += "&platform=${platformRecord.counterR5SushiPlatform}"
		}
		else if(configMap.revision == AbstractReport.COUNTER_4) {
			StreamingMarkupBuilder requestBuilder = new StreamingMarkupBuilder()
			Date now = new Date()
			//building up fake request in order to check availability
			def requestBody = requestBuilder.bind {
				mkp.xmlDeclaration()
				mkp.declareNamespace(x: "http://schemas.xmlsoap.org/soap/envelope/")
				mkp.declareNamespace(cou: "http://www.niso.org/schemas/sushi/counter")
				mkp.declareNamespace(sus: "http://www.niso.org/schemas/sushi")
				x.Envelope {
					x.Header {}
					x.Body {
						cou.ReportRequest(Created: DateUtils.getSDF_yyyyMMddTHHmmss().format(now), ID: '?') {
							sus.Requestor {
								sus.ID(ci.requestorKey)
								sus.Name('?')
								sus.Email('?')
							}
							sus.CustomerReference {
								sus.ID(ci.value)
								sus.Name('?')
							}
							sus.ReportDefinition(Name: 'PR1', Release: 4) {
								sus.Filters {
									sus.UsageDateRange {
										sus.Begin('2021-01-01')
										//if (currentYearEnd.before(calendarConfig.now))
										sus.End('2021-01-31')
										/*else {
                                            sus.End(calendarConfig.now.format("yyyy-MM-dd"))
                                        }*/
									}
								}
							}
						}
					}
				}
			}
			queryArguments = requestBody.toString()
		}
		queryArguments
	}

	/**
	 * Builds the SUSHI request and fetches the data from the provider's SUSHI API server
	 * @param configMap the map containing the request parameters
	 * @return a {@link Map}
	 */
	Map<String, Object> getReports(Map configMap) {
		Map<String, Object> result = [:]
		SimpleDateFormat monthFormatter = DateUtils.getSDF_yyyyMM()
		Map queryResult = gokbService.executeQuery(Wekb.getSushiSourcesURL(), [:])
		Map platformRecord
		if (queryResult) {
			Map<String, Object> records = queryResult
			if(records.counter4ApiSources.containsKey(configMap.platform.gokbId)) {
				platformRecord = records.counter4ApiSources.get(configMap.platform.gokbId)
			}
			else if(records.counter5ApiSources.containsKey(configMap.platform.gokbId)) {
				platformRecord = records.counter5ApiSources.get(configMap.platform.gokbId)
			}
		}
		if(platformRecord) {
			Map<String, String> statsSource = prepareSushiCall(platformRecord)
			CustomerIdentifier customerId = CustomerIdentifier.findByCustomerAndPlatform(configMap.customer, configMap.platform)
			if(customerId) {
				if(statsSource.revision == 'counter4') {
					StreamingMarkupBuilder requestBuilder = new StreamingMarkupBuilder()
					Date now = new Date()
					def requestBody = requestBuilder.bind {
						mkp.xmlDeclaration()
						mkp.declareNamespace(x: "http://schemas.xmlsoap.org/soap/envelope/")
						mkp.declareNamespace(cou: "http://www.niso.org/schemas/sushi/counter")
						mkp.declareNamespace(sus: "http://www.niso.org/schemas/sushi")
						x.Envelope {
							x.Header {}
							x.Body {
								cou.ReportRequest(Created: DateUtils.getSDF_yyyyMMddTHHmmss().format(now), ID: '?') {
									sus.Requestor {
										sus.ID(customerId.requestorKey)
										sus.Name('?')
										sus.Email('?')
									}
									sus.CustomerReference {
										sus.ID(customerId.value)
										sus.Name('?')
									}
									sus.ReportDefinition(Name: configMap.reportType, Release: 4) {
										sus.Filters {
											sus.UsageDateRange {
												sus.Begin(DateUtils.getSDF_yyyyMMdd().format(configMap.startDate))
												//if (currentYearEnd.before(calendarConfig.now))
												sus.End(DateUtils.getSDF_yyyyMMdd().format(configMap.endDate))
												/*else {
                                                    sus.End(calendarConfig.now.format("yyyy-MM-dd"))
                                                }*/
											}
										}
									}
								}
							}
						}
					}
					//log.debug(requestBody.toString())
					result.putAll(statsSyncService.fetchXMLData(statsSource.statsUrl, customerId, requestBody))
				}
				else if(statsSource.revision == 'counter5') {
					String url = statsSource.statsUrl + "/${configMap.reportType}"
					url += buildQueryArguments(configMap, platformRecord, customerId)
					if(platformRecord.counterR5SushiPlatform)
						url += "&platform=${platformRecord.counterR5SushiPlatform}"
					url += configMap.metricTypes ? "&metric_type=${configMap.metricTypes}" : ""
					url += configMap.accessTypes ? "&access_type=${configMap.accessTypes}" : ""
					url += configMap.accessMethods ? "&access_method=${configMap.accessMethods}" : ""
					url += "&begin_date=${monthFormatter.format(configMap.startDate)}&end_date=${monthFormatter.format(configMap.endDate)}"
					if(statsSource.statsUrl.contains('r51'))
						result.subRevision = '5.1'
					else result.subRevision = '5'
					result.putAll(statsSyncService.fetchJSONData(url, customerId))
				}
			}
		}
		else
			result.error = 'no we:kb platform available'
		result
	}

	/**
	 * Assembles the identifiers of the given report item in order to enable more performant title match
	 * @param reportItem the report item whose identifiers should be collected
	 * @param revision the COUNTER revision of the report item
	 * @return a {@link Map} with unified identifiers
	 * @see AbstractReport
	 */
	Map<String, String> buildIdentifierMap(reportItem, String revision) {
		Map<String, String> identifierMap = [:]
		if(revision == AbstractReport.COUNTER_4) {
			reportItem.'ns2:ItemIdentifier'.each { identifier ->
				switch (identifier.'ns2:Type'.text().toLowerCase()) {
					case 'isbn': identifierMap.isbn = identifier.'ns2:Value'.text()
						break
					case 'online_isbn':
					case 'online_issn':
						identifierMap.onlineIdentifier = identifier.'ns2:Value'.text()
						break
					case 'print_issn':
					case 'print_isbn':
						identifierMap.printIdentifier = identifier.'ns2:Value'.text()
						break
					case 'doi': identifierMap.doi = identifier.'ns2:Value'.text()
						break
					case 'proprietary_id': identifierMap.proprietaryIdentifier = identifier.'ns2:Value'.text()
						break
				}
			}
		}
		else if(revision == AbstractReport.COUNTER_5) {
			reportItem["Item_ID"].each { idData ->
				if(idData.getMetaClass().respondsTo(idData, 'getKey')) {
					switch (idData.getKey().toLowerCase()) {
						case 'isbn': identifierMap.isbn = idData.Value
							break
						case 'online_issn':
						case 'online_isbn': identifierMap.onlineIdentifier = idData.Value
							break
						case 'print_isbn':
						case 'print_issn': identifierMap.printIdentifier = idData.Value
							break
						case 'doi': identifierMap.doi = idData.Value
							break
						case 'proprietary_id': identifierMap.proprietaryIdentifier = idData.Value
							break
					}
				}
				else {
					switch (idData.Type.toLowerCase()) {
						case 'isbn': identifierMap.isbn = idData.Value
							break
						case 'online_issn':
						case 'online_isbn': identifierMap.onlineIdentifier = idData.Value
							break
						case 'print_isbn':
						case 'print_issn': identifierMap.printIdentifier = idData.Value
							break
						case 'doi': identifierMap.doi = idData.Value
							break
						case 'proprietary_id': identifierMap.proprietaryIdentifier = idData.Value
							break
					}
				}
			}
		}
		identifierMap
	}

	/**
	 * Generates an Excel workbook of the property usage for the given {@link Map} of {@link PropertyDefinition}s.
	 * The structure of the workbook is the same as of the view _manage(Private)PropertyDefintions.gsp
	 * @param propDefs - the {@link Map} of {@link PropertyDefinition}s whose usages should be printed
	 * @return a {@link Map} of the Excel sheets containing the table data
	 */
	Map<String,Map> generatePropertyUsageExportXLS(Map propDefs) {
		Locale locale = LocaleUtils.getCurrentLocale()
		List titleRow = [messageSource.getMessage('default.name.label',null,locale),
						 messageSource.getMessage('propertyDefinition.expl.label',null,locale),
						 messageSource.getMessage('default.type.label',null,locale),
						 messageSource.getMessage('propertyDefinition.count.label',null,locale),
						 messageSource.getMessage('default.hardData.tooltip',null,locale),
						 messageSource.getMessage('default.multipleOccurrence.tooltip',null,locale),
						 messageSource.getMessage('default.isUsedForLogic.tooltip',null,locale),
						 messageSource.getMessage('default.mandatory.tooltip',null,locale),
						 messageSource.getMessage('default.multipleOccurrence.tooltip',null,locale)]
		Map<String,Map> sheetData = [:]
		propDefs.each { Map.Entry propDefEntry ->
			List rows = []
			propDefEntry.value.each { entry ->
				List row = []
				if(entry instanceof PropertyDefinition) {
					PropertyDefinition pd = (PropertyDefinition) entry
					row.add([field: pd.getI10n("name"), style: null])
					row.add([field: pd.getI10n("expl"), style: null])
					String typeString = pd.getLocalizedValue(pd.type)
					if (pd.isRefdataValueType()) {
						List refdataValues = []
						RefdataCategory.getAllRefdataValues(pd.refdataCategory).each { RefdataValue refdataValue ->
							refdataValues << refdataValue.getI10n("value")
						}
						typeString += "(${refdataValues.join('/')})"
					}
					row.add([field: typeString, style: null])
					row.add([field: pd.countOwnUsages(), style: null])
					row.add([field: pd.isHardData ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: pd.multipleOccurrence ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: pd.isUsedForLogic ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: pd.mandatory ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: pd.multipleOccurrence ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
				}
				else if(entry instanceof CostInformationDefinition) {
					CostInformationDefinition cif = (CostInformationDefinition) entry
					row.add([field: cif.getI10n("name"), style: null])
					row.add([field: cif.getI10n("expl"), style: null])
					String typeString = cif.getLocalizedValue(cif.type)
					if (cif.type == RefdataValue.class.name) {
						List refdataValues = []
						RefdataCategory.getAllRefdataValues(cif.refdataCategory).each { RefdataValue refdataValue ->
							refdataValues << refdataValue.getI10n("value")
						}
						typeString += "(${refdataValues.join('/')})"
					}
					row.add([field: typeString, style: null])
					row.add([field: cif.countOwnUsages(), style: null])
					row.add([field: cif.isHardData ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: RDStore.YN_NO.getI10n("value"), style: null])
					row.add([field: RDStore.YN_NO.getI10n("value"), style: null])
				}
				rows.add(row)
			}
			if(propDefEntry.key == CostInformationDefinition.COST_INFORMATION)
				sheetData.put(messageSource.getMessage("costInformationDefinition.label",null,locale),[titleRow:titleRow,columnData:rows])
			else
				sheetData.put(messageSource.getMessage("propertyDefinition.${propDefEntry.key}.label",null,locale),[titleRow:titleRow,columnData:rows])
		}
		sheetData
	}

	/**
	 * Exports the current usage of the property definitions within the defined groups
	 * @param propDefGroups the property definition groups of the institution
	 * @return an Excel worksheet containing the property distribution among the objects grouped per object type and property definition group
	 */
	Map<String,Map> generatePropertyGroupUsageXLS(Map propDefGroups) {
		Locale locale = LocaleUtils.getCurrentLocale()
		List titleRow = [messageSource.getMessage("default.name.label",null,locale),
						 messageSource.getMessage("propertyDefinitionGroup.table.header.description",null,locale),
						 messageSource.getMessage("propertyDefinitionGroup.table.header.properties",null,locale),
						 messageSource.getMessage("propertyDefinitionGroup.table.header.presetShow",null,locale)]
		Map<String,Map> sheetData = [:]
		propDefGroups.each { Map.Entry typeEntry ->
			List rows = []
			typeEntry.value.each { PropertyDefinitionGroup pdGroup ->
				List row = []
				row.add([field:pdGroup.name,style:null])
				row.add([field:pdGroup.description,style:null])
				row.add([field:pdGroup.getPropertyDefinitions().size(),style:null])
				row.add([field:pdGroup.isVisible ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				rows.add(row)
			}
			sheetData.put(messageSource.getMessage("propertyDefinition.${typeEntry.key}.label",null,locale),[titleRow:titleRow,columnData:rows])
		}
		//TODO cost information definition group!
		sheetData
	}

	Map<String, Object> generateTitleExport(Map configMap) {
		log.debug("Begin generateTitleExport")
		Set<Long> tippIDs = configMap.tippIDs, ieIDs = configMap.ieIDs
		boolean withPick = configMap.withPick
		Map<String, Object> export = [:]
		Locale locale = LocaleUtils.getCurrentLocale()
		//log.debug("after title data")
		List<String> titleHeaders
		if(configMap.format == ExportService.KBART) {
			titleHeaders = ['publication_title',
							'print_identifier',
							'online_identifier',
							'date_first_issue_online',
							'num_first_vol_online',
							'num_first_issue_online',
							'date_last_issue_online',
							'num_last_vol_online',
							'num_last_issue_online',
							'title_url',
							'first_author',
							'title_id',
							'embargo_info',
							'coverage_depth',
							'notes',
							'publication_type',
							'publisher_name',
							'date_monograph_published_print',
							'date_monograph_published_online',
							'monograph_volume',
							'monograph_edition',
							'first_editor',
							'parent_publication_title_id',
							'preceding_publication_title_id',
							'package_name',
							'platform_name',
							'last_changed',
							'access_start_date',
							'access_end_date',
							'medium',
							'zdb_id',
							'doi_identifier',
							'ezb_id',
							'title_wekb_uuid',
							'package_wekb_uuid',
							'package_isci',
							'package_isil',
							'package_ezb_anchor',
							'ill_indicator',
							'superceding_publication_title_id',
							'series_name',
							'subject_area',
							'status',
							'access_type',
							'oa_type',
							'zdb_ppn',
							'listprice_eur',
							'listprice_gbp',
							'listprice_usd']
			if(ieIDs) {
				titleHeaders.addAll(['localprice_eur','localprice_gbp','localprice_usd'])
			}
		}
		else {
			titleHeaders = [messageSource.getMessage('tipp.name',null,locale),
							'Print Identifier',
							'Online Identifier',
							messageSource.getMessage('tipp.startDate',null,locale),
							messageSource.getMessage('tipp.startVolume',null,locale),
							messageSource.getMessage('tipp.startIssue',null,locale),
							messageSource.getMessage('tipp.endDate',null,locale),
							messageSource.getMessage('tipp.endVolume',null,locale),
							messageSource.getMessage('tipp.endIssue',null,locale),
							messageSource.getMessage('tipp.hostPlatformURL',null,locale),
							messageSource.getMessage('tipp.firstAuthor',null,locale),
							messageSource.getMessage('tipp.titleId',null,locale),
							messageSource.getMessage('tipp.embargo',null,locale),
							messageSource.getMessage('tipp.coverageDepth',null,locale),
							messageSource.getMessage('tipp.coverageNote',null,locale),
							messageSource.getMessage('tipp.titleType',null,locale),
							messageSource.getMessage('tipp.publisher',null,locale),
							messageSource.getMessage('tipp.dateFirstInPrint',null,locale),
							messageSource.getMessage('tipp.dateFirstOnline',null,locale),
							messageSource.getMessage('tipp.volume',null,locale),
							messageSource.getMessage('tipp.editionNumber',null,locale),
							messageSource.getMessage('tipp.firstEditor',null,locale),
							messageSource.getMessage('tipp.parentTitleId',null,locale),
							messageSource.getMessage('tipp.precedingTitleId',null,locale),
							messageSource.getMessage('package.label',null,locale),
							messageSource.getMessage('platform.label',null,locale),
							messageSource.getMessage('tipp.lastUpdated',null,locale),
							messageSource.getMessage('tipp.accessStartDate',null,locale),
							messageSource.getMessage('tipp.accessEndDate',null,locale),
							messageSource.getMessage('tipp.medium',null,locale),
							IdentifierNamespace.ZDB,
							IdentifierNamespace.DOI,
							IdentifierNamespace.EZB,
							messageSource.getMessage('tipp.tippWekbId',null,locale),
							messageSource.getMessage('tipp.pkgWekbId',null,locale),
							IdentifierNamespace.ISCI,
							IdentifierNamespace.ISIL_PAKETSIGEL,
							IdentifierNamespace.EZB_ANCHOR,
							messageSource.getMessage('tipp.illIndicator',null,locale),
							messageSource.getMessage('tipp.supersedingPublicationTitleId',null,locale),
							messageSource.getMessage('tipp.seriesName',null,locale),
							messageSource.getMessage('tipp.subjectReference',null,locale),
							messageSource.getMessage('tipp.status',null,locale),
							messageSource.getMessage('tipp.accessType',null,locale),
							messageSource.getMessage('tipp.openAccess',null,locale),
							IdentifierNamespace.ZDB_PPN,
							messageSource.getMessage('tipp.listprice_eur',null,locale),
							messageSource.getMessage('tipp.listprice_gbp',null,locale),
							messageSource.getMessage('tipp.listprice_usd',null,locale)]
			if(ieIDs) {
				titleHeaders.addAll([messageSource.getMessage('tipp.localprice_eur',null,locale),
									 messageSource.getMessage('tipp.localprice_gbp',null,locale),
									 messageSource.getMessage('tipp.localprice_usd',null,locale)])
			}
		}
		if(tippIDs || ieIDs) {
			//List<GroovyRowResult> coreTitleIdentifierNamespaces = batchQueryService.longArrayQuery("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns in ('${coreTitleNSrestricted.join("','")}')", [tippIDs: titleIDs])
			List<GroovyRowResult> otherTitleIdentifierNamespaceList = batchQueryService.longArrayQuery("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and not idns_id = any(:namespaces)", [tippIDs: tippIDs, namespaces: IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_TITLE_NS+IdentifierNamespace.TITLE_ID).id])
			//titleHeaders.addAll(coreTitleIdentifierNamespaces['idns_ns'])
			List<String> otherTitleIdentifierNamespaces = otherTitleIdentifierNamespaceList['idns_ns']
			titleHeaders.addAll(otherTitleIdentifierNamespaces)
			if(configMap.containsKey('monthHeaders')) {
				titleHeaders.addAll(configMap.monthHeaders.collect { Date month -> DateUtils.getSDF_yyyyMM().format(month) })
			}
			if(withPick) {
				if(configMap.format == KBART)
					titleHeaders << "to_be_selected"
				else
					titleHeaders << messageSource.getMessage('renewEntitlementsWithSurvey.toBeSelectedIEs.export', null, locale)
				titleHeaders << "Pick"
			}
			export.columnData = buildRows(configMap)
		}
		else export.columnData = []
		export.titleRow = titleHeaders
		log.debug("End generateTitleExport")
		export
	}

	List buildRows(Map configMap) {
		List result = []
		String format = configMap.format, exportCellConfig = '', valueCol
		String style = configMap.style
		Set<Long> tippIDs = [], ieIDs = []
		Map<String, Object> arrayParams = [:]
		if(style)
			style = "'${style}'"
		else if(format == ExportService.EXCEL && configMap.containsKey('perpetuallyPurchasedTitleURLs')) {
			style = "(select case when tipp_host_platform_url = any(:perpetuallyPurchasedTitleURLs) then 'negative' else null end)"
			arrayParams.perpetuallyPurchasedTitleURLs = configMap.perpetuallyPurchasedTitleURLs
		}
		if(format == ExportService.KBART)
			valueCol = 'rdv_value'
		else valueCol = I10nTranslation.getRefdataValueColumn(LocaleUtils.getCurrentLocale())
		if(configMap.containsKey('tippIDs')) {
			tippIDs = configMap.tippIDs
			exportCellConfig = getTippExportCellConfig(format, style, valueCol)
		}
		else if(configMap.containsKey('ieIDs')) {
			ieIDs = configMap.ieIDs
			exportCellConfig = getIssueEntitlementExportCellConfig(format, style, valueCol)
		}
		List<String> otherTitleIdentifierNamespaces = configMap.otherTitleIdentifierNamespaces
		String specialRenewColumns = ''
		if(configMap.containsKey('usageData'))
			specialRenewColumns = "tipp_id, ${style} as style,"
		String rowQuery = "select ${specialRenewColumns} create_cell('${format}', tipp_name, ${style}) as publication_title," + exportCellConfig
		if(otherTitleIdentifierNamespaces) {
			otherTitleIdentifierNamespaces.each { String other ->
				rowQuery += ","
				rowQuery += "create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(other, IdentifierNamespace.NS_TITLE).id}), ${style}) as ${other}"
			}
		}
		if(!configMap.containsKey('usageData') && configMap.withPick == true) {
			rowQuery += ","
			rowQuery += "create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = (select case when tipp_host_platform_url = any(:perpetuallyPurchasedTitleURLs) then ${RDStore.YN_NO.id} else ${RDStore.YN_YES.id} end)), ${style}) as selectable,"
			rowQuery += "create_cell('${format}', null, ${style}) as pick"
		}
		if(tippIDs) {
			rowQuery += " from title_instance_package_platform left join tippcoverage on tc_tipp_fk = tipp_id where tipp_id = any(:tippIDs) order by tipp_sort_name"
			tippIDs.collate(65000).each { List<Long> subset ->
				arrayParams.tippIDs = subset
				List exportRows = batchQueryService.longArrayQuery(rowQuery, arrayParams)
				exportRows.collect { GroovyRowResult row ->
					if (configMap.containsKey('usageData')) {
						Long tippID = row.remove('tipp_id')
						String cellStyle = row.remove('style')
						Map titleReport = configMap.usageData.containsKey(tippID) ? configMap.usageData.get(tippID) : null
						configMap.monthHeaders.each { Date month ->
							def value
							String monthKey = DateUtils.getSDF_yyyyMM().format(month)
							if (titleReport) {
								value = titleReport.containsKey(monthKey) ? titleReport.get(monthKey) : ' '
							} else {
								value = ' '
							}
							row.put(monthKey, createCell(format, value, cellStyle))
						}
						if (configMap.containsKey('withPick')) {
							//implicit check
							if (cellStyle == 'negative') {
								row.put('selectable', createCell(format, RDStore.YN_NO.getI10n('value'), cellStyle))
							} else row.put('selectable', createCell(format, RDStore.YN_YES.getI10n('value'), cellStyle))
							row.put('pick', createCell(format, '', cellStyle))
						}
					}
					result << row.values()
				}
			}
		}
		else if(ieIDs) {
			rowQuery += " from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id left join issue_entitlement_coverage on ie_id = ic_ie_fk left join tippcoverage on tipp_id = tc_tipp_fk where ie_id = any(:ieIDs) order by tipp_sort_name"
			ieIDs.collate(65000).each { List<Long> subset ->
				arrayParams.ieIDs = subset
				List exportRows = batchQueryService.longArrayQuery(rowQuery, arrayParams)
				exportRows.collect { GroovyRowResult row ->
					result << row.values()
				}
			}
		}
		result
	}

	String getTippExportCellConfig(String format, String style, String valueCol) {
		Locale locale = LocaleUtils.getCurrentLocale()
		String config = "create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and ((lower(tipp_title_type) in ('monograph') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, IdentifierNamespace.NS_TITLE).id}) or (lower(tipp_title_type) in ('serial') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, IdentifierNamespace.NS_TITLE).id}))), ${style}) as print_identifier," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and ((lower(tipp_title_type) in ('monograph') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISBN, IdentifierNamespace.NS_TITLE).id}) or (lower(tipp_title_type) in ('serial') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, IdentifierNamespace.NS_TITLE).id}))), ${style}) as online_identifier," +
				"create_cell('${format}', to_char(tc_start_date, 'yyyy-MM-dd'), ${style}) as date_first_issue_online," +
				"create_cell('${format}', tc_start_volume, ${style}) as num_first_vol_online," +
				"create_cell('${format}', tc_start_issue, ${style}) as num_first_issue_online," +
				"create_cell('${format}', to_char(tc_end_date, 'yyyy-MM-dd'), ${style}) as date_last_issue_online," +
				"create_cell('${format}', tc_end_volume, ${style}) as num_last_vol_online," +
				"create_cell('${format}', tc_end_issue, ${style}) as num_last_issue_online," +
				"create_cell('${format}', tipp_host_platform_url, ${style}) as title_url," +
				"create_cell('${format}', tipp_first_author, ${style}) as first_author," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType('title_id', IdentifierNamespace.NS_TITLE).id}), ${style}) as title_id," +
				"create_cell('${format}', tc_embargo, ${style}) as embargo_info," +
				"create_cell('${format}', tc_coverage_depth, ${style}) as coverage_depth," +
				"create_cell('${format}', tc_coverage_note, ${style}) as notes," +
				"create_cell('${format}', tipp_title_type, ${style}) as publication_type," +
				"create_cell('${format}', tipp_publisher_name, ${style}) as publisher_name," +
				"create_cell('${format}', to_char(tipp_date_first_in_print, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as date_monograph_published_print," +
				"create_cell('${format}', to_char(tipp_date_first_online, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as date_monograph_published_online," +
				"create_cell('${format}', tipp_volume, ${style}) as monograph_volume," +
				"create_cell('${format}', tipp_edition_statement, ${style}) as monograph_edition," +
				"create_cell('${format}', tipp_first_editor, ${style}) as first_editor," +
				"create_cell('${format}', null, ${style}) as parent_publication_title_id," +
				"create_cell('${format}', null, ${style}) as preceding_publication_title_id," +
				"create_cell('${format}', (select pkg_name from package where pkg_id = tipp_pkg_fk), ${style}) as package_name," +
				"create_cell('${format}', (select plat_name from platform where plat_id = tipp_plat_fk), ${style}) as platform_name," +
				"create_cell('${format}', to_char(tipp_last_updated, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as last_changed," +
				"create_cell('${format}', to_char(tipp_access_start_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as access_start_date," +
				"create_cell('${format}', to_char(tipp_access_end_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as access_end_date," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_medium_rv_fk), ${style}) as medium," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ZDB, IdentifierNamespace.NS_TITLE).id}), ${style}) as zdb_id," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, IdentifierNamespace.NS_TITLE).id}), ${style}) as doi_identifier," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB, IdentifierNamespace.NS_TITLE).id}), ${style}) as ezb_id," +
				"create_cell('${format}', tipp_gokb_id, ${style}) as title_wekb_uuid," +
				"create_cell('${format}', (select pkg_gokb_id from package where pkg_id = tipp_pkg_fk), ${style}) as package_wekb_uuid," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNs(IdentifierNamespace.ISCI).id}), ${style}) as package_isci," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType('isil', IdentifierNamespace.NS_PACKAGE).id}), ${style}) as package_isil," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB, IdentifierNamespace.NS_PACKAGE).id}), ${style}) as package_ezb_anchor," +
				"create_cell('${format}', null, ${style}) as ill_indicator," +
				"create_cell('${format}', null, ${style}) as superceding_publication_title_id," +
				"create_cell('${format}', null, ${style}) as monograph_parent_collection_title," +
				"create_cell('${format}', tipp_subject_reference, ${style}) as subject_area," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_status_rv_fk), ${style}) as status," +
				"create_cell('${format}', (case when tipp_access_type_rv_fk = ${RDStore.TIPP_PAYMENT_PAID.id} then 'P' when tipp_access_type_rv_fk = ${RDStore.TIPP_PAYMENT_FREE.id} then 'F' else '' end), ${style}) as access_type," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_open_access_rv_fk), ${style}) as oa_type," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNs(IdentifierNamespace.ZDB_PPN).id}), ${style}) as zdb_ppn," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_EUR.id} order by pi_date_created desc limit 1), ${style}) as listprice_eur," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_GBP.id} order by pi_date_created desc limit 1), ${style}) as listprice_gbp," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_USD.id} order by pi_date_created desc limit 1), ${style}) as listprice_usd"
		config
	}

	String getIssueEntitlementExportCellConfig(String format, String style, String valueCol) {
		Locale locale = LocaleUtils.getCurrentLocale()
		String config = "create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and ((lower(tipp_title_type) in ('monograph') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, IdentifierNamespace.NS_TITLE).id}) or (lower(tipp_title_type) in ('serial') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, IdentifierNamespace.NS_TITLE).id}))), ${style}) as print_identifier," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and ((lower(tipp_title_type) in ('monograph') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISBN, IdentifierNamespace.NS_TITLE).id}) or (lower(tipp_title_type) in ('serial') and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, IdentifierNamespace.NS_TITLE).id}))), ${style}) as online_identifier," +
				"create_cell('${format}', to_char(coalesce(ic_start_date, tc_start_date), 'yyyy-MM-dd'), ${style}) as date_first_issue_online," +
				"create_cell('${format}', coalesce(ic_start_volume, tc_start_volume), ${style}) as num_first_vol_online," +
				"create_cell('${format}', coalesce(ic_start_issue, tc_start_issue), ${style}) as num_first_issue_online," +
				"create_cell('${format}', to_char(coalesce(ic_end_date, tc_end_date), 'yyyy-MM-dd'), ${style}) as date_last_issue_online," +
				"create_cell('${format}', coalesce(ic_end_volume, tc_end_volume), ${style}) as num_last_vol_online," +
				"create_cell('${format}', coalesce(ic_end_issue, tc_end_issue), ${style}) as num_last_issue_online," +
				"create_cell('${format}', tipp_host_platform_url, ${style}) as title_url," +
				"create_cell('${format}', tipp_first_author, ${style}) as first_author," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType('title_id', IdentifierNamespace.NS_TITLE).id}), ${style}) as title_id," +
				"create_cell('${format}', coalesce(ic_embargo, tc_embargo), ${style}) as embargo_info," +
				"create_cell('${format}', coalesce(ic_coverage_depth, tc_coverage_depth), ${style}) as coverage_depth," +
				"create_cell('${format}', coalesce(ic_coverage_note, tc_coverage_note), ${style}) as notes," +
				"create_cell('${format}', tipp_title_type, ${style}) as publication_type," +
				"create_cell('${format}', tipp_publisher_name, ${style}) as publisher_name," +
				"create_cell('${format}', to_char(tipp_date_first_in_print, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as date_monograph_published_print," +
				"create_cell('${format}', to_char(tipp_date_first_online, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as date_monograph_published_online," +
				"create_cell('${format}', tipp_volume, ${style}) as monograph_volume," +
				"create_cell('${format}', tipp_edition_statement, ${style}) as monograph_edition," +
				"create_cell('${format}', tipp_first_editor, ${style}) as first_editor," +
				"create_cell('${format}', null, ${style}) as parent_publication_title_id," +
				"create_cell('${format}', null, ${style}) as preceding_publication_title_id," +
				"create_cell('${format}', (select pkg_name from package where pkg_id = tipp_pkg_fk), ${style}) as package_name," +
				"create_cell('${format}', (select plat_name from platform where plat_id = tipp_plat_fk), ${style}) as platform_name," +
				"create_cell('${format}', to_char(tipp_last_updated, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as last_changed," +
				"create_cell('${format}', to_char(coalesce(ie_access_start_date, tipp_access_start_date), '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as access_start_date," +
				"create_cell('${format}', to_char(coalesce(ie_access_end_date, tipp_access_end_date), '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), ${style}) as access_end_date," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_medium_rv_fk), ${style}) as medium," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ZDB, IdentifierNamespace.NS_TITLE).id}), ${style}) as zdb_id," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, IdentifierNamespace.NS_TITLE).id}), ${style}) as doi_identifier," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB, IdentifierNamespace.NS_TITLE).id}), ${style}) as ezb_id," +
				"create_cell('${format}', tipp_gokb_id, ${style}) as title_wekb_uuid," +
				"create_cell('${format}', (select pkg_gokb_id from package where pkg_id = tipp_pkg_fk), ${style}) as package_wekb_uuid," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNs(IdentifierNamespace.ISCI).id}), ${style}) as package_isci," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType('isil', IdentifierNamespace.NS_PACKAGE).id}), ${style}) as package_isil," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = ${IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB, IdentifierNamespace.NS_PACKAGE).id}), ${style}) as package_ezb_anchor," +
				"create_cell('${format}', null, ${style}) as ill_indicator," +
				"create_cell('${format}', null, ${style}) as superceding_publication_title_id," +
				"create_cell('${format}', null, ${style}) as monograph_parent_collection_title," +
				"create_cell('${format}', tipp_subject_reference, ${style}) as subject_area," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_status_rv_fk), ${style}) as status," +
				"create_cell('${format}', (case when tipp_access_type_rv_fk = ${RDStore.TIPP_PAYMENT_PAID.id} then 'P' when tipp_access_type_rv_fk = ${RDStore.TIPP_PAYMENT_FREE.id} then 'F' else '' end), ${style}) as access_type," +
				"create_cell('${format}', (select ${valueCol} from refdata_value where rdv_id = tipp_open_access_rv_fk), ${style}) as oa_type," +
				"create_cell('${format}', (select string_agg(id_value,',') from identifier where id_tipp_fk = tipp_id and id_ns_fk = ${IdentifierNamespace.findByNs(IdentifierNamespace.ZDB_PPN).id}), ${style}) as zdb_ppn," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_EUR.id} order by pi_date_created desc limit 1), ${style}) as listprice_eur," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_GBP.id} order by pi_date_created desc limit 1), ${style}) as listprice_gbp," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = ${RDStore.CURRENCY_USD.id} order by pi_date_created desc limit 1), ${style}) as listprice_usd," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = ${RDStore.CURRENCY_EUR.id} order by pi_date_created desc limit 1), ${style}) as localprice_eur," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = ${RDStore.CURRENCY_GBP.id} order by pi_date_created desc limit 1), ${style}) as localprice_gbp," +
				"create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = ${RDStore.CURRENCY_USD.id} order by pi_date_created desc limit 1), ${style}) as localprice_usd"
		config
	}

	/**
	 * Creates a table cell in the given format. If the format is an Excel table, a style may be submitted as well
	 * @param format the format to use for the export (excel or kbart)
	 * @param data the data for the cell
	 * @param style styling parameter for an Excel sheet cell
	 * @return the table cell in the appropriate format; either as {@link Map} in structure [field: data, style: style]
	 */
	def createCell(String format, data, String style = null) {
		if(format == EXCEL)
			[field: data, style: style]
		else {
			if(format == KBART && (data == '' || data == null))
				' '
			else "${data}"
		}
	}

	/**
	 * Determines the issue entitlement instance of the given object
	 * @param rowData the object containing the issue entitlement
	 * @return the {@link IssueEntitlement} instance
	 */
	IssueEntitlement getIssueEntitlement(rowData) {
		if(rowData instanceof IssueEntitlement) {
			return (IssueEntitlement) rowData
		}
		else if(rowData instanceof IssueEntitlementCoverage) {
			return rowData.issueEntitlement
		}
		null
	}

	/**
	 * Helper method to order rows of an auxiliary table to a map for that the values belonging to a title record may be fetched more easily
	 * @param rows the rows of the auxiliary table
	 * @param tippKey the primary key of the {@link TitleInstancePackagePlatform}
	 * @return a {@link Map} containing the SQL row result with the primary key as map key
	 * @deprecated to be refactored by json_agg(json_build_object()) method chain in the SQL query for that the database does the collection more performantly
	 */
	@Deprecated
	static Map<Long, List<GroovyRowResult>> preprocessRows(List<GroovyRowResult> rows, String tippKey) {
		Map<Long, List<GroovyRowResult>> result = [:]
		rows.each { GroovyRowResult row ->
			List<GroovyRowResult> resList = result.get(row[tippKey])
			if(!resList)
				resList = []
			resList << row
			result.put(row[tippKey], resList)
		}
		result
	}

	/**
	 * Helper method to order rows of the price item table to a map for that the values belonging to a title record may be fetched more easily
	 * @param priceItemRows the rows of the {@link PriceItem} table
	 * @param tippKey the primary key of the {@link TitleInstancePackagePlatform}
	 * @return a {@link Map} containing the SQL row result with the primary key as map key
	 * @deprecated to be refactored by json_agg(json_build_object()) method chain in the SQL query for that the database does the collection more performantly
	 */
	@Deprecated
	static Map<Long, Map<String, GroovyRowResult>> preprocessPriceItemRows(List<GroovyRowResult> priceItemRows, String tippKey) {
		Map<Long, Map<String, GroovyRowResult>> priceItems = [:]
		priceItemRows.each { GroovyRowResult piRow ->
			Map<String, GroovyRowResult> priceItemMap = priceItems.get(piRow[tippKey])
			if(!priceItemMap)
				priceItemMap = [:]
			if(piRow['pi_list_currency']) {
				priceItemMap.put(piRow['pi_list_currency'], piRow)
				priceItems.put(piRow[tippKey], priceItemMap)
			}
		}
		priceItems
	}

	/**
	 * Helper method to order rows of the identifier table to a map for that the values belonging to a title record may be fetched more easily
	 * @param rows the rows of the {@link Identifier} table
	 * @return a {@link Map} containing the SQL row result, grouped by namespaces, with the primary key as map key
	 * @deprecated to be refactored by json_agg(json_build_object()) method chain in the SQL query for that the database does the collection more performantly
	 */
	@Deprecated
	static Map<Long, Map<String, List<String>>> preprocessIdentifierRows(List<GroovyRowResult> rows) {
		Map<Long, Map<String, List<String>>> identifiers = [:]
		rows.each { GroovyRowResult row ->
			Map<String, List<String>> idMap = identifiers.get(row['id_tipp_fk'])
			if(!idMap)
				idMap = [:]
			List<String> idValues = idMap.get(row['idns_ns'])
			if(!idValues)
				idValues = []
			idValues << row['id_value']
			idMap.put(row['idns_ns'], idValues)
			identifiers.put(row['id_tipp_fk'], idMap)
		}
		identifiers
	}

}
