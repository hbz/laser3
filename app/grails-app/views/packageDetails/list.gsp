
<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>


    <h1 class="ui header">${message(code:'package.search')}</h1>

    <semui:messages data="${flash}" />

  <semui:filter>
      <g:form action="list" method="get" class="form-inline">
         <table class="ui celled table">
           <tbody>
            <tr>
              <td style="white-space:nowrap">
                <label>${message(code:'package.search.text')} : </label> <input type="text" name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q?.encodeAsHTML()}"  /> &nbsp;
              </td>
              <td style="width:31%;text-align:right;">
                <div style="padding:5px 5px;white-space:nowrap;">
                  <label>${message(code:'package.search.updated_after')} : </label> <g:simpleHiddenValue id="updateStartDate" name="updateStartDate" type="date"/>
                </div>
                <div style="padding:5px 5px;white-space:nowrap;">
                  <label>${message(code:'package.search.created_after')} : </label> <g:simpleHiddenValue id="createStartDate" name="createStartDate" type="date"/>
                </div>
              </td>
              <td style="width:31%;text-align:right;">
                <div style="padding:5px 5px;white-space:nowrap;">
                  <label>${message(code:'package.search.updated_before')} : </label> <g:simpleHiddenValue id="updateEndDate" name="updateEndDate" type="date"/>
                </div>
                <div style="padding:5px 5px;white-space:nowrap;">
                  <label>${message(code:'package.search.created_before')} : </label> <g:simpleHiddenValue  id="createEndDate" name="createEndDate" type="date"/>
                </div>
              </td>
              <td>
                <input type="submit" class="ui primary button" value="${message(code:'default.button.search.label')}" style="margin:10px 5px;min-width:100px;font-size:1.3em;"></input>
                <button type="submit" name="format" value="csv" class="ui primary button" value="Search"  style="margin:10px 5px;">${message(code:'default.button.exports.csv')}</button>
              </td>
            </tr>
           </tbody>
        </table>
      </g:form>
  </semui:filter>

        
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
              <td>${packageInstance.identifier}</td>
              <td>${packageInstance.name}</td>
              <td><g:formatDate date="${packageInstance.dateCreated}" format="${message(code:'default.date.format', default:'yyyy-MM-dd HH:mm:ss z')}"/></td>
              <td><g:formatDate date="${packageInstance.lastUpdated}" format="${message(code:'default.date.format', default:'yyyy-MM-dd HH:mm:ss z')}"/></td>
              <td class="link">
                <g:link action="show" id="${packageInstance.id}" class="ui tiny button">${message(code:'package.search.show')} &raquo;</g:link>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <bootstrap:paginate  action="list" controller="packageDetails" params="${params}" next="Next" prev="Prev" max="${max}" total="${packageInstanceTotal}" />
      </div>

  </body>
</html>
