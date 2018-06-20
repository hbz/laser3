
<%@ page import="com.k_int.kbplus.Address" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'address.label', default: 'Address')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
	<semui:breadcrumbs>
		<g:if test="${addressInstance?.org?.orgType == com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'OrgType')}">
			<semui:crumb message="menu.institutions.all_provider" controller="organisations" action="listProvider"/>
			<semui:crumb message="${addressInstance?.org?.getDesignation()}" controller="organisations" action="show" id="${addressInstance?.org?.id}"/>
			<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
		</g:if>
		<g:else>
			<semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index"/>
			<semui:crumb message="${addressInstance?.org?.getDesignation()}" controller="organisations" action="show" id="${addressInstance?.org?.id}"/>
			<semui:crumb text="${g.message(code:'default.edit.label', args:[entityName])}" class="active"/>
		</g:else>
	</semui:breadcrumbs>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>
		<semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<div class="inline-lists">

					<dl>
                            <dt>${com.k_int.kbplus.RefdataCategory.findByDesc('AddressType').getI10n('desc')}</dt>
                            <dd><semui:xEditableRefData owner="${addressInstance}" field="type" config="AddressType" /></dd>

                            <g:if test="${addressInstance?.prs}">
                                <dt><g:message code="address.prs.label" default="Prs" /></dt>
                                <dd><g:link controller="person" action="show" id="${addressInstance?.prs?.id}">${addressInstance?.prs?.encodeAsHTML()}</g:link></dd>
                            </g:if>

                            <g:if test="${addressInstance?.org}">
                                <dt><g:message code="address.org.label" default="Org" /></dt>
                                <dd><g:link controller="organisations" action="show" id="${addressInstance?.org?.id}">${addressInstance?.org?.encodeAsHTML()}</g:link></dd>
                            </g:if>


                            <g:if test="${addressInstance?.prs?.tenant}">
                                <dt><g:message code="person.tenant.label" default="Tenant (derived from Prs)" /></dt>
                                <dd><g:link controller="organisations" action="show" id="${addressInstance?.prs?.tenant?.id}">${addressInstance?.prs?.tenant?.encodeAsHTML()}</g:link></dd>
                            </g:if>

                            <g:if test="${addressInstance?.prs?.isPublic}">
                                <dt><g:message code="person.isPublic.label" default="IsPublic (derived from Prs)" /></dt>
                                <dd>${addressInstance?.prs?.isPublic?.encodeAsHTML()}</dd>
                            </g:if>

                            <hr />

							<dt><g:message code="address.street_1.label" default="Street1" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_1" /></dd>

							<dt><g:message code="address.street_2.label" default="Street2" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="street_2" /></dd>

							<dt><g:message code="address.zipcode.label" default="Zipcode" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="zipcode" /></dd>

							<dt><g:message code="address.city.label" default="City" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="city" /></dd>

							<dt><g:message code="address.state.label" default="State" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="state" config="State" /></dd>

							<dt><g:message code="address.country.label" default="Country" /></dt>
							<dd><semui:xEditableRefData owner="${addressInstance}" field="country" config="Country" /></dd>

                            <hr />

							<dt><g:message code="address.pob.label" default="Pob" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pob" /></dd>

							<dt><g:message code="address.pobZipcode.label" default="pobZipcode" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pobZipcode" /></dd>

							<dt><g:message code="address.pobCity.label" default="pobCity" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="pobCity" /></dd>

                            <hr />

							<dt><g:message code="address.name.label" default="name" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="name" /></dd>

							<dt><g:message code="address.additionFirst.label" default="additionFirst" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="additionFirst" /></dd>

							<dt><g:message code="address.additionSecond.label" default="additionSecond" /></dt>
							<dd><semui:xEditable owner="${addressInstance}" field="additionSecond" /></dd>

					</dl>
				</div>

				<sec:ifAnyGranted roles="ROLE_YODA">
					<h3 class="ui header">ROLE_YODA</h3>
					<div class="inline-lists">
						<dl>
							<dt><g:message code="address.prs.label" default="Prs" /></dt>
							<dd>TODO: SELECT PERSON</dd>


							<dt><g:message code="address.org.label" default="Org" /></dt>
							<dd>TODO: SELECT ORG</dd>
						</dl>
					</div>
				</sec:ifAnyGranted>

				<g:if test="${editable}">
					<g:form>
						<g:hiddenField name="id" value="${addressInstance?.id}" />
						<div class="ui form-actions">
							<g:link class="ui button" action="edit" id="${addressInstance?.id}">
								<i class="write icon"></i>
								<g:message code="default.button.edit.label" default="Edit" />
							</g:link>
							<button class="ui negative button" type="submit" name="_action_delete">
								<i class="trash alternate icon"></i>
								<g:message code="default.button.delete.label" default="Delete" />
							</button>
						</div>
					</g:form>
				</g:if>

			</div><!-- .twelve -->

			<aside class="four wide column">
			</aside><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
