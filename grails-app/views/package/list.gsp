<%@ page import="de.laser.ui.Btn; de.laser.wekb.Package" %>
<laser:htmlStart text="${message(code: 'myinst.packages')} - ${message(code: 'default.onlyDatabase')}" />

    <g:set var="entityName" value="${message(code: 'package.label')}" />

    <ui:breadcrumbs>
        <ui:crumb text="${message(code: 'myinst.packages')} - ${message(code: 'default.onlyDatabase')}" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="${message(code: 'myinst.packages')} - ${message(code: 'default.onlyDatabase')}" total="${packageInstanceTotal}" floated="true" />

    <ui:messages data="${flash}" />

  <ui:filter>
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
              <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                  <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}" />
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
                            <g:if test="${packageInstance.provider}">
                                <g:link controller="provider" action="show" id="${packageInstance.provider.id}">${packageInstance.provider.name}</g:link>
                            </g:if>
                        </td>
                        <td><g:formatDate date="${packageInstance.dateCreated}" format="${message(code:'default.date.format.noZ')}"/></td>
                        <td><g:formatDate date="${packageInstance.lastUpdated}" format="${message(code:'default.date.format.noZ')}"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <ui:paginate action="list" controller="package" params="${params}" max="${max}" total="${packageInstanceTotal}" />

<laser:htmlEnd />
