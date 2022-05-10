
<%@ page import="de.laser.RefdataCategory; de.laser.Contact; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'contact.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
        <semui:breadcrumbs>
            <semui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>
            <semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
        </semui:breadcrumbs>

		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <button class="ui button" onclick="window.location.href = document.referrer">
            Zurück
        </button>

		<semui:messages data="${flash}" />

		<div class="ui grid">
			<div class="twelve wide column">

				<div class="inline-lists">
					<dl>
                        <dt><g:message code="contact.contentType.label" /></dt>
                        <dd><semui:xEditableRefData owner="${contactInstance}" field="contentType" config="${RDConstants.CONTACT_CONTENT_TYPE}" /></dd>

                        <dt><g:message code="default.content.label" /></dt>
                        <dd>
							<semui:xEditable owner="${contactInstance}" field="content" id="js-mailContent"/>
						</dd>

                        <dt>${RefdataCategory.getByDesc(RDConstants.CONTACT_TYPE).getI10n('desc')}</dt>
                        <dd><semui:xEditableRefData owner="${contactInstance}" field="type" config="${RDConstants.CONTACT_TYPE}" /></dd>

                        <g:if test="${contactInstance?.prs}">
                            <dt><g:message code="contact.prs.label" /></dt>
                            <dd><g:link controller="person" action="show" id="${contactInstance?.prs?.id}">${contactInstance?.prs}</g:link></dd>
                        </g:if>

                        <g:if test="${contactInstance?.org}">
                            <dt><g:message code="contact.org.label" /></dt>
                            <dd><g:link controller="organisation" action="show" id="${contactInstance?.org?.id}">${contactInstance?.org}</g:link></dd>
                        </g:if>

						<g:if test="${contactInstance?.prs?.tenant}">
							<dt><g:message code="person.tenant.label" /></dt>
							<dd><g:link controller="organisation" action="show" id="${contactInstance?.prs?.tenant?.id}">${contactInstance?.prs?.tenant}</g:link></dd>
						</g:if>

						<g:if test="${contactInstance?.prs?.isPublic}">
							<dt><g:message code="person.isPublic.label" /></dt>
							<dd>${RDStore.YN_YES.getI10n('value')}</dd>
						</g:if>
					</dl>

				</div>
			</div><!-- .twelve -->

            <aside class="four wide column">
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
