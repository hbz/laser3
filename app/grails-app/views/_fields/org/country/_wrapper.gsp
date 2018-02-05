<div class="control-group">
	<label class="control-label" for="country">${message(code:'org.country.label', default:'Country')}</label>
	<div class="controls">
		<g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Country')}" var="countrycat"/>
		<g:set value="${com.k_int.kbplus.RefdataValue.findAllByOwner(countrycat)}" var="refvalues"/>
		<g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" name="country" />
	</div>
</div>
