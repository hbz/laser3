<%@ page import="de.laser.Platform;de.laser.PlatformAccessMethod;de.laser.helper.RDConstants" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'platform.label')}" />
        <title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="platform" action="index" message="platform.show.all" />
            <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
        </semui:breadcrumbs>

        <semui:modeSwitch controller="platform" action="show" params="${params}" />

        <h1 class="ui header la-noMargin-top">
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

        <g:form class="form" url="[controller: 'accessMethod', action: 'create']" method="POST">
            <table  class="ui celled la-js-responsive-table la-table table">
                <thead>
                        <tr>
                            <g:sortableColumn property="AccessMethod" title="${message(code: 'accessMethod.label')}" />
                            <g:sortableColumn property="validFrom" title="${message(code: 'accessMethod.valid_from')}" />
                            <g:sortableColumn property="validTo" title="${message(code: 'accessMethod.valid_to')}" />
                            <th>${message(code: 'accessMethod.actions')}</th>
                        </tr>
                </thead>
                <tbody>
                <g:each in="${platformAccessMethodList}" var="accessMethod">
                        <tr>
                            <td>${accessMethod.accessMethod.getI10n('value')}</td>
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${accessMethod.validFrom}" />
                            </td>
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${accessMethod.validTo}" />
                            </td>
                            <td class="link">
                                <g:link action="edit" controller="accessMethod" id="${accessMethod?.id}" class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                                <g:link action="delete" controller="accessMethod" id="${accessMethod?.id}" class="ui negative icon button"
                                        onclick="return confirm('${message(code: 'accessPoint.details.delete.confirm', args: ['aaaa'])}')"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="delete icon"></i>
                                </g:link>
                            </td>
                        </tr>
                </g:each>

                    <tr>
                        <td>
                            <laser:select class="ui dropdown values" id="accessMethod"
                                          name="accessMethod"
                                          from="${PlatformAccessMethod.getAllRefdataValues(RDConstants.ACCESS_METHOD)}"
                                          optionKey="id"
                                          optionValue="value"
                            />
                        </td>
                        <td>
                            <div class="field wide six">
                                <semui:datepicker hideLabel="true" id="validFrom" name="validFrom" value ="${params.validFrom}">
                                </semui:datepicker>
                            </div>
                        </td>
                        <td>
                            <div class="field wide six">
                                <semui:datepicker  hideLabel="true" id="validTo" name="validTo" value ="${params.validTo}">
                                </semui:datepicker>
                            </div>
                        </td>
                        <td>
                            <input type="hidden" name="platfId" value="${platformInstance.id}" />
                            <input type="Submit" class="ui tiny button" value="${message(code:'accessMethod.button.add')}" onClick="this.form.submit()"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </g:form>

    </body>
</html>
