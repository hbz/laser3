<%=packageName%>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.edit.label" args="[entityName]" /></h1>

        <semui:messages data="\${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<g:hasErrors bean="\${${propertyName}}">
				<semui:msg class="negative">
					<ul>
						<g:eachError bean="\${${propertyName}}" var="error">
						<li <g:if test="\${error in org.springframework.validation.FieldError}">data-field-id="\${error.field}"</g:if>><g:message error="\${error}"/></li>
						</g:eachError>
					</ul>
				</semui:msg>
				</g:hasErrors>

				<fieldset>
					<g:form class="ui form" action="edit" id="\${${propertyName}?.id}" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
						<g:hiddenField name="version" value="\${${propertyName}?.version}" />

                        <f:all bean="${propertyName}"/>
                        <div class="ui form-actions">
                            <button type="submit" class="ui button">
                                <i aria-hidden="true" class="checkmark icon"></i>
                                <g:message code="default.button.update.label" />
                            </button>
                            <button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
                                <i aria-hidden="true" class="trash icon"></i>
                                <g:message code="default.button.delete.label" />
                            </button>
                        </div>
					</g:form>
				</fieldset>

			</div><!-- .twelve -->

            <aside class="four wide column">
                <g:render template="/templates/sideMenu" />
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
