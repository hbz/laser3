<%@ page import="de.laser.ui.CSS; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Identifier; de.laser.IdentifierNamespace; de.laser.I10nTranslation" %>

<laser:htmlStart message="menu.admin.manageIdentifierNamespaces" />

		<ui:breadcrumbs>
			<ui:crumb message="menu.admin" controller="admin" action="index" />
			<ui:crumb message="menu.admin.manageIdentifierNamespaces" class="active"/>
		</ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="menu.admin.manageIdentifierNamespaces" type="admin"/>

		<ui:messages data="${flash}" />

        <ui:errors bean="${identifierNamespaceInstance}" />

        <ui:form controller="admin" action="manageNamespaces">
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
                        <g:select id="nsType" name="nsType" class="ui dropdown clearable"
                                  from="${IdentifierNamespace.AVAILABLE_NSTYPES}"
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

                <button type="submit" class="${Btn.SIMPLE}">
                    <g:message code="default.button.create.label"/>
                </button>
        </ui:form>

        <g:if test="${cmd == 'details'}">

            <g:link controller="admin" action="manageNamespaces" class="${Btn.SIMPLE}"><g:message code="default.button.back"/></g:link>

            <div class="ui fluid card">
                <div class="content">
                    <table class="${CSS.ADMIN_HOVER_TABLE}">
                        <thead>
                            <tr>
                                <th><g:message code="identifierNamespace.detailsStats" args="${[identifierNamespaceInstance.ns]}" /></th>
                            </tr>
                        </thead>
                        <tbody>
                        <g:each in="${detailsStats}" var="list">
                            <g:if test="${list && list.value}">
                                <tr>
                                    <td>${list.key} - <strong>${list.value.size()}</strong> <g:message code="default.matches.label"/></td>
                                </tr>
                            </g:if>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>

            <g:each in="${detailsStats}" var="list">
                <g:if test="${list && list.value}">
                    <g:set var="listSize" value="${list.value.size()}" />
                    <g:set var="list1" value="${listSize > 50 ? list.value.subList(0, Math.ceil(listSize/2).intValue()) : list.value}" />
                    <g:set var="list2" value="${listSize > 50 ? list.value.subList(Math.ceil(listSize/2).intValue(), listSize) : []}" />

                    <div class="ui fluid card">
                        <div class="content">
                            <table class="${CSS.ADMIN_HOVER_TABLE}">
                                <thead>
                                    <tr>
                                        <th colspan="6"><icon:arrow /> ${list.key}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${list1}" var="entry" status="i">
                                        <tr>
                                            <td><span class="ui text grey">${1 + i}.</span></td>
                                            <td><a href="${list.key}/${entry[1]}">${list.key}/${entry[1]}</a></td>
                                            <td>${entry[0]}</td>
                                            <g:if test="${list2[i]}">
                                                <td><span class="ui text grey">${1 + i + list1.size()}.</span></td>
                                                <td><a href="${list.key}/${list2[i][1]}">${list.key}/${list2[i][1]}</a></td>
                                                <td>${list2[i][0]}</td>
                                            </g:if>
                                            <g:else>
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                            </g:else>
                                        </tr>
                                    </g:each>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </g:if>
            </g:each>

        </g:if>
        <g:else>
                <table class="${CSS.ADMIN_TABLE}">
                    <thead>
						<tr>
							<th><g:message code="identifierNamespace.ns.label"/></th>
                            <th><g:message code="default.count.label"/></th>
							<th><g:message code="default.name.label"/> (${currentLang})</th>
							<th><g:message code="default.description.label"/> (${currentLang})</th>
							<th><g:message code="identifierNamespace.family.label"/></th>
							<th><g:message code="default.type.label"/></th>
                            <th><g:message code="identifierNamespace.validationRegex.label"/></th>
                            <th><g:message code="identifierNamespace.urlPrefix.label"/></th>
                            <th><g:message code="identifierNamespace.isFromLaser.label"/></th>
                            <th><g:message code="identifierNamespace.unique.label"/></th>
							<th></th>
						</tr>
                    </thead>
                    <tbody>
						<g:each in="${IdentifierNamespace.where{}.sort('ns')}" var="idNs">
							<tr>
                                <g:if test="${Identifier.countByNs(idNs) == 0}">
                                    <td>
                                        ${idNs.ns}
                                        <g:if test="${!idNs.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.hardData.not.tooltip')}">
                                                <i class="${Icon.PROP.HARDDATA_NOT}"></i>
                                            </span>
                                        </g:if>
                                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.dataId.tooltip', args:[idNs.id])}">
                                            <i class="${Icon.PROP.IN_USE}"></i>
                                        </span>
                                    </td>
                                    <td></td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditable owner="${idNs}" field="name_${currentLang}"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs."name_${currentLang}"}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditable owner="${idNs}" field="description_${currentLang}"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs."description_${currentLang}"}
                                        </g:else>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${idNs}" field="family"/>
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditable owner="${idNs}" field="nsType"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs.nsType}
                                        </g:else>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${idNs}" field="validationRegex"/>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${idNs}" field="urlPrefix" validation="url"/>
                                    </td>
                                    <td>
                                        ${idNs.isFromLaser}
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditableBoolean owner="${idNs}" field="isUnique"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs.isUnique}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <g:link controller="admin" action="manageNamespaces"
                                                    params="${[cmd: 'deleteNamespace', ns: idNs.id]}" class="${Btn.MODERN.NEGATIVE}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="${Icon.CMD.DELETE}"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td>
                                        ${idNs.ns}
                                        <g:if test="${!idNs.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.hardData.not.tooltip')}">
                                                <i class="${Icon.PROP.HARDDATA_NOT}"></i>
                                            </span>
                                        </g:if>
                                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.dataId.tooltip', args:[idNs.id])}">
                                            <i class="${Icon.PROP.IN_USE}"></i>
                                        </span>
                                    </td>
                                    <td>
                                        ${Identifier.countByNs(idNs)}
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditable owner="${idNs}" field="name_${currentLang}"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs."name_${currentLang}"}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!idNs.isHardData}">
                                            <ui:xEditable owner="${idNs}" field="description_${currentLang}"/>
                                        </g:if>
                                        <g:else>
                                            ${idNs."description_${currentLang}"}
                                        </g:else>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${idNs}" field="family"/>
                                    </td>
                                    <td>
                                        ${idNs.nsType}
                                    </td>
                                    <td>
                                        ${idNs.validationRegex}
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${idNs}" field="urlPrefix" validation="url"/>
                                    </td>
                                    <td>
                                        ${idNs.isFromLaser}
                                    </td>
                                    <td>
                                        ${idNs.isUnique}
                                    </td>
                                    <td>
                                        <%
                                            List tooltip = []
                                            globalNamespaceStats.each { e ->
                                                if ( e[1] == idNs.id) {
                                                    if (e[2] > 0) tooltip.add("Verträge: ${e[2]}")
                                                    if (e[3] > 0) tooltip.add("Organisationen: ${e[3]}")
                                                    if (e[4] > 0) tooltip.add("Pakete: ${e[4]}")
                                                    if (e[5] > 0) tooltip.add("Lizenzen: ${e[5]}")
                                                    if (e[6] > 0) tooltip.add("TIPPs: ${e[6]}")
                                                }
                                            }
                                        %>
                                        <g:if test="${tooltip}">
                                            <span data-content="Verwendet für ${tooltip.join(', ')}" data-position="left center"
                                                  class="la-long-tooltip la-popup-tooltip">
                                                <g:link class="${Btn.MODERN.SIMPLE}" controller="admin" action="manageNamespaces"
                                                        params="${[cmd: 'details', ns: idNs.id]}"><i class="${Icon.UI.INFO}"></i></g:link>
                                            </span>
                                        </g:if>
                                    </td>
                                </g:else>
                            </tr>
						</g:each>
                    </tbody>
                </table>
        </g:else>

<laser:htmlEnd />
