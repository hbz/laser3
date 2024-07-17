<%@ page import="de.laser.ui.Button; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.helper.Params; de.laser.PersonRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Person; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.utils.DateUtils" %>

<laser:serviceInjection/>

<laser:htmlStart message="menu.institutions.addressbook" />

<ui:breadcrumbs>
    <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.addressbook" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.ADDRESSBOOK]"/>
        </ui:exportDropdownItem>
        <%--
        <g:if test="${filterSet == true}">
            <ui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal" params="${params+[exportXLS: true]}" action="addressbook"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}" data-confirm-term-how="ok">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item js-open-confirm-modal" params="${params+[format: 'csv']}" action="addressbook"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}" data-confirm-term-how="ok">
                    <g:message code="default.button.exports.csv"/>
                </g:link>
            </ui:exportDropdownItem>
        </g:if>
        <g:else>
            <ui:exportDropdownItem>
                <g:link class="item" params="${params+[exportXLS: true]}" action="addressbook"><g:message code="default.button.exports.xls"/></g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" params="${params+[format: 'csv']}" action="addressbook"><g:message code="default.button.exports.csv"/></g:link>
            </ui:exportDropdownItem>
        </g:else>
        --%>
    </ui:exportDropdown>
    <ui:actionsDropdown>
        <g:if test="${editable}">

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForProvider');"><i class="${Icon.UI.ACP_PRIVATE}"></i><g:message
                    code="person.create_new.contactPersonForProvider.label"/></a>
            <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForProvider');"><i class="map marked alternate icon"></i><g:message code="address.add.addressForProvider.label"/></a>

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForVendor');"><i class="${Icon.UI.ACP_PRIVATE}"></i><g:message
                    code="person.create_new.contactPersonForVendor.label"/></a>

            <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForVendor');"><i class="map marked alternate icon"></i><g:message code="address.add.addressForVendor.label"/></a>
            <g:if test="${institution.isCustomerType_Consortium()}">
                <a href="#createPersonModal" class="item" data-ui="modal"
                   onclick="JSPC.app.personCreate('contactPersonForInstitution');"><i class="${Icon.UI.ACP_PRIVATE}"></i><g:message
                        code="person.create_new.contactPersonForInstitution.label"/></a>

                <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForInstitution');"><i class="map marked alternate icon"></i><g:message code="address.add.addressForInstitution.label"/></a>
            </g:if>

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForPublic');"><i class="${Icon.UI.ACP_PRIVATE}"></i><g:message
                    code="person.create_new.contactPersonForPublic.label"/></a>

            <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForPublic');"><i class="map marked alternate icon"></i><g:message code="address.add.addressForPublic.label"/></a>

            <div class="divider"></div>
        </g:if>

        <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<laser:render template="/templates/copyFilteredEmailAddresses" model="[emailAddresses: emailAddresses]"/>

<ui:h1HeaderWithIcon message="menu.institutions.addressbook" type="addressbook" total="${num_visiblePersons}" floated="true" />

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="addressbook" controller="myInstitution" method="get" class="ui small form">
        <div class="four fields">
            <div class="field">
                <label for="org">${message(code: 'person.filter.org')}</label>

                <div class="ui input">
                    <input type="text" id="org" name="org" value="${params.org}" placeholder="${message(code: 'person.filter.org')}"/>
                </div>
            </div>

            <div class="field">
                <label for="prs">${message(code: 'person.filter.name')}</label>

                <div class="ui input">
                    <input type="text" id="prs" name="prs" value="${params.prs}" placeholder="${message(code: 'person.filter.name')}"/>
                </div>
            </div>
            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="function"><g:message code="person.function.label"/></label>
                <select id="function" name="function" multiple="" class="ui dropdown search">
                    <option value=""><g:message code="default.select.choose.label"/></option>
                    <g:each in="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}" var="rdv">
                        <option <%=Params.getLongList(params, 'function').contains(rdv.id) ? 'selected="selected"' : ''%> value="${rdv.id}">${rdv.getI10n('value')}</option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="position"><g:message code="person.position.label"/></label>
                <select id="position" name="position" multiple="" class="ui dropdown search">
                    <option value=""><g:message code="default.select.choose.label"/></option>
                    <g:each in="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}" var="rdv">
                        <option <%=Params.getLongList(params, 'position').contains(rdv.id) ? 'selected="selected"' : ''%> value="${rdv.id}">${rdv.getI10n('value')}</option>
                    </g:each>
                </select>
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
                        <label for="showOnlyContactPersonForProvider">${message(code: 'person.contactPersonForProvider.label')}</label>
                        <input id="showOnlyContactPersonForProvider" name="showOnlyContactPersonForProvider" type="checkbox"
                               <g:if test="${params.showOnlyContactPersonForProvider}">checked=""</g:if>
                               tabindex="0">
                    </div>
                </div>

                <div class="inline field">
                    <div class="ui checkbox">
                        <label for="showOnlyContactPersonForVendor">${message(code: 'person.contactPersonForVendor.label')}</label>
                        <input id="showOnlyContactPersonForVendor" name="showOnlyContactPersonForVendor" type="checkbox"
                               <g:if test="${params.showOnlyContactPersonForVendor}">checked=""</g:if>
                               tabindex="0">
                    </div>
                </div>
            </div>
        </div>


        <div class="field la-field-right-aligned">
            <label></label>
            <a href="${request.forwardURI}"
               class="${Button.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" class="${Button.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
        </div>
    </g:form>
</ui:filter>

<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="${params.tab == 'contacts' ? 'active' : ''} item" data-tab="contacts">
        ${message(code: 'org.prsLinks.label')} <span class="ui circular label">${num_visiblePersons}</span>
    </a>

    <%--<a class="${params.tab == 'personAddresses' ? 'active' : ''} item" data-tab="personAddresses">
        ${message(code: 'org.prsLinks.adresses.label')}
    </a>--%>

    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')} <span class="ui circular label">${num_visibleAddresses}</span>
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">

    <laser:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            offset        : personOffset,
            showContacts  : true,
            showAddresses : true,
            showOptions : true,
            tmplConfigShow: ['lineNumber', 'organisation', 'function', 'position', 'name', 'showContacts']
    ]}"/>

    <ui:paginate action="addressbook" controller="myInstitution" params="${params+[tab: 'contacts']}"
                 max="${max}" offset="${personOffset}"
                 total="${num_visiblePersons}"/>

</div>

<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <laser:render template="/templates/cpa/address_table" model="${[
            addresses           : addresses,
            offset              : addressOffset,
            tmplShowDeleteButton: true,
            tmplShowOrgName     : true,
            editable            : editable,
            showOptions : true
    ]}"/>

    <ui:paginate action="addressbook" controller="myInstitution" params="${params+[tab: 'addresses']}"
                 max="${max}" offset="${addressOffset}"
                 total="${num_visibleAddresses}"/>
</div>

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
    JSPC.app.addressCreate = function (addressFor) {
        let url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?addressFor=' + addressFor;
        let func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }

</laser:script>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
