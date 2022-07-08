<laser:htmlStart message="license.nav.edit_history" />

    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon>
        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <laser:render template="iconSubscriptionIsChild"/>
        </g:if>
        <semui:xEditable owner="${subscription}" field="name" />
    </semui:h1HeaderWithIcon>
    <semui:anualRings object="${subscription}" controller="subscription" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="message" />
    </g:if>

      <table  class="ui celled la-js-responsive-table la-table table">
        <thead>
            <tr>
              <th>${message(code:'default.eventID.label')}</th>
              <th>${message(code:'default.person.label')}</th>
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

    <semui:paginate action="history" controller="subscription" params="${params}" max="${max}" total="${historyLinesTotal}" />


<laser:htmlEnd />
