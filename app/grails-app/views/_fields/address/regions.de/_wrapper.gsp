<%@ page import="de.laser.helper.RDConstants" %>
<div class="control-group">
	<label class="control-label" for="region">${message(code:'org.region.label')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.REGIONS_DE)}"
				var="regioncat"/>
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.REGIONS_DE)}" var="refvalues"/>

	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="region" />
	</div>
</div>
