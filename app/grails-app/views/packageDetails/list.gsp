
<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>

    <div class="container">
      <div class="page-header">
        <h1>${message(code:'package.search')}</h1>
      </div>
      <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </g:if>
    </div>

    <div class="container" style="text-align:center">
      <g:form action="list" method="get" class="form-inline">
        <table>
          <tr>
            <td>
              <label>${message(code:'package.search.text')} : </label> <input type="text" name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q?.encodeAsHTML()}"  /> &nbsp;
            </td>
            <td style="width:30%;text-align:right;">
              <div style="padding:5px 5px;white-space:nowrap;">
                <label>${message(code:'package.search.updated_after')} : </label> <input name="updateStartDate" type="date" value="${params.updateStartDate}"/>
              </div>
              <div style="padding:5px 5px;white-space:nowrap;">
                <label>${message(code:'package.search.created_after')} : </label> <input name="createStartDate" type="date" value="${params.createStartDate}"/>
              </div>
            </td>
            <td style="width:30%;text-align:right;">
              <div style="padding:5px 5px;white-space:nowrap;">
                <label>${message(code:'package.search.updated_before')} : </label> <input name="updateEndDate" type="date" value="${params.updateEndDate}"/>
              </div>
              <div style="padding:5px 5px;white-space:nowrap;">
                <label>${message(code:'package.search.created_before')} : </label> <input name="createEndDate" type="date" value="${params.createEndDate}"/>
              </div>
            </td>
            <td>
              <input type="submit" class="btn btn-primary" value="${message(code:'default.button.search.label')}" style="margin:5px 5px;"></input>
              <button type="submit" name="format" value="csv" class="btn btn-primary" value="Search"  style="margin:5px 5px;">${message(code:'package.search.export.button')}</button>
            </td>
          </tr>
        </table>
      </g:form><br/>
    </div>

    <div class="container">
        
      <table class="ui celled striped table">
        <thead>
          <tr>
            <g:sortableColumn property="identifier" title="${message(code: 'package.identifier.label', default: 'Identifier')}" />
            <g:sortableColumn property="name" title="${message(code: 'package.name.label', default: 'Name')}" />
            <g:sortableColumn property="dateCreated" title="${message(code: 'package.dateCreated.label', default: 'Created')}" />
            <g:sortableColumn property="lastUpdated" title="${message(code: 'package.lastUpdated.label', default: 'Last Updated')}" />
            <th></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${packageInstanceList}" var="packageInstance">
            <tr>
              <td>${fieldValue(bean: packageInstance, field: "identifier")}</td>
              <td>${fieldValue(bean: packageInstance, field: "name")}</td>
              <td>${fieldValue(bean: packageInstance, field: "dateCreated")}</td>
              <td>${fieldValue(bean: packageInstance, field: "lastUpdated")}</td>
              <td class="link">
                <g:link action="show" id="${packageInstance.id}" class="btn btn-small">${message(code:'package.search.show')} &raquo;</g:link>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <bootstrap:paginate  action="list" controller="packageDetails" params="${params}" next="Next" prev="Prev" max="${max}" total="${packageInstanceTotal}" />
      </div>
    </div>
  </body>
</html>
