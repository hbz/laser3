<%@ page import="grails.converters.JSON; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.Platform; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report; de.laser.interfaces.CalculatedType; de.laser.base.AbstractReport" %>
<laser:htmlStart message="subscription.details.stats.label" serviceInjection="true"/>

        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>
        <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
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
                            <dl>
                                <dt><g:message code="platform.name"/></dt>
                                <dd>${platformInstanceRecord.name} <g:link url="${platformInstanceRecord.wekbUrl}" target="_blank" class="la-popup-tooltip la-delay" data-content="we:kb Link"><i class="ui icon la-gokb"></i></g:link></dd>
                            </dl>
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
            <g:if test="${reportTypes}">
                <g:if test="${revision == AbstractReport.COUNTER_4}">
                    <ui:msg icon="ui info icon" class="info" header="${message(code: 'default.usage.counter4reportInfo.header')}" message="default.usage.counter4reportInfo.text" noClose="true"/>
                </g:if>
                <g:form action="generateReport" name="stats" class="ui form" method="get">
                    <g:hiddenField name="id" value="${subscription.id}"/>
                    <g:hiddenField name="revision" value="${revision}"/>
                    <div class="five fields" id="filterDropdownWrapper">
                        <g:if test="${platformInstanceRecords.size() > 1}">
                            <div class="field">
                                <label for="platform"><g:message code="platform"/></label>
                                <ui:select class="ui search selection dropdown" from="${platformInstanceRecords}" name="platform"/>
                            </div>
                        </g:if>
                        <g:elseif test="${platformInstanceRecords.size() == 1}">
                            <g:hiddenField name="platform" value="${platformInstanceRecords.values()[0].id}"/>
                        </g:elseif>
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
                        <g:if test="${params.reportType}">
                            <laser:render template="/templates/filter/statsFilter"/>
                        </g:if>
                        <%-- reports filters in COUNTER 5 count only for master reports (tr, pr, dr, ir)! COUNTER 4 has no restriction on filter usage afaik --%>
                    </div>
                    <div class="four fields">
                        <div class="field"></div>
                        <div class="field"></div>
                        <div class="field la-field-right-aligned">
                            <%-- deactivated as of ERMS-3996; concept needs to be clarified
                            <input id="generateCostPerUse" type="button" class="ui secondary button" value="${message(code: 'default.stats.generateCostPerUse')}"/>--%>
                            <g:link action="stats" id="${subscription.id}" class="ui button secondary">${message(code:'default.button.reset.label')}</g:link>
                        </div>
                        <div class="field la-field-right-aligned">
                            <input id="generateReport" type="button" class="ui primary button" disabled="disabled" value="${message(code: 'default.stats.generateReport')}"/>
                        </div>
                    </div>
                </g:form>
            </g:if>
            <g:elseif test="${error}">
                <ui:msg icon="ui times icon" class="error" noClose="true">
                    <g:message code="default.stats.error.${error}" args="${errorArgs}"/>
                    <g:if test="${error == 'noCustomerId'}">
                        <%-- proxies are coming!!! --%>
                        <g:if test="${contextOrg.id == subscription.getConsortia()?.id}">
                            <g:link controller="subscription" action="membersSubscriptionsManagement" id="${subscription.instanceOf.id}" params="[tab: 'customerIdentifiers', isSiteReloaded: false]"><g:message code="org.customerIdentifier"/></g:link>
                        </g:if>
                        <g:elseif test="${contextOrg.id == subscription.getSubscriber().id}">
                            <g:link controller="org" action="ids" id="${institution.id}" params="[tab: 'customerIdentifiers']"><g:message code="org.customerIdentifier"/></g:link>
                        </g:elseif>
                    </g:if>
                </ui:msg>
            </g:elseif>
            <div id="reportWrapper"></div>
        </g:else>
        <laser:script file="${this.getGroovyPageFileName()}">
            $("#reportType").on('change', function() {
                <g:applyCodec encodeAs="none">
                    let platforms = ${platformsJSON};
                </g:applyCodec>
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="loadFilterList"/>",
                    data: {
                        reportType: $(this).val(),
                        platforms: platforms,
                        customer: '${subscription.getSubscriber().globalUID}',
                        subscription: ${subscription.id}
                    }
                }).done(function(response) {
                    $('.dynFilter').remove();
                    $('#filterDropdownWrapper').append(response);
                    $('#generateReport').removeAttr('disabled');
                    r2d2.initDynamicUiStuff('#filterDropdownWrapper');
                });
            });
            $("#generateCostPerUse").on('click', function() {
                $('#globalLoadingIndicator').show();
                let fd = new FormData($('#stats')[0]);
                //console.log($('#stats')[0]);
                $.ajax({
                    url: "<g:createLink controller="ajax" action="generateCostPerUse"/>",
                    data: fd,
                    type: 'POST',
                    processData: false,
                    contentType: false
                }).done(function(response){
                    $("#reportWrapper").html(response);
                    $('#globalLoadingIndicator').hide();
                });
            });
            $("#generateReport").on('click', function() {
                $('#globalLoadingIndicator').show();
                let fd = new FormData($('#stats')[0]);
                $.ajax({
                    url: "<g:createLink action="generateReport"/>",
                    data: fd,
                    type: 'POST',
                    processData: false,
                    contentType: false
                }).done(function(response){
                    $("#reportWrapper").html(response);
                    $('#globalLoadingIndicator').hide();
                });
            });
        </laser:script>
<laser:htmlEnd />
