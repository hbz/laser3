<laser:htmlStart text="Manage FTControl" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="FTControl" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="FTControl" />

    <ui:msg class="${dataload.update_running ? 'success' : 'info'}" noClose="true">
        Last doFTUpdate: <strong>${dataload.lastFTIndexUpdateInfo}</strong> ; Currently running: <strong>${dataload.update_running.toString().toUpperCase()}</strong>
    </ui:msg>

    <ui:messages data="${flash}" />

    <h2 class="ui header">FTControl</h2>

      <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
          <tr>
            <th>${message(code:'default.number')}</th>
            <th>Domain</th>
            <th>activity</th>
            <th>ES-Einträge</th>
            <th>DB-Einträge</th>
            <th>lastTimestamp</th>
            <th>as Date</th>
            <th>${message(code:'default.activated.label')}</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${ftControls}" var="ftControl" status="i">
            <tr>
              <td> ${i+1} </td>
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

    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
            <tr>
                <th>${message(code:'default.number')}</th>
                <th>Indexname</th>
                <th>Domain</th>
                <th>ES-Einträge</th>
                <th>DB-Einträge</th>
                <th>${message(code:'default.action.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${indices.sort{it.type}}" var="indexInfo" status="i">
            <tr>
                <td>${i+1}</td>
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
