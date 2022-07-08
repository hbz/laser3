<laser:htmlStart message="license.nav.edit_history" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:h1HeaderWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
        <semui:totalNumber total="${historyLinesTotal?:'0'}"/>
    </semui:h1HeaderWithIcon>

    <laser:render template="nav" />

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

        <semui:paginate action="editHistory" controller="license" params="${params}" max="${max}" total="${historyLinesTotal}" />

<laser:htmlEnd />
