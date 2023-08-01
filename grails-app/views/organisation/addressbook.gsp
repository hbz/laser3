<%@ page import="de.laser.Org; de.laser.Person; de.laser.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants " %>

<laser:htmlStart message="menu.institutions.myAddressbook" />

<g:set var="allOrgTypeIds" value="${orgInstance.getAllOrgTypeIds()}"/>
<g:set var="isProviderOrAgency"
       value="${RDStore.OT_PROVIDER.id in allOrgTypeIds || RDStore.OT_AGENCY.id in allOrgTypeIds}"/>

<laser:render template="breadcrumb" model="${[orgInstance: orgInstance, params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions" />
</ui:controlButtons>

<laser:render template="/templates/copyFilteredEmailAddresses"
          model="[emailAddresses: emailAddresses]"/>

<ui:h1HeaderWithIcon text="${orgInstance.name} - ${message(code: 'menu.institutions.myAddressbook')}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav" model="${[orgInstance: orgInstance]}"/>

<ui:messages data="${flash}"/>

<ui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible"/>

<ui:filter>
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
               class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" class="ui primary button" value="${message(code: 'default.button.filter.label')}">
        </div>
    </g:form>
</ui:filter>

<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="${params.tab == 'contacts' ? 'active' : ''} item" data-tab="contacts">
        ${message(code: 'org.prsLinks.label')}
    </a>

    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')}
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">
    <laser:render template="/templates/cpa/person_table" model="${[
            persons       : visiblePersons,
            restrictToOrg : orgInstance,
            showContacts: true,
            showAddresses: true,
            tmplConfigShow: ['lineNumber', 'name', 'function', 'position',  'showContacts', 'showAddresses']
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
            controller          : 'org',
            action              : 'show',
            editable            : editable
    ]}"/>

    <ui:paginate action="addressbook" controller="myInstitution" params="${params+[tab: 'addresses']}"
                 max="${max}" offset="${addressOffset}"
                 total="${num_visibleAddresses}"/>
</div>

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
    JSPC.app.addressCreate = function (addressFor, orgId) {
        let url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?addressFor=' + addressFor+'&orgId='+orgId;
        let func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }
</laser:script>
<laser:htmlEnd />
