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
            ["Template", "Local", "Consortial", "Participation", "Unkown"]
        billingCurrency:
          type: string
          description: Mapping RefdataCategory "Currency"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Currency').collect{ it.value }.join(', ') }]
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
          description: Mapping RefdataCategory "CostItemElement"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('CostItemElement').collect{ it.value }.join(', ') }]
        costItemStatus:
          type: string
          description: Mapping RefdataCategory "CostItemStatus"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('CostItemStatus').collect{ it.value }.join(', ') }]
      #  costItemCategory:
      #    type: string
      #    description: Mapping RefdataCategory "CostItemCategory"
      #    enum:
      #      [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('CostItemCategory').collect{ it.value }.join(', ') }]
        costTitle:
          type: string
        costDescription:
          type: string
        currencyRate:
          type: string
        dateCreated:
          type: string
          format: date
        datePaid:
          type: string
          format: date
        endDate:
          type: string
          format: date
        finalCostRounding:
          type: string
        invoiceDate:
          type: string
          format: date
        invoice:
          $ref: "#/components/schemas/Invoice"
        issueEntitlement:
          $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        lastUpdated:
          type: string
          format: date
        order:
          $ref: "#/components/schemas/Order"
        owner:
          $ref: "#/components/schemas/OrganisationStub"
        reference:
          type: string
        startDate:
          type: string
          format: date
        sub:
          $ref: "#/components/schemas/SubscriptionStub"
      #  subPkg:
      #    $ref: "#/components/schemas/PackageStub"
        taxCode:
          type: string
          description: Mapping RefdataCategory "TaxType"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('TaxType').collect{ it.value }.join(', ') }]
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
        title:
          type: string
        type:
          type: string
          description: Mapping RefdataCategory "Document Type"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Document Type').collect{ it.value }.join(', ') }]
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
          format: date
        dateOfInvoice:
          type: string
          format: date
        datePassedToFinance:
          type: string
          format: date
        endDate:
          type: string
          format: date
        invoiceNumber:
          type: string
        startDate:
          type: string
          format: date
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
          format: date
        accessEndDate:
          type: string
          format: date
        coreStatusStart:
          type: string
          format: date
        coreStatusEnd:
          type: string
          format: date
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
          format: date
        endVolume:
          type: string
        endIssue:
          type: string
        embargo:
          type: string
        ieReason:
          type: string
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
          format: date
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
          format: date
        documents:
          type: array
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        endDate:
          type: string
          format: date
        instanceOf:
          $ref: "#/components/schemas/LicenseStub"
        isPublic:
          type: string
          description: Mapping RefdataCategory "YN"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
        lastUpdated:
          type: string
          format: date
        licenseStatus:
          type: string
        licenseType:
          type: string
        onixplLicense:
          $ref: "#/components/schemas/OnixplLicense"
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
          format: date
        status:
          type: string
          description: Mapping RefdataCategory "License Status"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('License Status').collect{ it.value }.join(', ') }]
        subscriptions:
          type: array
          items:
            $ref: "#/components/schemas/SubscriptionStub"


    OnixplLicense:
      type: object
      properties:
        document: # mapping attr doc
          $ref: "#/components/schemas/Document"
        lastmod:
          type: string
          format: date
        title:
          type: string


    Order:
      type: object
      properties:
        id:
          type: string
        orderNumber:
          type: string
        owner:
          $ref: "#/components/schemas/OrganisationStub"


    Property:
      type: object
      properties:
        description: # mapping attr descr
          type: string
        explanation: # mapping attr expl
          type: string
        paragraph: # only if license preoperties
          type: string
        name:
          type: string
        note:
          type: string
        isPublic: # derived to substitute tentant
          type: string
          description: Mapping RefdataCategory "YN". If set *No*, it's an hidden entry to/from the given organisation context
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
        value: # mapping attr stringValue, intValue, decValue, refValue, urlValue, dateValue
          type: string


    Subscription:
      allOf:
        - $ref: "#/components/schemas/SubscriptionStub"
      properties:
        cancellationAllowances:
          type: string
        dateCreated:
          type: string
          format: date
        documents:
          type: array
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        endDate:
          type: string
          format: date
        form:
          type: string
          description: Mapping RefdataCategory "Subscription Form"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Subscription Form').collect{ it.value }.join(', ') }]
        instanceOf:
          $ref: "#/components/schemas/SubscriptionStub"
        isSlaved:
          type: string
          description: Mapping RefdataCategory "YN"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
        lastUpdated:
          type: string
          format: date
        license: # mapping attr owner
          $ref: "#/components/schemas/LicenseStub"
        manualCancellationDate:
          type: string
          format: date
        manualRenewalDate:
          type: string
          format: date
        noticePeriod:
          type: string
        organisations: # mapping attr orgRelations
          type: array
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual"
        packages:
          type: array
          items:
            $ref: "#/components/schemas/Package_in_Subscription"
        previousSubscription:
          $ref: "#/components/schemas/SubscriptionStub"
        properties: # mapping customProperties and privateProperties
          type: array
          items:
            $ref: "#/components/schemas/Property"
        resource:
          type: string
          description: Mapping RefdataCategory "Subscription Resource"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Subscription Resource').collect{ it.value }.join(', ') }]
        startDate:
          type: string
          format: date
        status:
          type: string
          description: Mapping RefdataCategory "Subscription Status"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Subscription Status').collect{ it.value }.join(', ') }]
        type:
          type: string
          description: Mapping RefdataCategory "Subscription Type"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Subscription Type').collect{ it.value }.join(', ') }]


    TitleInstancePackagePlatform:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Subscription"
      properties:
        package:
          $ref: "#/components/schemas/PackageStub"
        subscription:
          $ref: "#/components/schemas/SubscriptionStub"


