<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : Manage FTControl</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
        <semui:crumb text="FTControl" class="active" />
    </semui:breadcrumbs>

  <h3>Currently Running: ${dataloadService.update_running}</h3>
  <h4>Last update run: <g:formatDate date="${dataloadService.lastIndexUpdate}" format="${message(code:'default.date.format.noZ')}"/></h4>

    <semui:messages data="${flash}" />

      <table class="ui celled la-table table">
        <thead>
          <tr>
            <th>domainClassName</th>
            <th>activity</th>
            <th>esElements</th>
            <th>dbElements</th>
            <th>lastTimestamp</th>
            <th>lastTimestamp in Date</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${ftControls}" var="ftControl">
            <tr>
              <td>${ftControl.domainClassName}</td>
              <td>
                  ${ftControl.activity}
              </td>
              <td>
                  ${ftControl.esElements}
              </td>
              <td>
                  ${ftControl.dbElements}
              </td>
              <td>
                  <semui:xEditable owner="${ftControl}" field="lastTimestamp"/>
              </td>
              <td>
                <g:formatDate date="${new Date(ftControl.lastTimestamp)}" format="${message(code:'default.date.format.noZ')}"/>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
  </body>
</html>
