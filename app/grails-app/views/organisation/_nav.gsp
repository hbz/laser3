<%@page import="de.laser.helper.RDStore; com.k_int.kbplus.Combo" %>

<laser:serviceInjection/>

<g:set var="departmentalView" value="${orgInstance.isDepartment()}" />
<g:set var="checkForeignOrgComboPermAffiliationX" value="${accessService.checkForeignOrgComboPermAffiliationX([
        org: orgInstance,
        affiliation: "INST_USER",
        comboPerm: "ORG_CONSORTIUM",
        comboAffiliation: "INST_USER",
        specRoles: "ROLE_ORG_EDITOR,ROLE_ADMIN"])}" />

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisation" action="show" params="${[id: orgInstance.id]}" message="org.nav.details"/>

    <g:if test="${(orgInstance.sector != RDStore.O_SECTOR_PUBLISHER) && (!departmentalView)}">
        <g:if test="${inContextOrg}">
            <semui:securedSubNavItem controller="organisation" action="ids" params="${[id: orgInstance.id]}"
                                     message="org.nav.ids" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:elseif test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                comboPerm: "ORG_INST_COLLECTIVE, ORG_CONSORTIUM",
                comboAffiliation: "INST_USER",
                specRoles: "ROLE_ORG_EDITOR, ROLE_ADMIN"
        ])}">
            <semui:subNavItem controller="organisation" action="ids" params="${[id: orgInstance.id]}" message="org.nav.ids"/>
        </g:elseif>
        <g:else>
            <semui:subNavItem message="org.nav.ids" disabled="disabled" />
        </g:else>
    </g:if>
    <g:if test="${inContextOrg}">
        <semui:subNavItem controller="myInstitution" action="myPublicContacts" message="menu.institutions.publicContacts" />
    </g:if>

    <g:if test="${orgInstance.sector != RDStore.O_SECTOR_PUBLISHER && !departmentalView}">

        <g:if test="${checkForeignOrgComboPermAffiliationX}">
                <semui:subNavItem controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}"
                          message="menu.institutions.readerNumbers"/>
        </g:if>
        <g:else>
            <semui:subNavItem message="menu.institutions.readerNumbers" disabled="disabled" />
        </g:else>
    </g:if>

    <g:if test="${orgInstance.sector != RDStore.O_SECTOR_PUBLISHER && !departmentalView}">

        <g:if test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_USER",
                specRoles: "ROLE_ADMIN"])}">

            <semui:subNavItem controller="organisation" action="accessPoints" params="${[id:orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <semui:subNavItem message="org.nav.accessPoints" disabled="disabled" />
        </g:else>
    </g:if>

    <semui:securedSubNavItem controller="organisation" action="documents" params="${[id: orgInstance.id]}"
                             affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                             message="menu.my.documents" />

    <g:if test="${!inContextOrg}">
        <semui:securedSubNavItem controller="organisation" action="addressbook" params="${[id: orgInstance.id]}"
                                 affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                                 message="menu.institutions.myAddressbook"/>
    </g:if>

    <g:if test="${orgInstance.sector != RDStore.O_SECTOR_PUBLISHER}">
        <g:if test="${inContextOrg}">
            <semui:securedSubNavItem controller="myInstitution" action="userList"
                                     message="org.nav.users" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:elseif test="${ accessService.checkForeignOrgComboPermAffiliation([
                org: orgInstance,
                comboPerm: "ORG_INST_COLLECTIVE, ORG_CONSORTIUM",
                comboAffiliation: "INST_ADM"
        ]) && orgInstance.getAllValidInstAdmins().isEmpty() }">
            <semui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
        </g:elseif>
        <g:else>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                <semui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
            </sec:ifAnyGranted>
        </g:else>

        <g:if test="${!departmentalView}">
            <g:if test="${inContextOrg}">
                <semui:securedSubNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}"
                                         message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
            </g:if>
            <g:elseif test="${accessService.checkForeignOrgComboPermAffiliationX([
                    org: orgInstance,
                    comboPerm: "ORG_INST_COLLECTIVE, ORG_CONSORTIUM",
                    comboAffiliation: "INST_ADM",
                    specRoles: "ROLE_ORG_EDITOR, ROLE_ADMIN"
            ])}">
                <semui:subNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}" message="org.nav.options"/>
            </g:elseif>
            <g:else>
                <semui:subNavItem disabled="disabled" message="org.nav.options"/>
            </g:else>
        </g:if>
    </g:if>
</semui:subNav>