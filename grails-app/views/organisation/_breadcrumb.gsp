<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<ui:breadcrumbs>
    <g:if test="${!inContextOrg}">
        <g:if test="${isProviderOrAgency}">
            <g:if test="${params.my}">
                <ui:crumb message="menu.my.providers" controller="myInstitution" action="currentProviders"/>
            </g:if>
            <g:else>
                <ui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
            </g:else>
            <ui:crumb text="${orgInstance.name}" class="active"/>
        </g:if>
        <g:else>
            <g:if test="${institutionalView}">
                <ui:crumb message="menu.my.insts" controller="myInstitution" action="manageMembers" params="[comboType:RDStore.COMBO_TYPE_CONSORTIUM]"/>
                <ui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:if>
            <g:else>
                <ui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:else>
        </g:else>
    </g:if>
    <g:else>
        <ui:crumb text="${institution.name}" class="active"/>
    </g:else>
</ui:breadcrumbs>