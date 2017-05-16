
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
						
							<dd><ul>
								<g:each in="${personInstance.contacts}" var="c">
									<li>
										<g:render template="/templates/cpa/contact" model="${[contact: c]}"></g:render>
									</li>
								</g:each>
							</ul></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.addresses}">
						<dt><g:message code="person.addresses.label" default="Addresses" /></dt>
						
							<dd><ul>
								<g:each in="${personInstance.addresses}" var="a">
									<li>
										<g:render template="/templates/cpa/address" model="${[address: a]}"></g:render>
									</li>
								</g:each>
							</ul></dd>
						
					</g:if>
				</dl>
				<dl class="debug-only">
					<g:if test="${personInstance?.tenant}">
						<dt><g:message code="person.tenant.label" default="Tenant" /></dt>
						<dd><g:link controller="organisations" action="show" id="${personInstance.tenant?.id}">${personInstance.tenant?.encodeAsHTML()}</g:link></dd>	
					</g:if>
				</dl>
				<dl class="debug-only">
					<g:if test="${personInstance?.isPublic}">
						<dt><g:message code="person.isPublic.label" default="IsPublic" /></dt>
						<dd><g:fieldValue bean="${personInstance}" field="isPublic"/></dd>	
					</g:if>
				</dl>
				<dl>
					<g:if test="${personInstance?.roleLinks}">
						<dt><g:message code="person.functions.label" default="Functions" /></dt>
						
							<dd><ul>
								<g:each in="${personInstance.roleLinks}" var="link">
									<g:if test="${link.functionType}">
										<li>
											${link.functionType?.value}
											<br/>
		
			                                <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
			                                (Organisation) 
			                            </li>
		                            </g:if>
								</g:each>
							</ul></dd>
				
					</g:if>
				
				</dl>
				<dl>
					<g:if test="${personInstance?.roleLinks}">
						<dt><g:message code="person.responsibilites.label" default="Responsibilites" /></dt>
						
							<dd><ul>
								<g:each in="${personInstance.roleLinks}" var="link">
									<g:if test="${link.responsibilityType}">
										<li>
											${link.responsibilityType?.value}<br/>
										 	
										 	<g:if test="${link.pkg}">
										 		<g:link controller="package" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
										 		(Package) <br />
										 	</g:if>
			                                <g:if test="${link.cluster}">
			                                	<g:link controller="cluster" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
			                                	(Cluster) <br />
			                                </g:if>
			                                <g:if test="${link.sub}">
			                                	<g:link controller="subscription" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
			                                	(Subscription) <br />
			                                </g:if>
			                                <g:if test="${link.lic}">
			                                	${link.lic}
			                                	(License) <br />
			                                </g:if>
			                                <g:if test="${link.title}">
			                                	<g:link controller="titleInstance" action="show" id="${link.title.id}">${link.title.title}</g:link>
			                                	(Title) <br />
			                                </g:if>
		
			                                <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
			                                (Organisation) 
			                            </li>
			                    	</g:if>
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
