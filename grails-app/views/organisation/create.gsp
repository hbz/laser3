<%@ page import="de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'org.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	    <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_orgs" controller="organisation" action="index" />
            <semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	    </semui:breadcrumbs>

		<semui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<semui:messages data="${flash}" />

		<semui:errors bean="${orgInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">
				<div class="ui grey segment la-clear-before">

					<g:form class="ui form" action="create" >
						<fieldset>
                            <laser:render template="form"/>

							<div class="ui form-actions">
								<button type="submit" class="ui button">
									<i class="checkmark icon"></i>
									<g:message code="default.button.create.label"/>
								</button>
								<input type="button" class="ui button js-click-control" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
							</div>
						</fieldset>
					</g:form>

				</div>
			</div><!-- .twelve -->

				<aside class="four wide column">
				</aside><!-- .four -->

			</div><!-- .grid -->

<laser:htmlEnd />
