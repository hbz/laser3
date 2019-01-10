
  ### nested ###

<%--
  License(inSubscription):
    allOf:
      - $ref: "#/definitions/LicenseStub"
      - type: object
        properties:
          contact:
            type: string
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
          isPublic:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          instanceOf:
            $ref: "#/definitions/LicenseStub(inLicense)"
          lastmod:
            type: string
            format: date
          lastUpdated:
            type: string
            format: date
          licenseCategory:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
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
            description: Mapping RefdataCategory
            enum:
              [""]
          type:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
--%>

  OrganisationRole(relation):
    properties:
      endDate:
        type: string
        format: date
      organisation:
        $ref: "#/definitions/OrganisationStub"
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

  Package(inSubscription):
    type: object
    properties:
      globalUID:
        type: string
        example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
    #  identifier:
    #    type: string
    #    example: "04bf5766-bf45-4b9e-afe1-d89de46f6c66"
      issueEntitlements:
        type: array
        items:
          $ref: "#/definitions/IssueEntitlement"
      name:
        type: string
      vendorURL:
        type: string
<%--
  PersonRole:
    type: object
    properties:
      endDate:
        type: string
        format: date
      startDate:
        type: string
        format: date

  PersonRole(usedAsFunction):
    allOf:
      - $ref: "#/definitions/PersonRole"
      - type: object
        properties:
          functionType:
            type: string
            description: |
              Exclusive with responsibilityType |
              Mapping RefdataCategory "Person Function"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Person Function').collect{ it.value }.join(', ') }]

  PersonRole(usedAsResponsibility):
    allOf:
      - $ref: "#/definitions/PersonRole"
      - type: object
        properties:
          cluster:
            description: |
              Exclusive with license, organisation, package, subscription and title
            $ref: "#/definitions/ClusterStub"
          license:
            description: |
              Exclusive with cluster, organisation, package, subscription and title
            $ref: "#/definitions/LicenseStub"
          organisation:
            description: |
              Exclusive with cluster, license, package, subscription and title
            $ref: "#/definitions/OrganisationStub"
          package:
            description: |
              Exclusive with cluster, license, organisation, subscription and title
            $ref: "#/definitions/PackageStub"
          responsibilityType:
            type: string
            description: |
              Exclusive with functionType |
              Mapping RefdataCategory "Person Responsibility"
            enum:
              [${ com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Person Responsibility').collect{ it.value }.join(', ') }]
          subscription:
            description: |
              Exclusive with cluster, license, organisation, package and title
            $ref: "#/definitions/SubscriptionStub"
          title:
            description: |
              Exclusive with cluster, license, organisation, package and subscription
            $ref: "#/definitions/TitleStub"
--%>

  Property(licenseProperty):
    allOf:
      - $ref: "#/definitions/Property"
      - type: object
        properties:
          paragraph:
            type: string

  TitleInstancePackagePlatform(inPackage):
    allOf:
      - $ref: "#/definitions/TitleInstancePackagePlatformStub"
      - type: object
        description: TODO
        properties:
          accessStartDate:
            type: string
          accessEndDate:
            type: string
          coreStatusStart:
            type: string
          coreStatusEnd:
            type: string
          coverageDepth:
            type: string
          coverageNote:
            type: string
          delayedOA:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
    #      derivedFrom:
    #        $ref: "#/definitions/TitleInstancePackagePlatformStub"
          embargo:
            type: string
          endDate:
            type: string
          endVolume:
            type: string
          endIssue:
            type: string
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
          payment:
            type: string
            description: Mapping RefdataCategory
            enum:
              [""]
          platform:
            $ref: "#/definitions/PlatformStub"
          rectype:
            type: string
          startDate:
            type: string
          startIssue:
            type: string
          startVolume:
            type: string
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
            $ref: "#/definitions/TitleStub"
