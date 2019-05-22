package de.laser.usage

import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.IdentifierOccurrence
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgCustomProperty
import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.TitleInstance
import com.k_int.properties.PropertyDefinition
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
    String identifierType
    TitleInstance title_inst
    Org supplier_inst
    Org org_inst
    IdentifierOccurrence title_io_inst

    void setItemObjects(objectList)
    {
        title_inst = (TitleInstance)objectList[0]
        supplier_inst = (Org)objectList[1]
        org_inst = (Org)objectList[2]
        title_io_inst = (IdentifierOccurrence)objectList[3]
        statsTitleIdentifier = title_io_inst?.identifier?.value
        identifierType = title_io_inst?.identifier?.ns?.ns
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
        factType = RefdataCategory.lookupOrCreate('FactType', report.toString())
    }

    LinkedHashMap getQueryParams(org_inst, supplier_inst) {
        def platform = supplier_inst.getIdentifierByType('statssid').value
        def customer = org_inst.getIdentifierByType('wibid').value
        def apiKey = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("API Key"), org_inst)
        def requestor = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"),org_inst)
        [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
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
