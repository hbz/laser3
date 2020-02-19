
<%@ page import="com.k_int.kbplus.TitleInstancePackagePlatform" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstancePackagePlatform.label')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div>

          <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.list.label" args="[entityName]" /> (Showing ${titleInstancePackagePlatformInstanceTotal})...
            <semui:totalNumber total="${titleInstancePackagePlatformInstanceTotal}"/>
          </h1>

        <semui:messages data="${flash}" />
        
        <table class="ui sortable celled la-table table">
          <thead>
              <g:sortableColumn property="title.title" title="${message(code: 'title.label')}" />
              <g:sortableColumn property="platform.name" title="${message(code: 'tipp.show.platformName')}" />
              <g:sortableColumn property="pkg.name" title="${message(code: 'package.label')}" />
              <g:sortableColumn property="startDate" title="${message(code: 'tipp.show.tippStartDate')}" />
              <g:sortableColumn property="startVolume" title="${message(code: 'tipp.show.tippStartVol')}" />
              <g:sortableColumn property="startIssue" title="${message(code: 'tipp.show.tippStartIss')}" />
              <g:sortableColumn property="endDate" title="${message(code: 'tipp.show.tippEndDate')}" />
              <g:sortableColumn property="endVolume" title="${message(code: 'tipp.show.tippEndVol')}" />
              <g:sortableColumn property="endIssue" title="${message(code: 'tipp.show.tippEndIss')}" />
              <th></th>
            </tr>
          </thead>
          <tbody>
          <g:each in="${titleInstancePackagePlatformInstanceList}" var="tipp">
            <tr>
              <td><g:link controller="title" action="show" id="${tipp.title.id}">${fieldValue(bean: tipp, field: "title.title")}</g:link></td>
              <td><g:link controller="platform" action="show" id="${tipp.platform.id}">${fieldValue(bean: tipp, field: "platform.name")}</g:link></td>
              <td><g:link controller="package" action="show" id="${tipp.pkg.id}">${fieldValue(bean: tipp, field: "pkg.name")}</g:link></td>
              <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.startDate}" /></td>
              <td>${fieldValue(bean: tipp, field: "startVolume")}</td>
              <td>${fieldValue(bean: tipp, field: "startIssue")}</td>
              <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.endDate}" /></td>
              <td>${fieldValue(bean: tipp, field: "endVolume")}</td>
              <td>${fieldValue(bean: tipp, field: "endIssue")}</td>
              <td class="link">
                <g:link action="show" id="${tipp.id}" class="ui tiny button">${message('code':'default.button.show.label')}</g:link>
              </td>
            </tr>
          </g:each>
          </tbody>
        </table>

          <semui:paginate total="${titleInstancePackagePlatformInstanceTotal}" />


    </div>
  </body>
</html>
