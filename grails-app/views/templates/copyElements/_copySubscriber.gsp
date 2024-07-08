<%@ page import="de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.Person; de.laser.SubscriptionsQueryService; java.text.SimpleDateFormat; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

    <g:if test="${!copyObject}">
        <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form controller="${controllerName}" action="${actionName}"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <g:if test="${targetObject instanceof Subscription && SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(targetObject, true)}">
            <ui:msg class="error" message="copyElementsIntoObject.surveyExist"/>
        </g:if>
        <g:else>

            <table class="ui celled table">
                <tbody>
                <tr>
                    <td>
                        <table class="ui celled la-js-responsive-table la-table table" id="firstTable">
                            <thead>
                            <tr>
                                <th colspan="6">
                                    <g:if test="${sourceObject}">
                                        <g:link controller="subscription" action="members" id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link>
                                    </g:if>
                                </th>
                            </tr>
                            <tr>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.startDate.label.shy')}</th>
                                <th>${message(code: 'default.endDate.label.shy')}</th>
                                <th class="la-no-uppercase">
                                    <ui:multiYearIcon />
                                </th>
                                <th>${message(code: 'default.status.label')}</th>
                                <th class="center aligned">
                                    <input type="checkbox" data-action="copy" onClick="JSPC.app.toggleAllCheckboxes(this)" checked/>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${validSourceSubChilds}" var="sub" status="i">
                                <tr>
                                    <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                                        <td class="titleCell">
                                            <g:link controller="subscription" action="show" id="${sub.id}">${subscriberOrg.sortname}</g:link>

                                            <ui:customerTypeProIcon org="${subscriberOrg}" />
                                        </td>
                                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                                        <td>
                                            <g:if test="${sub.isMultiYear}">
                                                <ui:multiYearIcon isConsortial="true" color="orange" />
                                            </g:if>
                                        </td>
                                        <td>${sub.status.getI10n('value')}</td>
                                        <td class="center aligned">
                                            <g:set var="orgInSurveyRenewal" value="${sub.isOrgInSurveyRenewal()}"/>
                                            <g:if test="${!orgInSurveyRenewal}">
                                                <div class="ui checkbox la-toggle-radio la-replace">
                                                    <g:checkBox name="copyObject.copySubscriber"
                                                                value="${genericOIDService.getOID(sub)}"
                                                                data-action="copy" checked="${true}"/>
                                                </div>
                                            </g:if>
                                            <g:else>
                                                <span class="la-popup-tooltip la-delay" data-content="${g.message(code: 'renewalEvaluation.orgsInSurvey')}" data-position="top right"><i class="icon times circle red"></i></span>
                                            </g:else>
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
                                <th colspan="5">
                                    <g:if test="${targetObject}">
                                        <g:link controller="subscription" action="members" id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link>
                                    </g:if>
                                </th>
                            </tr>
                            <tr>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.startDate.label.shy')}</th>
                                <th>${message(code: 'default.endDate.label.shy')}</th>
                                <th class="la-no-uppercase">
                                    <ui:multiYearIcon />
                                </th>
                                <th>${message(code: 'default.status.label')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${validTargetSubChilds}" var="sub" status="i">
                                <tr>
                                    <g:each in="${sub.refresh().getAllSubscribers()}" var="subscriberOrg">
                                        <td class="titleCell">
                                            <g:link controller="subscription" action="show" id="${sub.id}">${subscriberOrg.sortname}</g:link>
                                        </td>
                                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                                        <td>
                                            <g:if test="${sub.isMultiYear}">
                                                <ui:multiYearIcon isConsortial="true" color="orange" />
                                            </g:if>
                                        </td>
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
    </g:form>

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
