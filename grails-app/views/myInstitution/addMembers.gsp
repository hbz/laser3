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

    <semui:h1HeaderWithIcon message="menu.institutions.add_consortia_members" />

    <semui:messages data="${flash}" />

    <semui:filter>
        <g:form action="addMembers" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name'], ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:if test="${availableOrgs}">
    <g:form action="addMembers" controller="myInstitution" method="post" class="ui form">

        <laser:render template="/templates/filter/orgFilterTable"
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
