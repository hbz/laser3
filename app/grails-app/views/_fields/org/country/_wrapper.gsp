<div class="control-group">
	<label class="control-label" for="country">${message(code:'org.country.label')}</label>
	<div class="controls">
		<g:set value="${com.k_int.kbplus.RefdataCategory.getByDesc(de.laser.helper.RDConstants.COUNTRY)}" var="countrycat"/>
		<g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.COUNTRY)}" var="refvalues"/>
		<g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="country" />
	</div>
</div>
