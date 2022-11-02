package com.k_int.kbplus

import de.laser.ContextService
import de.laser.FilterService
import de.laser.GlobalService
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.OrgRole
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractReport
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.CostItemGroup
import de.laser.helper.RDConstants
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.base.AbstractCoverage
import de.laser.IssueEntitlementCoverage
import de.laser.TIPPCoverage
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5ApiSource
import de.laser.stats.Counter5Report
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
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
import org.hibernate.Session
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.awt.*
import java.math.RoundingMode
import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.List

/**
 * This service should contain the methods required to build the different exported files.
 * CSV methods will stream out the content of the file to a given output.
 * XML methods are provided to build the XML document
 * JSON methods build a Map object which can then be converted into Json.
 *
 * To be modified: the now specialised methods should be generalised into one method generating all exports.
 *
 * @author wpetit
 * @author agalffy
 */
@Transactional
class ExportService {

	SimpleDateFormat formatter = DateUtils.getSDF_ymd()
	MessageSource messageSource
	ContextService contextService
	FilterService filterService

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
		output.add(titleRow.join(separator))
		columnData.each { row ->
			if(row.size() > 0)
				output.add(row.join(separator))
			else output.add(" ")
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
		Locale locale = LocaleContextHolder.getLocale()
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
		XSSFCellStyle bold = wb.createCellStyle()
		XSSFFont font = wb.createFont()
		font.setBold(true)
		bold.setFont(font)
		XSSFCellStyle lb = wb.createCellStyle()
		lb.setWrapText(true)
		Map workbookStyles = ['positive':csPositive,'neutral':csNeutral,'negative':csNegative,'bold':bold]
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
					cell.setCellValue(colHeader)
				}
				sheet.createFreezePane(0,1)
				Row row
				Cell cell
				CellStyle numberStyle = wb.createCellStyle();
				XSSFDataFormat df = wb.createDataFormat();
				numberStyle.setDataFormat(df.getFormat("#,##0.00"));
				columnData.each { rowData ->
					int cellnum = 0
					row = sheet.createRow(rownum)
					rowData.each { cellData ->
						cell = row.createCell(cellnum++)
						if (cellData.field instanceof String) {
							cell.setCellValue((String) cellData.field)
							if(cellData.field.contains('\n'))
								cell.setCellStyle(lb)
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
						sheet.autoSizeColumn(i)
					}
					catch (Exception e) {
						log.error("Null pointer exception in column ${i}")
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
	 * Prepares the given collection of institutions or organisations for the export in the given format
	 * @param orgs the collection of organisations (or institutions) to export
	 * @param message the title of the Excel worksheet
	 * @param addHigherEducationTitles should be columns included which count only for institutions?
	 * @param format the file format for the export
	 * @return depending on the format, an Excel worksheet or a CSV file
	 */
	def exportOrg(Collection orgs, String message, boolean addHigherEducationTitles, String format) {
		Locale locale = LocaleContextHolder.getLocale()
		List titles = [messageSource.getMessage('org.sortname.label',null,locale), 'Name', messageSource.getMessage('org.shortname.label',null,locale),messageSource.getMessage('globalUID.label',null,locale)]


		if (addHigherEducationTitles) {
			titles.add(messageSource.getMessage('org.libraryType.label',null,locale))
			titles.add(messageSource.getMessage('org.libraryNetwork.label',null,locale))
			titles.add(messageSource.getMessage('org.funderType.label',null,locale))
			titles.add(messageSource.getMessage('org.region.label',null,locale))
			titles.add(messageSource.getMessage('org.country.label',null,locale))
		}

		titles.add(messageSource.getMessage('subscription.details.startDate',null,locale))
		titles.add(messageSource.getMessage('subscription.details.endDate',null,locale))
		titles.add(messageSource.getMessage('subscription.isPublicForApi.label',null,locale))
		titles.add(messageSource.getMessage('subscription.hasPerpetualAccess.label',null,locale))
		titles.add(messageSource.getMessage('default.status.label',null,locale))
		titles.add(RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION).getI10n('value'))
		//titles.add(RefdataValue.getByValueAndCategory('Functional contact', RDConstants.PERSON_CONTACT_TYPE).getI10n('value'))

		def propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

		propList.sort { a, b -> a.name.compareToIgnoreCase b.name }

		propList.each {
			titles.add(it.name)
		}

		orgs.sort { it.sortname } //see ERMS-1196. If someone finds out how to put order clauses into GORM domain class mappings which include a join, then OK. Otherwise, we must do sorting here.
		try {
			if(format == "xlsx") {

				XSSFWorkbook workbook = new XSSFWorkbook()
				POIXMLProperties xmlProps = workbook.getProperties()
				POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
				coreProps.setCreator(messageSource.getMessage('laser',null,locale))
				SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50,true)

				Sheet sheet = wb.createSheet(message)

				//the following three statements are required only for HSSF
				sheet.setAutobreaks(true)

				//the header row: centered text in 48pt font
				Row headerRow = sheet.createRow(0)
				headerRow.setHeightInPoints(16.75f)
				titles.eachWithIndex { titlesName, index ->
					Cell cell = headerRow.createCell(index)
					cell.setCellValue(titlesName)
				}

				//freeze the first row
				sheet.createFreezePane(0, 1)

				Row row
				Cell cell
				int rownum = 1


				orgs.each { org ->
					int cellnum = 0
					row = sheet.createRow(rownum)

					//Sortname
					cell = row.createCell(cellnum++)
					cell.setCellValue(org.sortname ?: '')

					//Name
					cell = row.createCell(cellnum++)
					cell.setCellValue(org.name ?: '')

					//Shortname
					cell = row.createCell(cellnum++)
					cell.setCellValue(org.shortname ?: '')

					//subscription globalUID
					cell = row.createCell(cellnum++)
					cell.setCellValue(org.globalUID)

					if (addHigherEducationTitles) {

						//libraryType
						cell = row.createCell(cellnum++)
						cell.setCellValue(org.libraryType?.getI10n('value') ?: ' ')

						//libraryNetwork
						cell = row.createCell(cellnum++)
						cell.setCellValue(org.libraryNetwork?.getI10n('value') ?: ' ')

						//funderType
						cell = row.createCell(cellnum++)
						cell.setCellValue(org.funderType?.getI10n('value') ?: ' ')

						//region
						cell = row.createCell(cellnum++)
						cell.setCellValue(org.region?.getI10n('value') ?: ' ')

						//country
						cell = row.createCell(cellnum++)
						cell.setCellValue(org.country?.getI10n('value') ?: ' ')
					}

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.startDate) //null check done already in calling method

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.endDate) //null check done already in calling method

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.isPublicForApi)

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.hasPerpetualAccess)

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.status?.getI10n('value') ?: ' ')

					cell = row.createCell(cellnum++)
					cell.setCellValue(org.generalContacts ?: '')

					/*cell = row.createCell(cellnum++)
                    cell.setCellValue('')*/

					propList.each { pd ->
						def value = ''
						org.customProperties.each { prop ->
							if (prop.type.descr == pd.descr && prop.type == pd) {
								if (prop.type.isIntegerType()) {
									value = prop.intValue.toString()
								} else if (prop.type.isStringType()) {
									value = prop.stringValue ?: ''
								} else if (prop.type.isBigDecimalType()) {
									value = prop.decValue.toString()
								} else if (prop.type.isDateType()) {
									value = prop.dateValue.toString()
								} else if (prop.type.isRefdataValueType()) {
									value = prop.refValue?.getI10n('value') ?: ''
								}
							}
						}

						org.privateProperties.each { prop ->
							if (prop.type.descr == pd.descr && prop.type == pd) {
								if (prop.type.isIntegerType()) {
									value = prop.intValue.toString()
								} else if (prop.type.isStringType()) {
									value = prop.stringValue ?: ''
								} else if (prop.type.isBigDecimalType()) {
									value = prop.decValue.toString()
								} else if (prop.type.isDateType()) {
									value = prop.dateValue.toString()
								} else if (prop.type.isRefdataValueType()) {
									value = prop.refValue?.getI10n('value') ?: ''
								}

							}
						}
						cell = row.createCell(cellnum++)
						cell.setCellValue(value)
					}

