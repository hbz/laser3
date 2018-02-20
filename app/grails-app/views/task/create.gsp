<%@ page import="com.k_int.kbplus.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>

		<div class="ui grid">

            <div class="four wide column">
                <div class="well">
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
            </div><!-- .four -->

			<div class="twelve wide column">

				<div class="page-header">
					<h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>
				</div>

				<g:if test="${flash.message}">
				    <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>

				<semui:errors bean="${taskInstance}" />

				<fieldset>
					<g:form class="ui form" action="create" >
						<fieldset>
							<% /* f:all bean="taskInstance"/*/ %>

                            <g:render template="form" />

							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label" default="Create" />
								</button>
							</div>
						</fieldset>
					</g:form>
				</fieldset>
				
			</div><!-- .twelve -->

		</div><!-- .grid -->

	</body>
</html>
