package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import de.laser.domain.AbstractCoverage
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.TIPPCoverage
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.awt.*
import java.math.RoundingMode
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
class ExportService {

	SimpleDateFormat formatter = DateUtil.getSDF_ymd()
	MessageSource messageSource
	Locale locale = LocaleContextHolder.getLocale()

	/**
		new CSV/TSV export interface - should subsequently replace StreamOutLicenseCSV, StreamOutSubsCSV and StreamOutTitlesCSV
		expect data in structure:
		@param titleRow - {@link Collection} of column headers [header1,header2,...,headerN]
		@param columnData - {@link Collection} of the rows, each row is itself a {@link Collection}:
	 	[
		 	[column1, column2, ..., columnN], //for row 1
		 	[column1, column2, ..., columnN], //for row 2
		 	...
		 	[column1, column2, ..., columnN]  //for row N
		]
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
		new XSLX export interface - should subsequently collect the Excel export points
		expect data in structure:
		 [sheet:
		 	titleRow: [colHeader1, colHeader2, ..., colHeaderN]
			columnData:[
				[field:field1,style:style1], //for row 1
				[field:field2,style:style2], //for row 2
				...,
				[field:fieldN,style:styleN]  //for row N
			]
		 ]
	 */
    SXSSFWorkbook generateXLSXWorkbook(Map sheets) {
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
		Map workbookStyles = ['positive':csPositive,'neutral':csNeutral,'negative':csNegative,'bold':bold]
		SXSSFWorkbook output = new SXSSFWorkbook(wb,50,true)
		output.setCompressTempFiles(true)
		sheets.entrySet().each { sheetData ->
			try {
				def title = sheetData.key
				List titleRow = (List) sheetData.value.titleRow
				List columnData = (List) sheetData.value.columnData
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
				columnData.each { rowData ->
					int cellnum = 0
					row = sheet.createRow(rownum)
					rowData.each { cellData ->
						cell = row.createCell(cellnum++)
						cell.setCellValue(cellData.field)
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
	 * Retrieves for the given property definition type and organisation of list of headers, containing property definition names. Includes custom and privare properties
	 * @param propDefConst - a {@link PropertyDefinition} constant which property definition type should be loaded
	 * @param contextOrg - the context {@link Org}
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
	 *
	 * @param propDefConst
	 * @param contextOrg
	 * @param format
	 * @return a {@link List} or a {@link List} of {@link Map}s for the export sheet containing the value
	 */
	List processPropertyListValues(Set<PropertyDefinition> propertyDefinitions, String format, def target) {
		List cells = []
		SimpleDateFormat sdf = DateUtil.getSimpleDateFormatByToken('default.date.format.notime')
		propertyDefinitions.each { pd ->
			def value = ''
			target.customProperties.each{ CustomProperty prop ->
				if(prop.type.descr == pd.descr && prop.type == pd && prop.value) {
					if(prop.refValue)
						value = prop.refValue.getI10n('value')
					else
						value = prop.getValue() ?: ' '
				}
			}
			target.privateProperties.each{ PrivateProperty prop ->
				if(prop.type.descr == pd.descr && prop.type == pd && prop.value) {
					if(prop.refValue)
						value = prop.refValue.getI10n('value')
					else
						value = prop.getValue() ?: ' '
				}
			}
			def cell
			switch(format) {
				case "xls":
				case "xlsx": cell = [field: value, style: null]
					break
				case "csv": cell = value.replaceAll(',',';')
					break
			}
			if(cell)
				cells.add(cell)
		}
		cells
	}

	/**
	 * Generates a title stream export list according to the KBART II-standard but enriched with proprietary fields such as ZDB-ID
	 * The standard is defined here: <a href="https://www.uksg.org/kbart/s5/guidelines/data_fields">KBART definition</a>
	 *
	 * @param entitlementData - a {@link Collection} containing the actual data
	 * @return a {@link Map} containing lists for the title row and the column data
	 */
	Map<String,List> generateTitleExportKBART(Collection entitlementData) {
		Map<String,List> export = [titleRow:[
				'publication_title',
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
				'publisher_name',
				'publication_type',
				'date_monograph_published_print',
				'date_monograph_published_online',
				'monograph_volume',
				'monograph_edition',
				'first_editor',
				'parent_publication_title_id',
				'preceding_publication_title_id',
				'access_type',
				'access_start_date',
				'access_end_date',
				'zdb_id',
				'zdb_ppn',
				'ezb_anchor',
				'ezb_collection_id',
				'subscription_isil',
				'subscription_isci',
				'package_isil',
				'package_isci',
				'package_gokb_uid',
				'DOI',
				'ISSNs',
				'eISSNs',
				'pISBNs',
				'ISBNs',
				'listprice_value',
				'listprice_currency',
				'localprice_value',
				'localprice_currency',
				'price_date'
		],columnData:[]]
		List allRows = []
		entitlementData.each { ieObj ->
			def entitlement
			if(ieObj instanceof IssueEntitlement) {
				entitlement = (IssueEntitlement) ieObj
			}
			else if(ieObj instanceof TitleInstancePackagePlatform) {
				entitlement = (TitleInstancePackagePlatform) ieObj
			}
			if(entitlement) {
				entitlement.coverages.each { AbstractCoverage covStmt ->
					allRows << covStmt
				}
				if(!entitlement.coverages)
					allRows << entitlement
			}
		}
		//this double strucutre is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
		allRows.each { rowData ->
			IssueEntitlement entitlement = null
			TitleInstancePackagePlatform tipp = null
			AbstractCoverage covStmt = null
			if(rowData instanceof IssueEntitlement) {
				entitlement = (IssueEntitlement) rowData
				tipp = entitlement.tipp
			}
			else if(rowData instanceof IssueEntitlementCoverage) {
				covStmt = (IssueEntitlementCoverage) rowData
				entitlement = covStmt.issueEntitlement
				tipp = covStmt.issueEntitlement.tipp
			}
			else if(rowData instanceof TitleInstancePackagePlatform) {
				tipp = (TitleInstancePackagePlatform) rowData
			}
			else if(rowData instanceof TIPPCoverage) {
				covStmt = (TIPPCoverage) rowData
				tipp = covStmt.tipp
			}
			List row = []
			log.debug("processing ${tipp.title}")
			//publication_title
			row.add("${tipp.title.title}")
			log.debug("add main identifiers")
			//print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
			if(tipp.title.getIdentifierValue('pISBN'))
				row.add(tipp.title.getIdentifierValue('pISBN'))
			else if(tipp.title.getIdentifierValue('ISSN'))
				row.add(tipp.title.getIdentifierValue('ISSN'))
			else row.add(' ')
			//online_identifier
			if(tipp.title.getIdentifierValue('ISBN'))
				row.add(tipp.title.getIdentifierValue('ISBN'))
			else if(tipp.title.getIdentifierValue('eISSN'))
				row.add(tipp.title.getIdentifierValue('eISSN'))
			else row.add(' ')
			log.debug("process package start and end")
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
			log.debug("add title url")
			//title_url
			row.add(tipp.hostPlatformURL ?: ' ')
			//first_author (no value?)
			row.add(' ')
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
			//publisher_name (no value?)
			row.add(' ')
			//publication_type
			switch(tipp.title.medium) {
				case RDStore.TITLE_TYPE_JOURNAL: row.add('serial')
					break
				case RDStore.TITLE_TYPE_EBOOK: row.add('monograph')
					break
				default: row.add(' ')
					break
			}
			if(tipp.title instanceof BookInstance) {
				BookInstance bookInstance = (BookInstance) tipp.title
				//date_monograph_published_print (no value unless BookInstance)
				row.add(bookInstance.dateFirstInPrint ? formatter.format(bookInstance.dateFirstInPrint) : ' ')
				//date_monograph_published_online (no value unless BookInstance)
				row.add(bookInstance.dateFirstOnline ? formatter.format(bookInstance.dateFirstOnline) : ' ')
				//monograph_volume (no value unless BookInstance)
				row.add(bookInstance.editionNumber ?: ' ')
				//monograph_edition (no value unless BookInstance)
				row.add(bookInstance.editionStatement ?: ' ')
				//first_editor (no value unless BookInstance)
				row.add(bookInstance.firstEditor ?: ' ')
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
			row.add(' ')
			/*
            switch(entitlement.tipp.payment) {
                case RDStore.TIPP_PAYMENT_OA: row.add('F')
                    break
                case RDStore.TIPP_PAYMENT_PAID: row.add('P')
                    break
                default: row.add(' ')
                    break
            }*/
			//access_start_date
			row.add(entitlement?.derivedAccessStartDate ? formatter.format(entitlement.derivedAccessStartDate) : ' ')
			//access_end_date
			row.add(entitlement?.derivedAccessEndDate ? formatter.format(entitlement.derivedAccessEndDate) : ' ')
			log.debug("processing identifiers")
			//zdb_id
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB,','))
			//zdb_ppn
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB_PPN,','))
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
			//package_isil
			row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISIL_PAKETSIGEL,','))
			//package_isci
			row.add(joinIdentifiers(tipp.pkg.ids,IdentifierNamespace.ISCI,','))
			//package_gokb_uid
			row.add(tipp.pkg.gokbId)
			//DOI
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.DOI,','))
			//ISSNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ISSN,','))
			//eISSNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.EISSN,','))
			//pISBNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.PISBN,','))
			//ISBNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.PISBN,','))
			if(entitlement?.priceItem) {
				//listprice_value
				row.add(entitlement.priceItem.listPrice ? entitlement.priceItem.listPrice.setScale(2, RoundingMode.HALF_UP) : ' ')
				//listprice_currency
				row.add(entitlement.priceItem.listCurrency ? entitlement.priceItem.listCurrency.value : ' ')
				//localprice_value
				row.add(entitlement.priceItem.localPrice ? entitlement.priceItem.localPrice.setScale(2, RoundingMode.HALF_UP) : ' ')
				//localprice_currency
				row.add(entitlement.priceItem.localCurrency ? entitlement.priceItem.localCurrency.value : ' ')
				//price_date
				row.add(entitlement.priceItem.priceDate ? formatter.format(entitlement.priceItem.priceDate) : ' ')
			}
			else {
				//empty values for price item columns
				row.add(' ')
				row.add(' ')
				row.add(' ')
				row.add(' ')
				row.add(' ')
			}
			export.columnData.add(row)
		}
		export
	}

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

	Map<String,List> generateTitleExportCSV(Collection entitlementData) {
		Map<String,List> export = [titleRow:[messageSource.getMessage('title',null,locale),
											 messageSource.getMessage('tipp.volume',null,locale),
											 messageSource.getMessage('author.slash.editor',null,locale),
											 messageSource.getMessage('title.editionStatement.label',null,locale),
											 messageSource.getMessage('title.summaryOfContent.label',null,locale),
											 'zdb_id',
											 'zdb_ppn',
											 'DOI',
											 'ISSNs',
											 'eISSNs',
											 'pISBNs',
											 'ISBNs',
											 messageSource.getMessage('title.dateFirstInPrint.label',null,locale),
											 messageSource.getMessage('title.dateFirstOnline.label',null,locale),
											 messageSource.getMessage('tipp.listPrice',null,locale),
											 messageSource.getMessage('financials.currency',null,locale),
											 messageSource.getMessage('tipp.localPrice',null,locale),
											 messageSource.getMessage('financials.currency',null,locale)],
		columnData:[]]
	}

	Map<String, List> generateTitleExportXLS(Collection entitlements) {
		Map<String,List> export = [titles:[
				messageSource.getMessage('title',null,locale),
				messageSource.getMessage('tipp.volume',null,locale),
				messageSource.getMessage('author.slash.editor',null,locale),
				messageSource.getMessage('title.editionStatement.label',null,locale),
				messageSource.getMessage('title.summaryOfContent.label',null,locale),
				'zdb_id',
				'zdb_ppn',
				'DOI',
				'ISSNs',
				'eISSNs',
				'pISBNs',
				'ISBNs',
				messageSource.getMessage('title.dateFirstInPrint.label',null,locale),
				messageSource.getMessage('title.dateFirstOnline.label',null,locale),
				messageSource.getMessage('tipp.listPrice',null,locale),
				messageSource.getMessage('financials.currency',null,locale),
				messageSource.getMessage('tipp.localPrice',null,locale),
				messageSource.getMessage('financials.currency',null,locale)]]

		List rows = []
		entitlements.each { entObj ->
			IssueEntitlement entitlement = null
			TitleInstancePackagePlatform tipp = null
			if(entObj instanceof IssueEntitlement) {
				entitlement = (IssueEntitlement) entObj
				tipp = entitlement.tipp
			}
			else if(entObj instanceof TitleInstancePackagePlatform) {
				tipp = (TitleInstancePackagePlatform) entObj
			}
			List row = []
			row.add([field: tipp.title.title ?: '', style:null])
			if(tipp.title instanceof BookInstance) {
				row.add([field: tipp.title.volume ?: '', style: null])
				row.add([field: tipp.title.getEbookFirstAutorOrFirstEditor() ?: '', style: null])
				row.add([field: tipp.title.editionStatement ?: '', style:null])
				row.add([field: tipp.title.summaryOfContent ?: '', style:null])
			}else{
				row.add([field: '', style:null])
				row.add([field: '', style:null])
				row.add([field: '', style:null])
				row.add([field: '', style:null])
			}

			//zdb_id
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB,','), style:null])
			//zdb_ppn
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB_PPN,','), style:null])
			//DOI
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.DOI,','), style:null])
			//ISSNs
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.ISSN,','), style:null])
			//eISSNs
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.EISSN,','), style:null])
			//pISBNs
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.PISBN,','), style:null])
			//ISBNs
			row.add([field: joinIdentifiers(tipp.title.ids,IdentifierNamespace.ISBN,','), style:null])

			if(tipp.title instanceof BookInstance) {
				row.add([field: tipp.title.dateFirstInPrint ? formatter.format(tipp.title.dateFirstInPrint) : '', style: null])
				row.add([field: tipp.title.dateFirstOnline ? formatter.format(tipp.title.dateFirstOnline) : '', style: null])
			}else{
				row.add([field: '', style:null])
				row.add([field: '', style:null])
			}

			if(entitlement && entitlement.priceItem) {
				row.add([field: entitlement.priceItem.listPrice ? entitlement.priceItem.listPrice.setScale(2,RoundingMode.HALF_UP) : '', style: null])
				row.add([field: entitlement.priceItem.listCurrency ? entitlement.priceItem.listCurrency.value : '', style: null])
				row.add([field: entitlement.priceItem.localPrice ? entitlement.priceItem.localPrice.setScale(2,RoundingMode.HALF_UP) : '', style: null])
				row.add([field: entitlement.priceItem.localCurrency ? entitlement.priceItem.listCurrency.value : '', style: null])
			}else{
				row.add([field: '', style:null])
				row.add([field: '', style:null])
				row.add([field: '', style:null])
				row.add([field: '', style:null])
			}

			rows.add(row)
		}
		export.rows = rows
		export
	}

	/* *************
	 * legacy CSV Exports
	 */

    def HQLCoreDates = "SELECT ca.startDate, ca.endDate FROM TitleInstitutionProvider as tip join tip.coreDates as ca WHERE tip.title.id= :ie_title AND tip.institution.id= :ie_institution AND tip.provider.id= :ie_provider"

    /*
    legacy
	def StreamOutLicenseCSV(out,result,licenses){
		log.debug("StreamOutLicenseCSV - ${result} - ${licenses}")
		Set propertiesSet = new TreeSet();

		def custProps = licenses.each{ license ->
			propertiesSet.addAll(license.customProperties.collect{ prop ->
				prop.type.name
			})
		}
		
		out.withWriter{writer ->
			//See if we are handling a currentLicenses Search
			if(result != null && result.searchHeader ){
				writer.write("SEARCH TERMS\n")
				writer.write("Institution,ValidOn,ReferenceSearch,LicenceProperty,LicencePropertyValue\n")
				writer.write("${val(result.institution?.name)},${val(result.validOn)},${val(result.keyWord)},${val(result.propertyFilterType)},${val(result.propertyFilter)}\n" )
				writer.write("\n")	
			}
			writer.write("KB+ Licence ID,LicenceReference,NoticePeriod,LicenceURL,StartDate,EndDate,Licence Category,Licence Status")
			propertiesSet.each{
				writer.write(",${val(it)}")
				writer.write(",\"${it} Notes\"")
			}
			writer.write("\n")
			licenses.each{ lic ->
				writer.write("${lic.id},${val(lic.reference)},${val(lic.noticePeriod)},${val(lic.licenseUrl)},${val(lic.startDate)},${val(lic.endDate)},${val(lic.licenseCategory?.value)},${val(lic.status?.value)}")
 
				propertiesSet.each{ prop_name ->
					def prop_match = lic.customProperties.find{it.type.name == prop_name}
					if(prop_match){
						writer.write(",${val(prop_match.value)},${val(prop_match.note)}")
					}else{
						writer.write(", , ")
					}
				}

				writer.write("\n")
			}
			
			writer.flush()
			writer.close()
		}
	}
	*/


	@Deprecated
	def addLicenseSubPkgXML(Document doc, Element into_elem, List licenses){
		log.debug("addLicenseSubPkgXML - ${licenses}")

		licenses.each() { license ->
			def licElem = addXMLElementInto(doc, into_elem, "Licence", null)
			addXMLElementInto(doc, licElem, "LicenceReference", license.reference)
			addXMLElementInto(doc, licElem, "LicenceID", license.id)

			def licSubs =  addXMLElementInto(doc, licElem, "Subscriptions", null)
			license.subscriptions.each{ subscription ->
				def licSub = addXMLElementInto(doc, licSubs, "Subscription", null)

				addXMLElementInto(doc, licSub, "SubscriptionID", subscription.id)
				addXMLElementInto(doc, licSub, "SubscriptionName", subscription.name)
				addXMLElementInto(doc, licSub, "SubTermStartDate", formatDate(subscription.startDate))
				addXMLElementInto(doc, licSub, "SubTermEndDate", formatDate(subscription.endDate))
				addXMLElementInto(doc, licSub, "ManualRenewalDate", formatDate(subscription.manualRenewalDate))

				def subPkgs = addXMLElementInto(doc, licSub, "Packages", null)
				subscription.packages.each{ subPkg ->
					def pkg = subPkg.pkg
					def pkgElem = addXMLElementInto(doc, subPkgs, "Package", null)
					addXMLElementInto(doc, pkgElem, "PackageID", pkg.id)
					addXMLElementInto(doc, pkgElem, "PackageName", pkg.name)
					addXMLElementInto(doc, pkgElem, "PackageContentProvider", pkg.getContentProvider()?.name)
				}
			}
		}
	}

	@Deprecated
	def addLicenseSubPkgTitleXML(Document doc, Element into_elem, List licenses){
		log.debug("addLicenseSubPkgXML - ${licenses}")

		licenses.each() { license ->
			def licElem = addXMLElementInto(doc, into_elem, "Licence", null)
			addXMLElementInto(doc, licElem, "LicenceReference", license.reference)
			addXMLElementInto(doc, licElem, "LicenceID", license.id)

			def licSubs =  addXMLElementInto(doc, licElem, "Subscriptions", null)
			license.subscriptions.each{ subscription ->
				def licSub = addXMLElementInto(doc, licSubs, "Subscription", null)

				addXMLElementInto(doc, licSub, "SubscriptionID", subscription.id)
				addXMLElementInto(doc, licSub, "SubscriptionName", subscription.name)
				addXMLElementInto(doc, licSub, "SubTermStartDate", formatDate(subscription.startDate))
				addXMLElementInto(doc, licSub, "SubTermEndDate", formatDate(subscription.endDate))
				addXMLElementInto(doc, licSub, "ManualRenewalDate", formatDate(subscription.manualRenewalDate))

				def ieList = addXMLElementInto(doc, licSub, "TitleList", null)
				subscription.issueEntitlements.each{ ie ->
					def issue = addXMLElementInto(doc, ieList, "TitleListEntry", null)
					addXMLElementInto(doc, issue, "Title", ie.tipp.title.title)
					def title_ids = addXMLElementInto(doc, issue, "TitleIDs", null)
					def ie_issn = addXMLElementInto(doc, title_ids, "ID", ie.tipp.title.getIdentifierValue("issn"))
					addXMLAttr(doc,ie_issn,"namespace","issn")
					def ie_eissn = addXMLElementInto(doc, title_ids, "ID", ie.tipp.title.getIdentifierValue("eissn"))
					addXMLAttr(doc,ie_eissn,"namespace","eissn")

					addXMLElementInto(doc, issue, "PackageName", ie.tipp.pkg.name)
					addXMLElementInto(doc, issue, "PackageID", ie.tipp.pkg.id)

					def ie_coverage = addXMLElementInto(doc,issue,"CoverageStatement",null)
					addXMLAttr(doc, ie_coverage, "type", "Issue Entitlement")
					addXMLElementInto(doc, ie_coverage, "SubscriptionID", subscription.id)
					addXMLElementInto(doc, ie_coverage, "SubscriptionName", subscription.name)

					addXMLElementInto(doc, ie_coverage, "StartDate", formatDate(ie.coreStatusStart))
					addXMLElementInto(doc, ie_coverage, "EndDate", formatDate(ie.coreStatusEnd))
					addXMLElementInto(doc, ie_coverage, "StartVolume", ie.startVolume)
					addXMLElementInto(doc, ie_coverage, "EndVolume", ie.endVolume)
					addXMLElementInto(doc, ie_coverage, "StartIssue", ie.startIssue)
					addXMLElementInto(doc, ie_coverage, "EndIssue", ie.endIssue)
					addXMLElementInto(doc, ie_coverage, "CoverageNote", ie.coverageNote)

				}
			}
		}
	}
	
	/**
	 * This function will stream out the list of titles in a CSV format.
	 *
	 * @param out - the {@link OutputStream}
	 * @param entitlements - the list of {@link IssueEntitlement}
	 */
	@Deprecated
	def StreamOutTitlesCSV(out, entitlements){
 		def starttime = printStart("Get Namespaces and max IE")
		// Get distinct ID.Namespace and the maximum of entitlements for one title

		def namespaces = []
		def current_title_id = -1
		def current_nb_ie = 0
		def max_nb_ie = 1
		entitlements.each(){ ie ->
			def ti = ie.tipp.title
			if(ti.id != current_title_id){
				current_title_id = ti.id
				if(max_nb_ie<current_nb_ie) max_nb_ie = current_nb_ie
				current_nb_ie = 1
				//Add namespace
				ti.ids.each(){ id -> namespaces.add(id.ns.ns) }
			}else{
				current_nb_ie ++
			}
		}
		namespaces.unique()
		printDuration(starttime, "Get Namespaces and max IE=${max_nb_ie}")
		
		out.withWriter { writer ->
			// Output the header
			writer.write("Title,")
			namespaces.each(){ ns -> writer.write("${ns},") }
			writer.write("Earliest date,Latest date")
			(1..max_nb_ie).each(){
				writer.write(",IE.${it}.Subscription name,")
				writer.write("IE.${it}.Start date,")
				writer.write("IE.${it}.Start Volume,")
				writer.write("IE.${it}.Start Issue,")
				writer.write("IE.${it}.End date,")
				writer.write("IE.${it}.End Volume,")
				writer.write("IE.${it}.End Issue,")
				writer.write("IE.${it}.Embargo,")
				writer.write("IE.${it}.Coverage,")
				writer.write("IE.${it}.Coverage note,")
				writer.write("IE.${it}.platform.host.name,")
				writer.write("IE.${it}.platform.host.url,")
				writer.write("IE.${it}.platform.admin.name,")
				writer.write("IE.${it}.Core date list,")
				writer.write("IE.${it}.Core medium")
			}
			writer.write("\n")

			// result.titles.each { title ->
			// def ti = title[0]
			current_title_id = -1
			String entitlements_str
			def earliest_date
			def latest_date
			entitlements.each { e ->
				e.coverages.each { covStmt ->
					if(e.tipp.title.id != current_title_id){
						if(current_title_id != -1){
							//Write earliest and latest dates
							writer.write("\"${earliest_date?formatter.format(earliest_date):''}\",");
							writer.write("\"${latest_date?formatter.format(latest_date):''}\"");
							//Write entitlements
							writer.write("${entitlements_str}");
							writer.write("\n");
						}

						//Start a new title
						current_title_id = e.tipp.title.id
						def ti = e.tipp.title
						entitlements_str = ""

						writer.write("\"${ti.title}\",");
						namespaces.each(){ ns ->
							writer.write("\"${ti.getIdentifierValue(ns)?:''}\",");
						}
						earliest_date = e.startDate?:null
						latest_date = e.endDate?:null
					}

					if(covStmt.startDate && (!earliest_date || earliest_date>covStmt.startDate)) earliest_date = covStmt.startDate
					if(covStmt.endDate && (!latest_date || latest_date<covStmt.endDate)) latest_date = covStmt.endDate

//                    grouped_ies[ti[0].id].each(){ ie ->
					entitlements_str += ",\"${e.subscription.name}\","
					entitlements_str += "${covStmt.startDate?formatter.format(covStmt.startDate):''},"
					entitlements_str += "\"${covStmt.startVolume?:''}\","
					entitlements_str += "\"${covStmt.startIssue?:''}\","
					entitlements_str += "${covStmt.endDate?formatter.format(covStmt.endDate):''},"
					entitlements_str += "\"${covStmt.endVolume?:''}\","
					entitlements_str += "\"${covStmt.endIssue?:''}\","
					entitlements_str += "\"${covStmt.embargo?:''}\","
					entitlements_str += "\"${covStmt.coverageDepth?:''}\","
					entitlements_str += "\"${covStmt.coverageNote?:''}\","
					entitlements_str += "\"${e.tipp?.platform?.name?:''}\","
					entitlements_str += "\"${e.tipp?.hostPlatformURL?:''}\","
					entitlements_str += "\""
					e.tipp?.additionalPlatforms.eachWithIndex(){ ap, i ->
						if(i>0) entitlements_str += ", "
						entitlements_str += "${ap.platform.name}"
					}
					entitlements_str += "\","
					def coreDateList = ""
					for(Date[] coreDate : getIECoreDates(e)){
						coreDateList += formatCoreDates(coreDate) + " - "
					}
					entitlements_str += "\"${coreDateList}\","
					entitlements_str += "\"${e.coreStatus?:''}\""
//                    }
				}
			}

			
			//Write earliest and latest dates for last title
			writer.write("\"${earliest_date?formatter.format(earliest_date):''}\",");
			writer.write("\"${latest_date?formatter.format(latest_date):''}\"");
			//Write entitlements for last title
			writer.write("${entitlements_str}");
			writer.write("\n");
			
			printDuration(starttime, "Finished Export.Closing")

			writer.flush()
			writer.close()
		}
	}
	
	/* ************
	 * XML Exports
	 */
	
	/**
	 * Create the document and with the root Element of the XML file (deploy that for what in an external method???) Legacy!
	 *
	 * @param root - the name of the root {@link Element}
	 * @return the {@link Document} created
	 */
	/*
	def buildDocXML(root) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(root);
		doc.appendChild(rootElement);
		
		return doc
	}
	*/
	/*
	def getIECoreDates(ie){
	def coreDates = TitleInstitutionProvider.executeQuery(hqlCoreDates,[ie:ie,cp_role:role_cprov,sub_role:role_subscriber])
    def inst = ie.subscription?.getSubscriber()
    def title = ie.tipp?.title
    def provider = ie.tipp?.pkg?.getContentProvider()
    
    def coreDates = null
    
    if (inst && title && provider) {
      coreDates = TitleInstitutionProvider.executeQuery(HQLCoreDates,[ie_title: title.id, ie_institution: inst.id, ie_provider: provider.id])
    }
		return coreDates
	}
	def formatCoreDates(dates){
	    return "${dates[0]?formatter.format(dates[0]):''} : ${dates[1]?formatter.format(dates[1]):''}"
	}
	 */
	/**
	 * Add a list of titles from a given entitlement list into a given Element - legacy!
	 * 
	 * @param doc - the {@link Document}
	 * @param into_elem - the {@link Element} into which we want to insert the list of titles
	 * @param entries - the list of {@link IssueEntitlement} or {@link TitleInstancePackagePlatform}
	 * @param type -  either "TIPP" or default "Issue Entitlement"
	 */
	/*
    def addTitleListXML(Document doc, Element into_elem, List entries, String type = "Issue Entitlement") {
		def current_title_id = -1
		def starttime = printStart("Add TitleListXML")

		Element titlelistentry
		entries.each { e ->
			// There is a few distinction between TIPP and IE objects, they are handled here
			def tipp = (type=="Issue Entitlement")?e.tipp:e
			def sub  = (type=="Issue Entitlement")?e.subscription:e.sub
			def status  = (type=="Issue Entitlement")?e.coreStatus:e.status
			def statusColumnHeader = (type == "Issue Entitlement") ? "CoreMedium" : "TIPPStatus"
			
			if(tipp.title.id != current_title_id){
				current_title_id = tipp.title.id
				def ti = tipp.title
				
				// TitleListEntry elements
				titlelistentry = addXMLElementInto(doc, into_elem, "TitleListEntry", null)
				// Title elements
				Element title = addXMLElementInto(doc, titlelistentry, "Title", ti.title)
				// TitleIDs elements
				Element titleids = addXMLElementInto(doc, titlelistentry, "TitleIDs", null)
				
				ti.ids.each(){ id ->
					def value = id.identifier.value
					def ns = id.identifier.ns.ns
					Element titleid = addXMLElementInto(doc, titleids, "ID", value)
					// set attribute to titleid element
					addXMLAttr(doc, titleid, "namespace", ns)
				}
			}
			
			// CoverageStatement elements
			Element coveragestatement = addXMLElementInto(doc, titlelistentry, "CoverageStatement", null)
			addXMLAttr(doc, coveragestatement, "type", type)
			
			addXMLElementInto(doc, coveragestatement, "SubscriptionID", sub?.id?:'')
			addXMLElementInto(doc, coveragestatement, "SubscriptionName", sub?.name?:'')
			addXMLElementInto(doc, coveragestatement, "StartDate", e.startDate?formatter.format(e.startDate):'')
			addXMLElementInto(doc, coveragestatement, "StartVolume", e.startVolume?:'')
			addXMLElementInto(doc, coveragestatement, "StartIssue", e.startIssue?:'')
			addXMLElementInto(doc, coveragestatement, "EndDate", e.endDate?formatter.format(e.endDate):'')
			addXMLElementInto(doc, coveragestatement, "EndVolume", e.endVolume?:'')
			addXMLElementInto(doc, coveragestatement, "EndIssue", e.endIssue?:'')
			addXMLElementInto(doc, coveragestatement, "Embargo", e.embargo?:'')
			addXMLElementInto(doc, coveragestatement, "Coverage", e.coverageDepth?:'')
			addXMLElementInto(doc, coveragestatement, "CoverageNote", e.coverageNote?:'')
			addXMLElementInto(doc, coveragestatement, "HostPlatformName", tipp?.platform?.name?:'')
			addXMLElementInto(doc, coveragestatement, "HybridOA", tipp?.hybridOA?.value?:'')
			addXMLElementInto(doc, coveragestatement, "DelayedOA", tipp?.delayedOA?.value?:'')
			addXMLElementInto(doc, coveragestatement, "Payment", tipp?.payment?.value?:'')
			addXMLElementInto(doc, coveragestatement, "HostPlatformURL", tipp?.hostPlatformURL?:'')
			
			tipp.additionalPlatforms.each(){ ap ->
				def platform = addXMLElementInto(doc, coveragestatement, "Platform", null)
				addXMLElementInto(doc, platform, "PlatformName", ap.platform?.name?:'')
				addXMLElementInto(doc, platform, "PlatformRole", ap.rel?:'')
				addXMLElementInto(doc, platform, "PlatformURL", ap.platform?.primaryUrl?:'')
			}
			
			addXMLElementInto(doc, coveragestatement, statusColumnHeader, status?.value?:'')
			if(type == "Issue Entitlement"){
				Element coreDateList = addXMLElementInto(doc,coveragestatement,"CoreDateList",null)
				getIECoreDates(e)?.each{
					Element coreDate = addXMLElementInto(doc,coreDateList,"CoreDate",null)
					addXMLElementInto(doc,coreDate,"CoreStart",it[0]?formatter.format(it[0]):'')
					if(it[1]){
						addXMLElementInto(doc,coreDate,"CoreEnd",it[1]?formatter.format(it[1]):'')
					}
				}
			}
			addXMLElementInto(doc, coveragestatement, "PackageID", tipp?.pkg?.id?:'')
			addXMLElementInto(doc, coveragestatement, "PackageName", tipp?.pkg?.name?:'')
            addXMLElementInto(doc, coveragestatement, "AccessStatus", tipp?.getAvailabilityStatusAsString()?:'')
            addXMLElementInto(doc, coveragestatement, "AccessFrom",  tipp?.accessStartDate?:'')
            addXMLElementInto(doc, coveragestatement, "AccessTo", tipp?.accessEndDate?:'')
        }

		printDuration(starttime, "Add TitleListXML")
    }
	*/
	
	/**
	 * Add the licenses of a given list into a given XML element.
	 * 
	 * @param doc - the {@link Document} to update
	 * @param into_elem - the {@link Element} we want to put the list of license(s) in.
	 * @param lics - the {@link com.k_int.kbplus.License} list
	 */
	@Deprecated
	def addLicensesIntoXML(Document doc, Element into_elem, List lics) {
		lics.each() { license ->
			def licElem = addXMLElementInto(doc, into_elem, "Licence", null)
			addXMLElementInto(doc, licElem, "LicenceReference", license.reference)
			addXMLElementInto(doc, licElem, "NoticePeriod", license.noticePeriod)
			addXMLElementInto(doc, licElem, "LicenceURL", license.licenseUrl)
			//addXMLElementInto(doc, licElem, "LicensorRef", license.licensorRef)
			//addXMLElementInto(doc, licElem, "LicenseeRef", license.licenseeRef)
			
			addRelatedOrgsIntoXML(doc, licElem, license.orgLinks)
			
			def licPropElem = addXMLElementInto(doc, licElem, "LicenceProperties", null)
			
			license.customProperties.each{ prop ->
				def propertyType = addXMLElementInto(doc, licPropElem, "${prop.type.name.replaceAll("\\s","").replaceAll("/","_").replaceAll(":","")}", null)
				addXMLElementInto(doc, propertyType, "Value","${prop.getValue()}")
				addXMLElementInto(doc, propertyType, "Note","${prop.note?:''}")

			}
			def licenseNotes = addXMLElementInto(doc, licElem, "LicenceNotes", null)
    		license.documents.each{docctx->
			      if(docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING && (docctx.status == null || docctx.status?.value != 'Deleted')){
			          def note_val = docctx.owner?.content
			          if(note_val){
      					addXMLElementInto(doc, licenseNotes, "Note","${note_val}")
      				}
			      }
		    }
		}
	}
	
	/**
	 * Add a subscription into a XML file
	 * It will also add the License (owner) and Titles of that subscription
	 * 
	 * @param doc - the {@link Document} to update
	 * @param into_elem - the {@link Element} we want to put the list of license(s) in.
	 * @param sub - the {@link Subscription}
	 * @param entitlements - the list of {@link IssueEntitlement}
	 */
	@Deprecated
	def addSubIntoXML(Document doc, Element into_elem, sub, entitlements) {
		def subElem = addXMLElementInto(doc, into_elem, "Subscription", null)
		addXMLElementInto(doc, subElem, "SubscriptionID", sub.id.toString())
		addXMLElementInto(doc, subElem, "SubscriptionName", sub.name)
		addXMLElementInto(doc, subElem, "SubTermStartDate", sub.startDate?formatter.format(sub.startDate):'')
		addXMLElementInto(doc, subElem, "SubTermEndDate", sub.endDate?formatter.format(sub.endDate):'')
		
		addRelatedOrgsIntoXML(doc, subElem, sub.orgRelations)
		
		if(sub.owner) addLicensesIntoXML(doc, subElem, [sub.owner])
		
		def titlesElem = addXMLElementInto(doc, subElem, "TitleList", null)
		addTitleListXML(doc, titlesElem, entitlements)
	}
	
	/**
	 * Add a package into a XML file
	 * It will also add the License and the Titles of that subscription
	 * 
	 * @param doc - the {@link Document} to update
	 * @param into_elem - the {@link Element} we want to put the list of license(s) in.
	 * @param pck - the {@link Package}
	 * @param tipps - the list of {@link TitleInstancePackagePlatform}
	 */
	@Deprecated
	def addPackageIntoXML(Document doc, Element into_elem, pck, tipps) {
		def subElem = addXMLElementInto(doc, into_elem, "Package", null)
		addXMLElementInto(doc, subElem, "PackageID", pck.id.toString())
		addXMLElementInto(doc, subElem, "PackageName", pck.name)
		addXMLElementInto(doc, subElem, "PackageTermStartDate", pck.startDate?formatter.format(pck.startDate):'')
		addXMLElementInto(doc, subElem, "PackageTermEndDate", pck.endDate?formatter.format(pck.endDate):'')
		
		addRelatedOrgsIntoXML(doc, subElem, pck.orgs)
		
		if(pck.license) addLicensesIntoXML(doc, subElem, [pck.license])
		
		def titlesElem = addXMLElementInto(doc, subElem, "TitleList", null)
		addTitleListXML(doc, titlesElem, tipps, "TIPP")
	}
	
	/**
	 * Add Organisation into a given Element.
	 * 
	 * @param doc - the {@link Document} to update
	 * @param into_elem - the {@link Element} we want to put the list of license(s) in.
	 * @param orgs - list of {@link Org}
	 */
	@Deprecated
	private addRelatedOrgsIntoXML(Document doc, Element into_elem, orgs){
		orgs.each { or ->
			def orgElem = addXMLElementInto(doc, into_elem, "RelatedOrg", null)
			addXMLAttr(doc, orgElem, "id", or.org.id.toString())
			addXMLElementInto(doc, orgElem, "OrgName", or.org.name)
			addXMLElementInto(doc, orgElem, "OrgRole", or.roleType?.value?:'')
			
			def orgIDsElem = addXMLElementInto(doc, orgElem, "OrgIDs", null)
			or.org.ids.each(){ id ->
				def value = id.value
				def ns = id.ns.ns
				def idElem = addXMLElementInto(doc, orgIDsElem, "ID", value)
				addXMLAttr(doc, idElem, "namespace", ns)
			}
		}
	}
	
	/**
	 * Stream out a given Document into a given output.
	 * This function is using TransformerFactory to create the XML output.
	 * It will use UTF-8 and add line break and space to get a readable XML architecture.
	 * 
	 * @param doc - the {@link Document} to stream
	 * @param out - the {@link OutputStream}
	 * @return - the {@link StreamResult} created
	 */
	@Deprecated
	def streamOutXML(doc, out) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
