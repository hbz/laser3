<%=packageName%>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
        <h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>

        <semui:messages data="\${flash}" />

        <div class="ui grid">

            <div class="twelve wide column">

				<g:hasErrors bean="\${${propertyName}}">
				<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="\${${propertyName}}" var="error">
					<li <g:if test="\${error in org.springframework.validation.FieldError}">data-field-id="\${error.field}"</g:if>><g:message error="\${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>

                <fieldset>
                    <g:form class="ui form form-horizontal" action="create" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
                        <f:all bean="${propertyName}"/>
                        <div class="ui segment form-actions">
                            <button type="submit" class="ui primary button">
                                <i class="icon-ok icon-white"></i>
                                <g:message code="default.button.create.label" default="Create" />
                            </button>
                        </div>
                    </g:form>
                </fieldset>
				
			</div><!-- .twelve -->

            <div class="four wide column">
                <g:render template="../templates/sideMenu" />
            </div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
