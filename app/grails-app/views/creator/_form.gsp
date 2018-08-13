<%@ page import="com.k_int.kbplus.Creator" %>



<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'lastname', 'error')} required">
	<label for="lastname">
		<g:message code="creator.lastname.label" default="Lastname" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="lastname" required="" value="${creatorInstance?.lastname}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'firstname', 'error')} ">
	<label for="firstname">
		<g:message code="creator.firstname.label" default="Firstname" />
		
	</label>
	<g:textField name="firstname" value="${creatorInstance?.firstname}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'middlename', 'error')} ">
	<label for="middlename">
		<g:message code="creator.middlename.label" default="Middlename" />
		
	</label>
	<g:textField name="middlename" value="${creatorInstance?.middlename}"/>

</div>

%{--<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'gnd_id', 'error')} ">
	<label for="gnd_id">
		<g:message code="creator.gnd_id.label" default="Gndid" />
		
	</label>
	<g:select id="gnd_id" name="gnd_id.id" from="${com.k_int.kbplus.IdentifierOccurrence.list()}" optionKey="id" value="${creatorInstance?.gnd_id?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>--}%

%{--<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'globalUID', 'error')} ">
	<label for="globalUID">
		<g:message code="creator.globalUID.label" default="Global UID" />
		
	</label>
	<g:textArea name="globalUID" cols="40" rows="5" maxlength="255" value="${creatorInstance?.globalUID}"/>

</div>--}%


%{--<div class="fieldcontain ${hasErrors(bean: creatorInstance, field: 'title', 'error')} ">
		<label for="title">
            <g:message code="creator.title.label" default="Title" />

        </label>--}%
	
%{--<ul class="one-to-many">
<g:each in="${creatorInstance?.title?}" var="t">
    <li><g:link controller="creatorTitle" action="show" id="${t.id}">${t}</g:link></li>
</g:each>
<li class="add">
<g:link controller="creatorTitle" action="create" params="['creator.id': creatorInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle')])}</g:link>
</li>
</ul>


</div>--}%

