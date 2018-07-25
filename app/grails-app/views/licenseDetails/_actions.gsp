<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${license.getLicensor()?.id == contextService.getOrg()?.id && ! license.isTemplate()}">
    <g:if test="${!( license.instanceOf && ! license.hasTemplate())}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="licenseDetails" action="addMembers" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
        </semui:actionsDropdown>
    </g:if>
</g:if>
