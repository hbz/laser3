<%@ page import="de.laser.ui.Btn; de.laser.Org" %>

<g:set var="entityName" value="${message(code: 'vendor.label')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	<ui:breadcrumbs>
		<ui:crumb message="menu.public.all_vendors" controller="vendor" action="list"  />
		<ui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	</ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" />

		<ui:messages data="${flash}" />

		<ui:errors bean="${vendor}" />

		<p>${message(code:'org.findVendorMatches.note')}</p>

		<ui:searchSegment controller="vendor" action="findVendorMatches" method="get">
			<div class="field">
				<label for="proposedVendor">${message(code:'org.findVendorMatches.proposed')}</label>
				<input type="text" id="proposedVendor" name="proposedVendor" value="${params.proposedVendor}" />
			</div>
			<div class="field la-field-right-aligned">
				<a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.searchreset.label')}</a>
				<input type="submit" value="${message(code:'default.button.search.label')}" class="${Btn.PRIMARY}">
				<g:link controller="vendor" action="list" class="${Btn.SIMPLE}">${message(code:'default.button.cancel.label')}</g:link>
			</div>
		</ui:searchSegment>

				<g:if test="${vendorMatches != null}">
					<g:if test="${vendorMatches.size()>0}">
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
							<g:each in="${vendorMatches}" var="vendorInstance">
								<tr>
									<td>${vendorInstance.name} <g:link controller="vendor" action="show" id="${vendorInstance.id}">(${message(code:'default.button.edit.label')})</g:link></td>
									<td>
										<ul>
											<li><g:message code="globalUID.label" />: <g:fieldValue bean="${vendorInstance}" field="globalUID"/></li>
											<g:if test="${vendorInstance.gokbId}">
												<li><g:message code="org.wekbId.label" />: <g:fieldValue bean="${vendorInstance}" field="gokbId"/></li>
											</g:if>
											<g:if test="${vendorInstance.id}">
												<g:each in="${vendorInstance.ids}" var="id"><li>${id.ns.ns}: ${id.value}</li></g:each>
											</g:if>
										</ul>
									</td>
									<td>${vendorInstance.sortname}</td>
									<td>
										<ul>
											<g:each in="${vendorInstance.altnames}" var="altname">
												<li>${altname.name}</li>
											</g:each>
										</ul>
									</td>
								</tr>
							</g:each>
							</tbody>
						</table>
						<ui:msg class="warning" message="org.findVendorMatches.match" args="[params.proposedVendor]" />
						<g:link controller="vendor" action="createVendor" class="${Btn.NEGATIVE_SINGLECLICK}" params="${[vendor:params.proposedVendor]}">${message(code:'org.findVendorMatches.matches.create', args: [params.proposedVendor])}</g:link>
					</g:if>
					<g:else>
						<ui:msg class="warning" message="org.findVendorMatches.no_match" args="[params.proposedVendor]" />
						<g:link controller="vendor" action="createVendor" class="${Btn.POSITIVE_SINGLECLICK}" params="${[vendor:params.proposedVendor]}">${message(code:'org.findVendorMatches.no_matches.create', args: [params.proposedVendor])}</g:link>
					</g:else>
				</g:if>

<laser:htmlEnd />
