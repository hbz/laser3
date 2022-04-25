<%@ page import="de.laser.Subscription; de.laser.Person; de.laser.SurveyConfig; de.laser.SubscriptionsQueryService; java.text.SimpleDateFormat; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<g:set var="formService" bean="formService"/>

<semui:form>
    <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
            sourceObject          : sourceObject,
            targetObject          : targetObject,
            allObjects_readRights : allObjects_readRights,
            allObjects_writeRights: allObjects_writeRights]"/>
    <g:form action="copyElementsIntoSurvey" controller="survey" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>


        <table class="ui celled table">
            <tbody>
            <table>
                <tr>
                    <td>
                        <table class="ui celled la-js-responsive-table la-js-responsive-table la-table table" id="firstTable">
                            <thead>
                            <tr>
                                <th colspan="4">
                                    <g:if test="${sourceObject}">
                                        <g:link controller="survey" action="show"
                                                params="[id: sourceObject.surveyInfo.id, surveyConfigID: sourceObject.id]">${sourceObject.dropdownNamingConvention()}</g:link>
                                    </g:if>
                                </th>
                            </tr>
                            <tr>
                                <th><g:message code="sidewide.number"/></th>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.name.label')}</th>
                                <th class=" center aligned">
                                    <input type="checkbox" data-action="copy"
                                           onClick="JSPC.app.toggleAllCheckboxes(this)" checked/>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${sourceObject.orgs.sort { it.org.sortname }}" var="surveyOrg" status="i">
                                <tr>
                                    <td>${i + 1}</td>
                                    <td>
                                        <g:link controller="organisation"
                                                action="show"
                                                id="${surveyOrg.org.id}">${surveyOrg.org.sortname}</g:link>
                                        <g:if test="${surveyOrg.org.getCustomerType() in ['ORG_INST']}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="bottom center"
                                                  data-content="${surveyOrg.org.getCustomerTypeI10n()}">
                                                <i class="chess rook grey icon"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td class="titleCell">
                                        <g:link controller="organisation"
                                                action="show"
                                                id="${surveyOrg.org.id}">${surveyOrg.org.name}</g:link>

                                        <g:if test="${sourceObject.subscription}">
                                            <g:set var="existSubforOrg"
                                                   value="${Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                                           [parentSub  : sourceObject.subscription,
                                                            participant: surveyOrg.org
                                                           ])}"/>
                                            <g:if test="${existSubforOrg}">

                                                <br /><br />
                                                <g:if test="${existSubforOrg[0].isCurrentMultiYearSubscriptionNew()}">
                                                    <g:message code="surveyOrg.perennialTerm.available"/>
                                                    <br />
                                                    <g:link controller="subscription" action="show"
                                                            class="ui icon button"
                                                            id="${existSubforOrg[0].id}">
                                                        <i class="icon clipboard la-list-icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:link controller="subscription" action="show"
                                                            class="ui icon button"
                                                            id="${existSubforOrg[0].id}">
                                                        <i class="icon clipboard la-list-icon"></i>
                                                    </g:link>
                                                </g:else>
                                            </g:if>
                                        </g:if>
                                    </td>
                                    <td class="center aligned">
                                        <div class="ui checkbox la-toggle-radio la-replace">
                                            <g:checkBox name="copyObject.copyParticipants"
                                                        value="${genericOIDService.getOID(surveyOrg.org)}"
                                                        data-action="copy" checked="${true}"/>
                                        </div>
                                    </td>
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
                                    <g:if test="${targetObject}">
                                        <g:link controller="survey" action="show"
                                                params="[id: targetObject.surveyInfo.id, surveyConfigID: targetObject.id]">${targetObject.dropdownNamingConvention()}</g:link>
                                    </g:if>
                                </th>
                            </tr>
                            <tr>
                                <th><g:message code="sidewide.number"/></th>
                                <th>${message(code: 'default.sortname.label')}</th>
                                <th>${message(code: 'default.name.label')}</th>
                                <th class=" center aligned">
                                    <g:if test="${targetObject}">
                                        <input class="setDeletionConfirm" type="checkbox" data-action="delete"
                                               onClick="JSPC.app.toggleAllCheckboxes(this)"/>
                                    </g:if>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${targetObject.orgs.sort { it.org.sortname }}" var="surveyOrg" status="i">
                                <tr>
                                    <td>${i+1}</td>
                                    <td>
                                        <g:link controller="organisation"
                                                action="show"
                                                id="${surveyOrg.org.id}">${surveyOrg.org.sortname}</g:link>
                                        <g:if test="${surveyOrg.org.getCustomerType() in ['ORG_INST']}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="bottom center"
                                                  data-content="${surveyOrg.org.getCustomerTypeI10n()}">
                                                <i class="chess rook grey icon"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td class="titleCell">
                                        <g:link controller="organisation"
                                                action="show"
                                                id="${surveyOrg.org.id}">${surveyOrg.org.name}</g:link>

                                        <g:if test="${targetObject.subscription}">
                                            <g:set var="existSubforOrg"
                                                   value="${Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                                           [parentSub  : targetObject.subscription,
                                                            participant: surveyOrg.org
                                                           ])}"/>

                                            <g:set var="existSubforOrg"
                                                   value="${Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                                           [parentSub  : sourceObject,
                                                            participant: surveyOrg.org
                                                           ])}"/>
                                            <g:if test="${existSubforOrg}">

                                                <br /><br />
                                                <g:if test="${existSubforOrg[0].isCurrentMultiYearSubscriptionNew()}">
                                                    <g:message code="surveyOrg.perennialTerm.available"/>
                                                    <br />
                                                    <g:link controller="subscription" action="show"
                                                            class="ui icon button"
                                                            id="${existSubforOrg[0].id}">
                                                        <i class="icon clipboard la-list-icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:link controller="subscription" action="show"
                                                            class="ui icon button"
                                                            id="${existSubforOrg[0].id}">
                                                        <i class="icon clipboard la-list-icon"></i>
                                                    </g:link>
                                                </g:else>
                                            </g:if>
                                        </g:if>

                                    </td>
                                    <td class="center aligned">
                                        <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                            <g:checkBox name="copyObject.deleteParticipants"
                                                        value="${genericOIDService.getOID(surveyOrg.org)}"
                                                        data-action="delete" checked="${true}"/>
                                        </div>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </td>
            </table>


            <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete"
                       value="${message(code: 'copyElementsIntoObject.copySubscriber.button')}" ${submitDisabled}/>
            </div>
            </tbody>
        </table>
    </g:form>
</semui:form>

<laser:script file="${this.getGroovyPageFileName()}">
        $("#firstTable .titleCell").each(function(k) {
            var v = $(this).height();
            $("#secondTable .titleCell").eq(k).height(v);
        });

        $("#secondTable .titleCell").each(function(k) {
            var v = $(this).height();
            $("#firstTable .titleCell").eq(k).height(v);
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