//		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //add line break
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1"); //add spaces for xml architecture
		DOMSource source = new DOMSource(doc);
		
		StreamResult streamout = new StreamResult(out);
		transformer.transform(source, streamout);
		
		return streamout
	}
	
	/* 
	 * A few useful method to build XML document 
	 */
	
	/**
	 * Add an attribute into a given Element
	 * 
	 * @param doc - the {@link Document}
	 * @param e - the {@link Element} to update
	 * @param name - name of the attribute
	 * @param val - value of the attribute
	 */
	@Deprecated
	private Element addXMLAttr(Document doc, Element e, String name, String val){
		Attr attr = doc.createAttribute(name);
		attr.setValue(val);
		e.setAttributeNode(attr);
	}
	
	/**
	 * Add XML Element into another given Element
	 * 
	 * @param doc - the {@link Document}
	 * @param p - parent {@link Element}
	 * @param name - name of the element
	 * @param content - text content of the element 
	 * @return the {@link Element} created
	 */
	@Deprecated
	private Element addXMLElementInto(def doc, Element p, String name, def content){
		Element e = doc.createElement(name);
		if(content)
			e.appendChild(doc.createTextNode("${content}"));
		p.appendChild(e)
		return e
	}
	
	/* *************
	 * JSON EXPORTS
	 */
	
	/**
	 * Add a list of titles from a given entitlement list into a given Map.
	 * The Map created with this function has the purpose to be transformed into JSON.
	 * 
	 * @param into_map - Map which will contain the list
	 * @param ie_list - list of {@link com.k_int.kbplus.IssueEntitlement}
	 */
	@Deprecated
	def addTitlesToMap(into_map, ie_list, String type = "Issue Entitlement"){
		def starttime = printStart("Add titles to MAP")


		def current_title_id = -1
		def titles = []
		def title
		def entitlements
		ie_list.each { e ->
			// There is a few distinction between TIPP and IE objects, they are handled here
			def tipp = (type=="Issue Entitlement")?e.tipp:e
			def sub  = (type=="Issue Entitlement")?e.subscription:e.sub
			def status  = (type=="Issue Entitlement")?e.coreStatus:e.status
			def statusColumnHeader = (type=="Issue Entitlement")? "CoreMedium" : "TIPPStatus"
			if(tipp.title.id != current_title_id){
				//start new title
				if(current_title_id!=-1) titles.add(title) // not the first time
				title = [:]
				
				current_title_id = tipp.title.id
				def ti = tipp.title
				
				title."Title" = ti.title
			
				def ids = [:]
				ti.ids.each(){ id ->
					def value = id.value
					def ns = id.ns.ns
					if(ids.containsKey(ns)){
						def current = ids[ns]
						def newval = []
						newval << current
						newval << value
						ids[ns] = newval
					} else {
						ids[ns]=value
					}
				}
				title."TitleIDs" = ids
				entitlements = title."CoverageStatements" = []
			}
			
			def ie = [:]
			ie."CoverageStatementType" = type
			ie."SubscriptionID" = sub?.id
			ie."SubscriptionName" = sub?.name
			ie."StartDate" = e.startDate?formatter.format(e.startDate):''
			ie."StartVolume" = e.startVolume?:''
			ie."StartIssue" = e.startIssue?:''
			ie."EndDate" = e.endDate?formatter.format(e.endDate):''
			ie."EndVolume" = e.endVolume?:''
			ie."EndIssue" = e.endIssue?:''
			ie."Embargo" = e.embargo?:''
			ie."Coverage" = e.coverageDepth?:''
			ie."CoverageNote" = e.coverageNote?:''
			ie."HostPlatformName" = tipp?.platform?.name?:''
			ie."HostPlatformURL" = tipp?.hostPlatformURL?:''
                        ie."HybridOA" =  tipp?.hybridOA?.value?:''
                        ie."DelayedOA"= tipp?.delayedOA?.value?:''
                        ie."Payment" = tipp?.payment?.value?:''
			ie."AdditionalPlatforms" = []
			tipp?.additionalPlatforms.each(){ ap ->
				def platform = [:]
				platform.PlatformName = ap.platform?.name?:''
				platform.PlatformRole = ap.rel?:''
				platform.PlatformURL = ap.platform?.primaryUrl?:''
				ie."AdditionalPlatforms" << platform
			}
			ie."${statusColumnHeader}" = status?.value?:''
			if(type == "Issue Entitlement"){
				def dateList = []
				getIECoreDates(e)?.each{
					def dates = [:]
					dates."startDate" = it[0] ? formatter.format(it[0]) :''
					dates."endDate" = it[1] ? formatter.format(it[1]) : ''
					dateList.add(dates)
				}
				ie."CoreDateList" = dateList
			}
			ie."PackageID" = tipp?.pkg?.id?:''
			ie."PackageName" = tipp?.pkg?.name?:''
            ie."AccessFrom" = tipp?.accessStartDate?:''
            ie."AccessTo" = tipp?.accessEndDate?:''
            ie."AccessStatus" = tipp?.getAvailabilityStatus().toString()?:''


            entitlements.add(ie)
		}
		titles.add(title) // add last title

		into_map."TitleList" = titles
		printDuration(starttime, "Add titles to MAP")

	}
	
	/**
	 * Add Organisations into a given Map.
	 * The Map created with this function has the purpose to be transformed into JSON.
	 * 
	 * @param into_map - map which will contain the list of organisation
	 * @param orgs - list of {@link com.k_int.kbplus.Org}
	 */
	@Deprecated
	def addOrgMap(into_map, orgs){
		orgs.each { or ->
			def org = [:]
			org."OrgID" = or.org.id
			org."OrgName" = or.org.name
			org."OrgRole" = or.roleType?.value?:''
			
			def ids = [:]
			or.org.ids.each(){ id ->
				def value = id.value
				def ns = id.ns.ns
				if(ids.containsKey(ns)){
					def current = ids[ns]
					def newval = []
					newval << current
					newval << value
					ids[ns] = newval
				} else {
					ids[ns]=value
				}
			}
			org."OrgIDs" = ids
			
			into_map."RelatedOrgs" << org
		}
	}
	
	/**
	 * Add Licenses into a given Map.
	 * The Map created with this function has the purpose to be transformed into JSON.
	 * 
	 * @param into_map - map which will contain the list of licenses
	 * @param lics - list of {@link com.k_int.kbplus.License}
	 * @return the Map created
	 */
	@Deprecated
	def addLicensesToMap(into_map, lics){
		def licenses = []
		
		lics.each { license ->
			def lic = [:]
			
			lic."LicenceReference" = license.reference
			lic."NoticePeriod" = license.noticePeriod
			lic."LicenceURL" = license.licenseUrl
			// removed - lic."LicensorRef" = license.licensorRef
			// removed - lic."LicenseeRef" = license.licenseeRef
				
			lic."RelatedOrgs" = []
			addOrgMap(lic, license.orgLinks)
			
			def prop = lic."LicenceProperties" = [:]
			license.customProperties.each{
				def custprop = prop."${it.type.name}" = [:]
				custprop."Status" = it.getValue()?:""
				custprop."Notes" = it.getNote()?:""
			}

			licenses << lic
		}
		into_map."Licences" = licenses
		
		return into_map
	}
	
	
	/**
	 * Create a Subscription Map which has the purpose to be transformed into JSON.
	 * 
	 * @param sub - the {@link com.k_int.kbplus.Subscription}
	 * @param entitlements - list of {@link com.k_int.kbplus.IssueEntitlement}
	 * @return the Map created
	 */
	@Deprecated
	def getSubscriptionMap(sub, entitlements){
		def map = [:]
		def subscriptions = []
		
		def subscription = [:]
		subscription."SubscriptionID" = sub.id
		subscription."SubscriptionName" = sub.name
		subscription."SubTermStartDate" = sub.startDate?formatter.format(sub.startDate):''
		subscription."SubTermEndDate" = sub.endDate?formatter.format(sub.endDate):''
		
		subscription."RelatedOrgs" = []
		
		addOrgMap(subscription, sub.orgRelations)
		
		if(sub.owner) addLicensesToMap(subscription, [sub.owner])
		
		addTitlesToMap(subscription, entitlements)
					
		subscriptions.add(subscription)
		
		map."Subscriptions" = subscriptions
		
		return map
	}
	
	/**
	 * Create a Package Map which has the purpose to be transformed into JSON.
	 * 
	 * @param pck - the {@link com.k_int.kbplus.Package}
	 * @param tipps - the list of {@link com.k_int.kbplus.TitleInstancePackagePlatform}
	 * @return the Map created
	 */
	@Deprecated
	def getPackageMap(pck, tipps){
		def map = [:]
		def packages = []
		
		def pckage = [:]
		pckage."PackageID" = pck.id
		pckage."PackageName" = pck.name
		pckage."PackageTermStartDate" = pck.startDate?formatter.format(pck.startDate):''
		pckage."PackageTermEndDate" = pck.endDate?formatter.format(pck.endDate):''
				
		pckage."RelatedOrgs" = []
		
		addOrgMap(pckage, pck.orgs)
		
		if(pck.license) addLicensesToMap(pckage, [pck.license])
		
		addTitlesToMap(pckage, tipps, "TIPP")
					
		packages.add(pckage)
		
		map."Packages" = packages
		
		return map
	}
	
	/* **************
	 * OTHER METHODS
	 */
	
	/**
	 * This function has been created to track the time taken by the different methods provided by this service
	 * It's suppose to be run at the start of an event and it will catch the time and display it.
	 * 
	 * @param event - text which will be print out, describing the event
	 * @return time when the method is called
	 */
	def printStart(event){
		def starttime = new Date();
		log.debug("******* Start ${event}: ${starttime} *******")
		return starttime
	}
	
	/**
	 * This function has been created to track the time taken by the different methods provided by this service.
	 * It's suppose to be run at the end of an event.
	 * It will print the duration between the given time and the current time.
	 * 
	 * @param starttime - the time when the event started
	 * @param event - text which will be print out, describing the event
	 */
	def printDuration(starttime, event){
		use(groovy.time.TimeCategory) {
			def duration = new Date() - starttime
			log.debug("******* End ${event}: ${new Date()} *******")
			log.debug("Duration: ${(duration.hours*60)+duration.minutes}m ${duration.seconds}s")
		}
	}
	def formatDate(date){
		if(date){
			return formatter.format(date)
		}else
			return null
	}
	/**
	* @return the value in the required format for CSV exports.
	**/
	def val(val){
		if(val instanceof java.sql.Timestamp || val instanceof Date){
			return val?formatter.format(val):" "
		}else{
			val = val? val.replaceAll('"',"'") :" "
			return "\"${val}\""
		}
	}
}
