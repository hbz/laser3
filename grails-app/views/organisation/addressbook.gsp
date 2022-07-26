<%@ page import="de.laser.Org; de.laser.Person; de.laser.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants " %>

<g:set var="entityName" value="${message(code: 'org.label')}"/>
<laser:htmlStart text="${message(code:"default.show.label", args:[entityName])}" />

<g:set var="allOrgTypeIds" value="${orgInstance.getAllOrgTypeIds()}"/>
<g:set var="isProviderOrAgency"
       value="${RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds}"/>

<laser:render template="breadcrumb" model="${[orgInstance: orgInstance, params: params]}"/>

<ui:controlButtons>
    <ui:actionsDropdown>
    <g:if test="${editable}">
        <g:if test="${(institution.getCustomerType() == 'ORG_CONSORTIUM') && !isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForInstitution', ${orgInstance.id});"><g:message
                    code="person.create_new.contactPersonForInstitution.label"/></a>
        </g:if>
        <g:if test="${isProviderOrAgency}">
            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForProviderAgency', ${orgInstance.id});"><g:message
                    code="person.create_new.contactPersonForProviderAgency.label"/></a>
        </g:if>
    </g:if>

        <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<laser:render template="/templates/copyFilteredEmailAddresses"
          model="[emailAddresses: emailAddresses]"/>

<ui:h1HeaderWithIcon text="${orgInstance.name} - ${message(code: 'menu.institutions.myAddressbook')}" />

<laser:render template="nav" model="${[orgInstance: orgInstance]}"/>

<ui:messages data="${flash}"/>

<ui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible"/>

<ui:filter showFilterButton="true" addFilterJs="true">
    <g:form action="addressbook" controller="organisation" method="get" params="[id: orgInstance.id]" class="ui small form">
        <div class="three fields">
            <div class="field">
                <label for="prs">${message(code: 'person.filter.name')}</label>

                <div class="ui input">
                    <input type="text" id="prs" name="prs" value="${params.prs}"
                           placeholder="${message(code: 'person.filter.name')}"/>
                </div>
            </div>

            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
        </div>

        <div class="two fields">
            <div class="field">
                <label><g:message code="person.function.label"/></label>
                <ui:select class="ui dropdown search"
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
                <ui:select class="ui dropdown search"
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
</ui:filter>

<g:if test="${visiblePersons}">
    <laser:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            restrictToOrg : orgInstance,
            showContacts: true,
            showAddresses: true,
            tmplConfigShow: ['lineNumber', 'name', 'function', 'position',  'showContacts', 'showAddresses']
    ]}"/>

    <ui:paginate action="addressbook" controller="myInstitution" params="${params}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</g:if>

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
                        r2d2.initDynamicUiStuff('#personModal');
                        r2d2.initDynamicXEditableStuff('#personModal');
                    }
                }).modal('show');
            }
        });
    }
</laser:script>
<laser:htmlEnd />
