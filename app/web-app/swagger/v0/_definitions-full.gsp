
  ### full ###

<%--
  Address:
    type: object
    properties:
      street1:
        type: string
      street2:
        type: string
      zipcode:
        type: string
      city:
        type: string
      state:
        type: string
        description: Mapping RefdataCategory "Federal State"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Federal State').collect{ it.value }.join(', ') }]
      country:
        type: string
        description: Mapping RefdataCategory "Country"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Country').collect{ it.value }.join(', ') }]
      type:
        type: string
        description: Mapping RefdataCategory "AddressType"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('AddressType').collect{ it.value }.join(', ') }]
      pob:
        type: string
      pobZipcode:
        type: string
      pobCity:
        type: string
      name:
        type: string
      additionFirst:
        type: string
      additionSecond:
        type: string
--%>
<%--
  Cluster:
    allOf:
      - $ref: "#/definitions/ClusterStub"
      - type: object
        properties:
          definition:
            type: string
          organisations: # mapping attr orgs
            type: array
            items:
              $ref: "#/definitions/OrganisationStub" # resolved OrgRole
          persons: # mapping attr prsLinks
            type: array
            items:
              $ref: "#/definitions/Person" # resolved PersonRole
--%>
  <%--
  Contact:
    type: object
    properties:
      category: # mapping attr contentType
        type: string
        description: Mapping RefdataCategory "ContactContentType"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('ContactContentType').collect{ it.value }.join(', ') }]
      content:
        type: string
      type:
        type: string
        description: Mapping RefdataCategory "ContactType"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('ContactType').collect{ it.value }.join(', ') }]"
--%>

  CostItem:
    type: object
    properties:
      placeholder:
        type: string

  CostItems:
    type: object
    properties:
      placeholder:
        type: string

  Document:
    type: object
    properties:
      placeholder:
        type: string

  #    filename:
  #      type: string
  #    mimetype:
  #      type: string
  #    title:
  #      type: string
  #    type:
  #      type: string
  #      description: Mapping RefdataCategory
  #      enum:
  #        [""]
  #    uuid:
  #      type: string
  #      example: "70d4ef8a-71b9-4b39-b339-9f3773c29b26"

  Identifier:
    type: object
    properties:
      namespace:
        type: string
      value:
        type: string

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
    #  status:
    #    type: string
    #    description: Mapping RefdataCategory
    #    enum:
    #      [""]
      subscription:
        $ref: "#/definitions/SubscriptionStub"
      tipp:
        $ref: "#/definitions/TitleInstancePackagePlatform"

  License:
    allOf:
      - $ref: "#/definitions/LicenseStub"
      - type: object
        properties:
          contact:
            type: string
          calculatedType:
            type: string
            description: Calculated object type
            enum:
              ["Template", "Local", "Consortial", "Participation", "Unkown"]
          dateCreated:
            type: string
            format: date
          documents:
            type: array
            items:
              $ref: "#/definitions/Document" # resolved DocContext
          endDate:
            type: string
            format: date
          instanceOf:
            $ref: "#/definitions/LicenseStub"
          isPublic:
            type: string
            description: Mapping RefdataCategory "YN"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
          lastmod:
            type: string
            format: date
          lastUpdated:
            type: string
            format: date
          licenseCategory:
            type: string
            description: Mapping RefdataCategory "LicenseCategory"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('LicenseCategory').collect{ it.value }.join(', ') }]
          licenseUrl:
            type: string
          licensorRef:
            type: string
          licenseeRef:
            type: string
          licenseStatus:
            type: string
          licenseType:
            type: string
          noticePeriod:
            type: string
          onixplLicense:
            $ref: "#/definitions/OnixplLicense"
          organisations: # mapping attr orgRelations
            type: array
            items:
              $ref: "#/definitions/OrganisationRole(relation)" # resolved OrgRole
    #      packages:
    #        type: array
    #        items:
    #          $ref: "#/definitions/PackageStub"
    #      persons: # mapping attr prsLinks
    #        type: array
    #        items:
    #          $ref: "#/definitions/Person" # resolved PersonRole
          properties: # mapping attr customProperties
            type: array
            items:
              $ref: "#/definitions/Property(licenseProperty)"
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
              $ref: "#/definitions/SubscriptionStub"
          type:
            type: string
            description: Mapping RefdataCategory "License Type"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('License Type').collect{ it.value }.join(', ') }]

  OnixplLicense:
    type: object
    properties:
#      id:
#        type: integer
      document: # mapping attr doc
        $ref: "#definitions/Document"
      lastmod:
        type: string
        format: date
      title:
        type: string
