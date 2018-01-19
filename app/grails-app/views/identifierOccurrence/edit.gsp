<%@ page import="com.k_int.kbplus.IdentifierOccurrence" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
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


					<h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>


					<semui:messages data="${flash}" />

					<semui:errors bean="${identifierOccurrenceInstance}" />

				<p><b> Action disabled. Please contact system administrator if you require access. </b></p>
				%{-- The following code is causing the system to hang and crash, while failing to find the required fields. Something that was tested to work is defining a template within views/identifierOccurance path, like edit/_widget.gsp --}%

%{-- 				<fieldset>
					<g:form class="ui form" action="edit" id="${identifierOccurrenceInstance?.id}" >
						<g:hiddenField name="version" value="${identifierOccurrenceInstance?.version}" />
						<fieldset>
							<f:all bean="identifierOccurrenceInstance"/>
							<div class="ui segment form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.update.label" default="Update" />
								</button>
								<button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
									<i class="trash icon"></i>
									<g:message code="default.button.delete.label" default="Delete" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset> --}%

			</div>

		</div>
	</body>
</html>
