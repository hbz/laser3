
### endpoints ###

paths:

  /refdatas:
    get:
      tags:
      - Catalogues
      summary: Catalogue of refdatas
      description: >
        An Overview for RefdataCategories and RefdataValues
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/json
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/Refdatas"
        401:
          $ref: "#/responses/notAuthorized"

  /document:
    get:
      tags:
        - Documents
      summary: Find document by identifier
      description: >
        Supported are queries by following identifiers: *uuid*
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/*
        - text/*
      responses:
        200:
          description: OK
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but document not found
        406:
          $ref: "#/responses/notAcceptable"
<%--
  /issueEntitlements:
    get:
      tags:
        - IssueEntitlements
      summary: Find issue entitlements by subscription identifier and package identifier
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_. Both Parameters have to be separated by *comma*.
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - text/plain
        - application/json
      responses:
        200:
          description: OK
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but no issue entitlements found
        406:
          $ref: "#/responses/notAcceptable"
--%>
<%--
  /license:
    get:
      tags:
        - Licenses
      summary: Find license by identifier
      description: >
        Supported are queries by following identifiers: *globalUID*, *impId* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/json
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/License"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but license not found
        406:
          $ref: "#/responses/notAcceptable"
        412:
          $ref: "#/responses/preconditionFailed"
--%><%--
    post:
      tags:
        - Licenses
      summary: Create license (work in progess)
      #TODO:  description: Organisations and Subscriptions will NOT be _created_, but _linked_ if found
      description: Organisations will NOT be _created_, but _linked_ if found
      parameters:
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
        - in: body
          name: body
          required: true
          schema:
            $ref: "#/definitions/License"
          description: Object data
      consumes:
        - application/json
      produces:
        - application/json
      responses:
        201:
          $ref: "#/responses/created"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        409:
          $ref: "#/responses/conflict"
        500:
          $ref: "#/responses/internalServerError"

  /onixpl:
    get:
      tags:
        - Documents
      summary: Find onixpl documents by license identifier
      description: >
        Supported are queries by following identifiers: *globalUID* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/xml
      responses:
        200:
          description: OK
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but document not found
        406:
          $ref: "#/responses/notAcceptable"

  /organisation:
    get:
      tags:
        - Organisations
      summary: Find organisation by identifier
      description: >
        Supported are queries by following identifiers: *globalUID*, *impId* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _isil:DE-123_
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/json
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/Organisation"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but organisation not found
        406:
          $ref: "#/responses/notAcceptable"
        412:
          $ref: "#/responses/preconditionFailed"
    post:
      tags:
        - Organisations
      summary: Create organisation
      description: Organisation will NOT be created, if one organisation with same name AND namespace-identifier exists
      parameters:
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
        - in: body
          name: body
          required: true
          schema:
            $ref: "#/definitions/Organisation"
          description: Object data
      consumes:
        - application/json
      produces:
        - application/json
      responses:
        201:
          $ref: "#/responses/created"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        409:
          $ref: "#/responses/conflict"
        500:
          $ref: "#/responses/internalServerError"

  /package:
    get:
      tags:
        - Packages
      summary: Find packge by identifier
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier*, *impId* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _xyz:4711_
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/json
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/Package"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but license not found
        406:
          $ref: "#/responses/notAcceptable"
        412:
          $ref: "#/responses/preconditionFailed"
--%>

  /subscription:
    get:
      tags:
      - Subscriptions
      summary: Find subscription by identifier
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier*, *impId* and *ns:identifier*. Ns:identifier value has to be defined like this: _xyz:4711_
      parameters:
        - $ref: "#/parameters/q"
        - $ref: "#/parameters/v"
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
      produces:
        - application/json
      responses:
        200:
          description: OK
          schema:
            $ref: "#/definitions/Subscription"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        403:
          $ref: "#/responses/forbidden"
        404:
          description: Valid request, but subscription not found
        406:
          $ref: "#/responses/notAcceptable"
        412:
          $ref: "#/responses/preconditionFailed"
<%--
    post:
      tags:
        - Subscriptions
      summary: Create Subscription (work in progess)
      #TODO:  description: Organisations and Subscriptions will NOT be _created_, but _linked_ if found
      description: Organisations will NOT be _created_, but _linked_ if found
      parameters:
        - $ref: "#/parameters/context"
        - $ref: "#/parameters/authorization"
        - in: body
          name: body
          required: true
          schema:
            $ref: "#/definitions/Subscription"
          description: Object data
      consumes:
        - application/json
      produces:
        - application/json
      responses:
        201:
          $ref: "#/responses/created"
        400:
          $ref: "#/responses/badRequest"
        401:
          $ref: "#/responses/notAuthorized"
        409:
          $ref: "#/responses/conflict"
        500:
          $ref: "#/responses/internalServerError"
--%>
