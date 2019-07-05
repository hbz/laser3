<%@ page import="com.k_int.kbplus.Person; de.laser.SubscriptionsQueryService; com.k_int.kbplus.Subscription; java.text.SimpleDateFormat; de.laser.helper.RDStore" %>
<laser:serviceInjection />
<semui:form>
	<g:render template="selectSourceAndTargetSubscription" model="[
			sourceSubscription: sourceSubscription,
			targetSubscription: targetSubscription,
			allSubscriptions_readRights: allSubscriptions_readRights,
			allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
	<g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">

		<table class="ui celled table">
			<tbody>
			<g:if test="${validSourceSubChilds}">
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
                                                    <input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
												</g:if>
											</th>
										</tr>
									</thead>
									<tbody>
										<g:each in="${validSourceSubChilds}" var="sub">
											<tr>
												<g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
													<td>${subscriberOrg.sortname}</td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
													<td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
													<td>${sub.status.getI10n('value')}</td>
													<td>
														<div class="ui checkbox la-toggle-radio la-replace">
															<g:checkBox name="subscription.copySubscriber" value="${genericOIDService.getOID(sub)}" data-action="copy" checked="${true}" />
														</div>
													</td>
												</g:each>
											</tr>
										</g:each>
									</tbody>
								</table>
							</td>
							<td>
							%{---------------------------------------}%
								<table class="ui celled la-table table">
									<thead>
									<tr>
										<th colspan="4">
											<g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
										</th>
									</tr>
									<tr>
										<th>${message(code: 'default.sortname.label')}</th>
										<th>${message(code: 'default.startDate.label')}</th>
										<th>${message(code: 'default.endDate.label')}</th>
										<th>${message(code: 'subscription.details.status')}</th>
									</tr>
									</thead>
									<tbody>
									<g:each in="${validTargetSubChilds}" var="sub">
										<tr>
											<g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
												<td>${subscriberOrg.sortname}</td>
												<td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
												<td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
												<td>${sub.status.getI10n('value')}</td>
											</g:each>
										</tr>
									</g:each>
									</tbody>
								</table>
							%{---------------------------------------}%
							</td>
					</table>

				<g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
				<div class="sixteen wide field" style="text-align: right;">
	                <input type="submit" class="ui button js-click-control" value="${message(code: 'subscription.details.copyElementsIntoSubscription.copySubscriber.button')}" ${submitDisabled} />
				</div>
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
