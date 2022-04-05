<%@ page import="de.laser.RefdataCategory; de.laser.finance.CostItem; de.laser.api.v0.ApiToolkit; de.laser.storage.RDConstants; de.laser.properties.PropertyDefinition" %>
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
        type:
          type: string
          description: Mapping RefdataCategory "${RDConstants.CONTACT_TYPE}"
          enum: <% printRefdataEnum(RDConstants.CONTACT_TYPE, 12) %>


    CostItem:
      type: object
      properties:
        globalUID:
          type: string
          example: "costitem:ab1360cc-147b-d632-2dc8-1a6c56d84b00"
        calculatedType:
          type: string
          description: Calculated object type
          enum:
            ["${de.laser.interfaces.CalculatedType.TYPE_LOCAL}", "${de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}"]
        billingCurrency:
          type: string
          description: Mapping RefdataCategory "Currency"
          enum: <% printRefdataEnum('Currency', 12) %>
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
        costItemStatus:
          type: string
          description: Mapping RefdataCategory "${RDConstants.COST_ITEM_STATUS}"
          enum: <% printRefdataEnum(RDConstants.COST_ITEM_STATUS, 12) %>
<%--    costItemCategory:
          type: string
          description: Mapping RefdataCategory "CostItemCategory"
          enum:
            [${ RefdataCategory.getAllRefdataValues('CostItemCategory').collect{ it.value }.join(', ') }] --%>
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
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        isVisibleForSubscriber:
          type: string #mapped to boolean
          description: Mapping RefdataCategory "${RDConstants.Y_N}"
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
        invoiceDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        invoice:
          $ref: "#/components/schemas/Invoice"
        issueEntitlement:
          $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        order:
          $ref: "#/components/schemas/Order"
        owner:
          $ref: "#/components/schemas/OrganisationStub"
        reference:
          type: string
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        sub:
          $ref: "#/components/schemas/SubscriptionStub"
<%--    subPkg:
         $ref: "#/components/schemas/PackageStub" --%>
        taxCode:
          type: string
          description: Mapping RefdataCategory "${RDConstants.TAX_TYPE}"
          enum: <% printRefdataEnum(RDConstants.TAX_TYPE, 12) %>
        taxRate:
          type: string


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
      properties:
        id:
          type: string
        dateOfPayment:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        dateOfInvoice:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        datePassedToFinance:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        endDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        invoiceNumber:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        startDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
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

<%--
    OnixplLicense:
      type: object
      properties:
        document: # mapping attr doc
          $ref: "#/components/schemas/Document"
        lastmod:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        title:
          type: string
          --%>


    Order:
      type: object
      properties:
        id:
          type: string
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        orderNumber:
          type: string
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
          vendorURL:
            type: string


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
        cancellationAllowances:
          type: string
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
        instanceOf:
          $ref: "#/components/schemas/SubscriptionStub"
        isSlaved:
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
        manualRenewalDate:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        noticePeriod:
          type: string
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
        type:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_TYPE}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_TYPE, 12) %>
        kind:
          type: string
          description: Mapping RefdataCategory "${RDConstants.SUBSCRIPTION_KIND}"
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_KIND, 12) %>


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
            example: "costitem:ab1360cc-147b-d632-2dc8-1a6c56d84b00"
          calculatedType:
            type: string
            description: Calculated object type
            enum:
              ["${de.laser.interfaces.CalculatedType.TYPE_LOCAL}", "${de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}"]
          billingCurrency:
            type: string
            description: Mapping RefdataCategory "Currency"
            enum: <% printRefdataEnum('Currency', 12) %>
          budgetCodes:
            type: array
            items:
              type: string
          copyBase:
            type: string
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
<%--      finalCostRounding:
            type: string --%>
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
            $ref: "#/components/schemas/OrganisationStub"
          package:
            $ref: "#/components/schemas/PackageStub" --%>
          reference:
            type: string
          startDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
<%--      sub:
            $ref: "#/components/schemas/SubscriptionStub"
          subPkg:
            $ref: "#/components/schemas/PackageStub" --%>
          taxCode:
            type: string
            description: Mapping RefdataCategory "${RDConstants.TAX_TYPE}"
            enum: <% printRefdataEnum(RDConstants.TAX_TYPE, 12) %>
          taxRate:
            type: string
            enum:
              [${ CostItem.TAX_RATES.collect{ it }.join(', ') }]

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
        perpetualAccessBySub:
          $ref: "#/components/schemas/SubscriptionStub"
        coverages:
          $ref: "#/components/schemas/CoverageCollection"

    CoverageCollection:
      type: array
      items:
        type: object
        properties:
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
          lastUpdated:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          startVolume:
            type: string
          startIssue:
            type: string
          startDate:
            type: string
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>


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
        vendorURL:
          type: string


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
        delayedOA:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        hostPlatformURL:
          type: string
        hybridOA:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        lastUpdated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        option:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        payment:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        platform:
          $ref: "#/components/schemas/PlatformStub"
        status:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        statusReason:
          type: string
          description: Mapping RefdataCategory
          enum:
            [""]
        title:
          $ref: "#/components/schemas/TitleStub"


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
          example: "subscription:3026078c-bdf1-4309-ba51-a9ea5f7fb234"
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
            ["Administrative", "Consortial", "Local", "Participation", "Unkown"]


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


    TitleStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "title:eeb41a3b-a2c5-0e32-b7f8-3581d2ccf17f"
        gokbId:
          type: string
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        title:
          type: string
        medium:
          type: string
          description: Mapping RefdataCategory "${RDConstants.TITLE_MEDIUM}"
          enum: <% printRefdataEnum(RDConstants.TITLE_MEDIUM, 12) %>
        normTitle:
          type: string


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
