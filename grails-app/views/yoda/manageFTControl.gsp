<laser:htmlStart text="Manage FTControl" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="FTControl" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="FTControl" />

    <ui:msg class="${dataload.running ? 'success' : 'info'}" noClose="true">
        Last doFTUpdate: <strong>${dataload.lastFTIndexUpdateInfo}</strong> ; Currently running: <strong>${dataload.running.toString().toUpperCase()}</strong>
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
              <td>
                  <g:if test="${ftControl.esElements != ftControl.dbElements}">
                      <span class="sc_red"><g:formatNumber number="${ftControl.esElements}" format="${message(code:'default.decimal.format')}"/></span>
                  </g:if>
                  <g:else>
                      <g:formatNumber number="${ftControl.esElements}" format="${message(code:'default.decimal.format')}"/>
                  </g:else>
              </td>
              <td>
                  <g:formatNumber number="${ftControl.dbElements}" format="${message(code:'default.decimal.format')}"/>
              </td>
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
                <td>
                    <g:if test="${indexInfo.countIndex != indexInfo.countDB}">
                        <span class="sc_red">
                            <g:if test="${indexInfo.countIndex != 'n/a'}">
                                <g:formatNumber number="${indexInfo.countIndex}" format="${message(code:'default.decimal.format')}"/>
                            </g:if>
                            <g:else>
                                ${indexInfo.countIndex}
                            </g:else>
                        </span>
                    </g:if>
                    <g:else>
                        <g:formatNumber number="${indexInfo.countIndex}" format="${message(code:'default.decimal.format')}"/>
                    </g:else>
                </td>
                <td>
                    <g:formatNumber number="${indexInfo.countDB}" format="${message(code:'default.decimal.format')}"/>
                </td>
                <td><g:link action="deleteAndRefillIndex" params="[name: indexInfo.name]">Delete and refill Index</g:link></td>
            </tr>
            </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
