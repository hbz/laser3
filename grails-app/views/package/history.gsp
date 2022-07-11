<%@ page import="de.laser.Package" %>
<laser:htmlStart message="package.show.nav.history" />

    <ui:breadcrumbs>
        <ui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
        <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="${packageInstance?.name}" />

    <ui:messages data="${flash}" />

    <laser:render template="nav"/>

    <g:if test="${formattedHistoryLines?.size() > 0}">

        <table class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'default.name.label')}</th>
              <th>${message(code:'event.actor')}</th>
              <th>${message(code:'event.name')}</th>
              <th>${message(code:'default.property.label')}</th>
              <th>${message(code:'event.old')}</th>
              <th>${message(code:'event.new')}</th>
              <th>${message(code:'default.date.label')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${formattedHistoryLines}" var="hl">
              <tr>
                <td><a href="${hl.link}">${hl.name}</a></td>
                <td>
                  <g:link controller="user" action="edit" id="${hl.actor?.id}">${hl.actor?.displayName}</g:link>
                </td>
                <td>${hl.eventName}</td>
                <td>${hl.propertyName}</td>
                <td>${hl.oldValue}</td>
                <td>${hl.newValue}</td>
                <td style="white-space:nowrap;"><g:formatDate format="${message(code:'default.date.format')}" date="${hl.lastUpdated}"/></td>
              </tr>
            </g:each>
          </tbody>
        </table>

        <g:if test="${historyLines != null}" >
          <ui:paginate action="history" controller="package" params="${params}" maxsteps="${max}" total="${num_hl}" />
        </g:if>

    </g:if>
    <g:else>
      <div class="container alert-warn">
      </div>
    </g:else>

<laser:htmlEnd />
