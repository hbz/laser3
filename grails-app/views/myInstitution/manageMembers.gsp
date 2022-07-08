<%@ page import="de.laser.storage.RDStore" %>

    <g:set var="entityName" value="${message(code: 'org.label')}"/>
    <g:set var="title" value="${message(code: 'menu.institutions.manage_consortia')}"/>
    <g:set var="memberPlural" value="${message(code: 'consortium.member.plural')}"/>

<laser:htmlStart text="${title}" serviceInjection="true" />

<semui:breadcrumbs>
    <semui:crumb text="${title}" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
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
    </semui:exportDropdown>
    <g:if test="${editable}">
        <laser:render template="actions"/>
    </g:if>
</semui:controlButtons>

<semui:h1HeaderWithIcon text="${title}" total="${membersCount}" floated="true" />

<laser:render template="/templates/filter/javascript" />

<semui:messages data="${flash}"/>
    <%
        List configShowFilter = [['name', 'identifier', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['subStatus', 'subValidOn'], ['subPerpetualAccess']]
        List configShowTable = ['sortname', 'name', 'status', 'mainContact', 'libraryType', 'legalInformation', 'numberOfSubscriptions', 'numberOfSurveys']
    %>

    <semui:filter showFilterButton="true">
        <g:form action="manageMembers" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: configShowFilter,
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>
<div class="la-clear-before">
    <g:if test="${members}">
        <laser:render template="export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'institution']" />
        <g:form action="manageMembers" controller="myInstitution" method="post" class="ui form la-clear-before">
            <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList: members,
                              tmplShowCheckbox: editable,
                              comboType: comboType,
                              tmplConfigShow: configShowTable
                      ]"/>


        <g:if test="${members && editable}">
            <input type="submit" class="ui button"
                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: [message(code:'members.confirmDelete')])}"
                   data-confirm-term-how="delete" value="${message(code: 'default.button.revoke.label')}"/>
        </g:if>
    </g:form>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br /><strong><g:message code="filter.result.empty.object" args="${[memberPlural]}"/></strong>
    </g:if>
    <g:else>
        <br /><strong><g:message code="result.empty.object" args="${[memberPlural]}"/></strong>
    </g:else>
</g:else>

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: totalMembers]"/>
    <semui:paginate action="manageMembers" controller="myInstitution" params="${params}" max="${max}" total="${membersCount}" />

    <semui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

<laser:htmlEnd />
