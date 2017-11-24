
<%@ page import="com.k_int.kbplus.Cluster" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'cluster.label', default: 'Cluster')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><g:message code="default.show.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<div class="ui grid">
			
			<div class="twelve wide column">

				<div class="inline-lists">
				<dl>
				
					<g:if test="${clusterInstance?.definition}">
						<dt><g:message code="cluster.definition.label" default="Definition" /></dt>
						
							<dd><g:fieldValue bean="${clusterInstance}" field="definition"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${clusterInstance?.name}">
						<dt><g:message code="cluster.name.label" default="Name" /></dt>
						
							<dd><g:fieldValue bean="${clusterInstance}" field="name"/></dd>
						
					</g:if>
				</dl>
				<dl>
					<g:if test="${clusterInstance?.orgs}">
						<dt><g:message code="cluster.orgs.label" default="Orgs" /></dt>
						<dd><ul>
							<g:each in="${clusterInstance.orgs}" var="o">
							<li>${o?.roleType?.value} - <g:link controller="org" action="show" id="${o?.org?.id}">${o?.org?.name}</g:link></li>
							</g:each>
						</ul></dd>
					</g:if>
				</dl>
				<dl>
					<g:if test="${clusterInstance?.type}">
						<dt><g:message code="cluster.type.label" default="Type" /></dt>
						
							<dd>${clusterInstance?.type?.encodeAsHTML()}</dd>
						
					</g:if>
				
				</dl>
				</div>
				<g:form>
					<g:hiddenField name="id" value="${clusterInstance?.id}" />
					<div class="ui segment form-actions">
						<g:link class="ui button" action="edit" id="${clusterInstance?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
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
