<div class="control-group">
	<label class="control-label" for="sector">${message(code:'org.sector.label', default:'Sector')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('OrgSector')}" var="orgsectorcat"/>
		 <g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(orgsectorcat)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" name="sector" optionKey="id" optionValue="${{it.getI10n('value')}}" value="${orgInstance?.sector?.id}" noSelection="['null': '']"/>
	</div>
</div>
