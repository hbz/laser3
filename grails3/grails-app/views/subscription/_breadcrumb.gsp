<laser:serviceInjection />

<semui:breadcrumbs>
    <%--<semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}" /> --%>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

    <g:if test="${subscription}">
        <semui:crumb class="active" id="${subscription.id}" text="${subscription.name}" />
    </g:if>
    <g:if test="${actionName == 'compare'}">
        ${message(code:'subscription.compare.label')}
    </g:if>
</semui:breadcrumbs>

<g:if test="${actionName == 'compare'}">
    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </semui:controlButtons>
</g:if>