					rownum++
				}

				for (int i = 0; i < titles.size(); i++) {
					sheet.autoSizeColumn(i)
				}
				// Write the output to a file
				/* String file = message + ".xlsx"
                response.setHeader "Content-disposition", "attachment; filename=\"${file}\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose() */
				wb
			}
			else if(format == 'csv') {
				List orgData = []
				orgs.each{  org ->
					List row = []
					//Sortname
					row.add(org.sortname ? org.sortname.replaceAll(',','') : '')
					//Name
					row.add(org.name ? org.name.replaceAll(',','') : '')
					//Shortname
					row.add(org.shortname ? org.shortname.replaceAll(',','') : '')
					//subscription globalUID
					row.add(org.globalUID)
					if(addHigherEducationTitles) {
						//libraryType
						row.add(org.libraryType?.getI10n('value') ?: ' ')
						//libraryNetwork
						row.add(org.libraryNetwork?.getI10n('value') ?: ' ')
						//funderType
						row.add(org.funderType?.getI10n('value') ?: ' ')
						//region
						row.add(org.region?.getI10n('value') ?: ' ')
						//country
						row.add(org.country?.getI10n('value') ?: ' ')
					}
					//startDate
					row.add(org.startDate) //null check already done in calling method
					//endDate
					row.add(org.endDate) //null check already done in calling method
					//isPublicForApi
					row.add(org.isPublicForApi) //null check already done in calling method
					//hasPerpetualAccess
					row.add(org.hasPerpetualAccess) //null check already done in calling method
					//status
					row.add(org.status?.getI10n('value') ?: ' ')
					//generalContacts
					row.add(org.generalContacts ?: '')
					propList.each { pd ->
						def value = ''
						org.customProperties.each{ prop ->
							if(prop.type.descr == pd.descr && prop.type == pd) {
								if(prop.type.isIntegerType()){
									value = prop.intValue.toString()
								}
								else if (prop.type.isStringType()){
									value = prop.stringValue ?: ''
								}
								else if (prop.type.isBigDecimalType()){
									value = prop.decValue.toString()
								}
								else if (prop.type.isDateType()){
									value = prop.dateValue.toString()
								}
								else if (prop.type.isRefdataValueType()) {
									value = prop.refValue?.getI10n('value') ?: ''
								}
							}
						}
						org.privateProperties.each{ prop ->
							if(prop.type.descr == pd.descr && prop.type == pd) {
								if(prop.type.isIntegerType()){
									value = prop.intValue.toString()
								}
								else if (prop.type.isStringType()){
									value = prop.stringValue ?: ''
								}
								else if (prop.type.isBigDecimalType()){
									value = prop.decValue.toString()
								}
								else if (prop.type.isDateType()){
									value = prop.dateValue.toString()
								}
								else if (prop.type.isRefdataValueType()) {
									value = prop.refValue?.getI10n('value') ?: ''
								}
							}
						}
						row.add(value.replaceAll(',',';'))
					}
					orgData.add(row)
				}
				generateSeparatorTableString(titles,orgData,',')
			}
		}
		catch (Exception e) {
			log.error("Problem", e)
		}
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
			titles.add(it.name_de)
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
		SimpleDateFormat sdf = DateUtils.getSimpleDateFormatByToken('default.date.format.notime')
		propertyDefinitions.each { PropertyDefinition pd ->
			Set<String> value = []
			target.propertySet.each{ AbstractPropertyWithCalculatedLastUpdated prop ->
				if(prop.type.descr == pd.descr && prop.type == pd && prop.value) {
					if(prop.refValue)
						value << prop.refValue.getI10n('value')
					else
						value << prop.getValue() ?: ' '
				}
			}
			if(childObjects) {
				childObjects.get(target).each { childObj ->
					if(childObj.hasProperty("propertySet")) {
						childObj.propertySet.findAll{ AbstractPropertyWithCalculatedLastUpdated childProp -> childProp.type.descr == pd.descr && childProp.type == pd && childProp.value && !childProp.instanceOf && (childProp.tenant == contextOrg || childProp.isPublic) }.each { AbstractPropertyWithCalculatedLastUpdated childProp ->
							if(childProp.refValue)
								value << "${childProp.refValue.getI10n('value')} (${objectNames.get(childObj)})"
							else
								value << childProp.getValue() ? "${childProp.getValue()} (${objectNames.get(childObj)})" : ' '
						}
					}
				}
			}
			def cell
			switch(format) {
				case "xls":
				case "xlsx": cell = [field: value.join(', '), style: null]
					break
				case "csv": cell = value.join('; ').replaceAll(',',';')
					break
			}
			if(cell)
				cells.add(cell)
		}
		cells
	}

	/**
	 * Exports the given usage data in an Excel worksheet. The export is COUNTER compliant unless it is used for
	 * the title selection survey; then, additional data is needed which is not covered by the COUNTER compliance
	 * @param params the filter parameter map
	 * @param data the retrieved and filtered COUNTER data
	 * @param showPriceDate should price columns be displayed?
	 * @param showMetricType should the metric type be included (what seems not to be the case per definition in COUNTER 4)?
	 * @param showOtherData should other data be displayed?
	 * @return an Excel worksheet of the usage report, either according to the COUNTER 4 or COUNTER 5 format
	 */
	SXSSFWorkbook exportReport(GrailsParameterMap params, Map data, Boolean showPriceDate = false, Boolean showMetricType = false, Boolean showOtherData = false) {
		Locale locale = LocaleContextHolder.getLocale()
		XSSFWorkbook workbook = new XSSFWorkbook()
		POIXMLProperties xmlProps = workbook.getProperties()
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
		coreProps.setCreator(messageSource.getMessage('laser',null,locale))
		SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
		wb.setCompressTempFiles(true)
		Row row
		Cell cell
		Set<String> reportTypes = []
		if(params.data != 'fetchAll' && params.reportType)
			reportTypes << params.reportType
		else reportTypes.addAll(data.reportTypes)
		Org customer = data.subscription.getSubscriber()
		if(!params.metricType) {
			if('ft_total' in data.metricTypes)
				params.metricType = 'ft_total'
			else params.metricType = data.metricTypes[0]
		}
		//IMMENSELY UGLY WORKAROUND, TODO [ticket=3814]
		Set<IdentifierNamespace> propIdNamespaces = IdentifierNamespace.findAllByIsFromLaserAndNsNotInList(false, IdentifierNamespace.CORE_TITLE_NS)
		reportTypes.each { String reportType ->
			SXSSFSheet sheet = wb.createSheet(reportType)
			sheet.flushRows(10)
			sheet.setAutobreaks(true)
			LinkedHashSet<String> columnHeaders = []
			Map<TitleInstancePackagePlatform, Map> titleRows = [:]
			int rowno = 0
			//revision 4
			if(data.revision == 'counter4') {
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
				cell.setCellValue("${data.startDate.format('yyyy-MM-dd')} to ${data.endDate.format('yyyy-MM-dd')}")
				headerRow = sheet.createRow(5)
				cell = headerRow.createCell(0)
				cell.setCellValue("Date run:")
				headerRow = sheet.createRow(6)
				cell = headerRow.createCell(0)
				cell.setCellValue(data.dateRun.format('yyyy-MM-dd'))
				columnHeaders.addAll(Counter4Report.COLUMN_HEADERS.valueOf(reportType).headers)
				columnHeaders.addAll(data.monthsInRing.collect { Date month -> month.format('yyyy-MM') })
				if(showPriceDate) {
					columnHeaders.addAll(["List Price EUR", "List Price GBP", "List Price USD"])
				}

				if(showOtherData) {
					columnHeaders.addAll(["Year First Online", "Date First Online"])
				}

				row = sheet.createRow(7)
				columnHeaders.eachWithIndex { String colHeader, int i ->
					cell = row.createCell(i)
					cell.setCellValue(colHeader)
				}
				row = sheet.createRow(8)
				switch(reportType) {
					case Counter4ApiSource.JOURNAL_REPORT_1:
					case Counter4ApiSource.JOURNAL_REPORT_1_GOA:
					case Counter4ApiSource.BOOK_REPORT_1:
					case Counter4ApiSource.BOOK_REPORT_2: cell = row.createCell(0)
						cell.setCellValue("Total for all titles")
						for(int i = 1; i < 7; i++) {
							cell = row.createCell(i)
							cell.setCellValue("")
						}
						cell = row.createCell(7)
						if(data.total) {
							int totalCount = 0
							if(data.total.find { Map totalRow -> totalRow.reportType == reportType && totalRow.metricType == params.metricType })
								totalCount = data.total.find { Map totalRow -> totalRow.reportType == reportType && totalRow.metricType == params.metricType }.reportCount
							else if(data.total.find { Map totalRow -> totalRow.reportType == reportType })
								totalCount = data.total.find { Map totalRow -> totalRow.reportType == reportType }.reportCount
							cell.setCellValue(totalCount)
						}
						else cell.setCellValue(0)
						data.sums.findAll { Map totalRow -> totalRow.metricType == params.metricType && totalRow.reportType == reportType }.eachWithIndex { Map totalRow, int i ->
							cell = row.createCell(i+8)
							cell.setCellValue(totalRow.reportCount ?: 0)
						}
						titleRows = prepareTitleRows(data.usages, propIdNamespaces, reportType, showPriceDate, showMetricType, showOtherData, params.metricType)
						rowno = 8
						break
					case Counter4ApiSource.JOURNAL_REPORT_2:
					case Counter4ApiSource.BOOK_REPORT_3:
					case Counter4ApiSource.BOOK_REPORT_4:
						Map<String, Set<Map>> categoryRows = [:]
						data.sums.findAll { Map report -> report.reportType == reportType }.each { Map sumRow ->
							Set<Map> categoryRow = categoryRows.get(sumRow.category)
							if(!categoryRow)
								categoryRow = []
							categoryRow.add(sumRow)
							categoryRows.put(sumRow.category, categoryRow)
						}
						categoryRows.each { String category, Set<Map> categoryRow ->
							cell = row.createCell(0)
							cell.setCellValue(category)
							cell = row.createCell(2)
							cell.setCellValue(categoryRow[0].platform.name)
							cell = row.createCell(8)
							cell.setCellValue(categoryRow.sum { Map sumRow -> sumRow.reportCount })
							categoryRow.eachWithIndex { Map sumRow, int i ->
								cell = row.createCell(i+9)
								cell.setCellValue(sumRow.reportCount ?: 0)
							}
							rowno++
							row = sheet.createRow(rowno)
						}
						titleRows = prepareTitleRows(data.usages, propIdNamespaces, reportType, showPriceDate, showMetricType, showOtherData, params.metricType)
						rowno = 8
						break
					case Counter4ApiSource.JOURNAL_REPORT_5:
					case Counter4ApiSource.BOOK_REPORT_5:
						Map<String, Set<Map>> metricRows = [:]
						rowno = 8
						data.sums.findAll { Map report -> report.reportType == reportType }.each { Map sumRow ->
							Set<Map> metricRow = metricRows.get(sumRow.metricType)
							if(!metricRow)
								metricRow = []
							metricRow.add(sumRow)
							metricRows.put(sumRow.metricType, metricRow)
						}
						metricRows.each { String metric, Set<Map> metricRow ->
							cell = row.createCell(0)
							cell.setCellValue(metric == 'search_reg' ? "Total searches" : "Total searches: Searches: federated and automated")
							cell = row.createCell(2)
							cell.setCellValue(metricRow[0].platform.name)
							cell = row.createCell(8)
							cell.setCellValue(metricRow.sum { Map sumRow -> sumRow.reportCount })
							metricRow.eachWithIndex { Map sumRow, int i ->
								cell = row.createCell(i+9)
								cell.setCellValue(sumRow.reportCount)
							}
							rowno++
							row = sheet.createRow(rowno)
						}
						titleRows = prepareTitleRows(data.usages, propIdNamespaces, reportType, showPriceDate, showMetricType, showOtherData, params.metricType)
						break
					case Counter4ApiSource.DATABASE_REPORT_1:
						Map<String, Set<Map>> metricRows = [:]
						rowno = 8
						data.sums.findAll { Map report -> report.reportType == reportType }.each { Map sumRow ->
							Set<Map> metricRow = metricRows.get(sumRow.metricType)
							if(!metricRow)
								metricRow = []
							metricRow.add(sumRow)
							metricRows.put(sumRow.metricType, metricRow)
						}
						metricRows.each { String metric, Set<Map> metricRow ->
							cell = row.createCell(0)
							cell.setCellValue(metricRow[0].platform.name)
							cell = row.createCell(1)
							cell.setCellValue(metricRow[0].publisher ?: '')
							cell = row.createCell(2)
							switch(metric) {
								case 'search_reg': cell.setCellValue('Regular Searches')
									break
								case 'search_fed': cell.setCellValue('Searches-federated and automated')
									break
								case 'record_view': cell.setCellValue('Record Views')
									break
								case 'result_click': cell.setCellValue('Result Clicks')
									break
							}
							cell = row.createCell(3)
							cell.setCellValue(metricRow.sum { Map sumRow -> sumRow.reportCount })
							metricRow.eachWithIndex { Map sumRow, int i ->
								cell = row.createCell(i+4)
								cell.setCellValue(sumRow.reportCount)
							}
							rowno++
							row = sheet.createRow(rowno)
						}
						break
					case Counter4ApiSource.PLATFORM_REPORT_1:
						Map<String, Set<Map>> metricRows = [:]
						rowno = 8
						data.sums.findAll { Map report -> report.reportType == reportType }.each { Map sumRow ->
							Set<Map> metricRow = metricRows.get(sumRow.metricType)
							if(!metricRow)
								metricRow = []
							metricRow.add(sumRow)
							metricRows.put(sumRow.metricType, metricRow)
						}
						metricRows.each { String metric, Set<Map> metricRow ->
							cell = row.createCell(0)
							cell.setCellValue(metricRow[0].platform.name)
							cell = row.createCell(1)
							cell.setCellValue(metricRow[0].publisher ?: '')
							cell = row.createCell(2)
							switch(metric) {
								case 'search_reg': cell.setCellValue('Regular Searches')
									break
								case 'search_fed': cell.setCellValue('Searches-federated and automated')
									break
								case 'record_view': cell.setCellValue('Record Views')
									break
								case 'result_click': cell.setCellValue('Result Clicks')
									break
							}
							cell = row.createCell(3)
							cell.setCellValue(metricRow.sum { Map sumRow -> sumRow.reportCount })
							metricRow.eachWithIndex { Map sumRow, int i ->
								cell = row.createCell(i+4)
								cell.setCellValue(sumRow.reportCount)
							}
							rowno++
							row = sheet.createRow(rowno)
						}
						break
				}
			}
			//revision 5
			else if(data.revision == 'counter5') {
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
				cell.setCellValue(5)
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
				cell.setCellValue(params.metricType ?: Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).metricTypes)
				headerRow = sheet.createRow(6)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_Filters")
				cell = headerRow.createCell(1)
				cell.setCellValue(Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).reportFilters)
				headerRow = sheet.createRow(7)
				cell = headerRow.createCell(0)
				cell.setCellValue("Report_Attributes")
				cell = headerRow.createCell(1)
				cell.setCellValue(Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).reportAttributes)
				headerRow = sheet.createRow(8)
				cell = headerRow.createCell(0)
				cell.setCellValue("Exceptions")
				headerRow = sheet.createRow(9)
				cell = headerRow.createCell(0)
				cell.setCellValue("Reporting_Period")
				cell = headerRow.createCell(1)
				cell.setCellValue("Begin_Date:${data.startDate.format('yyyy-MM-dd')}; End_Date=${data.endDate.format('yyyy-MM-dd')}")
				headerRow = sheet.createRow(10)
				cell = headerRow.createCell(0)
				cell.setCellValue("Created")
				cell = headerRow.createCell(1)
				cell.setCellValue(data.dateRun.format("yyyy-MM-dd'T'HH:mm:ss'Z'"))
				headerRow = sheet.createRow(11)
				cell = headerRow.createCell(0)
				cell.setCellValue("Created_By")
				cell = headerRow.createCell(1)
				cell.setCellValue(messageSource.getMessage('laser', null, locale))
				headerRow = sheet.createRow(12) //the 13th row is mandatory empty
				cell = headerRow.createCell(0)
				cell.setCellValue("")
				columnHeaders.addAll(data.monthsInRing.collect { Date month -> month.format('MMM-yyyy') })
				if(showPriceDate) {
					columnHeaders.addAll(["List Price EUR", "List Price GBP", "List Price USD"])
				}

				if(showOtherData) {
					columnHeaders.addAll(["Year First Online", "Date First Online"])
				}

				row = sheet.createRow(13)
				columnHeaders.eachWithIndex { String colHeader, int i ->
					cell = row.createCell(i)
					cell.setCellValue(colHeader)
				}
				rowno = 14
				if(reportType.toLowerCase() in [Counter5ApiSource.PLATFORM_MASTER_REPORT, Counter5ApiSource.PLATFORM_USAGE]) {
					Map<String, Map> metricRows = [:]
					data.usages.findAll{ Counter5Report r -> r.reportType == reportType }.each { Counter5Report r ->
						Map<String, Object> metricRow = metricRows.get(r.metricType)
						int periodTotal = 0
						if(!metricRow) {
							metricRow = [:]
						}
						else periodTotal = metricRow.get('Reporting_Period_Total')
						metricRow.put('Platform', r.platform.name)
						metricRow.put('Metric_Type', r.metricType)
						periodTotal += r.reportCount
						metricRow.put('Reporting_Period_Total', periodTotal)
						metricRow.put(r.reportFrom.format('MMM-yyyy'), r.reportCount)
						metricRows.put(r.metricType, metricRow)
					}
					metricRows.eachWithIndex{ String metricType, Map metricRow, int i ->
						row = sheet.createRow(i+rowno)
						for(int c = 0; c < columnHeaders.size(); c++) {
							cell = row.createCell(c)
							cell.setCellValue(metricRow.get(columnHeaders[c]) ?: "")
						}
					}
				}
				else
					titleRows = prepareTitleRows(data.usages, propIdNamespaces, reportType, showPriceDate, showMetricType, showOtherData)
			}
			titleRows.each{ TitleInstancePackagePlatform title, Map<String, Map> titleMetric ->
				titleMetric.eachWithIndex { String metricType, Map titleRow, int i ->
					row = sheet.createRow(i+rowno)
					cell = row.createCell(0)
					cell.setCellValue(title.name)
					for(int c = 1; c < columnHeaders.size(); c++) {
						cell = row.createCell(c)
						cell.setCellValue(titleRow.get(columnHeaders[c]) ?: "")
					}
				}
				rowno += titleMetric.size()
			}
		}
		wb
	}

	/**
	 * Assembles the rows containing the usages for each title according to the given report type.
	 * The reports exports are COUNTER-compliant unless if used for other purposes which make the
	 * display of other data necessary as well, i.e. list prices, for pick-and-choose purposes
	 * @param usages the data to prepare
	 * @param propIdNamespaces the proprietary namespace(s) of the provider
	 * @param reportType the report type which is about to be exported
	 * @param showPriceDate should the list price shown?
	 * @param showMetricType should the metric types be included in the report?
	 * @param showOtherData should other data being shown as well?
	 * @param metricType (for COUNTER 4 exports only) the metric typ which should be exported
	 * @return a map of titles with the row containing the columns as specified for the given report
	 */
	Map<TitleInstancePackagePlatform, Map<String, Map>> prepareTitleRows(Set<AbstractReport> usages, Set<IdentifierNamespace> propIdNamespaces, String reportType, Boolean showPriceDate = false, Boolean showMetricType = false, Boolean showOtherData = false, String metricType = 'ft_total') {
		Map<TitleInstancePackagePlatform, Map<String, Map>> titleRows = [:]
		//inconsistent storage of the report type makes that necessary
		usages.findAll { AbstractReport report -> report.reportType in [reportType, reportType.toLowerCase(), reportType.toUpperCase()] }.each { AbstractReport report ->
			if((showMetricType) ||
					!(reportType in [Counter4ApiSource.BOOK_REPORT_1, Counter4ApiSource.BOOK_REPORT_2, Counter4ApiSource.JOURNAL_REPORT_1, Counter4ApiSource.JOURNAL_REPORT_1_GOA, Counter4ApiSource.JOURNAL_REPORT_2, Counter4ApiSource.JOURNAL_REPORT_5]) ||
					((reportType in [Counter4ApiSource.BOOK_REPORT_1, Counter4ApiSource.BOOK_REPORT_2, Counter4ApiSource.JOURNAL_REPORT_1, Counter4ApiSource.JOURNAL_REPORT_1_GOA, Counter4ApiSource.JOURNAL_REPORT_2, Counter4ApiSource.JOURNAL_REPORT_5]) && report.metricType == metricType)) {
				Map<String, Map> titleMetrics = titleRows.get(report.title)
				if(!titleMetrics)
					titleMetrics = [:]
				Map<String, Object> titleRow = titleMetrics.get(report.metricType)
				int periodTotal = 0
				if(report instanceof Counter4Report) {
					if(!titleRow) {
						titleRow = [:]
						//key naming identical to column headers
						titleRow.put("Publisher", report.publisher)
						titleRow.put("Platform", report.platform.name)
						titleRow.put("Book DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
						titleRow.put("Proprietary Identifier", report.title.ids.find { Identifier id -> id.ns in propIdNamespaces }?.value)
						titleRow.put("ISBN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISBN }?.value)
						titleRow.put("ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
					}
					else periodTotal = titleRow.get("Reporting Period Total") as int
					periodTotal += report.reportCount
					if(reportType in [Counter4ApiSource.JOURNAL_REPORT_5, Counter4ApiSource.BOOK_REPORT_5])
						titleRow.put("User activity", report.metricType == 'search_reg' ? "Regular Searches" : "Searches: federated and automated")
					titleRow.put("Reporting Period Total", periodTotal)
					titleRow.put(report.reportFrom.format("yyyy-MM"), report.reportCount)

					if (showPriceDate && report.title.priceItems) {
						//listprice_eur
						titleRow.put("List Price EUR", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }?.listPrice ?: ' ')
						//listprice_gbp
						titleRow.put("List Price GBP", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }?.listPrice ?: ' ')
						//listprice_usd
						titleRow.put("List Price USD", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice ?: ' ')
					}

					if (showOtherData) {
						titleRow.put("Year First Online", report.title.dateFirstOnline ? report.title.dateFirstOnline.format('yyyy'): ' ')
						titleRow.put("Date First Online", report.title.dateFirstOnline ? report.title.dateFirstOnline.format('yyyy-MM-dd'): ' ')
					}
				}
				else if(report instanceof Counter5Report) {
					if(!titleRow) {
						titleRow = [:]
						//key naming identical to column headers
						titleRow.put("Publisher", report.publisher)
						//publisher ID is ususally not available
						titleRow.put("Platform", report.platform.name)
						titleRow.put("Proprietary_ID", report.title.ids.find { Identifier id -> id.ns in propIdNamespaces }?.value)
						titleRow.put("Metric_Type", report.metricType)
					}
					else periodTotal = titleRow.get("Reporting_Period_Total") as int
					periodTotal += report.reportCount
					switch(reportType.toLowerCase()) {
						case Counter5ApiSource.TITLE_MASTER_REPORT:
							titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							break
						case Counter5ApiSource.BOOK_REQUESTS:
						case Counter5ApiSource.BOOK_ACCESS_DENIED: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							titleRow.put("YOP", report.title.dateFirstOnline?.format("YYYY"))
							titleRow.put("ISBN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISBN }?.value)
							break
						case Counter5ApiSource.BOOK_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("ISBN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISBN }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							titleRow.put("YOP", report.title.dateFirstOnline?.format("YYYY"))
							titleRow.put("Access_Type", report.accessType)
							break
						case Counter5ApiSource.JOURNAL_REQUESTS:
						case Counter5ApiSource.JOURNAL_ACCESS_DENIED: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							break
						case Counter5ApiSource.JOURNAL_USAGE_BY_ACCESS_TYPE: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							titleRow.put("Access_Type", report.accessType)
							break
						case Counter5ApiSource.JOURNAL_REQUESTS_BY_YOP: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							titleRow.put("YOP", report.title.dateFirstOnline?.format("YYYY"))
							break
						case Counter5ApiSource.ITEM_MASTER_REPORT: titleRow.put("DOI", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.DOI }?.value)
							titleRow.put("ISBN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISBN }?.value)
							titleRow.put("Print_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISSN }?.value)
							titleRow.put("Online_ISSN", report.title.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.EISSN }?.value)
							titleRow.put("URI", report.title.hostPlatformURL)
							break
					}
					titleRow.put("Reporting_Period_Total", periodTotal)
					titleRow.put(report.reportFrom.format("MMM-yyyy"), report.reportCount)
					if (showPriceDate && report.title.priceItems) {
						//listprice_eur
						titleRow.put("List Price EUR", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }?.listPrice ?: ' ')
						//listprice_gbp
						titleRow.put("List Price GBP", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }?.listPrice ?: ' ')
						//listprice_usd
						titleRow.put("List Price USD", report.title.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice ?: ' ')
					}

					if (showOtherData) {
						titleRow.put("Year First Online", report.title.dateFirstOnline ? report.title.dateFirstOnline.format('yyyy'): ' ')
						titleRow.put("Date First Online", report.title.dateFirstOnline ? report.title.dateFirstOnline.format('yyyy-MM-dd'): ' ')
					}

				}
				titleMetrics.put(report.metricType, titleRow)
				titleRows.put(report.title, titleMetrics)
			}
		}
		titleRows
	}

	/**
	 * Make a XLSX export of cost item results
	 * @param result the loaded financial data
	 * @return a Excel worksheet of the given cost data
	 */
	SXSSFWorkbook processFinancialXLSX(Map<String,Object> result) {
		Locale locale = LocaleContextHolder.getLocale()
		SimpleDateFormat dateFormat = DateUtils.getSDF_NoTime()
		XSSFWorkbook workbook = new XSSFWorkbook()
		POIXMLProperties xmlProps = workbook.getProperties()
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
		coreProps.setCreator(messageSource.getMessage('laser',null,locale))
		//LinkedHashMap<Subscription,List<Org>> subscribers = [:]
		//LinkedHashMap<Subscription,Set<Org>> providers = [:]
		LinkedHashMap<Subscription, BudgetCode> costItemGroups = [:]
		/*OrgRole.findAllByRoleTypeInList([RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]).each { it ->
			List<Org> orgs = subscribers.get(it.sub)
			if(orgs == null)
				orgs = [it.org]
			else orgs.add(it.org)
			subscribers.put(it.sub,orgs)
		}
		OrgRole.findAllByRoleTypeInList([RDStore.OR_PROVIDER,RDStore.OR_AGENCY]).each { it ->
			Set<Org> orgs = providers.get(it.sub)
			if (orgs == null)
				orgs = [it.org]
			else orgs.add(it.org)
			providers.put(it.sub, orgs)
		}*/
		XSSFCellStyle csPositive = workbook.createCellStyle()
		csPositive.setFillForegroundColor(new XSSFColor(new Color(198,239,206)))
		csPositive.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle csNegative = workbook.createCellStyle()
		csNegative.setFillForegroundColor(new XSSFColor(new Color(255,199,206)))
		csNegative.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		XSSFCellStyle csNeutral = workbook.createCellStyle()
		csNeutral.setFillForegroundColor(new XSSFColor(new Color(255,235,156)))
		csNeutral.setFillPattern(FillPatternType.SOLID_FOREGROUND)
		SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
		wb.setCompressTempFiles(true)
		CostItemGroup.findAll().each{ cig -> costItemGroups.put(cig.costItem,cig.budgetCode) }
		result.cost_item_tabs.entrySet().each { cit ->
			String sheettitle
			String viewMode = cit.getKey()
			switch(viewMode) {
				case "own": sheettitle = messageSource.getMessage('financials.header.ownCosts',null,locale)
					break
				case "cons": sheettitle = messageSource.getMessage('financials.header.consortialCosts',null,locale)
					break
				case "subscr": sheettitle = messageSource.getMessage('financials.header.subscriptionCosts',null,locale)
					break
			}
			SXSSFSheet sheet = wb.createSheet(sheettitle)
			sheet.flushRows(10)
			sheet.setAutobreaks(true)
			Row headerRow = sheet.createRow(0)
			headerRow.setHeightInPoints(16.75f)
			ArrayList titles = [messageSource.getMessage( 'sidewide.number',null,locale)]
			if(viewMode == "cons")
				titles.addAll([messageSource.getMessage('org.sortName.label',null,locale),messageSource.getMessage('financials.newCosts.costParticipants',null,locale),messageSource.getMessage('financials.isVisibleForSubscriber',null,locale)])
			titles.add(messageSource.getMessage( 'financials.newCosts.costTitle',null,locale))
			if(viewMode == "cons")
				titles.add(messageSource.getMessage('default.provider.label',null,locale))
			titles.addAll([messageSource.getMessage('default.subscription.label',null,locale), messageSource.getMessage('subscription.startDate.label',null,locale), messageSource.getMessage('subscription.endDate.label',null,locale),
						   messageSource.getMessage('financials.costItemConfiguration',null,locale), messageSource.getMessage('package.label',null,locale), messageSource.getMessage('issueEntitlement.label',null,locale),
						   messageSource.getMessage('financials.datePaid',null,locale), messageSource.getMessage('financials.dateFrom',null,locale), messageSource.getMessage('financials.dateTo',null,locale), messageSource.getMessage('financials.financialYear',null,locale),
						   messageSource.getMessage('default.status.label',null,locale), messageSource.getMessage('financials.billingCurrency',null,locale), messageSource.getMessage('financials.costInBillingCurrency',null,locale),"EUR",
						   messageSource.getMessage('financials.costInLocalCurrency',null,locale)])
			if(["own","cons"].indexOf(viewMode) > -1)
				titles.addAll([messageSource.getMessage('financials.taxRate',null,locale), messageSource.getMessage('financials.billingCurrency',null,locale),messageSource.getMessage('financials.costInBillingCurrencyAfterTax',null,locale),"EUR",messageSource.getMessage('financials.costInLocalCurrencyAfterTax',null,locale)])
			titles.addAll([messageSource.getMessage('financials.costItemElement',null,locale), messageSource.getMessage('financials.newCosts.description',null,locale),
                           messageSource.getMessage('financials.newCosts.costsReferenceOn',null,locale), messageSource.getMessage('financials.budgetCode',null,locale),
                           messageSource.getMessage('financials.invoice_number',null,locale), messageSource.getMessage('financials.order_number',null,locale), messageSource.getMessage('globalUID.label',null,locale)])
			titles.eachWithIndex { titleName, int i ->
				Cell cell = headerRow.createCell(i)
				cell.setCellValue(titleName)
			}
			sheet.createFreezePane(0, 1)
			Row row
			Cell cell
			int rownum = 1
			int sumcell = -1
			int sumcellAfterTax = -1
			int sumTitleCell = -1
			int sumCurrencyCell = -1
			int sumCurrencyAfterTaxCell = -1
			HashSet<String> currencies = new HashSet<String>()
			if(cit.getValue().count > 0) {
				cit.getValue().costItems.eachWithIndex { ci, int i ->
					//log.debug("now processing entry #${i}")
					BudgetCode codes = costItemGroups.get(ci)
					String start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
					String end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
					String paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
					int cellnum = 0
					row = sheet.createRow(rownum)
					//sidewide number
					cell = row.createCell(cellnum++)
					cell.setCellValue(rownum)
					if(viewMode == "cons") {
						if(ci.sub) {
							List<Org> orgRoles = ci.sub.orgRelations.findAll { OrgRole oo -> oo.roleType in [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN] }.collect { it.org }
							//participants (visible?)
							Cell cellA = row.createCell(cellnum++)
							Cell cellB = row.createCell(cellnum++)
							String cellValueA = ""
							String cellValueB = ""
							orgRoles.each { Org or ->
								cellValueA += or.sortname
								cellValueB += or.name
							}
							cellA.setCellValue(cellValueA)
							cellB.setCellValue(cellValueB)
							cell = row.createCell(cellnum++)
							cell.setCellValue(ci.isVisibleForSubscriber ? messageSource.getMessage('financials.isVisibleForSubscriber',null,locale) : "")
						}
					}
					//cost title
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci.costTitle ?: '')
					if(viewMode == "cons") {
						//provider
						cell = row.createCell(cellnum++)
						if(ci.sub) {
							Set<Org> orgRoles = ci.sub.orgRelations.findAll { OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.collect { it.org }
							String cellValue = ""
							orgRoles.each { Org or ->
								cellValue += or.name
							}
							cell.setCellValue(cellValue)
						}
						else cell.setCellValue("")
					}
					//cell.setCellValue(ci.sub ? ci.sub"")
					//subscription
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci.sub ? ci.sub.name : "")
					//dates from-to
					Cell fromCell = row.createCell(cellnum++)
					Cell toCell = row.createCell(cellnum++)
					if(ci.sub) {
						if(ci.sub.startDate)
							fromCell.setCellValue(dateFormat.format(ci.sub.startDate))
						else
							fromCell.setCellValue("")
						if(ci.sub.endDate)
							toCell.setCellValue(dateFormat.format(ci.sub.endDate))
						else
							toCell.setCellValue("")
					}
					//cost sign
					cell = row.createCell(cellnum++)
					if(ci.costItemElementConfiguration) {
						cell.setCellValue(ci.costItemElementConfiguration.getI10n("value"))
					}
					else
						cell.setCellValue(messageSource.getMessage('financials.costItemConfiguration.notSet',null,locale))
					//subscription package
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.subPkg ? ci.subPkg.pkg.name:'')
					//issue entitlement
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.issueEntitlement ? ci.issueEntitlement.name:'')
					//date paid
					cell = row.createCell(cellnum++)
					cell.setCellValue(paid_date ?: '')
					//date from
					cell = row.createCell(cellnum++)
					cell.setCellValue(start_date ?: '')
					//date to
					cell = row.createCell(cellnum++)
					cell.setCellValue(end_date ?: '')
					//financial year
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.financialYear ? ci.financialYear.toString():'')
					//for the sum title
					sumTitleCell = cellnum
					//cost item status
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.costItemStatus ? ci.costItemStatus.getI10n("value"):'')
					if(["own","cons"].indexOf(viewMode) > -1) {
						//billing currency and value
						cell = row.createCell(cellnum++)
						cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
						sumCurrencyCell = cellnum
						cell = row.createCell(cellnum++)
						cell.setCellValue(ci?.costInBillingCurrency ? ci.costInBillingCurrency : 0.0)
						if(ci.costItemElementConfiguration) {
							switch(ci.costItemElementConfiguration) {
								case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
									break
								case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
									break
								case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
									break
							}
						}
						//local currency and value
						cell = row.createCell(cellnum++)
						cell.setCellValue("EUR")
						sumcell = cellnum
						cell = row.createCell(cellnum++)
						cell.setCellValue(ci?.costInLocalCurrency ? ci.costInLocalCurrency : 0.0)
						if(ci.costItemElementConfiguration) {
							switch(ci.costItemElementConfiguration) {
								case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
									break
								case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
									break
								case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
									break
							}
						}
						//tax rate
						cell = row.createCell(cellnum++)
						String taxString
						if(ci.taxKey && ci.taxKey.display) {
							taxString = "${ci.taxKey.taxType.getI10n('value')} (${ci.taxKey.taxRate} %)"
						}
						else if(ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19, CostItem.TAX_TYPES.TAX_REVERSE_CHARGE]) {
							taxString = "${ci.taxKey.taxType.getI10n('value')}"
						}
						else taxString = messageSource.getMessage('financials.taxRate.notSet',null,locale)
						cell.setCellValue(taxString)
					}
					//billing currency and value
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
					sumCurrencyAfterTaxCell = cellnum
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.costInBillingCurrencyAfterTax ? ci.costInBillingCurrencyAfterTax : 0.0)
					if(ci.costItemElementConfiguration) {
						switch(ci.costItemElementConfiguration) {
							case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
								break
							case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
								break
							case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
								break
						}
					}
					//local currency and value
					cell = row.createCell(cellnum++)
					cell.setCellValue("EUR")
					sumcellAfterTax = cellnum
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.costInLocalCurrencyAfterTax ? ci.costInLocalCurrencyAfterTax : 0.0)
					if(ci.costItemElementConfiguration) {
						switch(ci.costItemElementConfiguration) {
							case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
								break
							case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
								break
							case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
								break
						}
					}
					//cost item element
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.costItemElement?ci.costItemElement.getI10n("value") : '')
					//cost item description
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.costDescription?: '')
					//reference
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.reference?:'')
					//budget codes
					cell = row.createCell(cellnum++)
					cell.setCellValue(codes ? codes.value : '')
					//invoice number
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.invoice ? ci.invoice.invoiceNumber : "")
					//order number
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.order ? ci.order.orderNumber : "")
					//globalUUID
					cell = row.createCell(cellnum++)
					cell.setCellValue(ci?.globalUID ?: '')
					rownum++
				}
				rownum++
				sheet.createRow(rownum)
				Row sumRow = sheet.createRow(rownum)
				cell = sumRow.createCell(sumTitleCell)
				cell.setCellValue(messageSource.getMessage('financials.export.sums',null,locale))
				if(sumcell > 0) {
					cell = sumRow.createCell(sumcell)
					BigDecimal localSum = BigDecimal.valueOf(cit.getValue().sums.localSums.localSum)
					cell.setCellValue(localSum.setScale(2, RoundingMode.HALF_UP))
				}
				cell = sumRow.createCell(sumcellAfterTax)
				BigDecimal localSumAfterTax = BigDecimal.valueOf(cit.getValue().sums.localSums.localSumAfterTax)
				cell.setCellValue(localSumAfterTax.setScale(2, RoundingMode.HALF_UP))
				rownum++
				cit.getValue().sums.billingSums.each { entry ->
					sumRow = sheet.createRow(rownum)
					cell = sumRow.createCell(sumTitleCell)
					cell.setCellValue(entry.currency)
					if(sumCurrencyCell > 0) {
						cell = sumRow.createCell(sumCurrencyCell)
						BigDecimal billingSum = BigDecimal.valueOf(entry.billingSum)
						cell.setCellValue(billingSum.setScale(2, RoundingMode.HALF_UP))
					}
					cell = sumRow.createCell(sumCurrencyAfterTaxCell)
					BigDecimal billingSumAfterTax = BigDecimal.valueOf(entry.billingSumAfterTax)
					cell.setCellValue(billingSumAfterTax.setScale(2, RoundingMode.HALF_UP))
					rownum++
				}
			}
			else {
				row = sheet.createRow(rownum)
				cell = row.createCell(0)
				cell.setCellValue(messageSource.getMessage("finance.export.empty",null,locale))
			}

			for(int i = 0; i < titles.size(); i++) {
				try {
					sheet.autoSizeColumn(i)
				}
				catch(NullPointerException e) {
					log.error("Null value in column ${i}")
				}
			}
		}
		wb
	}

	/**
	 * Generates an Excel workbook of the property usage for the given {@link Map} of {@link PropertyDefinition}s.
	 * The structure of the workbook is the same as of the view _manage(Private)PropertyDefintions.gsp
	 * @param propDefs - the {@link Map} of {@link PropertyDefinition}s whose usages should be printed
	 * @return a {@link Map} of the Excel sheets containing the table data
	 */
	Map<String,Map> generatePropertyUsageExportXLS(Map propDefs) {
		Locale locale = LocaleContextHolder.getLocale()
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
			propDefEntry.value.each { PropertyDefinition pd ->
				List row = []
				row.add([field:pd.getI10n("name"),style:null])
				row.add([field:pd.getI10n("expl"),style:null])
				String typeString = pd.getLocalizedValue(pd.type)
				if(pd.isRefdataValueType()) {
					List refdataValues = []
                    RefdataCategory.getAllRefdataValues(pd.refdataCategory).each { RefdataValue refdataValue ->
						refdataValues << refdataValue.getI10n("value")
					}
					typeString += "(${refdataValues.join('/')})"
				}
				row.add([field:typeString,style:null])
				row.add([field:pd.countOwnUsages(),style:null])
				row.add([field:pd.isHardData ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				row.add([field:pd.multipleOccurrence ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				row.add([field:pd.isUsedForLogic ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				row.add([field:pd.mandatory ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				row.add([field:pd.multipleOccurrence ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"),style:null])
				rows.add(row)
			}
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
		Locale locale = LocaleContextHolder.getLocale()
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
		sheetData
	}

	/**
	 * Generates a title stream export list according to the KBART II-standard but enriched with proprietary fields such as ZDB-ID
	 * The standard is defined here: <a href="https://www.niso.org/standards-committees/kbart">KBART definition</a>
	 * @param entitlementData a {@link Collection} containing the actual data
	 * @return a {@link Map} containing lists for the title row and the column data
	 */
	Map<String,List> generateTitleExportKBART(Map configMap, String entitlementInstance) {
		log.debug("Begin generateTitleExportKBART")
		Sql sql = GlobalService.obtainSqlConnection()
		List<String> titleHeaders = getBaseTitleHeaders()
		Map<String, List> export = [titleRow:titleHeaders]
		List rows = []
		Map<String, Object> data = getTitleData(configMap, entitlementInstance, sql)
		data.titles.eachWithIndex { GroovyRowResult title, int outer ->
			if(entitlementInstance == IssueEntitlement.class.name && data.coverageMap.get(title['ie_id'])) {
				data.coverageMap.get(title['ie_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					println "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('kbart', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces))
				}
			}
			else if(entitlementInstance == TitleInstancePackagePlatform.class.name && data.coverageMap.get(title['tipp_id'])) {
				data.coverageMap.get(title['tipp_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					println "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('kbart', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces))
				}
			}
			else {
				println "now processing record ${outer}"
				rows.add(buildRow('kbart', title, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces))
			}
		}
		export.columnData = rows
		/*
		Set<IdentifierNamespace> otherTitleIdentifierNamespaces = getOtherIdentifierNamespaces(entitlementIDs,entitlementInstance)
		titleHeaders.addAll(otherTitleIdentifierNamespaces.collect { IdentifierNamespace ns -> "${ns.ns}_identifer"})
		int max = 500
		TitleInstancePackagePlatform.withSession { Session sess ->
			for(int offset = 0; offset < entitlementIDs.size(); offset+=max) {
				List allRows = []
				Set<TitleInstancePackagePlatform> titleInstances = []
				//this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
				if(entitlementInstance == TitleInstancePackagePlatform.class.name) {
					titleInstances.addAll(TitleInstancePackagePlatform.findAllByIdInList(entitlementIDs.drop(offset).take(max),[sort:'sortname']))
					titleInstances.each { TitleInstancePackagePlatform tipp ->
						if(!tipp.coverages && !tipp.priceItems) {
							allRows << tipp
						}
						else if(tipp.coverages.size() > 1){
							tipp.coverages.each { AbstractCoverage covStmt ->
								allRows << covStmt
							}
						}
						else {
							allRows << tipp
						}
					}
				}
				else if(entitlementInstance == IssueEntitlement.class.name) {
					Set<IssueEntitlement> issueEntitlements = IssueEntitlement.findAllByIdInList(entitlementIDs.drop(offset).take(max),[sort:'tipp.sortname'])
					issueEntitlements.each { IssueEntitlement entitlement ->
						titleInstances << entitlement.tipp
						if(!entitlement.coverages && !entitlement.priceItems) {
							allRows << entitlement
						}
						else if(entitlement.coverages.size() > 1){
							entitlement.coverages.each { AbstractCoverage covStmt ->
								allRows << covStmt
							}
						}
						else {
							allRows << entitlement
						}
					}
				}
				allRows.each { rowData ->
					IssueEntitlement entitlement = getIssueEntitlement(rowData)
					TitleInstancePackagePlatform tipp = getTipp(rowData)
					AbstractCoverage covStmt = getCoverageStatement(rowData)

					List row = []
					//log.debug("processing ${tipp.name}")
					//publication_title
					row.add("${tipp.name}")

					//print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
					if(tipp.getIdentifierValue('pISBN'))
						row.add(tipp.getIdentifierValue('pISBN'))
					else if(tipp.getIdentifierValue('ISSN'))
						row.add(tipp.getIdentifierValue('ISSN'))
					else row.add(' ')
					//online_identifier
					if(tipp.getIdentifierValue('ISBN'))
						row.add(tipp.getIdentifierValue('ISBN'))
					else if(tipp.getIdentifierValue('eISSN'))
						row.add(tipp.getIdentifierValue('eISSN'))
					else row.add(' ')

					if(covStmt) {
						//date_first_issue_online
						row.add(covStmt.startDate ? formatter.format(covStmt.startDate) : ' ')
						//num_first_volume_online
						row.add(covStmt.startVolume ?: ' ')
						//num_first_issue_online
						row.add(covStmt.startIssue ?: ' ')
						//date_last_issue_online
						row.add(covStmt.endDate ? formatter.format(covStmt.endDate) : ' ')
						//num_last_volume_online
						row.add(covStmt.endVolume ?: ' ')
						//num_last_issue_online
						row.add(covStmt.endIssue ?: ' ')
					}
					else {
						//empty values for coverage fields
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//title_url
					row.add(tipp.hostPlatformURL ?: ' ')
					//first_author (no value?)
					row.add(tipp.firstAuthor ?: ' ')
					//title_id (no value?)
					row.add(' ')
					if(covStmt) {
						//embargo_information
						row.add(covStmt.embargo ?: ' ')
						//coverage_depth
						row.add(covStmt.coverageDepth ?: ' ')
						//notes
						row.add(covStmt.coverageNote ?: ' ')
					}
					else {
						//empty values for coverage fields
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//publication_type
					switch(tipp.titleType) {
						case "Journal": row.add('serial')
							break
						case "Book": row.add('monograph')
							break
						case "Database": row.add('database')
							break
						default: row.add('other')
							break
					}
					//publisher_name
					row.add(tipp.publisherName)
					if(tipp.titleType == 'Book') {
						//date_monograph_published_print (no value unless BookInstance)
						row.add(tipp.dateFirstInPrint ? formatter.format(tipp.dateFirstInPrint) : ' ')
						//date_monograph_published_online (no value unless BookInstance)
						row.add(tipp.dateFirstOnline ? formatter.format(tipp.dateFirstOnline) : ' ')
						//monograph_volume (no value unless BookInstance)
						row.add(tipp.volume ?: ' ')
						//monograph_edition (no value unless BookInstance)
						row.add(tipp.editionNumber ?: ' ')
						//first_editor (no value unless BookInstance)
						row.add(tipp.firstEditor ?: ' ')
					}
					else {
						//empty values from date_monograph_published_print to first_editor
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//parent_publication_title_id (no values defined for LAS:eR, must await GOKb)
					row.add(' ')
					//preceding_publication_title_id (no values defined for LAS:eR, must await GOKb)
					row.add(' ')
					/*
                    switch(entitlement.tipp.payment) {
                        case RDStore.TIPP_PAYMENT_OA: row.add('F')
                            break
                        case RDStore.TIPP_PAYMENT_PAID: row.add('P')
                            break
                        default: row.add(' ')
                            break
                    }
					//package_name
					row.add(tipp.pkg.name ?: ' ')
					//package_id
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.PKG_ID,','))
					//last_changed
					row.add(tipp.lastUpdated ? formatter.format(tipp.lastUpdated) : ' ')
					//access_start_date
					row.add(entitlement?.accessStartDate ? formatter.format(entitlement.accessStartDate) : (tipp.accessStartDate ? formatter.format(tipp.accessStartDate) : ' ') )
					//access_end_date
					row.add(entitlement?.accessEndDate ? formatter.format(entitlement.accessEndDate) : (tipp.accessEndDate ? formatter.format(tipp.accessEndDate) : ' '))
					//medium
					row.add(tipp.medium ? tipp.medium.value : ' ')


					//zdb_id
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ZDB,','))
					//doi_identifier
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.DOI,','))
					//ezb_id
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.EZB,','))
					//title_gokb_uuid
					row.add(tipp.gokbId)
					//package_gokb_uid
					row.add(tipp.pkg.gokbId)
					//package_isci
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISCI,','))
					//package_isil
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISIL_PAKETSIGEL,','))
					//package_ezb_anchor
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.EZB_ANCHOR,','))
					//ill_indicator
					row.add(' ')
					//superceding_publication_title_id
					row.add(' ')
					//monograph_parent_collection_title
					row.add(tipp.seriesName ?: '')
					//subject_area
					row.add(tipp.subjectReference ?: '')
					//status
					row.add(tipp.status.value ?: '')
					//access_type (no values defined for LAS:eR, must await GOKb)
					row.add(tipp.accessType ? tipp.accessType.value : '')
					//oa_type
					row.add(tipp.openAccess ? tipp.openAccess.value : '')

					//zdb_ppn
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ZDB_PPN,','))
					if(entitlement) {
                        //ezb_anchor
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.EZB_ANCHOR,','))
                        //ezb_collection_id
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.EZB_COLLECTION_ID,','))
                        //subscription_isil
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.ISIL_PAKETSIGEL,','))
                        //subscription_isci
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.ISCI,','))
                    }
                    else {
                        //empty values for subscription identifiers
                        row.add(' ')
                        row.add(' ')
                        row.add(' ')
                        row.add(' ')
                    }
					//ISSNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ISSN,','))
                    //eISSNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.EISSN,','))
                    //pISBNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.PISBN,','))
                    //ISBNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.PISBN,','))
					//other identifier namespaces

					if(entitlement?.priceItems) {
						//listprice_eur
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_EUR}?.listPrice ?: ' ')
						//listprice_gbp
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_GBP}?.listPrice ?: ' ')
						//listprice_usd
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_USD}?.listPrice ?: ' ')
						//localprice_eur
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_EUR}?.localPrice ?: ' ')
						//localprice_gbp
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_GBP}?.localPrice ?: ' ')
						//localprice_usd
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_USD}?.localPrice ?: ' ')
					} else if (entitlementInstance == TitleInstancePackagePlatform.class.name && tipp.priceItems) {
						//listprice_eur
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }?.listPrice ?: ' ')
						//listprice_gbp
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }?.listPrice ?: ' ')
						//listprice_usd
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice ?: ' ')
						//localprice_eur
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_EUR }?.localPrice ?: ' ')
						//localprice_gbp
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_GBP }?.localPrice ?: ' ')
						//localprice_usd
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_USD }?.localPrice ?: ' ')
					}
					else {
						//empty values for price item columns
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}

					otherTitleIdentifierNamespaces.each { IdentifierNamespace ns ->
						row.add(joinIdentifiers(tipp.ids,ns.ns,','))
					}
					export.columnData.add(row)
				}
				println("flushing after ${offset} ...")
				sess.flush()
			}
		}
		*/
		log.debug("End generateTitleExportKBART")
		export
	}

	/**
	 * Concatenates the set of identifiers belonging to the given namespace to a character-separated list
	 * @param ids the set of identifiers to output
	 * @param namespace the namespace whose identifiers should be concatenated
	 * @param separator the character to use for separation
	 * @return the concatenated string of identifiers
	 */
	String joinIdentifiers(Set<Identifier>ids, String namespace, String separator) {
		String joined = ' '
		List values = []
		ids.each { id ->
			if(id.ns.ns.equalsIgnoreCase(namespace)) {
				values.add(id.value)
			}
		}
		if(values)
			joined = values.join(separator)
		joined
	}

	String joinIdentifiersSQL(List<String> ids, String separator) {
		String joined = ' '
		if(ids)
			joined = ids.join(separator)
		joined
	}

	/**
	 * became a duplicate of generateTitleExportKBART - separate usage intended???
	 */
	@Deprecated
	Map<String,List> generateTitleExportCSV(Collection entitlementIDs, String entitlementInstance) {
		log.debug("Begin generateTitleExportCSV")
		Set<IdentifierNamespace> otherTitleIdentifierNamespaces = getOtherIdentifierNamespaces(entitlementIDs,entitlementInstance)
		List<String> titleHeaders = getBaseTitleHeadersForCSV()
		titleHeaders.addAll(otherTitleIdentifierNamespaces.collect { IdentifierNamespace ns -> "${ns.ns}_identifer"})
		Map<String,List> export = [titleRow:titleHeaders,rows:[]]
		int max = 500
		TitleInstancePackagePlatform.withSession { Session sess ->
			for(int offset = 0; offset < entitlementIDs.size(); offset+=max) {
				List allRows = []
				Set<TitleInstancePackagePlatform> titleInstances = []
				//this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
				if(entitlementInstance == TitleInstancePackagePlatform.class.name) {
					titleInstances.addAll(TitleInstancePackagePlatform.findAllByIdInList(entitlementIDs.drop(offset).take(max),[sort:'sortname']))
					titleInstances.each { TitleInstancePackagePlatform tipp ->
						if(!tipp.coverages && !tipp.priceItems) {
							allRows << tipp
						}
						else if(tipp.coverages.size() > 1){
							tipp.coverages.each { AbstractCoverage covStmt ->
								allRows << covStmt
							}
						}
						else {
							allRows << tipp
						}
					}
				}
				else if(entitlementInstance == IssueEntitlement.class.name) {
					Set<IssueEntitlement> issueEntitlements = IssueEntitlement.findAllByIdInList(entitlementIDs.drop(offset).take(max),[sort:'tipp.sortname'])
					issueEntitlements.each { IssueEntitlement entitlement ->
						titleInstances << entitlement.tipp
						if(!entitlement.coverages && !entitlement.priceItems) {
							allRows << entitlement
						}
						else if(entitlement.coverages.size() > 1){
							entitlement.coverages.each { AbstractCoverage covStmt ->
								allRows << covStmt
							}
						}
						else {
							allRows << entitlement
						}
					}
				}
				allRows.each { rowData ->
					IssueEntitlement entitlement = getIssueEntitlement(rowData)
					TitleInstancePackagePlatform tipp = getTipp(rowData)
					AbstractCoverage covStmt = getCoverageStatement(rowData)
					List row = []
					//log.debug("processing ${tipp.name}")
					//publication_title
					row.add("${tipp.name}")

					//print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
					if(tipp.getIdentifierValue('pISBN'))
						row.add(tipp.getIdentifierValue('pISBN'))
					else if(tipp.getIdentifierValue('ISSN'))
						row.add(tipp.getIdentifierValue('ISSN'))
					else row.add(' ')
					//online_identifier
					if(tipp.getIdentifierValue('ISBN'))
						row.add(tipp.getIdentifierValue('ISBN'))
					else if(tipp.getIdentifierValue('eISSN'))
						row.add(tipp.getIdentifierValue('eISSN'))
					else row.add(' ')

					if(covStmt) {
						//date_first_issue_online
						row.add(covStmt.startDate ? formatter.format(covStmt.startDate) : ' ')
						//num_first_volume_online
						row.add(covStmt.startVolume ?: ' ')
						//num_first_issue_online
						row.add(covStmt.startIssue ?: ' ')
						//date_last_issue_online
						row.add(covStmt.endDate ? formatter.format(covStmt.endDate) : ' ')
						//num_last_volume_online
						row.add(covStmt.endVolume ?: ' ')
						//num_last_issue_online
						row.add(covStmt.endIssue ?: ' ')
					}
					else {
						//empty values for coverage fields
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//title_url
					row.add(tipp.hostPlatformURL ?: ' ')
					//first_author (no value?)
					row.add(tipp.firstAuthor ?: ' ')
					//title_id (no value?)
					row.add(' ')
					if(covStmt) {
						//embargo_information
						row.add(covStmt.embargo ?: ' ')
						//coverage_depth
						row.add(covStmt.coverageDepth ?: ' ')
						//notes
						row.add(covStmt.coverageNote ?: ' ')
					}
					else {
						//empty values for coverage fields
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//publication_type
					switch(tipp.titleType) {
						case "Journal": row.add('serial')
							break
						case "Book": row.add('monograph')
							break
						case "Database": row.add('database')
							break
						default: row.add('other')
							break
					}
					//publisher_name
					row.add(tipp.publisherName)
					if(tipp.titleType == 'Book') {
						//date_monograph_published_print (no value unless BookInstance)
						row.add(tipp.dateFirstInPrint ? formatter.format(tipp.dateFirstInPrint) : ' ')
						//date_monograph_published_online (no value unless BookInstance)
						row.add(tipp.dateFirstOnline ? formatter.format(tipp.dateFirstOnline) : ' ')
						//monograph_volume (no value unless BookInstance)
						row.add(tipp.volume ?: ' ')
						//monograph_edition (no value unless BookInstance)
						row.add(tipp.editionNumber ?: ' ')
						//first_editor (no value unless BookInstance)
						row.add(tipp.firstEditor ?: ' ')
					}
					else {
						//empty values from date_monograph_published_print to first_editor
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					//parent_publication_title_id (no values defined for LAS:eR, must await GOKb)
					row.add(' ')
					//preceding_publication_title_id (no values defined for LAS:eR, must await GOKb)
					row.add(' ')
					//access_type (no values defined for LAS:eR, must await GOKb)
					row.add(tipp.accessType ? tipp.accessType.value : ' ')
					/*
                    switch(entitlement.tipp.payment) {
                        case RDStore.TIPP_PAYMENT_OA: row.add('F')
                            break
                        case RDStore.TIPP_PAYMENT_PAID: row.add('P')
                            break
                        default: row.add(' ')
                            break
                    }*/
					//package_name
					row.add(tipp.pkg.name ?: ' ')
					//package_id
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.PKG_ID,','))
					//last_changed
					row.add(tipp.lastUpdated ? formatter.format(tipp.lastUpdated) : ' ')
					//access_start_date
					row.add(entitlement?.accessStartDate ? formatter.format(entitlement.accessStartDate) : (tipp.accessStartDate ? formatter.format(tipp.accessStartDate) : ' ') )
					//access_end_date
					row.add(entitlement?.accessEndDate ? formatter.format(entitlement.accessEndDate) : (tipp.accessEndDate ? formatter.format(tipp.accessEndDate) : ' '))
					//medium
					row.add(tipp.medium ? tipp.medium.value : ' ')


					//zdb_id
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ZDB,','))
					//doi_identifier
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.DOI,','))
					//ezb_id
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.EZB,','))
					//title_gokb_uuid
					row.add(tipp.gokbId)
					//package_gokb_uid
					row.add(tipp.pkg.gokbId)
					//package_isci
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISCI,','))
					//package_isil
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISIL_PAKETSIGEL,','))
					//package_ezb_anchor
					row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.EZB_ANCHOR,','))
					//ill_indicator
					row.add(' ')
					//superceding_publication_title_id
					row.add(' ')
					//monograph_parent_collection_title
					row.add(tipp.seriesName ?: '')
					//subject_area
					row.add(tipp.subjectReference ?: '')
					//status
					row.add(tipp.status.value ?: '')
					//access_type
					row.add(tipp.accessType ? tipp.accessType.value : '')
					//oa_type
					row.add(tipp.openAccess ? tipp.openAccess.value : '')

					//zdb_ppn
					row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ZDB_PPN,','))
					/*if(entitlement) {
                        //ezb_anchor
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.EZB_ANCHOR,','))
                        //ezb_collection_id
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.EZB_COLLECTION_ID,','))
                        //subscription_isil
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.ISIL_PAKETSIGEL,','))
                        //subscription_isci
                        row.add(joinIdentifiers(entitlement.subscription.ids,IdentifierNamespace.ISCI,','))
                    }
                    else {
                        //empty values for subscription identifiers
                        row.add(' ')
                        row.add(' ')
                        row.add(' ')
                        row.add(' ')
                    }*/
					/*//ISSNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.ISSN,','))
                    //eISSNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.EISSN,','))
                    //pISBNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.PISBN,','))
                    //ISBNs
                    row.add(joinIdentifiers(tipp.ids,IdentifierNamespace.PISBN,','))*/
					//other identifier namespaces

					if(entitlement?.priceItems) {
						//listprice_eur
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_EUR}?.listPrice ?: ' ')
						//listprice_gbp
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_GBP}?.listPrice ?: ' ')
						//listprice_usd
						row.add(entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_USD}?.listPrice ?: ' ')
						//localprice_eur
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_EUR}?.localPrice ?: ' ')
						//localprice_gbp
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_GBP}?.localPrice ?: ' ')
						//localprice_usd
						row.add(entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_USD}?.localPrice ?: ' ')
					} else if (tipp.priceItems) {
						//listprice_eur
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }?.listPrice ?: ' ')
						//listprice_gbp
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }?.listPrice ?: ' ')
						//listprice_usd
						row.add(tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice ?: ' ')
						//localprice_eur
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_EUR }?.localPrice ?: ' ')
						//localprice_gbp
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_GBP }?.localPrice ?: ' ')
						//localprice_usd
						row.add(tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_USD }?.localPrice ?: ' ')
					}
					else {
						//empty values for price item columns
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
						row.add(' ')
					}
					if(entitlement) {
						//is Perpetual Access
						row.add(entitlement.perpetualAccessBySub ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value'))
					}else {
						row.add(' ')
					}

					otherTitleIdentifierNamespaces.each { IdentifierNamespace ns ->
						row.add(joinIdentifiers(tipp.ids,ns.ns,','))
					}
					export.rows.add(row)
				}
				println("flush after ${offset} ...")
				sess.flush()
			}
		}

		log.debug("End generateTitleExportCSV")
		export
	}

	/**
	 * Was initially set up for generating Excel-export; as for some reason, the CSV export has been abandoned, this export is now
	 * generic export for non-KBART headers, used for purposes where the KBART headers are not needed in full.
	 * To use this export for tab-separated values, access result.rows.field directly for the data; it is like PHP's array_column()
	 * @param entitlementIDs the IDs of the issue entitlements or title instances to export
	 * @param entitlementInstance the class name to look for the complete objects
	 * @return a {@link Map} containing headers and data for export; it may be used for Excel worksheets as style information is defined in format-style maps or for
	 * raw text output; access rows.field for the bare data
	 */
	Map<String, List> generateTitleExportCustom(Map configMap, String entitlementInstance, List showStatsInMonthRings = [], Org subscriber = null, Collection perpetuallyPurchasedTitleURLs = []) {
		log.debug("Begin generateTitleExportCustom")
		Sql sql = GlobalService.obtainSqlConnection()
		Locale locale = LocaleContextHolder.getLocale()
		Map<String, Object> data = getTitleData(configMap, entitlementInstance, sql)
		List<String> titleHeaders = [
				messageSource.getMessage('tipp.name',null,locale),
				'Print Identifier',
				'Online Identifier',
				messageSource.getMessage('package.label',null,locale),
				messageSource.getMessage('platform.label',null,locale),
				messageSource.getMessage('tipp.titleType',null,locale),
				messageSource.getMessage('tipp.publisher',null,locale),
				messageSource.getMessage('tipp.medium',null,locale),
				messageSource.getMessage('tipp.accessStartDate',null,locale),
				messageSource.getMessage('tipp.accessEndDate',null,locale),
				messageSource.getMessage('tipp.hostPlatformURL',null,locale),
				messageSource.getMessage('tipp.firstAuthor',null,locale),
				messageSource.getMessage('tipp.firstEditor',null,locale),
				messageSource.getMessage('tipp.startDate',null,locale),
				messageSource.getMessage('tipp.startVolume',null,locale),
				messageSource.getMessage('tipp.startIssue',null,locale),
				messageSource.getMessage('tipp.endDate',null,locale),
				messageSource.getMessage('tipp.endVolume',null,locale),
				messageSource.getMessage('tipp.endIssue',null,locale),
				messageSource.getMessage('tipp.embargo',null,locale),
				messageSource.getMessage('tipp.coverageDepth',null,locale),
				messageSource.getMessage('tipp.coverageNote',null,locale),
				messageSource.getMessage('tipp.dateFirstInPrint',null,locale),
				messageSource.getMessage('tipp.dateFirstOnline',null,locale),
				messageSource.getMessage('tipp.volume',null,locale),
				messageSource.getMessage('tipp.editionNumber',null,locale),
				messageSource.getMessage('tipp.seriesName',null,locale),
				messageSource.getMessage('tipp.subjectReference',null,locale),
				messageSource.getMessage('tipp.status',null,locale),
				messageSource.getMessage('tipp.accessType',null,locale),
				messageSource.getMessage('tipp.openAccess',null,locale),
				messageSource.getMessage('tipp.listprice_eur',null,locale),
				messageSource.getMessage('tipp.listprice_gbp',null,locale),
				messageSource.getMessage('tipp.listprice_usd',null,locale),
				messageSource.getMessage('tipp.localprice_eur',null,locale),
				messageSource.getMessage('tipp.localprice_gbp',null,locale),
				messageSource.getMessage('tipp.localprice_usd',null,locale)]
		titleHeaders.addAll(data.coreTitleIdentifierNamespaces.collect { GroovyRowResult row -> row['idns_ns']})
		titleHeaders.addAll(data.otherTitleIdentifierNamespaces.collect { GroovyRowResult row -> row['idns_ns']})
		if(showStatsInMonthRings){
			titleHeaders.addAll(showStatsInMonthRings.collect { Date month -> month.format('yyyy-MM') })
		}
		List rows = []
		Map<String,List> export = [titles:titleHeaders]
		data.titles.eachWithIndex { GroovyRowResult title, int outer ->
			if(entitlementInstance == IssueEntitlement.class.name && data.coverageMap.get(title['ie_id'])) {
				data.coverageMap.get(title['ie_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					println "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('excel', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces, perpetuallyPurchasedTitleURLs, showStatsInMonthRings, subscriber))
				}
			}
			else if(entitlementInstance == TitleInstancePackagePlatform.class.name && data.coverageMap.get(title['tipp_id'])) {
				data.coverageMap.get(title['tipp_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					println "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('excel', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces, perpetuallyPurchasedTitleURLs, showStatsInMonthRings, subscriber))
				}
			}
			else {
				println "now processing record ${outer}"
				rows.add(buildRow('excel', title, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces, perpetuallyPurchasedTitleURLs, showStatsInMonthRings, subscriber))
			}
		}
		export.rows = rows
		log.debug("End generateTitleExportCustom")
		export
	}

	/**
	 * Gets the list of column headers for KBART export
	 * @return a list of column headers
	 */
	List<String> getBaseTitleHeaders() {
		['publication_title',
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
		 'package_id',
		 'last_changed',
		 'access_start_date',
		 'access_end_date',
		 'medium',
		 'zdb_id',
		 'doi_identifier',
		 'ezb_id',
		 'title_gokb_uuid',
		 'package_gokb_uuid',
		 'package_isci',
		 'package_isil',
		 'package_ezb_anchor',
		 'ill_indicator',
		 'superceding_publication_title_id',
		 'monograph_parent_collection_title',
		 'subject_area',
		 'status',
		 'access_type',
		 'oa_type',
		 'zdb_ppn',
		 'listprice_eur',
		 'listprice_gbp',
		 'listprice_usd',
		 'localprice_eur',
		 'localprice_gbp',
		 'localprice_usd']
	}

	@Deprecated
	List<String> getBaseTitleHeadersForCSV() {
		['publication_title',
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
		 'access_type',
		 'package_name',
		 'package_id',
		 'last_changed',
		 'access_start_date',
		 'access_end_date',
		 'medium',
		 'zdb_id',
		 'doi_identifier',
		 'ezb_id',
		 'title_gokb_uuid',
		 'package_gokb_uuid',
		 'package_isci',
		 'package_isil',
		 'package_ezb_anchor',
		 'ill_indicator',
		 'superceding_publication_title_id',
		 'monograph_parent_collection_title',
		 'subject_area',
		 'status',
		 'access_type',
		 'oa_type',
		 'zdb_ppn',
		 'listprice_eur',
		 'listprice_gbp',
		 'listprice_usd',
		 'localprice_eur',
		 'localprice_gbp',
		 'localprice_usd',
		 'perpetual_access']
	}

	/**
	 * Builds a row for the export table, assembling the data contained in the output
	 * @param format the format of the exporting table
	 * @param titleRecord the title to be displayed in the row
	 * @param identifierMap a map of title {@link Identifier}s
	 * @param priceItemMap a map of title {@link de.laser.finance.PriceItem}s
	 * @param reportMap a map of COUNTER reports (see {@link AbstractReport} implementations)
	 * @param perpetuallyPurchasedTitleURLs a list of title URLs of titles which the given subscriber have perpetually bought
	 * @param coreTitleIdentifierNamespaces {@link List} of identifier namespaces which are core set for titles
	 * @param otherTitleIdentifierNamespaces {@link List} of identifier namespaces beyond the core set
	 * @param showStatsInMonthRings if given: a {@link List} of usage report months
	 * @param subscriber the institution ({@link Org}) whose holding should be exported
	 * @return a {@link List} containing the columns for the next output row
	 */
	List buildRow(String format, GroovyRowResult titleRecord, Map identifierMap, Map priceItemMap, Map reportMap, List<GroovyRowResult> coreTitleIdentifierNamespaces, List<GroovyRowResult> otherTitleIdentifierNamespaces, Collection perpetuallyPurchasedTitleURLs = [], List showStatsInMonthRings = [], Org subscriber = null) {
		titleRecord.identifiers = identifierMap.get(titleRecord['tipp_id'])
		if(titleRecord.containsKey('ie_id')) {
			titleRecord.priceItems = priceItemMap.get(titleRecord['ie_id'])
		}
		else {
			titleRecord.priceItems = priceItemMap.get(titleRecord['tipp_id'])
		}
		if(reportMap.containsKey(titleRecord['tipp_id']))
			titleRecord.usages = reportMap.get(titleRecord['tipp_id'])
		String style = null
		if(titleRecord['tipp_host_platform_url'] in perpetuallyPurchasedTitleURLs)
			style = 'negative'
		List row = []
		row.add(createCell(format, titleRecord['name'], style))
		if(titleRecord.identifiers) {
			//print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
			if(titleRecord.identifiers.get('pisbn'))
				row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('pisbn'), ','), style))
			else if(titleRecord.identifiers.get('issn'))
				row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('issn'), ','), style))
			else row.add(createCell(format, '', style))
			//online_identifier
			if(titleRecord.identifiers.get('isbn'))
				row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('isbn'), ','), style))
			else if(titleRecord.identifiers.get('eissn'))
				row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('eissn'), ','), style))
			else row.add(createCell(format, '', style))
		}
		else {
			row.add(createCell(format, '', style))
			row.add(createCell(format, '', style))
		}
		row.add(createCell(format, titleRecord['tipp_pkg_name'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_plat_name'] ?: '', style))
		row.add(createCell(format, titleRecord['title_type'], style))
		row.add(createCell(format, titleRecord['tipp_publisher_name'] ? titleRecord['tipp_publisher_name'] : '', style))
		row.add(createCell(format, titleRecord['tipp_medium'] ? titleRecord['tipp_medium'] : '', style))
		row.add(createCell(format, titleRecord['accessStartDate'] ? formatter.format(titleRecord['accessStartDate']) : '', style))
		row.add(createCell(format, titleRecord['accessEndDate'] ? formatter.format(titleRecord['accessEndDate']) : '', style))
		row.add(createCell(format, titleRecord['tipp_host_platform_url'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_first_author'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_first_editor'] ?: '', style))
		//date_first_issue_online
		row.add(createCell(format, titleRecord.containsKey('startDate') && titleRecord.startDate ? formatter.format(titleRecord.startDate) : ' ', style))
		//num_first_volume_online
		row.add(createCell(format, titleRecord.containsKey('startVolume') && titleRecord.startVolume ?: ' ', style))
		//num_first_issue_online
		row.add(createCell(format, titleRecord.containsKey('startIssue') && titleRecord.startIssue ?: ' ', style))
		//date_last_issue_online
		row.add(createCell(format, titleRecord.containsKey('endDate') && titleRecord.endDate ? formatter.format(titleRecord.endDate) : ' ', style))
		//num_last_volume_online
		row.add(createCell(format, titleRecord.containsKey('endVolume') && titleRecord.endVolume ?: ' ', style))
		//num_last_issue_online
		row.add(createCell(format, titleRecord.containsKey('endIssue') && titleRecord.endIssue ?: ' ', style))
		//embargo_information
		row.add(createCell(format, titleRecord.containsKey('embargo') && titleRecord.embargo ?: ' ', style))
		//coverage_depth
		row.add(createCell(format, titleRecord.containsKey('coverageDepth') && titleRecord.coverageDepth ?: ' ', style))
		//notes
		row.add(createCell(format, titleRecord.containsKey('coverageNote') && titleRecord.coverageNote ?: ' ', style))
		if(titleRecord['title_type'] == 'monograph') {
			row.add(createCell(format, titleRecord['tipp_date_first_in_print'] ? formatter.format(titleRecord['tipp_date_first_in_print']) : ' ', style))
			row.add(createCell(format, titleRecord['tipp_date_first_online'] ? formatter.format(titleRecord['tipp_date_first_online']) : ' ', style))
			row.add(createCell(format, titleRecord['tipp_volume'] ?: ' ', style))
			row.add(createCell(format, titleRecord['tipp_edition_number'] ?: ' ', style))
		}
		else {
			//empty values from date_monograph_published_print to first_editor
			row.add(createCell(format, '', style))
			row.add(createCell(format, '', style))
			row.add(createCell(format, '', style))
			row.add(createCell(format, '', style))
		}
		row.add(createCell(format, titleRecord['tipp_series_name'] ?: ' ', style))
		row.add(createCell(format, titleRecord['tipp_subject_reference'] ?: ' ', style))
		row.add(createCell(format, titleRecord['status'] ?: ' ', style))
		row.add(createCell(format, titleRecord['accessType'] ?: ' ', style))
		row.add(createCell(format, titleRecord['openAccess'] ?: ' ', style))
		if(titleRecord.priceItems) {
			//listprice_eur
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_EUR.value)?.get('pi_list_price') ?: ' ', style))
			//listprice_gbp
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_GBP.value)?.get('pi_list_price') ?: ' ', style))
			//listprice_usd
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_USD.value)?.get('pi_list_price') ?: ' ', style))
			//localprice_eur
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_EUR.value)?.get('pi_local_price') ?: ' ', style))
			//localprice_gbp
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_GBP.value)?.get('pi_local_price') ?: ' ', style))
			//localprice_usd
			row.add(createCell(format, titleRecord.priceItems.get(RDStore.CURRENCY_USD.value)?.get('pi_local_price') ?: ' ', style))
		}
		else {
			//empty values for price item columns
			row.add(createCell(format, ' ', style))
			row.add(createCell(format, ' ', style))
			row.add(createCell(format, ' ', style))
			row.add(createCell(format, ' ', style))
			row.add(createCell(format, ' ', style))
			row.add(createCell(format, ' ', style))
		}

		coreTitleIdentifierNamespaces.each { GroovyRowResult ns ->
			row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers?.get(ns['idns_ns']), ','), style))
		}
		otherTitleIdentifierNamespaces.each { GroovyRowResult ns ->
			row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers?.get(ns['idns_ns']),','), style))
		}

		if(showStatsInMonthRings && subscriber) {
			Map<Date, GroovyRowResult> usageMap = reportMap.get(titleRecord['tipp_id'])
			if(usageMap) {
				Calendar filterTime = GregorianCalendar.getInstance()
				showStatsInMonthRings.each { Date month ->
					filterTime.setTime(month)
					GroovyRowResult counterReport
					counterReport = usageMap.get(filterTime.getTime())
					if(counterReport){
						//println(counterReport)
						//println(counterReport.reportCount ?: '')
						row.add(createCell(format, counterReport['report_count'] ?: '', style))
					}
					else
						row.add(createCell(format, ' ', style))
				}
			}
		}
		row
	}

	def createCell(String format, data, String style = null) {
		if(format == 'excel')
			[field: data, style: style]
		else {
			if(format == 'kbart' && data == '')
				' '
			else "${data}"
		}
	}

	Map<String, Object> getTitleData(Map configMap, String entitlementInstance, Sql sql, List showStatsInMonthRings = [], Org subscriber = null) {
		Map<String, Object> queryData = filterService.prepareTitleSQLQuery(configMap, entitlementInstance, sql)
		List<GroovyRowResult> titles = sql.rows(queryData.query+queryData.join+' where '+queryData.where+queryData.order, queryData.params),
							  identifiers, coverages, priceItems, coreTitleIdentifierNamespaces, otherTitleIdentifierNamespaces
		Map<Long, List<GroovyRowResult>> coverageMap = [:], priceItemMap = [:]
		Map<Long, Map<Date, GroovyRowResult>> reportMap = [:]
		Map<Long, Map<String, List<String>>> identifierMap = [:]
		List<String> coreTitleNSrestricted = IdentifierNamespace.CORE_TITLE_NS.collect { String coreTitleNS ->
			!(coreTitleNS in [IdentifierNamespace.ISBN, IdentifierNamespace.PISBN, IdentifierNamespace.ISSN, IdentifierNamespace.EISSN])
		}
		if(entitlementInstance == TitleInstancePackagePlatform.class.name) {
			identifiers = sql.rows("select id_tipp_fk, id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id ${queryData.join} where ${queryData.where}", queryData.params)
			coverages = sql.rows("select tc_tipp_fk, tc_start_date as startDate, tc_start_volume as startVolume, tc_start_issue as startIssue, tc_end_date as endDate, tc_end_volume as endIssue, tc_end_issue as endIssue, tc_coverage_note as coverageNote, tc_coverage_depth as coverageDepth, tc_embargo as embargo from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id ${queryData.join} where ${queryData.where}", queryData.params)
			priceItems = sql.rows("select pi_tipp_fk, pi_list_price, (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk) as pi_list_currency, pi_local_price, (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk) as pi_local_currency from price_item join title_instance_package_platform on pi_tipp_fk = tipp_id ${queryData.join} where ${queryData.where}", queryData.params)
			coreTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id ${queryData.join} where idns_ns in ('${coreTitleNSrestricted.join("','")}') and ${queryData.where}", queryData.params)
			otherTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id ${queryData.join} where idns_ns not in ('${IdentifierNamespace.CORE_TITLE_NS.join("','")}') and ${queryData.where}", queryData.params)
			identifierMap.putAll(preprocessIdentifierRows(identifiers))
			coverageMap.putAll(preprocessRows(coverages, 'tc_tipp_fk'))
			priceItemMap.putAll(preprocessPriceItemRows(priceItems, 'pi_tipp_fk'))
		}
		else if(entitlementInstance == IssueEntitlement.class.name) {
			identifiers = sql.rows("select id_tipp_fk, id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id join issue_entitlement on ie_tipp_fk = id_tipp_fk join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			coverages = sql.rows("select ic_ie_fk, ic_start_date as startDate, ic_start_volume as startVolume, ic_start_issue as startIssue, ic_end_date as endDate, ic_end_volume as endVolume, ic_end_issue as endIssue, ic_coverage_note as coverageNote, ic_coverage_depth as coverageDepth, ic_embargo as embargo from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			priceItems = sql.rows("select pi_ie_fk, pi_list_price, (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk) as pi_list_currency, pi_local_price, (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk) as pi_local_currency from price_item join issue_entitlement on pi_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			coreTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on ie_tipp_fk = tipp_id where idns_ns in ('${coreTitleNSrestricted.join("','")}') and ${queryData.where}", queryData.params)
			otherTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on ie_tipp_fk = tipp_id where idns_ns not in ('${IdentifierNamespace.CORE_TITLE_NS.join("','")}') and ${queryData.where}", queryData.params)
			identifierMap.putAll(preprocessIdentifierRows(identifiers))
			coverageMap.putAll(preprocessRows(coverages, 'ic_ie_fk'))
			priceItemMap.putAll(preprocessPriceItemRows(priceItems, 'pi_ie_fk'))
			if(showStatsInMonthRings && subscriber) {
				Connection connection = sql.dataSource.getConnection()
				Calendar filterTime = GregorianCalendar.getInstance()
				filterTime.setTime(showStatsInMonthRings.first())
				filterTime.set(Calendar.DATE, filterTime.getActualMinimum(Calendar.DAY_OF_MONTH))
				Timestamp startDate = new Timestamp(filterTime.getTime().getTime())
				filterTime.setTime(showStatsInMonthRings.last())
				filterTime.set(Calendar.DATE, filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
				Timestamp endDate = new Timestamp(filterTime.getTime().getTime())
				List<Map<Date, GroovyRowResult>> counterReportRows
				counterReportRows = sql.rows('select c5r_title_fk as title, c5r_report_from as report_from, c5r_report_to as report_to, c5r_report_count as report_count from counter5report join title_instance_package_platform on c5r_title_fk = tipp_id join issue_entitlement on ie_tipp_fk = c5r_title_fk where c5r_report_institution_fk = :customer and lower(c5r_report_type) = :defaultReport and c5r_metric_type = :defaultMetric and c5r_report_from >= :startDate and c5r_report_to <= :endDate and '+queryData.where, queryData.params+[customer: subscriber.id, startDate: startDate, endDate: endDate, defaultReport: Counter5ApiSource.TITLE_MASTER_REPORT, defaultMetric: 'Unique_Title_Requests'])
				if(!counterReportRows) {
					List<Object> defaultReports = [Counter4ApiSource.BOOK_REPORT_1, Counter4ApiSource.JOURNAL_REPORT_1]
					counterReportRows = Counter4Report.executeQuery('select c4r_title_fk as title, c4r_report_from as report_from, c4r_report_to as report_to, c4r_report_count as report_count from counter4report join title_instance_package_platform on c4r_title_fk = tipp_id join issue_entitlement on ie_tipp_fk = c4r_title_fk where c4r_report_institution_fk = :customer and c4r_report_type = any(:defaultReports) and c4r_metric_type = :defaultMetric and c4r_report_from >= :startDate and c4r_report_to <= :endDate and ' + queryData.where, queryData.params + [customer: subscriber.id, startDate: startDate, endDate: endDate, defaultReports: connection.createArrayOf('varchar', defaultReports.toArray()), defaultMetric: 'ft_total'])
				}
				counterReportRows.each { GroovyRowResult reportRow ->
					Map<Date, GroovyRowResult> usageMap = reportMap.get(reportRow['title'])
					if(!usageMap)
						usageMap = [:]
					usageMap.put((Date) reportRow['report_from'], reportRow)
					reportMap.put((Long) reportRow['title'], usageMap)
				}
			}
		}
		else {
			coreTitleIdentifierNamespaces = []
			otherTitleIdentifierNamespaces = []
		}
		[titles: titles, coverageMap: coverageMap, priceItemMap: priceItemMap, identifierMap: identifierMap, reportMap: reportMap,
		 coreTitleIdentifierNamespaces: coreTitleIdentifierNamespaces, otherTitleIdentifierNamespaces: otherTitleIdentifierNamespaces]
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
	 * Determines the title instance of the given object
	 * @param rowData the object containing the title
	 * @return the {@link TitleInstancePackagePlatform} title instance
	 */
	TitleInstancePackagePlatform getTipp(rowData) {
		if(rowData instanceof IssueEntitlement) {
			return rowData.tipp
		}
		else if(rowData instanceof IssueEntitlementCoverage) {
			return rowData.issueEntitlement.tipp
		}
		else if(rowData instanceof TitleInstancePackagePlatform) {
			return (TitleInstancePackagePlatform) rowData
		}
		else if(rowData instanceof TIPPCoverage) {
			return rowData.tipp
		}
		null
	}

	/**
	 * Determines the coverage statement, based on the instance type of the object
	 * @param rowData the title whose coverage statement should be recovered
	 * @return the coverage statement or null if there are more than one or none.
	 * If there are more than one coverage statements for a title, the set of coverages is processed in
	 * a loop elsewhere
	 */
	AbstractCoverage getCoverageStatement(rowData) {
		if(rowData instanceof IssueEntitlement) {
			return rowData.coverages.size() == 1 ? (IssueEntitlementCoverage) rowData.coverages[0] : null
		}
		else if(rowData instanceof IssueEntitlementCoverage) {
			return (IssueEntitlementCoverage) rowData
		}
		else if(rowData instanceof TitleInstancePackagePlatform) {
			return rowData.coverages.size() == 1 ? (TIPPCoverage) rowData.coverages[0] : null
		}
		else if(rowData instanceof TIPPCoverage) {
			return (TIPPCoverage) rowData
		}
		null
	}

	/**
	 * Gets all namespaces in which the given entitlements have identifiers and which are not among the core namespaces
	 * @param entitlements the title IDs whose identifiers should be queried
	 * @param entitlementInstance the type of instance whose IDs have been submitted
	 * @return a {@link Set} of {@link IdentifierNamespace}s whose identifiers should be exported
	 */
	Set<IdentifierNamespace> getOtherIdentifierNamespaces(Collection<Long> entitlements,String entitlementInstance) {
		Set<IdentifierNamespace> result = []
		//32768 is the maximum number of placeholders Postgres supports. Some placeholders must be reserved for coreTitleNS.
		List entitlementChunks = entitlements.collate(32767-IdentifierNamespace.CORE_TITLE_NS.size())
		entitlementChunks.each { Collection<Long> entitlementChunk ->
			//println('('+entitlementChunk.join(',')+')')
			String whereTo
			if(entitlementInstance == TitleInstancePackagePlatform.class.name)
				whereTo = 'id.tipp.id in (:titleInstances)'
			else if(entitlementInstance == IssueEntitlement.class.name)
				whereTo = 'id.tipp.id in (select ie.tipp from IssueEntitlement ie where ie.tipp.id in (:titleInstances))'
			if(whereTo)
				result.addAll(IdentifierNamespace.executeQuery('select distinct(ns) from Identifier id join id.ns ns where '+whereTo+' and ns.ns not in (:coreTitleNS)',[titleInstances:entitlementChunk,coreTitleNS:IdentifierNamespace.CORE_TITLE_NS]))
		}
		result
	}

	/**
	 * Gets all core title namespaces in which the given entitlements have identifiers
	 * @param entitlements the title IDs whose identifiers should be queried
	 * @param entitlementInstance the type of instance whose IDs have been submitted
	 * @return a {@link Set} of {@link IdentifierNamespace}s whose identifiers should be exported
	 */
	Set<IdentifierNamespace> getCoreIdentifierNamespaces(Collection<Long> entitlements,String entitlementInstance) {
		Set<IdentifierNamespace> result = []
		entitlements.collate(32767-IdentifierNamespace.CORE_TITLE_NS.size()).each { Collection<Long> entitlementChunk ->
			String whereTo
			if(entitlementInstance == TitleInstancePackagePlatform.class.name)
				whereTo = 'id.tipp.id in (:titleInstances)'
			else if(entitlementInstance == IssueEntitlement.class.name)
				whereTo = 'id.tipp.id in (select ie.tipp from IssueEntitlement ie where ie.tipp.id in (:titleInstances))'
			if(whereTo)
				result.addAll(IdentifierNamespace.executeQuery('select distinct(ns) from Identifier id join id.ns ns where '+whereTo+' and ns.ns in (:coreTitleNS)',[titleInstances:entitlementChunk,coreTitleNS:IdentifierNamespace.CORE_TITLE_NS]))
		}
		result
	}

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
