<%@ page import="de.laser.ui.Btn; de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'provider.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	<ui:breadcrumbs>
		<ui:crumb message="menu.public.all_providers" controller="provider" action="list"  />
		<ui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	</ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<ui:messages data="${flash}" />

		<ui:errors bean="${provider}" />

		<p>${message(code:'org.findProviderMatches.note')}</p>

		<ui:searchSegment controller="provider" action="findProviderMatches" method="get">
			<div class="field">
				<label for="proposedProvider">${message(code:'org.findProviderMatches.proposed')}</label>
				<input type="text" id="proposedProvider" name="proposedProvider" value="${params.proposedProvider}" />
			</div>
			<div class="field la-field-right-aligned">
				<a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.searchreset.label')}</a>
				<input type="submit" value="${message(code:'default.button.search.label')}" class="${Btn.PRIMARY}">
				<g:link controller="provider" action="list" class="${Btn.SIMPLE}">${message(code:'default.button.cancel.label')}</g:link>
			</div>
		</ui:searchSegment>

				<g:if test="${providerMatches != null}">
					<g:if test="${providerMatches.size()>0}">
						<table class="ui celled la-js-responsive-table la-table table">
							<thead>
							<tr>
								<th>${message(code:'default.name.label')}</th>
								<th>${message(code:'identifier.plural')}</th>
								<th>${message(code:'org.sortname.label')}</th>
								<th>${message(code:'altname.plural')}</th>
							</tr>
							</thead>
							<tbody>
							<g:each in="${providerMatches}" var="providerInstance">
								<tr>
									<td>${providerInstance.name} <g:link controller="provider" action="show" id="${providerInstance.id}">(${message(code:'default.button.edit.label')})</g:link></td>
									<td>
										<ul>
											<li><g:message code="globalUID.label" />: <g:fieldValue bean="${providerInstance}" field="globalUID"/></li>
											<g:if test="${providerInstance.gokbId}">
												<li><g:message code="org.wekbId.label" />: <g:fieldValue bean="${providerInstance}" field="gokbId"/></li>
											</g:if>
											<g:if test="${providerInstance.id}">
												<g:each in="${providerInstance.ids}" var="id"><li>${id.ns.ns}: ${id.value}</li></g:each>
											</g:if>
										</ul>
									</td>
									<td>${providerInstance.sortname}</td>
									<td>
										<ul>
											<g:each in="${providerInstance.altnames}" var="altname">
												<li>${altname.name}</li>
											</g:each>
										</ul>
									</td>
								</tr>
							</g:each>
							</tbody>
						</table>
						<ui:msg class="warning" message="org.findProviderMatches.match" args="[params.proposedProvider]" />
						<g:link controller="provider" action="createProvider" class="${Btn.NEGATIVE_SINGLECLICK}" params="${[provider:params.proposedProvider]}">${message(code:'org.findProviderMatches.matches.create', args: [params.proposedProvider])}</g:link>
					</g:if>
					<g:else>
						<ui:msg class="warning" message="org.findProviderMatches.no_match" args="[params.proposedProvider]" />
						<g:link controller="provider" action="createProvider" class="${Btn.POSITIVE_SINGLECLICK}" params="${[provider:params.proposedProvider]}">${message(code:'org.findProviderMatches.no_matches.create', args: [params.proposedProvider])}</g:link>
					</g:else>
				</g:if>

<laser:htmlEnd />
