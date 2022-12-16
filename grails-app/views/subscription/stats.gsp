<%@ page import="grails.converters.JSON; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.Platform; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report; de.laser.interfaces.CalculatedType" %>
<laser:htmlStart message="subscription.details.stats.label" serviceInjection="true"/>

        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>
        <ui:h1HeaderWithIcon>
            <laser:render template="iconSubscriptionIsChild"/>
            ${subscription.name}
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="stats" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <laser:render template="nav" />

        <ui:objectStatus object="${subscription}" status="${subscription.status}" />
        <laser:render template="message" />
        <ui:messages data="${flash}" />

        <g:if test="${wekbServerUnavailable}">
            <div class="ui icon error message">
                <i class="exclamation icon"></i>
                ${wekbServerUnavailable}
            </div>
        </g:if>
        <g:else>
            <g:each in="${platformInstanceRecords.values()}" var="platformInstanceRecord">
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
                            <g:if test="${platformInstanceRecord.counterR5SushiApiSupported}">
                                <sec:ifAnyGranted roles="ROLE_YODA">
                                    <g:set var="laserPlat" value="${Platform.get(platformInstanceRecord.id)}"/>
                                    <dl>
                                        <dt><g:message code="platform.stats.counter.centralApiKey"/></dt>
                                        <dd>
                                            <ui:xEditable owner="${laserPlat}" field="centralApiKey" overwriteEditable="${true}"/>
                                        </dd>
                                    </dl>
                                </sec:ifAnyGranted>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:each>
        </g:else>
        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <g:if test="${platformInstanceRecords.values().statisticsFormat.contains('COUNTER')}">
                <div class="ui segment">
                    <table class="ui celled table">
                        <tr>
                            <th><g:message code="default.usage.consortiaTableHeader"/></th>
                        </tr>
                        <g:each in="${Subscription.executeQuery('select new map(sub.id as memberSubId, org.sortname as memberName, org.id as memberId) from OrgRole oo join oo.org org join oo.sub sub where sub.instanceOf = :parent and oo.roleType in (:subscrRoles) and exists (select sp.id from SubscriptionPackage sp where sp.subscription = sub) order by org.sortname asc', [parent: subscription, subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}" var="row">
                            <tr>
                                <td>
                                    <g:link action="stats" id="${row.memberSubId}">${row.memberName}</g:link>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
            </g:if>
        </g:if>
        <g:else>
            <ui:filter>
                <g:form action="generateReport" name="stats" class="ui form" method="get">
                    <g:hiddenField name="id" value="${subscription.id}"/>
                    <g:hiddenField name="revision" value="${revision}"/>
                    <div class="four fields">
                        <div class="field">
                            <label for="reportType"><g:message code="default.usage.reportType"/></label>
                            <select name="reportType" id="reportType" class="ui search selection dropdown">
                                <option value=""><g:message code="default.select.choose.label"/></option>
                                <g:each in="${reportTypes}" var="reportType">
                                    <option <%=(params.reportType == reportType) ? 'selected="selected"' : ''%>
                                            value="${reportType}">
                                        <g:message code="default.usage.${reportType}"/>
                                    </option>
                                </g:each>
                                <g:if test="${reportTypes.size() == 0}">
                                    <option value="<g:message code="default.stats.noReport" />"><g:message code="default.stats.noReport" /></option>
                                </g:if>
                            </select>
                        </div>

                        <div class="field">
                            <label for="metricType"><g:message code="default.usage.metricType"/></label>
                            <div id="metricType" class="ui multiple search selection dropdown">
                                <input type="hidden" name="metricType"/>
                                <div class="text"></div>
                                <i class="dropdown icon"></i>
                            </div>
                            <%--<select name="metricType" id="metricType" multiple="multiple" class="ui search selection dropdown">
                                <option value=""><g:message code="default.select.choose.label"/></option>
                                <g:each in="${metricTypes}" var="metricType">
                                    <option <%=(params.list('metricType')?.contains(metricType)) ? 'selected="selected"' : ''%>
                                            value="${metricType}">
                                        ${metricType}
                                    </option>
                                </g:each>
                                <g:if test="${metricTypes.size() == 0}">
                                    <option value="<g:message code="default.stats.noMetric" />"><g:message code="default.stats.noMetric" /></option>
                                </g:if>
                            </select>--%>
                        </div>

                        <%-- postponed for 3.1
                        <div class="field la-field-right-aligned">
                            <input id="generateCostPerUse" type="button" class="ui secondary button" value="${message(code: 'default.stats.generateCostPerUse')}"/>
                        </div>
                        --%>
                        <div class="field la-field-right-aligned">
                            <input type="submit" class="ui primary button" value="${message(code: 'default.stats.generateReport')}"/>
                        </div>
                    </div>
                </g:form>
            </ui:filter>
            <div id="costPerUseWrapper"></div>
        </g:else>
        <laser:script file="${this.getGroovyPageFileName()}">
            $("#reportType").on('change', function() {
                <g:applyCodec encodeAs="none">
                    let platforms = ${platformsJSON};
                </g:applyCodec>
                $.ajax({
                    url: "<g:createLink controller="ajaxJson" action="adjustMetricList"/>",
                    data: {
                        reportType: $(this).val(),
                        platforms: platforms,
                        customer: '${subscription.getSubscriber().globalUID}',
                        subscription: ${subscription.id}
                    }
                }).done(function(response){
                    $('#metricType').dropdown({
                        values: response
                    });
                });
            });
            $("#generateCostPerUse").on('click', function() {
                let fd = new FormData($('#stats')[0]);
                console.log($('#stats')[0]);
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="generateCostPerUse"/>",
                    data: fd,
                    type: 'POST',
                    processData: false,
                    contentType: false
                }).done(function(response){
                    $("#costPerUseWrapper").html(response);
                });
            });
        </laser:script>
<laser:htmlEnd />
