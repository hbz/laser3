<%@page import="de.laser.helper.RDStore" %>

<laser:serviceInjection/>

<g:set var="departmentalView" value="${orgInstance.getallOrgTypeIds().contains(RDStore.OT_DEPARTMENT.id)}" />

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisation" action="show" params="${[id: orgInstance.id]}" message="org.nav.details"/>
    <g:if test="${inContextOrg}">
        <semui:subNavItem controller="myInstitution" action="myPublicContacts" message="menu.institutions.publicContacts" />
    </g:if>

    <g:if test="${orgInstance.sector?.id != RDStore.O_SECTOR_PUBLISHER && !departmentalView}">

        <g:if test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ORG_EDITOR,ROLE_ADMIN"])}">

                <semui:subNavItem controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}"
                          message="menu.institutions.readerNumbers"/>
        </g:if>
        <g:else>
            <semui:subNavItem controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}"
                              message="menu.institutions.readerNumbers" disabled="disabled" />
        </g:else>
    </g:if>

    <g:if test="${orgInstance.sector != RDStore.O_SECTOR_PUBLISHER && !departmentalView}">
        <semui:securedSubNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}"
                                     message="org.nav.users" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        <%-- TODO: check ctx != foreign org --%>
        <semui:securedSubNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}"
                                 orgPerm="ORG_BASIC_MEMBER,ORG_CONSORTIUM" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR"
                                 affiliation="INST_ADM" affiliationOrg="${orgInstance}"
                                 message="org.nav.options" />
    </g:if>

    <semui:securedSubNavItem controller="organisation" action="documents" params="${[id: orgInstance.id]}"
                             affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                             message="menu.my.documents" />

    <g:if test="${!inContextOrg}">
        <semui:securedSubNavItem controller="organisation" action="addressbook" params="${[id: orgInstance.id]}"
                                 affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                                 message="menu.institutions.myAddressbook"/>
    </g:if>

    <g:if test="${orgInstance.sector != RDStore.O_SECTOR_PUBLISHER && !departmentalView}">

        <g:if test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ADMIN"])}">

            <semui:subNavItem controller="organisation" action="accessPoints" params="${[id:orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <semui:subNavItem controller="organisation" action="accessPoints" params="${[id:orgInstance.id]}"
                              message="org.nav.accessPoints" disabled="disabled" />
        </g:else>

    </g:if>
</semui:subNav>