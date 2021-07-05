<%@ page import="de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<semui:breadcrumbs>
    <g:if test="${!inContextOrg}">
        <g:if test="${isProviderOrAgency}">
            <g:if test="${params.my}">
                <semui:crumb message="menu.my.providers" controller="myInstitution" action="currentProviders"/>
            </g:if>
            <g:else>
                <semui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
            </g:else>
            <semui:crumb text="${orgInstance.name}" class="active"/>
        </g:if>
        <g:else>
            <g:if test="${institutionalView}">
                <semui:crumb message="menu.my.insts" controller="myInstitution" action="manageMembers" params="[comboType:RDStore.COMBO_TYPE_CONSORTIUM]"/>
                <semui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:if>
            <g:else>
                <semui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:else>
        </g:else>
    </g:if>
    <g:else>
        <semui:crumb text="${institution.name}" class="active"/>
    </g:else>
</semui:breadcrumbs>