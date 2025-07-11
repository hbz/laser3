<%@ page import="de.laser.remote.Wekb; de.laser.wekb.Platform; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.base.AbstractReport; grails.converters.JSON; de.laser.CustomerIdentifier; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<%
    Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription])
    if(!subscribedPlatforms) {
        subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: subscription])
    }
    Map<String, Map> platformInstanceRecords = [:]
    JSON platformsJSON = subscribedPlatforms.laserID as JSON
    String revision
    List<CustomerIdentifier> dummyCIs = []
    List<String> errors = []
    Set reportTypes
    String dummy
    subscribedPlatforms.each { Platform platformInstance ->
        Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: platformInstance.gokbId])
        if (queryResult) {
            List records = queryResult.result
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
            CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(subscriber, platformInstance)
            if(ci?.value) {
                reportTypes = subscriptionControllerService.getAvailableReports([subscription: subscription], false)
            }
            else if(ci) {
                dummyCIs << ci
            }
            else {
                CustomerIdentifier dummyCI = new CustomerIdentifier(customer: subscriber, platform: platformInstance, owner: institution, type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT, isPublic: true)
                if(dummyCI.save()) {
                    dummyCIs << dummyCI
                }
                else errors << dummyCI.errors.getAllErrors().toListString()
            }
        }
    }
%>
<ui:modal id="individuallyExportModal" modalSize="large" text="${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}" refreshModal="true" hideSubmitButton="true">
    <g:if test="${reportTypes}">
        <g:if test="${revision == AbstractReport.COUNTER_4}">
            <ui:msg class="info" showIcon="true" header="${message(code: 'default.usage.counter4reportInfo.header')}" message="default.usage.counter4reportInfo.text" hideClose="true"/>
        </g:if>
        <g:form action="exportRenewalEntitlements" name="stats" class="ui form" method="get">
            <g:hiddenField name="revision" value="${revision}"/>
            <g:hiddenField name="tab" value="usage"/>
            <g:hiddenField name="exportConfig" value="${de.laser.ExportService.EXCEL}"/>
            <g:each in="${params.keySet()}" var="param">
                <g:if test="${!(param in ['tab', 'subTab', 'status'])}">
                    <g:hiddenField name="${param}" value="${params.get(param)}"/>
                </g:if>
            </g:each>
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
                    <input id="generateReport" type="button" class="${Btn.PRIMARY}" disabled="disabled" value="${message(code: 'default.stats.generateReport')}"/>
                </div>
            </div>
        </g:form>
        <div class="ui teal progress" id="localLoadingIndicator" hidden>
            <div class="bar">
                <div class="progress"></div>
            </div>
            <div class="label"></div>
        </div>
        <div id="reportWrapper"></div>
    </g:if>
    <g:elseif test="${dummyCIs}">
        <g:message code="default.usage.renewal.dummy.header"/>
        <table>
            <thead>
            <tr>
                <th><g:message code="default.institution"/></th>
                <th><g:message code="platform"/></th>
                <th><g:message code="org.customerIdentifier"/></th>
                <th><g:message code="org.requestorKey"/></th>
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
    <g:else>
        <strong><g:message code="default.stats.error.noReportAvailable"/></strong>
    </g:else>
</ui:modal>
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
                multiple: false,
                customer: '${subscriber.laserID}',
                subscription: ${subscription.id}
            }
        }).done(function(response) {
            $('.dynFilter').remove();
            $('#filterDropdownWrapper').append(response);
            $('#generateReport').removeAttr('disabled');
            r2d2.initDynamicUiStuff('#filterDropdownWrapper');
        });
    });
    $("#generateReport").on('click', function() {
        $('#localLoadingIndicator').progress();
        let fd = new FormData($('#individuallyExportModal').find('form')[0]);
        $.ajax({
            url: "<g:createLink action="exportRenewalEntitlements"/>",
            data: fd,
            type: 'POST',
            processData: false,
            contentType: false
        }).done(function(response){
            $("#reportWrapper").html(response);
        }).fail(function(resp, status){
            $("#reportWrapper").text('Es ist zu einem Fehler beim Abruf gekommen');
        });
        checkProgress();
    });

    function checkProgress() {
        let percentage = 0;
        setTimeout(function() {
            $.ajax({
                url: "<g:createLink controller="ajaxJson" action="checkProgress" params="[cachePath: '/subscription/renewEntitlementsWithSurvey/generateExport']"/>"
            }).done(function(response){
                percentage = response.percent;
                $('#localLoadingIndicator div.label').text(response.label);
                if(percentage !== null)
                    $('#localLoadingIndicator').progress('set percent', percentage);
                if($('#localLoadingIndicator').progress('is complete'))
                    $('#localLoadingIndicator').hide();
                else
                    checkProgress();
            }).fail(function(resp, status){
                //TODO
            });
        }, 500);
    }
</laser:script>