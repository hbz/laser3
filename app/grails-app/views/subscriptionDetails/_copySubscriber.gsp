<%@ page import="com.k_int.kbplus.Person; de.laser.SubscriptionsQueryService; com.k_int.kbplus.Subscription; java.text.SimpleDateFormat; de.laser.helper.RDStore" %>
<laser:serviceInjection />
<semui:form>
	<g:render template="selectSourceAndTargetSubscription" model="[
			sourceSubscription: sourceSubscription,
			targetSubscription: targetSubscription,
			allSubscriptions_readRights: allSubscriptions_readRights,
			allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
	<hr>
	<g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"
			params="[workFlowPart: workFlowPart]" method="post" class="ui form newLicence">
		<g:hiddenField name="baseSubscription" value="${params.id}"/>
		<g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}"/>
		<g:hiddenField name="newSubscription" value="${newSub?.id}"/>
		<g:set var="rdvGcpI10n" value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}"/>
		<g:set var="rdvSseI10n" value="${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')}"/>

		<table class="ui celled table">
			<tbody>
			<g:if test="${validSourceSubChilds}">
				<br><b>${message(code: 'subscription.renewSubscriptionConsortia.addMembers')}</b><br>
				<g:each in="${[validSourceSubChilds]}" status="i" var="outerLoop">
					<table class="ui celled la-table table">
						<thead>
						<tr>
							<th>
								<g:if test="${outerLoop}">
									<g:checkBox name="subListToggler" id="subListToggler" checked="false"/>
								</g:if>
							</th>
							<th>${message(code: 'sidewide.number')}</th>
							<th>${message(code: 'default.sortname.label')}</th>
							<th>${message(code: 'subscriptionDetails.members.members')}</th>
							<th>${message(code: 'default.startDate.label')}</th>
							<th>${message(code: 'default.endDate.label')}</th>
							<th>${message(code: 'subscription.details.status')}</th>
						</tr>
						</thead>
						<tbody>
						<g:each in="${outerLoop}" status="j" var="sub">
							<tr>
								<g:each in="${sub.getAllSubscribers()}" var="subscr">
									<td>
										<g:checkBox type="text" name="selectedSubs" value="${sub.id}"
													checked="false"/>
									</td>
									<td>${j + 1}</td>
									<td>${subscr.sortname}</td>
									<td>
										<g:link controller="organisations" action="show" id="${subscr.id}">${subscr}</g:link>
										<div class="ui list">
											<g:each in="${com.k_int.kbplus.Person.getPublicByOrgAndFunc(subscr, 'General contact person')}"
													var="gcp">
												<div class="item">
													<g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
													(${rdvGcpI10n})
												</div>
											</g:each>
											<g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', contextService.getOrg())}"
													var="gcp">
												<div class="item">
													<g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
													(${rdvGcpI10n} <i class="address book outline icon" style="display:inline-block"></i>)
												</div>
											</g:each>
											<g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}"
													var="sse">
												<div class="item">
													<g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
													(${rdvSseI10n})
												</div>
											</g:each>
											<g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}"
													var="sse">
												<div class="item">
													<g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
													(${rdvSseI10n} <i class="address book outline icon" style="display:inline-block"></i>)
												</div>
											</g:each>

										</div>
									</td>

								</g:each>
								<g:if test="${!sub.getAllSubscribers()}">
									<td></td>
									<td></td>
								</g:if>
								<td>
									<g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/>
								</td>
								<td>
									<g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/>
								</td>
								<td>
									${sub.status.getI10n('value')}
								</td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</g:each>

				<script language="JavaScript">
					$('#subListToggler').click(function () {
						if ($(this).prop('checked')) {
							$("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
						} else {
							$("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
						}
					})
				</script>
				<input type="submit" class="ui button js-click-control" value="${message(code: 'subscription.renewSubscriptionConsortia.finish')}"/>
			</g:if>
			<g:else>
				<br><strong><g:message code="subscription.details.copyElementsIntoSubscription.noMembers" /></strong>
			</tbody>
		</table>
		</g:else>
	</g:form>
</semui:form>