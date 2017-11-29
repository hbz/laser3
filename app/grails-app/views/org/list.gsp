
<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<h2 class="ui header">DEPRECATED</h2>
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
						<sec:ifAnyGranted roles="ROLE_ADMIN">
						<li>
							<g:link class="create" action="create">
								<i class="icon-plus"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
						 </sec:ifAnyGranted>
					</ul>
				</div>
			</div>

			<div class="span9">
				

					<h1 class="ui header">Organisations</h1>


                <div class="well">
			          <g:form action="list" method="get" class="form-inline">
			            Org Name Contains: 
			            <input type="text" name="orgNameContains" value="${params.orgNameContains}"/> 
			            Restrict to orgs who are 
			            <g:select name="orgRole" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}" from="${RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Organisational Role'))}" value="${params.orgRole}" optionKey="id" optionValue="value"/>
			            <input type="submit" value="GO" class="ui primary button"/> (${orgInstanceTotal} Matches)
			          </g:form>
                </div>

				<semui:messages data="${flash}" />
				
				<table class="ui celled striped table">
					<thead>
						<tr>
							<g:sortableColumn property="impId" title="${message(code: 'org.impId.label', default: 'Imp Id')}" />
							<g:sortableColumn property="comment" title="${message(code: 'org.comment.label', default: 'Comment')}" />					
							<g:sortableColumn property="ipRange" title="${message(code: 'org.ipRange.label', default: 'Ip Range')}" />
							<g:sortableColumn property="type" title="${message(code: 'org.type.label', default: 'Type')}" />
							<g:sortableColumn property="sector" title="${message(code: 'org.sector.label', default: 'Sector')}" />
							<g:sortableColumn property="shortcode" title="${message(code: 'org.shortcode.label', default: 'Shortcode')}" />
							<g:sortableColumn property="scope" title="${message(code: 'org.scope.label', default: 'Scope')}" />
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${orgInstanceList}" var="orgInstance">
						<tr>
							<td>${fieldValue(bean: orgInstance, field: "impId")}</td>
							<td>${fieldValue(bean: orgInstance, field: "comment")}</td>	
							<td>${fieldValue(bean: orgInstance, field: "ipRange")}</td>
							<td>${orgInstance?.orgType?.value}</td>
							<td>${orgInstance?.sector?.value}</td>
							<td>${fieldValue(bean: orgInstance, field: "shortcode")}</td>						
							<td>${fieldValue(bean: orgInstance, field: "scope")}</td>						
							<td class="link">
								<g:link action="show" id="${orgInstance.id}" class="ui tiny button">Show</g:link>
								<g:link action="edit" id="${orgInstance.id}" class="ui tiny button">Edit</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${orgInstanceTotal}" />
				</div>
			</div>

		</div>
	</body>
</html>
