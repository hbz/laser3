<%@ page import="de.laser.ui.Icon; de.laser.config.ConfigMapper; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.storage.RDConstants" %>
    <g:if test="${wekbServerUnavailable}">
        <ui:msg class="error" showIcon="true" text="${wekbServerUnavailable}" />
    </g:if>
    <g:else>
        <h2 class="ui header">
            <g:message code="platform.stats.data.header"/> <g:link url="${platformInstanceRecord.wekbUrl}" target="_blank" class="la-popup-tooltip" data-content="we:kb Link"><i class="${Icon.WEKB}"></i></g:link>
        </h2>
        <div class="ui grid">
            <div class="eight wide column">
                <g:if test="${platformInstanceRecord.statisticsFormat}">
                    <dl>
                        <dt><g:message code="platform.stats.format"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsFormat, RDConstants.PLATFORM_STATISTICS_FORMAT).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.statisticsUpdate}">
                    <dl>
                        <dt><g:message code="platform.stats.update"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsUpdate, RDConstants.PLATFORM_STATISTICS_FREQUENCY).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.statisticsAdminPortalUrl}">
                    <dl>
                        <dt><g:message code="platform.stats.adminURL"/></dt>
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
                        <dt><g:message code="platform.stats.counter.certified"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterCertified, RDConstants.Y_N).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.lastAuditDate}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.lastAudit"/></dt>
                        <dd>${formatDate(date: DateUtils.parseDateGeneric(platformInstanceRecord.lastAuditDate), format: message(code: 'default.date.format.notime'))}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.counterRegistryUrl}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.registryURL"/></dt>
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
                <g:if test="${platformInstanceRecord.counterRegistryApiUuid}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.registryUUID"/></dt>
                        <dd>
                            ${platformInstanceRecord.counterRegistryApiUuid} <ui:linkWithIcon href="${ConfigMapper.getSushiCounterRegistryUrl()}${platformInstanceRecord.counterRegistryApiUuid}"/>
                        </dd>
                    </dl>
                </g:if>
            </div>
            <div class="eight wide column">
                <g:if test="${platformInstanceRecord.counterR4Supported}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.r4supported"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4Supported, RDConstants.Y_N).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.counterR5Supported}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.r5supported"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5Supported, RDConstants.Y_N).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.counterR4SushiApiSupported}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.r4sushi"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4SushiApiSupported, RDConstants.Y_N).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.counterR5SushiApiSupported}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.r5sushi"/></dt>
                        <dd>${RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5SushiApiSupported, RDConstants.Y_N).getI10n("value")}</dd>
                    </dl>
                </g:if>
                <g:if test="${platformInstanceRecord.counterR4SushiServerUrl}">
                    <dl>
                        <dt><g:message code="platform.stats.counter.r4serverURL"/></dt>
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
                        <dt><g:message code="platform.stats.counter.r5serverURL"/></dt>
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
    </g:else>
