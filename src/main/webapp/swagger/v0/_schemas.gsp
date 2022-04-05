<%@ page import="de.laser.interfaces.CalculatedType; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.api.v0.ApiToolkit; de.laser.helper.RDConstants; de.laser.properties.PropertyDefinition; de.laser.helper.RDStore" %>
<%
    def printRefdataEnum = { rdc, pos ->
        String spacer = ''
        for (pos; pos>0; pos--) {
            spacer += ' '
        }

        println()
        RefdataCategory.getAllRefdataValues(rdc).each {
            println("${spacer}- \"${it.value}\"")
        }
    }
%>
<%-- indention: 4 --%>

    PlaceholderObject:
      type: object
      format: string

    PlaceholderList:
      type: array
      items:
        $ref: "#/components/schemas/PlaceholderObject"

    PlaceholderBinary:
      type: object
      format: binary


<%-- objects --%>

    Address:
      type: object
      properties:
        street1:
          type: string
        street2:
          type: string
        pob:
          type: string
        pobZipcode:
          type: string
        pobCity:
          type: string
        zipcode:
          type: string
        city:
          type: string
        name:
          type: string
        additionFirst:
          type: string
        additionSecond:
          type: string
        region:
          type: string
          description: Mapping RefdataCategory "${RDConstants.REGIONS_DE}", "${RDConstants.REGIONS_AT}" and "${RDConstants.REGIONS_CH}"
          enum: <% printRefdataEnum([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH], 12) %>
        country:
          type: string
          description: Mapping RefdataCategory "${RDConstants.COUNTRY}"
          enum: <% printRefdataEnum(RDConstants.COUNTRY, 12) %>
        type:
          type: array
          items:
            type: string
          description: Mapping RefdataCategory "${RDConstants.ADDRESS_TYPE}"
          enum: <% printRefdataEnum(RDConstants.ADDRESS_TYPE, 12) %>
        lastUpdated:
          type: string
          format: "<% print ApiToolkit.DATE_TIME_PATTERN %>"


    Contact:
      type: object
      properties:
        category: # mapping attr contentType
          type: string
          description: Mapping RefdataCategory "${RDConstants.CONTACT_CONTENT_TYPE}"
          enum: <% printRefdataEnum(RDConstants.CONTACT_CONTENT_TYPE, 12) %>
        content:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        language:
          type: string
          description: Mapping RefdataCategory "${RDConstants.LANGUAGE_ISO}"
          enum: <% printRefdataEnum(RDConstants.LANGUAGE_ISO, 12) %>


    CostItem:
      type: object
      properties:
        globalUID:
          type: string
          description: global unique identifier for system-wide identification in LAS:eR.
          example: "costitem:ab1360cc-147b-d632-2dc8-1a6c56d84b00"
        calculatedType:
          type: string
          description: Calculated object type of the cost item; internally defined.
          enum:
            ["${CalculatedType.TYPE_CONSORTIAL}", "${CalculatedType.TYPE_LOCAL}"]
          example: ${CalculatedType.TYPE_LOCAL}
        billingCurrency:
          type: string
          description: The billing currency of the cost item. Maps to the RefdataCategory "${RDConstants.CURRENCY}".
          enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
          example: ${RDStore.CURRENCY_EUR.value}
        billingSumRounding:
          type: string #mapped to boolean
          description: Should the billing sum be rounded? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: ${RDStore.YN_YES.value}
        costInBillingCurrency:
          type: string
          description: Numeric value of the sum in billing currency.
          example: 575
        costInBillingCurrencyAfterTax:
          type: string
          description: Numeric value of the sum with included tax in billing currency.
          example: 615.25
        costInLocalCurrency:
          type: string
          description: Numeric value of the sum in local currency.
          example: 575
        costInLocalCurrencyAfterTax:
          type: string
          description: Numeric value of the sum with included tax in local currency.
          example: 615.25
        costItemElement:
          type: string
          description: Element of the cost item; maps to the RefdataCategory "${RDConstants.COST_ITEM_ELEMENT}".
          enum: <% printRefdataEnum(RDConstants.COST_ITEM_ELEMENT, 12) %>
          example: "${RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.value}"
        costItemStatus:
          type: string
          description: Status of the cost item; maps to the RefdataCategory "${RDConstants.COST_ITEM_STATUS}".
          enum: <% printRefdataEnum(RDConstants.COST_ITEM_STATUS, 12) %>
          example: ${RDStore.COST_ITEM_ACTUAL.value}
        costTitle:
          type: string
          description: Free text to name the cost item.
          example: Gentz'sche Schulden
        costDescription:
          type: string
          description: Free text to describe/annotate the cost item.
          example: Bei Sadewasser eintreiben
        currencyRate:
          type: string
          description: The conversation rate in the moment of the cost item validity. Defaults are 1 or 0.
          example: 1
        dateCreated:
          type: string
          description: Timestamp when the cost item has been created.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-03-30T09:45:08"
        datePaid:
          type: string
          description: Timestamp when the cost item has been paid.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-01-15T00:00:00"
        endDate:
          type: string
          description: Timestamp when the cost item validity span ends.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-12-31T00:00:00"
        financialYear:
          type: string
          description: The financial year for which this cost item has been accounted.
          format: YYYY
          example: 2022
        finalCostRounding:
          type: string #mapped to boolean
          description: Should the final sum be rounded? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: ${RDStore.YN_NO.value}
        isVisibleForSubscriber:
          type: string #mapped to boolean
          description: Is the given cost item visible for the subscriber? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: ${RDStore.YN_YES.value}
        invoiceDate:
          type: string
          description: Timestamp when the invoice has been issued.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-03-07T00:00:00"
        invoice:
          $ref: "#/components/schemas/Invoice"
        issueEntitlement:
          $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        lastUpdated:
          type: string
          description: Timestamp when the cost item has most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-03-30T09:45:20"
        order:
          $ref: "#/components/schemas/Order"
        owner:
          $ref: "#/components/schemas/OrganisationStub"
        reference:
          type: string
          description: "Alphanumeric reference / code of the given cost item."
          example: C-968-2022
        startDate:
          type: string
          description: Timestamp when the cost item validity span starts.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-01-01T00:00:00"
        sub:
          $ref: "#/components/schemas/SubscriptionStub"
        subPkg:
          $ref: "#/components/schemas/Package_in_CostItem"
        taxCode:
          type: string
          description: The type of tax for this cost item; maps to RefdataCategory "${RDConstants.TAX_TYPE}".
          enum: <% printRefdataEnum(RDConstants.TAX_TYPE, 12) %>
        taxRate:
          type: string
          description: The percent value of tax issues to this cost item.
          enum: [${ CostItem.TAX_RATES.collect{ it }.join(', ') }]
        titleGroup:
          $ref: "#/components/schemas/TitleGroup"


    Document:
      type: object
      properties:
        content:
          type: string
        filename:
          type: string
        mimetype:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        title:
          type: string
        type:
          type: string
          description: Mapping RefdataCategory "${RDConstants.DOCUMENT_TYPE}"
          enum: <% printRefdataEnum(RDConstants.DOCUMENT_TYPE, 12) %>
        uuid:
          type: string
          example: "70d4ef8a-71b9-4b39-b339-9f3773c29b26"


    Identifier:
      type: object
      properties:
        namespace:
          type: string
        value:
          type: string


    Invoice:
      type: object
      description: The underlying invoice for this cost item.
      properties:
        invoiceNumber:
          type: string
          description: An alphanumeric invoice number.
          example: CRC-2022-DT68
        lastUpdated:
          type: string
          description: Timestamp when the given invoice has been updated for the last time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T15:10:26"
        owner:
          $ref: "#/components/schemas/OrganisationStub"


