<%@ page import="de.laser.wekb.TitleInstancePackagePlatform; de.laser.wekb.Package; de.laser.wekb.Platform; de.laser.wekb.Provider; de.laser.wekb.Vendor; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Identifier; de.laser.Subscription; de.laser.License; de.laser.storage.RDStore; de.laser.IdentifierNamespace; de.laser.IssueEntitlement; de.laser.I10nTranslation; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection />

<table class="ui table">
<thead>
    <tr>
    <th>${message(code: 'identifier.namespace.label')}</th>
    <th>${message(code: 'default.identifier.label')}</th>

    <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
        <th>${message(code: 'default.note.label')}</th>
        <th class="center aligned">
            <ui:optionsIcon />
        </th>
    </g:if>
    </tr>
</thead>
<tbody>
    <g:each in="${objectIds}" var="row">
        <g:set var="namespace" value="${row.getKey()}"/>
        <g:each in="${row.getValue()}" var="ident">
            <tr>
                <td>
                    ${namespace}
                    <g:if test="${ident instanceof Identifier && ident.ns.getI10n('description')}">
                        <span data-position="top right" class="la-popup-tooltip" data-content="${ident.ns.getI10n('description')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </g:if>
                </td>
                <td class="column js-copyTriggerParent">
                    <g:if test="${ident instanceof Identifier}">
                        <g:if test="${!ident.instanceOf}">
                            <ui:xEditable owner="${ident}" field="value"/>
                        </g:if>
                        <g:else>
                            <span class="js-copyTrigger js-copyTopic la-popup-tooltip"
                                      data-position="top right" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                                    <i class="la-copy grey icon la-js-copyTriggerIcon" aria-hidden="true"></i>
                                    <span class="js-copyTopic">${ident.value}</span>
                            </span>
                        </g:else>
                        %{--
                        <g:if test="${ident.ns.urlPrefix}">
                            <a target="_blank" href="${ident.ns.urlPrefix}${ident.value}"><i title="${ident.ns.getI10n('name')} Link" class="${Icon.LNK.EXTERNAL}"></i></a>
                        </g:if>
                        --}%
                        <g:if test="${ident.getURL()}">
                            <ui:linkWithIcon href="${ident.getURL()}" />
                        </g:if>
                    </g:if>
                    <g:else>
                        <span class="js-copyTrigger la-popup-tooltip"
                                      data-position="top right" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                            <i class="la-copy grey icon la-js-copyTriggerIcon" aria-hidden="true"></i>
                            <span class="js-copyTopic">${ident}</span>
                        </span>
                        <g:if test="${!objIsOrgAndInst && object.hasProperty("gokbId") && ident == object.gokbId}">
                            <g:if test="${object instanceof Package}">
                                <ui:wekbIconLink type="package" gokbId="${object.gokbId}"/>
                            </g:if>
                            <g:elseif test="${object instanceof Platform}">
                                <ui:wekbIconLink type="platform" gokbId="${object.gokbId}"/>
                            </g:elseif>
                            <g:elseif test="${object instanceof Provider}">
                                <ui:wekbIconLink type="provider" gokbId="${object.gokbId}"/>
                            </g:elseif>
                            <g:elseif test="${object instanceof TitleInstancePackagePlatform}">
                                <ui:wekbIconLink type="tipp" gokbId="${object.gokbId}"/>
                            </g:elseif>
                            <g:elseif test="${object instanceof Vendor}">
                                <ui:wekbIconLink type="vendor" gokbId="${object.gokbId}"/>
                            </g:elseif>
                        </g:if>
                    </g:else>
                </td>
                <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
                    <td class="column">
                        <g:if test="${ident instanceof Identifier}">
                            <g:if test="${!ident.instanceOf}">
                                <ui:xEditable owner="${ident}" field="note"/>
                            </g:if>
                            <g:else>
                                ${ident.note}
                            </g:else>
                        </g:if>
                    </td>
                    <td class="column">
                        <g:if test="${editable && ident instanceof Identifier}">
                            <g:if test="${(object instanceof Subscription || object instanceof License)}">
                                <g:if test="${showConsortiaFunctions}">
                                    <g:if test="${!ident.instanceOf}">
                                        <g:if test="${! AuditConfig.getConfig(ident)}">
                                            <ui:remoteLink class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                              controller="ajax"
                                                              action="toggleIdentifierAuditConfig"
                                                              params='[ownerId: "${object.id}",
                                                                       ownerClass: "${object.class}",
                                                                       showConsortiaFunctions: true,
                                                                       (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                              ]'
                                                              data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [ident.value])}"
                                                              data-confirm-term-how="inherit"
                                                              id="${ident.id}"
                                                              data-content="${message(code:'property.audit.off.tooltip')}"
                                                              data-update="objIdentifierPanel"
                                                              role="button"
                                            >
                                                <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                            </ui:remoteLink>
                                            <g:link controller="ajax" action="deleteIdentifier" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                    params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [ident.ns.ns+": "+ident.value])}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="${Icon.CMD.DELETE}"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <ui:remoteLink class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                              controller="ajax" action="toggleIdentifierAuditConfig"
                                                              params='[ownerId: "${object.id}",
                                                                       ownerClass: "${object.class}",
                                                                       showConsortiaFunctions: true,
                                                                       (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                              ]'
                                                              id="${ident.id}"
                                                              data-content="${message(code:'property.audit.on.tooltip')}"
                                                              data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [ident.value])}"
                                                              data-confirm-term-how="inherit"
                                                              data-update="objIdentifierPanel"
                                                              role="button"
                                            >
                                                <i class="${Icon.SIG.INHERITANCE}"></i>
                                            </ui:remoteLink>
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <g:link controller="ajax" action="deleteIdentifier" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [ident.ns.ns+": "+ident.value])}"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="${Icon.CMD.DELETE}"></i>
                                        </g:link>
                                    </g:else>
                                </g:if>
                                <g:elseif test="${ident.instanceOf}">
                                    <ui:auditIcon type="auto" />
                                </g:elseif>
                                <g:else>
                                    <g:link controller="ajax" action="deleteIdentifier" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [ident.ns.ns+": "+ident.value])}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:link controller="ajax" action="deleteIdentifier" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [ident.ns.ns+": "+ident.value])}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:else>
                        </g:if>
                        <g:elseif test="${ident instanceof Identifier && ident.instanceOf}">
                            <ui:auditIcon type="auto" />
                        </g:elseif>
                    </td>
                </g:if><%-- hidden if org[type=institution] --%>
            </tr>
        </g:each>
    </g:each>
</tbody>
</table>