
<%@ page import="de.laser.Platform" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<title>${message(code: 'laser')} : ${message(code: 'platforms.all_platforms.label')}</title>
	</head>
	<body>
	<semui:breadcrumbs>
		<semui:crumb message="platforms.all_platforms.label" class="active" />
	</semui:breadcrumbs>

	<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon /><g:message code="platforms.all_platforms.label" />
		<semui:totalNumber total="${platformInstanceTotal}"/>
	</h1>

			<semui:messages data="${flash}" />

			<g:render template="/templates/filter/javascript" />
			<semui:filter showFilterButton="true">
				<g:form action="list" method="get" class="ui form">
                    <div class="two fields">
                        <div class="field">
                            <label>${message(code:'default.search.text')}</label>
                            <input type="text" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
                        </div>
						<div class="field la-field-right-aligned">
							<a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
                        </div>
                    </div>
				</g:form>
			</semui:filter>

			<table class="ui sortable celled la-table table">
				<thead>
				<tr>
					<th>${message(code:'sidewide.number')}</th>
					<g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
					<th>${message(code:'default.provider.label')}</th>
					<th>${message(code:'org.url.label')}</th>
				</tr>
				</thead>
				<tbody>
				<g:each in="${platformInstanceList}" var="platformInstance" status="jj">
					<tr>
						<td>
							${ (params.int('offset') ?: 0)  + jj + 1 }
						</td>
						<th scope="row" class="la-th-column">
							<g:link controller="platform" action="show" class="la-main-object"  id="${platformInstance.id}">${fieldValue(bean: platformInstance, field: "name")}</g:link>
						</th>
						<td>
							<g:if test="${platformInstance.org}">
								<g:link controller="organisation" action="show" id="${platformInstance.org?.id}">${platformInstance.org?.getDesignation()}</g:link>
							</g:if>
						</td>
						<td>
							<g:if test="${platformInstance.primaryUrl}">
								${platformInstance.primaryUrl}
								<a href="<g:createLink url="${platformInstance.primaryUrl}" />" target="_blank"><i class="external alternate icon"></i></a>
							</g:if>
						</td>
					</tr>
				</g:each>
				</tbody>
			</table>

    <semui:paginate  action="list" controller="platform" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${platformInstanceTotal}" />


	</body>
</html>
