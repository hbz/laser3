<%@ page import="grails.converters.JSON; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.Platform; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report; de.laser.interfaces.CalculatedType; de.laser.base.AbstractReport" %>
<laser:htmlStart message="subscription.details.stats.label" serviceInjection="true"/>

        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleOrgRelations="${visibleOrgRelations}">
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
            <g:if test="${platformInstanceRecords.values().statisticsFormat.contains('COUNTER')}">
                <ui:tabs>
                    <g:each in="${platformInstanceRecords.values()}" var="platform">
                        <ui:tabsItem controller="subscription" action="stats" tab="${platform.id.toString()}"
                                     params="${params + [tab: platform.id]}" text="${platform.name}"/>
                    </g:each>
                </ui:tabs>
                <div class="ui bottom attached tab active segment" id="customerIdWrapper">
                    <table class="ui la-js-responsive-table la-table table">
                        <thead>
                        <tr>
                            <th class="three wide">${message(code: 'consortium.member')}</th>
                            <th class="four wide">${message(code: 'default.provider.label')} : ${message(code: 'platform.label')}</th>
                            <th class="three wide">${message(code: 'org.customerIdentifier')}</th>
                            <th class="three wide">${message(code: 'org.requestorKey')}</th>
                            <th class="two wide">${message(code: 'default.note.label')}</th>
                            <th class="one wide">${message(code: 'default.actions')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${keyPairs}" var="pair" status="rowno">
                        %{-- TODO: erms-5495 --}%
                        %{--                <g:set var="overwriteEditable_ci" value="${editable}" />--}%
                            <%
                                boolean overwriteEditable_ci = contextService.getUser().isAdmin() ||
                                        userService.hasFormalAffiliation(contextService.getUser(), pair.owner, 'INST_EDITOR') ||
                                        userService.hasFormalAffiliation(contextService.getUser(), pair.customer, 'INST_EDITOR')
                            %>
                            <tr>
                                <td>${pair.customer.sortname ?: pair.customer.name}</td>
                                <td>${pair.getProvider()} : ${pair.platform.name}</td>
                                <td><ui:xEditable owner="${pair}" field="value"
                                                  overwriteEditable="${overwriteEditable_ci}"/></td>
                                <td><ui:xEditable owner="${pair}" field="requestorKey"
                                                  overwriteEditable="${overwriteEditable_ci}"/></td>
                                <td><ui:xEditable owner="${pair}" field="note"
                                                  overwriteEditable="${overwriteEditable_ci}"/></td>
                                <td>
                                    <g:if test="${overwriteEditable_ci}">
                                        <g:link controller="subscription"
                                                action="unsetCustomerIdentifier"
                                                id="${subscription.id}"
                                                params="${[deleteCI: pair.id]}"
                                                class="ui button icon red la-modern-button js-open-confirm-modal"
                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unset.customeridentifier", args: ["" + pair.getProvider() + " : " + (pair.platform ?: '') + " " + (pair.value ?: '')])}"
                                                data-confirm-term-how="unset"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="eraser icon"></i>
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>

                <g:if test="${reportTypes}">
                    <g:if test="${revision == AbstractReport.COUNTER_4}">
                        <ui:msg icon="ui info icon" class="info"
                                header="${message(code: 'default.usage.counter4reportInfo.header')}"
                                message="default.usage.counter4reportInfo.text" noClose="true"/>
                    </g:if>
                    <g:form action="generateReport" name="stats" class="ui form" method="get">
                        <g:hiddenField name="id" value="${subscription.id}"/>
                        <g:hiddenField name="revision" value="${revision}"/>
                        <div class="five fields" id="filterDropdownWrapper">
                            <g:if test="${platformInstanceRecords.size() > 1}">
                                <div class="field">
                                    <label for="platform"><g:message code="platform"/></label>
                                    <ui:select class="ui search selection dropdown" from="${platformInstanceRecords}"
                                               name="platform"/>
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
                                        <option value="<g:message code="default.stats.noReport"/>"><g:message
                                                code="default.stats.noReport"/></option>
                                    </g:if>
                                </select>
                            </div>
                            <g:if test="${params.reportType}">
                                <laser:render template="/templates/filter/statsFilter"/>
                            </g:if>
                            <%-- reports filters in COUNTER 5 count only for master reports (tr, pr, dr, ir)! COUNTER 4 has no restriction on filter usage afaik --%>
                        </div>

                        <div class="field">
                            <div id="selDate" class="ui labeled ticked range slider"></div>
                        </div>

                        <div class="four fields">
                            <div class="field"></div>

                            <div class="field"></div>

                            <div class="field la-field-right-aligned">
                                <%-- deactivated as of ERMS-3996; concept needs to be clarified
                                <input id="generateCostPerUse" type="button" class="ui secondary button" value="${message(code: 'default.stats.generateCostPerUse')}"/>--%>
                                <g:link action="stats" id="${subscription.id}"
                                        class="ui button secondary">${message(code: 'default.button.reset.label')}</g:link>
                            </div>

                            <div class="field la-field-right-aligned">
                                <input id="generateReport" type="button" class="ui primary button" disabled="disabled"
                                       value="${message(code: 'default.stats.generateReport')}"/>
                            </div>
                        </div>
                    </g:form>
                </g:if>
                <g:elseif test="${error}">
                    <ui:msg icon="ui times icon" class="error" noClose="true">
                        <g:if test="${error == 'noCustomerId'}">
                            <g:message code="default.stats.error.${error}.local" args="${errorArgs}"/>

                            <g:if test="${contextOrg.id == subscription.getConsortia()?.id}">
                                <br/>
                                Alternativ: <g:link controller="subscription" action="membersSubscriptionsManagement"
                                                    id="${subscription.instanceOf.id}"
                                                    params="[tab: 'customerIdentifiers', isSiteReloaded: false]">
                                <g:message code="subscriptionsManagement.subscriptions.members"/> &rarr; <g:message
                                        code="org.customerIdentifier"/>
                            </g:link>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:message code="default.stats.error.${error}" args="${errorArgs}"/>
                        </g:else>
                    </ui:msg>
                </g:elseif>
                <div class="ui teal progress" id="progressIndicator" hidden>
                    <div class="bar">
                        <div class="progress"></div>
                    </div>
                    <div class="label">Erzeuge Tabelle ...</div>
                </div>
                <div id="reportWrapper"></div>
            </g:if>
        </g:else>
        <laser:script file="${this.getGroovyPageFileName()}">
            /*
                equivalency table:
                0: 2021-01
                max: current value +1
                start: last month
                end: current month
            */
            let step = 1;
            let monthIndex = 0;
            let limit = new Date();
            limit.setHours(0);
            limit.setMinutes(0);
            limit.setSeconds(0);
            limit.setMilliseconds(0);
            let currDate = new Date(2021, 0, 1, 0, 0, 0, 0);
            let currMonth = currDate.getMonth()+1;
            if(currMonth < 10)
                currMonth = '0'+currMonth;
            let startDate = currDate.getFullYear()+'-'+currMonth;
            currMonth = limit.getMonth(); //previous month
            let endDate;
            if(currMonth > 0 && currMonth < 10) {
                currMonth = '0'+currMonth;
                endDate = limit.getFullYear()+'-'+currMonth;
            }
            else if(currMonth === 0)
                endDate = (limit.getFullYear()-1)+'-12';
            let months = [];
            let displayMonths = [];
            while(currDate.getTime() <= limit.getTime()) {
                currMonth = currDate.getMonth()+1;
                if(currMonth < 10)
                    currMonth = '0'+currMonth;
                if(currMonth === '01')
                    displayMonths.push(currDate.getFullYear()+'-'+currMonth);
                months.push(currDate.getFullYear()+'-'+currMonth);
                currDate.setMonth(currDate.getMonth()+1);
                monthIndex++;
            }
            $("#selDate").slider({
                min: 0,
                max: months.length-1,
                start: 0,
                end: months.length-2,
                step: step,
                restrictedLabels: displayMonths,
                showLabelTicks: 'always',
                interpretLabel: function(value) {
                    return months[value];
                },
                onChange: function(range, start, end) {
                    startDate = months[start];
                    endDate = months[end];
                }
            });
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
                $('#progressIndicator').show();
                let fd = new FormData($('#stats')[0]);
                fd.append('startDate',startDate);
                fd.append('endDate',endDate);
                $.ajax({
                    url: "<g:createLink action="generateReport"/>",
                    data: fd,
                    type: 'POST',
                    processData: false,
                    contentType: false
                }).done(function(response){
                    $("#reportWrapper").html(response);
                    $('#progressIndicator').hide();
                });
                checkProgress();
            });

            function checkProgress() {
                let percentage = 0;
                setTimeout(function() {
                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="checkProgress" params="[cachePath: '/subscription/stats', cacheKey: 'progress']"/>"
                    }).done(function(response){
                        percentage = response.percent;
                        if(percentage !== null)
                            $('#progressIndicator').progress('set percent', percentage);
                        if($('#progressIndicator').progress('is complete')) {
                            $('#progressIndicator').hide();
                        }
                        else {
                            checkProgress();
                        }
                    }).fail(function(resp, status){
                        //TODO
                    });
                }, 500);
            }
        </laser:script>
<laser:htmlEnd />
