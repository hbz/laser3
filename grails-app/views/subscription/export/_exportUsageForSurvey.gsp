<%@ page import="de.laser.remote.ApiSource; de.laser.Platform; de.laser.base.AbstractReport; grails.converters.JSON; de.laser.CustomerIdentifier" %>
<laser:serviceInjection/>
<%
    Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription in (:subscriptions)", [subscriptions: allSubscriptions])
    if(!subscribedPlatforms) {
        subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subscriptions)", [subscriptions: allSubscriptions])
    }
    Map<String, Map> platformInstanceRecords = [:]
    JSON platformsJSON = subscribedPlatforms.globalUID as JSON
    ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
    String revision
    List<CustomerIdentifier> dummyCIs = []
    SortedSet reportTypes
    String dummy
    subscribedPlatforms.each { Platform platformInstance ->
        Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: platformInstance.gokbId])
        if (queryResult.warning) {
            List records = queryResult.warning.result
            if(records[0]) {
                if(records[0].counterR5SushiApiSupported == 'Yes') {
                    revision = AbstractReport.COUNTER_5
                }
                else if(records[0].counterR4SushiApiSupported == 'Yes') {
                    revision = AbstractReport.COUNTER_4
                }
                records[0].lastRun = platformInstance.counter5LastRun ?: platformInstance.counter4LastRun
                records[0].id = platformInstance.id
                platformInstanceRecords[platformInstance.gokbId] = records[0]
            }
        }
        CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(subscriber, platformInstance)
        if(ci?.value) {
            reportTypes = subscriptionControllerService.getAvailableReports(subscribedPlatforms, [subscription: subscriberSub])
        }
        else if(ci) {
            dummyCIs << ci
        }
        else {
            CustomerIdentifier dummyCI = new CustomerIdentifier(customer: subscriber, platform: platformInstance, owner: institution)
            dummyCI.save()
            dummyCIs << dummyCI
        }
    }
%>
<ui:modal id="individuallyExportModal" modalSize="large" text="${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}" refreshModal="true" hideSubmitButton="true">
    <g:if test="${reportTypes}">
        <g:if test="${revision == AbstractReport.COUNTER_4}">
            <ui:msg icon="ui info icon" class="info" header="${message(code: 'default.usage.counter4reportInfo.header')}" message="default.usage.counter4reportInfo.text" noClose="true"/>
        </g:if>
        <g:form action="renewEntitlementsWithSurvey" name="stats" class="ui form" method="get">
            <g:hiddenField name="id" value="${subscription.id}"/>
            <g:hiddenField name="revision" value="${revision}"/>
            <div class="four fields" id="filterDropdownWrapper">
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
            <div class="three fields">
                <div class="field"></div>
                <div class="field"></div>
                <div class="field la-field-right-aligned">
                    <input id="generateReport" type="button" class="ui primary button" disabled="disabled" value="${message(code: 'default.stats.generateReport')}"/>
                </div>
            </div>
        </g:form>
    </g:if>
    <g:elseif test="${dummyCIs}">
        <%-- continue here: backend implementation; create tokens for the table --%>
        Es fehlen Kundennummer/Requestor-ID-Schlüsselpaare. Hier können Sie die fehlenden Schlüsselpaare nachtragen. Laden Sie bitte anschließend die Seite neu.
        <table>
            <thead>
            <tr>
                <th>Einrichtung</th>
                <th>Plattform</th>
                <th>Kundennummer</th>
                <th>Requestor-ID/API-Key</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${dummyCIs}" var="dummyCI">
                <tr>
                    <td>${dummyCI.customer.sortname}</td>
                    <td>${dummyCI.platform}</td>
                    <td><ui:xEditable owner="${dummyCI}" field="value"/></td>
                    <td><ui:xEditable owner="${dummyCI}" field="requestorKey"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:elseif>
    <div id="reportWrapper"></div>
</ui:modal>
<laser:script>
    $("#reportType").on('change', function() {
        <g:applyCodec encodeAs="none">
            let platforms = ${platformsJSON};
        </g:applyCodec>
        $.ajax({
            url: "<g:createLink controller="ajaxHtml" action="loadFilterList"/>",
            data: {
                reportType: $(this).val(),
                platforms: platforms,
                customer: '${subscriber.globalUID}',
                subscription: ${subscriberSub.id}
            }
        }).done(function(response) {
            $('.dynFilter').remove();
            $('#filterDropdownWrapper').append(response);
            $('#generateReport').removeAttr('disabled');
            r2d2.initDynamicUiStuff('#filterDropdownWrapper');
        });
    });
    $("#generateReport").on('click', function() {
        $('#globalLoadingIndicator').show();
        let fd = new FormData($('#stats')[0]);
        $.ajax({
            url: "<g:createLink action="renewEntitlementsWithSurvey"/>",
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