
  ### stubs ###

<%--
  ClusterStub:
    type: object
    properties:
      id:
        type: integer
      name:
        type: string
--%>

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
          $ref: "#/definitions/Identifier"
      reference:
        type: string
      sortableReference:
        type: string
<%--
  LicenseStub(inLicense):
    allOf:
      - $ref: "#/definitions/LicenseStub"
      - type: object
--%>

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
          $ref: "#/definitions/Identifier"

  PackageStub:
    type: object
    properties:
      globalUID:
        type: string
        example: "package:f08250fc-257e-43d6-9528-c56d841a6b00"
      identifier:
        type: string
        example: "5d83bbbe-2a26-4eef-8708-6d6b86fd8453"
      identifiers: # mapping attr ids
        type: array
        items:
          $ref: "#/definitions/Identifier"
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
      normname:
        type: string

  SubscriptionStub:
    type: object
    properties:
      globalUID:
        type: string
        example: "subscription:3026078c-bdf1-4309-ba51-a9ea5f7fb234"
      identifier:
        type: string
        example: "1038ac38-eb21-4bf0-ab7e-fe8b8ba34b6c"
      identifiers: # mapping attr ids
        type: array
        items:
          $ref: "#/definitions/Identifier"
      impId:
        type: string
        example: "ff74dd15-e27f-48a2-b2d7-f02389e62639"
      name:
        type: string

<%--
  SubscriptionStub(inSubscription):
    allOf:
      - $ref: "#/definitions/SubscriptionStub"
      - type: object
--%>

  TitleInstancePackagePlatformStub:
    type: object
    description: TODO
    properties:
      globalUID:
        type: string
        example: "titleinstancepackageplatform:9d5c918a-80b5-a121-a7f8-b05ac53004a"
      identifiers: # mapping attr ids
        type: array
        items:
          $ref: "#/definitions/Identifier"
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
          $ref: "#/definitions/Identifier"
      impId:
        type: string
        example: "daccb411-e7c6-4048-addf-1d2ccf35817f"
      title:
        type: string
      normtitle:
        type: string
