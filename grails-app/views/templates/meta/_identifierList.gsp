<%@ page import="de.laser.Identifier; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.IdentifierNamespace; de.laser.Package; de.laser.TitleInstancePackagePlatform; de.laser.IssueEntitlement; de.laser.I10nTranslation; de.laser.Platform; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection />
<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 27.05.2021
  Time: 15:13
--%>
<g:set var="wekbAPI" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>

<div class="ui three column grid">
    <div class="ui header row">
        <div class="column">${message(code: 'identifier.namespace.label')}</div>
        <div class="column">${message(code: 'default.identifier.label')}</div>
        <g:if test="${editable}">
            <div class="column">${message(code: 'default.actions.label')}</div>
        </g:if>
        <g:else>
            <div class="column"></div>
        </g:else>
    </div>
    <g:each in="${objectIds}" var="row">
        <g:set var="namespace" value="${row.getKey()}"/>
        <g:each in="${row.getValue()}" var="ident">
            <div class="ui row">
                <div class="column">
                    ${namespace}
                    <g:if test="${ident instanceof Identifier && ident.ns.getI10n('description')}">
                        <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${ident.ns.getI10n('description')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </div>
                <div class="column">
                    <g:if test="${ident instanceof Identifier}">
                        ${ident.value}
                        <g:if test="${ident.ns.urlPrefix}"><a target="_blank" href="${ident.ns.urlPrefix}${ident.value}"><i title="${ident.ns.getI10n('name')} Link" class="external alternate icon"></i></a></g:if>
                    </g:if>
                    <g:else>
                        ${ident}
                        <g:if test="${!objIsOrgAndInst && object.hasProperty("gokbId") && ident == object.gokbId}">
                            <g:if test="${object instanceof Package}">
                                <a target="_blank"
                                   href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/packageContent/' + ident : '#'}"><i
                                        title="${wekbAPI.name} Link" class="external alternate icon"></i></a>
                            </g:if>
                            <g:elseif test="${object instanceof TitleInstancePackagePlatform}">
                                <a target="_blank"
                                   href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/tippContent/' + ident : '#'}"><i
                                        title="${wekbAPI.name} Link" class="external alternate icon"></i></a>
                            </g:elseif>
                            <g:elseif test="${object instanceof Platform}">
                                <a target="_blank"
                                   href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/platformContent/' + ident : '#'}"><i
                                        title="${wekbAPI.name} Link" class="external alternate icon"></i></a>
                            </g:elseif>
                        </g:if>
                    </g:else>
                </div>
                <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
                    <div class="column">
                        <g:if test="${editable && ident instanceof Identifier}">
                            <g:if test="${(object instanceof Subscription || object instanceof License) && showConsortiaFunctions}">
                                <g:if test="${!ident.instanceOf}">
                                    <g:if test="${! AuditConfig.getConfig(ident)}">
                                        <laser:remoteLink class="ui mini icon button la-popup-tooltip la-delay js-open-confirm-modal"
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
                                            <i class="icon la-thumbtack slash la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                        <g:link controller="ajax" action="deleteIdentifier" class="ui icon negative mini button"
                                                params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="icon trash alternate"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <laser:remoteLink class="ui mini icon green button la-popup-tooltip la-delay js-open-confirm-modal"
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
                                            <i class="thumbtack icon la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:link controller="ajax" action="deleteIdentifier" class="ui icon negative mini button"
                                            params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="icon trash alternate"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                            <g:elseif test="${ident.instanceOf}">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon thumbtack blue"></i></span>
                            </g:elseif>
                        </g:if>
                        <g:elseif test="${ident instanceof Identifier && ident.instanceOf}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon thumbtack blue"></i></span>
                        </g:elseif>
                    </div>
                </g:if><%-- hidden if org[type=institution] --%>
            </div>
        </g:each>
    </g:each>
</div>