---
swagger: "2.0"
info:
  version: "0 - 0.12"
  title: LAS:eR - API
  description: >
    Known Issues:
    _Authorization_ has to be set manually.
    Usual javascript insertion isn't working due shadow dom mechanic of [React](https://facebook.github.io/react).
    Please copy and paste
  contact:
    email: david.klober@hbz-nrw.de

<g:if test="${grails.util.Environment.current == grails.util.Environment.PRODUCTION}">basePath: /api/v0 # production

schemes:
  - https
  - http</g:if>
<g:else>basePath: /laser/api/v0 # development

schemes:
  - http</g:else>

tags:
  - name: Documents
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home
  - name: IssueEntitlements
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home
  - name: Licenses
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home
  - name: Organisations
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home
  - name: Packages
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home
  - name: Subscriptions
    description: ""
    externalDocs:
      url: http://wiki1.hbz-nrw.de/display/ERM/Home


<g:render template="/swagger/v0/parameters" />

<g:render template="/swagger/v0/responses" />

<g:render template="/swagger/v0/paths" />

definitions:

<g:render template="/swagger/v0/definitions-stubs" />

<g:render template="/swagger/v0/definitions-full" />
