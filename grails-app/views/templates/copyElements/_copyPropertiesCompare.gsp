<%@ page import="de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.License; de.laser.SubscriptionController; de.laser.CopyElementsService;" %>
<laser:serviceInjection/>

    <g:if test="${!fromSurvey && !isRenewSub}">
        <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form controller="${controllerName}" action="${actionName}" id="${params.id ?: params.sourceObjectId}" data-confirm-id="copyElements_form"
            params="[workFlowPart: CopyElementsService.WORKFLOW_END, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">

        <g:if test="${sourceObject instanceof SurveyConfig}">
            <g:if test="${customProperties?.size() > 0 }">
                <table class="ui celled table la-js-responsive-table la-table">
                    <laser:render template="/templates/copyElements/propertyComparisonTableRow" model="[group:customProperties, key:message(code:'surveyconfig.properties'), sourceObject:sourceObject]" />
                </table>
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${privateProperties?.size() > 0}">

                <div class="content">
                    <h2 class="ui header">${message(code: 'surveyconfig.properties.private')}: ${contextOrg.name}</h2>
                    <table class="ui celled table la-js-responsive-table la-table">
                        <laser:render template="/templates/copyElements/propertyComparisonTableRow"
                                  model="[group: privateProperties, key: message(code: 'surveyconfig.properties') + ': ' + contextService.getOrg().name]"/>
                    </table>
                </div>

            </g:if>
        </g:if>

        <g:if test="${sourceObject instanceof Subscription || sourceObject instanceof License}">
            <g:if test="${groupedProperties?.size() > 0}">
                <g:each in="${groupedProperties}" var="groupedProps">
                    <g:if test="${groupedProps.getValue()}">

                        <h5 class="ui header">
                            ${message(code: 'subscription.properties.public')}
                            (${groupedProps.getKey().name})

                            <g:if test="${showConsortiaFunctions}">
                                <g:if test="${groupedProps.getValue().binding?.isVisibleForConsortiaMembers}">
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'financials.isVisibleForSubscriber')}"
                                          style="margin-left:10px">
                                        <i class="ui icon eye orange"></i>
                                    </span>
                                </g:if>
                            </g:if>
                        </h5>

                        <table class="ui celled table la-js-responsive-table la-table">
                            <laser:render template="/templates/copyElements/propertyComparisonTableRow"
                                      model="[group: groupedProps.getValue().groupTree, key: groupedProps.getKey().name, propBinding: groupedProps.getValue().binding]"/>
                        </table>

                        <div class="ui divider"></div>

                    </g:if>
                </g:each>
            </g:if>

            <g:if test="${orphanedProperties?.size() > 0}">

                <div class="content">
                    <h2 class="ui header">
                        <g:if test="${groupedProperties?.size() > 0}">
                            ${message(code: 'subscription.properties.orphaned')}
                        </g:if>
                        <g:else>
                            ${message(code: 'subscription.properties')}
                        </g:else>
                    </h2>

                    <table class="ui celled table la-js-responsive-table la-table">
                        <laser:render template="/templates/copyElements/propertyComparisonTableRow"
                                  model="[group: orphanedProperties, key: message(code: 'subscription.properties'), sourceObject: sourceObject]"/>
                    </table>

                </div>

                <div class="ui divider"></div>
            </g:if>
            <g:if test="${privateProperties?.size() > 0}">

                <div class="content">
                    <h2 class="ui header">${message(code: 'subscription.properties.private')} ${contextOrg.name}</h2>
                    <table class="ui celled table la-js-responsive-table la-table">
                        <laser:render template="/templates/copyElements/propertyComparisonTableRow"
                                  model="[group: privateProperties, key: message(code: 'subscription.properties.private') + ' ' + contextService.getOrg().name]"/>
                    </table>
                </div>

            </g:if>
        </g:if>

        <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>

        <g:if test="${fromSurvey}">
            <g:if test="${customProperties || privateProperties || groupedProperties || orphanedProperties}">
                <g:set var="submitButtonText" value="${isRenewSub ?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStepSurvey') :
                        message(code: 'copyElementsIntoObject.copyProperties.button')}"/>
            </g:if>
            <g:else>
                <strong>${message(code: 'copyElementsIntoObject.copyProperties.empty')}</strong>
                <br /><br />
                <g:set var="submitButtonText" value="${isRenewSub ?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStepbySurvey') :
                        message(code: 'copyElementsIntoObject.lastStepWithoutSaveDate')}"/>
            </g:else>
        </g:if>
        <g:elseif test="${copyObject}">
            <g:if test="${customProperties || privateProperties || groupedProperties || orphanedProperties}">
                <g:set var="submitButtonText" value="${message(code: 'default.button.copy.label')}"/>
            </g:if>
            <g:else>
                <strong>${message(code: 'copyElementsIntoObject.copyProperties.empty')}</strong>
                <br /><br />
                <g:set var="submitButtonText" value="${message(code: 'default.button.copy.label')}"/>
            </g:else>
        </g:elseif>
        <g:else>
            <g:if test="${customProperties || privateProperties || groupedProperties || orphanedProperties}">
                <g:set var="submitButtonText" value="${isRenewSub ?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStep') :
                        message(code: 'copyElementsIntoObject.copyProperties.button')}"/>
            </g:if>
            <g:else>
                <strong>${message(code: 'copyElementsIntoObject.copyProperties.empty')}</strong>
                <br /><br />
                <g:set var="submitButtonText" value="${isRenewSub ?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStepWithoutSaveDate') :
                        message(code: 'copyElementsIntoObject.lastStepWithoutSaveDate')}"/>
            </g:else>
        </g:else>

        <div class="sixteen wide field" style="text-align: right;">
            <input id="copyElementsSubmit" type="submit" class="ui button js-click-control" value="${submitButtonText}" ${submitDisabled}
                   data-confirm-id="copyElements"
                   data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                   data-confirm-term-how="delete"/>
        </div>
    </g:form>

