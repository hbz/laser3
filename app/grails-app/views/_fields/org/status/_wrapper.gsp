<div class="control-group">
	<label class="control-label" for="status">${message(code:'default.status.label', default:'Status')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Org Status')}" var="orgstatuscat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(orgstatuscat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="status" />
	</div>
</div>
