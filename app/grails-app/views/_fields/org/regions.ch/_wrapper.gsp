<div class="control-group">
	<label class="control-label" for="region">${message(code:'org.region.label')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.REGIONS_CH)}"
				var="regioncat"/>
		 <g:set value="${RefdataCategory.getAllRefdataValues(RDConstants.REGIONS_CH)}" var="refvalues"/>

	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="region" />
	</div>
</div>
