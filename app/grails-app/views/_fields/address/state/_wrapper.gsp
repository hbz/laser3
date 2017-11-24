<div class="control-group">
	<label class="control-label" for="state">${message(code:'address.state.label', default:'State')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Federal State')}" var="statecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(statecat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" optionKey="id" name="state" />
	</div>
</div>
