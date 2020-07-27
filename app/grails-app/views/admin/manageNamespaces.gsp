<%@ page import="com.k_int.kbplus.IdentifierNamespace; com.k_int.kbplus.Identifier;de.laser.I10nTranslation" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code:'laser')} : ${message(code: 'menu.admin.manageIdentifierNamespaces')}</title>
	</head>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin.dash" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageIdentifierNamespaces" class="active"/>
		</semui:breadcrumbs>
		<br>
		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="menu.admin.manageIdentifierNamespaces"/></h1>

		<semui:messages data="${flash}" />

			<semui:errors bean="${identifierNamespaceInstance}" />

        <semui:form message="identifier.namespace.add.label">

            <g:form class="ui form" action="manageNamespaces">
                <div class="two fields">
                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'name_de', 'error')} ">
                        <label for="name_de"><g:message code="default.name.label" /> (DE)</label>
                        <g:textField name="name_de"/>
                    </div>

                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'name_en', 'error')} ">
                        <label for="name_en"><g:message code="default.name.label" /> (EN)</label>
                        <g:textField name="name_en"/>
                    </div>
                </div>

                <div class="two fields">
                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'description_de', 'error')} ">
                        <label for="description_de"><g:message code="default.description.label" /> (DE)</label>
                        <g:textField name="description_de"/>
                    </div>

                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'description_en', 'error')} ">
                        <label for="description_en"><g:message code="default.description.label" /> (EN)</label>
                        <g:textField name="description_en"/>
                    </div>
                </div>

                <div class="two fields">
                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'ns', 'error')} required">
                        <label for="ns"><g:message code="identifierNamespace.ns.label" /></label>
                        <g:textField name="ns" required=""/>
                    </div>

                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'nsType', 'error')} ">
                        <label for="nsType"><g:message code="default.type.label" /></label>
                        <g:select id="nsType" name="nsType" class="ui dropdown la-clearable"
                                  from="${IdentifierNamespace.getAVAILABLE_NSTYPES()}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>
                </div>

                <div class="two fields">
                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'urlPrefix', 'error')} ">
                        <label for="urlPrefix"><g:message code="identifierNamespace.urlPrefix.label" /></label>
                        <g:textField name="urlPrefix"/>
                    </div>

                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'family', 'error')} ">
                        <label for="family"><g:message code="identifierNamespace.family.label" /></label>
                        <g:textField name="family"/>
                    </div>
                </div>

                <div class="two fields">
                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'isUnique', 'error')} ">
                        <label for="isUnique"><g:message code="identifierNamespace.unique.label" /></label>
                        <g:checkBox name="isUnique" checked="true" />
                    </div>

                    <div class="field ${hasErrors(bean: identifierNamespaceInstance, field: 'validationRegex', 'error')} ">
                        <label for="validationRegex"><g:message code="identifierNamespace.validationRegex.label" /></label>
                        <g:textField name="validationRegex"/>
                    </div>
                </div>

                <input name="isHidden" type="hidden" value="false" />

                <button type="submit" class="ui button">
                    <g:message code="default.button.create.label"/>
                </button>
            </g:form>
        </semui:form>

                <table class="ui celled la-table compact table">
                    <thead>
						<tr>
							<th><g:message code="identifierNamespace.ns.label"/></th>
							<th></th>
							<th><g:message code="default.name.label"/> (${currentLang})</th>
							<th><g:message code="default.description.label"/> (${currentLang})</th>
							<th><g:message code="identifierNamespace.family.label"/></th>
							<th><g:message code="default.type.label"/></th>
                            <th><g:message code="identifierNamespace.validationRegex.label"/></th>
                            <th><g:message code="identifierNamespace.urlPrefix.label"/></th>
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
                                        <semui:xEditable owner="${idNs}" field="name_${currentLang}"/>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="description_${currentLang}"/>
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
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="urlPrefix" validation="url"/>
                                    </td>
                                    <%--<td>${fieldValue(bean: idNs, field: "hide")}</td>--%>
                                    <td>
                                        <semui:xEditableBoolean owner="${idNs}" field="isUnique"/>
                                    </td>
                                    <td>
                                        <g:link controller="admin" action="manageNamespaces"
                                                params="${[cmd: 'deleteNamespace', oid: 'com.k_int.kbplus.IdentifierNamespace:' + idNs.id]}" class="ui icon negative button">
                                            <i class="trash alternate icon"></i>
                                        </g:link>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td>
                                        ${fieldValue(bean: idNs, field: "ns")}
                                    </td>
                                    <td>
                                        ${Identifier.countByNs(idNs)}
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="name_${currentLang}"/>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="description_${currentLang}"/>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="family"/>
                                    </td>
                                    <td>
                                        ${fieldValue(bean: idNs, field: "nsType")}
                                    </td>
                                    <td>
                                        ${fieldValue(bean: idNs, field: "validationRegex")}
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${idNs}" field="urlPrefix" validation="url"/>
                                    </td>
                                    <%--<td>${fieldValue(bean: idNs, field: "hide")}</td>--%>
                                    <td>
                                        ${idNs.isUnique}
                                    </td>
                                    <td></td>
                                </g:else>
                            </tr>
						</g:each>
                    </tbody>
                </table>

	</body>
</html>
