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
          description: Street of address.
          example: Jülicher Straße
        street2:
          type: string
          description: Number of house.
          example: 6
        pob:
          type: string
          description: Name/designator of postal box.
          example: Postfach 27 04 51
        pobZipcode:
          type: string
          description: Postal code of postal box.
          example: 50510
        pobCity:
          type: string
          description: City of postal box.
          example: Köln
        zipcode:
          type: string
          description: Postal code of the address.
          example: 50674
        city:
          type: string
          description: City of address.
          example: Köln
        name:
          type: string
          description: Name or designator of addressee.
          example: Konsortiale Rechnungsstellung
        additionFirst:
          type: string
          description: First addition to address.
          example: Digitale Inhalte
        additionSecond:
          type: string
          description: Second addition to address.
          example: Raum 509
        region:
          type: string
          description: Maps to the RefdataCategory "${RDConstants.REGIONS_DE}", "${RDConstants.REGIONS_AT}" and "${RDConstants.REGIONS_CH}" and is used iff country is set to *DE*, *AT* or *CH*.
          enum: <% printRefdataEnum([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH], 12) %>
          example: North Rhine-Westphalia
        country:
          type: string
          description: The country of address. Setting country to *DE*, *AT* or *CH* enables the usage of region. Maps to the RefdataCategory "${RDConstants.COUNTRY}".
          enum: <% printRefdataEnum(RDConstants.COUNTRY, 12) %>
          example: DE
        type:
          type: array
          description: The type of the address. Maps to the RefdataCategory "${RDConstants.ADDRESS_TYPE}".
          enum: <% printRefdataEnum(RDConstants.ADDRESS_TYPE, 12) %>
          example: ["Billing address"]
          items:
            type: string


    Contact:
      type: object
      properties:
        category: # mapping attr contentType
          type: string
          description: The technical type of contact address. Maps to the RefdataCategory "${RDConstants.CONTACT_CONTENT_TYPE}".
          enum: <% printRefdataEnum(RDConstants.CONTACT_CONTENT_TYPE, 12) %>
          example: E-Mail
        content:
          type: string
          description: The contact datum, i.e. the phone number, mail address, etc.
          example: inhalte@hbz-nrw.de
        language:
          type: string
          description: The language which can be used to contact the address. Maps to the RefdataCategory "${RDConstants.LANGUAGE_ISO}"
          enum: <% printRefdataEnum(RDConstants.LANGUAGE_ISO, 12) %>
          example: ger

    CostItem:
      type: object
      properties:
        globalUID:
          type: string
          description: Global unique identifier for system-wide identification in LAS:eR.
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
        budgetCodes:
          type: array
          description: Units to group cost items. Values are free text.
          items:
            type: string
            example: Fonds Chirac
        copyBase:
          type: string
          description: Cost item UID from which the given cost item has been coped.
          example: "costitem:d151f58f-9386-4b27-a65c-7a5cf4888001"
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
        costItemElementConfiguration:
          type: string
          description: Sign of the cost item (should the value be added, subtracted or left out of the total sum?); maps to the RefdataCategory "${RDConstants.COST_CONFIGURATION}"
          enum: <% printRefdataEnum(RDConstants.COST_CONFIGURATION, 12) %>
          example: ${RDStore.CIEC_POSITIVE.value}
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
        invoiceNumber:
          type: string
          description: An alphanumeric invoice number.
          example: CRC-2022-DT68
        issueEntitlement:
          $ref: "#/components/schemas/IssueEntitlement_in_Subscription"
        lastUpdated:
          type: string
          description: Timestamp when the cost item has been most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-03-30T09:45:20"
        orderNumber:
          type: string
          description: An alphanumeric order designator.
          example: BC-1871-1973
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
          example: "${CostItem.TAX_TYPES.TAXABLE_7.taxType.value}"
        taxRate:
          type: string
          description: The percent value of tax issues to this cost item.
          enum: [${ CostItem.TAX_RATES.collect{ it }.join(', ') }]
          example: 7
        titleGroup:
          $ref: "#/components/schemas/TitleGroup"


    Document:
      type: object
      properties:
        content:
          type: string
          description: Only returned for annotations. Displays the content of the note.
          example: "An diesem Tag wurde der Analysestadtverbund eröffnet."
        filename:
          type: string
          description: The filename of the uploaded document.
          example: "test_sh1.txt"
        mimetype:
          type: string
          description: The MIME type of the uploaded document.
          example: "text/plain"
        lastUpdated:
          type: string
          description: Timestamp when the document has been most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-25T10:35:03"
        title:
          type: string
          description: The name of the document.
          example: Abschlußprotokoll
        type:
          type: string
          description: The type of the document attached. Maps to the RefdataCategory "${RDConstants.DOCUMENT_TYPE}".
          enum: <% printRefdataEnum(RDConstants.DOCUMENT_TYPE, 12) %>
          example: "License"
        uuid:
          type: string
          description: The unique identifier of the file within the system.
          example: "70d4ef8a-71b9-4b39-b339-9f3773c29b26"


    Identifier:
      type: object
      properties:
        namespace:
          type: string
          description: The identifier namespace.
          example: eissn
        value:
          type: string
          description: The value of the identifier.
          example: 2751-1421


    License:
      allOf:
        - $ref: "#/components/schemas/LicenseStub"
      properties:
        dateCreated:
          type: string
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T10:21:45"
        documents:
          type: array
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        instanceOf:
          $ref: "#/components/schemas/LicenseStub"
        lastUpdated:
          type: string
          description: Timestamp when the license has been most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T10:26:07"
        licenseCategory:
          type: string
          description: Mapping RefdataCategory "${RDConstants.LICENSE_CATEGORY}"
          enum: <% printRefdataEnum(RDConstants.LICENSE_CATEGORY, 12) %>
        organisations: # mapping attr orgRelations
          type: array
          description: Organisations related to this license.
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual" # resolved OrgRole
        properties: # mapping customProperties and privateProperties
          type: array
          description: Public and private properties of the calling institution for this license.
          items:
            $ref: "#/components/schemas/Property"
        subscriptions:
          type: array
          description: Set of subscriptions covered by the given license.
          items:
            $ref: "#/components/schemas/SubscriptionStub"


    OrgAccessPoint:
      type: object
      description: "An access point configuration for the calling institution. May be one of the following methods: ezproxy, ip, openathens, proxy or shibboleth."
      properties:
        globalUID:
          type: string
          description: Global unique identifier for system-wide identification in LAS:eR.
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
      description: |
        "A full organisation record. Important is the distinction between "organisations" and "institutions": organisation is used as a superordinate concept to institution; an institution is thus one type of organisation. The term institution implicates the academic context and
        means in technical context of LAS:eR that it may organise itself in the app. Only institutions can thus have user accounts. Apart from institutions like university libraries, universities, research institutions etc., organisations may be commercial like providers, editors or agencies."
      allOf:
        - $ref: "#/components/schemas/OrganisationStub"
      properties:
        addresses:
          type: array
          description: The contact addresses of the given organisation.
          items:
            $ref: "#/components/schemas/Address"
        altNames:
          type: array
          description: A set of alternative names of the organisation.
          example: ["Bloomsbury Journals (formerly Berg Journals)", "Bloomsbury Publishing Plc"]
          items:
            type: string
        contacts:
          type: array
          description: The virtual contact points of the organisation, e.g. e-mails, phone numbers.
          items:
            $ref: "#/components/schemas/Contact"
        country:
          type: string
          description: The country where the organisation is seated. Maps to the RefdataCategory "${RDConstants.COUNTRY}".
          enum: <% printRefdataEnum(RDConstants.COUNTRY, 12) %>
          example: DE
        eInvoice:
          type: string #mapped to boolean
          description: Is the institution connected to the eInvoice portal? Maps to the RefdataCategory "${RDConstants.Y_N}".
          example: Yes
        eInvoicePortal:
          type: string
          description: To which eInvoice portal is the given institution connected? Maps to the RefdataCategory "${RDConstants.E_INVOICE_PORTAL}".
          enum: <% printRefdataEnum(RDConstants.E_INVOICE_PORTAL, 12) %>
          example: E-Rechnungsportal NRW
        lastUpdated:
          type: string
          description: Timestamp when the organisation record has been most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2021-10-05T12:27:47"
        legalPatronName:
          type: string
          description: Name of the legal patron of the institution.
          example: Akademie der Künste
        libraryType:
          type: string
          description: The type of library if the organisation called is a library. Maps to the RefdataCategory "${RDConstants.LIBRARY_TYPE}".
          enum: <% printRefdataEnum(RDConstants.LIBRARY_TYPE, 12) %>
        linkResolverBaseURL:
          type: string
          description: The base URL for the permalink resolver.
          example: "https://www.redi-bw.de/links/fhkn"
        links:
          type: array
          description: Other organisations linked to the given one
          items:
            $ref: "#/components/schemas/Link_Org"
        orgAccessPoints:
          type: array
          description: A set of access point configurations defined by the given institution.
          items:
            $ref: "#/components/schemas/OrgAccessPointCollection"
        persons: # mapping attr prsLinks
          type: array
          description: Contacts of the given organisation.
          items:
            $ref: "#/components/schemas/Person" # resolved PersonRole
        properties: # mapping attr customProperties and privateProperties
          type: array
          description: Set of public and private properties of the calling institution.
          items:
            $ref: "#/components/schemas/Property"
        region:
          type: string
          description: In which German-speaking region/county is the institution seated? Maps to the RefdataCategory "${RDConstants.REGIONS_DE}", "${RDConstants.REGIONS_AT}" and "${RDConstants.REGIONS_CH}".
          enum: <% printRefdataEnum([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH], 12) %>
          example: North Rhine-Westphalia
        retirementDate:
          type: string
          description: Timestamp when the organisation has ceased to be active.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2021-06-30T11:36:44"
        sector:
          type: string
          description: Sector of the organisation. Maps to the RefdataCategory "${RDConstants.ORG_SECTOR}".
          enum: <% printRefdataEnum(RDConstants.ORG_SECTOR, 12) %>
          example: Higher Education
        shortname:
          type: string
          description: Short name or abbreviation of the organisation.
          example: hbz
        sortname:
          type: string
          description: Sort name of the organisation for easier retrieval in lists. Convetion is city, abbreviation.
          example: Köln, hbz
        status:
          type: string
          description: The organisation status. Maps to the RefdataCategory "${RDConstants.ORG_STATUS}".
          enum: <% printRefdataEnum(RDConstants.ORG_STATUS, 12) %>
          example: Current
        subjectGroup:
          type: array
          description: A set of subject groups which are aimed by the given organisation. The values map to the RefdataCategory ${RDConstants.SUBJECT_GROUP}.
          example: ["socialSciences", "artMusicDesign", "teaching", "mathematicsSciences", "medicineHealthScience", "languagesCulturalStudies", "economicsLaw", "civilService"]
          items:
            type: string
        url:
          type: string
          description: Web site of the organisation.
          example: "http://www.ub.uni-koeln.de"
        urlGov:
          type: string
          description: Web site of the organisation governing the given one.
          example: "http://www.uni-koeln.de"

    Package:
      allOf:
      - $ref: "#components/schemas/PackageStub"
      - type: object
        properties:
          breakable:
            type: string
            description: Can the contents of this package be licensed partly or only as a whole? Maps to the RefdataCategory "${RDConstants.Y_N}".
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
            example: ${RDStore.YN_YES.value}
          dateCreated:
            type: string
            description: Timestamp when the package has been recorded in LAS:eR.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-02-08T00:01:50"
          lastUpdated:
            type: string
            description: Timestamp when the package has been most recently updated.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-02-08T00:03:29"
          nominalPlatform:
            description: The default platform for titles contained in this package.
            $ref: "#/components/schemas/Platform"
          organisations: # mapping attr orgs
            type: array
            description: Providers attached to this package.
            items:
              $ref: "#/components/schemas/OrganisationRole_Virtual"
          scope:
            type: string
            description: Scope of validity for the given package, i.e. is it a globally offered set of titles or restricted to countries and/or regions? Maps to the RefdataCategory "${RDConstants.PACKAGE_SCOPE}".
            enum: <% printRefdataEnum(RDConstants.PACKAGE_SCOPE, 12) %>
            example: Global
          packageStatus:
            type: string
            description: Status of the package. Maps to the RefdataCategory "${RDConstants.PACKAGE_STATUS}".
            enum: <% printRefdataEnum(RDConstants.PACKAGE_STATUS, 12) %>
            example: Current
          contentType:
            type: string
            description: The type of content offered in this package. Maps to the RefdataCategory "${RDConstants.PACKAGE_CONTENT_TYPE}".
            enum: <% printRefdataEnum(RDConstants.PACKAGE_CONTENT_TYPE, 12) %>
            example: Book
          tipps:
            type: array
            description: The title stock of this package.
            items:
              $ref: "#/components/schemas/TitleInstancePackagePlatform_in_Package"


    Person:
      type: object
      properties:
        globalUID:
          type: string
          description: global unique identifier for system-wide identification in LAS:eR.
          example: "person:a45a3cf0-f3ad-f231-d5ab-fc1d217f583c"
        addresses:
          type: array
          description: Physical address locations of the given contact.
          items:
            $ref: "#/components/schemas/Address"
        contacts:
          type: array
          description: Virtual contact possibilities (telephone, e-mail address) of the given contact.
          items:
            $ref: "#/components/schemas/Contact"
        firstName:
          type: string
          description: Given name of the person contact.
          example: Ingrid
        gender:
          type: string
          description: Mapping RefdataCategory "${RDConstants.GENDER}"
          enum: <% printRefdataEnum(RDConstants.GENDER, 12) %>
          example: Female
        isPublic:
          type: string
          description: Mapping RefdataCategory "${RDConstants.Y_N}". If set to *No*, it is a hidden entry from the private address book of the requesting institution.
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: ${RDStore.YN_NO.value}
        lastName:
          type: string
          description: Last name of a physical person or functional name of the collective contact.
          example: Meyrath
        middleName:
          type: string
          description: Middle name of the person contact.
          example: E.
        lastUpdated:
          type: string
          description:
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2021-10-05T12:27:47"
        contactType:
          type: string
          description: The type of contact (personal or job-related). Maps to the RefdataCategory "${RDConstants.PERSON_CONTACT_TYPE}".
          enum: <% printRefdataEnum(RDConstants.PERSON_CONTACT_TYPE, 12) %>
          example: ${RDStore.CONTACT_TYPE_JOBRELATED.value}
        properties: # mapping attr privateProperties
          type: array
          description: Properties of the requesting institution for the given contact.
          items:
            $ref: "#/components/schemas/Property"
        roles:
          type: array
          description: Positions or functions the person has.
          items:
            $ref: "#/components/schemas/Role_in_Person"
        title:
          type: string
          description: Form of address for the given person contact
          example: "Prof. Dr."


    Platform:
      allOf:
        - $ref: "#/components/schemas/PlatformStub"
        - type: object
          properties:
            dateCreated:
              type: string
              description: Timestamp when the platform has been created.
              format: <% print ApiToolkit.DATE_TIME_PATTERN %>
              example: "2018-08-13T07:53:53"
            lastUpdated:
              type: string
              description: Timestamp when the platform has been updated most recently.
              format: <% print ApiToolkit.DATE_TIME_PATTERN %>
              example: "2022-03-10T14:52:39"
            properties: # mapping customProperties
              type: array
              items:
                $ref: "#/components/schemas/Property"
            provider:
              description: The provider responsible for this platform.
              $ref: "#/components/schemas/OrganisationStub"


    Property:
      type: object
      description: A public or private property of the given object.
      properties:
        scope: # mapping attr descr
          type: string
          description: The type of object to which the property may be attached to.
          enum: [${ PropertyDefinition.AVAILABLE_CUSTOM_DESCR.toList().plus(PropertyDefinition.AVAILABLE_PRIVATE_DESCR.toList()).unique().join(', ') }]
          example: Subscription Property
        paragraph:
          type: string
          description: The underlying paragraph in the contract serving as proof for the given property value. Displayed iff scope = "License Property"
          example: Bei den teilnehmenden Einrichtungen nach Anlage 2 ist der remote-Zugriff über IP-Adressen eingeschlossen.
        token: # mapping attr name
          type: string
          description: Primary identifier of the property type.
          example: Access choice remote
        type:
          type: string
          description: The type of value for this property.
          enum: <% println "[" + PropertyDefinition.validTypes.collect{ "\"${it.value['en']}\"" }.join(', ') + "]" %>
        refdataCategory:
          type: string
          description: If the scope is *Refdata*, this is the controlled list token (reference data category) whose values are permitted here.
          example: access.choice.remote
        note:
          type: string
          description: Remark to the given property
          example: An diesem Tag wurde der Analysestadtverbund erschaffen
        isPublic: # derived to substitute tentant
          type: string
          description: Maps to the RefdataCategory "${RDConstants.Y_N}". If set to *No*, it is a private entry of the calling institution and only the caller can see the property.
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: ${RDStore.YN_YES.value}
        value: # mapping attr stringValue, intValue, decValue, refValue, urlValue, dateValue
          type: string
          description: the property value
          example: "Zeidel'sches Recht"


    Subscription:
      allOf:
        - $ref: "#/components/schemas/SubscriptionStub"
      properties:
        costItems:
          description: A set of cost items linked to this subscription.
          $ref: "#/components/schemas/CostItemCollection" # resolved CostItemCollection
        dateCreated:
          type: string
          description: Timestamp when the subscription has been created.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T08:07:45"
        documents:
          type: array
          description: A set of documents attached to this subscription.
          items:
            $ref: "#/components/schemas/Document" # resolved DocContext
        form:
          type: string
          description: The form of the subscription. Maps to the RefdataCategory "${RDConstants.SUBSCRIPTION_FORM}".
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_FORM, 12) %>
          example: "ebs"
        hasPerpetualAccess:
          type: string #mapped to boolean
          description: Ensures the subscription perpetual access to the titles subscribed? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: "${RDStore.YN_YES.value}"
        hasPublishComponent:
          type: string #mapped to boolean
          description: Has the subscription a publish component? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: "${RDStore.YN_YES.value}"
        instanceOf:
          description: The subscription instance of the consortium from which this member subscription is derived.
          $ref: "#/components/schemas/SubscriptionStub"
        isAutomaticRenewAnnually:
          type: string #mapped to boolean
          description: Is the subscription renewed for one year once the end date has been reached? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: "${RDStore.YN_NO.value}"
        isMultiYear:
          type: string #mapped to boolean
          description: Does the subscription running time cover several years? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: "${RDStore.YN_NO.value}"
        isPublicForApi:
          type: string #mapped to boolean
          description: Is the subscription enabled for API calls? Maps to the RefdataCategory "${RDConstants.Y_N}".
          enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
          example: "${RDStore.YN_NO.value}"
        kind:
          type: string
          description: The type of the subscription; not to be confounded with calculatedType. Maps to the RefdataCategory "${RDConstants.SUBSCRIPTION_KIND}".
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_KIND, 12) %>
          example: ${RDStore.SUBSCRIPTION_KIND_LOCAL.value}
        lastUpdated:
          type: string
          description: Timestamp when the subscription has been most recently updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T08:17:39"
        license: # mapping attr owner
          description: The underlying license to this subscription.
          $ref: "#/components/schemas/LicenseStub"
        linkedSubscriptions:
          type: array
          description: Other subscriptions related to this subscription instance.
          items:
            $ref: "#/components/schemas/Link_Subscription"
        manualCancellationDate:
          type: string
          description: Date when the notice period ends.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-01-01T00:00:00"
        orgAccessPoints:
          type: array
          description: Access point configurations holding for this subscription.
          items:
            $ref: "#/components/schemas/OrgAccessPointCollection"
        organisations: # mapping attr orgRelations
          type: array
          description: A set of linked organisations to this license. Those can be subscribers, the subscription consortium, providers or agencies related to subscribed titles.
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual"
        packages:
          type: array
          description: A set of linked packages to this subscription.
          items:
            $ref: "#/components/schemas/Package_in_Subscription"
        predecessor:
          description: The subscription instance preceding this subscription.
          $ref: "#/components/schemas/SubscriptionStub"
        properties: # mapping customProperties and privateProperties
          type: array
          description: Set of public and private properties of this subscription.
          items:
            $ref: "#/components/schemas/Property"
        resource:
          type: string
          description: The resource type of the titles covered by this subscription. Maps to the RefdataCategory "${RDConstants.SUBSCRIPTION_RESOURCE}".
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_RESOURCE, 12) %>
          example: "ebookPackage"
        status:
          type: string
          description: The subscription status. Maps to the RefdataCategory "${RDConstants.SUBSCRIPTION_STATUS}".
          enum: <% printRefdataEnum(RDConstants.SUBSCRIPTION_STATUS, 12) %>
          example: ${RDStore.SUBSCRIPTION_CURRENT.value}
        successor:
          description: The subscription instance following to this subscription.
          $ref: "#/components/schemas/SubscriptionStub"


    TitleGroup:
      description: Grouping unit for issue entitlements
      properties:
        name:
          type: string
          description: Name of the title group
          example: Phase 1
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
            description: global unique identifier for system-wide identification in LAS:eR.
            example: "costitem:be3227d3-0d69-4ebd-ac11-906a13d59057"
          calculatedType:
            type: string
            description: Calculated object type of the cost item; internally defined.
            enum:
              ["${de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}", "${de.laser.interfaces.CalculatedType.TYPE_LOCAL}"]
          billingCurrency:
            type: string
            description: The billing currency of the cost item. Maps to the RefdataCategory "${RDConstants.CURRENCY}".
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: ${de.laser.helper.RDStore.CURRENCY_EUR}
          billingSumRounding:
            type: string #mapped to boolean
            description: Should the billing sum be rounded? Maps to the RefdataCategory "${RDConstants.Y_N}".
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
            example: ${RDStore.YN_YES.value}
          budgetCodes:
            type: array
            description: Units to group cost items. Values are free text.
            items:
              type: string
              example: "Fonds Chirac"
          copyBase:
            type: string
            description: Cost item UID from which the given cost item has been coped.
            example: "costitem:d151f58f-9386-4b27-a65c-7a5cf4888001"
          costInBillingCurrency:
            type: string
            description: Numeric value of the sum in billing currency.
            example: 970
          costInBillingCurrencyAfterTax:
            type: string
            description: Numeric value of the sum with included tax in billing currency.
            example: 1154.3
          costInLocalCurrency:
            type: string
            description: Numeric value of the sum in local currency.
            example: 970
          costInLocalCurrencyAfterTax:
            type: string
            description: Numeric value of the sum with included tax in local currency.
            example: 1154.3
          costItemElement:
            type: string
            description: Element of the cost item; maps to the RefdataCategory "${RDConstants.COST_ITEM_ELEMENT}".
            enum: <% printRefdataEnum(RDConstants.COST_ITEM_ELEMENT, 12) %>
            example: "${RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.value}"
          costItemElementConfiguration:
            type: string
            description: Sign of the cost item (should the value be added, subtracted or left out of the total sum?); maps to the RefdataCategory "${RDConstants.COST_CONFIGURATION}"
            enum: <% printRefdataEnum(RDConstants.COST_CONFIGURATION, 12) %>
            example: ${RDStore.CIEC_POSITIVE.value}
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
          invoiceDate:
            type: string
            description: Timestamp when the invoice has been issued.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-03-07T00:00:00"
          invoiceNumber:
            type: string
            description: An alphanumeric invoice number.
            example: CRC-2022-DT68
          lastUpdated:
            type: string
            description: Timestamp when the cost item has been most recently updated.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-03-30T09:45:20"
          orderNumber:
            type: string
            description: An alphanumeric order designator.
            example: BC-1871-1973
          reference:
            type: string
            description: "Alphanumeric reference / code of the given cost item."
            example: C-968-2022
          startDate:
            type: string
            description: Timestamp when the cost item validity span starts.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-01-01T00:00:00"
          taxCode:
            type: string
            description: The type of tax for this cost item; maps to RefdataCategory "${RDConstants.TAX_TYPE}".
            enum: <% printRefdataEnum(RDConstants.TAX_TYPE, 12) %>
            example: "${CostItem.TAX_TYPES.TAXABLE_7.taxRate.value}"
          taxRate:
            type: string
            description: The percent value of tax issues to this cost item.
            enum: [${ CostItem.TAX_RATES.collect{ it }.join(', ') }]
            example: 7

    CoverageCollection:
      type: array
      items:
        type: object
        properties:
          coverageDepth:
            type: string
            description: Coverage depth; for example, abstracts or full text.
            example: Fulltext
          coverageNote:
            type: string
            description: Notes; free-text field to describe the specifics of the coverage policy.
            example: "Certain articles are Open Access, see http://www.cambridge.org/core/product/38A3544260A4DA9847F95D6438BF4BE7/open-access"
          endDate:
            type: string
            description: Date of last issue available online.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2012-12-31T00:00:00"
          endVolume:
            type: string
            description: Number of last volume available online.
            example: 12
          endIssue:
            type: string
            description: Number of last issue available online.
            example: 5
          embargo:
            type: string
            description: Embargo info; describes any limitations on when resources become available online.
            example: P2Y
          lastUpdated:
            type: string
            description: Timestamp of last update of the coverage statement.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2022-04-04T00:35:15"
          startVolume:
            type: string
            description: Number of first volume available online.
            example: 1
          startIssue:
            type: string
            description: Number of first issue available online.
            example: 1
          startDate:
            type: string
            description: Date of first serial issue available online.
            format: <% print ApiToolkit.DATE_TIME_PATTERN %>
            example: "2005-01-01T00:00:00"

    DeweyDecimalClassification:
      type: object
      properties:
        value:
          type: string
          description: Three-digit key of the DDC record. Maps to ${RDConstants.DDC}.
          example: 100
          enum: <% printRefdataEnum(RDConstants.DDC, 12) %>
        value_de:
          type: string
          description: German descriptor of Dewey Decimal Classification
          example: Philosophie und Psychologie
        value_en:
          type: string
          description: English descriptor of Dewey Decimal Classification
          example: Philosophy & psychology

    EZProxy_in_OrgAccessPoint:
      type: object
      description: The EZProxy access point parameters.
      properties:
        name:
          type: string
          description: Name of the access point.
          example: Forschungsnetz (EZProxy)
        proxyurl:
          type: string
          description: The Proxy URL.
          example: https://ezproxy.hzb-nrw.de/login?url=https://www.hbz-nrw.de
        ipv4ranges:
          type: array
          description: A set of IPv4 proxy ranges.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv4 proxy address range.
                    format: CIDR
                    example: "193.111.0.0/16"
        ipv6ranges:
          type: array
          description: A set of IPv6 proxy ranges.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv6 proxy address range.
                    format: CIDR
                    example: "1973:ed00:0000:0000:0000:0000:0000:0000/24"

    IP_in_OrgAccessPoint:
      type: object
      description: The IP access point parameters.
      properties:
        name:
          type: string
          description: Name of the access point.
          example: Campusnetz mit Remote Access
        ipv4ranges:
          type: array
          description: A set of permitted IPv4 ranges.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv4 address range.
                    format: CIDR
                    example: "194.95.250.30/32"
        ipv6ranges:
          type: array
          description: A set of permitted IPv6 ranges.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv6 address range.
                    format: CIDR
                    example: "2001:db8::/47"

    IssueEntitlement_in_CostItem:
      type: object
      description: An element of the current title holding stock.
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given issue entitlement.
          example: "issueentitlement:af045a3c-0e32-a681-c21d-3cf17f581d2c"
        accessStartDate:
          type: string
          description: Point of time when the title joined the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2013-01-01T00:00:00"
        accessEndDate:
          type: string
          description: Point of time when the title left definitively the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2020-12-31T00:00:00"
        coverages:
          $ref: "#/components/schemas/CoverageCollection"
        lastUpdated:
          type: string
          description: Timestamp when the issue entitlement has been last updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01T08:16:17"
        medium:
          type: string
          description: The medium type of the issue entitlement; maps to RefdataCategory "${RDConstants.TITLE_MEDIUM}".
          enum: <% printRefdataEnum(RDConstants.TITLE_MEDIUM, 12) %>
          example: ${RDStore.TITLE_TYPE_EBOOK.value}
        name:
          type: string
          description: Name of the title.
          example: Accounting reseach journal
        perpetualAccessBySub:
          description: Subscription granting perpetual access for the given issue entitlement.
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

    Link_License:
      type: object
      properties:
        linktype:
          type: string
          description: Type of the link between two licenses
          example: references
        license:
          $ref: "#/components/schemas/LicenseStub"

    Link_Org:
      type: object
      properties:
        linktype:
          type: string
          description: Type of the link between two organisations
          example: ${RDStore.COMBO_TYPE_FOLLOWS.value}
        org:
          $ref: "#/components/schemas/OrganisationStub"

    Link_Subscription:
      type: object
      properties:
        linktype:
          type: string
          description: Type of the link between two subscriptions
          example: references
        subscription:
          $ref: "#/components/schemas/SubscriptionStub"

    OrgAccessPointCollection:
      type: array
      description: A list of access point configurations for the given context institution.
      items:
        $ref: "#/components/schemas/OrgAccessPointStub"

    OpenAthens_in_OrgAccessPoint:
      type: object
      description: The OpenAthens access point parameters.
      properties:
        name:
          type: string
          description: Name of the access point.
          example: Forschungsnetz (OpenAthens)
        entityid:
          type: string
          description: The entity ID of the access point.
          example: ASVB

    OrganisationRole_Virtual:
      description: A relation of a license, package, subscription or title instance with an organisation. Only one of license, package, subscription and title can be set for one link.
      properties:
        endDate:
          type: string
          description: End date of the duration of this organisational relation.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "1998-12-01 00:00:00"
        organisation:
          $ref: "#/components/schemas/OrganisationStub"
        roleType:
          type: string
          description: The type of linking. Maps to the RefdataCategory "${RDConstants.ORGANISATIONAL_ROLE}"
          enum: <% printRefdataEnum(RDConstants.ORGANISATIONAL_ROLE, 12) %>
          example: ${RDStore.OR_SUBSCRIBER_CONS.value}
        startDate:
          type: string
          description: Start date of the duration of this organisational relation.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2019-12-31 00:00:00"


    Package_in_Subscription:
      allOf:
        - $ref: "#/components/schemas/Package_in_CostItem"
      properties:
        issueEntitlements:
          type: array
          items:
            $ref: "#/components/schemas/IssueEntitlement_in_Subscription"


    Package_in_CostItem:
      type: object
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given package in LAS:eR.
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        gokbId:
          type: string
          description: The identifier of the package in the we:kb knowledge base. Naming gokbId is legacy.
          example: 2f045791-aaad-42bf-9563-9a3ca39a09a7
        name:
          type: string
          description: Name of the package.
          example: ACS eBooks Complete Collection


    PriceItemCollection:
      type: array
      items:
        type: object
        properties:
          listPrice:
            type: string
            description: Numeric value of list price.
            example: 143
          listCurrency:
            type: string
            description: Currency of list price; maps to "${RDConstants.CURRENCY}"
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: GBP
          localPrice:
            type: string
            description: Numeric value of locally negotiated price.
            example: 46.5
          localCurrency:
            type: string
            description: Currency of locally negotiated price; maps to "${RDConstants.CURRENCY}"
            enum: <% printRefdataEnum(RDConstants.CURRENCY, 12) %>
            example: EUR


    PropertyList:
      type: array
      description: List of public and/or private properties.
      items:
        type: object
        properties:
          token:
            type: string
            description: Primary identifier of the property type.
            example: Access choice remote
          scope:
            type: string
            description: The type of object to which the property may be attached to.
            enum: [${ PropertyDefinition.AVAILABLE_CUSTOM_DESCR.toList().plus(PropertyDefinition.AVAILABLE_PRIVATE_DESCR.toList()).unique().join(', ') }]
            example: Subscription Property
          type:
            type: string
            description: Data type of property value.
            enum: [${ PropertyDefinition.validTypes.collect{ it.value['en'] }.join(', ') }]
            example: Refdata
          label_de:
            type: string
            description: German name of the property type.
            example: Zugangswahl Remote
          label_en:
            type: string
            description: English name of the property type.
            example: Access choice remote
          explanation_de:
            type: string
            description: German explanatory text for this property type.
            example: Bitte geben Sie hier an, ob Sie 2FA, Zugang für Wissenschaftler oder kein remote Zugang wünschen?
          explanation_en:
            type: string
            description: English explanatory text for this property type.
            example: Please indicate here whether you want 2FA, access for scientists or no remote access?
          multiple:
            type: string #mapped to boolean
            description: Can the property be attached several times to the same object? Maps to the RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
            example: ${RDStore.YN_YES.value}
          usedForLogic:
            type: string #mapped to boolean
            description: Is the property used for evaluations inside the program logic? Maps to the RefdataCategory "${RDConstants.Y_N}"
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
            example: ${RDStore.YN_NO.value}
          isPublic:
            type: string #mapped to boolean
            description: Maps to the RefdataCategory "${RDConstants.Y_N}". If set *No*, it is a private entry of the owner institution and only this owner institution can call this property.
            enum: <% printRefdataEnum(RDConstants.Y_N, 12) %>
            example: ${RDStore.YN_YES.value}
          refdataCategory:
            type: string
            description: If the scope is *Refdata*, this is the controlled list token (reference data category) whose values are permitted here.
            example: access.choice.remote

    Proxy_in_OrgAccessPoint:
      type: object
      description: The proxy access point parameters.
      properties:
        name:
          type: string
          description: Name of the access point.
          example: Forschungsnetz (Proxy)
        ipv4ranges:
          type: array
          description: A set of IPv4 ranges suitable as proxy access points.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv4 address range.
                    format: CIDR
                    example: "193.111.0.0/16"
        ipv6ranges:
          type: array
          description: A set of IPv6 ranges suitable as proxy access points.
          items:
            type: object
            properties:
                iprange:
                    type: string
                    description: An IPv6 address range.
                    format: CIDR
                    example: "1973:ed01:0000:0000:0000:0000:0000:0000/48"

    Refdatas_Virtual:
      type: array
      description: A value of a controlled list. See /refdataList for a complete set of controlled lists and their values.
      items:
        type: object
        properties:
          token:
            type: string
            description: Primary identifier for the reference data category.
            example: sis.business.model.base
          label_de:
            type: string
            description: The German name of the reference data category.
            example: FID-Geschäftsmodell - Grundtyp
          label_en:
            type: string
            description: The English name of the reference data category.
            example: SIS business model - base type
          entries:
            type: array
            items:
              type: object
              properties:
                token:
                  type: string
                  description: Primary identifier for the reference value.
                  example: user-driven
                label_de:
                  type: string
                  description: German label of the reference value.
                  example: nutzergesteuerte Erwerbung
                label_en:
                  type: string
                  description: English label of the reference value.
                  example: user-driven acquisition
                explanation_de:
                  type: string
                  description: German explanation text for the reference value.
                  example: "Nutzergesteuerte Erwerbung: Bei nutzergesteuerten Erwerbungsmodellen entscheiden die Nutzer durch ihr Zugriffsverhalten, ob einzelne Publikationen erworben werden oder nicht. Dabei werden die Dokumente entweder laufend erworben (Patron-Driven Acquisition) oder nach Ablauf eines definierten Zeitraums werden Erwerbungsentscheidungen insbesondere anhand der Zugriffszahlen in einem definierten Kostenrahmen getroffen (Evidence-Based Selection)."
                explanation_en:
                  type: string
                  description: English explanation text for the reference value.
                  example: "User-driven acquisition: In user-driven purchase models, the users decide by their access behavior whether single publications are going to be purchased or not. The documents are being purchased either continuously (Patron-Driven Acquisition) or after the expiry of a defined time span, purchase decisions are being taken in a defined cost frame, particularly according to the access counts (Evidence-Based Selection)."


    Role_in_Person:
      type: object
      properties:
        functionType:
          type: string
          description: The function type of the person contact. Is exclusive with positionType. Maps to the RefdataCategory "${RDConstants.PERSON_FUNCTION}"
          enum: <% printRefdataEnum(RDConstants.PERSON_FUNCTION, 12) %>
          example: General contact person
        positionType:
          type: string
          description: The position type of the person contact. Is exclusive with functionType. Maps to the RefdataCategory "${RDConstants.PERSON_POSITION}"
          enum: <% printRefdataEnum(RDConstants.PERSON_POSITION, 12) %>
          example: Technical Support

    Shibboleth_in_OrgAccessPoint:
      type: object
      description: The Shibboleth access point parameters.
      properties:
        name:
          type: string
          description: Name of the access point.
          example: Forschungsnetz (Shibboleth)
        entityid:
          type: string
          description: The Shibboleth entity ID.
          example: ASVB

    # here: all fields which hold for the global level
    TitleInstancePackagePlatform_in_Package:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatformStub"
      properties:
        accessStartDate:
          type: string
          description: Point of time when the title joined the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2013-01-01T00:00:00"
        accessEndDate:
          type: string
          description: Point of time when the title left definitively the currently subscribed stock.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2020-12-31T00:00:00"
        coverages:
          $ref: "#/components/schemas/CoverageCollection"
        lastUpdated:
          type: string
          description: Timestamp when the issue entitlement has been last updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
        medium:
          type: string
          description: The medium type of the issue entitlement; maps to RefdataCategory "${RDConstants.TITLE_MEDIUM}".
          enum: <% printRefdataEnum(RDConstants.TITLE_MEDIUM, 12) %>
          example: ${RDStore.TITLE_TYPE_EBOOK.value}
        name:
          type: string
          description: Name of the title.
          example: Accounting reseach journal
        priceItems:
          $ref: "#/components/schemas/PriceItemCollection"
        status:
          type: string
          description: The global title status. Maps to the RefdataCategory "${RDConstants.TIPP_STATUS}"
          enum: <% printRefdataEnum(RDConstants.TIPP_STATUS, 12) %>
          example: ${RDStore.TIPP_STATUS_CURRENT}

    # exclude here every field which are contained by IssueEntitlement!
    TitleInstancePackagePlatform_in_Subscription:
      allOf:
        - $ref: "#/components/schemas/TitleInstancePackagePlatformStub"


