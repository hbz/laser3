<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'license.nav.edit_history')}</title>
</head>

<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
        <semui:totalNumber total="${historyLinesTotal?:'0'}"/>
    </h1>

    <g:render template="nav" />

      <table  class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'default.eventID.label')}</th>
              <th>${message(code:'person.label')}</th>
              <th>${message(code:'default.date.label')}</th>
              <th>${message(code:'default.event.label')}</th>
              <th>${message(code:'default.field.label')}</th>
              <th>${message(code:'default.oldValue.label')}</th>
              <th>${message(code:'default.newValue.label')}</th>
            </tr>
          </thead>
        <g:if test="${historyLines}">
          <g:each in="${historyLines}" var="hl">
            <tr>
              <td>${hl.id}</td>
              <td style="white-space:nowrap;">${hl.actor}</td>
              <td style="white-space:nowrap;">${hl.dateCreated}</td>
              <td style="white-space:nowrap;">${hl.eventName}</td>
              <td style="white-space:nowrap;">${hl.propertyName}</td>
              <td>${hl.oldValue}</td>
              <td>${hl.newValue}</td>
            </tr>
          </g:each>
        </g:if>
      </table>

        <semui:paginate  action="editHistory" controller="license" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${historyLinesTotal}" />

</body>
</html>
