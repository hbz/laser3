<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Org; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'default.institution')}" />
		<title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
	<semui:breadcrumbs>
		<semui:crumb message="menu.public.all_insts" controller="organisation" action="listInstitution"  />
		<semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${orgInstance}" />

		<p>${message(code:'org.findInstitutionMatches.note')}</p>

		<semui:searchSegment controller="organisation" action="findInstitutionMatches" method="get">
			<div class="field">
				<label>${message(code:'org.findInstitutionMatches.proposed')}</label>
				<input type="text" name="proposedInstitution" value="${params.proposedInstitution}" />
			</div>
			<div class="field">
				<label>${message(code:'org.findInstitutionMatches.searchId')}</label>
				<input type="text" name="proposedInstitutionID" value="${params.proposedInstitutionID}" />
			</div>
			<div class="field la-field-right-aligned">
				<a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.searchreset.label')}</a>
				<input type="submit" value="${message(code:'default.button.search.label', default:'Filter')}" class="ui secondary button">
			</div>
		</semui:searchSegment>



				<g:if test="${institutionMatches != null}">
					<g:if test="${institutionMatches.size()>0}">
						<table class="ui celled la-table table">
							<thead>
							<tr>
								<th>${message(code:'org.name.label', default:'Name')}</th>
								<th>${message(code:'identifier.plural', default:'Identifiers')}</th>
								<th>${message(code:'org.shortname.label', default:'Shortname')}</th>
								<th>${message(code:'org.country.label', default:'Country')}</th>
								<th>${message(code: 'org.consortiaToggle.label')}</th>
							</tr>
							</thead>
							<tbody>
							<g:each in="${institutionMatches}" var="institutionInstance">
								<tr>
									<td>
										${institutionInstance.name}
										<g:if test="${(contextService.org.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id) && consortia.get(institutionInstance.id)?.contains(contextService.org.id) && consortia.get(institutionInstance.id)?.size() == 1) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_YODA")}">
											<g:link controller="organisation" action="show" id="${institutionInstance.id}" params="${[institutionalView: true]}">(${message(code:'default.button.edit.label', default:'Edit')})</g:link>
										</g:if>
									</td>
									<td><ul>
											<li><g:message code="org.globalUID.label" default="Global UID" />: <g:fieldValue bean="${institutionInstance}" field="globalUID"/></li>
											<g:if test="${institutionInstance.impId}">
												<li><g:message code="org.impId.label" default="Import ID" />: <g:fieldValue bean="${institutionInstance}" field="impId"/></li>
											</g:if>
											<g:each in="${institutionInstance.ids.sort{it.identifier.ns.ns}}" var="id"><li>${id.identifier.ns.ns}: ${id.identifier.value}</li></g:each>
									</ul></td>
									<td>${institutionInstance.shortname}</td>
									<td>${institutionInstance.country}</td>
									<td>
									<%-- here: switch if in consortia or not --%>
										<g:if test="${!consortia.keySet().contains(institutionInstance.id)}">
											<g:link class="ui icon positive button" data-tooltip="${message(code:'org.consortiaToggle.add.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'add', fromOrg:institutionInstance.id]}">
												<i class="plus icon"></i>
											</g:link>
										</g:if>
										<g:elseif test="${consortia.keySet().contains(institutionInstance.id)}">
											<g:link class="ui icon negative button" data-tooltip="${message(code:'org.consortiaToggle.remove.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'remove', fromOrg:institutionInstance.id]}">
												<i class="minus icon"></i>
											</g:link>
										</g:elseif>
									</td>
								</tr>
							</g:each>
							</tbody>
						</table>
						<g:if test="${params.proposedInstitution && !params.proposedInstitution.isEmpty()}">
							<bootstrap:alert class="alert-info">
								${message(code:'org.findInstitutionMatches.match', args:[params.proposedInstitution])}
							</bootstrap:alert>
							<g:link controller="organisation" action="createInstitution" class="ui negative button" params="${[institution:params.proposedInstitution]}">${message(code:'org.findInstitutionMatches.matches.create', default:'Create New Institution with the Name', args: [params.proposedInstitution])}</g:link>
						</g:if>
						<g:else if="${params.proposedInstitution.isEmpty()}">
							<bootstrap:alert class="alert-info">
								${message(code:'org.findInstitutionMatches.matchNoName', args:[params.proposedInstitution])}
							</bootstrap:alert>
						</g:else>
					</g:if>
					<g:elseif test="${params.proposedInstitution && !params.proposedInstitution.isEmpty()}">
						<bootstrap:alert class="alert-info">${message(code:'org.findInstitutionMatches.no_match', args:[params.proposedInstitution])}</bootstrap:alert>
						<g:link controller="organisation" action="createInstitution" class="ui positive button" params="${[institution:params.proposedInstitution]}">${message(code:'org.findInstitutionMatches.no_matches.create', default:'Create New Institution with the Name', args: [params.proposedInstitution])}</g:link>
					</g:elseif>
					<g:elseif test="${params.proposedInstitutionID && !params.proposedInstitutionID.isEmpty()}">
						<bootstrap:alert class="alert-info">${message(code:'org.findInstitutionMatches.no_id_match', args:[params.proposedInstitutionID])}</bootstrap:alert>
					</g:elseif>
				</g:if>


	</body>
</html>
