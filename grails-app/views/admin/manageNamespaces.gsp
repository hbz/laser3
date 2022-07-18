<%@ page import="de.laser.Identifier; de.laser.IdentifierNamespace; de.laser.I10nTranslation" %>

<laser:htmlStart message="menu.admin.manageIdentifierNamespaces" />

		<ui:breadcrumbs>
			<ui:crumb message="menu.admin" controller="admin" action="index" />
			<ui:crumb message="menu.admin.manageIdentifierNamespaces" class="active"/>
		</ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="menu.admin.manageIdentifierNamespaces" />

		<ui:messages data="${flash}" />

        <ui:errors bean="${identifierNamespaceInstance}" />

        <ui:form message="identifier.namespace.add.label">
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
        </ui:form>

        <g:if test="${cmd == 'details'}">

            <g:link controller="admin" action="manageNamespaces" class="ui button right floated"><g:message code="default.button.back"/></g:link>

            &nbsp;&nbsp;

            <h2 class="ui header"><g:message code="identifierNamespace.detailsStats" args="${[identifierNamespaceInstance.ns]}" /></h2>

            <g:each in="${detailsStats}" var="list">
                <g:if test="${list && list.value}">
                    <p><strong>${list.key} - ${list.value.size()} <g:message code="default.matches.label"/></strong></p>
                </g:if>
            </g:each>

            &nbsp;

            <g:each in="${detailsStats}" var="list">
                <g:if test="${list && list.value}">
                    <p><strong><i class="ui icon angle right"></i> ${list.key}</strong></p>
                    <div class="ui list">
                        <g:each in="${list.value}" var="entry" status="i">
                            <div class="item" <%= ((i+1)%10)==0 ? 'style="margin-bottom:1.2em"':''%>>
                                ${entry[0]}
                                &nbsp;&nbsp;&nbsp;&nbsp; &rarr; &nbsp;&nbsp;&nbsp;&nbsp;
                                <a href="${list.key}/${entry[1]}">${list.key}/${entry[1]}</a>
                            </div>
                        </g:each>
                    </div>
                </g:if>
            </g:each>
        </g:if>
        <g:else>
                <table class="ui celled la-js-responsive-table la-table compact table">
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
                                        <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.dataId.tooltip', args:[idNs.id])}">
                                            <i class="info circle icon blue"></i>
                                        </span>
                                        <g:if test="${idNs.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>
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
                                                    params="${[cmd: 'deleteNamespace', oid: IdentifierNamespace.class.name + ':' + idNs.id]}" class="ui icon negative button  la-modern-button"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td>
                                        ${idNs.ns}
                                        <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.dataId.tooltip', args:[idNs.id])}">
                                            <i class="info circle icon blue"></i>
                                        </span>
                                        <g:if test="${idNs.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>
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
                                                  class="la-long-tooltip la-popup-tooltip la-delay">
                                                <g:link class="ui button icon" controller="admin" action="manageNamespaces"
                                                        params="${[cmd: 'details', oid: IdentifierNamespace.class.name + ':' + idNs.id]}"><i class="ui icon question"></i></g:link>
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
