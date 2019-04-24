<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Org; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:if test="${comboType == 'Consortium'}">
			<g:set var="entityName" value="${message(code: 'default.institution')}" />
		</g:if>
		<g:elseif test="${comboType == 'Department'}">
			<g:set var="entityName" value="${message(code: 'default.department')}" />
		</g:elseif>
		<title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
	<semui:breadcrumbs>
		<g:if test="${comboType == 'Consortium'}">
			<semui:crumb message="menu.public.all_insts" controller="organisation" action="listInstitution"  />
			<semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
		</g:if>
		<g:elseif test="${comboType == 'Department'}">
			<semui:crumb message="menu.my.departments" controller="myInstitution" action="manageMembers"  />
			<semui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
		</g:elseif>
	</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="default.create.label" args="[entityName]" /></h1>

		<semui:messages data="${flash}" />

		<semui:errors bean="${orgInstance}" />

		<p>${message(code:'org.findInstitutionMatches.note')}</p>

		<semui:searchSegment controller="organisation" action="findOrganisationMatches" method="get">
			<div class="field">
				<label>${message(code:'org.findInstitutionMatches.proposed')}</label>
				<input type="text" name="proposedOrganisation" value="${params.proposedOrganisation}" />
			</div>
			<g:if test="${comboType == 'Consortium'}">
				<div class="field">
                    <label>${message(code:'org.findInstitutionMatches.searchId')}</label>
					<input type="text" name="proposedOrganisationID" value="${params.proposedOrganisationID}" />
				</div>
			</g:if>
			<div class="field la-field-right-aligned">
				<a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.searchreset.label')}</a>
				<input type="hidden" value="${params.comboType}" name="comboType">
				<input type="submit" value="${message(code:'default.button.search.label', default:'Filter')}" class="ui secondary button">
			</div>
		</semui:searchSegment>



				<g:if test="${organisationMatches != null}">
					<g:if test="${organisationMatches.size()>0}">
						<table class="ui celled la-table table">
							<thead>
								<tr>
									<th>${message(code:'org.name.label', default:'Name')}</th>
									<g:if test="${comboType == 'Consortium'}">
										<th>${message(code:'identifier.plural', default:'Identifiers')}</th>
										<th>${message(code:'org.shortname.label', default:'Shortname')}</th>
										<th>${message(code:'org.country.label', default:'Country')}</th>
										<th>${message(code: 'org.consortiaToggle.label')}</th>
									</g:if>
									<g:elseif test="${comboType == 'Department'}">
										<th>
											${message(code: 'org.departmentRemoval.label')}
										</th>
									</g:elseif>
								</tr>
							</thead>
							<tbody>
							<g:each in="${organisationMatches}" var="organisationInstance">
								<tr>
									<td>
										${organisationInstance.name}
										<g:if test="${(accessService.checkPerm('ORG_CONSORTIUM') && members.get(organisationInstance.id)?.contains(contextService.org.id) && members.get(organisationInstance.id)?.size() == 1) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_YODA")}">
											<g:link controller="organisation" action="show" id="${organisationInstance.id}">(${message(code:'default.button.edit.label', default:'Edit')})</g:link>
										</g:if>
									</td>
									<g:if test="${comboType == 'Consortium'}">
										<td>
											<ul>
												<li><g:message code="org.globalUID.label" default="Global UID" />: <g:fieldValue bean="${organisationInstance}" field="globalUID"/></li>
												<g:if test="${organisationInstance.impId}">
													<li><g:message code="org.impId.label" default="Import ID" />: <g:fieldValue bean="${organisationInstance}" field="impId"/></li>
												</g:if>
												<g:each in="${organisationInstance.ids.sort{it.identifier.ns.ns}}" var="id"><li>${id.identifier.ns.ns}: ${id.identifier.value}</li></g:each>
											</ul>
										</td>
										<td>${organisationInstance.shortname}</td>
										<td>${organisationInstance.country}</td>
										<td>
										<%-- here: switch if in consortia or not --%>
											<g:if test="${!members.keySet().contains(organisationInstance.id)}">
												<g:link class="ui icon positive button" data-tooltip="${message(code:'org.consortiaToggle.add.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'add', fromOrg:organisationInstance.id]}">
													<i class="plus icon"></i>
												</g:link>
											</g:if>
											<g:elseif test="${members.keySet().contains(organisationInstance.id)}">
												<g:link class="ui icon negative button" data-tooltip="${message(code:'org.consortiaToggle.remove.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'remove', fromOrg:organisationInstance.id]}">
													<i class="minus icon"></i>
												</g:link>
											</g:elseif>
										</td>
									</g:if>
									<g:elseif test="${comboType == 'Department'}">
										<td>
											<g:if test="${!organisationInstance.isEmpty()}">
												<span data-tooltip="${message(code:'org.departmentRemoval.departmentNotEmpty')}">
													<button class="ui icon negative button" disabled="disabled">
														<i class="trash alternate icon"></i>
													</button>
												</span>
											</g:if>
											<g:else>
												<g:link class="ui icon negative button"
														data-confirm-term-what="department"
														data-confirm-term-what-detail="${organisationInstance.name}"
														data-confirm-term-where="institution"
														data-tooltip="${message(code:'org.departmentRemoval.remove.label')}"
														controller="myInstitution" action="removeDepartment"
														params="${[dept:organisationInstance.id]}">
													<i class="trash alternate icon"></i>
												</g:link>
											</g:else>
										</td>
									</g:elseif>
								</tr>
							</g:each>
							</tbody>
						</table>
						<g:if test="${params.proposedOrganisation && !params.proposedOrganisation.isEmpty()}">
							<bootstrap:alert class="alert-info">
								${message(code:'org.findInstitutionMatches.match', args:[params.proposedOrganisation])}
							</bootstrap:alert>
							<g:link controller="organisation" action="createMember" class="ui negative button" params="${[institution:params.proposedOrganisation]}">${message(code:'org.findInstitutionMatches.matches.create', default:'Create New Institution with the Name', args: [params.proposedOrganisation])}</g:link>
						</g:if>
						<g:else if="${params.proposedOrganisation.isEmpty()}">
							<bootstrap:alert class="alert-info">
								${message(code:'org.findInstitutionMatches.matchNoName', args:[params.proposedOrganisation])}
							</bootstrap:alert>
						</g:else>
					</g:if>
					<g:elseif test="${params.proposedOrganisation && !params.proposedOrganisation.isEmpty()}">
						<g:if test="${params.comboType == 'Consortium'}">
							<bootstrap:alert class="alert-info">${message(code:'org.findInstitutionMatches.no_match', args:[params.proposedOrganisation])}</bootstrap:alert>
							<g:link controller="organisation" action="createMember" class="ui positive button" params="${[institution:params.proposedOrganisation]}">${message(code:'org.findInstitutionMatches.no_matches.create', args: [params.proposedOrganisation])}</g:link>
						</g:if>
						<g:elseif test="${params.comboType == 'Department'}">
							<bootstrap:alert class="alert-info">${message(code:'org.findDepartmentMatches.no_match', args:[params.proposedOrganisation])}</bootstrap:alert>
							<g:link controller="organisation" action="createMember" class="ui positive button" params="${[department:params.proposedOrganisation]}">${message(code:'org.findDepartmentMatches.no_matches.create', args: [params.proposedOrganisation])}</g:link>
						</g:elseif>
					</g:elseif>
					<g:elseif test="${params.proposedOrganisationID && !params.proposedOrganisationID.isEmpty()}">
						<bootstrap:alert class="alert-info">${message(code:'org.findInstitutionMatches.no_id_match', args:[params.proposedOrganisationID])}</bootstrap:alert>
					</g:elseif>
				</g:if>


	</body>
</html>