#      licenses:
#        type: array
#        items:
#          $ref: "#/definitions/LicenseStub"

  <%--
  Organisation:
    allOf:
      - $ref: "#/definitions/OrganisationStub"
      - type: object
        properties:
          addresses:
            type: array
            items:
              $ref: "#/definitions/Address"
          comment:
            type: string
          contacts:
            type: array
            items:
              $ref: "#/definitions/Contact"
          fteStudents:
            type: integer
          fteStaff:
            type: integer
          impId:
            type: string
            example: "9ef8a0d4-a87c-4b39-71b9-c29b269f311b"
          persons: # mapping attr prsLinks
            type: array
            items:
              $ref: "#/definitions/Person" # resolved PersonRole
          properties: # mapping attr customProperties and privateProperties
            type: array
            items:
              $ref: "#/definitions/Property"
          #roleType:
          #  type: array
          #  items:
          #    $ref: "#/definitions/OrgRoleType"
          #
          #  description: Mapping RefdataCategory "OrgRoleType"
          #  enum:
          #    [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgRoleType').collect{ it.value }.join(', ') }]
          scope:
            type: string
          sector:
            #deprecated: true
            type: string
            description: Mapping RefdataCategory "OrgSector"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgSector').collect{ it.value }.join(', ') }]
          status:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          type:
            #deprecated: true
            type: string
            description: Mapping RefdataCategory "OrgType"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgType').collect{ it.value }.join(', ') }]
--%>
#  OrganisationRole:
#    properties:
#      id:
#        type: integer
#      cluster:
#        $ref: "#/definitions/ClusterStub"
#        description: |
#          Exclusive with license, organisation, package, subscription and title
#      endDate:
#        type: string
#        format: date
#      license:
#        $ref: "#/definitions/LicenseStub"
#        description: |
#          Exclusive with cluster, organisation, package, subscription and title
#      organisation:
#        $ref: "#/definitions/OrganisationStub"
#        description: |
#          Exclusive with cluster, license, package, subscription and title
#      package:
#        $ref: "#/definitions/PackageStub"
#        description: |
#          Exclusive with cluster, license, organisation, subscription and title
#      roleType:
#        type: string
#        description: Mapping RefdataCategory "Organisational Role"
#        enum:
#          [""]
#      startDate:
#        type: string
#        format: date
#      subscription:
#        $ref: "#/definitions/SubscriptionStub"
#        description: |
#          Exclusive with cluster, license, organisation, package and title
#      title:
#        $ref: "#/definitions/TitleStub"
#        description: |
#          Exclusive with cluster, license, organisation, package and subscription

<%--
  Package:
    allOf:
      - $ref: "#definitions/PackageStub"
      - type: object
        properties:
          autoAccept:
            type: string
          breakable:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          cancellationAllowances:
            type: string
          consistent:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          dateCreated:
            type: string
            format: date
          documents:
            type: array
            items:
              $ref: "#/definitions/Document" # resolved DocContext
          endDate:
            type: string
            format: date
          fixed:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          forumId:
            type: string
          isPublic:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          lastUpdated:
            type: string
            format: date
          license:
            $ref: "#/definitions/LicenseStub"
          nominalPlatform:
            $ref: "#/definitions/Platform"
          organisations: # mapping attr orgs
            type: array
            items:
              $ref: "#/definitions/OrganisationRole(relation)"
          packageListStatus:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          packageScope:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          packageStatus:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          packageType:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
    #      persons: # mapping attr prsLinks
    #        type: array
    #        items:
    #          $ref: "#/definitions/Person" # resolved PersonRole
          sortName:
            type: string
          startDate:
            type: string
            format: date
          subscriptions:
            type: array
            items:
              $ref: "#/definitions/SubscriptionStub" # resolved subscriptionPackages
            description: TODO
          tipps:
            type: array
            items:
              $ref: "#/definitions/TitleInstancePackagePlatform(inPackage)"
          vendorURL:
            type: string

  Platform:
    allOf:
      - $ref: "#definitions/PlatformStub"
      - type: object
        properties:
          dateCreated:
            type: string
          lastUpdated:
            type: string
          primaryUrl:
            type: string
          provenance:
            type: string
          serviceProvider:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          softwareProvider:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          status:
            type: stringRefdataCategory
            description: Mapping RefdataCategory
            enum:
              [""]
          type:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]

  Person:
    type: object
    properties:
      globalUID:
        type: string
        example: "person:a45a3cf0-f3ad-f231-d5ab-fc1d217f583c"
      addresses:
        type: array
        items:
          $ref: "#/definitions/Address"
      contacts:
        type: array
        items:
          $ref: "#/definitions/Contact"
      firstName:
        type: string
      gender:
        type: string
        description: Mapping RefdataCategory "Gender"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Gender').collect{ it.value }.join(', ') }]
      isPublic:
        type: string
        description: Mapping RefdataCategory "YN". If set *No*, it's an hidRefdataCategoryden entry to/from an addressbook (depending on the given organisation context)
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
      lastName:
        type: string
      middleName:
        type: string
      contactType:
        type: string
        description: Mapping RefdataCategory "Person Contact Type"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Person Contact Type').collect{ it.value }.join(', ') }]
      roleType:
        type: string
        description: Mapping RefdataCategory "Person Position"
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Person Position').collect{ it.value }.join(', ') }]
      properties: # mapping attr privateProperties
        type: array
        items:
          $ref: "#/definitions/Property"
      roles:
        type: array
        items:
          $ref: "#/definitions/PersonRole(usedAsFunction)"
      title:
        type: string