<%-- stubs --%>
    OrgAccessPointStub:
      type: object
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given organisation access point in LAS:eR.
          example: "orgaccesspoint:d64b3dc9-1c1f-4470-9e2b-ae3c341ebc3c"
        type:
          type: array
          items:
            type: string
            description: The type of access point, maps to the RefdataCategory "${RDConstants.ACCESS_POINT_TYPE}"
            enum: <% printRefdataEnum(RDConstants.ACCESS_POINT_TYPE, 12) %>

    OrganisationStub:
      type: object
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given organisation in LAS:eR.
          example: "org:e6be24ff-98e4-474d-9ef8-f0eafd843d17"
        gokbId:
          type: string
          description: "The identifier of the organisation in the we:kb knowledge base. Naming gokbId is legacy. Only providers or agencies may have we:kb-IDs as they are managed there; institutions have none as they are created and managed in LAS:eR only."
          example: f6ab152e-ce10-42cd-b9e5-44a2d41fc7f3
        name:
          type: string
          description: Name of the organisation.
          example: Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen
        identifiers: # mapping attr ids
          type: array
          description: Further set of identifiers of the organisation.
          items:
            $ref: "#/components/schemas/Identifier"
        type:
          type: array
          description: Describing the type of the organisation. Maps to the RefdataCategory "${RDConstants.ORG_TYPE}"
          items:
            type: string
          enum: <% printRefdataEnum(RDConstants.ORG_TYPE, 12) %>
          example: ["Provider", "Agency"]


    LicenseStub:
      type: object
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given license.
          example: "license:7e1e667b-77f0-4495-a1dc-a45ab18c1410"
        endDate:
          type: string
          description: End date of license running time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2017-04-30T00:00:00"
        identifiers: # mapping attr ids
          type: array
          description: Further set of identifiers of the license.
          items:
            $ref: "#/components/schemas/Identifier"
        reference:
          type: string
          description: Name of the license.
          example: Analysestadtverbund-Grundvertrag
        startDate:
          type: string
          description: Start date of license running time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2012-02-16T00:00:00"
        status:
          type: string
          description: Status of the license. Maps to the RefdataCategory "${RDConstants.LICENSE_STATUS}".
          enum: <% printRefdataEnum(RDConstants.LICENSE_STATUS, 12) %>
          example: Current
        calculatedType:
          type: string
          description: Calculated object type
          enum: ["${CalculatedType.TYPE_CONSORTIAL}", "${CalculatedType.TYPE_LOCAL}", "${CalculatedType.TYPE_PARTICIPATION}", "${CalculatedType.TYPE_UNKOWN}"]
          example: ${CalculatedType.TYPE_LOCAL}


    PackageStub:
      type: object
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given package in LAS:eR.
          example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
        gokbId:
          type: string
          description: The identifier of the package in the we:kb knowledge base. Naming gokbId is legacy.
          example: 2f045791-aaad-42bf-9563-9a3ca39a09a7
        identifiers: # mapping attr ids
          type: array
          description: Further set of identifiers of the package.
          items:
            $ref: "#/components/schemas/Identifier"
        name:
          type: string
          description: Name of the package.
          example: ACS eBooks Complete Collection


    PlatformStub:
      type: object
      description: A record for a platform hosting one or more title instances.
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given platform in LAS:eR.
          example: "platform:9d5c918a-55d0-4197-f22d-a418c14105ab"
        gokbId:
          type: string
          description: The identifier of the platform in the we:kb knowledge base. Naming gokbId is legacy.
          example: ac8cc77d-87b8-477e-b99b-ca6320f11aa0
        name:
          type: string
          description: Name of the platform.
          example: meiner-elibrary.de
        primaryUrl:
          type: string
          description: Primary URL of the host platform.
          example: https://meiner-elibrary.de


    SubscriptionStub:
      type: object
      description: A subscription record for an electronic resource.
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given subscription.
          example: "subscription:e96bd7eb-3a00-49c5-bac9-e84d5d335ef1"
        identifiers: # mapping attr ids
          type: array
          description: Further set of identifiers of the subscription.
          items:
            $ref: "#/components/schemas/Identifier"
        startDate:
          type: string
          description: Start date of the subscription running time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "${ApiToolkit.getStartOfYearRing()}"
        endDate:
          type: string
          description: End date of the subscription running time.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "${ApiToolkit.getEndOfYearRing()}"
        name:
          type: string
          description: Name of the subscription.
          example: "Preselect AllIn"
        calculatedType:
          type: string
          description: Calculated object type. Internally defined by the underlying institution relations.
          enum:
            ["${CalculatedType.TYPE_ADMINISTRATIVE}", "${CalculatedType.TYPE_CONSORTIAL}", "${CalculatedType.TYPE_LOCAL}", "${CalculatedType.TYPE_PARTICIPATION}", "${CalculatedType.TYPE_UNKOWN}"]
          example: "${CalculatedType.TYPE_CONSORTIAL}"


    TitleInstancePackagePlatformStub:
      type: object
      description: A title instance record.
      properties:
        globalUID:
          type: string
          description: A global unique identifier to identify the given title instance.
          example: "titleinstancepackageplatform:9d5c918a-80b5-a121-a7f8-b05ac53004a"
        accessType:
          type: string
          description: Access type. May be fee-based or Open Access. Maps to RefdataCategory ${RDConstants.TIPP_ACCESS_TYPE}
          enum: <% printRefdataEnum(RDConstants.TIPP_ACCESS_TYPE, 12) %>
          example: Paid
        altnames:
          type: array
          description: List of alternative names for this title instace.
          example: [40 Years of Chemometrics – From Bruce Kowalski to the Future, 40 Years of Chemometrics]
          items:
            type: string
        dateFirstInPrint:
          type: string
          description: Date the monograph is first published in print.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "1932-01-01 00:00:00"
        dateFirstOnline:
          type: string
          description: Date the monograph is first published online.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2021-01-01 00:00:00"
        ddcs:
          type: array
          description: List of Dewey Decimal Classification entries for the given title instance.
          items:
            $ref: "#/components/schemas/DeweyDecimalClassification"
        editionStatement:
          type: string
          description: Edition for monograph.
          example: 3
        firstAuthor:
          type: string
          description: First author. Only applicable to monographs.
          example: Devons
        firstEditor:
          type: string
          description: First editor. Applicable to monographs, i.e., e-books or conference proceedings volumes.
          example: Lavine
        gokbId:
          type: string
          description: The identifier of the title instance in the we:kb knowledge base. Naming gokbId is legacy.
          example: 44a264a7-c570-4f5b-a6a9-ff80b4e3fee4
        hostPlatformURL:
          type: string
          description: Access URL for title
          example: "https://www.emerald.com/insight/publication/issn/1030-9616"
        identifiers: # mapping attr ids
          type: array
          description: Further set of identifiers of the title.
          items:
            $ref: "#/components/schemas/Identifier"
        languages:
          type: array
          description: List of ISO 639-2 language keys for the given title instance.
          example: ["ger", "eng"]
          items:
            type: string
        lastUpdated:
          type: string
          description: Timestamp when the title has been last updated.
          format: <% print ApiToolkit.DATE_TIME_PATTERN %>
          example: "2022-04-01 23:14:41"
        openAccess:
          type: string
          description: Statement to indicate the type of Open Access.
          enum: <% printRefdataEnum(RDConstants.LICENSE_OA_TYPE, 12) %>
          example: Gold OA
        platform:
          $ref: "#/components/schemas/PlatformStub"
        publisherName:
          type: string
          description: Publisher name. Not to be confused with third-party platform hosting name.
          example: American Chemical Society
        providers:
          type: array
          description: List of provider and agency organisations related to this title.
          items:
            $ref: "#/components/schemas/OrganisationRole_Virtual"
        seriesName:
          type: string
          description: Series name.
          example: The Wiley Finance Series
        subjectReference:
          type: string
          description: Topics and subject classes (ideally Dewey Decimal Classification).
          example: Agriculture, Aquaculture & Food Science
        titleType:
          type: string
          description: Statement about the title type.
          enum: ["Book", "Database", "Journal", "Other"]
          example: Book
        volume:
          type: string
          description: Number of volume for monograph.
          example: XXXIII


<%-- lists --%>


    CostItemList:
      type: array
      description: List of cost item global identifiers.
      example: ["costitem:b5fc0542-f55f-4590-812f-91d93da01eff","costitem:6465e2b6-4534-4dd5-85fe-cf7b2b7ec6e5","costitem:ce573149-287b-4e27-a84c-20be3513198e"]
      items:
        type: string
        example: "costitem:b5fc0542-f55f-4590-812f-91d93da01eff"

    LicenseList:
      type: array
      description: List of license excerpts.
      items:
        $ref: "#/components/schemas/LicenseStub"

    PlatformList:
      type: array
      description: List of platform excerpts.
      items:
        $ref: "#/components/schemas/PlatformStub"

    SubscriptionList:
      type: array
      description: List of subscription excerpts.
      items:
        $ref: "#/components/schemas/SubscriptionStub"


<%-- special objects --%>


    Subscription_KBART:
      type: object
      description: A subscription record for an electronic resource, displayed in the KBART tab-separated value format. It is an object specially designed for the Elektronische Zeitschriftendatenbank (Electronic Journal Library).
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