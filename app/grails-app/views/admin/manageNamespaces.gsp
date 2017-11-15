<%@ page import="com.k_int.kbplus.IdentifierNamespace; com.k_int.kbplus.Identifier" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code: 'menu.admin.manageIdentifierNamespaces')}</title>
	</head>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin.dash" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageIdentifierNamespaces" class="active"/>
		</semui:breadcrumbs>

		<div>
			<div class="page-header">
				<h1><g:message code="menu.admin.manageIdentifierNamespaces"/></h1>
			</div>

			<g:if test="${flash.message}">
				<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
			</g:if>
			<g:if test="${flash.error}">
				<bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
			</g:if>

			<g:hasErrors bean="${identifierNamespaceInstance}">
				<bootstrap:alert class="alert-error">
					<ul>
						<g:eachError bean="${identifierNamespaceInstance}" var="error">
							<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
						</g:eachError>
					</ul>
				</bootstrap:alert>
			</g:hasErrors>
		</div>

		<div>
			<div class="row">
				<div class="span8">
					<table class="ui celled striped table">
						<thead>
						<tr>
							<th><g:message code="identifierNamespace.ns.label"/></th>
							<th>Identifiers</th>
							<th><g:message code="identifierNamespace.family.label"/></th>
							<th><g:message code="identifierNamespace.validationRegex.label"/></th>
							<th><g:message code="identifierNamespace.nstype.label"/></th>
							<th><g:message code="identifierNamespace.hide.label"/></th>
							<th><g:message code="identifierNamespace.nonUnique.label"/></th>
						</tr>
						</thead>
						<tbody>
						<g:each in="${identifierNamespaces}" var="idNs">
							<tr>
								<td>${fieldValue(bean: idNs, field: "ns")}</td>
								<td>${Identifier.countByNs(idNs)}</td>
								<td>${fieldValue(bean: idNs, field: "family")}</td>
								<td>${fieldValue(bean: idNs, field: "validationRegex")}</td>
								<td>${fieldValue(bean: idNs, field: "nstype")}</td>
								<td>${fieldValue(bean: idNs, field: "hide")}</td>
								<td>${fieldValue(bean: idNs, field: "nonUnique")}</td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div><!--.span8-->

				<div class="span4">
					<semui:card title="identifier.namespace.add.label" class="card-grey">
						<fieldset>
							<g:form class="form-horizontal" action="manageNamespaces">

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'ns', 'error')} required">
									<label for="ns">
										<g:message code="identifierNamespace.ns.label" />
										<span class="required-indicator">*</span>
									</label>
									<g:textField name="ns" value="${identifierNamespaceInstance?.ns}" required=""/>
								</div>

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'family', 'error')} ">
									<label for="family">
										<g:message code="identifierNamespace.family.label" />
									</label>
									<g:textField name="family" value="${identifierNamespaceInstance?.family}"/>
								</div>

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'validationRegex', 'error')} ">
									<label for="validationRegex">
										<g:message code="identifierNamespace.validationRegex.label" />
									</label>
									<g:textField name="validationRegex" value="${identifierNamespaceInstance?.validationRegex}"/>
								</div>

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'nstype', 'error')} ">
									<label for="nstype">
										<g:message code="identifierNamespace.nstype.label" /> TODO !!!
									</label>
									<laser:select id="nstype" name="nstype.id"
											  from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YNO')}"
											  optionKey="id"
											  optionValue="value"
											  value="${identifierNamespaceInstance?.nstype?.id}"
											  class="many-to-one" noSelection="['null': '']"/>
								</div>

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'hide', 'error')} ">
									<label for="hide">
										<g:message code="identifierNamespace.hide.label" />
									</label>
									<g:checkBox name="hide" value="${identifierNamespaceInstance?.hide}" checked="${identifierNamespaceInstance?.hide}" />
								</div>

								<div class="fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'nonUnique', 'error')} ">
									<label for="nonUnique">
										<g:message code="identifierNamespace.nonUnique.label" />
									</label>
									<g:checkBox name="nonUnique" value="${identifierNamespaceInstance?.nonUnique}" checked="${identifierNamespaceInstance?.nonUnique}" />
								</div>

								<br />

								<button type="submit" class="ui primary button">
									<i class="icon-ok icon-white"></i>
									<g:message code="default.button.create.label" default="Create" />
								</button>

							</g:form>
						</fieldset>

					</semui:card>
				</div><!--.span4-->
			</div><!--.row-->
		</div>

	</body>
</html>
