<%@ page import="de.laser.Package" %>
<laser:htmlStart message="myinst.packages" />

    <g:set var="entityName" value="${message(code: 'package.label')}" />

  <ui:breadcrumbs>
      <ui:crumb message="myinst.packages" class="active"/>
  </ui:breadcrumbs>

  <ui:controlButtons>
      <laser:render template="actions"/>
  </ui:controlButtons>

    <ui:h1HeaderWithIcon message="myinst.packages" total="${packageInstanceTotal}" floated="true" />

    <ui:messages data="${flash}" />

  <ui:filter showFilterButton="true" addFilterJs="true">
          <g:form action="list" method="get" class="ui form">

              <div class="field">
                  <label for="q">${message(code:'package.search.text')}</label>
                  <input type="text" id="q"  name="q" placeholder="${message(code:'package.search.ph')}" value="${params.q}" />
              </div>

              <div class="four fields">
                  <ui:datepicker label="package.search.updated_after" id="updateStartDate" name="updateStartDate" value="${params.updateStartDate}" />

                  <ui:datepicker label="package.search.created_after" id="createStartDate" name="createStartDate" value="${params.createStartDate}" />

                  <ui:datepicker label="package.search.updated_before" id="updateEndDate" name="updateEndDate" value="${params.updateEndDate}" />

                  <ui:datepicker label="package.search.created_before" id="createEndDate" name="createEndDate" value="${params.createEndDate}" />
              </div>


              <div class="field la-field-right-aligned">
              <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                  <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
              </div>

          </g:form>
    </ui:filter>

        
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

        <ui:paginate action="list" controller="package" params="${params}" max="${max}" total="${packageInstanceTotal}" />

<laser:htmlEnd />
