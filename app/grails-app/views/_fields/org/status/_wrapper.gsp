<div class="control-group">
	<label class="control-label" for="status">${message(code:'default.status.label')}</label>
	<div class="controls">
		 <g:set value="${de.laser.RefdataCategory.getByDesc(de.laser.helper.RDConstants.ORG_STATUS)}" var="orgstatuscat"/>
		 <g:set value="${de.laser.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.ORG_STATUS)}" var="refvalues"/>
	   	 <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="status" />
	</div>
</div>
