
<%@ page import="de.laser.Doc" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'doc.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>

		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

			<div class="ui grid">
				<div class="twelve wide column">

                    <div class="inline-lists">

						<dl>
				
                        <g:if test="${docInstance?.status}">
                            <dt><g:message code="default.status.label" /></dt>

                                <dd><g:link controller="refdataValue" action="show" id="${docInstance?.status?.id}">${docInstance?.status}</g:link></dd>

                        </g:if>

                        <g:if test="${docInstance?.type}">
                            <dt><g:message code="default.type.label" /></dt>

                                <dd><g:link controller="refdataValue" action="show" id="${docInstance?.type?.id}">${docInstance?.type}</g:link></dd>

                        </g:if>

                        <g:if test="${docInstance?.content}">
                            <dt><g:message code="default.content.label" /></dt>

                                <dd><g:fieldValue bean="${docInstance}" field="content"/></dd>

                        </g:if>

                        <g:if test="${docInstance?.uuid}">
                            <dt><g:message code="doc.uuid.label" default="Uuid" /></dt>

                                <dd><g:fieldValue bean="${docInstance}" field="uuid"/></dd>

                        </g:if>

                        <g:if test="${docInstance?.contentType}">
                            <dt><g:message code="doc.contentType.label" default="Content Type" /></dt>

                                <dd><g:fieldValue bean="${docInstance}" field="contentType"/></dd>

                        </g:if>

                        <g:if test="${docInstance?.title}">
                            <dt><g:message code="default.title.label" /></dt>

                                <dd><g:fieldValue bean="${docInstance}" field="title"/></dd>

                        </g:if>

                        <g:if test="${docInstance?.filename}">
                            <dt><g:message code="default.fileName.label" /></dt>

                                <dd><g:fieldValue bean="${docInstance}" field="filename"/></dd>

                        </g:if>

                        <g:if test="${docInstance?.dateCreated}">
                            <dt><g:message code="default.dateCreated.label" /></dt>

                                <dd><g:formatDate date="${docInstance?.dateCreated}" /></dd>

                        </g:if>

                        <g:if test="${docInstance?.lastUpdated}">
                            <dt><g:message code="default.lastUpdated.label" /></dt>

                                <dd><g:formatDate date="${docInstance?.lastUpdated}" /></dd>

                        </g:if>

                    </dl>
                    </div>

                    <g:if test="${editable}">
                        <g:form>
                            <g:hiddenField id="doc_id_${docInstance?.id}" name="id" value="${docInstance?.id}" />
                            <div class="ui form-actions">
                                <g:link class="ui button blue la-modern-button" action="show" id="${docInstance?.id}">
                                    <i aria-hidden="true" class="write icon"></i>
                                    <g:message code="default.button.edit.label" />
                                </g:link>
                                <button class="ui negative button la-modern-button" type="submit" name="_action_delete">
                                    <i class="trash alternate outline icon"></i>
                                    <g:message code="default.button.delete.label" />
                                </button>
                            </div>
                        </g:form>
                    </g:if>

		        </div>
		    </div>
	</body>
</html>
