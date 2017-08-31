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
							<th>Category (Key)</th>
							<th>Values</th>
							<th>DE</th>
							<th>EN</th>
						</tr>
						</thead>
						<tbody>
							<g:each in="${rdCategories}" var="rdc">
								<g:set var="rdcI10n" value="${I10nTranslation.createI10nIfNeeded(rdc, 'desc')}" />
								<tr>
									<td>${fieldValue(bean: rdc, field: "desc")}</td>
									<td>${(RefdataValue.findAllByOwner(rdc)).size()}</td>
									<td>
										<strong><g:xEditable owner="${rdcI10n}" field="valueDe" /></strong>
									</td>
									<td>
										<strong><g:xEditable owner="${rdcI10n}" field="valueEn" /></strong>
									</td>
								</tr>
								<tr>
									<td></td>
									<td></td>
									<td colspan="2" style="padding:0;border-left:none">
										<table style="width:100%" class="table-striped table-hover">
											<g:each in="${RefdataValue.findAllByOwner(rdc)}" var="rdv">
												<tr>
													<td style="width:50%">
														<g:xEditable owner="${I10nTranslation.createI10nIfNeeded(rdv, 'value')}" field="valueDe" />
													</td>
													<td style="width:50%">
														<g:xEditable owner="${I10nTranslation.createI10nIfNeeded(rdv, 'value')}" field="valueEn" />
													</td>
												</tr>
											</g:each>
										</table>
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div><!--.span8-->

				<!--<div class="span4">
					<laser:card title="identifier.namespace.add.label" class="card-grey">
			TODO
					</laser:card>
				</div>--><!--.span4-->
			</div><!--.row-->
		</div>

	</body>
</html>
