<%@ page import="com.k_int.kbplus.Person; de.laser.SubscriptionsQueryService; com.k_int.kbplus.Subscription; java.text.SimpleDateFormat; de.laser.helper.RDStore" %>
<laser:serviceInjection />
<semui:form>
	<g:render template="selectSourceAndTargetSubscription" model="[
			sourceSubscription: sourceSubscription,
			targetSubscription: targetSubscription,
			allSubscriptions_readRights: allSubscriptions_readRights,
			allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
	<g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id}"
            params="[workFlowPart: workFlowPart]" method="post" class="ui form newLicence">
		<g:hiddenField name="baseSubscription" value="${params.id}"/>
		<g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}"/>
		<g:hiddenField name="newSubscription" value="${newSub?.id}"/>
		<g:set var="rdvGcpI10n" value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}"/>
		<g:set var="rdvSseI10n" value="${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')}"/>

		<table class="ui celled table">
			<tbody>
			<g:if test="${validSourceSubChilds}">
				<g:each in="${[validSourceSubChilds]}" var="outerLoop">
					<table>
						<tr>
							<td>
							%{---------------------------------------}%
								<table class="ui celled la-table table">
									<thead>
										<tr>
											<th colspan="5">
												<g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
											</th>
										</tr>
										<tr>
											<th>${message(code: 'default.sortname.label')}</th>
											<th>${message(code: 'default.startDate.label')}</th>
											<th>${message(code: 'default.endDate.label')}</th>
											<th>${message(code: 'subscription.details.status')}</th>
											<th>
												<g:if test="${outerLoop}">
													%{--<g:checkBox name="subListToggler" id="subListToggler" checked="false"/>--}%
												%{--</g:if>--}%
												%{--<g:if test="${targetSubscription}">--}%
													<input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
												</g:if>
											</th>
										</tr>
									</thead>
									<tbody>
										<g:each in="${outerLoop}" var="sub">
											<tr>
												<g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
													<td>${subscriberOrg.sortname}</td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
													<td>${sub.status.getI10n('value')}</td>
													<td>
														%{--<g:checkBox type="text" name="selectedSubs" value="${sub.id}" checked="false"/>--}%
														<g:checkBox name="subscription.copySubscriber" value="${genericOIDService.getOID(target_role)}" data-action="copy" checked="${true}" />
													</td>
												</g:each>
											</tr>
										</g:each>
									</tbody>
								</table>
				</g:each>
							</td>
							<td>
							%{---------------------------------------}%
								<g:each in="${[validTargetSubChilds]}" var="outerLoop">
									<table class="ui celled la-table table">
										<thead>
										<tr>
											<th colspan="5">
												<g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
											</th>
										</tr>
										<tr>
											<th>${message(code: 'default.sortname.label')}</th>
											<th>${message(code: 'default.startDate.label')}</th>
											<th>${message(code: 'default.endDate.label')}</th>
											<th>${message(code: 'subscription.details.status')}</th>
											<th>
												<g:if test="${targetSubscription}">
													<input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
												</g:if>
											</th>
										</tr>
										</thead>
										<tbody>
										<g:each in="${outerLoop}" status="j" var="sub">
											<tr>
												<g:each in="${sub.getAllSubscribers()}" var="subscr">
													<td>${subscriberOrg.sortname}</td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
													<td>${sub.status.getI10n('value')}</td>
													<td>
														%{--<g:checkBox type="text" name="selectedSubs" value="${sub.id}" checked="false"/>--}%
														<g:checkBox name="subscription.deleteSubscriber" value="${genericOIDService.getOID(target_role)}" data-action="copy" checked="${false}" />
													</td>
												</g:each>
											</tr>
										</g:each>
										</tbody>
									</table>
								</g:each>
							%{---------------------------------------}%
							</td>
					</table>

				<input type="submit" class="ui button js-click-control" value="${message(code: 'subscription.details.copyElementsIntoSubscription.copySubscriber.button')}"/>
			</g:if>
			<g:else>
				<br><strong><g:message code="subscription.details.copyElementsIntoSubscription.noMembers" /></strong>
			</tbody>
		</table>
		</g:else>
	</g:form>
</semui:form>
<script language="JavaScript">
	$('#subListToggler').click(function () {
		if ($(this).prop('checked')) {
			$("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
		} else {
			$("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
		}
	})
</script>
<style>
	table  {
		table-layout: fixed;
		width: 100%;
	}
	table td {
		vertical-align: top;
	}
</style>
