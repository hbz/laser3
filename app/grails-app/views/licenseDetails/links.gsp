<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label', default:'License')}</title>
  </head>

  <body>

      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui header"><semui:headerIcon />

    %{--${license.licensee?.name}--}%
    ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
    <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

      <g:render template="nav"/>

      <table class="ui celled la-table table">
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
