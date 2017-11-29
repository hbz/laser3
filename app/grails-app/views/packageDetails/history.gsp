<%--
  Created by IntelliJ IDEA.
  User: ioannis
  Date: 15/05/2014
  Time: 15:00
--%>

<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb controller="packageDetails" action="index" text="${message(code:'package.show.all', default:'All Packages')}" />
        <semui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
    </semui:breadcrumbs>


<semui:messages data="${flash}" />


    <h1 class="ui header">${packageInstance?.name}</h1>
    <g:render template="nav"/>


<div>
    <g:if test="${formattedHistoryLines?.size() > 0}">

      <div class="container alert-warn">
        <table class="ui celled table">
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
                <td style="white-space:nowrap;"><g:formatDate format="${message(code:'default.date.format', default:'yyyy-MM-dd')}" date="${hl.lastUpdated}"/></td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>


      <div class="pagination" style="text-align:center">
        <g:if test="${historyLines != null}" >
          <bootstrap:paginate  action="history" controller="packageDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_hl}" />
        </g:if>
      </div>

    </g:if>
    <g:else>
      <div class="container alert-warn">
      </div>
    </g:else>

</div>


</body>
</html>
