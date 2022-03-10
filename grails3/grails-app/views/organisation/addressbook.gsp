<%@ page import="de.laser.Org; de.laser.Person; de.laser.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.helper.RDConstants " %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
    <title>${message(code: 'laser')} : <g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<g:set var="allOrgTypeIds" value="${orgInstance.getAllOrgTypeIds()}"/>
<g:set var="isProviderOrAgency"
       value="${RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds}"/>

<g:render template="breadcrumb" model="${[orgInstance: orgInstance, params: params]}"/>

<semui:controlButtons>
    <semui:actionsDropdown>
    <g:if test="${editable}">
        <g:if test="${(institution.getCustomerType() == 'ORG_CONSORTIUM') && !isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-semui="modal"
               onclick="JSPC.app.personCreate('contactPersonForInstitution', ${orgInstance.id});"><g:message
                    code="person.create_new.contactPersonForInstitution.label"/></a>
        </g:if>
        <g:if test="${isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-semui="modal"
               onclick="JSPC.app.personCreate('contactPersonForProviderAgency', ${orgInstance.id});"><g:message
                    code="person.create_new.contactPersonForProviderAgency.label"/></a>
        </g:if>
    </g:if>

        <semui:actionsDropdownItem notActive="true" data-semui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </semui:actionsDropdown>
</semui:controlButtons>

<g:render template="/templates/copyFilteredEmailAddresses"
          model="[emailAddresses: emailAddresses]"/>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${orgInstance.name} - ${message(code: 'menu.institutions.myAddressbook')}
</h1>

<g:render template="nav" model="${[orgInstance: orgInstance]}"/>

<semui:messages data="${flash}"/>

<semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible"/>

<g:render template="/templates/filter/javascript"/>
<semui:filter showFilterButton="true">
    <g:form action="addressbook" controller="organisation" method="get" params="[id: orgInstance.id]" class="ui small form">
        <div class="three fields">
            <div class="field">
                <label for="prs">${message(code: 'person.filter.name')}</label>

                <div class="ui input">
                    <input type="text" id="prs" name="prs" value="${params.prs}"
                           placeholder="${message(code: 'person.filter.name')}"/>
                </div>
            </div>

            <g:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
        </div>

        <div class="two fields">
            <div class="field">
                <label><g:message code="person.function.label"/></label>
                <laser:select class="ui dropdown search"
                              name="function"
                              from="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}"
                              multiple=""
                              optionKey="id"
                              optionValue="value"
                              value="${params.function}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label><g:message code="person.position.label"/></label>
                <laser:select class="ui dropdown search"
                              name="position"
                              from="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}"
                              multiple=""
                              optionKey="id"
                              optionValue="value"
                              value="${params.position}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="field la-field-right-aligned">
            <label></label>
            <a href="${request.forwardURI}"
               class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" class="ui secondary button" value="${message(code: 'default.button.filter.label')}">
        </div>
    </g:form>
</semui:filter>

<g:if test="${visiblePersons}">
    <g:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            restrictToOrg : orgInstance,
            showContacts: true,
            showAddresses: true,
            tmplConfigShow: ['lineNumber', 'name', 'function', 'position',  'showContacts', 'showAddresses']
    ]}"/>

    <semui:paginate action="addressbook" controller="myInstitution" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</g:if>

</body>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.personCreate = function (contactFor, org) {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor='+contactFor+'&org='+org+'&showAddresses=true&showContacts=true';
        JSPC.app.createPersonModal(url)
    }
    JSPC.app.createPersonModal = function (url) {
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
</laser:script>
</html>
