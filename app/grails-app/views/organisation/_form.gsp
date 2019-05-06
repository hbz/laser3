<%@ page import="com.k_int.kbplus.Org" %>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'globalUID', 'error')} ">
	<label for="globalUID">
		<g:message code="org.globalUID.label" default="Global UID" />
	</label>
	<g:textArea name="globalUID" cols="40" rows="5" maxlength="255" value="${orgInstance?.globalUID}"/>
</div>
*/ %>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="org.name.label" default="Name" />
	</label>
    <g:textField name="name" maxlength="255" value="${orgInstance?.name}"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'shortname', 'error')} ">
	<label for="shortname">
		<g:message code="org.shortname.label" default="Shortname" />
	</label>
    <g:textField name="shortname" maxlength="255" value="${orgInstance?.shortname}"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'sortname', 'error')} ">
	<label for="sortname">
		<g:message code="org.sortname.label" default="Sortname" />
	</label>
    <g:textField name="sortname" maxlength="255" value="${orgInstance?.sortname}"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'url', 'error')} ">
	<label for="url">
		<g:message code="org.url.label" default="Url" />
	</label>
    <g:textField name="url" maxlength="512" value="${orgInstance?.url}"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'urlGov', 'error')} ">
	<label for="urlGov">
		<g:message code="org.urlGov.label" default="UrlGov" />
	</label>
	<g:textField name="urlGov" maxlength="512" value="${orgInstance?.urlGov}"/>
</div>

%{--<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'fteStudents', 'error')}">
	<label for="fteStudents">
		<g:message code="org.fteStudents.label" default="Fte Students" />
	</label>
	<g:field name="fteStudents" type="number" value="${orgInstance?.fteStudents}" required=""/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'fteStaff', 'error')}">
	<label for="fteStaff">
		<g:message code="org.fteStaff.label" default="Fte Staff" />
	</label>
	<g:field name="fteStaff" type="number" value="${orgInstance?.fteStaff}" required=""/>
</div>--}%

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'impId', 'error')} ">
	<label for="impId">
		<g:message code="org.impId.label" default="Imp Id" />
	</label>
	<g:textArea name="impId" cols="40" rows="5" maxlength="255" value="${orgInstance?.impId}"/>
</div>
*/ %>

<%--
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'comment', 'error')} ">
	<label for="comment">
		<g:message code="org.comment.label" default="Comment" />
	</label>
	<g:textArea name="comment" cols="40" rows="5" maxlength="2048" value="${orgInstance?.comment}"/>
</div> --%>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'ipRange', 'error')} ">
	<label for="ipRange">
		<g:message code="org.ipRange.label" default="Ip Range" />
	</label>
	<g:textArea name="ipRange" cols="40" rows="5" maxlength="1024" value="${orgInstance?.ipRange}"/>
</div>
*/ %>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'sector', 'error')} ">
	<label for="sector">
		<g:message code="org.sector.label" default="Sector" />
	</label>
	<laser:select id="sector" name="sector.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgSector')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.sector?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'shortcode', 'error')} ">
	<label for="shortcode">
		<g:message code="org.shortcode.label" default="Shortcode" />
	</label>
	<g:textField name="shortcode" maxlength="128" value="${orgInstance?.shortcode}"/>
</div>
*/ %>

<%--
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'scope', 'error')} ">
	<label for="scope">
		<g:message code="org.scope.label" default="Scope" />
	</label>
	<g:textField name="scope" maxlength="128" value="${orgInstance?.scope}"/>
</div> --%>

<%--
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'categoryId', 'error')} ">
	<label for="categoryId">
		<g:message code="org.categoryId.label" default="Category Id" />
	</label>
	<g:textField name="categoryId" maxlength="128" value="${orgInstance?.categoryId}"/>
</div> --%>

%{--<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'orgType', 'error')} ">
	<label for="orgType">
		<g:message code="org.orgType.label" default="Org Type" />
	</label>
	<laser:select id="orgType" name="orgType.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('OrgType')}"
			  optionKey="id" optionValue="value"  value="${orgInstance?.orgType?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>--}%

<%--
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'status', 'error')} ">
	<label for="status">
		<g:message code="org.status.label" default="Status" />
	</label>
	<g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues()}"
			  optionKey="id" value="${orgInstance?.status?.id}" class="many-to-one" noSelection="['null': '']"/>
</div> --%>

<%--
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'membership', 'error')} ">
	<label for="membership">
		<g:message code="org.membership.label" default="Membership" />
	</label>
	<g:select id="membership" name="membership.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues()}"
			  optionKey="id" value="${orgInstance?.membership?.id}" class="many-to-one" noSelection="['null': '']"/>
