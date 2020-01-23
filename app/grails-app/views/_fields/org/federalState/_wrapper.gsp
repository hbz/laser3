<div class="control-group">
	<label class="control-label" for="federalState">${message(code:'org.federalState.label', default:'Federal State')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.FEDERAL_STATE)}" var="federalstatecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.FEDERAL_STATE)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="federalState" />
	</div>
</div>
