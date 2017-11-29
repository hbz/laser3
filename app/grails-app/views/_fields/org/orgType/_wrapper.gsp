<div class="control-group">
	<label class="control-label" for="orgType">${message(code:'org.type.label', default:'Org Type')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('OrgType')}" var="orgtypecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(orgtypecat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" name="orgType" optionKey="id" optionValue="${{it.getI10n('value')}}" value="${orgInstance?.orgType?.id}" noSelection="['null': '']"/>
	</div>
</div>
