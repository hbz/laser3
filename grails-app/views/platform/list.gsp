<%@ page import="de.laser.Platform; de.laser.Package; de.laser.Org; de.laser.storage.RDStore" %>
<laser:htmlStart message="platforms.all_platforms.label" />

	<ui:breadcrumbs>
		<ui:crumb message="platforms.all_platforms.label" class="active" />
	</ui:breadcrumbs>

	<ui:h1HeaderWithIcon message="platforms.all_platforms.label" total="${recordsCount}" floated="true" />

			<ui:messages data="${flash}" />

			<laser:render template="/templates/filter/platformFilter"/>

			<g:if test="${records}">
				<table class="ui sortable celled la-js-responsive-table la-table table">
					<thead>
					<tr>
						<th>${message(code:'sidewide.number')}</th>
						<g:sortableColumn property="sortname" title="${message(code: 'default.name.label')}" />
						<g:sortableColumn property="providerName" title="${message(code:'default.provider.label')}" />
						<th>${message(code:'default.url.label')}</th> <%-- needs we:kb rework to make the property sortable --%>
						<th class="center aligned">
							<span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.platforms')}">
								<i class="icon star"></i>
							</span>
						</th>
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
												  data-content="${message(code: 'org.isWekbCurated.header.label')}">
												<i class="la-gokb icon la-list-icon"></i>
											</span>
										</g:if>
										<g:link controller="organisation" action="show" id="${platformInstance.org.id}">${platformInstance.org.getDesignation()}</g:link>
									</div>
								</g:if>
								<g:elseif test="${record.providerUuid}">
									<span class="la-long-tooltip la-popup-tooltip la-delay"
										  data-content="${message(code: 'org.isWekbCurated.header.label')}">
										<i class="la-gokb icon la-list-icon"></i>
									</span>
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
							<td class="center aligned">
								<g:if test="${platformInstance && myPlatformIds.contains(platformInstance.id)}">
									<span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.platforms')}">
										<i class="icon yellow star"></i>
									</span>
								</g:if>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
			</g:if>
			<g:else>
				<g:if test="${filterSet}">
					<br/><strong><g:message code="filter.result.empty.object"
											args="${[message(code: "platform.plural")]}"/></strong>
				</g:if>
				<g:elseif test="${!error}">
					<br/><strong><g:message code="result.empty.object"
											args="${[message(code: "platform.plural")]}"/></strong>
				</g:elseif>
			</g:else>

    <ui:paginate action="list" controller="platform" params="${params}" max="${max}" total="${recordsCount}" />

<laser:htmlEnd />
