<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.Org; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.FormService" %>

<g:set var="entityName" value="${message(code: 'default.institution')}" />
<laser:htmlStart text="${message(code:"default.create.label", args:[entityName])}" />

	<ui:breadcrumbs>
		<ui:crumb message="menu.public.all_insts" controller="organisation" action="listInstitution"  />
		<ui:crumb text="${message(code:"default.create.label",args:[entityName])}" class="active"/>
	</ui:breadcrumbs>

		<ui:h1HeaderWithIcon message="default.create.label" args="[entityName]" type="institution"/>

		<ui:messages data="${flash}" />

		<ui:errors bean="${orgInstance}" />

		<p>${message(code:'org.findInstitutionMatches.note')}</p>

		<ui:searchSegment controller="organisation" action="findOrganisationMatches" method="get">
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
				<a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.searchreset.label')}</a>
				<input type="submit" value="${message(code:'default.button.search.label')}" class="${Btn.PRIMARY}">
				<g:link controller="organisation" action="list" class="${Btn.SIMPLE}">${message(code:'default.button.cancel.label')}</g:link>
			</div>
		</ui:searchSegment>

				<g:if test="${organisationMatches != null}">
					<g:if test="${organisationMatches.size()>0}">
						<table class="ui celled la-js-responsive-table la-table table">
							<thead>
								<tr>
									<th>${message(code:'default.name.label')}</th>
									<g:if test="${comboType == RDStore.COMBO_TYPE_CONSORTIUM}">
										<th>${message(code:'identifier.plural')}</th>
										<th>${message(code:'org.sortname.label')}</th>
										<th>${message(code:'altname.plural')}</th>
										<th>${message(code:'org.country.label')}</th>
										<th>${message(code: 'org.consortiaToggle.label')}</th>
									</g:if>
								</tr>
							</thead>
							<tbody>
							<g:each in="${organisationMatches}" var="organisationInstance">
								<tr>
									<td>
										${organisationInstance.name}
										<g:if test="${(contextService.getOrg().isCustomerType_Consortium() && members.get(organisationInstance.id)?.contains(contextService.getOrg().id) && members.get(organisationInstance.id)?.size() == 1) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
											<g:link controller="organisation" action="show" id="${organisationInstance.id}">(${message(code:'default.button.edit.label')})</g:link>
										</g:if>
									</td>
									<td>
										<div class="ui list">
											<span class="item js-copyTriggerParent">
												<span class="ui small basic image label js-copyTrigger la-popup-tooltip"
													  data-position="top center"
													  data-content="${message(code: 'globalUID.label')}">
													<i class="la-copy grey icon la-js-copyTriggerIcon"></i>
													<g:message code="globalUID.label"/>:
													<span class="detail js-copyTopic">
														<g:fieldValue bean="${organisationInstance}" field="globalUID"/>
														<g:if test="${organisationInstance.gokbId}">
															<g:message code="org.wekbId.label"/>:
															<g:fieldValue bean="${organisationInstance}"
																		  field="gokbId"/>
														</g:if>
													</span>
												</span>
											</span>
										</div>
										
										<laser:render template="/templates/identifier"
													  model="${[tipp: organisationInstance]}"/>

									</td>
									<td>${organisationInstance.sortname}</td>
									<td>
										<ul>
											<g:each in="${organisationInstance.altnames}" var="altname">
												<li>${altname.name}</li>
											</g:each>
										</ul>
									</td>
									<td>${organisationInstance.country}</td>
									<td>
									<%-- here: switch if in consortia or not --%>
										<g:if test="${members.get(organisationInstance.id)?.contains(contextService.getOrg().id)}">
											<g:link class="${Btn.MODERN.NEGATIVE_CONFIRM_TOOLTIP}"
													data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.consortiaToggle", args: [organisationInstance.name])}"
													data-confirm-term-how="unlink"
													data-content="${message(code:'org.consortiaToggle.remove.label')}"
													controller="organisation"
													action="toggleCombo"
													params="${params+[direction:'remove', fromOrg:organisationInstance.id]}"
													role="button"
													aria-label="${message(code: 'ariaLabel.unlink.universal')}">
												<i class="${Icon.CMD.REMOVE}"></i>
											</g:link>
										</g:if>
										<g:else>
											<g:link class="${Btn.MODERN.POSITIVE_TOOLTIP}" data-content="${message(code:'org.consortiaToggle.add.label')}" controller="organisation" action="toggleCombo" params="${params+[direction:'add', fromOrg:organisationInstance.id]}">
												<i class="${Icon.CMD.ADD}"></i>
											</g:link>
										</g:else>
									</td>
								</tr>
							</g:each>
							</tbody>
						</table>
						<g:if test="${params.proposedOrganisation && !params.proposedOrganisation.isEmpty()}">
							<ui:msg class="warning" message="org.findInstitutionMatches.match" args="[params.proposedOrganisation]" />
							<g:link controller="organisation" action="createMember" class="${Btn.NEGATIVE_SINGLECLICK}" params="${[institution:params.proposedOrganisation]}">${message(code:'org.findInstitutionMatches.matches.create', args: [params.proposedOrganisation])}</g:link>
						</g:if>
						<g:else if="${params.proposedOrganisation.isEmpty()}">
							<ui:msg class="warning" message="org.findInstitutionMatches.matchNoName" args="[params.proposedOrganisation]" />
						</g:else>
					</g:if>
					<g:elseif test="${params.proposedOrganisation && !params.proposedOrganisation.isEmpty()}">
						<ui:msg class="warning" message="org.findInstitutionMatches.no_match" args="[params.proposedOrganisation]" />
						<g:link controller="organisation" action="createMember" class="${Btn.POSITIVE_SINGLECLICK}" params="${[institution:params.proposedOrganisation,(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}">${message(code:'org.findInstitutionMatches.no_matches.create', args: [params.proposedOrganisation])}</g:link>
					</g:elseif>
					<g:elseif test="${params.proposedOrganisationID && !params.proposedOrganisationID.isEmpty()}">
						<ui:msg class="warning" message="org.findInstitutionMatches.no_id_match" args="[params.proposedOrganisationID]" />
					</g:elseif>
				</g:if>

<laser:htmlEnd />
