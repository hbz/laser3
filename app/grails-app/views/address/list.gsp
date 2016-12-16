
<%@ page import="com.k_int.kbplus.Address" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<g:set var="entityName" value="${message(code: 'address.label', default: 'Address')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="row-fluid">
			
			<div class="span3">
				<div class="well">
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li class="active">
							<g:link class="list" action="list">
								<i class="icon-list icon-white"></i>
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
					<h1><g:message code="default.list.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
				
				<table class="table table-bordered table-striped">
					<thead>
						<tr>
						
							<g:sortableColumn property="street_1" title="${message(code: 'address.street_1.label', default: 'Street1')}" />
						
							<g:sortableColumn property="street_2" title="${message(code: 'address.street_2.label', default: 'Street2')}" />
						
							<g:sortableColumn property="pob" title="${message(code: 'address.pob.label', default: 'Pob')}" />
						
							<g:sortableColumn property="zipcode" title="${message(code: 'address.zipcode.label', default: 'Zipcode')}" />
						
							<g:sortableColumn property="city" title="${message(code: 'address.city.label', default: 'City')}" />
						
							<g:sortableColumn property="state" title="${message(code: 'address.state.label', default: 'State')}" />
						
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${addressInstanceList}" var="addressInstance">
						<tr>
						
							<td>${fieldValue(bean: addressInstance, field: "street_1")}</td>
						
							<td>${fieldValue(bean: addressInstance, field: "street_2")}</td>
						
							<td>${fieldValue(bean: addressInstance, field: "pob")}</td>
						
							<td>${fieldValue(bean: addressInstance, field: "zipcode")}</td>
						
							<td>${fieldValue(bean: addressInstance, field: "city")}</td>
						
							<td>${fieldValue(bean: addressInstance, field: "state")}</td>
						
							<td class="link">
								<g:link action="show" id="${addressInstance.id}" class="btn btn-small">Show &raquo;</g:link>
								<g:link action="edit" id="${addressInstance.id}" class="btn btn-small">Edit</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${addressInstanceTotal}" />
				</div>
			</div>

		</div>
	</body>
</html>
