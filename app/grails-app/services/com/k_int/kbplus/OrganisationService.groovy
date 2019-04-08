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
    def exportService

    def exportOrg(List orgs, message, boolean addHigherEducationTitles, String format) {
        def titles = ['Name', messageSource.getMessage('org.shortname.label',null, LocaleContextHolder.getLocale()), messageSource.getMessage('org.sortname.label',null,LocaleContextHolder.getLocale())]
        def orgSector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
        if(addHigherEducationTitles) {
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
        List orgData = []
        switch(format) {
            case "xls":
            case "xlsx":
                orgs.each{  org ->
                    List row = []
                    //Name
                    row.add([field: org.name ?: '',style: null])
                    //Shortname
                    row.add([field: org.shortname ?: '',style: null])
                    //Sortname
                    row.add([field: org.sortname ?: '',style: null])
                    if(addHigherEducationTitles) {
                        //libraryType
                        row.add([field: org.libraryType?.getI10n('value') ?: ' ',style: null])
                        //libraryNetwork
                        row.add([field: org.libraryNetwork?.getI10n('value') ?: ' ',style: null])
                        //funderType
                        row.add([field: org.funderType?.getI10n('value') ?: ' ',style: null])
                        //federalState
                        row.add([field: org.federalState?.getI10n('value') ?: ' ',style: null])
                        //country
                        row.add([field: org.country?.getI10n('value') ?: ' ',style: null])
                    }
                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
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
                            if(prop.type.descr == pd.descr && prop.type == pd) {
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
                        row.add([field: value, style: null])
                    }
                    orgData.add(row)
                }
                return exportService.generateXLSXWorkbook(message,titles,orgData)
            case "csv":
                orgs.each{  org ->
                    List row = []
                    //Name
                    row.add(org.name ?: '')
                    //Shortname
                    row.add(org.shortname ?: '')
                    //Sortname
                    row.add(org.sortname ?: '')
                    if(addHigherEducationTitles) {
                        //libraryType
                        row.add(org.libraryType?.getI10n('value') ?: ' ')
                        //libraryNetwork
                        row.add(org.libraryNetwork?.getI10n('value') ?: ' ')
                        //funderType
                        row.add(org.funderType?.getI10n('value') ?: ' ')
                        //federalState
                        row.add(org.federalState?.getI10n('value') ?: ' ')
                        //country
                        row.add(org.country?.getI10n('value') ?: ' ')
                    }
                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
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
                            if(prop.type.descr == pd.descr && prop.type == pd) {
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
                        row.add(value)
                    }
                    orgData.add(row)
                }
                return exportService.generateCSVString(titles,orgData)
        }

    }

}
