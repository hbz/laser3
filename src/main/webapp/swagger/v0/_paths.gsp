<%-- indention: 2 --%>

  /costItem:

    get:
      tags:
        - Objects
      summary: Retrieving a single cost item
      description: >
        Supported are queries by following identifiers: *uuid*

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/context"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/CostItem"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but cost item not found
        406:
          $ref: "#/components/responses/notAcceptable"


  /costItemList:

    get:
      tags:
        - Lists
      summary: Retrieving a list of owner related cost items
      description: >
        Supported are queries by following identifiers: *globalUID*. Optional identifier/constraint *timestamp* is supported.

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/context"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/CostItemList"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but owner not found or result is empty
        406:
          $ref: "#/components/responses/notAcceptable"


  /document:

    get:
      tags:
        - Objects
      summary: Download a single document
      description: >
        Supported are queries by following identifiers: *uuid*

      parameters:
        - $ref: "#/components/parameters/q_withoutDefault"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            '*/*':
              schema:
                $ref: "#/components/schemas/PlaceholderBinary"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but document not found
        406:
          $ref: "#/components/responses/notAcceptable"


  /license:

    get:
      tags:
        - Objects
      summary: Retrieving a single license
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/License"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but license not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"


  /licenseList:

    get:
      tags:
        - Lists
      summary: Retrieving a list of owner related licenses
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _isil:DE-123_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v_forList"
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/LicenseList"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but owner not found or result is empty
        406:
          $ref: "#/components/responses/notAcceptable"

  /orgAccessPoint:

    get:
      tags:
        - Objects
      summary: Retrieving a single org access point
      description: >
        Supported are queries by following identifiers: *uuid*

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/context"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/OrgAccessPoint"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but cost item not found
        406:
          $ref: "#/components/responses/notAcceptable"

  /organisation:

    get:
      tags:
        - Objects
      summary: Retrieving a single organisation
      description: >
        Supported are queries by following identifiers: *globalUID*, *gokbId* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Organisation"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but organisation not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"


  /package:

    get:
      tags:
        - Objects
      summary: Retrieving a single package
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _xyz:4711_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Package"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but package not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"


  /platform:

    get:
      tags:
      - Objects
      summary: Retrieving a single platform
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier* and *ns:identifier*. Ns:identifier value has to be defined like this: _xyz:4711_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Platform"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        404:
          description: Valid request, but platform not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"


  /platformList:

    get:
      tags:
        - Lists
      summary: Retrieving a list of public platforms
      description: >
        Retrieving a list of public platforms

      parameters:
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlatformStub"
        401:
          $ref: "#/components/responses/notAuthorized"
        404:
          description: Valid request, but result is empty


  /propertyList:

    get:
      tags:
        - Lists
      summary: Retrieving a combined list of public and owner related property definitions
      description: >
        Retrieving a combined list of public and owner related property definitions

      parameters:
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertyList"
        401:
          $ref: "#/components/responses/notAuthorized"


  /refdataList:

    get:
      tags:
        - Lists
      summary: Retrieving catalogue of combined refdatas
      description: >
        Retrieving an overview for RefdataCategories and RefdataValues

      parameters:
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Refdatas_Virtual"
        401:
          $ref: "#/components/responses/notAuthorized"


  /subscription:

    get:
      tags:
      - Objects
      summary: Retrieving a single subscription
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier* and *ns:identifier*. Ns:identifier value has to be defined like this: _xyz:4711_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Subscription"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but subscription not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"


  /subscriptionList:

    get:
      tags:
        - Lists
      summary: Retrieving a list of owner related subscriptions
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _isil:DE-123_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v_forList"
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/SubscriptionList"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but owner not found or result is empty
        406:
          $ref: "#/components/responses/notAcceptable"

  /ezb/license/illIndicators:

    get:
      tags:
        - "Special: EZB"
      summary: Retrieving the interlibrary loan (ILL) indicators for a given license
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _ezb_anchor:acs_

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PropertySet"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but license not found
        406:
          $ref: "#/components/responses/notAcceptable"
        412:
          $ref: "#/components/responses/preconditionFailed"

  /ezb/subscription:

    get:
      tags:
        - "Special: EZB"
      summary: Retrieving a single subscription with more information
      description: >
        Retrieves a list of the given subscription's holding according to the KBART standard (<a href="https://groups.niso.org/higherlogic/ws/public/download/16900/RP-9-2014_KBART.pdf">see here</a>), enriched by columns used in LAS:eR.
        Although the structure suggests JSON, the response is a tabulator-separated table in plain-text format (TSV, MIME-type text/tab-separated-values). Order of the fields (= columns) is as specified in KBART reference linked above;
        KBART-standard defined columns go until "access_type". Columns listed after "access_type" are LAS:eR-proprietary fields and definitions come from the internal definition (<a href="https://dienst-wiki.hbz-nrw.de/display/KOE/KBART+to+LAS%3AeR">see here</a>).
        The columns appearing after 'localprice_usd' are further identifier columns and varying, depending on the underlying namespaces to which title identifiers are available. Please refer to the we:kb for a complete list of possible namespaces.

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/changedFrom"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            text/tab-separated-values:
              schema:
                $ref: "#/components/schemas/Subscription_KBART"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"

  /ezb/subscription/list:

    get:
      tags:
        - "Special: EZB"
      summary: Retrieving a list of subscriptions eligible for EZB yellow tagging
      description: >
        Retrieving a list of subscriptions of organisations that have granted the data exchange. An optional parameter changedFrom may be submitted
        for incremental harvesting

      parameters:
        - $ref: "#/components/parameters/ezbOrgId"
        - $ref: "#/components/parameters/changedFrom"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/EZBInstitutionList"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but no appropriate organisations found

  /oamonitor/organisations/list:

    get:
      tags:
        - "Special: OAMonitor"
      summary: Retrieving a list of appropriate organisations
      description: >
        Retrieving a list of organisations that have granted the data exchange

      parameters:
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlaceholderList"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but no appropriate organisations found
        406:
          $ref: "#/components/responses/notAcceptable"

  /oamonitor/organisations:

    get:
      tags:
        - "Special: OAMonitor"
      summary: Retrieving a single organisation with more information
      description: >
        **EXPERIMENTAL**

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlaceholderObject"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but organisation not found
        406:
          $ref: "#/components/responses/notAcceptable"


  /oamonitor/subscriptions:

    get:
      tags:
        - "Special: OAMonitor"
      summary: Retrieving a single subscription with more information
      description: >
        **EXPERIMENTAL**

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlaceholderObject"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        406:
          $ref: "#/components/responses/notAcceptable"


  /statistic/packages/list:

    get:
      tags:
        - "Special: Nationaler Statistikserver"
      summary: Retrieving a list of appropriate packages
      description: >
        Retrieving a list of packages related to organisations that have granted the data exchange

      parameters:
        - $ref: "#/components/parameters/authorization"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlaceholderList"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but no appropriate packages found
        406:
          $ref: "#/components/responses/notAcceptable"


  /statistic/packages:

    get:
      tags:
        - "Special: Nationaler Statistikserver"
      summary: Retrieving a single package with more information
      description: >
        **EXPERIMENTAL**

      parameters:
        - $ref: "#/components/parameters/q"
        - $ref: "#/components/parameters/v"
        - $ref: "#/components/parameters/authorization"
        - $ref: "#/components/parameters/debug"

      responses:
        200:
          description: OK
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PlaceholderObject"
        400:
          $ref: "#/components/responses/badRequest"
        401:
          $ref: "#/components/responses/notAuthorized"
        403:
          $ref: "#/components/responses/forbidden"
        404:
          description: Valid request, but package not found
        406:
          $ref: "#/components/responses/notAcceptable"
