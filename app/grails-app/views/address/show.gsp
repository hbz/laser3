
<%@ page import="com.k_int.kbplus.Address" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'address.label', default: 'Address')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">
		<div class="row-fluid">
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li>
							<g:link class="list" action="list">
								<i class="icon-list"></i>
								<g:message code="default.list.label" args="[entityName]" />
							</g:link>
						</li>
						<li>
							<g:link class="create" action="create">
								<i class="icon-plus"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div>
			
			<div class="span9">

				<div class="page-header">
					<h1><g:message code="default.show.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>


<div class="inline-lists">

				<dl>
				
					<g:if test="${addressInstance?.street_1}">
						<dt><g:message code="address.street_1.label" default="Street1" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="street_1"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.street_2}">
						<dt><g:message code="address.street_2.label" default="Street2" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="street_2"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.pob}">
						<dt><g:message code="address.pob.label" default="Pob" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="pob"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.zipcode}">
						<dt><g:message code="address.zipcode.label" default="Zipcode" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="zipcode"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.city}">
						<dt><g:message code="address.city.label" default="City" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="city"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.state}">
						<dt><g:message code="address.state.label" default="State" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="state"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.country}">
						<dt><g:message code="address.country.label" default="Country" /></dt>
						
							<dd><g:fieldValue bean="${addressInstance}" field="country"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${addressInstance?.type}">
						<dt><g:message code="address.type.label" default="Type" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${addressInstance?.type?.id}">${addressInstance?.type?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
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
						
							<dd><g:link controller="org" action="show" id="${addressInstance?.org?.id}">${addressInstance?.org?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				
				</dl>

				<dl class="debug-only">
					<g:if test="${addressInstance?.prs?.tenant}">
						<dt><g:message code="person.tenant.label" default="Tenant (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${addressInstance?.prs?.tenant?.id}">${addressInstance?.prs?.tenant?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
				<dl class="debug-only">
					<g:if test="${addressInstance?.prs?.isPublic}">
						<dt><g:message code="person.tenant.label" default="IsPublic (derived from Prs)" /></dt>
						<dd><g:link controller="org" action="show" id="${addressInstance?.prs?.isPublic?.id}">${addressInstance?.prs?.isPublic?.encodeAsHTML()}</g:link></dd>
					</g:if>
				</dl>
</div>
				<g:form>
					<g:hiddenField name="id" value="${addressInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${addressInstance?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="btn btn-danger" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div>

		</div>
		</div>
	</body>
</html>
