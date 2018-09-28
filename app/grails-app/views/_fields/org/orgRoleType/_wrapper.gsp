<div class="control-group">
	<label class="control-label" for="orgRoleType">${message(code:'org.orgRoleType.label', default:'Organisation Type')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('OrgRoleType')}" var="orgroletypecat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(orgroletypecat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" name="orgRoleType" optionKey="id" optionValue="${{it.getI10n('value')}}" value="${orgInstance?.orgRoleType?.id}" noSelection="['null': '']"/>
	</div>
</div>
