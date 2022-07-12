<%@ page import="de.laser.Task;de.laser.storage.RDConstants;de.laser.RefdataCategory; de.laser.RefdataValue" %>

<div class="field ${hasErrors(bean: taskInstance, field: 'license', 'error')} ">
	<label for="license">
		<g:message code="license.label" />
	</label>
	<g:select id="license" name="license.id" from="${validLicenses}" optionKey="id" value="${taskInstance?.license?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'org', 'error')} ">
	<label for="org">
		<g:message code="task.org.label" />
	</label>
	<g:select id="org" name="org.id" from="${validOrgs}" optionKey="id" value="${taskInstance?.org?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} ">
	<label for="pkg">
		<g:message code="package.label" />
	</label>
	<g:select id="pkg" name="pkg.id" from="${validPackages}" optionKey="id" value="${taskInstance?.pkg?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} ">
	<label for="subscription">
		<g:message code="default.subscription.label" />
	</label>
	<g:select id="subscription" name="subscription.id" from="${validSubscriptions}" optionKey="id" value="${taskInstance?.subscription?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
	<label for="title">
		<g:message code="default.title.label" />
	</label>
	<g:textField name="title" required="" value="${taskInstance?.title}"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
	<label for="description">
		<g:message code="default.description.label" />
	</label>
	<g:textField name="description" value="${taskInstance?.description}"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
	<label for="status">
		<g:message code="task.status.label" />
	</label>
	<g:select id="status" name="status.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.TASK_STATUS)}"
			  optionKey="id" required="" value="${taskInstance?.status?.id ?: RefdataValue.getByValueAndCategory("Open", RDConstants.TASK_STATUS).id}" class="many-to-one"/>
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'creator', 'error')} required">
	<label for="creator">
		<g:message code="task.creator.label" />
	</label>
	<g:select id="creator" name="creator.id" from="${taskCreator}" optionKey="id" optionValue="display" required="" value="${taskInstance?.creator?.id}" class="many-to-one"/>
</div>

<ui:datepicker label="task.createDate.label" id="createDate" name="createDate" placeholder="default.date.label" value="${taskInstance?.createDate}" required="" bean="${taskInstance}" />

<ui:datepicker label="task.endDate.label" id="endDate" name="endDate" placeholder="default.date.label" value="${taskInstance?.endDate}" required="" bean="${taskInstance}" />

<div class="field ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')} required">
	<label for="responsibleUser">
		<g:message code="task.responsibleUser.label" />
	</label>
	<g:select id="responsibleUser" name="responsibleUser.id" from="${validResponsibleUsers}" optionKey="id" optionValue="display" value="${taskInstance?.responsibleUser?.id}" class="many-to-one" noSelection="['null': '']" />
</div>

<div class="field ${hasErrors(bean: taskInstance, field: 'responsibleOrg', 'error')} required">
	<label for="responsibleOrg">
		<g:message code="task.responsibleOrg.label" />
	</label>
	<g:select id="responsibleOrg" name="responsibleOrg.id" from="${validResponsibleOrgs}" optionKey="id" value="${taskInstance?.responsibleOrg?.id}" class="many-to-one" noSelection="['null': '']" />
</div>