--%>
  Property:
    type: object
    properties:
#      id:
#        type: integer
      description: # mapping attr descr
        type: string
      explanation: # mapping attr expl
        type: string
      name:
        type: string
      note:
        type: string
  #    tenant:
  #      $ref: "#/definitions/OrganisationStub"
  #      description: If set, this property is *private*
      isPublic: # derived to substitute tentant
        type: string
        description: Mapping RefdataCategory "YN". If set *No*, it's an hidden entry to/from the given organisation context
        enum:
          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
      value: # mapping attr stringValue, intValue, decValue, refValue, urlValue, dateValue
        type: string

  Subscription:
    allOf:
      - $ref: "#/definitions/SubscriptionStub"
      - type: object
        properties:
          cancellationAllowances:
            type: string
          calculatedType:
            type: string
            description: Calculated object type
            enum:
              ["Template", "Local", "Consortial", "Participation", "Unkown"]
          dateCreated:
            type: string
            format: date
    #      derivedSubscriptions:
    #        type: array
    #        items:
    #          $ref: "#/definitions/SubscriptionStub"
          documents:
            type: array
            items:
              $ref: "#/definitions/Document" # resolved DocContext
          endDate:
            type: string
            format: date
          form:
            type: string
            description: Mapping RefdataCategory "Subscription Form"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Subscription Form').collect{ it.value }.join(', ') }]
          instanceOf:
            $ref: "#/definitions/SubscriptionStub"
    #      isPublic:
    #        type: string
    #        description: Mapping RefdataCategory "YN"
    #        enum:
    #          [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
    #      issueEntitlements:
    #        type: array
    #        items:
    #          $ref: "#/definitions/IssueEntitlement"
          isSlaved:
            type: string
            description: Mapping RefdataCategory "YN"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YN').collect{ it.value }.join(', ') }]
          lastUpdated:
            type: string
            format: date
          license: # mapping attr owner
            $ref: "#/definitions/LicenseStub"
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
              $ref: "#/definitions/OrganisationRole(relation)"
          packages:
            type: array
            items:
              $ref: "#/definitions/Package(inSubscription)"
    #      persons: # mapping attr prsLinks
    #        type: array
    #        items:
    #          $ref: "#/definitions/Person" # resolved PersonRole
          previousSubscription:
            $ref: "#/definitions/SubscriptionStub"
          properties: # mapping attr customProperties
            type: array
            items:
              $ref: "#/definitions/Property"
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

  Title:
    allOf:
      - $ref: "#/definitions/TitleStub"
      - type: object
        properties:
          dateCreated:
            type: string
            format: date
          keyTitle:
            type: string
          lastUpdated:
            type: string
            format: date
          sortTitle:
            type: string
          status:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          type:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]

  TitleInstancePackagePlatform:
    allOf:
      - $ref: "#/definitions/TitleInstancePackagePlatformStub"
      - type: object
        description: TODO
        properties:
  #        accessStartDate:
  #          type: string
  #        accessEndDate:
  #          type: string
  #        coreStatusStart:
  #          type: string
  #        coreStatusEnd:
  #          type: string
  #        coverageDepth:
  #          type: string
  #        coverageNote:
  #          type: string
          delayedOA:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
    #      derivedFrom:
    #        $ref: "#/definitions/TitleInstancePackagePlatformStub"
  #        embargo:
  #          type: string
  #        endDate:
  #          type: string
  #        endVolume:
  #          type: string
  #        endIssue:
  #          type: string
          hostPlatformURL:
            type: string
          hybridOA:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
    #      masterTipp:
    #        $ref: "#/definitions/TitleInstancePackagePlatformStub"
          option:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          package:
            $ref: "#/definitions/PackageStub"
          payment:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          platform:
            $ref: "#/definitions/PlatformStub"
  #        rectype:
  #          type: string
  #        startDate:
  #          type: string
  #        startIssue:
  #          type: string
  #        startVolume:
  #          type: string
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
          subscription:
            $ref: "#/definitions/SubscriptionStub"
          title:
            $ref: "#/definitions/TitleStub"
