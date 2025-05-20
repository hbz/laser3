<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore;de.laser.storage.RDConstants;" %>
<%@ page import="de.laser.Org; de.laser.addressbook.Person; de.laser.addressbook.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory" %>

<laser:htmlStart message="menu.institutions.publicContacts" />

<laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

<ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
</ui:controlButtons>

<ui:messages data="${flash}"/>

<laser:render template="${customerTypeService.getNavTemplatePath()}"/>

<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="${params.tab == 'contacts' ? 'active' : ''} item" data-tab="contacts">
        ${message(code: 'org.prsLinks.label')}
    </a>
    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')}
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">

    <laser:render template="/templates/copyFilteredEmailAddresses" model="[emailAddresses: emailAddresses]"/>

    <ui:filter simple="false" extended="false">
        <g:form action="${actionName}" controller="organisation" method="get" params="${params}" class="ui small form">
            <div class="three fields">
                <div class="field">
                    <label for="prs">${message(code: 'person.filter.name')}</label>

                    <div class="ui input">
                        <input type="text" id="prs" name="prs" value="${params.prs}" placeholder="${message(code: 'person.filter.name')}"/>
                    </div>
                </div>

                <div class="field">
                    <label><g:message code="person.function.label"/></label>
                    <ui:select class="ui dropdown search"
                               name="function"
                               from="${rdvAllPersonFunctions}"
                               multiple=""
                               optionKey="id"
                               optionValue="value"
                               value="${params.function}"/>
                </div>

                <div class="field">
                    <label><g:message code="person.position.label"/></label>
                    <ui:select class="ui dropdown search"
                               name="position"
                               from="${rdvAllPersonPositions}"
                               multiple=""
                               optionKey="id"
                               optionValue="value"
                               value="${params.position}"
                               noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
            </div>

            <div class="field la-field-right-aligned">
                <label></label>
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
            </div>
        </g:form>
    </ui:filter>

    <laser:render template="/addressbook/person_table"
                  model="${[persons       : visiblePersons,
                            showContacts  : true,
                            showOptions : true,
                            tmplConfigShow: ['lineNumber', 'name', 'showContacts', 'function', 'position', 'preferredForSurvey']
                  ]}"/>

    <ui:paginate action="contacts" controller="organisation" params="${params}"
                 max="${max}"
                 total="${num_visiblePersons}"/>

</div>

%{--------------------}%

<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <laser:render template="/addressbook/address_table" model="${[
            addresses           : addresses,
            tmplShowDeleteButton: true,
            editable            : editable,
            showOptions : true,
            showPreferredForSurvey: true
    ]}"/>

</div>

%{--------------------}%

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.personCreate = function (contactFor) {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&showContacts=true';
        var func = bb8.ajax4SimpleModalFunction("#personModal", url);
        func();
    }

    JSPC.app.addressCreate = function (addressFor) {
        let url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?addressFor=' + addressFor;
        let func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }
</laser:script>
<laser:htmlEnd />
