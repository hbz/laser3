<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'org.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	    <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_orgs" controller="organisation" action="index" />
            <ui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	    </ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" type="institution"/>

		<ui:messages data="${flash}" />

		<ui:errors bean="${orgInstance}" />

		<div class="ui grid">

			<div class="twelve wide column">

					<ui:form controller="org" action="create">
						<fieldset>
                            <laser:render template="form"/>

							<div class="ui form-actions">
								<button type="submit" class="${Btn.SIMPLE}">
									<i class="${Icon.SYM.YES}"></i>
									<g:message code="default.button.create.label"/>
								</button>
								<input type="button" class="${Btn.SIMPLE_CLICKCONTROL}" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
							</div>
						</fieldset>
					</ui:form>
			</div><!-- .twelve -->

				<aside class="four wide column">
				</aside><!-- .four -->

			</div><!-- .grid -->

<laser:htmlEnd />
