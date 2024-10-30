<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.Subscription; de.laser.wekb.Platform; de.laser.base.AbstractReport; de.laser.finance.CostItem; de.laser.properties.SubscriptionProperty; de.laser.storage.PropertyStore" %>
<laser:serviceInjection/>

<g:if test="${platformInstanceRecords.values().statisticsFormat.contains('COUNTER')}">
    <laser:serviceInjection/>
    <ui:tabs>
        <g:each in="${platformInstanceRecords.values()}" var="platform">
            <ui:tabsItem controller="$controllerName" action="$actionName" tab="${platform.id.toString()}"
                         params="${params + [tab: platform.id]}" text="${platform.name}"/>
        </g:each>
    </ui:tabs>
    <g:each in="${platformInstanceRecords.values()}" var="platform">
        <div class="ui bottom attached tab active segment" id="customerIdWrapper">
            <laser:render template="/platform/platformStatsDetails" model="[wekbServerUnavailable: wekbServerUnavailable, platformInstanceRecord: platform]"/>
            <g:set var="statsInfo" value="${SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp where (sp.owner = :subscription or sp.owner = (select s.instanceOf from Subscription s where s = :subscription)) and sp.type = :statsAccess', [statsAccess: PropertyStore.SUB_PROP_STATS_ACCESS, subscription: subscription])}"/>
            <g:if test="${statsInfo}">
                <ui:msg showIcon="true" class="info" noClose="true" header="${message(code: 'default.stats.info.header')}">
                    ${statsInfo[0]}<br>
                    <g:message code="default.stats.wekbContact"/><ui:wekbIconLink type="org" gokbId="${platform.providerUuid}"/>
                </ui:msg>
            </g:if>
            <table class="ui la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th class="three wide">${message(code: 'consortium.member')}</th>
                    <th class="four wide">${message(code: 'provider.label')} : ${message(code: 'platform.label')}</th>
                    <th class="three wide">${message(code: 'org.customerIdentifier')}</th>
                    <th class="three wide">${message(code: 'org.requestorKey')}</th>
                    <th class="two wide">${message(code: 'default.note.label')}</th>
                    <th class="one wide">${message(code: 'default.actions')}</th>
                </tr>
                </thead>
                <tbody>
                <g:set var="pair" value="${keyPairs.get(platform.uuid)}"/>
                %{-- TODO: erms-5495 --}%
                %{--                <g:set var="overwriteEditable_ci" value="${editable}" />--}%
                <%
                    boolean overwriteEditable_ci = contextService.getUser().isAdmin() ||
                            userService.hasFormalAffiliation(pair.owner, 'INST_EDITOR') ||
                            userService.hasFormalAffiliation(pair.customer, 'INST_EDITOR')
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
                                    class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unset.customeridentifier", args: ["" + pair.getProvider() + " : " + (pair.platform ?: '') + " " + (pair.value ?: '')])}"
                                    data-confirm-term-how="unset"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.ERASE}"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </g:each>

    <g:if test="${reportTypes}">
        <g:if test="${revision == AbstractReport.COUNTER_4}">
        <%-- taglib not displaying properly
        <ui:msg class="info" showIcon="true"
                header="${message(code: 'default.usage.counter4reportInfo.header')}"
                message="default.usage.counter4reportInfo.text" hideClose="true"/>
        --%>
            <ui:msg class="info" showIcon="true" hideClose="true"
                        header="${message(code: 'default.usage.counter4reportInfo.header')}"
                        message="default.usage.counter4reportInfo.text" />
        </g:if>
        <g:form controller="subscription" action="generateReport" name="stats" class="ui form" method="get">
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
                <label for="selDate">Zeitraum für Reports wählen (von .. bis)</label>
                <div style="margin:2em 2.5em 4em">
                    <div id="selDate" class="ui green labeled ticked range slider"></div>
                </div>
            </div>

            <div class="field la-field-right-aligned">
                <input id="generateReport" type="button" class="${Btn.PRIMARY}" disabled="disabled"
                       value="${message(code: 'default.stats.generateReport')}"/>
                <g:if test="${CostItem.findBySubAndCostItemElementConfiguration(subscription, RDStore.CIEC_POSITIVE)}">
                    <input id="generateCostPerUse" type="button" class="${Btn.PRIMARY}" disabled="disabled"
                           value="${message(code: 'default.stats.generateCostPerUse')}"/>
                </g:if>

                <g:if test="${controllerName == 'survey'}">
                    <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: params.viewTab]}"/>
                    <g:set var="participant" value="${participant}"/>
                </g:if>
                <g:elseif test="${controllerName == 'myInstitution'}">
                    <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, viewTab: params.viewTab]}"/>
                    <g:set var="participant" value="${institution}"/>
                </g:elseif>

                <g:link controller="$controllerName" action="$actionName" id="${params.id}" params="${parame}"
                        class="${Btn.SECONDARY}">${message(code: 'default.button.reset.label')}</g:link>
            </div>
        </g:form>
        <div class="ui teal progress" id="progressIndicator" hidden="hidden">
            <div class="bar">
                <div class="progress"></div>
            </div>
            <div class="label"></div>
        </div>
        <div id="reportWrapper"></div>
    </g:if>
    <g:elseif test="${error}">

        <ui:msg class="error" showIcon="true" hideClose="true">
            <g:if test="${error == 'noCustomerId'}">
                <g:message code="default.stats.error.${error}.local" args="${errorArgs}"/>

                <g:if test="${contextOrg.id == subscription.getConsortium()?.id}">
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
</g:if>
<g:elseif test="${platformInstanceRecords.values().statisticsFormat.contains('Document') || platformInstanceRecords.values().statisticsFormat.contains('Diagram')}">
    <laser:serviceInjection/>
    <ui:tabs>
        <g:each in="${platformInstanceRecords.values()}" var="platform">
            <ui:tabsItem controller="${controllerName}" action="${actionName}" tab="${platform.id.toString()}"
                         params="${params + [tab: platform.id]}" text="${platform.name}"/>
        </g:each>
    </ui:tabs>
    <g:each in="${platformInstanceRecords.values()}" var="platform">
        <div class="ui bottom attached tab active segment" id="customerIdWrapper">
            <laser:render template="/platform/platformStatsDetails" model="[wekbServerUnavailable: wekbServerUnavailable, platformInstanceRecord: platform]"/>
        </div>
    </g:each>
