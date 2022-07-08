<%@ page import="de.laser.PersonRole; de.laser.RefdataCategory; de.laser.Org; de.laser.Person; de.laser.storage.RDConstants" %>

<div class="field ${hasErrors(bean: personInstance, field: 'title', 'error')}">
	<label for="title">
		<g:message code="person.title.label"/>
	</label>
	<g:textField id="title" name="title" value="${personInstance?.title}"/>

</div>

<div class="field ${hasErrors(bean: personInstance, field: 'first_name', 'error')} required">
	<label for="first_name">
		<g:message code="person.first_name.label" />
	</label>
	<g:textField id="first_name" name="first_name" required="" value="${personInstance?.first_name}"/>

</div>

<div class="field ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
	<label for="middle_name">
		<g:message code="person.middle_name.label" />
		
	</label>
	<g:textField id="middle_name" name="middle_name" value="${personInstance?.middle_name}"/>

</div>

<div class="field ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
	<label for="last_name">
		<g:message code="person.last_name.label" />
	</label>
	<g:textField id="last_name" name="last_name" required="" value="${personInstance?.last_name}"/>

</div>

<div class="field ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
    <label for="gender">
        <g:message code="person.gender.label" />

    </label>
    <laser:select class="ui dropdown" id="gender" name="gender"
                  from="${Person.getAllRefdataValues(RDConstants.GENDER)}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.gender?.id}"
                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
</div>
<%--
<div class="field ${hasErrors(bean: personInstance, field: 'roleType', 'error')} ">
    <label for="roleType">
		${RefdataCategory.getByDesc(RDConstants.PERSON_POSITION).getI10n('desc')}

    </label>
    <laser:select class="ui dropdown" id="roleType" name="roleType"
                  from="${Person.getAllRefdataValues(RDConstants.PERSON_POSITION)}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.roleType?.id}"
                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
</div>
--%>
<div class="field ${hasErrors(bean: personInstance, field: 'contactType', 'error')} ">
    <label for="contactType">
		${RefdataCategory.getByDesc(RDConstants.PERSON_CONTACT_TYPE).getI10n('desc')}

    </label>
    <laser:select class="ui dropdown" id="contactType" name="contactType"
                  from="${Person.getAllRefdataValues(RDConstants.PERSON_CONTACT_TYPE)}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.contactType?.id}"
                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
</div>

<div class="field ${hasErrors(bean: personInstance, field: 'contacts', 'error')} ">
	<label for="contacts">
		<g:message code="person.contacts.label" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.contacts?}" var="c">
		    <li><g:link controller="contact" action="show" id="${c.id}">${c}</g:link></li>
		</g:each>
	</ul>
    <% /* <input class="ui button" type="button" data-semui="modal" href="#contactFormModal"
			   value="${message(code: 'default.add.label', args: [message(code: 'contact.label')])}"> */ %>
    <g:if test="${personInstance?.id}">
        <g:link class="ui button" controller="contact" action="create" params="['prs.id': personInstance?.id]">
            ${message(code: 'default.add.label', args: [message(code: 'contact.label')])}
        </g:link>
    </g:if>
    <% /* <laser:render template="/contact/formModal" model="['prsId': personInstance?.id]"/> */ %>
</div>

<div class="field ${hasErrors(bean: personInstance, field: 'addresses', 'error')} ">
	<label for="addresses">
		<g:message code="person.addresses.label" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.addresses?}" var="a">
		    <li><g:link controller="address" action="show" id="${a.id}">${a}</g:link></li>
		</g:each>
        <% /* <input class="ui button" type="button" data-semui="modal" href="#addressFormModal"
			   value="${message(code: 'default.add.label', args: [message(code: 'address.label')])}"> */ %>
	</ul>
    <g:if test="${personInstance?.id}">
        <g:link class="ui button" controller="address" action="create" params="['prs.id': personInstance?.id]">
            ${message(code: 'default.add.label', args: [message(code: 'address.label')])}
        </g:link>
    </g:if>
    <% /* <laser:render template="/templates/cpa/addressFormModal" model="['prsId': personInstance?.id]"/> */ %>
</div>

<div class="field ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
    <label for="isPublic">
        <g:message code="person.isPublic.label" />
    </label>
    <laser:select class="ui dropdown" id="isPublic" name="isPublic"
                  from="${Person.getAllRefdataValues(RDConstants.Y_N)}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.isPublic?.id}" /><%-- todo: ERMS-1562 --%>
</div>

<div class="field ${hasErrors(bean: personInstance, field: 'tenant', 'error')} required">
    <label for="tenant">
        <g:message code="person.tenant.label" />
    </label>
    <g:select id="tenant" name="tenant.id" class="ui dropdown" from="${userMemberships}"
              optionKey="id" value="${personInstance?.tenant?.id}" />
</div>

<div id="person-role-manager">

	<div class="ui person-role-function-manager">
		<h4 class="ui header"><g:message code="person.functions.label" /></h4>

		<div class="field">
			<div class="two fields">
				<div class="field wide ten">
					<laser:select class="ui dropdown"
								  name="ignore-functionType-selector"
								  from="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}"
								  optionKey="id"
								  optionValue="value" />
				</div>
				<div class="field wide six">
					<button class="ui button add-person-role" type="button">${message('code':'default.button.add.label')}</button>
				</div>
			</div>
		</div>

		<div class="ui segment workspace">
			<h5 class="ui header">
				<g:message code="default.button.create_new.label" />
			</h5>
			<div class="adding"></div>
			<h5 class="ui header">
				<g:message code="default.button.delete.label"/>
			</h5>
			<div class="existing"></div>
		</div>
	</div>

    <br />

	<div class="ui person-role-responsibility-manager">
		<h4 class="ui header"><g:message code="person.responsibilites.label" /></h4>

		<div class="field">
			<div class="two fields">
				<div class="field wide ten">
					<laser:select class="ui dropdown"
								  name="ignore-responsibilityType-selector"
								  from="${PersonRole.getAllRefdataValues(RDConstants.PERSON_RESPONSIBILITY)}"
								  optionKey="id"
								  optionValue="value" />
				</div>
				<div class="field wide six">
					<button class="ui button add-person-role" type="button">${message('code':'default.button.add.label')}</button>
				</div>
			</div>
		</div>
		<div class="ui segment workspace">
			<h5 class="ui header">
				<g:message code="default.button.create_new.label" />
			</h5>
			<div class="adding"></div>
			<h5 class="ui header">
				<g:message code="default.button.delete.label"/>
			</h5>
			<div class="existing"></div>
		</div>
	</div>


	<laser:script file="${this.getGroovyPageFileName()}">
		$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=func').done(function(data){
			$('.person-role-function-manager .workspace .existing').append(data);
		});
		$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=resp').done(function(data){
			$('.person-role-responsibility-manager .workspace .existing').append(data);
		});		


		$('.person-role-function-manager .add-person-role').click(function(){		
			var tt = $('.person-role-function-manager select').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=func&roleTypeId=' + tt + '&org=${params?.org?.id}').done(function(data){
				$('.person-role-function-manager .workspace .adding').append(data);
			});
		})
		
		$('.person-role-responsibility-manager .add-person-role').click(function(){
			var tt = $('.person-role-responsibility-manager select').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=resp&roleTypeId=' + tt + '&org=${params?.org?.id}').done(function(data){
				$('.person-role-responsibility-manager .workspace .adding').append(data);
			});
		})
	</laser:script>

</div>
