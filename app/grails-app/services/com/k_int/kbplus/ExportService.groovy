package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import de.laser.base.AbstractCoverage
import de.laser.IssueEntitlementCoverage
import de.laser.TIPPCoverage
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFDataFormat
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.MessageSource
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
	def messageSource

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
							cell.setCellValue((String) cellData.field);
						} else if (cellData.field instanceof Integer) {
							cell.setCellValue((Integer) cellData.field);
						} else if (cellData.field instanceof Double) {
							cell.setCellValue((Double) cellData.field);
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
						if(cellData.field instanceof Integer || cellData.field instanceof Double){
							cell.setCellStyle(numberStyle);
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
		List<IdentifierNamespace> otherTitleIdentifierNamespaces = IdentifierNamespace.executeQuery('select distinct(id.ns) from Identifier id where id.ti != null and id.ns.ns not in (:coreTitleNS)',[coreTitleNS:IdentifierNamespace.CORE_TITLE_NS])
		List<String> titleHeaders = [
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
				'ISBNs']
		titleHeaders.addAll(otherTitleIdentifierNamespaces.collect { IdentifierNamespace ns -> "${ns.ns}_identifer"})
		titleHeaders.addAll(['listprice_value',
				'listprice_currency',
				'localprice_value',
				'localprice_currency',
				'price_date'])
		Map<String,List> export = [titleRow:titleHeaders,columnData:[]]
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
				row.add(bookInstance.volume ?: ' ')
				//monograph_edition (no value unless BookInstance)
				row.add(bookInstance.editionNumber ?: ' ')
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
			//other identifier namespaces
			otherTitleIdentifierNamespaces.each { IdentifierNamespace ns ->
				row.add(joinIdentifiers(tipp.title.ids,ns.ns,','))
			}
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

	Map<String,List> generateTitleExportCSV(Collection entitlements) {
		Locale locale = LocaleContextHolder.getLocale()
		List<IdentifierNamespace> otherTitleIdentifierNamespaces = IdentifierNamespace.executeQuery('select distinct(id.ns) from Identifier id where id.ti != null and id.ns.ns not in (:coreTitleNS)',[coreTitleNS:IdentifierNamespace.CORE_TITLE_NS])
		List<String> titleHeaders = [messageSource.getMessage('title',null,locale),
									 messageSource.getMessage('tipp.volume',null,locale),
									 messageSource.getMessage('author.slash.editor',null,locale),
									 messageSource.getMessage('title.editionStatement.label',null,locale),
									 messageSource.getMessage('title.summaryOfContent.label',null,locale),
									 messageSource.getMessage('title.seriesName.label',null,locale),
									 messageSource.getMessage('title.subjectReference.label',null,locale),
									 'zdb_id',
									 'zdb_ppn',
									 'DOI',
									 'ISSNs',
									 'eISSNs',
									 'pISBNs',
									 'ISBNs',
									 messageSource.getMessage('title.dateFirstInPrint.label',null,locale),
									 messageSource.getMessage('title.dateFirstOnline.label',null,locale)]
		titleHeaders.addAll(otherTitleIdentifierNamespaces.collect{ IdentifierNamespace ns -> "${ns.ns}_identifier"})
		titleHeaders.addAll([messageSource.getMessage('tipp.listPrice',null,locale),
									 messageSource.getMessage('financials.currency',null,locale),
									 messageSource.getMessage('tipp.localPrice',null,locale),
									 messageSource.getMessage('financials.currency',null,locale)])
		Map<String,List> export = [titleRow:titleHeaders,
		columnData:[]]
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
			row.add(tipp.title.title ?: ' ')
			if(tipp.title instanceof BookInstance) {
				row.add(tipp.title.volume ?: ' ')
				row.add(tipp.title.getEbookFirstAutorOrFirstEditor() ?: ' ')
				row.add(tipp.title.editionStatement ?: ' ')
				row.add(tipp.title.summaryOfContent ?: ' ')
			}else{
				row.add(' ')
				row.add(' ')
				row.add(' ')
				row.add(' ')
			}
			row.add(tipp.title.seriesName ?: ' ')
			row.add(tipp.title.subjectReference ?: ' ')

			//zdb_id
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB,';'))
			//zdb_ppn
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ZDB_PPN,';'))
			//DOI
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.DOI,';'))
			//ISSNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ISSN,';'))
			//eISSNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.EISSN,';'))
			//pISBNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.PISBN,';'))
			//ISBNs
			row.add(joinIdentifiers(tipp.title.ids,IdentifierNamespace.ISBN,';'))

			if(tipp.title instanceof BookInstance) {
				row.add(tipp.title.dateFirstInPrint ? formatter.format(tipp.title.dateFirstInPrint) : ' ')
				row.add(tipp.title.dateFirstOnline ? formatter.format(tipp.title.dateFirstOnline) : ' ')
			}else{
				row.add(' ')
				row.add(' ')
			}
			otherTitleIdentifierNamespaces.each { IdentifierNamespace otherNS ->
				row.add(joinIdentifiers(tipp.title.ids,otherNS.ns,';'))
			}
			if(entitlement && entitlement.priceItem) {
				row.add(entitlement.priceItem.listPrice ? entitlement.priceItem.listPrice.setScale(2,RoundingMode.HALF_UP) : ' ')
				row.add(entitlement.priceItem.listCurrency ? entitlement.priceItem.listCurrency.value : ' ')
				row.add(entitlement.priceItem.localPrice ? entitlement.priceItem.localPrice.setScale(2,RoundingMode.HALF_UP) : ' ')
				row.add(entitlement.priceItem.localCurrency ? entitlement.priceItem.listCurrency.value : ' ')
			}else{
				row.add(' ')
				row.add(' ')
				row.add(' ')
				row.add(' ')
			}

			rows.add(row)
		}
		export.rows = rows
		export
	}

	Map<String, List> generateTitleExportXLS(Collection entitlements) {
		Locale locale = LocaleContextHolder.getLocale()
		List<IdentifierNamespace> otherTitleIdentifierNamespaces = IdentifierNamespace.executeQuery('select distinct(id.ns) from Identifier id where id.ti != null and id.ns.ns not in (:coreTitleNS)',[coreTitleNS:IdentifierNamespace.CORE_TITLE_NS])
		List<String> titleHeaders = [
				messageSource.getMessage('title',null,locale),
				messageSource.getMessage('tipp.volume',null,locale),
				messageSource.getMessage('author.slash.editor',null,locale),
				messageSource.getMessage('title.editionStatement.label',null,locale),
				messageSource.getMessage('title.summaryOfContent.label',null,locale),
				messageSource.getMessage('title.seriesName.label',null,locale),
				messageSource.getMessage('title.subjectReference.label',null,locale),
				'zdb_id',
				'zdb_ppn',
				'DOI',
				'ISSNs',
				'eISSNs',
				'pISBNs',
				'ISBNs',
				messageSource.getMessage('title.dateFirstInPrint.label',null,locale),
				messageSource.getMessage('title.dateFirstOnline.label',null,locale)]
		titleHeaders.addAll(otherTitleIdentifierNamespaces.collect {IdentifierNamespace ns -> "${ns.ns}_identifier"})
		titleHeaders.addAll([messageSource.getMessage('tipp.listPrice',null,locale),
				messageSource.getMessage('financials.currency',null,locale),
				messageSource.getMessage('tipp.localPrice',null,locale),
				messageSource.getMessage('financials.currency',null,locale)])
		Map<String,List> export = [titles:titleHeaders]
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
			row.add([field: tipp.title.seriesName ?: '',style: null])
			row.add([field: tipp.title.subjectReference ?: '',style: null])

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
			otherTitleIdentifierNamespaces.each { IdentifierNamespace otherNS ->
				row.add([field: joinIdentifiers(tipp.title.ids,otherNS.ns,','),style:null])
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
