<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : ${message(code: 'menu.public.all_insts')}</title>
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
            editable = (editable && accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')) || contextService.getUser()?.hasRole('ROLE_ADMIN,ROLE_ORG_EDITOR') || accessService.checkConstraint_ORG_COM_EDITOR()
        %>
        <g:if test="${editable}">
            <g:render template="actions" />
        </g:if>
    </semui:controlButtons>
    
    <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code: 'menu.public.all_insts')}</h1>

    <semui:messages data="${flash}" />

    <g:render template="../templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="listInstitution" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'identifier'], ['federalState', 'libraryNetwork', 'libraryType']],
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
                              'sortname', 'name', 'wibid', 'isil', 'federalState', 'libraryNetwork', 'libraryType', 'legalInformation',
                              (editable ? 'consortiaToggle' : '')
                      ]
              ]"/>



  </body>
</html>
