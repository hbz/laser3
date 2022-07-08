<%@ page import="de.laser.Person" %>

<g:set var="entityName" value="${message(code: 'person.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

		<semui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<semui:messages data="${flash}" />

		<semui:errors bean="${personInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

					<g:form class="ui form" action="create" >
						<fieldset>
							<laser:render template="form"/>
							
							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label"/>
								</button>
							</div>
						</fieldset>
					</g:form>
				
			</div><!-- .twelve -->

			<aside class="four wide column">
			</aside><!-- .four -->

		</div><!-- .grid -->

<laser:htmlEnd />
