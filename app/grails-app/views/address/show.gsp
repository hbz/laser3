
<%@ page import="com.k_int.kbplus.Address" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'address.label', default: 'Address')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui header"><g:message code="default.show.label" args="[entityName]" /></h1>
		<semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<div class="inline-lists">

					<dl>
							<dt><g:message code="address.street_1.label" default="Street1" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_1" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.street_2.label" default="Street2" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_2" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.pob.label" default="Pob" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pob" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.zipcode.label" default="Zipcode" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="zipcode" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.city.label" default="City" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="city" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.state.label" default="State" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="state" config="State" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.country.label" default="Country" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="country" config="Country" /></dd>
					</dl>
					<dl>
							<dt><g:message code="address.type.label" default="Type" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="type" config="AddressType" /></dd>
					</dl>
					<dl>
						<g:if test="${addressInstance?.prs}">
							<dt><g:message code="address.prs.label" default="Prs" /></dt>
							<dd><g:link controller="person" action="show" id="${addressInstance?.prs?.id}">${addressInstance?.prs?.encodeAsHTML()}</g:link></dd>
						</g:if>
					</dl>
					<dl>
						<g:if test="${addressInstance?.org}">
							<dt><g:message code="address.org.label" default="Org" /></dt>
							<dd><g:link controller="organisations" action="show" id="${addressInstance?.org?.id}">${addressInstance?.org?.encodeAsHTML()}</g:link></dd>
						</g:if>
					</dl>

					<dl class="debug-only">
						<g:if test="${addressInstance?.prs?.tenant}">
							<dt><g:message code="person.tenant.label" default="Tenant (derived from Prs)" /></dt>
							<dd><g:link controller="organisations" action="show" id="${addressInstance?.prs?.tenant?.id}">${addressInstance?.prs?.tenant?.encodeAsHTML()}</g:link></dd>
						</g:if>
					</dl>
					<dl class="debug-only">
						<g:if test="${addressInstance?.prs?.isPublic}">
							<dt><g:message code="person.isPublic.label" default="IsPublic (derived from Prs)" /></dt>
							<dd>${addressInstance?.prs?.isPublic?.encodeAsHTML()}</dd>
						</g:if>
					</dl>
				</div>
				<g:form>
					<g:hiddenField name="id" value="${addressInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${addressInstance?.id}">
							<i class="write icon"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="trash icon"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div><!-- .twelve -->

			<div class="four wide column">
                <g:render template="../templates/sideMenu" />
			</div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
