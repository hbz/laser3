<laser:htmlStart message="menu.public.all_cons" serviceInjection="true"/>

        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.public.all_cons" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>

        <%
            editable = (editable && accessService.checkPerm('ORG_INST')) || contextService.getUser()?.hasRole('ROLE_ADMIN,ROLE_ORG_EDITOR')
        %>
        <g:if test="${editable}">
            <laser:render template="actions" />
        </g:if>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.public.all_cons" total="${consortiaTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter showFilterButton="true" addFilterJs="true">
        <g:form action="listConsortia" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <laser:render template="/templates/filter/orgFilterTable"
              model="[orgList: availableOrgs,
                      consortiaIds: consortiaIds,
                      tmplShowCheckbox: false,
                      tmplConfigShow: [
                              'sortname', 'name'
                      ]
              ]"/>

    <ui:paginate action="listConsortia" params="${params}" max="${max}" total="${consortiaTotal}" />

<laser:htmlEnd />
