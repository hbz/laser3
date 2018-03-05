<%@ page import="com.k_int.kbplus.TitleInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'Title Instance')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb controller="titleDetails" action="list" message="menu.institutions.all_titles" />
        <semui:crumb class="active" text="${message(code:'title.title.label')}: ${titleInstance.title}" />
    </semui:breadcrumbs>

    <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
    </g:if>

    <h1 class="ui header">${titleInstance.title}</h1>

    <g:render template="nav" />

    <semui:messages data="${flash}" />

    <g:if test="${formattedHistoryLines?.size() > 0}">

      <div class="container alert-warn">
        <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'event.component', default:'Name')}</th>
              <th>${message(code:'event.actor', default:'Actor')}</th>
              <th>${message(code:'event.name', default:'Event name')}</th>
              <th>${message(code:'event.property', default:'Property')}</th>
              <th>${message(code:'event.old', default:'Old')}</th>
              <th>${message(code:'event.new', default:'New')}</th>
              <th>${message(code:'default.date.label', default:'Date')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${formattedHistoryLines}" var="hl">
              <tr>
                <td><a href="${hl.link}">${hl.name}</a></td>
                <td>
                  <g:link controller="userDetails" action="edit" id="${hl.actor?.id}">${hl.actor?.displayName}</g:link>
                </td>
                <td>${hl.eventName}</td>
                <td>${hl.propertyName}</td>
                <td>${hl.oldValue}</td>
                <td>${hl.newValue}</td>
                <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${hl.lastUpdated}"/></td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>



        <g:if test="${historyLines != null}" >
          <semui:paginate  action="history" controller="titleDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_hl}" />
        </g:if>

    </g:if>
    <g:else>
      <div class="container alert-warn"> </div>
    </g:else>


</body>
</html>
