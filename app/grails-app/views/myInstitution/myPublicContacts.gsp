<%@ page import="de.laser.helper.RDStore;de.laser.helper.RDConstants;" %>
<%@ page import="com.k_int.kbplus.Org; de.laser.Person; com.k_int.kbplus.PersonRole; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<!doctype html>

<laser:serviceInjection/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.institutions.publicContacts')}</title>
</head>

<body>

<semui:breadcrumbs>
    <g:if test="${institution.id != contextService.getOrg().id}">
        <semui:crumb text="${institution.getDesignation()}" class="active"/>
    </g:if>
    <semui:crumb message="menu.institutions.publicContacts" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>
<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${institution.name}</h1>

<semui:messages data="${flash}"/>

<%-- test, very ugly, is to avoid Hibernate Proxy exception when changing context --%>
<g:render template="/organisation/nav" model="${[orgInstance: Org.get(institution.id), inContextOrg: true]}"/>


<div class="ui top attached tabular menu">
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

    <semui:controlButtons>
        <semui:actionsDropdown>
            <g:if test="${editable && contextService.user.hasAffiliation('INST_EDITOR')}">
                <semui:actionsDropdownItem data-semui="modal" href="#personEditModal"
                                           message="person.create_new.contactPerson.label"/>
            </g:if><g:else>
            <semui:actionsDropdownItemDisabled data-semui="modal" href="#personEditModal"
                                               message="person.create_new.contactPerson.label"/>
        </g:else>
            <semui:actionsDropdownItem notActive="true" data-semui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                       message="menu.institutions.copy_emailaddresses.button"/>
        </semui:actionsDropdown>
    </semui:controlButtons>


    <g:render template="/templates/cpa/personFormModal" model="['org'   : institution,
                                                                'isPublic'           : RDStore.YN_YES,
                                                                'presetFunctionType' : RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                                                                'showContacts'       : true,
                                                                'addContacts'       : true,
                                                                'url'             :[controller: 'person', action: 'create']
    ]"/>

    <g:render template="/templates/copyFilteredEmailAddresses" model="[orgList: [institution], emailAddresses: emailAddresses]"/>
    <br>


    <semui:filter>
        <g:form action="${actionName}" controller="myInstitution" method="get" params="${params}" class="ui small form">
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
                                  value="${params.position}"/>
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
              model="${[persons: visiblePersons, restrictToOrg: null, showContacts: true]}"/>

    <semui:paginate action="myPublicContacts" controller="myInstitution" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</div>

%{--------------------}%
<div class="ui bottom attached tab segment ${params.tab == 'personAddresses' ? 'active' : ''}" data-tab="personAddresses">

    <semui:filter>
        <g:form action="${actionName}" controller="myInstitution" method="get" params="${params}" class="ui small form">
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
                                  value="${params.position}"/>
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
              model="${[persons: visiblePersons, restrictToOrg: null, showAddresses: true]}"/>

    <semui:paginate action="myPublicContacts" controller="myInstitution" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}"
                    max="${max}"
                    total="${num_visiblePersons}"/>

</div>

%{--------------------}%


%{--------------------}%
<div class="ui bottom attached tab segment ${params.tab == 'addresses' ? 'active' : ''}" data-tab="addresses">

    <semui:controlButtons>
        <semui:actionsDropdown>
            <g:if test="${editable && contextService.user.hasAffiliation('INST_EDITOR')}">
                <semui:actionsDropdownItem data-semui="modal" href="#addressFormModal"
                                           message="address.add.label"/>
            </g:if><g:else>
            <semui:actionsDropdownItemDisabled data-semui="modal" href="#addressFormModal"
                                               message="address.add.label"/>
        </g:else>

        </semui:actionsDropdown>
    </semui:controlButtons>
    <br>

    <g:render template="/templates/cpa/addressFormModal" model="['orgId'               : institution.id,
                                                                'url'                  : [controller: 'address', action: 'create']
    ]"/>

    <g:render template="/templates/cpa/address_table" model="${[
            hideAddressType     : true,
            addresses           : addresses,
            tmplShowDeleteButton: true,
            controller          : 'org',
            action              : 'show',
            id                  : institution.id,
            editable            : ((institution.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
    ]}"/>

</div>

%{--------------------}%
</body>


<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>
</html>
