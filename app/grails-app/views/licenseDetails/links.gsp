<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} License</title>
  </head>

  <body>

      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

      <h1 class="ui header">
        <semui:editableLabel editable="${editable}" />
        ${license.licensee?.name} ${license.type?.value} License : <span id="reference" style="padding-top: 5px;">${license.reference}</span>
      </h1>

      <g:render template="nav"/>

      <table class="ui celled striped table">
        <thead>
          <tr>
            <th>Link Type</th>
            <th>Linked Object</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${license.outgoinglinks}" var="links">
            <tr>
              <td>Outgoing</td>
              <td>${links.linkSource.genericLabel}</td>
            </tr>
          </g:each>
          <g:each in="${license.incomingLinks}" var="links">
            <tr>
              <td>Incoming</td>
              <td>${links.linkTarget.genericLabel}</td>
            </tr>
          </g:each>
        </tbody>
      </table>
    
  </body>
</html>
