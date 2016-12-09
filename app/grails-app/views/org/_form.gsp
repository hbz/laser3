<%@ page import="com.k_int.kbplus.Org" %>



<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'impId', 'error')} ">
	<label for="impId">
		<g:message code="org.impId.label" default="Imp Id" />
		
	</label>
	<g:textArea name="impId" cols="40" rows="5" maxlength="256" value="${orgInstance?.impId}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'address', 'error')} ">
	<label for="address">
		<g:message code="org.address.label" default="Address" />
		
	</label>
	<g:textArea name="address" cols="40" rows="5" maxlength="256" value="${orgInstance?.address}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'ipRange', 'error')} ">
	<label for="ipRange">
		<g:message code="org.ipRange.label" default="Ip Range" />
		
	</label>
	<g:textArea name="ipRange" cols="40" rows="5" maxlength="1024" value="${orgInstance?.ipRange}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'sector', 'error')} ">
	<label for="sector">
		<g:message code="org.sector.label" default="Sector" />
		
	</label>
	<g:textField name="sector" maxlength="128" value="${orgInstance?.sector}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'shortcode', 'error')} ">
	<label for="shortcode">
		<g:message code="org.shortcode.label" default="Shortcode" />
		
	</label>
	<g:textField name="shortcode" maxlength="128" value="${orgInstance?.shortcode}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'scope', 'error')} ">
	<label for="scope">
		<g:message code="org.scope.label" default="Scope" />
		
	</label>
	<g:textField name="scope" maxlength="128" value="${orgInstance?.scope}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'categoryId', 'error')} ">
	<label for="categoryId">
		<g:message code="org.categoryId.label" default="Category Id" />
		
	</label>
	<g:textField name="categoryId" maxlength="128" value="${orgInstance?.categoryId}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'orgType', 'error')} ">
	<label for="orgType">
		<g:message code="org.orgType.label" default="Org Type" />
		
	</label>
	<g:select id="orgType" name="orgType.id" from="${com.k_int.kbplus.RefdataValue.list()}" optionKey="id" value="${orgInstance?.orgType?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'status', 'error')} ">
	<label for="status">
		<g:message code="org.status.label" default="Status" />
		
	</label>
	<g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataValue.list()}" optionKey="id" value="${orgInstance?.status?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'membership', 'error')} ">
	<label for="membership">
		<g:message code="org.membership.label" default="Membership" />
		
	</label>
	<g:select id="membership" name="membership.id" from="${com.k_int.kbplus.RefdataValue.list()}" optionKey="id" value="${orgInstance?.membership?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'affiliations', 'error')} ">
	<label for="affiliations">
		<g:message code="org.affiliations.label" default="Affiliations" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.affiliations?}" var="a">
    <li><g:link controller="userOrg" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="userOrg" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'userOrg.label', default: 'UserOrg')])}</g:link>
</li>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'customProperties', 'error')} ">
	<label for="customProperties">
		<g:message code="org.customProperties.label" default="Custom Properties" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.customProperties?}" var="c">
    <li><g:link controller="orgCustomProperty" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="orgCustomProperty" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgCustomProperty.label', default: 'OrgCustomProperty')])}</g:link>
</li>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'ids', 'error')} ">
	<label for="ids">
		<g:message code="org.ids.label" default="Ids" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.ids?}" var="i">
    <li><g:link controller="identifierOccurrence" action="show" id="${i.id}">${i?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="identifierOccurrence" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence')])}</g:link>
</li>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'incomingCombos', 'error')} ">
	<label for="incomingCombos">
		<g:message code="org.incomingCombos.label" default="Incoming Combos" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.incomingCombos?}" var="i">
    <li><g:link controller="combo" action="show" id="${i.id}">${i?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="combo" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'combo.label', default: 'Combo')])}</g:link>
</li>
</ul>


</div>


<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="org.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${orgInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'outgoingCombos', 'error')} ">
	<label for="outgoingCombos">
		<g:message code="org.outgoingCombos.label" default="Outgoing Combos" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.outgoingCombos?}" var="o">
    <li><g:link controller="combo" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="combo" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'combo.label', default: 'Combo')])}</g:link>
</li>
</ul>


</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'links', 'error')} ">
	<label for="links">
		<g:message code="org.links.label" default="Links" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.links?}" var="l">
    <li><g:link controller="orgRole" action="show" id="${l.id}">${l?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="orgRole" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgRole.label', default: 'OrgRole')])}</g:link>
</li>
</ul>


</div>

<div id="ui-placeholder">
	<g:if test="${orgInstance?.id != null}">
		<script>
			$.get("/demo/org/ajax/${orgInstance?.id}").done(function(data){
					$("#ui-placeholder").append(data);
			});
		</script>
	</g:if>
</div>


