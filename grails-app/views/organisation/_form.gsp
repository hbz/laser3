<%@ page import="de.laser.Org;de.laser.storage.RDConstants;de.laser.RefdataCategory" %>

<div class="field ${hasErrors(bean: orgInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="default.name.label" />
	</label>
    <g:textField name="name" maxlength="255" value="${orgInstance?.name}"/>
</div>

<div class="fields two">
<div class="field ${hasErrors(bean: orgInstance, field: 'shortname', 'error')}">
	<label for="shortname">
		<g:message code="org.shortname.label" />
	</label>
    <g:textField name="shortname" maxlength="255" value="${orgInstance?.shortname}"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'sortname', 'error')}">
	<label for="sortname">
		<g:message code="org.sortname.label" />
	</label>
    <g:textField name="sortname" maxlength="255" value="${orgInstance?.sortname}"/>
</div>
</div>

<div class="fields two">
<div class="field ${hasErrors(bean: orgInstance, field: 'sector', 'error')}">
	<label for="sector">
		<g:message code="org.sector.label" />
	</label>
	<laser:select id="sector" name="sector.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.ORG_SECTOR)}"
				  optionKey="id" optionValue="value" value="${orgInstance?.sector?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'libraryType', 'error')}">
	<label for="libraryType">
		<g:message code="org.libraryType.label" />
	</label>
	<laser:select id="libraryType" name="libraryType.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.LIBRARY_TYPE)}"
				  optionKey="id" optionValue="value" value="${orgInstance?.libraryType?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'url', 'error')}">
	<label for="url">
		<g:message code="default.url.label" />
	</label>
    <g:textField name="url" maxlength="512" value="${orgInstance?.url}"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'urlGov', 'error')}">
	<label for="urlGov">
		<g:message code="org.urlGov.label" />
	</label>
	<g:textField name="urlGov" maxlength="512" value="${orgInstance?.urlGov}"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'country', 'error')}">
	<label for="country">
		<g:message code="org.country.label" />
	</label>
	<laser:select id="country" name="country.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.COUNTRY)}"
			  optionKey="id" optionValue="value" value="${orgInstance?.country?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'region', 'error')}">
	<label for="region">
		<g:message code="org.region.label" />
	</label>
	<laser:select id="region" name="region.id" from="${RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH])}"
			  optionKey="id" optionValue="value" value="${orgInstance?.region?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'libraryNetwork', 'error')}">
	<label for="libraryNetwork">
		<g:message code="org.libraryNetwork.label" />
	</label>
	<laser:select id="libraryNetwork" name="libraryNetwork.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.LIBRARY_NETWORK)}"
			  optionKey="id" optionValue="value" value="${orgInstance?.libraryNetwork?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: orgInstance, field: 'funderType', 'error')}">
	<label for="funderType">
		<g:message code="org.funderType.label" />
	</label>
	<laser:select id="funderType" name="funderType.id" from="${RefdataCategory.getAllRefdataValues(RDConstants.FUNDER_TYPE)}"
			  optionKey="id" optionValue="value" value="${orgInstance?.funderType?.id}" class="ui dropdown many-to-one" noSelection="['null': '']"/>
</div>