<%--
    IssueEntitlement:
      type: object
      properties:
        globalUID:
          type: string
          example: "issueentitlement:af045a3c-0e32-a681-c21d-3cf17f581d2c"
        accessStartDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        accessEndDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        coreStatusStart:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        coreStatusEnd:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        coreStatus:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        coverageDepth:
          type: string
        coverageNote:
          type: string
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        endVolume:
          type: string
        endIssue:
          type: string
        embargo:
          type: string
        ieReason:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        medium:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        startVolume:
          type: string
        startIssue:
          type: string
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        subscription:
          $ref: "#/components/schemas/SubscriptionStub"
        tipp:
          $ref: "#/components/schemas/TitleInstancePackagePlatform"
--%>

    License:
      allOf:
        - $ref: "#/components/schemas/LicenseStub"
      properties:
        dateCreated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        documents:
          type: array
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        instanceOf:
          $ref: "#/components/schemas/LicenseStub"
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        licenseCategory:
          type: string
          description: Mapping RefdataCategory "${RDConstants.LICENSE_CATEGORY}"
          enum: <% printRefdataEnum(RDConstants.LICENSE_CATEGORY, 12) %>
        organisations: # mapping attr orgRelations
          type: array
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual" # resolved OrgRole
        properties: # mapping customProperties and privateProperties
          type: array
          items:
            $ref: "#/components/schemas/Property"
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        status:
          type: string
          description: Mapping RefdataCategory "${RDConstants.LICENSE_STATUS}"
          enum: <% printRefdataEnum(RDConstants.LICENSE_STATUS, 12) %>
        subscriptions:
          type: array
          items:
            $ref: "#/components/schemas/SubscriptionStub"


    Order:
      type: object
      description: The order item for which this cost item has been issued.
      properties:
        lastUpdated:
          type: string
          description: Timestamp when the given order has been updated for the last time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T15:10:26"
        orderNumber:
          type: string
          description: The alphanumeric order designator.
          example: BC-1871-1973
        owner:
          $ref: "#/components/schemas/OrganisationStub"

    OrgAccessPoint:
      type: object
      properties:
        globalUID:
          type: string
          example: "orgaccesspoint:ab1360cc-147b-d632-2dc8-1a6c56d84b00"
        ezproxy:
          type: array
          items:
            $ref: "#/components/schemas/EZProxy_in_OrgAccessPoint"
        ip:
          type: array
          items:
            $ref: "#/components/schemas/IP_in_OrgAccessPoint"
        openathens:
          type: array
          items:
            $ref: "#/components/schemas/OpenAthens_in_OrgAccessPoint"
        proxy:
          type: array
          items:
            $ref: "#/components/schemas/Proxy_in_OrgAccessPoint"
        shibboleth:
          type: array
          items:
            $ref: "#/components/schemas/Shibboleth_in_OrgAccessPoint"

    Organisation:
      allOf:
        - $ref: "#/components/schemas/OrganisationStub"
      properties:
        addresses:
          type: array
          items:
            $ref: "#/components/schemas/Address"
        comment:
          type: string
        contacts:
          type: array
          items:
            $ref: "#/components/schemas/Contact"
        country:
          type: string
          description: Mapping RefdataCategory "${RDConstants.COUNTRY}"
          enum: <% printRefdataEnum(RDConstants.COUNTRY, 12) %>
        eInvoice:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
        eInvoicePortal:
          type: string
          description: Mapping RefdataCategory "${RDConstants.E_INVOICE_PORTAL}"
          enum: <% printRefdataEnum(RDConstants.E_INVOICE_PORTAL, 12) %>
        region:
          type: string
          description: Mapping RefdataCategory "${RDConstants.REGIONS_DE}", "${RDConstants.REGIONS_AT}" and "${RDConstants.REGIONS_CH}"
          enum: <% printRefdataEnum([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH], 12) %>
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        libraryType:
          type: string
          description: Mapping RefdataCategory "LibraryType"
          enum: <% printRefdataEnum('LibraryType', 12) %>
        orgAccessPoints:
          type: array
          items:
            $ref: "#/components/schemas/OrgAccessPointCollection"
        persons: # mapping attr prsLinks
          type: array
          items:
            $ref: "#/components/schemas/Person" # resolved PersonRole
        properties: # mapping attr customProperties and privateProperties
          type: array
          items:
            $ref: "#/components/schemas/Property"
        scope:
          type: string
        sector:
          #deprecated: true
          type: string
          description: Mapping RefdataCategory "${RDConstants.ORG_SECTOR}"
          enum: <% printRefdataEnum(RDConstants.ORG_SECTOR, 12) %>
        shortname:
          type: string
        sortname:
          type: string
        status:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        type:
          type: array
          items:
            type: string
          description: Mapping RefdataCategory "${RDConstants.ORG_TYPE}"
          enum: <% printRefdataEnum(RDConstants.ORG_TYPE, 12) %>

    Package:
      allOf:
      - $ref: "#components/schemas/PackageStub"
      - type: object
        properties:
          autoAccept:
            type: string #mapped to boolean
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          breakable:
            type: string
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          cancellationAllowances:
            type: string
          consistent:
            type: string
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          dateCreated:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--          documents:
            type: array
            items:
              $ref: "#/definitions/Document" # resolved DocContext--%>
          endDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--      listVerifiedDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>--%>
          file:
            type: string
            description: Mapping RefdataCategory "${RDConstants.PACKAGE_FILE}"
            enum: <% printRefdataEnum(RDConstants.PACKAGE_FILE, 12) %>
          isPublic:
            type: string #mapped to boolean
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          lastUpdated:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--          license:
            $ref: "#/definitions/LicenseStub" --%>
          nominalPlatform:
            $ref: "#/components/schemas/Platform"
          organisations: # mapping attr orgs
            type: array
            items:
              $ref: "#/components/schemas/OrganisationRole_Virtual"
