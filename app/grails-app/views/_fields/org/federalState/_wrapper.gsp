<div class="control-group">
	<label class="control-label" for="federalState">${message(code:'org.federalState.label', default:'Federal State')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Federal State')}" var="federalstatecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(federalstatecat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" optionKey="id" name="federalState" />
	</div>
</div>
