<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.config.ConfigMapper; de.laser.storage.RDStore; de.laser.storage.RDConstants;de.laser.wekb.Package;de.laser.RefdataValue;org.springframework.web.servlet.support.RequestContextUtils; de.laser.Org; de.laser.wekb.Platform; java.text.SimpleDateFormat; de.laser.addressbook.PersonRole; de.laser.addressbook.Contact" %>
<laser:htmlStart message="package.details" />

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>packageInstance.dateCreated: ${packageInstance.dateCreated}</p>
        <p>packageInstance.lastUpdated: ${packageInstance.lastUpdated}</p>
    </div>
</ui:debugInfo>

<g:set var="locale" value="${RequestContextUtils.getLocale(request)}"/>

<ui:breadcrumbs>
    <ui:crumb controller="package" action="index" message="package.show.all"/>
    <ui:crumb class="active" text="${packageInstance.name}"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon>
    <g:if test="${editable}"><span id="packageNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${packageInstance.class.name}:${packageInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax"
                                                           action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
    <g:else>${packageInstance.name}</g:else>
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyPkg: isMyPkg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<ui:objectStatus object="${packageInstance}" />

<laser:render template="/templates/meta/identifier" model="${[object: packageInstance, editable: false]}"/>

<ui:messages data="${flash}"/>

<ui:errors bean="${packageInstance}"/>

