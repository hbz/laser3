package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractReport
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.CostItemGroup
import de.laser.finance.PriceItem
import de.laser.helper.Profiler
import de.laser.remote.ApiSource
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.base.AbstractCoverage
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
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
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Session
import org.springframework.context.MessageSource

import java.awt.*
import java.math.RoundingMode
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

	ContextService contextService
	FilterService filterService
	GokbService gokbService
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
		Locale locale = LocaleUtils.getCurrentLocale()
		List titles = [messageSource.getMessage('org.sortname.label',null,locale), 'Name', messageSource.getMessage('globalUID.label',null,locale)]


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
		titles.add(RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value'))
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
				sheet.trackAllColumnsForAutoSizing()

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

	def exportAddressbook(String format, List visiblePersons) {
		Locale locale = LocaleUtils.getCurrentLocale()
		Map<String, String> columnHeaders = [
		        'organisation': messageSource.getMessage('address.org.label', null, locale),
		        'receiver': messageSource.getMessage('address.receiver.label', null, locale),
		        'additionFirst': messageSource.getMessage('address.additionFirst.label', null, locale),
		        'additionSecond': messageSource.getMessage('address.additionSecond.label', null, locale),
		        'street_1': messageSource.getMessage('address.street_1.label', null, locale),
		        'street_2': messageSource.getMessage('address.street_2.label', null, locale),
		        'zipcode': messageSource.getMessage('address.zipcode.label', null, locale),
		        'city': messageSource.getMessage('address.city.label', null, locale),
		        'pob': messageSource.getMessage('address.pob.label', null, locale),
		        'pobZipcode': messageSource.getMessage('address.pobZipcode.label', null, locale),
		        'pobCity': messageSource.getMessage('address.pobCity.label', null, locale),
		        'country': messageSource.getMessage('address.country.label', null, locale),
		        'region': messageSource.getMessage('address.region.label', null, locale),
				'language': messageSource.getMessage('contact.language.label', null, locale),
				'email': messageSource.getMessage('contact.icon.label.email', null, locale),
				'fax': messageSource.getMessage('contact.icon.label.fax', null, locale),
				'url': messageSource.getMessage('contact.icon.label.url', null, locale),
				'phone': messageSource.getMessage('contact.icon.label.phone', null, locale)
		]
		Map<Person, Map<String, Map<String, String>>> addressesContacts = [:]
		visiblePersons.each { Person p ->
			//lang: contactData
			Map<String, Map<String, String>> contactData = addressesContacts.get(p)
			int langCtr = 0
			if(!contactData)
				contactData = [:]
			p.contacts.sort{ Contact cc -> cc.content }.each { Contact c ->
				String langKey
				if(c.language) {
					langKey = c.language.getI10n('value')
					int langCount = p.contacts.findAll { Contact cc -> cc.language == c.language }.size()
					if(langCount > 1) {
						langKey += langCtr
						langCtr++
					}
				}
				else langKey = Contact.PRIMARY
				Map<String, String> contact = contactData.get(langKey)
				if(!contact)
					contact = [:]
				switch(c.contentType) {
					case RDStore.CCT_EMAIL: contact.email = c.content
						break
					case RDStore.CCT_FAX: contact.fax = c.content
						break
					case RDStore.CCT_PHONE: contact.phone = c.content
						break
					case RDStore.CCT_URL: contact.url = c.content
						break
				}
				contactData.put(langKey, contact)
			}
			addressesContacts.put(p, contactData)
		}
        if(format == 'xlsx') {
			XSSFWorkbook workbook = new XSSFWorkbook()
			POIXMLProperties xmlProps = workbook.getProperties()
			POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
			coreProps.setCreator(messageSource.getMessage('laser',null,locale))
			SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50,true)
			Sheet sheet = wb.createSheet(messageSource.getMessage('menu.institutions.addressbook', null, locale))
			//the following three statements are required only for HSSF
			sheet.setAutobreaks(true)
			sheet.trackAllColumnsForAutoSizing()
			//the header row: centered text in 48pt font
			Row headerRow = sheet.createRow(0)
			headerRow.setHeightInPoints(16.75f)
			columnHeaders.eachWithIndex { String key, String header, int index ->
				Cell cell = headerRow.createCell(index)
				cell.setCellValue(header)
			}
			//freeze the first row
			sheet.createFreezePane(0, 1)
			Row row
			Cell cell
			int rownum = 1
			addressesContacts.each { Person p, Map<String, Map<String, String>> contactData ->
				for(int addressRow = 0; addressRow < contactData.size();addressRow++) {
					row = sheet.createRow(rownum)
					Map.Entry<String, Map<String, String>> contact = contactData.entrySet()[addressRow]
                    //Address a = p.addresses[addressRow]
					columnHeaders.keySet().eachWithIndex { String fieldKey, int cellnum ->
						cell = row.createCell(cellnum)
						if (fieldKey == 'organisation') {
							p.roleLinks.each { PersonRole pr ->
								if (pr.org) {
									cell.setCellValue(pr.org.name)
								}
							}
						}
						else if (fieldKey == 'receiver') {
							cell.setCellValue(p.toString())
						}
						else if (fieldKey == 'language')
							contact?.key == Contact.PRIMARY ? cell.setCellValue(' ') : cell.setCellValue(contact.key)
						else if (fieldKey in ['email', 'fax', 'phone', 'url'])
							cell.setCellValue(contact?.value?.get(fieldKey))
						/*
						else {
							if (a && a.hasProperty(fieldKey)) {
								if (a[fieldKey] instanceof RefdataValue)
									cell.setCellValue(a[fieldKey].getI10n("value"))
								else cell.setCellValue(a[fieldKey])
							}
						}
						*/
					}
					rownum++
				}
			}
			wb
        }
        else if(format == 'csv') {
			List rows = []
			List<String> row = []
			addressesContacts.each { Person p, Map<String, Map<String, String>> contactData ->
				for(int addressRow = 0; addressRow < contactData.size(); addressRow++) {
					row = []
					Map.Entry<String, Map<String, String>> contact = contactData.entrySet()[addressRow]
					//Address a = p.addresses[addressRow]
					columnHeaders.keySet().each { String fieldKey ->
						if(fieldKey == 'organisation') {
							row << p.roleLinks.find { PersonRole pr -> pr.org != null }.org.name
						}
						else if(fieldKey == 'receiver') {
							row << p.toString()
						}
						//first CSV column of address, used as gap-filler
						else if(fieldKey == 'additionFirst')
							row.addAll([' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '])
						else if(fieldKey == 'language') {
							if(contact?.key == Contact.PRIMARY)
								row << ' '
							else row << contact.key
						}
						else if(fieldKey in ['email', 'fax', 'phone', 'url']) {
							if(contact?.value && contact.value.get(fieldKey))
								row << contact.value.get(fieldKey)
							else row << ' '
						}
						/*
						else {
							if(a.hasProperty(fieldKey)) {
								if(a[fieldKey]) {
									row << a[fieldKey] instanceof RefdataValue ? a[fieldKey].getI10n("value") : a[fieldKey]
								}
								else {
									row << ' '
								}
							}
						}
						*/
					}
					rows << row
				}
			}
			generateSeparatorTableString(columnHeaders.values(), rows, ';')
        }
        else {
            log.error("invalid format submitted: ${format}")
            null
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
			List<AbstractPropertyWithCalculatedLastUpdated> propCheck = (new GroovyClassLoader()).loadClass("de.laser.properties.${ownerClassName}Property").executeQuery('select prop from '+ownerClassName+'Property prop where prop.owner = :owner and prop.type = :pd and (prop.stringValue != null or prop.intValue != null or prop.decValue != null or prop.refValue != null or prop.urlValue != null or prop.dateValue != null)', [pd: pd, owner: target])
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
						(new GroovyClassLoader()).loadClass("de.laser.properties.${ownerClassName}Property").executeQuery('select prop from '+ownerClassName+'Property prop where prop.owner.instanceOf = :owner and prop.type = :pd and prop.instanceOf = null and (prop.tenant = :ctx or prop.isPublic = true) and (prop.stringValue != null or prop.intValue != null or prop.decValue != null or prop.refValue != null or prop.urlValue != null or prop.dateValue != null)', [pd: pd, ctx: contextOrg, owner: target]).each { AbstractPropertyWithCalculatedLastUpdated childProp ->
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
	 * @param showPriceDate should price columns be displayed?
	 * @param showMetricType should the metric type be included (what seems not to be the case per definition in COUNTER 4)?
	 * @param showOtherData should other data be displayed?
	 * @return an Excel worksheet of the usage report, either according to the COUNTER 4 or COUNTER 5 format
	 */
	Map<String, Object> generateReport(GrailsParameterMap params, Boolean showPriceDate = false, Boolean showMetricType = false, Boolean showOtherData = false) {
		Profiler prf = new Profiler()
		prf.setBenchmark('start export')
		Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
		if(!result)
			return null
		Set<Subscription> refSubs
		Org customer = result.subscription.getSubscriber()
		if (params.statsForSurvey == true) {
			if(params.loadFor == 'allIEsStats')
				refSubs = [result.subscription.instanceOf] //look at statistics of the whole set of titles, i.e. of the consortial parent subscription
			else if(params.loadFor == 'holdingIEsStats')
				refSubs = result.subscription._getCalculatedPrevious() //look at the statistics of the member, i.e. the member's stock of the previous year
		}
		else if(subscriptionService.getCurrentIssueEntitlementIDs(result.subscription).size() > 0){
			refSubs = [result.subscription]
		}
		else refSubs = [result.subscription.instanceOf]
		prf.setBenchmark('get platforms')
		Platform platform = Platform.get(params.platform)
		prf.setBenchmark('get namespaces')
		IdentifierNamespace propIdNamespace = IdentifierNamespace.findByNs(platform.titleNamespace)
		Set<IdentifierNamespace> namespaces = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, TitleInstancePackagePlatform.class.name)] as Set<IdentifierNamespace>
		namespaces.add(propIdNamespace)
		prf.setBenchmark('get date ranges')
		Map<String, Object> dateRangeParams = subscriptionControllerService.getDateRange(params, result.subscription)
		XSSFWorkbook workbook
		Row row
		Cell cell
		SXSSFSheet sheet
        Set<String> metricTypes = params.list('metricType')
		String reportType = params.reportType
		Map<String, Map<String, TitleInstancePackagePlatform>> titles = [:] //structure: namespace -> value -> tipp
		Set<TitleInstancePackagePlatform> titlesSorted = [] //fallback structure to preserve sorting
		prf.setBenchmark('prepare title identifier map')
        if(reportType in Counter4Report.COUNTER_4_TITLE_REPORTS || reportType in Counter5Report.COUNTER_5_TITLE_REPORTS) {
			subscriptionControllerService.fetchTitles(params, refSubs, namespaces, 'ids').each { Map titleMap ->
				//debug only! DO NOT COMMIT!
				//if(titleMap.tipp.id == 862496) {
				titlesSorted << titleMap.tipp
				Map<String, TitleInstancePackagePlatform> innerMap = titles.get(titleMap.namespace)
				if(!innerMap)
					innerMap = [:]
				innerMap.put(titleMap.value, titleMap.tipp)
				titles.put(titleMap.namespace, innerMap)
				//}
			}
        }
		//reportTypes.each { String reportType ->
		Set<String> columnHeaders = []
		SortedSet<String> monthHeaders = new TreeSet<String>()
		Map<Long, Map> titleRows = [:]
		Map<String, Object> sumsPerYOP
		int rowno = 0
		//revision 4
		if(params.revision == AbstractReport.COUNTER_4) {
			prf.setBenchmark('data fetched from provider')
			Map<String, Object> queryParams = [reportType: reportType, customer: customer, platform: platform]
			if(params.metricType) {
				queryParams.metricTypes = params.list('metricType')
			}
			queryParams.startDate = dateRangeParams.startDate
			queryParams.endDate = dateRangeParams.endDate
			//the data
			Map<String, Object> requestResponse = getReports(queryParams)
			if(requestResponse.containsKey('reports')) {
				Set<String> availableMetrics = requestResponse.reports.'**'.findAll { node -> node.name() == 'MetricType' }.collect { node -> node.text()}.toSet()
				workbook = new XSSFWorkbook()
				POIXMLProperties xmlProps = workbook.getProperties()
				POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
				coreProps.setCreator(messageSource.getMessage('laser',null,locale))
				SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
				wb.setCompressTempFiles(true)
				metricTypes.each { String metricType ->
					/*
					Map<String, Object> queryParams = [reportType: reportType, metricType: metricType, customer: customer.globalUID, platforms: subscribedPlatforms.globalUID]
					if(dateRangeParams.dateRange.length() > 0) {
						queryParams.startDate = dateRangeParams.startDate
						queryParams.endDate = dateRangeParams.endDate
					}
					*/
					//the data
					//Set<Counter4Report> counter4Reports = []
					//prf.setBenchmark('get usage data')
					//we do not need to distinguish right here between reports with and without title identifiers any more
					//Counter4Report.withTransaction {
					//counter4Sums.addAll(Counter4Report.executeQuery('select r.reportFrom, r.metricType, sum(r.reportCount) from Counter4Report r where r.reportType = :reportType and r.metricType = :metricType and r.reportInstitutionUID = :customer and r.reportFrom >= :startDate and r.reportTo <= :endDate group by r.reportFrom, r.metricType', queryParams))
					//counter4Reports.addAll(Counter4Report.executeQuery('select r from Counter4Report r where r.reportType = :reportType and r.metricType = :metricType and r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+dateRangeParams.dateRange+'order by r.reportFrom', queryParams))
					//reproduce counter report structure in non-transactional format!

					//}
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
						/*
                        else if(counter4Reports) {
                            cell.setCellValue("${DateUtils.getSDF_yyyyMMdd().format(counter4Reports.first().reportFrom)} to ${DateUtils.getSDF_yyyyMMdd().format(counter4Reports.last().reportTo)}")
                        }
                        */
						headerRow = sheet.createRow(5)
						cell = headerRow.createCell(0)
						cell.setCellValue("Date run:")
						headerRow = sheet.createRow(6)
						cell = headerRow.createCell(0)
						cell.setCellValue(DateUtils.getSDF_yyyyMMdd().format(new Date()))
						columnHeaders.addAll(Counter4Report.COLUMN_HEADERS.valueOf(reportType).headers)
						if(reportType == Counter4Report.JOURNAL_REPORT_5) {
							Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
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
							/*
                            if(dateRangeParams.dateRange.length() == 0) {
                                Calendar month = GregorianCalendar.getInstance(), finalPoint = GregorianCalendar.getInstance()
                                month.setTime(counter4Reports.first().reportFrom)
                                finalPoint.setTime(counter4Reports.last().reportTo)
                                while(month.before(finalPoint)) {
                                    dateRangeParams.monthsInRing << month.getTime()
                                    month.add(Calendar.MONTH, 1)
                                }
                            }
                            */
							if(dateRangeParams.containsKey('startDate') && dateRangeParams.containsKey('endDate')) {
								monthHeaders.addAll(dateRangeParams.monthsInRing.collect { Date month -> DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.localeEN).format(month) })
								columnHeaders.addAll(monthHeaders)
							}
						}
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
								Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
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
								Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
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
								/*
                                Counter4Report.withNewSession {
                                    Counter4Report.executeQuery('select new map(r.reportFrom as reportMonth, r.metricType as metricType, r.platformUID as platformUID, r.publisher as publisher, r.category as category, sum(r.reportCount) as count) from Counter4Report r where r.reportType = :reportType and r.metricType = :metricType and r.reportInstitutionUID = :customer and r.platformUID in (:platforms)' + dateRangeParams.dateRange + 'group by r.platformUID, r.publisher, r.category, r.reportFrom, r.metricType', queryParams)
                                    sumRows.eachWithIndex { sumRow, int i ->
                                        cell = row.createCell(0)
                                        cell.setCellValue(sumRow.category)
                                        cell = row.createCell(2)
                                        cell.setCellValue(Platform.findByGlobalUID(sumRow.platformUID).name)
                                        cell = row.createCell(i+9)
                                        cell.setCellValue(sumRow.count)
                                        totalCount += sumRow.count
                                    }
                                }
                                */
								cell = row.createCell(8)
								cell.setCellValue(totalCount)
								rowno++
								row = sheet.createRow(rowno)
								Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
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
								Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
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
								rowno = 9
								Map<String, Map<String, Object>> data = prepareDataWithDatabases(requestResponse, reportType)
								data.each { String databaseName, Map<String, Object> databaseMetrics ->
									databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
										columnHeaders.eachWithIndex { String colHeader, int c ->
											cell = row.createCell(c)
											cell.setCellValue(metricRow.get(colHeader))
										}
									}
									row = sheet.createRow(rowno)
									rowno++
								}
								break
							case Counter4Report.DATABASE_REPORT_2:
								rowno = 9
								Map<String, Object> data = prepareDataWithDatabases(requestResponse, reportType)
								int totalSum = 0
								/*
                                TODO migrate
                                Counter4Report.withNewSession {
                                    Map<String, Object> categoryRows = [:]
                                    Counter4Report.executeQuery('select new map(r.reportFrom as reportFrom, r.metricType as accessDeniedCategory, sum(r.reportCount) as count) from Counter4Report r where r.reportType = :reportType and r.metricType = :metricType and r.reportInstitutionUID = :customer and r.platformUID in (:platforms)'+dateRangeParams.dateRange+'group by r.metricType, r.reportFrom order by r.metricType, r.reportFrom', queryParams).each { countPerCategory ->
                                        Map<String, Object> categoryRow = categoryRows.get(countPerCategory.accessDeniedCategory)
                                        if(!categoryRow)
                                            categoryRow = ['Reporting Period Total': totalSum]
                                        else totalSum = categoryRow.get('Reporting Period Total')
                                        totalSum += countPerCategory.count
                                        categoryRow.put(DateUtils.getSDF_yyyyMM().format(countPerCategory.reportFrom), countPerCategory.count)
                                        categoryRow.put('Reporting Period Total', totalSum)
                                        categoryRows.put(countPerCategory.accessDeniedCategory, categoryRow)
                                    }
                                    categoryRows.eachWithIndex { String accessDeniedCategory, Map countsPerCategory, int r ->
                                        cell = row.createCell(0)
                                        cell.setCellValue('Total for all databases')
                                        cell = row.createCell(3)
                                        switch(accessDeniedCategory) {
                                            case 'no_license': cell.setCellValue('Access denied: content item not licenced')
                                                break
                                            case 'turnaway': cell.setCellValue('Access denied: concurrent/simultaneous user licence limit exceeded')
                                                break
                                            case 'ft_html': cell.setCellValue('Record Views HTML')
                                                break
                                            case 'ft_total': cell.setCellValue('Record Views Total')
                                                break
                                            default: cell.setCellValue(accessDeniedCategory)
                                                break
                                        }
                                        cell = row.createCell(4)
                                        cell.setCellValue(countsPerCategory.get('Reporting Period Total'))
                                        monthHeaders.eachWithIndex { String month, int i ->
                                            cell = row.createCell(i+5)
                                            if(countsPerCategory.containsKey(month))
                                                cell.setCellValue(countsPerCategory.get(month))
                                            else cell.setCellValue(0)
                                        }
                                        row = sheet.createRow(rowno)
                                        rowno += r
                                    }
                                }
                            */
								row = sheet.createRow(rowno)
								data.each { String databaseName, Map<String, Object> databaseMetrics ->
									databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
										columnHeaders.eachWithIndex { String colHeader, int c ->
											cell = row.createCell(c)
											cell.setCellValue(metricRow.get(colHeader))
										}
									}
									rowno++
									row = sheet.createRow(rowno)
								}
								break
							case Counter4Report.PLATFORM_REPORT_1:
								rowno = 9
								Map<String, Map<String, Object>> data = prepareDataWithDatabases(requestResponse, reportType)
								data.each { String databaseName, Map<String, Object> databaseMetrics ->
									databaseMetrics.each { String databaseMetricType, Map<String, Object> metricRow ->
										columnHeaders.eachWithIndex { String colHeader, int c ->
											cell = row.createCell(c)
											cell.setCellValue(metricRow.get(colHeader))
										}
									}
									row = sheet.createRow(rowno)
									rowno++
								}
								/*
                                TODO migrate
                                Counter4Report.withNewSession {
                                    Counter4Report.executeQuery('select new map(r.reportFrom as reportFrom, r.publisher as publisher, r.metricType as metricType, r.platformUID as platformUID, sum(r.reportCount) as count) from Counter4Report r where r.reportType = :reportType and r.metricType = :metricType and r.reportInstitutionUID = :customer and r.platformUID in (:platforms)'+dateRangeParams.dateRange+'group by r.platformUID, r.publisher, r.reportFrom, r.metricType', queryParams).each { countPerMonth ->
                                        cell = row.createCell(0)
                                        cell.setCellValue(Platform.findByGlobalUID(countPerMonth.platformUID).name)
                                        cell = row.createCell(1)
                                        cell.setCellValue(countPerMonth.publisher ?: '')
                                        cell = row.createCell(2)
                                        switch(countPerMonth.metricType) {
                                            case 'search_reg': cell.setCellValue('Regular Searches')
                                                break
                                            case 'search_fed': cell.setCellValue('Searches-federated and automated')
                                                break
                                            case 'record_view': cell.setCellValue('Record Views')
                                                break
                                            case 'result_click': cell.setCellValue('Result Clicks')
                                                break
                                            default: cell.setCellValue(countPerMonth.metricType)
                                                break
                                        }
                                        monthHeaders.eachWithIndex { String month, int i ->
                                            cell = row.getCell(i+4)
                                            if(!cell) {
                                                cell = row.createCell(i+4)
                                                if(DateUtils.getSDF_yyyyMM().format(countPerMonth.reportFrom) == month)
                                                    cell.setCellValue(countPerMonth.count)
                                                else cell.setCellValue(0)
                                            }
                                            else if(DateUtils.getSDF_yyyyMM().format(countPerMonth.reportFrom) == month)
                                                cell.setCellValue(countPerMonth.count)
                                        }
                                        totalCount += countPerMonth.count
                                    }
                                }
								cell = row.createCell(3)
								cell.setCellValue(totalCount)
								rowno++
								row = sheet.createRow(rowno)
								*/
								break
						}
						int i = 0
						prf.setBenchmark('loop through assembled rows')
						for(TitleInstancePackagePlatform title: titlesSorted) {
							Instant start = Instant.now().truncatedTo(ChronoUnit.MICROS)
							Map metricRow = titleRows.get(title.id)
							if(metricRow) {
								Map titleRow = metricRow.get(metricType)
								if(titleRow) {
									row = sheet.createRow(i+rowno)
									cell = row.createCell(0)
									cell.setCellValue(title.name)
									for(int c = 1; c < columnHeaders.size(); c++) {
										cell = row.createCell(c)
										def empty = ""
										if(columnHeaders[c].matches('\\d{4}-\\d{2}'))
											empty = 0
										cell.setCellValue(titleRow.get(columnHeaders[c]) ?: empty)
									}
									i++
								}
							}
							Duration diff = Duration.between(start, Instant.now().truncatedTo(ChronoUnit.MICROS))
							//log.debug("cell row generated in ${diff} micros")
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
				if(wb.getNumberOfSheets() == 0)
					[error: 'noMetricsAvailable']
				else [result: wb]
			}
			else requestResponse
		}
		//revision 5
		else if(params.revision == AbstractReport.COUNTER_5) {
			Map<String, Object> queryParams = [reportType: reportType, customer: customer, platform: platform]
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
				/*
				desideratum:
				if(reportType in Counter5Report.COUNTER_5_TITLE_REPORTS) {
					Counter5Report.withNewSession {
						//report type should restrict enough; we now need to select appropriate titles in case of an existing identifier
						counter5Reports.addAll(Counter5Report.executeQuery('select r from Counter5Report r where lower(r.reportType) = :reportType'+metricFilter+'and r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+dateRangeParams.dateRange+'order by r.reportFrom', queryParams))
					}
				}
				*/
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
				cell.setCellValue(params.metricType ? params.list('metricType').join(';'): Counter5Report.EXPORT_CONTROLLED_LISTS.valueOf(reportType.toUpperCase()).metricTypes)
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
				/*else {
					Calendar month = GregorianCalendar.getInstance(), finalPoint = GregorianCalendar.getInstance()
					month.setTime(counter5Reports.first().reportFrom)
					finalPoint.setTime(counter5Reports.last().reportTo)
					while(month.before(finalPoint)) {
						columnHeaders << DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(month.getTime())
						month.add(Calendar.MONTH, 1)
					}
				}*/
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
				switch(reportType.toLowerCase()) {
					case Counter5Report.PLATFORM_MASTER_REPORT:
					case Counter5Report.PLATFORM_USAGE:
						Map<String, Object> metricRows = [:]
						for(def reportItem: requestResponse.items) {
							for(Map performance: reportItem.Performance) {
								Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
								for(Map instance: performance.Instance) {
									Map<String, Object> metricRow = metricRows.get(instance.Metric_Type)
									int periodTotal, reportCount = instance.Count as int
									if(!metricRow) {
										metricRow = [:]
										periodTotal = 0
									}
									else periodTotal = metricRow.get('Reporting_Period_Total') as int
									metricRow.put('Platform', reportItem.Platform)
									metricRow.put('Metric_Type', instance.Metric_Type)
									metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
									periodTotal += reportCount
									metricRow.put('Reporting_Period_Total', periodTotal)
									metricRows.put(instance.Metric_Type, metricRow)
								}
							}
							int i = 0
							for (Map.Entry<String, Object> metricRow: metricRows) {
								row = sheet.createRow(i + rowno)
								for (int c = 0; c < columnHeaders.size(); c++) {
									cell = row.createCell(c)
									cell.setCellValue(metricRow.getValue().get(columnHeaders[c]) ?: "")
								}
								i++
							}
						}
						/*Counter5Report.withNewSession {
							Map<String, Object> metricRows = [:]
							Counter5Report.executeQuery('select new map(r.platformUID as platformUID, r.accessMethod as accessMethod, r.dataType as dataType, r.metricType as metricType, r.reportFrom as reportMonth, sum(r.reportCount) as count) from Counter5Report r where lower(r.reportType) = :reportType and r.metricType in (:metricTypes) and r.reportInstitutionUID = :customer and r.platformUID in (:platforms)'+dateRangeParams.dateRange+'group by r.metricType, r.platformUID, r.reportFrom, r.accessMethod, r.dataType order by r.reportFrom',queryParams).each { reportRow ->
								Map<String, Object> metricRow = metricRows.get(reportRow.metricType)
								Integer periodTotal
								if(!metricRow) {
									metricRow = [:]
									periodTotal = 0
								}
								else periodTotal = metricRow.get('Reporting_Period_Total') as Integer
								metricRow.put('Platform', Platform.findByGlobalUID(reportRow.platformUID).name)
								metricRow.put('Metric_Type', reportRow.metricType)
								metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportRow.reportMonth), reportRow.count)
								periodTotal += reportRow.count
								metricRow.put('Reporting_Period_Total', periodTotal)
								metricRow.put('Access_Method', reportRow.accessMethod)
								metricRow.put('Data_Type', reportRow.dataType)
								metricRows.put(reportRow.metricType, metricRow)
							}
							metricRows.eachWithIndex { String metricType, Map<String, Object> metricRow, int i ->
								row = sheet.createRow(i + rowno)
								for (int c = 0; c < columnHeaders.size(); c++) {
									cell = row.createCell(c)
									cell.setCellValue(metricRow.get(columnHeaders[c]) ?: "")
								}
							}
						}*/
						break
					case Counter5Report.DATABASE_MASTER_REPORT:
					case Counter5Report.DATABASE_ACCESS_DENIED:
					case Counter5Report.DATABASE_SEARCH_AND_ITEM_USAGE:
						Map<String, Object> data = prepareDataWithDatabases(requestResponse, reportType)
						int i = 0
						for(Map.Entry<String, Object> databaseRow: data) {
							for(Map.Entry<String, Object> metricRow: databaseRow.getValue()) {
								row = sheet.createRow(i + rowno)
								for(int c = 0;c < columnHeaders.size(); c++) {
									cell = row.createCell(c)
									cell.setCellValue(metricRow.getValue().get(columnHeaders[c]) ?: "")
								}
								i++
							}
						}
						/*Counter5Report.withNewSession {
							Map<String, Object> metricRows = [:]
							Counter5Report.executeQuery('select new map(r.platformUID as platformUID, r.accessMethod as accessMethod, r.metricType as metricType, r.reportFrom as reportMonth, sum(r.reportCount) as count) from Counter5Report r where lower(r.reportType) = :reportType and r.metricType in (:metricTypes) and r.reportInstitutionUID = :customer and r.platformUID in (:platforms)'+dateRangeParams.dateRange+'group by r.metricType, r.platformUID, r.reportFrom, r.accessMethod order by r.reportFrom',queryParams).each { reportRow ->
								Map<String, Object> metricRow = metricRows.get(reportRow.metricType)
								Integer periodTotal
								if(!metricRow) {
									metricRow = [:]
									periodTotal = 0
								}
								else periodTotal = metricRow.get('Reporting_Period_Total') as Integer
								metricRow.put('Platform', Platform.findByGlobalUID(reportRow.platformUID).name)
								metricRow.put('Metric_Type', reportRow.metricType)
								metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportRow.reportMonth), reportRow.count)
								periodTotal += reportRow.count
								metricRow.put('Reporting_Period_Total', periodTotal)
								metricRow.put('Access_Method', reportRow.accessMethod)
								metricRows.put(reportRow.metricType, metricRow)
							}
							metricRows.eachWithIndex { String metricType, Map<String, Object> metricRow, int i ->
								row = sheet.createRow(i + rowno)
								for (int c = 0; c < columnHeaders.size(); c++) {
									cell = row.createCell(c)
									cell.setCellValue(metricRow.get(columnHeaders[c]) ?: "")
								}
							}
						}*/
						break
					default: Map<String, Object> data = prepareDataWithTitles(titles, propIdNamespace, reportType, requestResponse, showPriceDate, showOtherData)
						titleRows = data.titleRows
						break
				}
				if(titleRows.size() > 0) {
					prf.setBenchmark('loop through assembled rows')
					for(TitleInstancePackagePlatform title: titlesSorted) {
						Instant start = Instant.now().truncatedTo(ChronoUnit.MICROS)
						Map titleMetrics = titleRows.get(title.id)
						for(Map.Entry titleMetric: titleMetrics) {
							Map titleAccessTypes = titleMetric.getValue()
							for(Map.Entry titleAccessType: titleAccessTypes) {
								int i = 0
								Map titleYop = titleAccessType.getValue()
								for(Map.Entry entry: titleYop) {
									Map titleRow = entry.getValue()
									row = sheet.createRow(i+rowno)
									cell = row.createCell(0)
									cell.setCellValue(title.name)
									for(int c = 1; c < columnHeaders.size(); c++) {
										cell = row.createCell(c)
										cell.setCellValue(titleRow.get(columnHeaders[c]) ?: "")
									}
									i++
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
				/*
				titleRows.each{ TitleInstancePackagePlatform title, Map<String, Map<String, Map>> titleMetric ->
					titleMetric.each { String metricType, Map titleAccessType ->
						titleAccessType.eachWithIndex { String accessType, Map titleRow, int i ->
							row = sheet.createRow(i+rowno)
							cell = row.createCell(0)
							cell.setCellValue(title.name)
							for(int c = 1; c < columnHeaders.size(); c++) {
								cell = row.createCell(c)
								cell.setCellValue(titleRow.get(columnHeaders[c]) ?: "")
							}
						}
						rowno += titleAccessType.size()
					}
				}
				*/
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
	 * @param propIdNamespace the proprietary namespace of the provider
	 * @param reportType the report type which is about to be exported
	 * @param showPriceDate should the list price shown?
	 * @param showMetricType should the metric types be included in the report?
	 * @param showOtherData should other data being shown as well?
	 * @param metricType (for COUNTER 4 exports only) the metric typ which should be exported
	 * @return a map of titles with the row containing the columns as specified for the given report
	 */
	Map<String, Object> prepareDataWithTitles(Map<String, Map<String, TitleInstancePackagePlatform>> titles, IdentifierNamespace propIdNamespace, String reportType, requestResponse, Boolean showPriceDate = false, Boolean showOtherData = false) {
		Map<Long, Map<String, Map>> titleRows = [:]
		Map<String, Object> result = [:]
		Calendar limit = GregorianCalendar.getInstance()
		limit.set(2000, 0, 1)
		if(reportType in Counter5Report.COUNTER_5_REPORTS) {
			Map<Date, Integer> countsPerMonth = [:]
			for(def reportItem: requestResponse.items) {
				Map<String, String> identifierMap = buildIdentifierMap(reportItem, AbstractReport.COUNTER_5)
				TitleInstancePackagePlatform tipp = subscriptionControllerService.matchReport(titles, propIdNamespace, identifierMap)
				if(tipp) {
					Set<Identifier> titleIDs = tipp.ids
					Set<PriceItem> priceItems = []
					if(showPriceDate)
						priceItems.addAll(tipp.priceItems)
					String isbn = identifierMap.isbn ?: identifierMap.printIdentifier
					String eisbn = identifierMap.onlineIdentifier
					String issn = identifierMap.printIdentifier
					String eissn = identifierMap.onlineIdentifier
					String doi = identifierMap.doi
					for(Map performance : reportItem.Performance) {
						Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
						for(Map instance : performance.Instance) {
							int periodTotal = 0
							String yopKey = reportItem.containsKey('YOP') && reportItem.get('YOP') != 'null' && reportItem.get('YOP') != null ? reportItem.get('YOP') : 'empty'
							Map<String, Map<String, Object>> titleMetrics = titleRows.get(tipp.id)
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
							titleRow.put("Proprietary_ID", titleIDs.find { Identifier id -> id.ns == propIdNamespace }?.value)
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
							if (showPriceDate && priceItems) {
								//listprice_eur
								titleRow.put("List Price EUR", priceItems.find { it.listCurrency == RDStore.CURRENCY_EUR }?.listPrice ?: ' ')
								//listprice_gbp
								titleRow.put("List Price GBP", priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }?.listPrice ?: ' ')
								//listprice_usd
								titleRow.put("List Price USD", priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice ?: ' ')
							}
							if (showOtherData) {
								titleRow.put("Year First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyy().format( tipp.dateFirstOnline ): ' ')
								titleRow.put("Date First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyyMMdd().format( tipp.dateFirstOnline ): ' ')
							}
							periodTotal += reportCount
							titleRow.put("Reporting_Period_Total", periodTotal)
							titleRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
							titlesByYop.put(yopKey, titleRow)
							titlesByAccessType.put(reportItem.Access_Type, titlesByYop)
							titleMetrics.put(instance.Metric_Type, titlesByAccessType)
							titleRows.put(tipp.id, titleMetrics)
						}
					}
				}
			}
			result.sumRows =  countsPerMonth
		}
        else if(reportType in Counter4Report.COUNTER_4_REPORTS) {
			Map<String, Map<Date, Integer>> countsPerMonth = [:], sumsPerYOP = [:]
			Set<String> metricsAvailable = []
			for (GPathResult reportItem: requestResponse.reports) {
				Map<String, String> identifierMap = buildIdentifierMap(reportItem, AbstractReport.COUNTER_4)
				TitleInstancePackagePlatform tipp = subscriptionControllerService.matchReport(titles, propIdNamespace, identifierMap)
				if(tipp) {
					Set<PriceItem> priceItems = []
					if(showPriceDate)
						priceItems.addAll(tipp.priceItems)
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
							Map<String, Map<String, Object>> titleMetrics = titleRows.get(tipp.id)
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
								titleRow.put(DateUtils.getSDF_yyyyMM().format(reportFrom), count)
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
							titleRows.put(tipp.id, titleMetrics)
						}
					}
				}
			}
			result.metricsAvailable = metricsAvailable
			result.sumRows = countsPerMonth
			result.sumsPerYOP = sumsPerYOP
		}
		/*
            Map<String, Object> titleRow = titleRows.get(tipp)
            if(!titleRow) {
                String proprietaryIdentifier = report.proprietaryIdentifier
                titleRow = [:]
                //key naming identical to column headers
                titleRow.put("Publisher", report.publisher)
                titleRow.put("Platform", report.platform.name)
                titleRow.put("Proprietary Identifier", proprietaryIdentifier)
                titleRow.put("Journal DOI", doi)
                titleRow.put("Book DOI", doi)
                if(reportType == Counter4Report.JOURNAL_REPORT_5) {
                    titleRow.put("Print ISSN", issn)
                    titleRow.put("Online ISSN", eissn)
                }
                else {
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
                    titleRow.put("Year First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyy().format(tipp.dateFirstOnline): ' ')
                    titleRow.put("Date First Online", tipp.dateFirstOnline ? DateUtils.getSDF_yyyyMMdd().format(tipp.dateFirstOnline): ' ')
                }
            }
            else {
                if(reportType == Counter4Report.JOURNAL_REPORT_1) {
                    periodHTML = titleRow.containsKey("Reporting Period HTML") ? titleRow.get("Reporting Period HTML") as int : 0
                    periodPDF = titleRow.containsKey("Reporting Period PDF") ? titleRow.get("Reporting Period PDF") as int : 0
                }
                if(reportType != Counter4Report.JOURNAL_REPORT_5) {
                    periodTotal = titleRow.get("Reporting Period Total") as int
                }
            }
            periodTotal += report.reportCount
            if(reportType != Counter4Report.JOURNAL_REPORT_5) {
                titleRow.put("Reporting Period Total", periodTotal)
                titleRow.put(DateUtils.getSDF_yyyyMM().format(report.reportFrom), report.reportCount)
            }
            switch(reportType) {
                case Counter4Report.BOOK_REPORT_5: titleRow.put("User activity", report.metricType == 'search_reg' ? "Regular Searches" : "Searches: federated and automated")
                    break
                case Counter4Report.JOURNAL_REPORT_1:
                    if(report.metricType == 'ft_html') {
                        periodHTML += report.reportCount
                        titleRow.put("Reporting Period HTML", periodHTML)
                    }
                    else if(report.metricType == 'ft_pdf') {
                        periodPDF += report.reportCount
                        titleRow.put("Reporting Period PDF", periodPDF)
                    }
                    break
                case Counter4Report.JOURNAL_REPORT_5:
                    String key
                    if(report.yop < limit.getTime())
                        key = "YOP Pre-2000"
                    else if(report.yop == null)
                        key = "YOP unknown"
                    else
                        key = "YOP ${DateUtils.getSDF_yyyy().format(report.yop)}"
                    Integer currVal = titleRow.get(key)
                    if(!currVal)
                        currVal = 0
                    currVal += report.reportCount
                    titleRow.put(key, currVal)
                    Integer countPerYOP = sumsPerYOP.get(key)
                    if(!countPerYOP) {
                        countPerYOP = 0
                    }
                    countPerYOP += report.reportCount
                    sumsPerYOP.put(key, countPerYOP)
                    break
            }
            titleRows.put(tipp, titleRow)*/
		//Duration iterInner = Duration.between(innerStart, Instant.now().truncatedTo(ChronoUnit.MICROS))
		//log.debug("iteration time inner loop: ${iterInner}")

		//}
		result.titleRows = titleRows
		result
	}

	Map<String, Object> prepareDataWithDatabases(Map requestResponse, String reportType) {
		/*
		generates structure
		[
			databaseName: {
				metric1: [date1: count, date2: count ... dateN: count]
				metric2: [date1: count, date2: count ... dateN: count]
				...
				metricN: [date1: count, date2: count ... dateN: count]
			}
		]
		 */
		Map<String, Object> databaseRows = [:]
		//mini example: https://connect.liblynx.com/sushi/r5/reports/dr?customer_id=2246867&requestor_id=facd706b-cf11-42f9-8d92-a874e594a218&begin_date=2023-01&end_date=2023-12
		//take https://laser-dev.hbz-nrw.de/subscription/membersSubscriptionsManagement/59727?tab=customerIdentifiers&isSiteReloaded=false as base for customer identifiers!
		if(reportType in Counter5Report.COUNTER_5_REPORTS) {
			for(def reportItem: requestResponse.items) {
				for(Map performance: reportItem.Performance) {
					Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
					for(Map instance: performance.Instance) {
						int periodTotal = 0, reportCount = instance.Count as int
						Map<String, Object> databaseRow = databaseRows.get(reportItem.Database) as Map<String, Object>
						if(!databaseRow)
							databaseRow = [:]
						Map<String, Object> metricRow = databaseRow.get(instance.Metric_Type) as Map<String, Object>
						databaseRow.put(instance.Metric_Type, metricRow)
						databaseRows.put(reportItem.Database, databaseRow)
						if(!metricRow) {
							metricRow = ['Database': reportItem.Database, 'Publisher': reportItem.Publisher, 'Platform': reportItem.Platform, 'Metric_Type': instance.Metric_Type]
						}
						else periodTotal = metricRow.get('Reporting_Period_Total')
						periodTotal += reportCount
						metricRow.put('Reporting_Period_Total', periodTotal)
						metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.getLocaleEN()).format(reportFrom), reportCount)
						databaseRow.put(instance.Metric_Type, metricRow)
						databaseRows.put(reportItem.Database, databaseRow)
					}
				}
			}
		}
		else if(reportType in Counter4Report.COUNTER_4_REPORTS) {
			for (GPathResult reportItem: requestResponse.reports) {
				String databaseName = reportItem.'ns2:ItemName'.text()
				for (GPathResult performance: reportItem.'ns2:ItemPerformance') {
					Date reportMonth = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
					for (GPathResult instance: performance.'ns2:Instance') {
						String metricType = instance.'ns2:MetricType'.text()
						int periodTotal = 0, count = Integer.parseInt(instance.'ns2:Count'.text())
						Map<String, Object> databaseRow = databaseRows.get(databaseName) as Map<String, Object>
						if(!databaseRow)
							databaseRow = [:]
						Map<String, Object> metricRow = databaseRow.get(metricType) as Map<String, Object>
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
							metricRow = ['Database': databaseName, 'Publisher': reportItem.'ns2:ItemPublisher'.text(), 'Platform': reportItem.'ns2:ItemPlatform'.text()]
						}
						else periodTotal = metricRow.get('Reporting Period Total')
						if(reportType == Counter4Report.DATABASE_REPORT_2)
							metricRow.put('Access denied category', metricHumanReadable)
						else
							metricRow.put('User Activity', metricHumanReadable)
						periodTotal += count
						metricRow.put('Reporting Period Total', periodTotal)
						metricRow.put(DateUtils.getLocalizedSDF_MMMyyyy(LocaleUtils.localeEN).format(reportMonth), count)
						databaseRow.put(metricType, metricRow)
						databaseRows.put(databaseName, databaseRow)
					}
				}
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
		databaseRows
	}

	Map<String, String> prepareSushiCall(Map platformRecord) {
		String revision = null, statsUrl = null
		if(platformRecord.counterR5SushiApiSupported == "Yes") {
			revision = 'counter5'
			statsUrl = platformRecord.counterR5SushiServerUrl
			if (!platformRecord.counterR5SushiServerUrl.contains('reports')) {
				if (platformRecord.counterR5SushiServerUrl.endsWith('/'))
					statsUrl = platformRecord.counterR5SushiServerUrl + 'reports'
				else statsUrl = platformRecord.counterR5SushiServerUrl + '/reports'
			}
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
		[revision: revision, statsUrl: statsUrl]
	}

	Map<String, Object> getReports(Map configMap) {
		Map<String, Object> result = [:]
		ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
		SimpleDateFormat monthFormatter = DateUtils.getSDF_yyyyMM()
		Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/sushiSources?uuid=${configMap.platform.gokbId}")
		Map platformRecord
		if (queryResult.warning) {
			List records = queryResult.warning.records
			if(records[0]) {
				platformRecord = records[0]
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
					result.putAll(statsSyncService.fetchXMLData(statsSource.statsUrl, requestBody))
				}
				else if(statsSource.revision == 'counter5') {
					String url = statsSource.statsUrl + "/${configMap.reportType}"
					url += "?customer_id=${customerId.value}&requestor_id=${customerId.requestorKey}"
					url += configMap.platform.centralApiKey ? "&api_key=${configMap.platform.centralApiKey}" : "&api_key=${customerId.requestorKey}"
					url += configMap.metricTypes ? "&metric_type=${configMap.metricTypes}" : ""
					url += configMap.accessTypes ? "&access_type=${configMap.accessTypes}" : ""
					url += configMap.accessMethods ? "&access_method=${configMap.accessMethods}" : ""
					url += "&begin_date=${monthFormatter.format(configMap.startDate)}&end_date=${monthFormatter.format(configMap.endDate)}"
					result.putAll(statsSyncService.fetchJSONData(url))
				}
			}
		}
		else
			result.error = 'no we:kb platform available'
		result
	}

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
		identifierMap
	}

	/**
	 * Make a XLSX export of cost item results
	 * @param result the loaded financial data
	 * @return a Excel worksheet of the given cost data
	 */
	SXSSFWorkbook processFinancialXLSX(Map<String,Object> result) {
		Locale locale = LocaleUtils.getCurrentLocale()
		SimpleDateFormat dateFormat = DateUtils.getLocalizedSDF_noTime()
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
			sheet.trackAllColumnsForAutoSizing()
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
						   messageSource.getMessage('default.status.label',null,locale), messageSource.getMessage('default.currency.label',null,locale), messageSource.getMessage('financials.costInBillingCurrency',null,locale),"EUR",
						   messageSource.getMessage('financials.costInLocalCurrency',null,locale)])
			if(["own","cons"].indexOf(viewMode) > -1)
				titles.addAll([messageSource.getMessage('financials.taxRate',null,locale), messageSource.getMessage('default.currency.label',null,locale),messageSource.getMessage('financials.costInBillingCurrencyAfterTax',null,locale),"EUR",messageSource.getMessage('financials.costInLocalCurrencyAfterTax',null,locale)])
			titles.addAll([messageSource.getMessage('financials.costItemElement',null,locale), messageSource.getMessage('default.description.label',null,locale),
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
			if(cit.getValue()?.count > 0) {
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
		Map<String, Object> data = getTitleData(configMap+[format: 'kbart'], entitlementInstance, sql)
        titleHeaders.addAll(data.otherTitleIdentifierNamespaces.idns_ns)
		data.titles.eachWithIndex { GroovyRowResult title, int outer ->
			if(entitlementInstance == IssueEntitlement.class.name && data.coverageMap.get(title['ie_id'])) {
				data.coverageMap.get(title['ie_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					log.debug "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('kbart', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces))
				}
			}
			else if(entitlementInstance == TitleInstancePackagePlatform.class.name && data.coverageMap.get(title['tipp_id'])) {
				data.coverageMap.get(title['tipp_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					log.debug "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('kbart', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces))
				}
			}
			else {
				log.debug "now processing record ${outer}"
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
					row.add(tipp.firstAuthor ? tipp.firstAuthor.replaceAll(';', ',') : ' ')
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
						row.add(tipp.firstEditor ? tipp.firstEditor.replaceAll(';', ',') : ' ')
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
					DecimalFormat df = new DecimalFormat("###,##0.00")
					df.decimalFormatSymbols = new DecimalFormatSymbols(LocaleUtils.getCurrentLocale())
					if(entitlement?.priceItems) {
						//listprice_eur
						BigDecimal listEUR = entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_EUR}?.listPrice
						row.add(listEUR ? df.format(listEUR) : ' ')
						//listprice_gbp
						BigDecimal listGBP = entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_GBP}?.listPrice
						row.add(listGBP ? df.format(listGBP) : ' ')
						//listprice_usd
						BigDecimal listUSD = entitlement.priceItems.find {it.listCurrency == RDStore.CURRENCY_USD}?.listPrice
						row.add(listUSD ? df.format(listUSD) : ' ')
						//localprice_eur
						BigDecimal localEUR = entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_EUR}?.localPrice
						row.add(localEUR ? df.format(localEUR) : ' ')
						//localprice_gbp
						BigDecimal localGBP = entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_GBP}?.localPrice
						row.add(localGBP ? df.format(localGBP) : ' ')
						//localprice_usd
						BigDecimal localUSD = entitlement.priceItems.find {it.localCurrency == RDStore.CURRENCY_USD}?.localPrice
						row.add(localUSD ? df.format(localUSD) : ' ')
					} else if (tipp.priceItems) {
						//listprice_eur
						BigDecimal listEUR = tipp.priceItems.find {it.listCurrency == RDStore.CURRENCY_EUR}?.listPrice
						row.add(listEUR ? df.format(listEUR) : ' ')
						//listprice_gbp
						BigDecimal listGBP = tipp.priceItems.find {it.listCurrency == RDStore.CURRENCY_GBP}?.listPrice
						row.add(listGBP ? df.format(listGBP) : ' ')
						//listprice_usd
						BigDecimal listUSD = tipp.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }?.listPrice
						row.add(listUSD ? df.format(listUSD) : ' ')
						//localprice_eur
						BigDecimal localEUR = tipp.priceItems.find {it.localCurrency == RDStore.CURRENCY_EUR}?.localPrice
						row.add(localEUR ? df.format(localEUR) : ' ')
						//localprice_gbp
						BigDecimal localGBP = tipp.priceItems.find {it.localCurrency == RDStore.CURRENCY_GBP}?.localPrice
						row.add(localGBP ? df.format(localGBP) : ' ')
						//localprice_usd
						BigDecimal localUSD = tipp.priceItems.find { it.localCurrency == RDStore.CURRENCY_USD }?.localPrice
						row.add(localUSD ? df.format(localUSD) : ' ')
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
				log.debug("flush after ${offset} ...")
				sess.flush()
				sess.clear()
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
		Locale locale = LocaleUtils.getCurrentLocale()
		Map<String, Object> data = getTitleData(configMap, entitlementInstance, sql, showStatsInMonthRings, subscriber)
		List<String> titleHeaders = [
				messageSource.getMessage('tipp.name',null,locale),
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
				messageSource.getMessage('tipp.listprice_usd',null,locale),
				messageSource.getMessage('tipp.localprice_eur',null,locale),
				messageSource.getMessage('tipp.localprice_gbp',null,locale),
				messageSource.getMessage('tipp.localprice_usd',null,locale)]
		titleHeaders.addAll(data.coreTitleIdentifierNamespaces.collect { GroovyRowResult row -> row['idns_ns']})
		titleHeaders.addAll(data.otherTitleIdentifierNamespaces.collect { GroovyRowResult row -> row['idns_ns']})
		if(showStatsInMonthRings){
			titleHeaders.addAll(showStatsInMonthRings.collect { Date month -> DateUtils.getSDF_yyyyMM().format(month) })
		}
		List rows = []
		Map<String,List> export = [titles:titleHeaders]
		data.titles.eachWithIndex { GroovyRowResult title, int outer ->
			if(entitlementInstance == IssueEntitlement.class.name && data.coverageMap.get(title['ie_id'])) {
				data.coverageMap.get(title['ie_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					log.debug "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('excel', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces, perpetuallyPurchasedTitleURLs, showStatsInMonthRings, subscriber))
				}
			}
			else if(entitlementInstance == TitleInstancePackagePlatform.class.name && data.coverageMap.get(title['tipp_id'])) {
				data.coverageMap.get(title['tipp_id']).eachWithIndex { GroovyRowResult covStmt, int inner ->
					log.debug "now processing coverage statement ${inner} for record ${outer}"
					covStmt.putAll(title)
					rows.add(buildRow('excel', covStmt, data.identifierMap, data.priceItemMap, data.reportMap, data.coreTitleIdentifierNamespaces, data.otherTitleIdentifierNamespaces, perpetuallyPurchasedTitleURLs, showStatsInMonthRings, subscriber))
				}
			}
			else {
				log.debug "now processing record ${outer}"
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
		 'platform_name',
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
	List buildRow(String format, GroovyRowResult titleRecord, Map identifierMap, Map priceItemMap, Map reports, List<GroovyRowResult> coreTitleIdentifierNamespaces, List<GroovyRowResult> otherTitleIdentifierNamespaces, Collection perpetuallyPurchasedTitleURLs = [], List showStatsInMonthRings = [], Org subscriber = null) {
		titleRecord.identifiers = identifierMap.get(titleRecord['tipp_id'])
		if(titleRecord.containsKey('ie_id')) {
			titleRecord.priceItems = priceItemMap.get(titleRecord['ie_id'])
		}
		else {
			titleRecord.priceItems = priceItemMap.get(titleRecord['tipp_id'])
		}
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
		row.add(createCell(format, titleRecord['tipp_host_platform_url'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_first_author'] ?: '', style))
		row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get(titleRecord['tipp_plat_namespace']), ','), style))
		//embargo_information
		row.add(createCell(format, titleRecord.containsKey('embargo') && titleRecord.embargo ?: ' ', style))
		//coverage_depth
		row.add(createCell(format, titleRecord.containsKey('coverageDepth') && titleRecord.coverageDepth ?: ' ', style))
		//notes
		row.add(createCell(format, titleRecord.containsKey('coverageNote') && titleRecord.coverageNote ?: ' ', style))
		row.add(createCell(format, titleRecord['title_type'], style))
		row.add(createCell(format, titleRecord['tipp_publisher_name'] ? titleRecord['tipp_publisher_name'] : '', style))
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
		row.add(createCell(format, titleRecord['tipp_first_editor'] ?: '', style))
		row.add(createCell(format, '', style))
		row.add(createCell(format, '', style))
		row.add(createCell(format, titleRecord['tipp_pkg_name'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_plat_name'] ?: '', style))
		row.add(createCell(format, titleRecord['tipp_last_updated'] ? formatter.format(titleRecord['tipp_last_updated']) : '', style))
		row.add(createCell(format, titleRecord['accessStartDate'] ? formatter.format(titleRecord['accessStartDate']) : '', style))
		row.add(createCell(format, titleRecord['accessEndDate'] ? formatter.format(titleRecord['accessEndDate']) : '', style))
		row.add(createCell(format, titleRecord['tipp_medium'] ? titleRecord['tipp_medium'] : '', style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('zdb'), ','), style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('doi'), ','), style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('ezb'), ','), style))
        row.add(createCell(format, titleRecord['tipp_gokb_id'] ?: '', style))
        row.add(createCell(format, titleRecord['tipp_pkg_uuid'] ?: '', style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('ISCI'), ','), style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('package_isil'), ','), style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('ezb_anchor'), ','), style))
        row.add(createCell(format, '', style))
        row.add(createCell(format, '', style))
        row.add(createCell(format, titleRecord['tipp_series_name'] ?: ' ', style))
        row.add(createCell(format, titleRecord['tipp_subject_reference'] ?: ' ', style))
        row.add(createCell(format, titleRecord['status'] ?: ' ', style))
		row.add(createCell(format, titleRecord['accessType'] ?: ' ', style))
		row.add(createCell(format, titleRecord['openAccess'] ?: ' ', style))
        row.add(createCell(format, joinIdentifiersSQL(titleRecord.identifiers.get('zdb_ppn'), ','), style))
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
			Map<String, Integer> usageRow = reports.get(titleRecord['tipp_id'])
			Calendar filterTime = GregorianCalendar.getInstance()
			showStatsInMonthRings.each { Date month ->
				if(usageRow) {
					filterTime.setTime(month)
					//println(counterReport)
					//println(counterReport.reportCount ?: '')
					row.add(createCell(format, usageRow.get(DateUtils.getSDF_yyyyMM().format(month)) ?: ' ', style))
				}
				else row.add(createCell(format, ' ', style))
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
		Map<Long, Map<String, Integer>> reportMap = [:]
		Map<Long, List<GroovyRowResult>> coverageMap = [:], priceItemMap = [:]
		Map<Long, Map<String, List<String>>> identifierMap = [:]
		Map<String, Map<String, Long>> identifierInverseMap = [:]
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
			identifiers = sql.rows("select id_tipp_fk, id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id join issue_entitlement on ie_tipp_fk = id_tipp_fk ${queryData.subJoin} join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			coverages = sql.rows("select ic_ie_fk, ic_start_date as startDate, ic_start_volume as startVolume, ic_start_issue as startIssue, ic_end_date as endDate, ic_end_volume as endVolume, ic_end_issue as endIssue, ic_coverage_note as coverageNote, ic_coverage_depth as coverageDepth, ic_embargo as embargo from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id ${queryData.subJoin} join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			priceItems = sql.rows("select pi_ie_fk, pi_list_price, (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk) as pi_list_currency, pi_local_price, (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk) as pi_local_currency from price_item join issue_entitlement on pi_ie_fk = ie_id ${queryData.subJoin} join title_instance_package_platform on ie_tipp_fk = tipp_id where ${queryData.where}", queryData.params)
			coreTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on ie_tipp_fk = tipp_id ${queryData.subJoin} where idns_ns in ('${coreTitleNSrestricted.join("','")}') and ${queryData.where}", queryData.params)
			otherTitleIdentifierNamespaces = sql.rows("select distinct idns_ns from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on ie_tipp_fk = tipp_id ${queryData.subJoin} where idns_ns not in ('${IdentifierNamespace.CORE_TITLE_NS.join("','")}') and ${queryData.where}", queryData.params)
			identifierMap.putAll(preprocessIdentifierRows(identifiers))
			coverageMap.putAll(preprocessRows(coverages, 'ic_ie_fk'))
			priceItemMap.putAll(preprocessPriceItemRows(priceItems, 'pi_ie_fk'))
			if(showStatsInMonthRings && subscriber) {
				List<GroovyRowResult> platformData = sql.rows("select plat_title_namespace, plat_guid from platform join title_instance_package_platform on plat_id = tipp_plat_fk join issue_entitlement on ie_tipp_fk = tipp_id ${queryData.subJoin} where ${queryData.where} group by plat_guid, plat_title_namespace", queryData.params)
				List<Object> platforms = []
				Set<String> propIdNamespaces = []
				platformData.each { GroovyRowResult row ->
					platforms << row.plat_guid
					propIdNamespaces << row.plat_title_namespace
				}
				identifiers.each { GroovyRowResult idRow ->
					Map<String, Long> innerMap = identifierInverseMap.containsKey(idRow.idns_ns) ? identifierInverseMap.get(idRow.idns_ns) : [:]
					innerMap.put(idRow.id_value, idRow.id_tipp_fk)
					identifierInverseMap.put(idRow.idns_ns, innerMap)
				}
				Calendar filterTime = GregorianCalendar.getInstance()
				filterTime.setTime(showStatsInMonthRings.first())
				filterTime.set(Calendar.DATE, filterTime.getActualMinimum(Calendar.DAY_OF_MONTH))
				Date startDate = filterTime.getTime()
				filterTime.setTime(showStatsInMonthRings.last())
				filterTime.set(Calendar.DATE, filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
				Date endDate = filterTime.getTime()
				configMap.customer = subscriber
				configMap.startDate = startDate
				configMap.endDate = endDate
				Map<String, Object> requestResponse = getReports(configMap)
				//implicit COUNTER 4 check
				Long titleMatch
				if(requestResponse.containsKey('reports')) {
					//COUNTER 4 result
					for (GPathResult reportItem: requestResponse.reports) {
						titleMatch = null
						reportItem.'ns2:ItemIdentifier'.each { identifier ->
							if(!titleMatch) {
								switch (identifier.'ns2:Type'.text().toLowerCase()) {
									case 'isbn': titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(identifier.'ns2:Value'.text().replaceAll('-',''))
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(identifier.'ns2:Value'.text().replaceAll('-',''))
										break
									case 'online_isbn':
									case 'online_issn': titleMatch = identifierInverseMap[IdentifierNamespace.EISSN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.EISSN]?.get(identifier.'ns2:Value'.text().replaceAll('-',''))
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(identifier.'ns2:Value'.text().replaceAll('-',''))
										break
									case 'print_issn':
									case 'print_isbn': titleMatch = identifierInverseMap[IdentifierNamespace.ISSN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(identifier.'ns2:Value'.text())
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(identifier.'ns2:Value'.text().replaceAll('-',''))
										break
									case 'doi': titleMatch = identifierInverseMap[IdentifierNamespace.DOI]?.get(identifier.'ns2:Value'.text())
										break
									case 'proprietary_id': propIdNamespaces.each { String propIdNamespace ->
										if(!titleMatch)
											titleMatch = identifierInverseMap[propIdNamespace]?.get(identifier.'ns2:Value'.text())
									}
										break
								}
							}
						}
						if(titleMatch) {
							Map<String, Integer> titlePerformance = reportMap.containsKey(titleMatch) ? reportMap.get(titleMatch) : [:]
							for(GPathResult performance: reportItem.'ns2:ItemPerformance') {
								Date reportFrom = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
								for(GPathResult instance: performance.'ns2:Instance'.findAll { instCand -> instCand.'ns2:MetricType'.text() == configMap.metricTypes }) {
									titlePerformance.put(DateUtils.getSDF_yyyyMM().format(reportFrom), Integer.parseInt(instance.'ns2:Count'.text()))
								}
							}
							reportMap.put(titleMatch, titlePerformance)
						}
					}
				}
				else if(requestResponse.containsKey('items')) {
					//COUNTER 5 result
					for(def reportItem: requestResponse.items) {
						titleMatch = null
						reportItem["Item_ID"].each { idData ->
							if(!titleMatch) {
								switch(idData.Type.toLowerCase()) {
									case 'isbn': titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(idData.Value.replaceAll('-',''))
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(idData.Value.replaceAll('-',''))
										break
									case 'online_issn':
									case 'online_isbn': titleMatch = identifierInverseMap[IdentifierNamespace.EISSN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.EISSN]?.get(idData.Value.replaceAll('-',''))
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.ISBN]?.get(idData.Value.replaceAll('-',''))
										break
									case 'print_isbn':
									case 'print_issn': titleMatch = identifierInverseMap[IdentifierNamespace.ISSN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(idData.Value)
										if(!titleMatch)
											titleMatch = identifierInverseMap[IdentifierNamespace.PISBN]?.get(idData.Value.replaceAll('-',''))
										break
									case 'doi': titleMatch = identifierInverseMap[IdentifierNamespace.DOI]?.get(idData.Value)
										break
									case 'proprietary_id': propIdNamespaces.each { String propIdNamespace ->
										if(!titleMatch)
											titleMatch = identifierInverseMap[propIdNamespace]?.get(idData.Value)
									}
										break
								}
							}
						}
						if(titleMatch) {
							Map<String, Integer> titlePerformance = reportMap.containsKey(titleMatch) ? reportMap.get(titleMatch) : [:]
							for(Map performance: reportItem.Performance) {
								Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
								for(Map instance: performance.Instance) {
									titlePerformance.put(DateUtils.getSDF_yyyyMM().format(reportFrom), instance.Count)
								}
							}
							reportMap.put(titleMatch, titlePerformance)
						}
					}
				}
				/*
				showStatsInMonthRings.each { Date month ->
					queriedMonths << "max(case when to_char(c5r_report_from, 'MM') = '${DateUtils.getSDF_MM().format(month)}' then c5r_report_count else 0 end) as \"${DateUtils.getSDF_yyyyMM().format(month)}\""
				}
				reports = storageSql.rows('select c5r_online_identifier as online_identifier, c5r_print_identifier as print_identifier, c5r_doi as doi, c5r_isbn as isbn, c5r_proprietary_identifier as proprietary_identifier, ' +
						queriedMonths.join(',')+
						' from counter5report where c5r_report_institution_guid = :customer and lower(c5r_report_type) = :defaultReport and c5r_metric_type = :defaultMetric and c5r_platform_guid = any(:platforms) and c5r_report_from >= :startDate and c5r_report_to <= :endDate ' +
						'group by c5r_online_identifier, c5r_print_identifier, c5r_doi, c5r_isbn, c5r_proprietary_identifier',
						[customer: subscriber.globalUID, platforms: connection.createArrayOf('varchar', platforms.toArray()), startDate: startDate, endDate: endDate, defaultReport: Counter5Report.TITLE_MASTER_REPORT, defaultMetric: 'Unique_Title_Requests'])
				if(!reports) {
					List<Object> defaultReports = [Counter4Report.BOOK_REPORT_1, Counter4Report.JOURNAL_REPORT_1]
					queriedMonths.clear()
					showStatsInMonthRings.each { Date month ->
						queriedMonths << "max(case when to_char(c4r_report_from, 'MM') = '${DateUtils.getSDF_MM().format(month)}' then c4r_report_count else 0 end) as \"${DateUtils.getSDF_yyyyMM().format(month)}\""
					}
					reports = storageSql.rows('select c4r_online_identifier as online_identifier, c4r_print_identifier as print_identifier, c4r_doi as doi, c4r_isbn as isbn, c4r_proprietary_identifier as proprietary_identifier, ' +
							queriedMonths.join(',')+
							' from counter4report where c4r_report_institution_guid = :customer and c4r_report_type = any(:defaultReports) and c4r_metric_type = :defaultMetric and c4r_platform_guid = any(:platforms) and c4r_report_from >= :startDate and c4r_report_to <= :endDate ' +
							'group by c4r_online_identifier, c4r_print_identifier, c4r_doi, c4r_isbn, c4r_proprietary_identifier',
							[customer: subscriber.globalUID, platforms: connection.createArrayOf('varchar', platforms.toArray()), startDate: startDate, endDate: endDate, defaultReports: connection.createArrayOf('varchar', defaultReports.toArray()), defaultMetric: 'ft_total'])
				}
				*/
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
			if(whereTo) {
				List subResult = IdentifierNamespace.executeQuery('select distinct(ns) from Identifier id join id.ns ns where ' + whereTo + ' and ns.ns not in (:coreTitleNS)', [titleInstances: entitlementChunk, coreTitleNS: IdentifierNamespace.CORE_TITLE_NS])
				result.addAll(subResult)
			}
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
