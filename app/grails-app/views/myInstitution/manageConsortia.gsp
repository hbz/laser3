<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'menu.institutions.manage_consortia')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="menu.institutions.manage_consortia" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="manageConsortia" params="${params+[exportXLS:'yes']}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.institutions.manage_consortia')}</h1>

<semui:messages data="${flash}"/>

    <semui:filter>
        <g:form action="manageConsortia" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: ['property', 'name', 'federalState', 'libraryNetwork', 'libraryType'],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>


    <g:form action="manageConsortia" controller="myInstitution" method="post" class="ui form">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: consortiaMembers,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['sortname', 'name', 'mainContact', 'currentFTEs', 'numberOfLicenses', 'wibid', 'isil', 'federalState', 'libraryNetwork', 'libraryType']
                  ]"/>

        <br/>

        <g:if test="${consortiaMembers}">
            <input type="submit" class="ui button"
               value="${message(code: 'default.button.revoke.label', default: 'Revoke')}"/>
        </g:if>
    </g:form>
    <g:render template="../templates/copyEmailaddresses" model="[orgList: consortiaMembers]"/>

</body>
</html>
