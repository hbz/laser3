<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:breadcrumbs>
    <g:if test="${inContextOrg}">
        <ui:crumb text="${institution.getDesignation()}" class="active"/>
    </g:if>
    <g:else>
        <g:if test="${isProviderOrAgency}">
            <g:if test="${params.my}">
                <ui:crumb message="menu.my.providers" controller="myInstitution" action="currentProviders"/>
            </g:if>
            <g:else>
                <ui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
            </g:else>
        </g:if>
        <g:else>
            <g:if test="${institutionalView}">
                <g:if test="${params.my}">
                    <ui:crumb message="menu.my.insts" controller="myInstitution" action="manageMembers" params="[comboType:RDStore.COMBO_TYPE_CONSORTIUM]"/>
                </g:if>
                <g:else>
                    <ui:crumb message="menu.public.all_insts" controller="org" action="listInstitution"/> %{-- todo!? --}%
                </g:else>
            </g:if>
            <g:elseif test="${consortialView}">
                <ui:crumb message="menu.my.consortia" controller="myInstitution" action="currentConsortia"/>
            </g:elseif>
            <g:else>
                <ui:crumb message="menu.public.all_insts" controller="org" action="listInstitution"/>
            </g:else>
        </g:else>

        <ui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:else>
</ui:breadcrumbs>