<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : ${message(code: 'menu.public.all_cons')}</title>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.public.all_cons" class="active" />
    </semui:breadcrumbs>

    <semui:controlButtons>

        <%
            editable = (editable && accessService.checkPerm('ORG_INST')) || contextService.getUser()?.hasRole('ROLE_ADMIN,ROLE_ORG_EDITOR')
        %>
        <g:if test="${editable}">
            <g:render template="actions" />
        </g:if>
    </semui:controlButtons>
    
    <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code: 'menu.public.all_cons')}
        <semui:totalNumber total="${consortiaTotal}"/>
    </h1>

    <semui:messages data="${flash}" />

    <g:render template="/templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="listConsortia" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: availableOrgs,
                      consortiaIds: consortiaIds,
                      tmplShowCheckbox: false,
                      tmplConfigShow: [
                              'sortname', 'name'
                      ]
              ]"/>

    <semui:paginate action="listConsortia" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${consortiaTotal}" />

  </body>
</html>
