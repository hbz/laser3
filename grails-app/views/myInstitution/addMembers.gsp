<%@page import="de.laser.FormService" %>
<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : ${message(code: 'menu.institutions.add_consortia_members')}</title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
        <semui:crumb message="menu.institutions.manage_consortia" controller="myInstitution" action="manageMembers"/>
        <semui:crumb message="menu.institutions.add_consortia_members" class="active" />
    </semui:breadcrumbs>

    <%--<semui:controlButtons>
        <semui:exportDropdown>
            <g:if test="${filterSet}">
                <semui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="myInstitution" action="addMembers"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="myInstitution" action="addMembers"
                            params="${params+[format:'csv']}">
                        ${message(code:'default.button.exports.csv')}
                    </g:link>
                </semui:exportDropdownItem>
            </g:if>
            <g:else>
                <semui:exportDropdownItem>
                    <g:link class="item" action="addMembers" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item" action="addMembers" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                </semui:exportDropdownItem>
            </g:else>
        </semui:exportDropdown>
        <g:render template="actions" />
    </semui:controlButtons>--%>
    
    <h1 class="ui left aligned icon header la-clear-before"><semui:headerIcon />${message(code: 'menu.institutions.add_consortia_members')}</h1>

    <semui:messages data="${flash}" />

    <semui:filter>
        <g:form action="addMembers" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name'], ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:if test="${availableOrgs}">
    <g:form action="addMembers" controller="myInstitution" method="post" class="ui form">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: availableOrgs,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['sortname', 'name', 'wibid', 'isil', 'region', 'libraryNetwork', 'libraryType']
                  ]"/>

        <br />
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <input type="submit" class="ui button" value="${message(code:'default.button.add.label')}" />
    </g:form>
    </g:if>
    <g:else>
        <g:if test="${filterSet}">
            <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"myinst.consortiaSubscriptions.consortia")]}"/></strong>
        </g:if>
        <g:else>
            <br /><strong><g:message code="result.empty.object" args="${[message(code:"myinst.consortiaSubscriptions.consortia")]}"/></strong>
        </g:else>
    </g:else>

  </body>
</html>
