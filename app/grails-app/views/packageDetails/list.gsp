<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.packages')}</title>
  </head>
  <body>

  <semui:breadcrumbs>
      <semui:crumb message="myinst.packages" class="active"/>
  </semui:breadcrumbs>

  <semui:controlButtons>
      <semui:exportDropdown>
          <semui:exportDropdownItem>
              <g:link class="item" action="list" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
          </semui:exportDropdownItem>
      </semui:exportDropdown>
      <g:render template="actions"/>
  </semui:controlButtons>

  <h1 class="ui header"><semui:headerIcon />${message(code:'myinst.packages')}</h1>

    <semui:messages data="${flash}" />

    <semui:filter>
          <g:form action="list" method="get" class="ui form">

              <div class="field">
                  <label>${message(code:'package.search.text')}</label>
                  <input type="text" name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q}" />
              </div>

              <div class="fields">
                  <semui:datepicker label="package.search.updated_after" name="updateStartDate" value="${params.updateStartDate}" />

                  <semui:datepicker label="package.search.created_after" name="createStartDate" value="${params.createStartDate}" />

                  <semui:datepicker label="package.search.updated_before" name="updateEndDate" value="${params.updateEndDate}" />

                  <semui:datepicker label="package.search.created_before" name="createEndDate" value="${params.createEndDate}" />
              </div>

              <div class="fields">
                  <div class="field">
                  <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                    </div>
                  <div class="field">
                      <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label')}" />
                  </div>
              </div>
          </g:form>
    </semui:filter>

        
        <table class="ui sortable celled la-table table">
            <thead>
                <tr>
                    <g:sortableColumn property="name" title="${message(code: 'package.name.label', default: 'Name')}" />
                    <th>
                        ${message(code: 'package.content_provider')}
                    </th>
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
                            <g:each in="${packageInstance.orgs}" var="orgLink">
                                <g:link controller="organisations" action="show" id="${orgLink.org.id}">${orgLink.org.name}</g:link> <br/>
                            </g:each>
                        </td>
                        <td><g:formatDate date="${packageInstance.dateCreated}" format="${message(code:'default.date.format.noZ', default:'yyyy-MM-dd HH:mm:ss')}"/></td>
                        <td><g:formatDate date="${packageInstance.lastUpdated}" format="${message(code:'default.date.format.noZ', default:'yyyy-MM-dd HH:mm:ss')}"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <semui:paginate action="list" controller="packageDetails" params="${params}" next="Next" prev="Prev" max="${max}" total="${packageInstanceTotal}" />

    </body>
</html>
