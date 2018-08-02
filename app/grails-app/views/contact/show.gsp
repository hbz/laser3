
<%@ page import="com.k_int.kbplus.Contact" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'contact.label', default: 'Contact')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />


		<div class="ui grid">
			<div class="twelve wide column">

				<div class="inline-lists">
					<dl>
                        <dt><g:message code="contact.contentType.label" default="ContentType" /></dt>
                        <dd><semui:xEditableRefData owner="${contactInstance}" field="contentType" config="ContactContentType" /></dd>

                        <dt><g:message code="contact.content.label" default="Content" /></dt>
                        <dd>
							<semui:xEditable owner="${contactInstance}" field="content" id="js-mailContent"/>
							<g:if test="${contactInstance?.contentType?.value.equalsIgnoreCase('E-Mail')}">
								<a id="js-mailto" class="mini ui icon button">
									<i class="envelope outline icon"></i>
								</a>
								<r:script>
                                    $('#js-mailContent').editable().on('save', function(e, params){
                                        console.log(params)
                                        $('#js-mailto').attr(
                                            'href', "mailto:"  + params.newValue
                                        );
									});
								</r:script>
							</g:if>
						</dd>

                        <dt>${com.k_int.kbplus.RefdataCategory.findByDesc('ContactType').getI10n('desc')}</dt>
                        <dd><semui:xEditableRefData owner="${contactInstance}" field="type" config="ContactType" /></dd>

                        <g:if test="${contactInstance?.prs}">
                            <dt><g:message code="contact.prs.label" default="Prs" /></dt>
                            <dd><g:link controller="person" action="show" id="${contactInstance?.prs?.id}">${contactInstance?.prs?.encodeAsHTML()}</g:link></dd>
                        </g:if>

                        <g:if test="${contactInstance?.org}">
                            <dt><g:message code="contact.org.label" default="Org" /></dt>
                            <dd><g:link controller="organisations" action="show" id="${contactInstance?.org?.id}">${contactInstance?.org?.encodeAsHTML()}</g:link></dd>
                        </g:if>

						<g:if test="${contactInstance?.prs?.tenant}">
							<dt><g:message code="person.tenant.label" default="Tenant (derived from Prs)" /></dt>
							<dd><g:link controller="organisations" action="show" id="${contactInstance?.prs?.tenant?.id}">${contactInstance?.prs?.tenant?.encodeAsHTML()}</g:link></dd>
						</g:if>

						<g:if test="${contactInstance?.prs?.isPublic}">
							<dt><g:message code="person.isPublic.label" default="IsPublic (derived from Prs)" /></dt>
							<dd>${contactInstance?.prs?.isPublic?.encodeAsHTML()}</dd>
						</g:if>
					</dl>
				</div>
				<sec:ifAnyGranted roles="ROLE_YODA">
					<h3 class="ui header">ROLE_YODA</h3>
					<div class="inline-lists">
						<dl>
							<dt><g:message code="contact.prs.label" default="Prs" /></dt>
							<dd>TODO: SELECT PERSON</dd>


							<dt><g:message code="contact.org.label" default="Org" /></dt>
							<dd>TODO: SELECT ORG</dd>
						</dl>
					</div>
				</sec:ifAnyGranted>
				<g:if test="${editable}">
					<g:form>
						<g:hiddenField name="id" value="${contactInstance?.id}" />
						<div class="ui form-actions">
							<g:link class="ui button" action="edit" id="${contactInstance?.id}">
								<i class="write icon"></i>
								<g:message code="default.button.edit.label" default="Edit" />
							</g:link>
							<button class="ui negative button" type="submit" name="_action_delete">
								<i class="trash alternate icon"></i>
								<g:message code="default.button.delete.label" default="Delete" />
							</button>
						</div>
					</g:form>
				</g:if>

			</div><!-- .twelve -->

            <aside class="four wide column">
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
