<%@ page import="de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Platform"%>
<div class="ui two doubling stackable cards">
    <div class="ui card">
        <div class="content">
            <h4>
                ${platformInstanceRecord.name}
            </h4>
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
                            ${platformInstanceRecord.statisticsAdminPortalUrl} <a href="${platformInstanceRecord.statisticsAdminPortalUrl}"><i title="${message(code: 'platform.stats.adminURL')} Link" class="external alternate icon"></i></a>
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
                            ${platformInstanceRecord.counterRegistryUrl} <a href="${platformInstanceRecord.counterRegistryUrl}"><i title="${message(code: 'platform.stats.counter.registryURL')} Link" class="external alternate icon"></i></a>
                        </g:if>
                        <g:else>
                            <g:message code="default.url.invalid"/>
                        </g:else>
                    </dd>
                </dl>
            </g:if>
        </div>
    </div>
    <div class="ui card">
        <div class="content">
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
                            ${platformInstanceRecord.counterR4SushiServerUrl} <a href="${platformInstanceRecord.counterR4SushiServerUrl}"><i title="${message(code: 'platform.stats.counter.r4serverURL')} Link" class="external alternate icon"></i></a>
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
                            ${platformInstanceRecord.counterR5SushiServerUrl} <a href="${platformInstanceRecord.counterR5SushiServerUrl}"><i title="${message(code: 'platform.stats.counter.r5serverURL')} Link" class="external alternate icon"></i></a>
                        </g:if>
                        <g:else>
                            ${platformInstanceRecord.counterR5SushiServerUrl}
                        </g:else>
                    </dd>
                </dl>
            </g:if>
        <%-- lastRun and centralApiKey come from LAS:eR, not from we:kb! --%>
            <g:if test="${platformInstanceRecord.lastRun}">
                <dl>
                    <dt><g:message code="platform.stats.counter.lastRun"/></dt>
                    <dd>
                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${platformInstanceRecord.lastRun}"/>
                    </dd>
                </dl>
            </g:if>
            <sec:ifAnyGranted roles="ROLE_YODA">
                <g:set var="laserPlat" value="${Platform.get(platformInstanceRecord.id)}"/>
                <dl>
                    <dt><g:message code="platform.stats.counter.centralApiKey"/></dt>
                    <dd>
                        <ui:xEditable owner="${laserPlat}" field="centralApiKey" overwriteEditable="${true}"/>
                    </dd>
                </dl>
            </sec:ifAnyGranted>
        </div>
    </div>
</div>