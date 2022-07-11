<%@ page import="de.laser.Task" %>

<g:set var="entityName" value="${message(code: 'task.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

		<div class="ui grid">

            <div class="four wide column">
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
            </div><!-- .four -->

			<div class="twelve wide column">

				<div class="page-header">
					<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />
				</div>

				<g:if test="${flash.message}">
					<ui:msg class="warning" text="${flash.message}" />
				</g:if>

				<ui:errors bean="${taskInstance}" />

					<g:form class="ui form" action="create" >
						<fieldset>
							<% /* f:all bean="taskInstance"/*/ %>

                            <laser:render template="form" />

							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label"/>
								</button>
							</div>
						</fieldset>
					</g:form>
				
			</div><!-- .twelve -->

		</div><!-- .grid -->

<laser:htmlEnd />
