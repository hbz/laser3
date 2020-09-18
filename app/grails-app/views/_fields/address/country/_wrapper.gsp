<div class="control-group">
	<label class="control-label" for="country">${message(code:'address.country.label')}</label>
	<div class="controls">
		<g:set value="${de.laser.RefdataCategory.getByDesc(de.laser.helper.RDConstants.COUNTRY)}" var="countrycat"/>
		<g:set value="${de.laser.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.COUNTRY)}" var="refvalues"/>
		<g:select from="${refvalues}" optionKey="id" name="country" />
	</div>
</div>