<%-- virtual objects --%>

    IssueEntitlement_in_CostItem:
      type: object
      properties:
        globalUID:
          type: string
          example: "issueentitlement:af045a3c-0e32-a681-c21d-3cf17f581d2c"
        accessStartDate:
          type: string
          format: date
        accessEndDate:
          type: string
          format: date
        coreStatusStart:
          type: string
          format: date
        coreStatusEnd:
          type: string
          format: date
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
          format: date
        endVolume:
          type: string
        endIssue:
          type: string
        embargo:
          type: string
        ieReason:
          type: string
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
          format: date


    IssueEntitlement_in_Subscription:
      allOf:
        - $ref: "#/components/schemas/IssueEntitlement_in_CostItem"
      properties:
        tipp:
          $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Subscription"


    OrganisationRole_Virtual:
      properties:
        endDate:
          type: string
          format: date
        organisation:
          $ref: "#/components/schemas/OrganisationStub"
          description: |
            Exclusive with cluster, license, package, subscription and title
        roleType:
          type: string
          description: Mapping RefdataCategory "Organisational Role"
          enum:
            [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Organisational Role').collect{ it.value }.join(', ') }]
        startDate:
          type: string
          format: date


    Package_in_Subscription:
      type: object
      properties:
        globalUID:
          type: string
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        issueEntitlements:
          type: array
          items:
            $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        name:
          type: string
        vendorURL:
          type: string


    Refdatas_Virtual:
      type: array
      items:
        type: object
        properties:
          desc:
            type: string
          label_de:
            type: string
          label_en:
            type: string
          entries:
            type: array
            items:
              type: object
              properties:
                value:
                  type: string
                label_de:
                  type: string
                label_en:
                  type: string


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


    OrganisationStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "org:d64b3dc9-1c1f-4470-9e2b-ae3c341ebc3c"
        name:
          type: string
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"


    LicenseStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "license:7e1e667b-77f0-4495-a1dc-a45ab18c1410"
        impId:
          type: string
          example: "47bf5716-af45-7b7d-bfe1-189ab51f6c66"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        reference:
          type: string
        normReference:
          type: string
        calculatedType:
          type: string
          description: Calculated object type
          enum:
            ["Template", "Local", "Consortial", "Participation", "Unkown"]


    PackageStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        impId:
          type: string
          example: "e6b41905-f1aa-4d0c-8533-e39f30220f65"
        name:
          type: string


    PlatformStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "platform:9d5c918a-55d0-4197-f22d-a418c14105ab"
        impId:
          type: string
          example: "9d5c918a-851f-4639-a6a1-e2dd124c2e02"
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
        impId:
          type: string
          example: "ff74dd15-e27f-48a2-b2d7-f02389e62639"
        name:
          type: string
        calculatedType:
          type: string
          description: Calculated object type
          enum:
            ["Template", "Local", "Consortial", "Participation", "Unkown"]


    TitleInstancePackagePlatformStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "titleinstancepackageplatform:9d5c918a-80b5-a121-a7f8-b05ac53004a"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        impId:
          type: string
          example: "c920188c-a7f8-54f6-80b5-e0161df3d360"


    TitleStub:
      type: object
      properties:
        globalUID:
          type: string
          example: "title:eeb41a3b-a2c5-0e32-b7f8-3581d2ccf17f"
        identifiers: # mapping attr ids
          type: array
          items:
            $ref: "#/components/schemas/Identifier"
        impId:
          type: string
          example: "daccb411-e7c6-4048-addf-1d2ccf35817f"
        title:
          type: string
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


    SubscriptionList:
      type: array
      items:
        $ref: "#/components/schemas/SubscriptionStub"
