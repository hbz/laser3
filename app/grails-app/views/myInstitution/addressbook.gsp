<%@ page
        import="de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.Org; de.laser.Person; com.k_int.kbplus.PersonRole; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory"
%>

<!doctype html>
<%-- r:require module="annotations" / --%>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.institutions.myAddressbook')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.myAddressbook" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
        <g:if test="${editable}">
            <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">
                <semui:actionsDropdownItem message="person.create_new.contactPersonForInstitution.label"
                                           data-semui="modal"
                                           href="#institutionPersonFormModal"/>
            </g:if>

            <semui:actionsDropdownItem message="person.create_new.contactPersonForProviderAgency.label"
                                       data-semui="modal"
                                       href="#providerAgencyPersonFormModal"/>

            <semui:actionsDropdownItem message="person.create_new.contactPersonForPublic.label" data-semui="modal"
                                       href="#publicPersonFormModal"/>

        </g:if>



        <semui:actionsDropdownItem notActive="true" data-semui="modal" href="#copyFilteredEmailAddresses_ajaxModal"
                                   message="menu.institutions.copy_emailaddresses.button"/>
    </semui:actionsDropdown>
</semui:controlButtons>

<g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">
    <g:render template="/templates/cpa/personFormModal" model="['org'               : institution,
                                                                'isPublic'          : RDStore.YN_NO,
                                                                'presetFunctionType': RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                                                                'showContacts'      : true,
                                                                'addContacts'       : true,
                                                                'modalID'           : 'institutionPersonFormModal',
                                                                'url'               : [controller: 'person', action: 'create']
    ]"/>
</g:if>

<g:render template="/templates/cpa/personFormModal" model="['org'               : institution,
                                                            'isPublic'          : RDStore.YN_NO,
                                                            'presetFunctionType': RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                                                            'showContacts'      : true,
                                                            'addContacts'       : true,
                                                            'modalID'           : 'providerAgencyPersonFormModal',
                                                            'url'               : [controller: 'person', action: 'create']
]"/>


<g:render template="/templates/cpa/personFormModal" model="['org'               : institution,
                                                            'isPublic'          : RDStore.YN_YES,
                                                            'presetFunctionType': RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                                                            'showContacts'      : true,
                                                            'addContacts'       : true,
                                                            'modalID'           : 'publicPersonFormModal',
                                                            'url'               : [controller: 'person', action: 'create']
]"/>

<g:render template="/templates/copyFilteredEmailAddresses"
          model="[emailAddresses: emailAddresses]"/>


<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'menu.institutions.myAddressbook')}
<semui:totalNumber total="${num_visiblePersons}"/>
</h1>

<semui:messages data="${flash}"/>


<g:render template="/person/formModal" model="['org'               : institution,
                                               'isPublic'          : false,
                                               'presetFunctionType': RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
]"/>

<g:render template="/templates/filter/javascript"/>
<semui:filter showFilterButton="true">
    <g:form action="addressbook" controller="myInstitution" method="get" class="ui small form">
        <div class="four fields">
            <div class="field">
                <label for="prs">${message(code: 'person.filter.name')}</label>

                <div class="ui input">
                    <input type="text" id="prs" name="prs" value="${params.prs}"
                           placeholder="${message(code: 'person.filter.name')}"/>
                </div>
            </div>

            <div class="field">
                <label for="org">${message(code: 'person.filter.org')}</label>

                <div class="ui input">
                    <input type="text" id="org" name="org" value="${params.org}"
                           placeholder="${message(code: 'person.filter.org')}"/>
                </div>
            </div>
            <g:render template="/templates/properties/genericFilter" model="[propList: propList]"/>
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

<g:render template="/templates/cpa/person_table" model="${[
        persons       : visiblePersons,
        restrictToOrg : null,
        showContacts  : true,
        showAddresses : true,
        tmplConfigShow: ['linenumber', 'organisation', 'functionPosition', 'name', 'showContacts', 'showAddresses']
]}"/>

<semui:paginate action="addressbook" controller="myInstitution" params="${params}"
                next="${message(code: 'default.paginate.next')}"
                prev="${message(code: 'default.paginate.prev')}"
                max="${max}"
                total="${num_visiblePersons}"/>

</body>
</html>
