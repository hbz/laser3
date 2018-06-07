<%@ page import="com.k_int.kbplus.Numbers" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'numbers.label', default: 'Numbers')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
        <h1 class="ui header"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

        <div class="ui grid">

            <div class="twelve wide column">

				<g:hasErrors bean="${numbersInstance}">
				<bootstrap:alert class="alert-error">
				<ul>
					<g:eachError bean="${numbersInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>

                <fieldset>
                    <g:form class="ui form form-horizontal" action="create" >
                        <f:all bean="numbersInstance"/>
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
