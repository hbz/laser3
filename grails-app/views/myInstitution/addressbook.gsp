<%@ page import="de.laser.PersonRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Person; de.laser.RefdataValue; de.laser.RefdataCategory" %>

<laser:htmlStart message="menu.institutions.myAddressbook" />

<ui:breadcrumbs>
    <ui:crumb message="menu.institutions.myAddressbook" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:actionsDropdown>
        <g:if test="${editable}">
            <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">

                <a href="#createPersonModal" class="item" data-ui="modal"
                   onclick="JSPC.app.personCreate('contactPersonForInstitution');"><g:message
                        code="person.create_new.contactPersonForInstitution.label"/></a>
            </g:if>

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForProviderAgency');"><g:message
                    code="person.create_new.contactPersonForProviderAgency.label"/></a>

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message
                    code="person.create_new.contactPersonForPublic.label"/></a>

        </g:if>

        <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<laser:render template="/templates/copyFilteredEmailAddresses" model="[emailAddresses: emailAddresses]"/>

<ui:h1HeaderWithIcon message="menu.institutions.myAddressbook" total="${num_visiblePersons}" floated="true" />

<ui:messages data="${flash}"/>

<ui:filter showFilterButton="true" addFilterJs="true">
    <g:form action="addressbook" controller="myInstitution" method="get" class="ui small form">
        <div class="four fields">
            <div class="field">
                <label for="org">${message(code: 'person.filter.org')}</label>

                <div class="ui input">
                    <input type="text" id="org" name="org" value="${params.org}"
                           placeholder="${message(code: 'person.filter.org')}"/>
                </div>
            </div>

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

        <div class="field">
            <label>${message(code: 'person.filter.contactArt')}</label>

            <div class="inline fields la-filter-inline">
                <div class="inline field">
                    <div class="ui checkbox">
                        <label for="showOnlyContactPersonForInstitution">${message(code: 'person.contactPersonForInstitution.label')}</label>
                        <input id="showOnlyContactPersonForInstitution" name="showOnlyContactPersonForInstitution" type="checkbox"
                               <g:if test="${params.showOnlyContactPersonForInstitution}">checked=""</g:if>
                               tabindex="0">
                    </div>
                </div>

                <div class="inline field">
                    <div class="ui checkbox">
                        <label for="showOnlyContactPersonForProviderAgency">${message(code: 'person.contactPersonForProviderAgency.label')}</label>
                        <input id="showOnlyContactPersonForProviderAgency" name="showOnlyContactPersonForProviderAgency" type="checkbox"
                               <g:if test="${params.subRunTime}">checked=""</g:if>
                               tabindex="0">
                    </div>
                </div>
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

<laser:render template="/templates/cpa/person_table" model="${[
        persons       : visiblePersons,
        showContacts  : true,
        showAddresses : true,
        tmplConfigShow: ['lineNumber', 'organisation', 'function', 'position', 'name', 'showContacts', 'showAddresses']
]}"/>

<ui:paginate action="addressbook" controller="myInstitution" params="${params}"
                max="${max}"
                total="${num_visiblePersons}"/>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.personCreate = function (contactFor) {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor='+contactFor+'&showAddresses=true&showContacts=true';
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