<%--          packageListStatus:
            type: string
            description: Mapping RefdataCategory "${RDConstants.PACKAGE_LIST_STATUS}"
            enum: <% printRefdataEnum(RDConstants.PACKAGE_LIST_STATUS, 12) --%>
          scope:
            type: string
            description: Mapping RefdataCategory "${RDConstants.PACKAGE_SCOPE}"
            enum: <% printRefdataEnum(RDConstants.PACKAGE_SCOPE, 12) %>
          packageStatus:
            type: string
            description: Mapping RefdataCategory "${RDConstants.PACKAGE_STATUS}"
            enum: <% printRefdataEnum(RDConstants.PACKAGE_STATUS, 12) %>
          contentType:
            type: string
            description: Mapping RefdataCategory "${RDConstants.PACKAGE_CONTENT_TYPE}"
            enum: <% printRefdataEnum(RDConstants.PACKAGE_CONTENT_TYPE, 12) %>
<%--     persons: # mapping attr prsLinks
           type: array
           items:
             $ref: "#/definitions/Person" # resolved PersonRole --%>
          sortName:
            type: string
          startDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--          subscriptions:
            type: array
            items:
              $ref: "#/definitions/SubscriptionStub" # resolved subscriptionPackages --%>
          tipps:
            type: array
            items:
              $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Package"


    Person:
      type: object
      properties:
        globalUID:
          type: string
          example: "person:a45a3cf0-f3ad-f231-d5ab-fc1d217f583c"
        addresses:
          type: array
          items:
            $ref: "#/components/schemas/Address"
        contacts:
          type: array
          items:
            $ref: "#/components/schemas/Contact"
        firstName:
          type: string
        gender:
          type: string
          description: Mapping RefdataCategory "${RDConstants.GENDER}"
          enum: <% printRefdataEnum(RDConstants.GENDER, 12) %>
        isPublic:
          type: string
          description: Mapping RefdataCategory "${RDConstants.Y_N}". If set *No*, it's an hidden entry to/from an addressbook (depending on the given organisation context)
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        lastName:
          type: string
        middleName:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        contactType:
          type: string
          description: Mapping RefdataCategory "${RDConstants.PERSON_CONTACT_TYPE}"
          enum: <% printRefdataEnum(RDConstants.PERSON_CONTACT_TYPE, 12) %>
        properties: # mapping attr privateProperties
          type: array
          items:
            $ref: "#/components/schemas/Property"
        roles:
          type: array
          items:
            $ref: "#/components/schemas/Role_in_Person"
        title:
          type: string


    Platform:
      allOf:
        - $ref: "#/components/schemas/PlatformStub"
        - type: object
          properties:
            dateCreated:
              type: string
              format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            lastUpdated:
              type: string
              format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            properties: # mapping customProperties
              type: array
              items:
                $ref: "#/components/schemas/Property"
            provider:
              $ref: "#/components/schemas/OrganisationStub"
            serviceProvider:
              type: string
              description: Mapping RefdataCategory "${RDConstants.Y_N}"
              enum: <% printRefdataEnum(RDConstants.Y_N, 14) %>
            softwareProvider:
              type: string
              description: Mapping RefdataCategory "${RDConstants.Y_N}"
              enum: <% printRefdataEnum(RDConstants.Y_N, 14) %>


    Property:
      type: object
      properties:
        scope: # mapping attr descr
          type: string
        paragraph:
          description: Only if scope = "License Property"
          type: string
        token: # mapping attr name
          type: string
        type:
          type: string
          enum: <% println "[" + PropertyDefinition.validTypes.collect{ "\"${it.value['en']}\"" }.join(', ') + "]" %>
        refdataCategory:
          description: Only if type = "Refdata"
          type: string
        note:
          type: string
        isPublic: # derived to substitute tentant
          type: string
          description: Mapping RefdataCategory "${RDConstants.Y_N}". If set *No*, it's an hidden entry to/from the given organisation context
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        value: # mapping attr stringValue, intValue, decValue, refValue, urlValue, dateValue
          type: string


    Subscription:
      allOf:
        - $ref: "#/components/schemas/SubscriptionStub"
      properties:
        costItems:
          $ref: "#/components/schemas/CostItemCollection" # resolved CostItemCollection
        dateCreated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        documents:
          type: array
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        form:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_FORM}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_FORM, 12) %>
        hasPerpetualAccess:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        hasPublishComponent:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        instanceOf:
          $ref: "#/components/schemas/SubscriptionStub"
        isAutomaticRenewAnnually:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        isMultiYear:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        isPublicForApi:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        license: # mapping attr owner
          $ref: "#/components/schemas/LicenseStub"
        manualCancellationDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        orgAccessPoints:
          type: array
          items:
            $ref: "#/components/schemas/OrgAccessPointCollection"
        organisations: # mapping attr orgRelations
          type: array
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual"
        packages:
          type: array
          items:
            $ref: "#/components/schemas/Package_in_Subscription"
        predecessor:
          $ref: "#/components/schemas/SubscriptionStub"
        properties: # mapping customProperties and privateProperties
          type: array
          items:
            $ref: "#/components/schemas/Property"
        resource:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_RESOURCE}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_RESOURCE, 12) %>
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        status:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_STATUS}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_STATUS, 12) %>
        successor:
          $ref: "#/components/schemas/SubscriptionStub"
        kind:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_KIND}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_KIND, 12) %>


    TitleGroup:
      description: Grouping unit for issue entitlements
      properties:
        name:
          type: string
          description: Name of the title group
        issueEntitlements:
          type: array
          description: Issue entitlements contained by the group
          items:
            $ref: "#/components/schemas/IssueEntitlement_in_Subscription"


    TitleInstancePackagePlatform:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Subscription"
      properties:
        package:
          $ref: "#/components/schemas/PackageStub"
        subscription:
          $ref: "#/components/schemas/SubscriptionStub"


