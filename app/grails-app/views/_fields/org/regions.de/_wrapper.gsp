<div class="control-group">
	<label class="control-label" for="region">${message(code:'org.region.label')}</label>
	<div class="controls">
		 <g:set value="${de.laser.RefdataCategory.getByDesc(de.laser.helper.RDConstants.REGIONS_DE)}"
				var="regioncat"/>
		 <g:set value="${de.laser.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.REGIONS_DE)}" var="refvalues"/>

	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="region" />
	</div>
</div>
