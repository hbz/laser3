<%@ page import="com.k_int.kbplus.CreatorTitle" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'creatorTitle.label', default: 'CreatorTitle')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
        <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

        <div class="ui grid">

            <div class="twelve wide column">

				<g:hasErrors bean="${creatorTitleInstance}">
				<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="${creatorTitleInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>

                <fieldset>
                    <g:form class="ui form form-horizontal" action="create" >
                        <g:render template="form"/>
                        <div class="ui form-actions">
                            <button type="submit" class="ui button">
                                <i class="checkmark icon"></i>
                                <g:message code="default.button.create.label" default="Create" />
                            </button>
                        </div>
                    </g:form>
                </fieldset>
				
			</div><!-- .twelve -->

            <aside class="four wide column">
                <g:render template="../templates/sideMenu" />
            </aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
