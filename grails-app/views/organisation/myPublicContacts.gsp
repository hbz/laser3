<%@ page import="de.laser.helper.RDStore;de.laser.helper.RDConstants;" %>
<%@ page import="de.laser.Org; de.laser.Person; de.laser.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<!doctype html>

<laser:serviceInjection/>

<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'menu.institutions.publicContacts')}</title>
</head>

<body>

<semui:breadcrumbs>
    <g:if test="${inContextOrg}">
        <semui:crumb text="${orgInstance.getDesignation()}" controller="organisation" show="show"
                     params="[id: orgInstance.id]"/>
    </g:if>
    <semui:crumb message="menu.institutions.publicContacts" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

<semui:controlButtons>
    <semui:actionsDropdown>
        <g:if test="${editable}">
            <a href="#createPersonModal" class="item" data-semui="modal"
               onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message
                    code="person.create_new.contactPerson.label"/></a>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled tooltip="${message(code: 'default.notAutorized.message')}"
                                               message="person.create_new.contactPerson.label"/>
        </g:else>
        <g:if test="${editable}">
            <a href="#addressFormModal" class="item" data-semui="modal"
               onclick="JSPC.app.addresscreate_org('${orgInstance.id}');"><g:message code="address.add.label"/></a>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled tooltip="${message(code: 'default.notAutorized.message')}"
                                               message="address.add.label"/>
        </g:else>
        <semui:actionsDropdownItem notActive="true" data-semui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </semui:actionsDropdown>
</semui:controlButtons>

<semui:messages data="${flash}"/>

<g:render template="/organisation/nav"/>

<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="${params.tab == 'contacts' ? 'active' : ''} item" data-tab="contacts">
        ${message(code: 'org.prsLinks.label')}
    </a>

    <a class="${params.tab == 'personAddresses' ? 'active' : ''} item" data-tab="personAddresses">
        ${message(code: 'org.prsLinks.adresses.label')}
    </a>

    <a class="${params.tab == 'addresses' ? 'active' : ''} item" data-tab="addresses">
        ${message(code: 'org.addresses.label')}
    </a>
</div>

<div class="ui bottom attached tab segment ${params.tab == 'contacts' ? 'active' : ''}" data-tab="contacts">

    <g:render template="/templates/copyFilteredEmailAddresses" model="[emailAddresses: emailAddresses]"/>
    <br/>


    <semui:filter>
        <g:form action="${actionName}" controller="organisation" method="get" params="${params}" class="ui small form">
            <div class="three fields">
                <div class="field">
                    <label for="prs">${message(code: 'person.filter.name')}</label>

                    <div class="ui input">
                        <input type="text" id="prs" name="prs" value="${params.prs}"
                               placeholder="${message(code: 'person.filter.name')}"/>
                    </div>
                </div>

                <div class="field">
                    <label><g:message code="person.function.label"/></label>
                    <laser:select class="ui dropdown search"
                                  name="function"
                                  from="${rdvAllPersonFunctions}"
                                  multiple=""
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.function}"/>
                </div>

                <div class="field">
                    <label><g:message code="person.position.label"/></label>
                    <laser:select class="ui dropdown search"
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
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>
        </g:form>
    </semui:filter>

    <g:render template="/templates/cpa/person_table"
              model="${[persons       : visiblePersons,
                        showContacts  : true,
                        tmplConfigShow: ['lineNumber', 'name', 'showContacts', 'function', 'position']
              ]}"/>

    <semui:paginate action="myPublicContacts" controller="organisation" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</div>

%{--------------------}%
<div class="ui bottom attached tab segment ${params.tab == 'personAddresses' ? 'active' : ''}"
     data-tab="personAddresses">

    <semui:filter>
        <g:form action="${actionName}" controller="organisation" method="get" params="${params}" class="ui small form">
            <div class="three fields">
                <div class="field">
                    <label for="prs">${message(code: 'person.filter.name')}</label>

                    <div class="ui input">
                        <input type="text" name="prs" value="${params.prs}"
                               placeholder="${message(code: 'person.filter.name')}"/>
                    </div>
                </div>

                <div class="field">
                    <label><g:message code="person.function.label"/></label>
                    <laser:select class="ui dropdown search"
                                  name="function"
                                  from="${rdvAllPersonFunctions}"
                                  multiple=""
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.function}"/>
                </div>

                <div class="field">
                    <label><g:message code="person.position.label"/></label>
                    <laser:select class="ui dropdown search"
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
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>
        </g:form>
    </semui:filter>

    <g:render template="/templates/cpa/person_table"
              model="${[persons       : visiblePersons,
                        showAddresses : true,
                        showContacts  : true,
                        tmplConfigShow: ['lineNumber', 'name', 'showAddresses', 'function', 'position']
              ]}"/>

    <semui:paginate action="myPublicContacts" controller="organisation" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</div>

%{--------------------}%


%{--------------------}%
<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <br/>


    <g:render template="/templates/cpa/address_table" model="${[
            hideAddressType     : true,
            addresses           : addresses,
            tmplShowDeleteButton: true,
            controller          : 'org',
            action              : 'show',
            id                  : orgInstance.id,
            editable            : editable
    ]}"/>

</div>

%{--------------------}%
</body>


<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.personCreate = function (contactFor) {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&showAddresses=true&showContacts=true';
        var func = bb8.ajax4SimpleModalFunction("#personModal", url, false);
        func();
    }

    JSPC.app.addresscreate_org = function (orgId) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?orgId=' + orgId;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url, false);
        func();
    }
</laser:script>
</html>
