<%@ page import="de.laser.titles.TitleInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'title.nav.history')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb controller="title" action="list" message="menu.public.all_titles" />
        <semui:crumb class="active" text="${message(code:'title.title.label')}: ${titleInstance.title}" />
    </semui:breadcrumbs>

    <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
    </g:if>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${titleInstance.title}</h1>

    <laser:render template="nav" />

    <semui:messages data="${flash}" />

    <g:if test="${formattedHistoryLines?.size() > 0}">

      <div class="container alert-warn">
        <table class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'event.component')}</th>
              <th>${message(code:'event.actor')}</th>
              <th>${message(code:'event.name')}</th>
              <th>${message(code:'event.property')}</th>
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
                <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${hl.lastUpdated}"/></td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>



        <g:if test="${historyLines != null}" >
          <semui:paginate  action="history" controller="title" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" maxsteps="${max}" total="${num_hl}" />
        </g:if>

    </g:if>
    <g:else>
      <div class="container alert-warn"> </div>
    </g:else>


</body>
</html>
