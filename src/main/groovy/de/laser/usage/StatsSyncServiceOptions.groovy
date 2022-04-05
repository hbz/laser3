package de.laser.usage


import de.laser.Identifier
import de.laser.Org
import de.laser.OrgSetting
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.TitleInstancePackagePlatform
import de.laser.storage.RDConstants
import de.laser.properties.PlatformProperty
import de.laser.titles.TitleInstance
import groovy.util.logging.Slf4j

@Slf4j
class StatsSyncServiceOptions {

    // Report specific options
    RefdataValue factType
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
    TitleInstancePackagePlatform title_inst
    Platform supplier_inst
    Org org_inst
    Identifier identifier

    void setItemObjects(objectList)
    {
        title_inst = (TitleInstancePackagePlatform)objectList[0]
        supplier_inst = (Platform)objectList[1]
        org_inst = (Org)objectList[2]
        identifier = (Identifier)objectList[3]
        statsTitleIdentifier = identifier.value
        setStatsIdentifierType(identifier)
    }

    void setStatsIdentifierType(Identifier identifier) {
        String type = identifier.ns?.ns
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
        Map<String,String> params = getQueryParams(org_inst, supplier_inst)
        platform = params?.platform
        customer = params?.customer
        apiKey = params?.apiKey
        requestor = params?.requestor
    }

    LinkedHashMap getBasicQueryParams() {
        [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
    }

    void setReportSpecificQueryParams(RefdataValue report) {
        def matcher = report.value =~ /^(.*).(\d)$/
        reportName = matcher[0][1]
        reportVersion = matcher[0][2]
        setReportType()

        factType = RefdataValue.construct([
                token   : report.toString(),
                rdc     : RDConstants.FACT_TYPE,
                hardData: false,
                i10n    : [value_en: report.toString(), value_de: report.toString()]
        ])
    }

    LinkedHashMap getQueryParams(Org org_inst, Platform supplier_inst) {
        PlatformProperty platform = PlatformProperty.executeQuery(
            "select pcp from PlatformProperty pcp where pcp.owner = :supplier and pcp.type.name = 'NatStat Supplier ID'",[supplier:supplier_inst]).get(0)
        String customer = org_inst.getIdentifierByType('wibid').value
        String apiKey = OrgSetting.get(org_inst, OrgSetting.KEYS.NATSTAT_SERVER_API_KEY)?.getValue()
        String requestor = OrgSetting.get(org_inst, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)?.getValue()
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
