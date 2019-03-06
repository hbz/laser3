<div class="control-group">
	<label class="control-label" for="orgType">${message(code:'org.orgType.label', default:'Organisation Type')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('OrgRoleType')}" var="orgroletypecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(orgroletypecat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" name="orgType" optionKey="id" optionValue="${{it.getI10n('value')}}" value="${orgInstance?.orgType?.id}" noSelection="['null': '']"/>
	</div>
</div>
