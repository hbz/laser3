<%-- indention: 2 --%>

  /refdatas:

    get:
      tags:
        - Catalogues
      summary: Catalogue of refdatas
      description: >
        Retrieving an overview for RefdataCategories and RefdataValues

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
                $ref: "#/components/schemas/Refdatas_Virtual"
        401:
          $ref: "#/components/responses/notAuthorized"


  /document:

    get:
      tags:
        - Objects
      summary: Downloading a document
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
            application/*:
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
      summary: Retrieving a license
      description: >
        Supported are queries by following identifiers: *globalUID*, *impId* and *ns:identifier*. *Ns:identifier* value has to be defined like this: _gasco-lic:0815_

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


  /subscription:

    get:
      tags:
      - Objects
      summary: Retrieving a subscription
      description: >
        Supported are queries by following identifiers: *globalUID*, *identifier*, *impId* and *ns:identifier*. Ns:identifier value has to be defined like this: _xyz:4711_

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
