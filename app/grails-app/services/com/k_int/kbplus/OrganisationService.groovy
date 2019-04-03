package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import grails.transaction.Transactional
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class OrganisationService {

    def contextService
    def messageSource

    SXSSFWorkbook exportOrg(orgs, message, addHigherEducationTitles) {
            def titles = [
                    'Name', messageSource.getMessage('org.shortname.label',null, LocaleContextHolder.getLocale()), messageSource.getMessage('org.sortname.label',null,LocaleContextHolder.getLocale())]

            def orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
            def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')


            if(addHigherEducationTitles)
            {
                titles.add(messageSource.getMessage('org.libraryType.label',null,LocaleContextHolder.getLocale()))
                titles.add(messageSource.getMessage('org.libraryNetwork.label',null,LocaleContextHolder.getLocale()))
                titles.add(messageSource.getMessage('org.funderType.label',null,LocaleContextHolder.getLocale()))
                titles.add(messageSource.getMessage('org.federalState.label',null,LocaleContextHolder.getLocale()))
                titles.add(messageSource.getMessage('org.country.label',null,LocaleContextHolder.getLocale()))
            }

            def propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

            propList.sort { a, b -> a.name.compareToIgnoreCase b.name}

            propList.each {
                titles.add(it.name)
            }

            XSSFWorkbook workbook = new XSSFWorkbook()
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

            orgs.sort{it.name}
            orgs.each{  org ->
                int cellnum = 0
                row = sheet.createRow(rownum)

                //Name
                cell = row.createCell(cellnum++)
                cell.setCellValue(org.name ?: '')

                //Shortname
                cell = row.createCell(cellnum++)
                cell.setCellValue(org.shortname ?: '')

                //Sortname
                cell = row.createCell(cellnum++)
                cell.setCellValue(org.sortname ?: '')


                if(addHigherEducationTitles) {

                    //libraryType
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.libraryType?.getI10n('value') ?: ' ')

                    //libraryNetwork
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.libraryNetwork?.getI10n('value') ?: ' ')

                    //funderType
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.funderType?.getI10n('value') ?: ' ')

                    //federalState
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.federalState?.getI10n('value') ?: ' ')

                    //country
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(org.country?.getI10n('value') ?: ' ')
                }

                propList.each { pd ->
                    def value = ''
                    org.customProperties.each{ prop ->
                        if(prop.type.descr == pd.descr && prop.type == pd)
                        {
                            if(prop.type.type == Integer.toString()){
                                value = prop.intValue.toString()
                            }
                            else if (prop.type.type == String.toString()){
                                value = prop.stringValue ?: ''
                            }
                            else if (prop.type.type == BigDecimal.toString()){
                                value = prop.decValue.toString()
                            }
                            else if (prop.type.type == Date.toString()){
                                value = prop.dateValue.toString()
                            }
                            else if (prop.type.type == RefdataValue.toString()) {
                                value = prop.refValue?.getI10n('value') ?: ''
                            }

                        }
                    }

                    org.privateProperties.each{ prop ->
                        if(prop.type.descr == pd.descr && prop.type == pd)
                        {
                            if(prop.type.type == Integer.toString()){
                                value = prop.intValue.toString()
                            }
                            else if (prop.type.type == String.toString()){
                                value = prop.stringValue ?: ''
                            }
                            else if (prop.type.type == BigDecimal.toString()){
                                value = prop.decValue.toString()
                            }
                            else if (prop.type.type == Date.toString()){
                                value = prop.dateValue.toString()
                            }
                            else if (prop.type.type == RefdataValue.toString()) {
                                value = prop.refValue?.getI10n('value') ?: ''
                            }

                        }
                    }
                    cell = row.createCell(cellnum++);
                    cell.setCellValue(value)
                }

                rownum++
            }

            for (int i = 0; i < 22; i++) {
                sheet.autoSizeColumn(i)
            }
            wb
        }

}
