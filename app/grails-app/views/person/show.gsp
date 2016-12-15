
<%@ page import="com.k_int.kbplus.Person" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
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
				
					<g:if test="${personInstance?.first_name}">
						<dt><g:message code="person.first_name.label" default="Firstname" /></dt>
						
							<dd><g:fieldValue bean="${personInstance}" field="first_name"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.middle_name}">
						<dt><g:message code="person.middle_name.label" default="Middlename" /></dt>
						
							<dd><g:fieldValue bean="${personInstance}" field="middle_name"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.last_name}">
						<dt><g:message code="person.last_name.label" default="Lastname" /></dt>
						
							<dd><g:fieldValue bean="${personInstance}" field="last_name"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.gender}">
						<dt><g:message code="person.gender.label" default="Gender" /></dt>
						
							<dd><g:link controller="refdataValue" action="show" id="${personInstance?.gender?.id}">${personInstance?.gender?.encodeAsHTML()}</g:link></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.contacts}">
						<dt><g:message code="person.contacts.label" default="Contacts" /></dt>
						
							<g:each in="${personInstance.contacts}" var="c">
							<dd><g:link controller="contact" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></dd>
							</g:each>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.links}">
						<dt><g:message code="person.links.label" default="Links" /></dt>
						
							<dd><ul>
								<g:each in="${personInstance.links}" var="l">
								<li>
									<g:link controller="org" action="show" id="${l.org?.id}">Org: ${l.org?.name}</g:link> /
								 	
								 	<g:if test="${l.pkg}">
								 		<g:link controller="package" action="show" id="${l.pkg.id}">Package: ${l.pkg.name}</g:link>
								 	</g:if>
	                                <g:if test="${l.cluster}">
	                                	<g:link controller="cluster" action="show" id="${l.cluster.id}">Cluster: ${l.cluster.name}</g:link>
	                                </g:if>
	                                <g:if test="${l.sub}">
	                                	<g:link controller="subscription" action="show" id="${l.sub.id}">Subscription: ${l.sub.name}</g:link>
	                                </g:if>
	                                <g:if test="${l.lic}">Licence: ${l.lic.id}</g:if>
	                                <g:if test="${l.title}">
	                                	<g:link controller="titleInstance" action="show" id="${l.title.id}">Title: ${l.title.title}</g:link>
	                                </g:if>
	                                  / ${l.roleType?.value}
	                            </li>
								</g:each>
							</ul></dd>
				
					</g:if>
				
				</dl>
</div>
				<g:form>
					<g:hiddenField name="id" value="${personInstance?.id}" />
					<div class="form-actions">
						<g:link class="btn" action="edit" id="${personInstance?.id}">
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
	</body>
</html>
