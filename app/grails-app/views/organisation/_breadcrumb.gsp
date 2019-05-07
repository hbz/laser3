<%@ page import="de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<semui:breadcrumbs>
    <g:if test="${orgInstance != contextOrg}">
        <g:if test="${(RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds())}">
            <semui:crumb message="menu.public.all_provider" controller="organisation" action="listProvider"/>
            <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
        <g:elseif test="${departmentalView}">
            <semui:crumb message="menu.my.departments" controller="myInstitution" action="manageMembers" params="[comboType:'Department']"/>
            <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:elseif>
        <g:else>
            <g:if test="${accessService.checkPerm('ORG_CONSORTIUM') && institutionalView}">
                <semui:crumb message="menu.my.consortia" controller="myInstitution" action="manageMembers" params="[comboType:'Consortium']"/>
                <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
            </g:if>
            <g:else>
                <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
            </g:else>
        </g:else>
    </g:if>
    <g:else>
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:else>
</semui:breadcrumbs>