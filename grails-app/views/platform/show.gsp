<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.storage.RDConstants; de.laser.wekb.Platform; de.laser.RefdataValue; de.laser.utils.DateUtils;" %>
<laser:htmlStart message="platform.details" />

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>platformInstance.dateCreated: ${platformInstance.dateCreated}</p>
        <p>platformInstance.lastUpdated: ${platformInstance.lastUpdated}</p>
    </div>
</ui:debugInfo>

    <g:set var="entityName" value="${message(code: 'platform.label')}"/>

<ui:breadcrumbs>
    <g:if test="${isMyPlatform}">
        <ui:crumb controller="myInstitution" action="currentPlatforms" message="menu.my.platforms"/>
    </g:if>
    <g:else>
        <ui:crumb controller="platform" action="list" message="platform.show.all"/>
    </g:else>
    <ui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${platformInstance.name}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyPlatform: isMyPlatform]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="/templates/meta/identifier" model="${[object: platformInstance, editable: false]}"/>

<ui:messages data="${flash}"/>

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'platform.name')}</dt>
                            <dd><ui:xEditable owner="${platformInstance}" field="name" overwriteEditable="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                            <dd>${platformInstance.status.getI10n("value")}</dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'platform.natstat.supplier')}</dt>
                            <dd><ui:xEditable owner="${platformInstance}" field="natstatSupplierID" overwriteEditable="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}"/></dd>
                        </dl>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'platform.provider')}</dt>
                            <dd>
                                <g:if test="${platformInstance.provider}">
                                    <g:link controller="provider" action="show" id="${platformInstance.provider.id}">${platformInstance.provider.name}</g:link>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'platform.primaryURL')}</dt>
                            <dd>
                                <ui:xEditable owner="${platformInstance}" field="primaryUrl" overwriteEditable="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}"/>
                                <g:if test="${platformInstance.primaryUrl}">
                                    <a role="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                                       href="${platformInstance.primaryUrl.startsWith('http') ? platformInstance.primaryUrl : 'http://' + platformInstance.primaryUrl}"
                                       target="_blank"><i class="${Icon.LNK.EXTERNAL}"></i></a>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div>
            </div>
            <div class="ui card">
                <div class="content">
                    <h2 class="ui header">
                        <g:message code="platform.auth.header"/>
                    </h2>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.ip.supported"/></dt>
                        <dd>${platformInstanceRecord.ipAuthentication && RefdataValue.getByValueAndCategory(platformInstanceRecord.ipAuthentication, RDConstants.IP_AUTHENTICATION) ? RefdataValue.getByValueAndCategory(platformInstanceRecord.ipAuthentication, RDConstants.IP_AUTHENTICATION).getI10n("value") : message(code: 'default.not.available')}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.shibboleth.supported"/></dt>
                        <dd>${platformInstanceRecord.shibbolethAuthentication ? RefdataValue.getByValueAndCategory(platformInstanceRecord.shibbolethAuthentication, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.shibboleth.federations"/></dt>
                        <dd>
                            <ul>
                                <g:each in="${platformInstanceRecord.federations}" var="fedRec">
                                    <li>${fedRec.federation}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.userPass.supported"/></dt>
                        <dd>${platformInstanceRecord.passwordAuthentication ? RefdataValue.getByValueAndCategory(platformInstanceRecord.passwordAuthentication, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.other.proxies"/></dt>
                        <dd>${platformInstanceRecord.otherProxies ? RefdataValue.getByValueAndCategory(platformInstanceRecord.otherProxies, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="platform.auth.openathens.supported"/></dt>
                        <dd>${platformInstanceRecord.openAthens ? RefdataValue.getByValueAndCategory(platformInstanceRecord.openAthens, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                    </dl>
                </div>
            </div>
            <div class="ui card">
                <div class="content">
                    <h2 class="ui header">
                        <g:message code="platform.stats.header"/>
                    </h2>
                    <g:if test="${platformInstanceRecord.statisticsFormat}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.format"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsFormat, RDConstants.PLATFORM_STATISTICS_FORMAT).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.statisticsUpdate}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.update"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsUpdate, RDConstants.PLATFORM_STATISTICS_FREQUENCY).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.statisticsAdminPortalUrl}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.adminURL"/></dt>
                            <dd>
                                <g:if test="${platformInstanceRecord.statisticsAdminPortalUrl.startsWith('http')}">
                                    ${platformInstanceRecord.statisticsAdminPortalUrl} <ui:linkWithIcon href="${platformInstanceRecord.statisticsAdminPortalUrl}"/>
                                </g:if>
                                <g:else>
                                    <g:message code="default.url.invalid"/>
                                </g:else>
                            </dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterCertified}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.certified"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterCertified, RDConstants.Y_N).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.lastAuditDate}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.lastAudit"/></dt>
                            <dd>${formatDate(date: DateUtils.parseDateGeneric(platformInstanceRecord.lastAuditDate), format: message(code: 'default.date.format.notime'))}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterRegistryUrl}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.registryURL"/></dt>
                            <dd>
                                <g:if test="${platformInstanceRecord.counterRegistryUrl.startsWith('http')}">
                                    ${platformInstanceRecord.counterRegistryUrl} <ui:linkWithIcon href="${platformInstanceRecord.counterRegistryUrl}"/>
                                </g:if>
                                <g:else>
                                    <g:message code="default.url.invalid"/>
                                </g:else>
                            </dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR4Supported}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r4supported"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4Supported, RDConstants.Y_N).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR5Supported}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r5supported"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5Supported, RDConstants.Y_N).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR4SushiApiSupported}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r4sushi"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4SushiApiSupported, RDConstants.Y_N).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR5SushiApiSupported}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r5sushi"/></dt>
                            <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5SushiApiSupported, RDConstants.Y_N).getI10n("value")}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR4SushiServerUrl}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r4serverURL"/></dt>
                            <dd>
                                <g:if test="${platformInstanceRecord.counterR4SushiServerUrl.startsWith('http')}">
                                    ${platformInstanceRecord.counterR4SushiServerUrl} <ui:linkWithIcon href="${platformInstanceRecord.counterR4SushiServerUrl}"/>
                                </g:if>
                                <g:else>
                                    ${platformInstanceRecord.counterR4SushiServerUrl}
                                </g:else>
                            </dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstanceRecord.counterR5SushiServerUrl}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.stats.counter.r5serverURL"/></dt>
                            <dd>
                                <g:if test="${platformInstanceRecord.counterR5SushiServerUrl.startsWith('http')}">
                                    ${platformInstanceRecord.counterR5SushiServerUrl} <ui:linkWithIcon href="${platformInstanceRecord.counterR5SushiServerUrl}"/>
                                </g:if>
                                <g:else>
                                    ${platformInstanceRecord.counterR5SushiServerUrl}
                                </g:else>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div>

            <div id="accessibility-segment">
                <div class="ui top attached tabular menu">
                    <a class="active item" data-tab="accessibility-1">
                        <g:message code="platform.accessibility.header"/>
                    </a>
                    <a class="item" data-tab="accessibility-2">
                        <g:message code="platform.accessibility.content.header"/>
                    </a>
                    <a class="item" data-tab="accessibility-3">
                        <g:message code="platform.accessibility.accessibilityStatement.header"/>
                    </a>
                </div>
                <div class="ui bottom attached active tab segment" data-tab="accessibility-1">
                    <p>
                        <g:message code="platform.accessibility.link.header"/>
                        <g:link url="https://accessible-eu-centre.ec.europa.eu/content-corner/digital-library/en-3015492021-accessibility-requirements-ict-products-and-services_en"
                                target="_blank">EN 301549 &nbsp;<i class="${Icon.LNK.EXTERNAL}"></i></g:link>
                    </p>
                    <g:each in="['accessPlatform','viewerForPdf','viewerForEpub','playerForAudio','playerForVideo']" var="platformAccessiblityField">
                        <g:if test="${platformInstance[platformAccessiblityField]}">
                            <dl>
                                <dt class="control-label"><g:message code="platform.accessibility.${platformAccessiblityField}"/></dt>
                                <dd>${platformInstance[platformAccessiblityField].getI10n('value')}</dd>
                            </dl>
                        </g:if>
                    </g:each>
                </div>
                <div class="ui bottom attached tab segment" data-tab="accessibility-2">
                    <p>
                        <g:message code="platform.accessibility.link.header"/>
                        <g:link url="https://accessible-eu-centre.ec.europa.eu/content-corner/digital-library/en-3015492021-accessibility-requirements-ict-products-and-services_en"
                                target="_blank">EN 301549 &nbsp;<i class="${Icon.LNK.EXTERNAL}"></i></g:link>
                    </p>
                    <g:each in="['accessEPub', 'accessPdf', 'accessAudio', 'accessVideo', 'accessDatabase']" var="contentAccessibilityField">
                        <g:if test="${platformInstance[contentAccessibilityField]}">
                            <dl>
                                <dt class="control-label"><g:message code="platform.accessibility.${contentAccessibilityField}"/></dt>
                                <dd>${platformInstance[contentAccessibilityField].getI10n('value')}</dd>
                            </dl>
                        </g:if>
                    </g:each>
                </div>
                <div class="ui bottom attached tab segment" data-tab="accessibility-3">
                    <g:if test="${platformInstance.accessibilityStatementAvailable}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.accessibility.accessibilityStatementAvailable"/></dt>
                            <dd>${platformInstance.accessibilityStatementAvailable.getI10n('value')}</dd>
                        </dl>
                    </g:if>
                    <g:if test="${platformInstance.accessibilityStatementUrl}">
                        <dl>
                            <dt class="control-label"><g:message code="platform.accessibility.accessibilityStatement.link"/></dt>
                            <dd>
                                ${platformInstance.accessibilityStatementUrl} <ui:linkWithIcon href="${platformInstance.accessibilityStatementUrl}"/>
                            </dd>
                        </dl>
                    </g:if>
                </div>

                <style>
                    #accessibility-segment > .tab.segment { box-shadow: none; }
                </style>
                <laser:script file="${this.getGroovyPageFileName()}">
                    $(function() { $('#accessibility-segment .menu .item').tab(); });
                </laser:script>
            </div>

            <div id="new-dynamic-properties-block">
                <laser:render template="properties" model="${[ platform: platformInstance ]}"/>
            </div><!-- #new-dynamic-properties-block -->
            <div class="ui card">
                <div class="content">
                    <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                        <h2 class="ui header">
                            <g:message code="accessPoint.label"/>
                        </h2>
                        <laser:render template="/templates/links/accessPointLinksModal"
                                      model="${[tmplText:message(code:'platform.link.accessPoint.button.label'),
                                                tmplID:'addLink',
                                                tmplModalID:'platf_link_ap',
                                                editmode: editable,
                                                accessPointList: accessPointList,
                                                tmplIcon      : 'add',
                                                tmplTooltip   : message(code: 'platform.link.accessPoint.button.label'),
                                      ]}" />
                    </div>
                    <table class="ui three column table">
                        <g:each in="${orgAccessPointList}" var="orgAccessPoint">
                            <tr>
                                <th scope="row" class="control-label">${message(code: 'platform.accessPoint')}</th>
                                <td>
                                    <g:link controller="accessPoint" action="edit_${orgAccessPoint.oap.accessMethod.value.toLowerCase()}"  id="${orgAccessPoint.oap.id}">
                                        ${orgAccessPoint.oap.name}  (${orgAccessPoint.oap.accessMethod.getI10n('value')})
                                    </g:link>
                                </td>
                                <td class="right aligned">
                                <g:if test="${editable}">
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="accessPoint" action="unlinkPlatform" id="${orgAccessPoint.id}"
                                            data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform', args: [orgAccessPoint.oap.name, platformInstance.name])}"
                                            data-confirm-term-how="unlink"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                        <i class="${Icon.CMD.UNLINK}"></i>
                                    </g:link>
                                </g:if>

                                </td>
                            </tr>
                        </g:each>
                    </table>

                </div>
            </div>

            <div class="clearfix"></div>
        </div>
    </div>
</div>

<laser:htmlEnd />
