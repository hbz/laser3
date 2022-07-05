<%@ page import="de.laser.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'task.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

		<semui:h1HeaderWithIcon message="default.edit.label" args="[entityName]" />

        <g:if test="${flash.message}">
			<semui:msg class="warning" text="${flash.message}" />
        </g:if>

        <semui:errors bean="${taskInstance}" />

        <div class="ui grid">

            <div class="twelve wide column">

					<g:form class="ui form" action="edit" id="${taskInstance?.id}" >
						<g:hiddenField name="version" value="${taskInstance?.version}" />
						<fieldset>
							<% /* f:all bean="taskInstance"/ */ %>

							<laser:render template="form" />

							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.update.label" />
								</button>
								<button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
									<i class="trash alternate outline icon"></i>
									<g:message code="default.button.delete.label" />
								</button>
							</div>
						</fieldset>
					</g:form>

			</div><!-- .twelve -->

            <aside class="four wide column">

                <semui:card text="${entityName}">
					<div class="content">
                    <ul class="nav nav-list">
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
                </semui:card>
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
