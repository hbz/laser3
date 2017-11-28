<%@ page import="com.k_int.kbplus.Task" %>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'license', 'error')} ">
	<label for="license">
		<g:message code="task.license.label" default="License" />
		
	</label>
	<g:select id="license" name="license.id" from="${validLicenses}" optionKey="id" value="${taskInstance?.license?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'org', 'error')} ">
	<label for="org">
		<g:message code="task.org.label" default="Org" />
		
	</label>
	<g:select id="org" name="org.id" from="${validOrgs}" optionKey="id" value="${taskInstance?.org?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} ">
	<label for="pkg">
		<g:message code="task.pkg.label" default="Pkg" />
		
	</label>
	<g:select id="pkg" name="pkg.id" from="${validPackages}" optionKey="id" value="${taskInstance?.pkg?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} ">
	<label for="subscription">
		<g:message code="task.subscription.label" default="Subscription" />
		
	</label>
	<g:select id="subscription" name="subscription.id" from="${validSubscriptions}" optionKey="id" value="${taskInstance?.subscription?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
	<label for="title">
		<g:message code="task.title.label" default="Title" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="title" required="" value="${taskInstance?.title}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'description', 'error')} required">
	<label for="description">
		<g:message code="task.description.label" default="Description" />
	</label>
	<g:textField name="description" value="${taskInstance?.description}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
	<label for="status">
		<g:message code="task.status.label" default="Status" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Task Status')}" optionKey="id" required="" value="${taskInstance?.status?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'owner', 'error')} required">
	<label for="owner">
		<g:message code="task.owner.label" default="Owner" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="owner" name="owner.id" from="${taskOwner}" optionKey="id" optionValue="display" required="" value="${taskInstance?.owner?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'createDate', 'error')} required">
    <label for="createDate">
        <g:message code="task.createDate.label" default="Create Date" />
        <span class="required-indicator">*</span>
    </label>
    <g:datePicker name="createDate" precision="day"  value="${taskInstance?.createDate}"  />

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'endDate', 'error')} required">
	<label for="endDate">
		<g:message code="task.endDate.label" default="End Date" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker name="endDate" precision="day"  value="${taskInstance?.endDate}"  />

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'tenantUser', 'error')} required">
	<label for="tenantUser">
		<g:message code="task.tenantUser.label" default="Tenant User" />
	</label>
	<g:select id="tenantUser" name="tenantUser.id" from="${validTenantUsers}" optionKey="id" optionValue="display" value="${taskInstance?.tenantUser?.id}" class="many-to-one" noSelection="['null': '']" />

</div>

<div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'tenantOrg', 'error')} required">
	<label for="tenantOrg">
		<g:message code="task.tenantOrg.label" default="Tenant Org" />
	</label>
	<g:select id="tenantOrg" name="tenantOrg.id" from="${validTenantOrgs}" optionKey="id" value="${taskInstance?.tenantOrg?.id}" class="many-to-one" noSelection="['null': '']" />

</div>
