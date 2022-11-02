<%@ page import="de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'org.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	    <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_orgs" controller="organisation" action="index" />
            <ui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	    </ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<ui:messages data="${flash}" />

		<ui:errors bean="${orgInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">
				<ui:greySegment>

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

				</ui:greySegment>
			</div><!-- .twelve -->

				<aside class="four wide column">
				</aside><!-- .four -->

			</div><!-- .grid -->

<laser:htmlEnd />
