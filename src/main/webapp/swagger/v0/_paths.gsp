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


  /oamonitor/organisations/list:

    get:
      tags:
        - Datamanager
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
        - Datamanager
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
        - Datamanager
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
        - Datamanager
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
        - Datamanager
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