<%-- virtual objects --%>
    CostItemCollection:
      type: array
      items:
        type: object
        properties:
          globalUID:
            type: string
            example: "costitem:be3227d3-0d69-4ebd-ac11-906a13d59057"
          calculatedType:
            type: string
            description: Calculated object type
            enum:
              ["${de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}", "${de.laser.interfaces.CalculatedType.TYPE_LOCAL}"]
          billingCurrency:
            type: string
            description: Mapping RefdataCategory "${RDConstants.CURRENCY}"
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: ${de.laser.helper.RDStore.CURRENCY_EUR}
          budgetCodes:
            type: array
            items:
              type: string
              example: "Fonds Chirac"
          copyBase:
            type: string
            example: "costitem:d151f58f-9386-4b27-a65c-7a5cf4888001"
          costInBillingCurrency:
            type: string
          costInBillingCurrencyAfterTax:
            type: string
          costInLocalCurrency:
            type: string
          costInLocalCurrencyAfterTax:
            type: string
          costItemElement:
            type: string
            description: Mapping RefdataCategory "${RDConstants.COST_ITEM_ELEMENT}"
            enum: <% printRefdataEnum(RDConstants.COST_ITEM_ELEMENT, 12) %>
          costItemElementConfiguration:
            type: string
            description: Mapping RefdataCategory "${RDConstants.COST_CONFIGURATION}"
            enum: <% printRefdataEnum(RDConstants.COST_CONFIGURATION, 12) %>
          costItemStatus:
            type: string
            description: Mapping RefdataCategory "${RDConstants.COST_ITEM_STATUS}"
            enum: <% printRefdataEnum(RDConstants.COST_ITEM_STATUS, 12) %>
          costItemCategory:
            type: string
            description: Mapping RefdataCategory "${RDConstants.COST_ITEM_CATEGORY}"
            enum: <% printRefdataEnum(RDConstants.COST_ITEM_CATEGORY, 12) %>
          costTitle:
            type: string
          costDescription:
            type: string
          currencyRate:
            type: string
          dateCreated:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          datePaid:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          endDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          finalCostRounding:
            type: string
          financialYear:
            type: string
          invoiceDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          invoiceNumber:
            type: string
