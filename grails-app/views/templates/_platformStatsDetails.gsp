<%@ page import="de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils"%>
<dl>
    <dt><g:message code="platform.stats.format"/></dt>
    <dd>${platformInstanceRecord.statisticsFormat ? RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsFormat, RDConstants.PLATFORM_STATISTICS_FORMAT).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.update"/></dt>
    <dd>${platformInstanceRecord.statisticsUpdate ? RefdataValue.getByValueAndCategory(platformInstanceRecord.statisticsUpdate, RDConstants.PLATFORM_STATISTICS_FREQUENCY).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.adminURL"/></dt>
    <dd>
        <g:if test="${platformInstanceRecord.statisticsAdminPortalUrl}">
            <g:if test="${platformInstanceRecord.statisticsAdminPortalUrl.startsWith('http')}">
                ${platformInstanceRecord.statisticsAdminPortalUrl} <a href="${platformInstanceRecord.statisticsAdminPortalUrl}"><i title="${message(code: 'platform.stats.adminURL')} Link" class="external alternate icon"></i></a>
            </g:if>
            <g:else>
                <g:message code="default.url.invalid"/>
            </g:else>
        </g:if>
        <g:else>
            <g:message code="default.not.available"/>
        </g:else>
    </dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.certified"/></dt>
    <dd>${platformInstanceRecord.counterCertified ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterCertified, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.lastAudit"/></dt>
    <dd>${platformInstanceRecord.lastAuditDate ? formatDate(date: DateUtils.parseDateGeneric(platformInstanceRecord.lastAuditDate), format: message(code: 'default.date.format.notime')) : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.registryURL"/></dt>
    <dd>
        <g:if test="${platformInstanceRecord.counterRegistryUrl}">
            <g:if test="${platformInstanceRecord.counterRegistryUrl.startsWith('http')}">
                ${platformInstanceRecord.counterRegistryUrl} <a href="${platformInstanceRecord.counterRegistryUrl}"><i title="${message(code: 'platform.stats.counter.registryURL')} Link" class="external alternate icon"></i></a>
            </g:if>
            <g:else>
                <g:message code="default.url.invalid"/>
            </g:else>
        </g:if>
        <g:else>
            <g:message code="default.not.available"/>
        </g:else>
    </dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r3supported"/></dt>
    <dd>${platformInstanceRecord.counterR3Supported ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR3Supported, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r4supported"/></dt>
    <dd>${platformInstanceRecord.counterR4Supported ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4Supported, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r5supported"/></dt>
    <dd>${platformInstanceRecord.counterR5Supported ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5Supported, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r4sushi"/></dt>
    <dd>${platformInstanceRecord.counterR4SushiApiSupported ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR4SushiApiSupported, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r5sushi"/></dt>
    <dd>${platformInstanceRecord.counterR5SushiApiSupported ? RefdataValue.getByValueAndCategory(platformInstanceRecord.counterR5SushiApiSupported, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r4serverURL"/></dt>
    <dd>
        <g:if test="${platformInstanceRecord.counterR4SushiServerUrl}">
            <g:if test="${platformInstanceRecord.counterR4SushiServerUrl.startsWith('http')}">
                ${platformInstanceRecord.counterR4SushiServerUrl} <a href="${platformInstanceRecord.counterR4SushiServerUrl}"><i title="${message(code: 'platform.stats.counter.r4serverURL')} Link" class="external alternate icon"></i></a>
            </g:if>
            <g:else>
                ${platformInstanceRecord.counterR4SushiServerUrl}
            </g:else>
        </g:if>
        <g:else>
            <g:message code="default.not.available"/>
        </g:else>
    </dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.r5serverURL"/></dt>
    <dd>
        <g:if test="${platformInstanceRecord.counterR5SushiServerUrl}">
            <g:if test="${platformInstanceRecord.counterR5SushiServerUrl.startsWith('http')}">
                ${platformInstanceRecord.counterR5SushiServerUrl} <a href="${platformInstanceRecord.counterR5SushiServerUrl}"><i title="${message(code: 'platform.stats.counter.r5serverURL')} Link" class="external alternate icon"></i></a>
            </g:if>
            <g:else>
                ${platformInstanceRecord.counterR5SushiServerUrl}
            </g:else>
        </g:if>
        <g:else>
            <g:message code="default.not.available"/>
        </g:else>
    </dd>
</dl>
<dl>
    <dt><g:message code="platform.stats.counter.lastRun"/></dt>
    <dd>
        <%-- lastRun comes from LAS:eR, not from we:kb! --%>
        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${platformInstanceRecord.lastRun}"/>
    </dd>
</dl>