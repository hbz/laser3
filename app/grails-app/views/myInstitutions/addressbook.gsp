<%@ page
import="com.k_int.kbplus.Org"  
import="com.k_int.kbplus.Person" 
import="com.k_int.kbplus.PersonRole"
import="com.k_int.kbplus.RefdataValue" 
import="com.k_int.kbplus.RefdataCategory" 
%>

<!doctype html>
<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'menu.institutions.addressbook', default:'Addressbook')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
            <semui:crumb message="menu.institutions.addressbook" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui header">${institution?.name} - ${message(code:'menu.institutions.addressbook', default:'Addressbook')}</h1>

        <semui:messages data="${flash}" />

        <div>
            <p>${message(code:'myinst.addressBook.visible', default:'These persons are visible to you due your membership')} ..</p>

            <div>
                <input class="ui button"
                   value="${message(code: 'default.add.label', args: [message(code: 'person.label', default: 'Person')])}"
                   data-semui="modal"
                   href="#personFormModal" />

                <g:render template="/person/formModal" model="['org': institution, 'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No')]"/>
            </div>

            <g:if test="${visiblePersons}">
                <h5 class="ui header"><g:message code="org.prsLinks.label" default="Persons" /></h5>
                <div class="ui divided list">
                    <g:each in="${visiblePersons}" var="p">
                        <g:render template="/templates/cpa/person_details" model="${[person: p]}"></g:render>
                    </g:each>
                </div>
            </g:if>

        </div>

  </body>
</html>
