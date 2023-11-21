<%@ page import="de.laser.PersonRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Person; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.utils.DateUtils" %>

<laser:serviceInjection/>

<laser:htmlStart message="menu.institutions.addressbook" />

<ui:breadcrumbs>
    <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.addressbook" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
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
               onclick="JSPC.app.personCreate('contactPersonForProviderAgency');"><g:message
                    code="person.create_new.contactPersonForProviderAgency.label"/></a>

            <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForProviderAgency');"><g:message code="address.add.addressForProviderAgency.label"/></a>
            <g:if test="${institution.isCustomerType_Consortium()}">
                <a href="#createPersonModal" class="item" data-ui="modal"
                   onclick="JSPC.app.personCreate('contactPersonForInstitution');"><g:message
                        code="person.create_new.contactPersonForInstitution.label"/></a>

                <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForInstitution');"><g:message code="address.add.addressForInstitution.label"/></a>
            </g:if>

            <a href="#createPersonModal" class="item" data-ui="modal"
               onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message
                    code="person.create_new.contactPersonForPublic.label"/></a>

            <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForPublic');"><g:message code="address.add.addressForPublic.label"/></a>

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
                               <g:if test="${params.showOnlyContactPersonForProviderAgency}">checked=""</g:if>
                               tabindex="0">
                    </div>
                </div>
            </div>
        </div>


        <div class="field la-field-right-aligned">
            <label></label>
            <a href="${request.forwardURI}"
               class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" class="ui primary button" value="${message(code: 'default.button.filter.label')}">
        </div>
    </g:form>
</ui:filter>

<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="${params.tab == 'contacts' ? 'active' : ''} item" data-tab="contacts">
        ${message(code: 'org.prsLinks.label')}
    </a>

    <%--<a class="${params.tab == 'personAddresses' ? 'active' : ''} item" data-tab="personAddresses">
        ${message(code: 'org.prsLinks.adresses.label')}
    </a>--%>

    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')}
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">

    <laser:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            offset        : personOffset,
            showContacts  : true,
            showAddresses : true,
            tmplConfigShow: ['lineNumber', 'organisation', 'function', 'position', 'name', 'showContacts', 'showAddresses']
    ]}"/>

    <ui:paginate action="addressbook" controller="myInstitution" params="${params+[tab: 'contacts']}"
                 max="${max}" offset="${personOffset}"
                 total="${num_visiblePersons}"/>

</div>

<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <laser:render template="/templates/cpa/address_table" model="${[
            hideAddressType     : true,
            addresses           : addresses,
            offset              : addressOffset,
            tmplShowDeleteButton: true,
            tmplShowOrgName     : true,
            controller          : 'org',
            action              : 'show',
            editable            : editable
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

    $('#individuallyExportModal form').submit(function () {
        $("#tab").val($('div.tab.active').attr('data-tab'));
    });
</laser:script>

<!-- _individuallyExportModal.gsp -->
<%
    Map<String, Object> fields = exportClickMeService.getExportAddressFieldsForUI()
    Map<String, Object> formFields = fields.exportFields as Map, filterFields = fields.filterFields as Map
    Map<String, Object> urlParams = params.clone()
    urlParams.remove('tab')
%>

<ui:modal modalSize="large" id="individuallyExportModal" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="addressbook" controller="myInstitution" params="${urlParams}">
        <g:hiddenField name="tab" value="${params.tab}"/>
        <laser:render template="/templates/export/individuallyExportForm" model="${[currentTabNotice: true, modalID: 'individuallyExportModal', formFields: formFields, filterFields: filterFields, exportFileName: escapeService.escapeString("${message(code: 'menu.institutions.myAddressbook')}_${DateUtils.getSDF_yyyyMMdd().format(new Date())}"), orgSwitch: true]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

<laser:htmlEnd />
