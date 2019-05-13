<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.institutions.add_consortia_members')}</title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.public.all_insts" class="active" />
    </semui:breadcrumbs>

    <semui:controlButtons>
        <%--<semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="addMembers" params="${params+[exportXLS:'yes']}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>--%>

        <%
            editable = (editable && accessService.checkPerm('ORG_BASIC,ORG_CONSORTIUM')) || contextService.getUser()?.hasRole('ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()
        %>
        <g:if test="${editable}">
            <g:render template="actions" />
        </g:if>
    </semui:controlButtons>
    
    <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.public.all_insts')}</h1>

    <semui:messages data="${flash}" />

    <semui:filter>
        <g:form action="listInstitution" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name'], ['federalState', 'libraryNetwork', 'libraryType']],
                              tmplConfigFormFilter: true,
                              useNewLayouter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: availableOrgs,
                      consortiaMemberIds: consortiaMemberIds,
                      tmplShowCheckbox: false,
                      tmplConfigShow: [
                              'sortname', 'name', 'wibid', 'isil', 'federalState', 'libraryNetwork', 'libraryType',
                              (editable ? 'consortiaToggle' : '')
                      ]
              ]"/>



  </body>
</html>
