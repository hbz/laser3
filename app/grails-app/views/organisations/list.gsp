<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>

        <h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

        <semui:filter>
            <g:form action="list" method="get" class="ui form">
                <div class="field">
                    <label>${message(code: 'org.search.contains')}</label>
                    <input type="text" name="orgNameContains" value="${params.orgNameContains}"/>
                </div>
                <div class="fields">
                    <div class="field">
                        <label>${message(code: 'org.orgType.label')}</label>
                        <g:select name="orgType" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                                  from="${RefdataCategory.getAllRefdataValues('OrgType')}" value="${params.orgType}" optionKey="id" optionValue="value"/>
                    </div>
                    <div class="field">
                        <label>${message(code: 'org.sector.label')}</label>
                        <g:select name="orgSector" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                                  from="${RefdataCategory.getAllRefdataValues('OrgSector')}" value="${params.orgSector}" optionKey="id" optionValue="value"/>
                    </div>
                    <div class="field">
                        <label>${message(code: 'org.federalState.label')}</label>
                        <g:select name="federalState" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                                  from="${RefdataCategory.getAllRefdataValues('Federal State')}" value="${params.federalState}" optionKey="id" optionValue="value"/>
                    </div>
                    <div class="field">
                        <label>${message(code: 'org.libraryNetwork.label')}</label>
                        <g:select name="libraryNetwork" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                                  from="${RefdataCategory.getAllRefdataValues('Library Network')}" value="${params.libraryNetwork}" optionKey="id" optionValue="value"/>
                    </div>
                    <div class="field">
                        <label>${message(code: 'org.libraryType.label')}</label>
                        <g:select name="libraryType" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                                  from="${RefdataCategory.getAllRefdataValues('Library Type')}" value="${params.libraryType}" optionKey="id" optionValue="value"/>
                    </div>
                </div>
                <div class="field">
                    <label>${orgInstanceTotal} Matches</label>
                    <input type="submit" value="${message(code:'default.button.search.label')}" class="ui secondary button"/>
                </div>
            </g:form>
        </semui:filter>
        
        <table class="ui celled striped table">
            <thead>
                <tr>
                    <th>${message(code: 'org.name.label', default: 'Name')}</th>
                    <th>WIB</th>
                    <th>ISIL</th>
                    <th>${message(code: 'org.type.label', default: 'Type')}</th>
                    <th>${message(code: 'org.sector.label', default: 'Sector')}</th>
                    <th>${message(code: 'org.federalState.label')}</th>
                    <th>${message(code: 'org.libraryNetwork.label')}</th>
                    <th>${message(code: 'org.libraryType.label')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${orgInstanceList}" var="orgInstance">
                    <tr>
                        <td>
                            <g:link action="show" id="${orgInstance.id}">
                                <g:if test="${orgInstance.shortname}">
                                    ${fieldValue(bean: orgInstance, field: "shortname")}
                                </g:if>
                                <g:else>
                                    ${fieldValue(bean: orgInstance, field: "name")}
                                </g:else>
                            </g:link>
                        </td>
                        <td>${orgInstance.getIdentifierByType('wib')?.value}</td>
                        <td>${orgInstance.getIdentifierByType('isil')?.value}</td>
                        <td>${orgInstance?.orgType?.getI10n('value')}</td>
                        <td>${orgInstance?.sector?.getI10n('value')}</td>
                        <td>${orgInstance?.federalState?.getI10n('value')}</td>
                        <td>${orgInstance?.libraryNetwork?.getI10n('value')}</td>
                        <td>${orgInstance?.libraryType?.getI10n('value')}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <semui:paginate total="${orgInstanceTotal}" params="${params}" />

    </body>
</html>