<%--      invoice:
            $ref: "#/components/schemas/Invoice"
          issueEntitlement:
            $ref: "#/components/schemas/IssueEntitlement_in_Subscription" --%>
          lastUpdated:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          orderNumber:
            type: string
<%--      order:
            $ref: "#/components/schemas/Order"
          owner:
            $ref: "#/components/schemas/OrganisationStub" --%>
          package:
            $ref: "#/components/schemas/PackageStub"
          reference:
            type: string
          startDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--      sub:
            $ref: "#/components/schemas/SubscriptionStub" --%>
          subPkg:
            $ref: "#/components/schemas/PackageStub"
          taxCode:
            type: string
            description: Mapping RefdataCategory "${RDConstants.TAX_TYPE}"
            enum: <% printRefdataEnum(RDConstants.TAX_TYPE, 12) %>
          taxRate:
            type: string
            enum:
              [${ CostItem.TAX_RATES.collect{ it }.join(', ') }]

    CoverageCollection:
      type: array
      items:
        type: object
        properties:
          coverageDepth:
            type: string
            definition: Coverage depth; for example, abstracts or full text.
            example: Fulltext
          coverageNote:
            type: string
            definition: Notes; free-text field to describe the specifics of the coverage policy.
            example: "Certain articles are Open Access, see http://www.cambridge.org/core/product/38A3544260A4DA9847F95D6438BF4BE7/open-access"
          endDate:
            type: string
            definition: Date of last issue available online.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2012-12-31T00:00:00"
          endVolume:
            type: string
            definition: Number of last volume available online.
            example: 12
          endIssue:
            type: string
            definition: Number of last issue available online
            example: 5
          embargo:
            type: string
            definition: Embargo info; describes any limitations on when resources become available online.
            example: P2Y
          lastUpdated:
            type: string
            definition: Timestamp of last update of the coverage statement
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-04-04T00:35:15"
          startVolume:
            type: string
            definition: Number of first volume available online.
            example: 1
          startIssue:
            type: string
            definition: Number of first issue available online.
            example: 1
          startDate:
            type: string
            definition: Date of first serial issue available online.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2005-01-01T00:00:00"

    EZProxy_in_OrgAccessPoint:
      type: object
      properties:
        name:
          type: string
        proxyurl:
          type: string
        ipv4ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR
        ipv6ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR

    IP_in_OrgAccessPoint:
      type: object
      properties:
        name:
          type: string
        ipv4ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR
        ipv6ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR

    IssueEntitlement_in_CostItem:
      type: object
      description: An element of the current title holding stock.
      properties:
        globalUID:
          type: string
          definition: A global unique identifier to identify the given issue entitlement.
          example: "issueentitlement:af045a3c-0e32-a681-c21d-3cf17f581d2c"
        accessStartDate:
          type: string
          definition: Point of time when the title joined the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2013-01-01T00:00:00"
        accessEndDate:
          type: string
          definition: Point of time when the title left definitively the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2020-12-31T00:00:00"
        coverages:
          $ref: "#/components/schemas/CoverageCollection"
        lastUpdated:
          type: string
          definition: Timestamp when the issue entitlement has been last updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        medium:
          type: string
          description: The medium type of the issue entitlement; maps to RefdataCategory "${RDConstants.TITLE_MEDIUM}".
          enum: <% printRefdataEnum(RDConstants.TITLE_MEDIUM, 12) %>
          example: ${RefdataValue.getByValueAndCategory(RDConstants.TITLE_MEDIUM, 'Book').value}
        perpetualAccessBySub:
          $ref: "#/components/schemas/SubscriptionStub"
        priceItems:
          $ref: "#/components/schemas/PriceItemCollection"
        status:
          type: string
          description: The status of the issue entitlement; maps to RefdataCategory "${RDConstants.TIPP_STATUS}".
          enum: <% printRefdataEnum(RDConstants.TIPP_STATUS, 12) %>
          example: ${RDStore.TIPP_STATUS_CURRENT}


    IssueEntitlement_in_Subscription:
      allOf:
        - $ref: "#/components/schemas/IssueEntitlement_in_CostItem"
      properties:
        tipp:
          $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Subscription"

    OrgAccessPointCollection:
      type: array
      items:
        $ref: "#/components/schemas/OrgAccessPointStub"

    OA2020_Virtual:
      type: object

    OpenAthens_in_OrgAccessPoint:
      type: object
      properties:
        name:
          type: string
        entityid:
          type: string
        url:
          type: string

    OrganisationRole_Virtual:
      properties:
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        organisation:
          $ref: "#/components/schemas/OrganisationStub"
          description: |
            Exclusive with license, package, subscription and title
        roleType:
          type: string
          description: Mapping RefdataCategory "${RDConstants.ORGANISATIONAL_ROLE}"
          enum: <% printRefdataEnum(RDConstants.ORGANISATIONAL_ROLE, 12) %>
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>


    Package_in_Subscription:
      type: object
      properties:
        globalUID:
          type: string
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        gokbId:
          type: string
        issueEntitlements:
          type: array
          items:
            $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        name:
          type: string


    Package_in_CostItem:
      type: object
      properties:
        globalUID:
          type: string
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        gokbId:
          type: string
        name:
          type: string


    PriceItemCommection:
      type: array
      items:
        type: object
        properties:
          listPrice:
            type: string
            description: TODO
            example: TODO
          listCurrency:
            type: string
            description: TODO; maps to "${RDConstants.CURRENCY}"
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: TODO
          localPrice:
            type: string
            description: TODO
            example: TODO
          localCurrency:
            type: string
            description: TODO; maps to "${RDConstants.CURRENCY}"
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: TODO


    PropertyList:
      type: array
      items:
        type: object
        properties:
          token:
            type: string
            description: Primary Identifier
          scope:
            type: string
            enum:
              [${ PropertyDefinition.AVAILABLE_CUSTOM_DESCR.toList().plus(PropertyDefinition.AVAILABLE_PRIVATE_DESCR.toList()).unique().join(', ') }]
          type:
            type: string
            enum:
              [${ PropertyDefinition.validTypes.collect{ it.value['en'] }.join(', ') }]
          label_de:
            type: string
          label_en:
            type: string
          explanation_de:
            type: string
          explanation_en:
            type: string
          multiple:
            type: string #mapped to boolean
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          usedForLogic:
            type: string #mapped to boolean
            description: Mapping RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          isPublic:
            type: string #mapped to boolean
            description: Mapping RefdataCategory "${RDConstants.Y_N}". If set *No*, it's an hidden entry to/from the given organisation context
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          refdataCategory:
            type: string

    Proxy_in_OrgAccessPoint:
      type: object
      properties:
        name:
          type: string
        ipv4ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR
        ipv6ranges:
          type: array
          items:
            type: object
            properties:
                iprange:
                    type: string
                    format: CIDR

    Refdatas_Virtual:
      type: array
      items:
        type: object
        properties:
          token:
            type: string
            description: Primary Identifier
          label_de:
            type: string
          label_en:
            type: string
          entries:
            type: array
            items:
              type: object
              properties:
                token:
                  type: string
                  description: Primary Identifier
                label_de:
                  type: string
                label_en:
                  type: string
                explanation_de:
                  type: string
                explanation_en:
                  type: string


    Role_in_Person:
      type: object
      properties:
        functionType:
          type: string
          description: Mapping RefdataCategory "${RDConstants.PERSON_FUNCTION}"
          enum: <% printRefdataEnum(RDConstants.PERSON_FUNCTION, 12) %>
        positionType:
          type: string
          description: Mapping RefdataCategory "${RDConstants.PERSON_POSITION}"
          enum: <% printRefdataEnum(RDConstants.PERSON_POSITION, 12) %>


    Statistic_Virtual:
      type: object


    Shibboleth_in_OrgAccessPoint:
      type: object
      properties:
        name:
          type: string
        entityid:
          type: string
        url:
          type: string


    TitleInstancePackagePlatform_in_Package:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatformStub"


    TitleInstancePackagePlatform_in_Subscription:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatformStub"
      properties:
        hostPlatformURL:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        platform:
          $ref: "#/components/schemas/PlatformStub"
        status:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]