<g:if test="${packageInstanceRecord}">
    <div class="ui stackable grid">

        <div class="eleven wide column">
            <div class="la-inline-lists">
                <div class="ui two doubling stackable cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'default.status.label')}</dt>
                                <dd>${packageInstance.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.show.altname')}</dt>
                                <dd>
                                    <div class="ui list">
                                        <g:each in="${packageInstanceRecord.altname}" var="altname">
                                            <div class="item">
                                                <i class="${Icon.SYM.ALTNAME} grey"></i>
                                                <div class="content">${altname}</div>
                                            </div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.curatoryGroup.label')}</dt>
                                <dd>
                                    <div class="ui list">
                                        <g:each in="${packageInstanceRecord.curatoryGroups}" var="curatoryGroup">
                                            <div class="item">
                                                <div class="content">
                                                    ${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}
                                                    <ui:wekbIconLink type="curatoryGroup" gokbId="${curatoryGroup.curatoryGroup}"/>
                                                </div>
                                            </div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.lastUpdated.label')}</dt>
                                <dd>
                                    <g:if test="${packageInstanceRecord.lastUpdatedDisplay}">
                                        <g:formatDate formatName="default.date.format.notime"
                                                      date="${DateUtils.parseDateGeneric(packageInstanceRecord.lastUpdatedDisplay)}"/>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.breakable')}</dt>
                                <dd>${packageInstanceRecord.breakable ? RefdataValue.getByValueAndCategory(packageInstanceRecord.breakable, RDConstants.PACKAGE_BREAKABLE).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.contentType.label')}</dt>
                                <dd>${packageInstance.contentType?.getI10n("value")}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.file')}</dt>
                                <dd>${packageInstance.file?.getI10n("value")}</dd>
                            </dl>
                        </div>
                    </div>

                    <div class="ui card">
                        <div class="content">
                            <%--<dl>
                                <dt>${message(code: 'package.consistent')}</dt>
                                <dd>${packageInstanceRecord.consistent ? RefdataValue.getByValueAndCategory(packageInstanceRecord.consistent, RDConstants.PACKAGE_CONSISTENT).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>--%>
                            <dl>
                                <dt>${message(code: 'package.scope.label')}</dt>
                                <dd>
                                    ${packageInstanceRecord.scope ? RefdataValue.getByValueAndCategory(packageInstanceRecord.scope, RDConstants.PACKAGE_SCOPE).getI10n("value") : message(code: 'default.not.available')}
                                    <g:if test="${packageInstanceRecord.scope == RDStore.PACKAGE_SCOPE_NATIONAL.value}">
                                        <dl>
                                            <dt>${message(code: 'package.nationalRange.label')}</dt>
                                            <g:if test="${packageInstanceRecord.nationalRanges}">
                                                <dd>
                                                    <div class="ui bulleted list">
                                                        <g:each in="${packageInstanceRecord.nationalRanges}" var="nr">
                                                            <div class="item">${RefdataValue.getByValueAndCategory(nr.value,RDConstants.COUNTRY) ? RefdataValue.getByValueAndCategory(nr.value,RDConstants.COUNTRY).getI10n('value') : nr}</div>
                                                        </g:each>
                                                    </div>
                                                </dd>
                                            </g:if>
                                        </dl>
                                        <dl>
                                            <dt>${message(code: 'package.regionalRange.label')}</dt>
                                            <g:if test="${packageInstanceRecord.regionalRanges}">
                                                <dd>
                                                    <div class="ui bulleted list">
                                                        <g:each in="${packageInstanceRecord.regionalRanges}" var="rr">
                                                            <div class="item">${RefdataValue.getByValueAndCategory(rr.value,RDConstants.REGIONS_DE) ? RefdataValue.getByValueAndCategory(rr.value,RDConstants.REGIONS_DE).getI10n('value') : rr}</div>
                                                        </g:each>
                                                    </div>
                                                </dd>
                                            </g:if>
                                        </dl>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.paymentType.label')}</dt>
                                <dd>${packageInstanceRecord.paymentType ? RefdataValue.getByValueAndCategory(packageInstanceRecord.paymentType,RDConstants.PAYMENT_TYPE).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.openAccess.label')}</dt>
                                <dd>${packageInstanceRecord.openAccess ? RefdataValue.getByValueAndCategory(packageInstanceRecord.openAccess, RDConstants.LICENSE_OA_TYPE)?.getI10n("value") : RDStore.LICENSE_OA_TYPE_EMPTY.getI10n("value")}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.freeTrial.label')}</dt>
                                <dd>${packageInstanceRecord.freeTrial ? RefdataValue.getByValueAndCategory(packageInstanceRecord.freeTrial,RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.freeTrialPhase.label')}</dt>
                                <dd>${packageInstanceRecord.freeTrialPhase ?: message(code: 'default.not.available')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.ddc.label')}</dt>
                                <dd>
                                    <div class="ui list">
                                        <g:each in="${packageInstanceRecord.ddcs}" var="ddc">
                                            <div class="item">
                                                <i class="sitemap grey icon"></i>
                                                <div class="content">${RefdataValue.getByValueAndCategory(ddc.value,RDConstants.DDC) ? RefdataValue.getByValueAndCategory(ddc.value,RDConstants.DDC).getI10n('value') : message(code:'package.ddc.invalid')}</div>
                                            </div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.archivingAgency.label')}</dt>
                                <dd>
                                    <div class="ui list">
                                        <g:each in="${packageInstanceRecord.packageArchivingAgencies}" var="arcAgency">
                                            <div class="item">
                                                <i class="archive grey icon"></i>
                                                <div class="content">
                                                    <div class="header">${arcAgency.archivingAgency ? RefdataValue.getByValueAndCategory(arcAgency.archivingAgency, RDConstants.ARCHIVING_AGENCY).getI10n("value") : message(code: 'package.archivingAgency.invalid')}</div>
                                                    <div class="description">${message(code: 'package.archivingAgency.openAccess.label')}: ${arcAgency.openAccess ? RefdataValue.getByValueAndCategory(arcAgency.openAccess, RDConstants.Y_N_P).getI10n("value") : ""}</div>
                                                    <div class="description">${message(code: 'package.archivingAgency.postCancellationAccess.label')}: ${arcAgency.postCancellationAccess ? RefdataValue.getByValueAndCategory(arcAgency.postCancellationAccess, RDConstants.Y_N_P).getI10n("value") : ""}</div>
                                                </div>
                                            </div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header">${message(code: 'package.source.label')}</h2>
                        <g:if test="${packageInstanceRecord?.source}">
                            <div class="ui accordion la-accordion-showMore">
                                <div class="ui raised segments la-accordion-segments">
                                    <div class="ui fluid segment title">
                                        <ui:wekbIconLink type="source" gokbId="${packageInstanceRecord.source.uuid}"/>
                                        ${packageInstanceRecord.source.name}
                                        <div class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                             data-content="${message(code: 'platform.details')}">
                                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                                        </div>
                                    </div>
                                    <div class="ui fluid segment content">
                                        <dl>
                                            <dt><g:message code="package.source.url.label"/></dt>
                                            <dd>
                                                <g:if test="${packageInstanceRecord.source.url}">
                                                    ${packageInstanceRecord.source.url} <ui:linkWithIcon target="_blank" href="${packageInstanceRecord.source.url}"/>
                                                </g:if>
                                            </dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="package.source.frequency"/></dt>
                                            <dd>${packageInstanceRecord.source.frequency ? RefdataValue.getByValueAndCategory(packageInstanceRecord.source.frequency, RDConstants.PLATFORM_STATISTICS_FREQUENCY)?.getI10n('value') : packageInstanceRecord.source.frequency}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="package.source.automaticUpdates"/></dt>
                                            <dd>${Boolean.valueOf(packageInstanceRecord.source.automaticUpdates) ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="package.source.lastRun"/></dt>
                                            <dd>
                                                <g:if test="${packageInstanceRecord.source.lastRun}">
                                                    <g:set var="sourceLastRun" value="${DateUtils.parseDateGeneric(packageInstanceRecord.source.lastRun)}"/>
                                                    <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${sourceLastRun}"/>
                                                </g:if>
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header">${message(code: 'platform.label')}</h2>
                        <g:if test="${platformInstanceRecord}">
                            <div class="ui accordion la-accordion-showMore">
                                <div class="ui raised segments la-accordion-segments">
                                    <div class="ui fluid segment title">
                                        <div class="ui grid">
                                            <div class="equal width row">
                                                <div class="column">
                                                    <g:link controller="platform" action="show" id="${platformInstanceRecord.id}">${platformInstanceRecord.name}</g:link>

                                                    <g:if test="${platformInstanceRecord.primaryUrl}">
                                                        <ui:linkWithIcon href="${platformInstanceRecord.primaryUrl?.startsWith('http') ? platformInstanceRecord.primaryUrl : 'http://' + platformInstanceRecord.primaryUrl}"/>
                                                    </g:if>
                                                    <ui:wekbIconLink type="platform" gokbId="${platformInstanceRecord.uuid}"/>
                                                </div>
                                                <div class="right aligned column">
                                                    <div class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                                         data-content="${message(code: 'platform.details')}">
                                                        <i class="${Icon.CMD.SHOW_MORE}"></i>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="ui fluid segment content">
                                        <dl>
                                            <dt>${message(code: 'default.status.label')}</dt>
                                            <dd>${platformInstanceRecord.status.getI10n("value")}</dd>
                                        </dl>
                                        <dl>
                                            <dt>${message(code: 'platform.provider')}</dt>
                                            <dd>
                                                <g:if test="${platformInstanceRecord.provider}">
                                                    <g:link controller="provider" action="show" id="${platformInstanceRecord.provider.id}">${platformInstanceRecord.provider.name}</g:link>
                                                    <g:if test="${platformInstanceRecord.provider.homepage}">
                                                        <ui:linkWithIcon href="${platformInstanceRecord.provider.homepage.startsWith('http') ? platformInstanceRecord.provider.homepage : 'http://' + platformInstanceRecord.provider.homepage}"/>
                                                    </g:if>
                                                    <ui:wekbIconLink type="provider" gokbId="${platformInstanceRecord.provider.gokbId}"/>
                                                </g:if>
                                            </dd>
                                        </dl>
                                        <h3 class="ui header">
                                            <g:message code="platform.auth.header"/>
                                        </h3>
                                        <dl>
                                            <dt><g:message code="platform.auth.ip.supported"/></dt>
                                            <dd>${platformInstanceRecord.ipAuthentication && RefdataValue.getByValueAndCategory(platformInstanceRecord.ipAuthentication, RDConstants.IP_AUTHENTICATION) ? RefdataValue.getByValueAndCategory(platformInstanceRecord.ipAuthentication, RDConstants.IP_AUTHENTICATION).getI10n("value") : message(code: 'default.not.available')}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="platform.auth.shibboleth.supported"/></dt>
                                            <dd>${platformInstanceRecord.shibbolethAuthentication ? RefdataValue.getByValueAndCategory(platformInstanceRecord.shibbolethAuthentication, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="platform.auth.shibboleth.federations"/></dt>
                                            <dd>
                                                <ul>
                                                    <g:each in="${platformInstanceRecord.federations}" var="fedRec">
                                                        <li>${fedRec.federation}</li>
                                                    </g:each>
                                                </ul>
                                            </dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="platform.auth.userPass.supported"/></dt>
                                            <dd>${platformInstanceRecord.passwordAuthentication ? RefdataValue.getByValueAndCategory(platformInstanceRecord.passwordAuthentication, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="platform.auth.other.proxies"/></dt>
                                            <dd>${platformInstanceRecord.otherProxies ? RefdataValue.getByValueAndCategory(platformInstanceRecord.otherProxies, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                                        </dl>
                                        <dl>
                                            <dt><g:message code="platform.auth.openathens.supported"/></dt>
                                            <dd>${platformInstanceRecord.openAthens ? RefdataValue.getByValueAndCategory(platformInstanceRecord.openAthens, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                                        </dl>
                                        <h3 class="ui header">
                                            <g:message code="platform.stats.header"/>
                                        </h3>
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
                            </div>
                        </g:if>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'default.description.label')}</dt>
                            <dd>
                                <g:if test="${packageInstanceRecord.description}">
                                    ${packageInstanceRecord.description}
                                </g:if>
                            </dd>
                        </dl>
                        <g:if test="${packageInstanceRecord.descriptionURL}">
                            <dl>
                                <dt>${message(code: 'default.url.label')}</dt>
                                <dd>
                                    ${packageInstanceRecord.descriptionURL}
                                    <ui:linkWithIcon
                                            href="${packageInstanceRecord.descriptionURL.startsWith('http') ? packageInstanceRecord.descriptionURL : 'http://' + packageInstanceRecord.descriptionURL}"/>
                                </dd>
                            </dl>
                        </g:if>
                        %{--<dl>
                            <dt>${message(code: 'package.breakable')}</dt>
                            <dd>
                                ${packageInstance.breakable}
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'package.consistent')}</dt>
                            <dd>
                                ${packageInstance.consistent}
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'package.fixed')}</dt>
                            <dd>
                                ${packageInstance.fixed}
                            </dd>
                        </dl>
    --}%
                        <%-- deactivated U.F.N. - do not delete as prespectively needed!
                        <g:if test="${statsWibid && packageIdentifier}">
                            <dl>
                                <dt><g:message code="package.show.usage"/></dt>
                                <dd>
                                    <ui:statsLink class="ui basic negative"
                                                     base="${ConfigMapper.getStatsApiUrl()}"
                                                     module="statistics"
                                                     controller="default"
                                                     action="select"
                                                     target="_blank"
                                                     params="[mode        : usageMode,
                                                              packages    : packageInstance.getIdentifierByType('isil').value,
                                                              institutions: statsWibid
                                                     ]"
                                                     title="${message(code: 'default.jumpToNatStat')}">
                                        <i class="${Icon.STATS}"></i>
                                    </ui:statsLink>
                                </dd>
                            </dl>
                        </g:if>
                        --%>
                    </div>
                </div>
            </div>
        </div><!-- .eleven -->
        <aside class="five wide column la-sidekick">
            <div class="ui one cards">
                <div id="container-provider">
                    <div class="ui card">
                        <div class="content">
                            <h2 class="ui header">${message(code: 'provider.label')}</h2>
                            <laser:render template="/templates/links/providerLinksAsList"
                                          model="${[providerRoles: [packageInstance],
                                                    roleObject   : packageInstance,
                                                    roleRespValue: RDStore.PRS_RESP_SPEC_PKG_EDITOR.value,
                                                    showPersons  : true
                                          ]}"/>
                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <h2 class="ui header">${message(code: 'vendor.label')}</h2>
                            <laser:render template="/templates/links/vendorLinksAsList"
                                          model="${[vendorRoles  : packageInstance.vendors,
                                                    roleObject   : packageInstance,
                                                    roleRespValue: RDStore.PRS_RESP_SPEC_PKG_EDITOR.value,
                                                    showPersons  : true
                                          ]}"/>
                        </div>
                    </div>
                    <g:if test="${gascoContacts}">
                        <div class="ui card">
                            <div class="content">
                                <h2 class="ui header">${message(code: 'gasco.contacts.plural')}</h2>
                                <table class="ui compact table">
                                    <g:each in="${gascoContacts}" var="entry">
                                        <g:set var="gascoContact" value="${entry.getValue()}"/>
                                        <g:each in ="${gascoContact.personRoles}" var="personRole">
                                            <g:set var="person" value="${personRole.getPrs()}" />
                                            <g:if test="${person.isPublic}">
                                                <tr>
                                                    <td>
                                                        <span class="la-flexbox la-minor-object">
                                                            <i class="${Icon.AUTH.ORG_CONSORTIUM} la-list-icon la-popup-tooltip" data-content="${message(code: 'gasco.filter.consortialAuthority')}"></i><g:link target="_blank" controller="organisation" action="show" id="${personRole.org.id}">${gascoContact.orgDisplay}</g:link>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <div class="ui segment la-timeLineSegment-contact">
                                                            <div class="la-timeLineGrid">
                                                                <div class="ui grid">
                                                                    <div class="row">
                                                                        <div class="two wide column">
                                                                            <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_URL)}" var="prsContact">
                                                                                <a class="la-break-all" href="${prsContact?.content}" target="_blank"><i class="circular large globe icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${RDStore.PRS_FUNC_GASCO_CONTACT.getI10n('value')}"></i></a>
                                                                            </g:each>
                                                                        </div>
                                                                        <div class="twelve wide column">
                                                                            <div class="ui label">${RDStore.PRS_FUNC_GASCO_CONTACT.getI10n('value')}</div>
                                                                            <div class="ui header">${person?.getFirst_name()} ${person?.getLast_name()}</div>
                                                                            <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_EMAIL)}" var="prsContact">
                                                                                <laser:render template="/addressbook/contact" model="${[
                                                                                        contact             : prsContact,
                                                                                        tmplShowDeleteButton: false,
                                                                                        overwriteEditable   : false
                                                                                ]}" />
                                                                            <%--<div class="js-copyTriggerParent">
                                                                                <i class="${Icon.SYM.EMAIL} la-list-icon js-copyTrigger"></i>
                                                                                <span class="la-popup-tooltip" data-position="right center " data-content="Mail senden an ${person?.getFirst_name()} ${person?.getLast_name()}">
                                                                                    <a class="la-break-all js-copyTopic" href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
                                                                                </span>
                                                                            </div>--%>
                                                                            </g:each>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </g:if>
                                        </g:each>
                                    </g:each>
                                </table>
                            </div>
                        </div>
                    </g:if>
                </div>
            </div>
        </aside>

    </div><!-- .grid -->
</g:if>

<laser:htmlEnd />
