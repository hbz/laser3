<%@ page import="de.laser.domain.I10nTranslation; com.k_int.kbplus.RefdataValue" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<title>${message(code: 'menu.admin.manageI10n')}</title>
	</head>

		<laser:breadcrumbs>
			<laser:crumb message="menu.admin.dash" controller="admin" action="index" />
			<laser:crumb message="menu.admin.manageI10n" class="active"/>
		</laser:breadcrumbs>

		<div class="container">
			<h1><g:message code="menu.admin.manageI10n"/></h1>

			<laser:subNav>
				<laser:subNavItem controller="admin" action="i10n" params="${[type:'refdata']}" text="RefdataCategories/-Values" />
				<laser:subNavItem controller="admin" action="i10n" params="${[type:'properties']}" text="Property Definitions" />
			</laser:subNav>
		</div>

		<div class="container">
			<div class="row">
				<div class="span8">
					<table class="table table-bordered">
						<thead>
						<tr>
							<th>Property Definition</th>
							<th>Name DE</th>
							<th>Name EN</th>
							<th>Description DE</th>
							<th>Description EN</th>
						</tr>
						</thead>
						<tbody>
							<g:each in="${propertyDefinitions}" var="pd">
								<g:set var="pdI10nName" value="${I10nTranslation.createOrUpdateI10n(pd, 'name', [:])}" />
								<g:set var="pdI10nDescr" value="${I10nTranslation.createOrUpdateI10n(pd, 'descr', [:])}" />
								<tr>
									<td>
										(${pd.getId()})
										${fieldValue(bean: pd, field: "descr")} <br/>
										<strong>${fieldValue(bean: pd, field: "name")}</strong>
									</td>
									<td><g:xEditable owner="${pdI10nName}"  field="valueDe" /></td>
									<td><g:xEditable owner="${pdI10nName}"  field="valueEn" /></td>
									<td><g:xEditable owner="${pdI10nDescr}" field="valueDe" /></td>
									<td><g:xEditable owner="${pdI10nDescr}" field="valueEn" /></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div><!--.span8-->
			</div><!--.row-->
		</div>

	</body>
</html>
