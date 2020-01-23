<div class="control-group">
	<label class="control-label" for="sector">${message(code:'org.sector.label', default:'Sector')}</label>
	<div class="controls">
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.ORG_SECTOR)}" var="orgsectorcat"/>
		 <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.ORG_SECTOR)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" name="sector" optionKey="id" optionValue="${{it.getI10n('value')}}" value="${orgInstance?.sector?.id}" noSelection="['null': '']"/>
	</div>
</div>
