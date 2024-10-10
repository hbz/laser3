<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:breadcrumbs>
    <g:if test="${inContextOrg}">
        <ui:crumb text="${institution.getDesignation()}" class="active"/>
    </g:if>
    <g:else>
            <g:if test="${consortialView}">
                <ui:crumb message="menu.my.consortia" controller="myInstitution" action="currentConsortia"/>
            </g:if>
            <g:elseif test="${institutionalView}">
                <ui:crumb message="menu.my.insts" controller="myInstitution" action="manageMembers" params="[comboType:RDStore.COMBO_TYPE_CONSORTIUM]"/>
            </g:elseif>
            <g:else>
                <g:if test="${orgInstance.isCustomerType_Consortium()}">
                    <ui:crumb message="menu.public.all_cons" controller="org" action="listConsortia"/>
                </g:if>
                <g:else>
                    <ui:crumb message="menu.public.all_insts" controller="org" action="listInstitution"/>
                </g:else>
            </g:else>

        <ui:crumb text="${orgInstance.getDesignation()}" class="active"/>
    </g:else>
</ui:breadcrumbs>