</g:elseif>


<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.stats_slider_date_format = function (value, variant) {
        let date = new Date(value.split('-')[0], value.split('-')[1]-1) //correction by -1 because Date() month counting is zero-based
        return date.toLocaleDateString('de-DE', {year: 'numeric', month: variant})
    }
    JSPC.app.stats_slider_color = function (start, end) {
        let colors = ['green', 'yellow', 'orange', 'red']
        $('#selDate').removeClass(colors)
        $('#selDate').addClass(colors[Math.min(3, Math.floor((end - start) * 0.33))])
    }

    let step = 1;
    let monthIndex = 0;
    let startIndex = 0;
    let endIndex = 0;
    let limit = new Date();
    limit.setHours(0);
    limit.setMinutes(0);
    limit.setSeconds(0);
    limit.setMilliseconds(0);
    let currDate = new Date(limit.getFullYear()-1, 0, 1, 0, 0, 0, 0);
    let startDate;
    <g:if test="${subscription.startDate}">
        let start = new Date(<g:formatDate date="${subscription.startDate}" format="yyyy, M, d"/>, 0, 0, 0, 0);
                start.setMonth(start.getMonth()-1); //correction because month is 0-based
                if(start.getTime() < currDate.getTime())
                    currDate = start;
                startDate = '<g:formatDate date="${subscription.startDate}" format="yyyy-MM"/>';
    </g:if>
    <g:else>
        let start = new Date(limit.getFullYear()-1, 0, 1, 0, 0, 0, 0);
        startDate = start.getFullYear()+'-01';
    </g:else>
    let currMonth = currDate.getMonth()+1;
    if(currMonth < 10)
        currMonth = '0'+currMonth;
    currMonth = limit.getMonth(); //previous month
    let endDate;
    <g:if test="${subscription.endDate}">
        <g:if test="${subscription.endDate < new Date() && subscription.status != RDStore.SUBSCRIPTION_TEST_ACCESS}">
            endDate = '<g:formatDate date="${subscription.endDate}" format="yyyy-MM"/>';
        </g:if>
        <g:else>
            endDate = '<g:formatDate date="${new Date()}" format="yyyy-MM"/>';
        </g:else>
    </g:if>
    <g:else>
        if(currMonth > 0 && currMonth < 10) {
            currMonth = '0'+currMonth;
            endDate = limit.getFullYear()+'-'+currMonth;
        }
        else if(currMonth === 0)
            endDate = (limit.getFullYear()-1)+'-12';
    </g:else>
    let months = [];
    while(currDate.getTime() <= limit.getTime()) {
        currMonth = currDate.getMonth()+1;
        if(currMonth < 10)
            currMonth = '0'+currMonth;
        months.push(currDate.getFullYear()+'-'+currMonth);
        if(currDate.getFullYear()+'-'+currMonth === startDate) {
            startIndex = monthIndex;
        }
        if(currDate.getFullYear()+'-'+currMonth === endDate)
            endIndex = monthIndex;
        currDate.setMonth(currDate.getMonth()+1);
        monthIndex++;
    }
    $("#selDate").slider({
        min: 0,
        max: months.length-1,
        step: step,
        showLabelTicks: 'always',
        interpretLabel: function(value) {
            return JSPC.app.stats_slider_date_format( months[value], 'short' )
        },
        showThumbTooltip: true,
        tooltipConfig: {
            position: 'bottom center',
            variation: 'visible primary large'
        },
        onMove: function(range, start, end) {
            $('#selDate .thumb[data-tooltip=' + start + ']').attr('data-tooltip', JSPC.app.stats_slider_date_format( months[start], 'long' ))
            $('#selDate .thumb[data-tooltip=' + end + ']').attr('data-tooltip', JSPC.app.stats_slider_date_format( months[end], 'long' ))
            JSPC.app.stats_slider_color(start, end)
        },
        onChange: function(range, start, end) {
            startDate = months[start];
            endDate = months[end];
            $('#selDate .thumb[data-tooltip=' + start + ']').attr('data-tooltip', JSPC.app.stats_slider_date_format( months[start], 'long' ))
            $('#selDate .thumb[data-tooltip=' + end + ']').attr('data-tooltip', JSPC.app.stats_slider_date_format( months[end], 'long' ))
            JSPC.app.stats_slider_color(start, end)
        }
    }).slider('set rangeValue', startIndex, endIndex);

    $(".sushiConnectionCheck").each(function(i) {
        let cell = $(this);
        let data = {
            org: cell.attr("data-org"),
            platform: cell.attr("data-platform"),
            customerId: cell.attr("data-customerId"),
            requestorId: cell.attr("data-requestorId")
        };
            $.ajax({
                url: "<g:createLink controller="ajaxJson" action="checkSUSHIConnection"/>",
                        data: data
                    }).done(function(response) {
                        if(response.error === true) {
                            cell.html('<span class="la-popup-tooltip" data-content="'+response.message+'"><i class="circular inverted icon red times"></i></span>');
                            r2d2.initDynamicUiStuff('#'+cell.attr('id'));
                        }
                    });
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
                        customer: '${subscription.getSubscriberRespConsortia().globalUID}',
                        subscription: ${subscription.id}
    }
}).done(function(response) {
    $('.dynFilter').remove();
    $('#filterDropdownWrapper').append(response);
    $('#generateReport, #generateCostPerUse').removeAttr('disabled');
    r2d2.initDynamicUiStuff('#filterDropdownWrapper');
});
});
$("#generateCostPerUse").on('click', function() {
$('#globalLoadingIndicator').show();
let fd = new FormData($('#stats')[0]);
fd.append('startDate',startDate);
fd.append('endDate',endDate);
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
                    url: "<g:createLink controller="subscription" action="generateReport"/>",
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
                        url: "<g:createLink controller="ajaxJson" action="checkProgress" params="[cachePath: '/'+controllerName+'/'+actionName]"/>"
                    }).done(function(response){
                        percentage = response.percent;
                        $('#progressIndicator div.label').text(response.label);
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