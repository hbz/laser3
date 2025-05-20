<%@ page import="de.laser.ExportClickMeService; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService" %>
<laser:htmlStart message="menu.public.all_cons" />

        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.public.all_cons" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>
        <%
            editable = (editable && contextService.getOrg().isCustomerType_Inst_Pro()) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        %>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.CONSORTIAS]"/>
            </ui:exportDropdownItem>
            <%--
            <g:if test="${filterSet}">
                <ui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisation" action="listConsortia"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </ui:exportDropdownItem>
                <ui:exportDropdownItem>
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="organisation" action="listConsortia"
                            params="${params+[format:'csv']}">
                        ${message(code:'default.button.exports.csv')}
                    </g:link>
                </ui:exportDropdownItem>
            </g:if>
            <g:else>
                <ui:exportDropdownItem>
                    <g:link class="item" action="listConsortia" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                </ui:exportDropdownItem>
                <ui:exportDropdownItem>
                    <g:link class="item" action="listConsortia" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                </ui:exportDropdownItem>
            </g:else>
            --%>
        </ui:exportDropdown>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.public.all_cons" total="${consortiaTotal}" floated="true" type="consortium" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="listConsortia" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [['name', 'isMyX']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <laser:render template="/templates/filter/orgFilterTable"
              model="[orgList: availableOrgs,
                      currentConsortiaIdList: consortiaIds,
                      tmplShowCheckbox: false,
                      tmplConfigShow: [
                              'sortname', 'name', 'isMyX'
                      ]
              ]"/>

    <ui:paginate action="listConsortia" params="${params}" max="${max}" total="${consortiaTotal}" />

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
