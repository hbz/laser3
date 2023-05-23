<laser:serviceInjection />

<ui:breadcrumbs>
    <%--<ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}" /> --%>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

    <g:if test="${subscription}">
        <ui:crumb class="active" text="${subscription.name}" />
    </g:if>
    <g:if test="${actionName == 'compare'}">
        ${message(code:'subscription.compare.label')}
    </g:if>
</ui:breadcrumbs>

<g:if test="${actionName == 'compare'}">
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </ui:controlButtons>
</g:if>