<%@ page import="com.k_int.kbplus.Org; de.laser.Person; com.k_int.kbplus.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.helper.RDConstants " %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
    <title>${message(code: 'laser')} : <g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<g:set var="allOrgTypeIds" value="${orgInstance.getAllOrgTypeIds()}"/>
<g:set var="isProviderOrAgency"
       value="${RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds}"/>

<g:render template="breadcrumb" model="${[orgInstance: orgInstance, params: params]}"/>

<semui:controlButtons>
    <g:if test="${editable}">
        <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM' && !isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-semui="modal"
               onclick="personCreate('contactPersonForInstitution', orgInstance.id);"><g:message
                    code="person.create_new.contactPersonForInstitution.label"/></a>
        </g:if>
        <g:if test="${isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-semui="modal"
               onclick="personCreate('contactPersonForProviderAgency', orgInstance.id);"><g:message
                    code="person.create_new.contactPersonForProviderAgency.label"/></a>
        </g:if>
    </g:if>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${orgInstance.name} - ${message(code: 'menu.institutions.myAddressbook')}
</h1>

<g:render template="nav" model="${[orgInstance: orgInstance]}"/>

<semui:messages data="${flash}"/>

<semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible"/>



<g:if test="${visiblePersons}">
    <g:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            restrictToOrg : orgInstance,
            tmplConfigShow: ['lineNumber', 'organisation', 'functionPosition', 'name', 'showContacts', 'showAddresses']
    ]}"/>
</g:if>

</body>

<g:javascript>
    function personCreate(contactFor, org) {
        var url = '<g:createLink controller="ajax"
                                 action="createPerson"/>?contactFor='+contactFor+'&org='+org+'&showAddresses='+${false}+'&showContacts='+${true};
        createPersonModal(url)
    }
    function createPersonModal(url) {
        $.ajax({
            url: url,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#personModal").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#personModal');
                        r2d2.initDynamicXEditableStuff('#personModal');
                    }
                }).modal('show');
            }
        });
    }
</g:javascript>
</html>