</div> --%>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'country', 'error')} ">
	<label for="country">
		<g:message code="org.country.label" default="Country" />
	</label>
	<laser:select id="country" name="country.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Country')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.country?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'federalState', 'error')} ">
	<label for="federalState">
		<g:message code="org.federalState.label" default="Federal State" />
	</label>
	<laser:select id="federalState" name="federalState.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Federal State')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.federalState?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'libraryNetwork', 'error')} ">
	<label for="libraryNetwork">
		<g:message code="org.libraryNetwork.label" default="Library Network" />
	</label>
	<laser:select id="libraryNetwork" name="libraryNetwork.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Library Network')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.libraryNetwork?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'funderType', 'error')} ">
	<label for="funderType">
		<g:message code="org.funderType.label" default="Funder Type" />
	</label>
	<laser:select id="funderType" name="funderType.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Funder Type')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.funderType?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'libraryType', 'error')} ">
	<label for="libraryType">
		<g:message code="org.libraryType.label" default="Library Type" />
	</label>
	<laser:select id="libraryType" name="libraryType.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Library Type')}"
			  optionKey="id" optionValue="value" value="${orgInstance?.libraryType?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'importSource', 'error')} ">
	<label for="importSource">
		<g:message code="org.importSource.label" default="Import Source" />
	</label>
	<g:textField name="importSource" value="${orgInstance?.importSource}"/>
</div>
*/ %>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'lastImportDate', 'error')} ">
	<label for="lastImportDate">
		<g:message code="org.lastImportDate.label" default="Last Import Date" />
	</label>
	<g:datePicker name="lastImportDate" precision="day"  value="${orgInstance?.lastImportDate}" default="none" noSelection="['': '']" />
</div>
*/ %>

<% /*
<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'addresses', 'error')} ">
	<label for="addresses">
		<g:message code="org.addresses.label" default="Addresses" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.addresses?}" var="a">
			<li><g:link controller="address" action="show" id="${a.id}">${a}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="address" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'affiliations', 'error')} ">
	<label for="affiliations">
		<g:message code="org.affiliations.label" default="Affiliations" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.affiliations?}" var="a">
			<li><g:link controller="userOrg" action="show" id="${a.id}">${a}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="userOrg" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'userOrg.label', default: 'UserOrg')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'contacts', 'error')} ">
	<label for="contacts">
		<g:message code="org.contacts.label" default="Contacts" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.contacts?}" var="c">
			<li><g:link controller="contact" action="show" id="${c.id}">${c}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="contact" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'customProperties', 'error')} ">
	<label for="customProperties">
		<g:message code="org.customProperties.label" default="Custom Properties" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.customProperties?}" var="c">
			<li><g:link controller="orgCustomProperty" action="show" id="${c.id}">${c}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="orgCustomProperty" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgCustomProperty.label', default: 'OrgCustomProperty')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'ids', 'error')} ">
	<label for="ids">
		<g:message code="org.ids.label" default="Ids" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.ids?}" var="i">
			<li><g:link controller="identifierOccurrence" action="show" id="${i.id}">${i}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="identifierOccurrence" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'incomingCombos', 'error')} ">
	<label for="incomingCombos">
		<g:message code="org.incomingCombos.label" default="Incoming Combos" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.incomingCombos?}" var="i">
			<li><g:link controller="combo" action="show" id="${i.id}">${i}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="combo" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'combo.label', default: 'Combo')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'links', 'error')} ">
	<label for="links">
		<g:message code="org.links.label" default="Links" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.links?}" var="l">
			<li><g:link controller="orgRole" action="show" id="${l.id}">${l}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="orgRole" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgRole.label', default: 'OrgRole')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'outgoingCombos', 'error')} ">
	<label for="outgoingCombos">
		<g:message code="org.outgoingCombos.label" default="Outgoing Combos" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.outgoingCombos?}" var="o">
			<li><g:link controller="combo" action="show" id="${o.id}">${o}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="combo" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'combo.label', default: 'Combo')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'privateProperties', 'error')} ">
	<label for="privateProperties">
		<g:message code="org.privateProperties.label" default="Private Properties" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.privateProperties?}" var="p">
			<li><g:link controller="orgPrivateProperty" action="show" id="${p.id}">${p}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="orgPrivateProperty" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgPrivateProperty.label', default: 'OrgPrivateProperty')])}</g:link>
		</li>
	</ul>
</div>

<div class="field fieldcontain ${hasErrors(bean: orgInstance, field: 'prsLinks', 'error')} ">
	<label for="prsLinks">
		<g:message code="org.prsLinks.label" default="Prs Links" />
	</label>

	<ul class="one-to-many">
		<g:each in="${orgInstance?.prsLinks?}" var="p">
			<li><g:link controller="personRole" action="show" id="${p.id}">${p}</g:link></li>
		</g:each>
		<li class="add">
			<g:link controller="personRole" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'personRole.label', default: 'PersonRole')])}</g:link>
		</li>
	</ul>
</div>
*/ %>
