<%@ page import="com.k_int.kbplus.CreatorTitle" %>



<div class="fieldcontain ${hasErrors(bean: creatorTitleInstance, field: 'creator', 'error')} required">
	<label for="creator">
		<g:message code="creatorTitle.creator.label" default="Creator" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="creator" name="creator.id" from="${com.k_int.kbplus.Creator.list()}" optionKey="id" optionValue="lastname" required="" value="${creatorTitleInstance?.creator?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: creatorTitleInstance, field: 'role', 'error')} required">
	<label for="role">
		<g:message code="creatorTitle.role.label" default="Role" />
		<span class="required-indicator">*</span>
	</label>
	<laser:select id="role" name="role.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('CreatorType')}"
				  optionKey="id" required="" optionValue="value" value="${creatorTitleInstance?.role?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: creatorTitleInstance, field: 'title', 'error')} required">
	<label for="title">
		<g:message code="creatorTitle.title.label" default="Title" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="title" name="title.id" from="${com.k_int.kbplus.TitleInstance.list()}" optionKey="id" optionValue="title" required="" value="${creatorTitleInstance?.title?.id}" class="many-to-one"/>

</div>

