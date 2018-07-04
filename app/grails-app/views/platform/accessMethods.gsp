<%@ page import="com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="platform" action="index" message="platform.show.all" />
            <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
        </semui:breadcrumbs>

        <semui:modeSwitch controller="platform" action="show" params="${params}" />

        <h1 class="ui header">
            <g:if test="${editable}"><span id="platformNameEdit"
                                           class="xEditableValue"
                                           data-type="textarea"
                                           data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                           data-name="name"
                                           data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
            </g:if>
            <g:else>${platformInstance.name}</g:else>
        </h1>

        <semui:messages data="${flash}" />
        
        <g:render template="nav" contextPath="." />
        
       %{-- <semui:filter>
            <g:form action="accessMethods" role="form" class="ui form" method="get" params="${params}">
              <div class="fields">
                <div class="field">
                  <label for="filter" class="control-label">${message(code: 'accessMethod.show')}</label>
                  <g:select id="filter"
                            name="filter" 
                            from="${[
                                        [key:'all',value:"${message(code: 'accessMethod.all')}"],
                                        [key:'valid',value:"${message(code:'accessMethod.valid')}"],
                                        [key:'invalid',value:"${message(code: 'accessMethod.invalid')}"]
                                   ]}" 
                            optionKey="key" optionValue="value" value="${params.filter}" 
                            onchange="this.form.submit()"/>
                </div>
              </div>
            </g:form>
        </semui:filter>--}%

        <g:form class="form" url="[controller: 'accessMethod', action: 'create']" method="POST">
            <table  class="ui celled la-table table">
                <thead>
                        <tr>
                            <g:sortableColumn property="AccessMethod" title="${message(code: 'accessMethod.label', default: 'Access Method')}" />
                            <g:sortableColumn property="validFrom" title="${message(code: 'accessMethod.valid_from', default: 'Valid From')}" />
                            <g:sortableColumn property="validTo" title="${message(code: 'accessMethod.valid_to', default: 'Valid From')}" />
                            <th>${message(code: 'accessMethod.actions', default: 'Actions')}</th>
                        </tr>
                </thead>
                <tbody>
                <g:each in="${platformAccessMethodList}" var="accessMethod">
                        <tr>
                            <td>${accessMethod.accessMethod.getI10n('value')}</td>
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${accessMethod.validFrom}" />
                            </td>
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${accessMethod.validTo}" />
                            </td>
                            <td class="link">
                                <g:link action="edit" controller="accessMethod" id="${accessMethod?.id}" class="ui icon button">
                                    <i class="write icon"></i>
                                </g:link>
                                <g:link action="delete" controller="accessMethod" id="${accessMethod?.id}" class="ui negative icon button"
                                        onclick="return confirm('${message(code: 'accessPoint.details.delete.confirm', args: ['aaaa'])}')">
                                    <i class="delete icon"></i>
                                </g:link>
                            </td>
                        </tr>
                </g:each>

                    <tr>
                        <td>
                            <laser:select class="ui dropdown values" id="accessMethod"
                                          name="accessMethod"
                                          from="${com.k_int.kbplus.PlatformAccessMethod.getAllRefdataValues('Access Method')}"
                                          optionKey="id"
                                          optionValue="value"
                            />
                        </td>
                        <td>
                            <div class="field wide six fieldcontain ">
                                <semui:datepicker hideLabel="true" name="validFrom" value ="${params.validFrom}">
                                </semui:datepicker>
                            </div>
                        </td>
                        <td>
                            <div class="field wide six fieldcontain  ">
                                <semui:datepicker  hideLabel="true" name="validTo" value ="${params.validTo}">
                                </semui:datepicker>
                            </div>
                        </td>
                        <td>
                            <input type="hidden" name="platfId" value="${platformInstance.id}" />
                            <input type="Submit" class="ui tiny button" value="${message(code:'accessMethod.button.add', default:'Add')}" onClick="this.form.submit()"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </g:form>



         %{--<input class="ui button"--}%
                       %{--value="${message(code: 'accessMethod.add.label', args: [message(code: 'accessMethod.add.label', default: 'Adresse')])}"--}%
                       %{--data-semui="modal"--}%
                       %{--href="#accessMethodFormModal" />--}%
                %{--<g:render template="/accessMethod/formModal" model="['platfId': platformInstance?.id, 'redirect': '.']"/>--}%
    </body>
</html>
