<%@ page import="de.laser.Subscription; de.laser.Person; de.laser.SurveyConfig; de.laser.SubscriptionsQueryService; java.text.SimpleDateFormat; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<g:set var="formService" bean="formService"/>

<semui:form>
    <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
            sourceObject          : sourceObject,
            targetObject          : targetObject,
            allObjects_readRights : allObjects_readRights,
            allObjects_writeRights: allObjects_writeRights]"/>
    <g:form action="copyElementsIntoSubscription" controller="subscription"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <g:if test="${sourceObject instanceof Subscription && SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(sourceObject, true)}">
            <semui:msg class="negative" message="copyElementsIntoObject.surveyExist"/>
        </g:if>
        <g:else>

            <table class="ui celled table">
                <tbody>
                <tr>
                    <td>
                        <table class="ui celled la-js-responsive-table la-table table" id="firstTable">
                            <thead>
                            <tr>
                                <th colspan="5">
                                    <g:if test="${sourceObject}"><g:link controller="subscription"
                                                                         action="members"
                                                                         id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                                </th>
                            </tr>
                            <tr>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.startDate.label')}</th>
                                <th>${message(code: 'default.endDate.label')}</th>
                                <th>${message(code: 'default.status.label')}</th>
                                <th class=" center aligned">
                                    <input type="checkbox" data-action="copy"
                                           onClick="JSPC.app.toggleAllCheckboxes(this)" checked/>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${validSourceSubChilds}" var="sub" status="i">
                                <tr>
                                    <td>${i+1}</td>
                                    <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                                        <td class="titleCell">
                                            <g:link controller="subscription"
                                                    action="show"
                                                    id="${sub.id}">${subscriberOrg.sortname}</g:link>
                                            <g:if test="${subscriberOrg.getCustomerType() in ['ORG_INST']}">
                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                      data-position="bottom center"
                                                      data-content="${subscriberOrg.getCustomerTypeI10n()}">
                                                    <i class="chess rook grey icon"></i>
                                                </span>
                                            </g:if>
                                        </td>
                                        <td><g:formatDate formatName="default.date.format.notime"
                                                          date="${sub.startDate}"/></td>
                                        <td><g:formatDate formatName="default.date.format.notime"
                                                          date="${sub.endDate}"/></td>
                                        <td>${sub.status.getI10n('value')}</td>
                                        <td class=" center aligned">
                                            <div class="ui checkbox la-toggle-radio la-replace">
                                                <g:checkBox name="copyObject.copySubscriber"
                                                            value="${genericOIDService.getOID(sub)}"
                                                            data-action="copy" checked="${true}"/>
                                            </div>
                                        </td>
                                    </g:each>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
                    <td>
                        <table class="ui celled la-js-responsive-table la-table table" id="secondTable">
                            <thead>
                            <tr>
                                <th colspan="4">
                                    <g:if test="${targetObject}"><g:link controller="subscription"
                                                                         action="members"
                                                                         id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if>
                                </th>
                            </tr>
                            <tr>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.startDate.label')}</th>
                                <th>${message(code: 'default.endDate.label')}</th>
                                <th>${message(code: 'default.status.label')}</th>
                                <th class=" center aligned">
                                    <input type="checkbox" data-action="copy"
                                           onClick="JSPC.app.toggleAllCheckboxes(this)" checked/>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${validTargetSubChilds}" var="sub" status="i">
                                <tr>
                                    <td>${i+1}</td>
                                    <g:each in="${sub.refresh().getAllSubscribers()}" var="subscriberOrg">
                                        <td class="titleCell">
                                            <g:link controller="subscription"
                                                    action="show"
                                                    id="${sub.id}">${subscriberOrg.sortname}</g:link>
                                        </td>
                                        <td><g:formatDate formatName="default.date.format.notime"
                                                          date="${sub.startDate}"/></td>
                                        <td><g:formatDate formatName="default.date.format.notime"
                                                          date="${sub.endDate}"/></td>
                                        <td>${sub.status.getI10n('value')}</td>
                                    </g:each>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
        </g:else>

        <g:set var="submitDisabled" value="${(sourceObject && targetObject) || processRunning ? '' : 'disabled'}"/>
        <div class="sixteen wide field" style="text-align: right;">
            <input id="copySubscriber" type="submit" class="ui button js-click-control"
                   value="${message(code: 'copyElementsIntoObject.copySubscriber.button')}" ${submitDisabled}/>
        </div>
        </tbody>
    </table>
    </g:form>
</semui:form>

<laser:script file="${this.getGroovyPageFileName()}">
        $("#firstTable .titleCell").each(function (k) {
            var v = $(this).height();
            $("#secondTable .titleCell").eq(k).height(v);
        });

        $("#secondTable .titleCell").each(function (k) {
            var v = $(this).height();
            $("#firstTable .titleCell").eq(k).height(v);
        });

    $("#copySubscriber").submit(function() {
        $(this).disable();
    });
</laser:script>

<style>
table {
    table-layout: fixed;
    width: 100%;
}
table td {
    vertical-align: top;
}
</style>