<%-- stubs --%>
    OrgAccessPointStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "orgaccesspoint:d64b3dc9-1c1f-4470-9e2b-ae3c341ebc3c"
        type:
          type: array
          items:
            type: string
            description: Mapping RefdataCategory "${RDConstants.ACCESS_POINT_TYPE}"
            enum: <% printRefdataEnum(RDConstants.ACCESS_POINT_TYPE, 12) %>

    OrganisationStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "org:d64b3dc9-1c1f-4470-9e2b-ae3c341ebc3c"
        gokbId:
          type: string
        name:
          type: string
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        type:
          type: array
          items:
            type: string
          description: Mapping RefdataCategory "${RDConstants.ORG_TYPE}"
          enum: <% printRefdataEnum(RDConstants.ORG_TYPE, 12) %>


    LicenseStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "license:7e1e667b-77f0-4495-a1dc-a45ab18c1410"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        reference:
          type: string
        normReference:
          type: string
        calculatedType:
          type: string
          description: Calculated object type
          enum:
            ["Administrative", "Consortial", "Local", "Participation", "Unkown"]


    PackageStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        gokbId:
          type: string
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        name:
          type: string
        sortName:
          type: string


    PlatformStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "platform:9d5c918a-55d0-4197-f22d-a418c14105ab"
        gokbId:
          type: string
        name:
          type: string
        normName:
          type: string
        primaryUrl:
          type: string


    SubscriptionStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "subscription:e96bd7eb-3a00-49c5-bac9-e84d5d335ef1"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        name:
          type: string
        calculatedType:
          type: string
          description: Calculated object type
          enum:
            ["${CalculatedType.TYPE_ADMINISTRATIVE}", "${CalculatedType.TYPE_CONSORTIAL}", "${CalculatedType.TYPE_LOCAL}", "${CalculatedType.TYPE_PARTICIPATION}", "${CalculatedType.TYPE_UNKOWN}"]
          example: "${CalculatedType.TYPE_CONSORTIAL}"


    TitleInstancePackagePlatformStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "titleinstancepackageplatform:9d5c918a-80b5-a121-a7f8-b05ac53004a"
        gokbId:
          type: string
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"


