<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.PersonRole;" %>
<laser:serviceInjection/>

<g:set var="showOnlyPublic" value="${true}"/>

<g:if test="${!isProviderOrAgency}">
    <h4 style="font-size: 1rem">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</h4>
    <g:each in="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)}"
            var="prs">
        <g:render template="/templates/cpa/person_full_details" model="${[
                person                 : prs,
                personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, prs),
                personContext          : orgInstance,
                tmplShowDeleteButton   : true,
                tmplShowFunctions      : false,
                tmplShowPositions      : false,
                tmplShowResponsiblities: true,
                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                controller             : 'organisation',
                action                 : 'show',
                id                     : orgInstance.id,
                editable               : false
        ]}"/>
    </g:each>
</g:if>
<g:if test="${!isProviderOrAgency}">
    <h4 style="font-size: 1rem">${RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS.getI10n('value')}</h4>
    <g:each in="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS)}"
            var="prs">
        <g:render template="/templates/cpa/person_full_details" model="${[
                person                 : prs,
                personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS, prs),
                personContext          : orgInstance,
                tmplShowDeleteButton   : true,
                tmplShowFunctions      : false,
                tmplShowPositions      : false,
                tmplShowResponsiblities: true,
                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                controller             : 'organisation',
                action                 : 'show',
                id                     : orgInstance.id,
                editable               : false
        ]}"/>
    </g:each>
</g:if>

<h4 style="font-size: 1rem">${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}</h4>
<g:each in="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_TECHNICAL_SUPPORT)}"
        var="prs">
    <g:render template="/templates/cpa/person_full_details" model="${[
            person                 : prs,
            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, prs),
            personContext          : orgInstance,
            tmplShowDeleteButton   : true,
            tmplShowFunctions      : false,
            tmplShowPositions      : false,
            tmplShowResponsiblities: true,
            tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
            controller             : 'organisation',
            action                 : 'show',
            id                     : orgInstance.id,
            editable               : false
    ]}"/>
</g:each>

<g:if test="${!isProviderOrAgency}">
    <h4 style="font-size: 1rem">${RDStore.PRS_FUNC_RESPONSIBLE_ADMIN.getI10n('value')}</h4>
    <g:each in="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN)}"
            var="prs">
        <g:render template="/templates/cpa/person_full_details" model="${[
                person                 : prs,
                personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, prs),
                personContext          : orgInstance,
                tmplShowDeleteButton   : true,
                tmplShowFunctions      : false,
                tmplShowPositions      : false,
                tmplShowResponsiblities: true,
                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                controller             : 'organisation',
                action                 : 'show',
                id                     : orgInstance.id,
                editable               : false
        ]}"/>
    </g:each>
</g:if>