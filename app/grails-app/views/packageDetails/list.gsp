<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>

      <semui:breadcrumbs>
          <semui:crumb message="myinst.packages" class="active" />
      </semui:breadcrumbs>

    <g:render template="actions" />

    <h1 class="ui header">${message(code:'myinst.packages')}</h1>

    <semui:messages data="${flash}" />

    <semui:filter>
          <g:form action="list" method="get" class="ui form">
              <div class="fields">
                  <div class="field">
                      <label>${message(code:'package.search.text')}</label>
                      <input type="text" name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q?.encodeAsHTML()}" />
                  </div>
                  <div class="field">
                      <label>${message(code:'package.search.updated_after')}</label>
                      <semui:simpleHiddenValue id="updateStartDate" name="updateStartDate" type="date" />
                  </div>
                  <div class="field">
                      <label>${message(code:'package.search.created_after')}</label>
                      <semui:simpleHiddenValue id="createStartDate" name="createStartDate" type="date" />
                  </div>
                  <div class="field">
                      <label>${message(code:'package.search.updated_before')}</label>
                      <semui:simpleHiddenValue id="updateEndDate" name="updateEndDate" type="date" />
                  </div>
                  <div class="field">
                      <label>${message(code:'package.search.created_before')}</label>
                      <semui:simpleHiddenValue id="createEndDate" name="createEndDate" type="date" />
                  </div>
                  <div class="field">
                      <label>&nbsp;</label>
                      <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label')}" />
                  </div>
                  <div class="field">
                      <label>&nbsp;</label>
                      <button type="submit" name="format" value="csv" class="ui button" value="Search">${message(code:'default.button.exports.csv')}</button>
                  </div>
              </div>
          </g:form>
    </semui:filter>

        
        <table class="ui celled striped table">
            <thead>
                <tr>
                    <g:sortableColumn property="name" title="${message(code: 'package.name.label', default: 'Name')}" />
                    <g:sortableColumn property="identifier" title="${message(code: 'package.identifier.label', default: 'Identifier')}" />
                    <g:sortableColumn property="dateCreated" title="${message(code: 'package.dateCreated.label', default: 'Created')}" />
                    <g:sortableColumn property="lastUpdated" title="${message(code: 'package.lastUpdated.label', default: 'Last Updated')}" />
                </tr>
            </thead>
            <tbody>
                <g:each in="${packageInstanceList}" var="packageInstance">
                    <tr>
                        <td>
                            <g:link action="show" id="${packageInstance.id}">${packageInstance.name}</g:link>
                        </td>
                        <td>
                            ${packageInstance.identifier}
                        </td>
                        <td><g:formatDate date="${packageInstance.dateCreated}" format="${message(code:'default.date.format', default:'yyyy-MM-dd HH:mm:ss z')}"/></td>
                        <td><g:formatDate date="${packageInstance.lastUpdated}" format="${message(code:'default.date.format', default:'yyyy-MM-dd HH:mm:ss z')}"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <semui:paginate action="list" controller="packageDetails" params="${params}" next="Next" prev="Prev" max="${max}" total="${packageInstanceTotal}" />

    </body>
</html>
