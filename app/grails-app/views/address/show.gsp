<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Address; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue; de.laser.helper.RDConstants;" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'address.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
	<semui:breadcrumbs>
		<g:if test="${addressInstance.org && (RDStore.OT_PROVIDER.id in addressInstance.org.getallOrgTypeIds())}">
			<semui:crumb message="menu.public.all_providers" controller="organisation" action="listProvider"/>
			<semui:crumb message="${addressInstance.org.getDesignation()}" controller="organisation" action="show" id="${addressInstance.org.id}"/>
		</g:if>
		<g:else>
			<semui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>
		</g:else>
		<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
	</semui:breadcrumbs>
	<br>
		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>

        <button class="ui button" onclick="window.location.href = document.referrer">
            Zurück
        </button>

		<semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<div class="inline-lists">

					<dl>
							<dt><g:message code="address.name.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="name" /></dd>

							<dt><g:message code="address.additionFirst.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="additionFirst" /></dd>

							<dt><g:message code="address.additionSecond.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="additionSecond" /></dd>

                            <dt>${RefdataCategory.getByDesc(RDConstants.ADDRESS_TYPE).getI10n('desc')}</dt>
                            <dd><semui:xEditableRefData owner="${addressInstance}" field="type" config="${RDConstants.ADDRESS_TYPE}" /></dd>

                            <g:if test="${addressInstance.prs}">
                                <dt><g:message code="address.prs.label" /></dt>
                                <dd><g:link controller="person" action="show" id="${addressInstance.prs.id}">${addressInstance.prs}</g:link></dd>
                            </g:if>

                            <g:if test="${addressInstance.org}">
                                <dt><g:message code="address.org.label" /></dt>
                                <dd><g:link controller="organisation" action="show" id="${addressInstance.org.id}">${addressInstance.org}</g:link></dd>
                            </g:if>


                            <g:if test="${addressInstance.prs?.tenant}">
                                <dt><g:message code="person.tenant.label" /></dt>
                                <dd><g:link controller="organisation" action="show" id="${addressInstance.prs.tenant.id}">${addressInstance.prs.tenant}</g:link></dd>
                            </g:if>

                            <g:if test="${addressInstance.prs?.isPublic}">
                                <dt><g:message code="person.isPublic.label" /></dt>
                                <dd>${RDStore.YN_YES.getI10n('value')}</dd>
                            </g:if>

                            <hr />

							<dt><g:message code="address.street_1.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_1" /></dd>

							<dt><g:message code="address.street_2.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_2" /></dd>

							<dt><g:message code="address.zipcode.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="zipcode" /></dd>

							<dt><g:message code="address.city.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="city" /></dd>

							<dt><g:message code="address.state.label" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="state" config="${RDConstants.FEDERAL_STATE}" /></dd>

							<dt><g:message code="address.country.label" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="country" config="${RDConstants.COUNTRY}" /></dd>

                            <hr />

							<dt><g:message code="address.pob.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pob" /></dd>

							<dt><g:message code="address.pobZipcode.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pobZipcode" /></dd>

							<dt><g:message code="address.pobCity.label" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pobCity" /></dd>

                            <hr />

                        </dl>
                    </div>


			</div><!-- .twelve -->

			<aside class="four wide column">
			</aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
