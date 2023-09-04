<%@ page import="de.laser.storage.RDStore" %>
<laser:htmlStart message="menu.my.consortia" serviceInjection="true"/>

    <g:set var="entityName" value="${message(code: 'org.label')}"/>

<ui:breadcrumbs>
    <ui:crumb text="${message(code: 'menu.my.consortia')}" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
        </ui:exportDropdownItem>
        <g:if test="${filterSet}">
            <%--
            <ui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentConsortia"
                        params="${params+[exportXLS:true]}">
                    ${message(code:'default.button.exports.xls')}
                </g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentConsortia"
                        params="${params+[format:'csv']}">
                    ${message(code:'default.button.exports.csv')}
                </g:link>
            </ui:exportDropdownItem>
            --%>
        </g:if>
        <g:else>
            <%--
            <ui:exportDropdownItem>
                <g:link class="item" action="currentConsortia" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" action="currentConsortia" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </ui:exportDropdownItem>
            --%>
        </g:else>
    </ui:exportDropdown>
    <g:if test="${editable}">
        <laser:render template="actions"/>
    </g:if>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="menu.my.consortia" total="${consortiaCount}" floated="true" />

<ui:messages data="${flash}"/>
    <%
        List configShowFilter = [['name']]
        List configShowTable = ['sortname', 'name', 'mainContact', 'numberOfSubscriptions', 'numberOfSurveys']
    %>

    <ui:filter>
        <g:form action="currentConsortia" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: configShowFilter,
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>
<div class="la-clear-before">
    <g:if test="${consortia}">
        <laser:render template="export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'consortium', contactSwitch: true]" />

            <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList: consortia,
                              invertDirection: true,
                              comboType: comboType,
                              tmplConfigShow: configShowTable
                      ]"/>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:'consortium.plural')]}"/></strong>
    </g:if>
    <g:else>
        <br /><strong><g:message code="result.empty.object" args="${[message(code:'consortium.plural')]}"/></strong>
    </g:else>
</g:else>

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: totalConsortia]"/>
    <ui:paginate action="currentConsortia" controller="myInstitution" params="${params}" max="${max}" total="${consortiaCount}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

<laser:htmlEnd />
