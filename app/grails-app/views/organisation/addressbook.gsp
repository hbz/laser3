<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; de.laser.helper.RDStore; de.laser.helper.RDConstants " %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <g:if test="${editable}">
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
    </g:if>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            ${orgInstance.name} - ${message(code: 'menu.institutions.myAddressbook')}
	    </h1>

        <g:render template="nav" model="${[orgInstance: orgInstance]}" />

        <semui:messages data="${flash}" />

        <semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible" />

        <g:if test="${editable}">
            <input class="ui button"
               value="${message(code: 'person.create_new.contactPerson.label')}"
               data-semui="modal"
               data-href="#personFormModal" />
        </g:if>

        <g:render template="/person/formModal" model="['tenant': contextOrg,
                                                       'org': orgInstance,
                                                       'isPublic': false,
                                                       'presetFunctionType': RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]"/>

		<g:if test="${visiblePersons}">
			<g:render template="/templates/cpa/person_table" model="${[persons: visiblePersons, restrictToOrg: orgInstance]}" />
		</g:if>

  </body>
</html>
