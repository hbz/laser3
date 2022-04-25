<%@ page import="de.laser.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'package.label')}" />
    <title>${message(code:'laser')} : ${message(code:'myinst.packages')}</title>
  </head>
  <body>

  <semui:breadcrumbs>
      <semui:crumb message="myinst.packages" class="active"/>
  </semui:breadcrumbs>

  <semui:controlButtons>
      <laser:render template="actions"/>
  </semui:controlButtons>

  <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'myinst.packages')}
    <semui:totalNumber total="${packageInstanceTotal}"/>
  </h1>

    <semui:messages data="${flash}" />

  <laser:render template="/templates/filter/javascript" />
  <semui:filter showFilterButton="true">
          <g:form action="list" method="get" class="ui form">

              <div class="field">
                  <label for="q">${message(code:'package.search.text')}</label>
                  <input type="text" id="q"  name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q}" />
              </div>

              <div class="four fields">
                  <semui:datepicker label="package.search.updated_after" id="updateStartDate" name="updateStartDate" value="${params.updateStartDate}" />

                  <semui:datepicker label="package.search.created_after" id="createStartDate" name="createStartDate" value="${params.createStartDate}" />

                  <semui:datepicker label="package.search.updated_before" id="updateEndDate" name="updateEndDate" value="${params.updateEndDate}" />

                  <semui:datepicker label="package.search.created_before" id="createEndDate" name="createEndDate" value="${params.createEndDate}" />
              </div>


              <div class="field la-field-right-aligned">
              <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                  <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
              </div>

          </g:form>
    </semui:filter>

        
        <table class="ui sortable celled la-js-responsive-table la-table table">
            <thead>
                <tr>
                    <th>${message(code:'sidewide.number')}</th>
                    <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
                    <th>
                        ${message(code: 'package.content_provider')}
                    </th>
                    <g:sortableColumn property="dateCreated" title="${message(code: 'package.dateCreated.label')}" />
                    <g:sortableColumn property="lastUpdated" title="${message(code: 'package.lastUpdated.label')}" />
                </tr>
            </thead>
            <tbody>
                <g:each in="${packageInstanceList}" var="packageInstance" status="jj">
                    <tr>
                        <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                        <td>
                            <g:link action="show" id="${packageInstance.id}">${packageInstance.name}</g:link>
                        </td>
                        <td>
                            <g:each in="${packageInstance.orgs}" var="orgLink">
                                <g:link controller="organisation" action="show" id="${orgLink.org.id}">${orgLink.org.name}</g:link> <br />
                            </g:each>
                        </td>
                        <td><g:formatDate date="${packageInstance.dateCreated}" format="${message(code:'default.date.format.noZ')}"/></td>
                        <td><g:formatDate date="${packageInstance.lastUpdated}" format="${message(code:'default.date.format.noZ')}"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <semui:paginate action="list" controller="package" params="${params}" next="Next" prev="Prev" max="${max}" total="${packageInstanceTotal}" />

    </body>
</html>