<%-- lists --%>


    CostItemList:
      type: array
      items:
        type: string

    LicenseList:
      type: array
      items:
        $ref: "#/components/schemas/LicenseStub"

    PlatformList:
      type: array
      items:
        $ref: "#/components/schemas/PlatformStub"

    SubscriptionList:
      type: array
      items:
        $ref: "#/components/schemas/SubscriptionStub"


<%-- special objects --%>


    Subscription_KBART:
      type: object
      properties:
        publication_title:
          type: string
          description: Publication title for serial or monograph (see 6.6.2)><br>Conference proceedings series titles should be entered as serial titles, while volume titles should be entered as monograph titles.
          example: Accounting research journal
        print_identifier:
          type: string
          description: Print-format identifier (see 6.6.3)<br>ISSN for serials, ISBN for monographs, etc.<br>Conference proceedings may have serial ISSNs while each proceeding volume may have its own ISBN.
          example: 1030-9616
        online_identifier:
          type: string
          description: Online-format identifier (see 6.6.4)<br>eISSN for serials, eISBN for monographs, etc.<br>Conference proceedings may have serial eISSNs while each proceeding volume many have its own eISBN.
          example: 1839-5465
        date_first_issue_online:
          type: string
          format: date
          description: Date of first serial issue available online (see 6.6.5)<br>Applicable only to serials.
          example: '2005-01-01'
        num_first_vol_online:
          type: string
          description: Number of first volume available online (see 6.6.6)<br>Applicable only to serials.
          example: 18
        num_first_issue_online:
          type: string
          description: Number of first issue available online (see 6.6.7)<br>Applicable only to serials.
          example: 1
        date_last_issue_online:
          type: string
          format: date
          description: Date of last issue available online (see 6.6.8)<br>Leave blank if coverage is to the present.<br>Applicable only to serials.
          example: '2012-12-31'
        num_last_vol_online:
          type: string
          description: Number of last volume available online (see 6.6.9)<br>Leave blank if coverage is to the present.<br>Applicable only to serials.
          example: 12
        num_last_issue_online:
          type: string
          description: Number of last issue available online (see 6.6.10)<br>Leave blank if coverage is to the present.<br>Applicable only to serials.
          example: 5
        title_url:
          type: string
          description: Title-level URL (see 6.6.11)<br>Applicable to both serials and monograph. For conference proceedings, the <i>title_url</i> for the proceedings series and the <i>title_url</i> for each volume should be different.
          example: https://www.emerald.com/insight/publication/issn/1030-9616
        first_author:
          type: string
          description: First author (see 6.6.12)<br>Applicable only to monographs.
          example: Richter
        title_id:
          type: string
          description: Title identifier (see 6.6.13)<br>Applicable to both serials and monographs. For conference proceedings, the <i>title_id</i> for the conference proceedings series and the <i>title_id</i> for each proceeding volume should be different.
          example: arj
        embargo_info:
          type: string
          description: Embargo information (see 6.6.14)´<br>Describes any limitations on when resources become available online.
          example: P2Y
        coverage_depth:
          type: string
          description: Coverage depth (see 6.6.15)<br>For example, abstracts or full text.
          example: Fulltext
        notes:
          type: string
          description: Notes (see 6.6.16)<br>Free-text field to describe the specifics of the coverage policy.
          example: Certain articles are Open Access, see http://www.cambridge.org/core/product/38A3544260A4DA9847F95D6438BF4BE7/open-access
        publisher_name:
          type: string
          description: Publisher name (see 6.6.17)<br>Not to be confused with third-party platform hosting name.
          example: Emerald
        publiction_type:
          type: string
          description: Serial or monograph (see 6.6.18)<br>Use “serial” for journals and conference proceeding series. Use “monograph” for books, e-books, and conference proceeding volumes.
          example: serial
        date_monograph_published_print:
          type: string
          format: date
          description: Date the monograph is first published in print (see 6.6.19).
          example: '1938-01-01'
        date_monograph_published_online:
          type: string
          format: date
          description: Date the monograph is first published online (see 6.6.20).
          example: '2017-01-01'
        monograph_volume:
          type: string
          description: Number of volume for monograph (see 6.6.21)<br>Applicable to e-books and conference proceedings. For proceedings, use the volume within the conference proceedings series.
          example: 1
        monograph_edition:
          type: string
          description: Edition of the monograph (see 6.6.22).
          example: 1
        first_editor:
          type: string
          description: First editor (see 6.6.23)<br>Applicable to monographs, i.e., e-books or conference proceedings volumes.
          example: Nugent
        parent_publication_title_id:
          type: string
          description: Title identifier of the parent publication (see 6.6.24) For a conference proceeding volume, the <i>parent_publication_title_id</i> is the <i>title_id</i> of the conference proceedings series.
          example: RO811
        preceding_publication_title_id:
          type: string
          description: Title identifier of any preceding publication title (see 6.6.25)<br>Applicable to serials and conference proceedings series.
          example: '10.1787/19900414'
        access_type:
          type: string
          description: Access type (see 6.6.26)<br>May be fee-based (P) or Open Access (F).
          example: P
