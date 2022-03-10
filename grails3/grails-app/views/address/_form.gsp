<%@ page import="de.laser.RefdataCategory; de.laser.Address;de.laser.Person;de.laser.helper.RDConstants;de.laser.RefdataValue;de.laser.Org" %>

<div class="field ${hasErrors(bean: addressInstance, field: 'type', 'error')} ">
	<label for="type">
		${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}

	</label>
	<laser:select class="ui dropdown" id="type" name="type.id"
				  from="${Address.getAllRefdataValues()}"
				  optionKey="id"
				  optionValue="value"
				  value="${addressInstance?.type?.id}"
				  required=""/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'prs', 'error')} ">
    <label for="prs">
        <g:message code="address.prs.label" />

    </label>
    <g:select id="prs" name="prs.id" from="${Person.list()}" optionKey="id" value="${addressInstance?.prs?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'org', 'error')} ">
    <label for="org">
        <g:message code="address.org.label" />

    </label>
    <g:select id="org" name="org.id" from="${Org.list()}" optionKey="id" value="${addressInstance?.org?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<hr />

<div class="field ${hasErrors(bean: addressInstance, field: 'street_1', 'error')} required">
	<label for="street_1">
		<g:message code="address.street_1.label" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField id="street_1" name="street_1" required="" value="${addressInstance?.street_1}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'street_2', 'error')} ">
	<label for="street_2">
		<g:message code="address.street_2.label" />
		
	</label>
	<g:textField id="street_2" name="street_2" value="${addressInstance?.street_2}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'zipcode', 'error')} required">
	<label for="zipcode">
		<g:message code="address.zipcode.label" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField id="zipcode" name="zipcode" required="" value="${addressInstance?.zipcode}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'city', 'error')} required">
	<label for="city">
		<g:message code="address.city.label" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField id="city" name="city" required="" value="${addressInstance?.city}"/>

</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'state', 'error')}">
	<label for="region">
		<g:message code="address.region.label" />
	</label>
	<laser:select class="ui dropdown" id="region" name="region.id"
				  from="${RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT,
                                                               RDConstants.REGIONS_CH])}"
				  optionKey="id"
				  optionValue="value"
				  value="${addressInstance?.state?.id}"
                  noSelection="['null': '']" />
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'country', 'error')}">
	<label for="country">
		<g:message code="address.country.label" />
	</label>
	<laser:select class="ui dropdown" id="country" name="country.id"
				  from="${RefdataCategory.getAllRefdataValues(RDConstants.COUNTRY)}"
				  optionKey="id"
				  optionValue="value"
				  value="${addressInstance?.country?.id}"
                  noSelection="['null': '']" />
</div>

<hr />

<div class="field ${hasErrors(bean: addressInstance, field: 'pob', 'error')} ">
	<label for="pob">
		<g:message code="address.pob.label" />

	</label>
	<g:textField id="pob" name="pob" value="${addressInstance?.pob}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'pobZipcode', 'error')} ">
	<label for="pobZipcode">
		<g:message code="address.pobZipcode.label" />

	</label>
	<g:textField id="pobZipcode" name="pobZipcode" value="${addressInstance?.pobZipcode}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'pobCity', 'error')} ">
	<label for="pobCity">
		<g:message code="address.pobCity.label" />

	</label>
	<g:textField id="pobCity" name="pobCity" value="${addressInstance?.pobCity}"/>
</div>

<hr />

<div class="field ${hasErrors(bean: addressInstance, field: 'name', 'error')} ">
    <label for="name">
        <g:message code="address.name.label" />

    </label>
    <g:textField id="name" name="name" value="${addressInstance?.name}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'additionFirst', 'error')} ">
    <label for="additionFirst">
        <g:message code="address.additionFirst.label" />

    </label>
    <g:textField id="additionFirst" name="additionFirst" value="${addressInstance?.additionFirst}"/>
</div>

<div class="field ${hasErrors(bean: addressInstance, field: 'additionSecond', 'error')} ">
    <label for="additionSecond">
        <g:message code="address.additionSecond.label" />

    </label>
    <g:textField id="additionSecond" name="additionSecond" value="${addressInstance?.additionSecond}"/>
</div>

