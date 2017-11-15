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

   <g:if test="${flash.message}">
      <div>
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </div>
    </g:if>

    <g:if test="${flash.error}">
      <div>
        <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
      </div>
    </g:if>

    <div>

        <h1 class="ui header">${institution?.name} - ${message(code:'menu.institutions.addressbook', default:'Addressbook')}</h1>

    </div>

    <div>
        <div class="row">
            <div class="span8">
		        <p>${message(code:'myinst.addressBook.visible', default:'These persons are visible to you due your membership')} ..</p>
		
                <dl>
                    <g:if test="${visiblePersons}">
                        <dt><g:message code="org.prsLinks.label" default="Persons" /></dt>
                        <g:each in="${visiblePersons}" var="p">
                            <g:render template="/templates/cpa/person_details" model="${[person: p]}"></g:render>
                        </g:each>
                    </g:if>
                </dl>
            </div>
            <div class="span4">
                <semui:card title="person.create_new.label" class="card-grey">
                    <g:link controller="person" action="create" params="['tenant.id': institution?.id, 'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No').id ]" >
                        ${message(code: 'default.add.label', args: [message(code: 'person.label', default: 'Person')])}
                    </g:link>
                </semui:card>
            </div>
        </div><!--.row-->
      </div><!--.container-->

  </body>
</html>
