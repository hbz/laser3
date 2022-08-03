<laser:htmlStart text="Manage FTControl" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="FTControl" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="FTControl" />

    <ui:msg class="${dataloadService.update_running ? 'success' : 'info'}" noClose="true">
        Is currently running: <strong>${dataloadService.update_running}</strong>
        <g:if test="${dataloadService.lastIndexUpdate}">
            <br />
            Last noticed run: <g:formatDate date="${dataloadService.lastIndexUpdate}" format="${message(code:'default.date.format.noZ')}"/>
        </g:if>
    </ui:msg>

    <ui:messages data="${flash}" />

    <h2 class="ui header">FTControl</h2>

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <th>domainClassName</th>
            <th>activity</th>
            <th>esElements</th>
            <th>dbElements</th>
            <th>lastTimestamp</th>
            <th>lastTimestamp in Date</th>
            <th>Active</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${ftControls}" var="ftControl">
            <tr>
              <td> ${ftControl.domainClassName} </td>
              <td> ${ftControl.activity} </td>
              <td> ${ftControl.esElements} </td>
              <td> ${ftControl.dbElements} </td>
              <td>
                  <ui:xEditable owner="${ftControl}" field="lastTimestamp"/>
              </td>
              <td>
                <g:formatDate date="${new Date(ftControl.lastTimestamp)}" format="${message(code:'default.date.format.noZ')}"/>
              </td>
              <td>
                <ui:xEditableBoolean owner="${ftControl}" field="active"/>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

    <h2 class="ui header">Elasticsearch</h2>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
            <tr>
                <th>Indexname</th>
                <th>${message(code:'default.type.label')}</th>
                <th>ES-Einträge</th>
                <th>DB-Einträge</th>
                <th>${message(code:'default.action.label')}</th>
            </tr>
        </thead>
      <tbody>
      <g:each in="${indices.sort{it.type}}" var="indexInfo">
        <tr>
          <td>${indexInfo.name}</td>
          <td>${indexInfo.type}</td>
          <td>${indexInfo.countIndex}</td>
          <td>${indexInfo.countDB}</td>
          <td><g:link action="deleteAndRefillIndex" params="[name: indexInfo.name]">Delete and refill Index</g:link></td>
        </tr>
      </g:each>
      </tbody>
    </table>

<laser:htmlEnd />