<%-- add fields as defined here: https://dienst-wiki.hbz-nrw.de/display/KOE/KBART+to+LAS%3AeR --%>
        package_name:
          type: string
          description: Official package name of the provider.
          example: Accounting Finance and Economics eJournal Collection
        package_id:
          type: string
          description: Provider-specific identifier of the package.
          example: EAFE
        last_changed:
          type: string
          description: Last change of dataset for automatic updates.
          example: '2021-05-15'
        access_start_date:
          type: string
          format: date
          description: Point in time when the title joined provider package.
          example: '2022-01-21'
        access_end_date:
          type: string
          format: date
          description: Point in time when the title left the package on provider platform completely.
          example: '2022-02-23'
        medium:
          type: string
          description: Detailed medium type of publication.
          example: Journal
        zdb_id:
          type: string
          description: Title ZDB identifier in the Zeitschriftendatenbank (<a href="https://zdb-katalog.de/index.xhtml">German Union Catalogue of Serials</a>).
          example: 2401135-6
        doi_identifier:
          type: string
          description: Digital Object Identifier (<a href="https://www.doi.org/">DOI</a>) of a monograph. Both pure DOI or DOI-Link are possible.
          example: '10.5771/9783845237565'
        ezb_id:
          type: string
          description: Title EZB identifier in the Elektronische Zeitschriftendatenbank (<a href="https://ezb.ur.de/index.phtml?bibid=AAAAA&colors=7&lang=en">Electronic Journals Library</a>).
          example: 101518
        package_isci:
          type: string
          description: International Standard Collection Identifier of package (<a href="https://www.iso.org/obp/ui/#iso:std:iso:27730:ed-1:v1:en">ISO norm</a>).
          example: 01.140.20
        package_isil:
          type: string
          description: Package library identifier (<a href="https://sigel.staatsbibliothek-berlin.de/nc/suche/">ISIL</a>).
          example: ZDB-112-SRM
        package_ezb_anchor:
          type: string
          description: EZB (Electronic Journals Library) anchor of package.
          example: oecd_2019
        ill_indicator:
          type: string
          description: Three-place indicator (1st place a, b, c, d, e; 2nd place x, n, - ; 3rd place p, -).
          example: bn
        superseding_publication_title_id:
          type: string
          description: ID of the title superseding the current one.
          example: '10.1787/24131962'
        monograph_parent_collection_title:
          type: string
          description: Series name.
          example: The Wiley Finance Series
        subject_area:
          type: string
          description: Topics and subject classes (ideally Dewey Decimal Classification).
          example: 'Business, Economics, Finance & Accounting'
        status:
          type: string
          description: One of the current status<br>current = current title<br>expected = expected title, becomes current once available<br>retired = title is not sold actively but still available on the provider platform (e.g. for institutions which have already purchased it)<br>deleted = title is not available any more on the provider platform
          example: Current
        oa_type:
          type: string
          description: Statement about the open access type.
          example: Gold OA
        zdb_ppn:
          type: string
          description: PICA production number of a title record.
          example: 018842836
        ezb_anchor:
          type: string
          description: EZB anchors are distributed by consortia managers for their respective collections in order to represent the subscription unit.
          example: sage_health_2021
        ezb_collection_id:
          type: string
          description: Automatically distributed ID. The collection ID serves as unique identifier upon a call of the entitlement lists of the collections.
          example: EZB-HBZCL-01778
        subscription_isil:
          type: string
          description: Subscription library identifier (<a href="https://sigel.staatsbibliothek-berlin.de/nc/suche/">ISIL</a>).
          example: oecd_2019
        subscription_isci:
          type: string
          description: International Standard Collection Identifier of the underlying subscription (<a href="https://www.iso.org/obp/ui/#iso:std:iso:27730:ed-1:v1:en">ISO norm</a>).
          example: 01.140.20
        listprice_eur:
          type: string
          description: Numeric value of list price in euro.
          example: 48.9
        listprice_gbp:
          type: string
          description: Numeric value of list price in British Pounds.
          example: 143
        listprice_usd:
          type: string
          description: Numeric value of list price in US Dollar.
          example: 190
        localprice_eur:
          type: string
          description: Numeric value of locally negotiated price in euro.
          example: 46.5
        localprice_gbp:
          type: string
          description: Numeric value of locally negotiated price in British Pounds.
          example: 132
        localprice_usd:
          type: string
          description: Numeric value of locally negotiated price in US Dollar.
          example: 170