<%@ page import="de.laser.Platform; de.laser.Package; de.laser.Org; de.laser.helper.RDStore" %>
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
		<semui:totalNumber total="${recordsCount}"/>
	</h1>

			<semui:messages data="${flash}" />

			<g:render template="/templates/filter/platformFilter"/>

			<table class="ui sortable celled la-js-responsive-table la-table table">
				<thead>
				<tr>
					<th>${message(code:'sidewide.number')}</th>
					<g:sortableColumn property="sortname" title="${message(code: 'default.name.label')}" />
					<g:sortableColumn property="providerName" title="${message(code:'default.provider.label')}" />
					<th>${message(code:'org.url.label')}</th> <%-- needs we:kb rework to make the property sortable --%>
				</tr>
				</thead>
				<tbody>
				<g:each in="${records}" var="record" status="jj">
					<tr>
						<g:set var="pkg" value="${Package.findByGokbId(record.uuid)}"/>
						<g:set var="org" value="${Org.findByGokbId(record.providerUuid)}"/>
						<g:set var="platformInstance" value="${Platform.findByGokbId(record.uuid)}"/>
						<td>
							${ (params.int('offset') ?: 0)  + jj + 1 }
						</td>
						<th scope="row" class="la-th-column">
							<g:if test="${platformInstance}">
								<g:link controller="platform" action="show" class="la-main-object"  id="${platformInstance.id}">${platformInstance.name}</g:link>
							</g:if>
							<g:else>
								${record.name}
								<a target="_blank" href="${editUrl ? editUrl + '/public/platformContent?id=' + record.uuid : '#'}">
									<i title="we:kb Link" class="external alternate icon"></i>
								</a>
							</g:else>
						</th>
						<td>
							<g:if test="${platformInstance && platformInstance.org}">
								<div class="la-flexbox">
									<g:if test="${platformInstance.org.gokbId != null && RDStore.OT_PROVIDER.id in platformInstance.org.getAllOrgTypeIds()}">
										<span class="la-long-tooltip la-popup-tooltip la-delay"
											  data-content="${RDStore.OT_PROVIDER.getI10n("value")}">
											<i class="handshake outline grey icon la-list-icon"></i>
										</span>
									</g:if>
									<g:link controller="organisation" action="show" id="${platformInstance.org.id}">${platformInstance.org.getDesignation()}</g:link>
								</div>
							</g:if>
							<g:elseif test="${record.providerUuid}">
								${record.providerName}
								<a target="_blank" href="${editUrl ? editUrl + '/public/orgContent?id=' + record.providerUuid : '#'}">
									<i title="we:kb Link" class="external alternate icon"></i>
								</a>
							</g:elseif>
						</td>
						<td>
							<g:if test="${platformInstance && platformInstance.primaryUrl}">
								<g:set var="primaryUrl" value="${platformInstance.primaryUrl}"/>
							</g:if>
							<g:elseif test="${record.primaryUrl}">
								<g:set var="primaryUrl" value="${record.primaryUrl}"/>
							</g:elseif>
							<g:if test="${primaryUrl}">
								${primaryUrl}<a href="<g:createLink url="${primaryUrl}" />" target="_blank"> <i class="external alternate icon"></i></a>
							</g:if>
						</td>
					</tr>
				</g:each>
				</tbody>
			</table>

    <semui:paginate  action="list" controller="platform" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${recordsCount}" />


	</body>
</html>
