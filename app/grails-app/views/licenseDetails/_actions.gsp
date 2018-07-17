<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${license?.isTemplate() || license?.getLicensor()?.id == contextService.getOrg()?.id}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="licenseDetails" action="addMembers" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
    </semui:actionsDropdown>
</g:if>
