<%@ page import="de.laser.Contact" %>

<g:set var="entityName" value="${message(code: 'contact.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

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

				<semui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

				<semui:messages data="${flash}" />

				<semui:errors bean="${contactInstance}" />

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
<laser:htmlEnd />
