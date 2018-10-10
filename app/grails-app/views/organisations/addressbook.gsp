<%@ page
import="com.k_int.kbplus.Org"  
import="com.k_int.kbplus.Person" 
import="com.k_int.kbplus.PersonRole"
import="com.k_int.kbplus.RefdataValue" 
import="com.k_int.kbplus.RefdataCategory" 
%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

        <h1 class="ui left aligned icon header"><semui:headerIcon />
            ${orgInstance.name}
	    </h1>

        <g:render template="nav" contextPath="." />

        <semui:messages data="${flash}" />

        <semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible" />

        <g:if test="${editable}">
            <input class="ui button"
               value="${message(code: 'person.create_new.contactPerson.label')}"
               data-semui="modal"
               href="#personFormModal" />
        </g:if>

        <g:render template="/person/formModal" model="['tenant': contextOrg,
                                                       'org': orgInstance,
                                                       'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No'),
                                                       'presetFunctionType': RefdataValue.getByValueAndCategory('General contact person', 'Person Function')]"/>

		<g:if test="${visiblePersons}">
			<g:render template="/templates/cpa/person_table" model="${[persons: visiblePersons]}" />
		</g:if>

  </body>
</html>
