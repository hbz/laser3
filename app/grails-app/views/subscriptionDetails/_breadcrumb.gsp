<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}" />
    <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

    <g:if test="${subscriptionInstance}">
        <semui:crumb class="active" id="${subscriptionInstance.id}" text="${subscriptionInstance.name}" />
    </g:if>

    <g:if test="${subscriptionInstance}">
       <g:annotatedLabel owner="${subscriptionInstance}" property="detailsPageInfo"></g:annotatedLabel>
    </g:if>
</semui:breadcrumbs>

<g:if test="${actionName == 'compare'}">
    <semui:crumb class="active" message="subscription.compare.label" />

    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </semui:controlButtons>
</g:if>