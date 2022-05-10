<%@ page import="de.laser.IssueEntitlement; de.laser.TitleInstancePackagePlatform; de.laser.Subscription; de.laser.RefdataValue" %>



<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'status', 'error')} ">
	<label for="status">
		<g:message code="default.status.label" />
	</label>
	<g:select id="status" name="status.id" from="${RefdataValue.list()}" optionKey="id" value="${issueEntitlementInstance?.status?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'subscription', 'error')} ">
	<label for="subscription">
		<g:message code="subscription.label" />
	</label>
	<g:select id="subscription" name="subscription.id" from="${Subscription.list()}" optionKey="id" value="${issueEntitlementInstance?.subscription?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'tipp', 'error')} ">
	<label for="tipp">
		<g:message code="tipp.label" />
	</label>
	<g:select id="tipp" name="tipp.id" from="${TitleInstancePackagePlatform.list()}" optionKey="id" value="${issueEntitlementInstance?.tipp?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'startDate', 'error')} ">
	<label for="startDate">
		<g:message code="default.startDate.label" />
	</label>
	<g:datePicker name="startDate" precision="day"  value="${issueEntitlementInstance?.startDate}" default="none" noSelection="['': '']" />
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'startVolume', 'error')} ">
	<label for="startVolume">
		<g:message code="tipp.startVolume" />
	</label>
	<g:textField name="startVolume" value="${issueEntitlementInstance?.startVolume}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'startIssue', 'error')} ">
	<label for="startIssue">
		<g:message code="tipp.startIssue" />
	</label>
	<g:textField name="startIssue" value="${issueEntitlementInstance?.startIssue}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'endDate', 'error')} ">
	<label for="endDate">
		<g:message code="default.endDate.label" />
	</label>
	<g:datePicker name="endDate" precision="day"  value="${issueEntitlementInstance?.endDate}" default="none" noSelection="['': '']" />
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'endVolume', 'error')} ">
	<label for="endVolume">
		<g:message code="tipp.endVolume" />
	</label>
	<g:textField name="endVolume" value="${issueEntitlementInstance?.endVolume}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'endIssue', 'error')} ">
	<label for="endIssue">
		<g:message code="tipp.endIssue" />
	</label>
	<g:textField name="endIssue" value="${issueEntitlementInstance?.endIssue}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'embargo', 'error')} ">
	<label for="embargo">
		<g:message code="tipp.embargo" />
	</label>
	<g:textField name="embargo" value="${issueEntitlementInstance?.embargo}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'coverageDepth', 'error')} ">
	<label for="coverageDepth">
		<g:message code="tipp.coverageDepth" />
	</label>
	<g:textField name="coverageDepth" value="${issueEntitlementInstance?.coverageDepth}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'coverageNote', 'error')} ">
	<label for="coverageNote">
		<g:message code="default.note.label" />
	</label>
	<g:textField name="coverageNote" value="${issueEntitlementInstance?.coverageNote}"/>
</div>

<div class="field ${hasErrors(bean: issueEntitlementInstance, field: 'ieReason', 'error')} ">
	<label for="ieReason">
		<g:message code="issueEntitlement.ieReason.label" default="Ie Reason" />
		
	</label>
	<g:textField name="ieReason" value="${issueEntitlementInstance?.ieReason}"/>
</div>

