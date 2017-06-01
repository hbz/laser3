<%@ page import="com.k_int.kbplus.Org" %>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="org.name.label" default="Name" />

	</label>
	<g:textArea name="name" cols="40" rows="1" maxlength="256" value="${orgInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'impId', 'error')} ">
	<label for="impId">
		<g:message code="org.impId.label" default="Imp Id" />

	</label>
	<g:textArea name="impId" cols="40" rows="1" maxlength="256" value="${orgInstance?.impId}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'comment', 'error')} ">
	<label for="comment">
		<g:message code="org.comment.label" default="Comment" />
		
	</label>
	<g:textArea name="comment" cols="40" rows="5" maxlength="256" value="${orgInstance?.comment}"/>

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
	<g:select id="sector" name="sector.id" 
		from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgSector')}" 
		optionKey="id" 
		optionValue="value" 
		value="${orgInstance?.sector?.id}" 
		class="many-to-one" noSelection="['null': '']"/>

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
	<g:select id="orgType" name="orgType.id" 
		from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgType')}"
		optionKey="id"
		optionValue="value" 
		value="${orgInstance?.orgType?.id}" 
		class="many-to-one" noSelection="['null': '']"/>
	
</div>

<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'status', 'error')} ">
	<label for="status">
		<g:message code="org.status.label" default="Status" />
	</label>
	<g:select id="status" name="status.id" 
		from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('PendingChangeStatus')}"
		optionKey="id" 
		optionValue="value" 
		value="${orgInstance?.status?.id}" 
		class="many-to-one" noSelection="['null': '']"/>

</div>
<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'membership', 'error')} ">
	<label for="membership">
		<g:message code="org.membership.label" default="Membership" />
		
	</label>
	<g:select id="membership" name="membership.id"
                from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(''YN')}"
                optionKey="id"
                value="${orgInstance?.membership?.id}"
                class="many-to-one"
                noSelection="['null': '']"/>

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


<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'contacts', 'error')} ">
	<label for="contacts">
		<g:message code="person.contacts.label" default="Contacts" />
		
	</label>
	<ul class="one-to-many">
	<g:each in="${orgInstance?.contacts?}" var="o">
	    <li><g:link controller="contact" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></li>
	</g:each>
	<li class="add">
	<g:link controller="contact" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}</g:link>
	</li>
	</ul>
</div>

<!-- 
<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'links', 'error')} ">
	<label for="links">
		<g:message code="org.links.other.label" default="Links" />
		
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
-->
<!-- 
<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'prsLinks', 'error')} ">
	<label for="prsLinks">
		<g:message code="org.prsLinks.label" default="prsLinks" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${orgInstance?.prsLinks?}" var="p">
    <li><g:link controller="personRole" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="personRole" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'personRole.label', default: 'PersonRole')])}</g:link>
</li>
</ul>

</div>
-->

<h3>Org-to-X-with-PersonRole (Demo only)</h3>
<p>TODO</p>

<g:if test="${orgInstance?.prsLinks}">
				<dl>
					<dt><g:message code="org.prsLinks.label" default="Person Roles" /></dt>
					<dd><ul>
						<g:each in="${orgInstance.prsLinks}" var="p">
							<li>
								${p.roleType?.value} - 
                                
                                <g:if test="${p.cluster}">
                                	<g:link controller="cluster" action="show" id="${p.cluster.id}">Cluster: ${p.cluster.name}</g:link>
                                </g:if>
                                <g:if test="${p.pkg}">
                                	<g:link controller="package" action="show" id="${p.pkg.id}">Package: ${p.pkg.name}</g:link>
                                </g:if>
                                <g:if test="${p.sub}">
                                	<g:link controller="subscription" action="show" id="${p.sub.id}">Subscription: ${p.sub.name}</g:link>
                                </g:if>
                                <g:if test="${p.lic}">Licence: ${p.lic.id}</g:if>
                                <g:if test="${p.title}">
                                	<g:link controller="titleInstance" action="show" id="${p.title.id}">Title: ${p.title.title}</g:link>
                                </g:if> 
						 	</li>
						</g:each>
					</ul></dd>
				</dl>
			</g:if>
			
<h3>Org-to-X-with-Role (Demo only)</h3>

<div id="ui-placeholder-cluster" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-lic" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-pkg" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-sub" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-title" class="ui-ajax"></div>

<g:if test="${orgInstance?.id != null}">
	<script>
		$.get("${webRequest.baseUrl}/org/ajax/${orgInstance.id}?type=cluster").done(function(data){
			$("#ui-placeholder-cluster").append(data);
		});
		$.get("${webRequest.baseUrl}/org/ajax/${orgInstance.id}?type=lic").done(function(data){
			$("#ui-placeholder-lic").append(data);
		});
		$.get("${webRequest.baseUrl}/org/ajax/${orgInstance.id}?type=pkg").done(function(data){
			$("#ui-placeholder-pkg").append(data);
		});
		$.get("${webRequest.baseUrl}/org/ajax/${orgInstance.id}?type=sub").done(function(data){
			$("#ui-placeholder-sub").append(data);
		});
		$.get("${webRequest.baseUrl}/org/ajax/${orgInstance.id}?type=title").done(function(data){
			$("#ui-placeholder-title").append(data);
		});
	</script>
</g:if>
