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
            <laser:render template="actions" />
        </g:if>
    </semui:controlButtons>

    <semui:h1HeaderWithIcon message="menu.public.all_cons" total="${consortiaTotal}" floated="true" />

    <semui:messages data="${flash}" />

    <laser:render template="/templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="listConsortia" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <laser:render template="/templates/filter/orgFilterTable"
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
