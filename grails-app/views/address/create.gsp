<%@ page import="de.laser.Address" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'address.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
		<div>
			
			<div class="span3">
				<div>
					<ul class="nav nav-list">
						<li class="nav-header">${entityName}</li>
						<li>
							<g:link class="list" action="list">
								<i class="icon-list"></i>
								<g:message code="default.list.label" args="[entityName]" />
							</g:link>
						</li>
						<li class="active">
							<g:link class="create" action="create">
								<i class="icon-plus icon-white"></i>
								<g:message code="default.create.label" args="[entityName]" />
							</g:link>
						</li>
					</ul>
				</div>
			</div>
			
			<div class="span9">

				<semui:headerWithIcon message="default.create.label" args="[entityName]" />

				<semui:messages data="${flash}" />

				<semui:errors bean="${addressInstance}" />

					<g:form class="ui form" action="create" >
						<fieldset>
							<laser:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label" />
								</button>
							</div>
						</fieldset>
					</g:form>
				
			</div>

		</div>
		</div>
	</body>
</html>
