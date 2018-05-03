<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>

        <g:javascript src="properties.js"/>
    </head>
    <body>
    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>
    <h1 class="ui header">
        <semui:editableLabel editable="${editable}" />
        ${orgInstance.name}
    </h1>

    <g:render template="nav" contextPath="." />

    <semui:messages data="${flash}" />
    
    
    <semui:filter>
            <g:form action="accessPoints" role="form" class="ui form" method="get" params="${params}">
              <div class="fields">
                <div class="field">
                  <label for="filter" class="control-label">${message(code: 'accessPoint.show')}</label>
                  <g:select id="filter" 
                            name="filter" 
                            from="${[
                                        [key:'all',value:"${message(code: 'accessPoint.all')}"],
                                        [key:'valid',value:"${message(code:'accessPoint.valid')}"],
                                        [key:'invalid',value:"${message(code: 'accessPoint.invalid')}"]
                                   ]}" 
                            optionKey="key" optionValue="value" value="${params.filter}" 
                            onchange="this.form.submit()"/>
                </div>
              </div>
            </g:form>
        </semui:filter>

        <g:form class="ui form" url="[controller: 'accessPoint', action: 'create']" method="POST">
            <table class="ui celled striped table">
                <thead>
                        <tr>
                            <g:sortableColumn property="AccessPoint" title="${message(code: 'accessPoint.name', default: 'Name')}" />
                            <g:sortableColumn property="accessMode" title="${message(code: 'accessMethod.label', default: 'Access Method')}" />
                            <g:sortableColumn property="rules" title="${message(code: 'accessRule.plural', default: 'Access Rules')}" />
                            <th>${message(code: 'actions', default: 'Actions')}</th>
                        </tr>
                </thead>
                <tbody>
                    <g:each in="${orgAccessPointList.sort{it.name+it.accessMethod}}" var="accessPoint">
                        <tr>
                            <td>${accessPoint.name}</td>
                            <td>${accessPoint.accessMethod}</td>
                            <td>###</td>
                            <td>
                                <g:link action="edit_${accessPoint.accessMethod.value.toLowerCase()}" controller="accessPoint" id="${accessPoint?.id}" class="ui tiny button">${message(code:'default.button.edit.label', default:'Edit')}</g:link>
                                <g:link action="delete" controller="accessPoint" id="${accessPoint.id}" class="ui tiny button">${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                            </td>
                        </tr>
                    </g:each>

                    <tr>
                        <td>
                            <div class="${hasErrors(bean: accessPoint, field: 'name', 'error')} required ui form">
                                <g:textField name="name" required="" value="${accessPoint?.name}"/>
                            </div>
                        </td>
                        <td>
                            <laser:select class="ui dropdown values" id="accessMethod"
                                          name="accessMethod"
                                          from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues('Access Method')}"
                                          optionKey="id"
                                          optionValue="value"
                            />
                        </td>
                        <td></td>
                        <td>
                            <input type="hidden" name="orgId" value="${orgInstance.id}" />
                            <input type="Submit" class="ui tiny button" value="${message(code:'default.button.new.label', default:'New')}" onClick="this.form.submit()"class="ui button"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </g:form>
  </body>
</html>
