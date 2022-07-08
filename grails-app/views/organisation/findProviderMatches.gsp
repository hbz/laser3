<%@ page import="de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'default.provider.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	<semui:breadcrumbs>
		<semui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"  />
		<semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	</semui:breadcrumbs>

		<semui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<semui:messages data="${flash}" />

		<semui:errors bean="${orgInstance}" />

		<p>${message(code:'org.findProviderMatches.note')}</p>

		<semui:searchSegment controller="organisation" action="findProviderMatches" method="get">
			<div class="field">
				<label for="proposedProvider">${message(code:'org.findProviderMatches.proposed')}</label>
				<input type="text" id="proposedProvider" name="proposedProvider" value="${params.proposedProvider}" />
			</div>
			<div class="field la-field-right-aligned">
				<a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.searchreset.label')}</a>
				<input type="submit" value="${message(code:'default.button.search.label')}" class="ui secondary button">
				<g:link controller="organisation" action="listProvider" class="ui button">${message(code:'default.button.cancel.label')}</g:link>
			</div>
		</semui:searchSegment>


				<g:if test="${providerMatches != null}">
					<g:if test="${providerMatches.size()>0}">
						<table class="ui celled la-js-responsive-table la-table table">
							<thead>
							<tr>
								<th>${message(code:'default.name.label')}</th>
								<th>${message(code:'identifier.plural')}</th>
								<th>${message(code:'org.shortname.label')}</th>
								<th>${message(code:'org.country.label')}</th>
							</tr>
							</thead>
							<tbody>
							<g:each in="${providerMatches}" var="providerInstance">
								<tr>
									<td>${providerInstance.name} <g:link controller="organisation" action="show" id="${providerInstance.id}">(${message(code:'default.button.edit.label')})</g:link></td>
									<td><ul>
											<li><g:message code="globalUID.label" />: <g:fieldValue bean="${providerInstance}" field="globalUID"/></li>
											<g:if test="${providerInstance.gokbId}">
												<li><g:message code="org.wekbId.label" />: <g:fieldValue bean="${providerInstance}" field="gokbId"/></li>
											</g:if>
											<g:each in="${providerInstance.ids?.sort{it?.ns?.ns}}" var="id"><li>${id.ns.ns}: ${id.value}</li></g:each>
									</ul></td>
									<td>${providerInstance.shortname}</td>
									<td>${providerInstance.country}</td>
								</tr>
							</g:each>
							</tbody>
						</table>
						<semui:msg class="warning" message="org.findProviderMatches.match" args="[params.proposedProvider]" />
						<g:link controller="organisation" action="createProvider" class="ui negative button" params="${[provider:params.proposedProvider]}">${message(code:'org.findProviderMatches.matches.create', args: [params.proposedProvider])}</g:link>
					</g:if>
					<g:else>
						<semui:msg class="warning" message="org.findProviderMatches.no_match" args="[params.proposedProvider]" />
						<g:link controller="organisation" action="createProvider" class="ui positive button" params="${[provider:params.proposedProvider]}">${message(code:'org.findProviderMatches.no_matches.create', args: [params.proposedProvider])}</g:link>
					</g:else>
				</g:if>


<laser:htmlEnd />
