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