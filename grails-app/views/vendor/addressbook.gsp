<%@ page import="de.laser.helper.Params; de.laser.Org; de.laser.Person; de.laser.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants " %>

<laser:htmlStart message="menu.institutions.myAddressbook" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[vendor: vendor]}"/>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
</ui:controlButtons>

<laser:render template="/templates/copyFilteredEmailAddresses" model="[emailAddresses: emailAddresses]"/>

<ui:h1HeaderWithIcon text="${vendor.name} - ${message(code: 'menu.institutions.myAddressbook')}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[vendor: vendor]}"/>

<ui:messages data="${flash}"/>

<ui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible"/>

<ui:filter>
    <g:form action="addressbook" controller="vendor" method="get" params="[id: vendor.id]" class="ui small form">
        <div class="three fields">
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
        ${message(code: 'org.prsLinks.label')} <span class="ui circular label">${num_visiblePersons}</span>
    </a>

    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')}  <span class="ui circular label">${num_visibleAddresses}</span>
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">
    <laser:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            restrictToVendor : vendor,
            showContacts: true,
            showAddresses: true,
            showOptions : true,
            tmplConfigShow: ['lineNumber', 'name', 'function', 'position',  'showContacts', 'showAddresses']
    ]}"/>

    <ui:paginate action="addressbook" controller="vendor" params="${params+[tab: 'contacts']}"
                    max="${max}" offset="${personOffset}"
                    total="${num_visiblePersons}"/>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <laser:render template="/templates/cpa/address_table" model="${[
            addresses           : addresses,
            offset              : addressOffset,
            tmplShowDeleteButton: true,
            editable            : editable,
            showOptions : true
    ]}"/>

    <ui:paginate action="addressbook" controller="vendor" params="${params+[tab: 'addresses']}"
                 max="${max}" offset="${addressOffset}"
                 total="${num_visibleAddresses}"/>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.personCreate = function (contactFor, provider) {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor='+contactFor+'&vendor='+vendor+'&showAddresses=true&showContacts=true';
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
    JSPC.app.addressCreate = function (addressFor, providerId) {
        let url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?addressFor=' + addressFor+'&vendorId='+vendorId;
        let func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }
</laser:script>
<laser:htmlEnd />
