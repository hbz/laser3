<%@ page import="com.k_int.kbplus.IdentifierNamespace; com.k_int.kbplus.Identifier" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.admin.manageIdentifierNamespaces')}</title>
	</head>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin.dash" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageIdentifierNamespaces" class="active"/>
		</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.admin.manageIdentifierNamespaces"/></h1>

		<semui:messages data="${flash}" />

			<semui:errors bean="${identifierNamespaceInstance}" />

			<div class="ui grid">
				<div class="twelve wide column">
					<table class="ui celled la-table la-table-small table">
						<thead>
						<tr>
							<th><g:message code="identifierNamespace.ns.label"/></th>
							<th>Identifiers</th>
							<th><g:message code="identifierNamespace.family.label"/></th>
							<th><g:message code="identifierNamespace.nsType.label"/></th>
                            <th><g:message code="identifierNamespace.validationRegex.label"/></th>
                                <%--<th><g:message code="identifierNamespace.hide.label"/></th>--%>
                            <th><g:message code="identifierNamespace.unique.label"/></th>
							<th></th>
						</tr>
						</thead>
						<tbody>
						<g:each in="${identifierNamespaces}" var="idNs">
							<tr>
                                <g:if test="${Identifier.countByNs(idNs) == 0}">
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="ns"/>
                                    </td>
                                    <td>
                                        ${Identifier.countByNs(idNs)}
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="family"/>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="nsType"/>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="validationRegex"/>
                                    </td>
                                        <%--<td>${fieldValue(bean: idNs, field: "hide")}</td>--%>
                                    <td>
                                        <semui:xEditableBoolean owner="${idNs}" field="unique"/>
                                    </td>
                                    <td>
                                        <g:link controller="admin" action="manageNamespaces"
                                                params="${[cmd: 'deleteNamespace', oid: 'com.k_int.kbplus.IdentifierNamespace:' + idNs.id]}" class="ui icon negative button">
                                            <i class="trash alternate icon"></i>
                                        </g:link>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td>${fieldValue(bean: idNs, field: "ns")}</td>
                                    <td>${Identifier.countByNs(idNs)}</td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="family"/>
                                    </td>
                                    <td>${fieldValue(bean: idNs, field: "nsType")}</td>
                                    <td>${fieldValue(bean: idNs, field: "validationRegex")}</td>
                                        <%--<td>${fieldValue(bean: idNs, field: "hide")}</td>--%>
                                    <td>${idNs.unique}</td>
                                    <td></td>
                                </g:else>
                            </tr>
						</g:each>
						</tbody>
					</table>
				</div><!--.twelve-->

				<aside class="four wide column">
					<semui:card message="identifier.namespace.add.label">
						<fieldset>
							<g:form class="ui form" action="manageNamespaces">

								<div class="field fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'ns', 'error')} required">
									<label for="ns">
										<g:message code="identifierNamespace.ns.label" />
										<span class="required-indicator">*</span>
									</label>
									<g:textField name="ns" required=""/>
								</div>

								<div class="field fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'family', 'error')} ">
									<label for="family">
										<g:message code="identifierNamespace.family.label" />
									</label>
									<g:textField name="family"/>
								</div>

								<div class="field fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'nsType', 'error')} ">
									<label for="nsType">
										<g:message code="identifierNamespace.nsType.label" />
									</label>
									<g:select id="nsType" name="nsType"
									        from="${IdentifierNamespace.getAVAILABLE_NSTYPES()}"
									        noSelection="['': '']"/>
								</div>

                                <div class="field fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'validationRegex', 'error')} ">
                                    <label for="validationRegex">
                                        <g:message code="identifierNamespace.validationRegex.label" />
                                    </label>
                                    <g:textField name="validationRegex"/>
                                </div>

								<div class="field fieldcontain ${hasErrors(bean: identifierNamespaceInstance, field: 'unique', 'error')} ">
									<label for="unique">
										<g:message code="identifierNamespace.unique.label" />
									</label>
									<g:checkBox name="unique" checked="true" />
								</div>

								<br />

								<button type="submit" class="ui button">
									<g:message code="default.button.create.label" default="Create" />
								</button>

							</g:form>
						</fieldset>

					</semui:card>
				</aside><!--.four-->
			</div><!--.grid-->

	</body>
</html>
