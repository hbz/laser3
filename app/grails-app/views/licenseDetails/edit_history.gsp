<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap"/>
        <title>${message(code:'laser', default:'LAS:eR')} Licence</title>
</head>

<body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>
    
    <div class="container">
        <h1>${license.licensee?.name} ${license.type?.value} Licence : ${license.reference}</h1>

<g:render template="nav" />

    </div>

    <div class="container">
      <h3>${message(code:'licence.editHistory', default:'Edit history')}</h3>
      <table  class="table table-striped table-bordered">
        <tr>
          <th>${message(code:'license.editHystory.table.eventID', default:'Event ID')}</th>
          <th>${message(code:'license.editHystory.table.person', default:'Person')}</th>
          <th>${message(code:'license.editHystory.table.date', default:'Date')}</th>
          <th>${message(code:'license.editHystory.table.event', default:'Event')}</th>
          <th>${message(code:'license.editHystory.table.field', default:'Field')}</th>
          <th>${message(code:'license.editHystory.table.oldValue', default:'Old Value')}</th>
          <th>${message(code:'license.editHystory.table.newValue', default:'New Value')}</th>
        </tr>
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
      <div class="pagination">
        <bootstrap:paginate  action="edit_history" controller="licenseDetails" params="${params}" next="Next" prev="Prev" max="${max}" total="${historyLinesTotal}" />
      </div>
    </div>

</body>
</html>
