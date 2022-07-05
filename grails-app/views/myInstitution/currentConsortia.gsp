<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.consortia')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb text="${message(code: 'menu.my.consortia')}" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <%-- needs to be done separately if needed at all
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
        </semui:exportDropdownItem>
        <g:if test="${filterSet}">
            <semui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageMembers"
                        params="${params+[exportXLS:true]}">
                    ${message(code:'default.button.exports.xls')}
                </g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageMembers"
                        params="${params+[format:'csv']}">
                    ${message(code:'default.button.exports.csv')}
                </g:link>
            </semui:exportDropdownItem>
        </g:if>
        <g:else>
            <semui:exportDropdownItem>
                <g:link class="item" action="manageMembers" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="manageMembers" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </semui:exportDropdownItem>
        </g:else>
    </semui:exportDropdown>--%>
    <g:if test="${editable}">
        <laser:render template="actions"/>
    </g:if>
</semui:controlButtons>

<semui:headerWithIcon message="menu.my.consortia" floated="true">
    <semui:totalNumber total="${consortiaCount}"/>
</semui:headerWithIcon>

<laser:render template="/templates/filter/javascript" />

<semui:messages data="${flash}"/>
    <%
        List configShowFilter = [['name']]
        List configShowTable = ['sortname', 'name', 'mainContact', 'numberOfSubscriptions', 'numberOfSurveys']
    %>

    <semui:filter showFilterButton="true">
        <g:form action="currentConsortia" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: configShowFilter,
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>
<div class="la-clear-before">
    <g:if test="${consortia}">
        <laser:render template="export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'institution']" />

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
    <semui:paginate action="currentConsortia" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${consortiaCount}" />

    <semui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

</body>
</html>
