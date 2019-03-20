package de.laser.usage

import com.k_int.kbplus.IdentifierOccurrence
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgCustomProperty
import com.k_int.kbplus.TitleInstance
import com.k_int.properties.PropertyDefinition
import groovy.util.logging.Log4j

@Log4j
class StatsSyncServiceOptions {

    //statsReport

    String platform
    String customer
    String apiKey
    String requestor
    String mostRecentClosedPeriod

    String statsTitleIdentifier

    TitleInstance title_inst
    Org supplier_inst
    Org org_inst
    IdentifierOccurrence title_io_inst



    def setItemObjects(objectList)
    {
        title_inst = objectList[0]
        supplier_inst = objectList[1]
        org_inst = objectList[2]
        title_io_inst = objectList[3]

        statsTitleIdentifier = title_io_inst?.identifier?.value
    }

    def setQueryParams()
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

    def getQueryParams()
    {
        [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
    }

    LinkedHashMap getQueryParams(org_inst, supplier_inst) {
        def platform = supplier_inst.getIdentifierByType('statssid').value
        def customer = org_inst.getIdentifierByType('wibid').value
        def apiKey = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("API Key"), org_inst)
        def requestor = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"),org_inst)
        [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
    }
}
