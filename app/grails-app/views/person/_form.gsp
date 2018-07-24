<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'title', 'error')}">
	<label for="title">
		<g:message code="person.title.label" default="Title" />
	</label>
	<g:textField name="title" required="" value="${personInstance?.title}"/>

</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'first_name', 'error')} required">
	<label for="first_name">
		<g:message code="person.first_name.label" default="Firstname" />
	</label>
	<g:textField name="first_name" required="" value="${personInstance?.first_name}"/>

</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
	<label for="middle_name">
		<g:message code="person.middle_name.label" default="Middlename" />
		
	</label>
	<g:textField name="middle_name" value="${personInstance?.middle_name}"/>

</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
	<label for="last_name">
		<g:message code="person.last_name.label" default="Lastname" />
	</label>
	<g:textField name="last_name" required="" value="${personInstance?.last_name}"/>

</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
    <label for="gender">
        <g:message code="person.gender.label" default="Gender" />

    </label>
    <laser:select class="ui dropdown" id="gender" name="gender"
                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Gender')}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.gender?.id}"
                  noSelection="['': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'roleType', 'error')} ">
    <label for="roleType">
		${com.k_int.kbplus.RefdataCategory.findByDesc('Person Position').getI10n('desc')}

    </label>
    <laser:select class="ui dropdown" id="roleType" name="roleType"
                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Person Position')}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.roleType?.id}"
                  noSelection="['': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'contactType', 'error')} ">
    <label for="contactType">
		${com.k_int.kbplus.RefdataCategory.findByDesc('Person Contact Type').getI10n('desc')}

    </label>
    <laser:select class="ui dropdown" id="contactType" name="contactType"
                  from="${com.k_int.kbplus.Person.getAllRefdataValues('Person Contact Type')}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.contactType?.id}"
                  noSelection="['': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'contacts', 'error')} ">
	<label for="contacts">
		<g:message code="person.contacts.label" default="Contacts" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.contacts?}" var="c">
		    <li><g:link controller="contact" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
		</g:each>
	</ul>
    <% /* <input class="ui button" type="button" data-semui="modal" href="#contactFormModal"
			   value="${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}"> */ %>
    <g:if test="${personInstance.id}">
        <g:link class="ui button" controller="contact" action="create" params="['prs.id': personInstance?.id]">
            ${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}
        </g:link>
    </g:if>
    <% /* <g:render template="/contact/formModal" model="['prsId': personInstance?.id]"/> */ %>
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'addresses', 'error')} ">
	<label for="addresses">
		<g:message code="person.addresses.label" default="Addresses" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.addresses?}" var="a">
		    <li><g:link controller="address" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
		</g:each>
        <% /* <input class="ui button" type="button" data-semui="modal" href="#addressFormModal"
			   value="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}"> */ %>
	</ul>
    <g:if test="${personInstance.id}">
        <g:link class="ui button" controller="address" action="create" params="['prs.id': personInstance?.id]">
            ${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}
        </g:link>
    </g:if>
    <% /* <g:render template="/address/formModal" model="['prsId': personInstance?.id]"/> */ %>
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
    <label for="isPublic">
        <g:message code="person.isPublic.label" default="IsPublic" />
    </label>
    <laser:select class="ui dropdown" id="isPublic" name="isPublic"
                  from="${com.k_int.kbplus.Person.getAllRefdataValues('YN')}"
                  optionKey="id"
                  optionValue="value"
                  value="${personInstance?.isPublic?.id}" />
</div>

<div class="field fieldcontain ${hasErrors(bean: personInstance, field: 'tenant', 'error')} required">
    <label for="org">
        <g:message code="person.tenant.label" default="Tenant (Permissions to edit this person and depending addresses and contacts)" />
    </label>
    <g:select id="tenant" name="tenant.id" class="ui dropdown" from="${userMemberships}"
              optionKey="id" value="${personInstance?.tenant?.id}" />
</div>

<div id="person-role-manager">

	<div class="ui person-role-function-manager">
		<h4 class="ui header"><g:message code="person.functions.label" default="Functions" /></h4>

		<div class="field">
			<div class="two fields">
				<div class="field wide ten">
					<laser:select class="ui dropdown"
								  name="ignore-functionType-selector"
								  from="${PersonRole.getAllRefdataValues('Person Function')}"
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
				<g:message code="default.button.create_new.label" default="Adding"/>
			</h5>
			<div class="adding"></div>
			<h5 class="ui header">
				<g:message code="default.button.delete.label" default="Delete"/>
			</h5>
			<div class="existing"></div>
		</div>
	</div>

    <br />

	<div class="ui person-role-responsibility-manager">
		<h4 class="ui header"><g:message code="person.responsibilites.label" default="Responsibilites" /></h4>

		<div class="field">
			<div class="two fields">
				<div class="field wide ten">
					<laser:select class="ui dropdown"
								  name="ignore-responsibilityType-selector"
								  from="${PersonRole.getAllRefdataValues('Person Responsibility')}"
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
				<g:message code="default.button.create_new.label" default="Adding"/>
			</h5>
			<div class="adding"></div>
			<h5 class="ui header">
				<g:message code="default.button.delete.label" default="Delete"/>
			</h5>
			<div class="existing"></div>
		</div>
	</div>


	<script>
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
	</script>

</div>
