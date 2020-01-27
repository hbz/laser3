package de.laser.usage

import com.k_int.kbplus.*
import de.laser.helper.RDConstants
import groovy.util.logging.Log4j

@Log4j
class StatsSyncServiceOptions {

    // Report specific options
    def factType
    String reportName
    String reportVersion
    String from
    String to
    String reportType

    // Basic options
    String platform
    String customer
    String apiKey
    String requestor
    String mostRecentClosedPeriod
    String statsTitleIdentifier
    String statsIdentifierType
    TitleInstance title_inst
    Platform supplier_inst
    Org org_inst
    Identifier identifier

    void setItemObjects(objectList)
    {
        title_inst = (TitleInstance)objectList[0]
        supplier_inst = (Platform)objectList[1]
        org_inst = (Org)objectList[2]
        identifier = (Identifier)objectList[3]
        statsTitleIdentifier = identifier.value
        setStatsIdentifierType(identifier)
    }

    void setStatsIdentifierType(identifier) {
        def type = identifier.ns?.ns
        // ugly difference in type name
        if (type == 'zdb'){
            statsIdentifierType = 'zdbid'
        } else {
            statsIdentifierType = type
        }
    }

    void setBasicQueryParams()
    {
        if (! org_inst || ! supplier_inst){
            log.debug("Inst Org or Supplier Org not set in StatsSyncOptions::setQueryParams")
        }
        def params = getQueryParams(org_inst, supplier_inst)
        platform = params?.platform
        customer = params?.customer
        apiKey = params?.apiKey
        requestor = params?.requestor
    }

    LinkedHashMap getBasicQueryParams() {
        [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
    }

    void setReportSpecificQueryParams(report) {
        def matcher = report.value =~ /^(.*).(\d)$/
        reportName = matcher[0][1]
        reportVersion = matcher[0][2]
        setReportType()
        // ERMS-2016: factType = RefdataCategory.lookupOrCreate('FactType', report.toString())
        // if value exists --> RefdataValue.getByValueAndCategory()
        factType = RefdataValue.construct([
                token   : report.toString(),
                rdc     : RDConstants.FACT_TYPE,
                hardData: false,
                i10n    : [value_en: report.toString(), value_de: report.toString()]
        ])
    }

    LinkedHashMap getQueryParams(org_inst, supplier_inst) {
        def platform = supplier_inst.customProperties.find(){
            it.type.name = "NatStat Supplier ID"
        }
        def customer = org_inst.getIdentifierByType('wibid').value
        def apiKey = OrgSettings.get(org_inst, OrgSettings.KEYS.NATSTAT_SERVER_API_KEY)?.getValue()
        def requestor = OrgSettings.get(org_inst, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID)?.getValue()
        [platform:platform.stringValue, customer:customer, apiKey: apiKey, requestor:requestor]
    }

    Boolean identifierTypeAllowedForAPICall()
    {
        if (! statsIdentifierType || ! reportType){
            return false
        }
        switch (reportType) {
            case "book":
            if (statsIdentifierType == "doi") {
                return true
            }
            break
            case "journal":
            if (statsIdentifierType == "zdbid") {
                return true
            }
            break
            case "database":
            if (statsIdentifierType == "zdbid") {
                return true
            }
            break
            default:
                return false
            break
        }
    }

    void setReportType() {
        if (reportName)
        switch (reportName) {
            case "JR1":
                reportType = "journal"
                break
            case "JR1GOA":
                reportType = "journal"
                break
            case "DB1":
                reportType = "database"
                break
            case "BR1":
                reportType = "book"
                break
            case "BR2":
                reportType = "book"
                break
            default:
                reportType = "journal"
                break
        }
    }

}
