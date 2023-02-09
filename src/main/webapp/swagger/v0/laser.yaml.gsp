openapi: 3.0.2
info:
  title: LAS:eR - API
  description: >
    Known Issues:
    _Authorization_ has to be set manually.
    Usual javascript insertion isn't working due shadow dom mechanic of [React](https://facebook.github.io/react).
    Please copy and paste required/generated fields.

     &#10095;
     [Here](${de.laser.config.ConfigMapper.getGrailsServerURL()}/api/v0/changelog.md) you will find the changelog,
     [here](${de.laser.config.ConfigMapper.getGrailsServerURL()}/api/v0/specs.yaml) you will find almost current specs and
     [here]() you will get an example for the HMAC generation.

  contact:
    email: laser@hbz-nrw.de
  version: "<% print de.laser.api.v0.ApiManager.VERSION %>"

<g:if test="${grails.util.Environment.current == grails.util.Environment.PRODUCTION}">
servers:
  - url: ${de.laser.config.ConfigMapper.getGrailsServerURL()}/api/v0
</g:if>
<g:else>
servers:
  - url: ${de.laser.config.ConfigMapper.getGrailsServerURL()}/api/v0
</g:else>

paths:

<g:render template="/swagger/v0/paths" />

components:

  parameters:

    q:
      name: q
      in: query
      schema:
        type: string
        default: globalUID
      required: true
      description: Identifier for this query

    q_withoutDefault:
      name: q
      in: query
      schema:
        type: string
      required: true
      description: Identifier for this query

    v:
      name: v
      in: query
      schema:
        type: string
      required: true
      description: Value for this query

<g:if test="${apiContext}">
    v_forList:
      name: v
      in: query
      schema:
        type: string
        default: ${apiContext}
      required: true
      description: Value for this query
</g:if>
<g:else>
    v_forList:
      name: v
      in: query
      schema:
        type: string
      required: true
      description: Value for this query
</g:else>

    changedFrom:
      name: changedFrom
      in: query
      schema:
        type: string
        format: date
      required: false
      description: Date from which changes should be considered

<g:if test="${apiContext}">
    context:
      name: context
      in: query
      schema:
        type: string
        default: ${apiContext}
      required: true
      description: Concrete globalUID of context organisation
</g:if>
<g:else>
    context:
      name: context
      in: query
      schema:
        type: string
      required: true
      description: Concrete globalUID of context organisation
</g:else>

    ezbOrgId:
      name: ezbOrgId
      in: query
      schema:
        type: string
      required: false
      description: Concrete EZB identifier of institution whose subscriptions should be requested

    debug:
      name: x-debug
      in: header
      schema:
        type: boolean
      required: false
      description: Adding debug informations

    authorization:
      name: x-authorization
      in: header
      schema:
        type: string
      required: true
      description: hmac-sha256 generated auth header


  responses:

    ok:
      description: OK

    badRequest:
      description: Invalid or missing identifier/value

    conflict:
      description: Conflict with existing resource

    created:
      description: Resource successfully created

    forbidden:
      description: Forbidden access to this resource

    internalServerError:
      description: Resource not created

    notAcceptable:
      description: Requested format not supported

    notAuthorized:
      description: Request is not authorized

    notImplemented:
      description: Requested method not implemented

    preconditionFailed:
      description: Multiple matches


  schemas:

<g:render template="/swagger/v0/schemas" />
