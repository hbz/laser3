<%@ page import="de.laser.Platform;de.laser.RefdataCategory;de.laser.helper.RDConstants;de.laser.TitleInstancePackagePlatform" %>

<div class="field ${hasErrors(bean: platformInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="default.name.label" />
		
	</label>
	<g:textField name="name" value="${platformInstance?.name}"/>
</div>

<div class="field ${hasErrors(bean: platformInstance, field: 'serviceProvider', 'error')} ">
	<label for="serviceProvider">
		<g:message code="platform.serviceProvider" />
		
	</label>
	<g:select name="serviceProvider" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N_O)}" multiple="multiple" optionKey="id" size="5" optionValue="${{it.getI10n('value')}}"/>
</div>

<div class="field ${hasErrors(bean: platformInstance, field: 'tipps', 'error')} ">
	<label for="tipps">
		<g:message code="platform.tipps.label" default="Tipps" />
		
	</label>
	<g:select name="tipps" from="${TitleInstancePackagePlatform.list()}" multiple="multiple" optionKey="id" size="5" value="${platformInstance?.tipps*.id}" class="many-to-many"/>
</div>

