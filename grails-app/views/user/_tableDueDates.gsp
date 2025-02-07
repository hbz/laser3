<%@ page import="de.laser.addressbook.Person; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.utils.LocaleUtils; de.laser.utils.SqlDateUtils; de.laser.survey.SurveyInfo; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.*;" %>
<laser:serviceInjection />
<table class="ui celled table la-js-responsive-table la-table">
    <thead>
    <tr>
        <th>${message(code:'myinst.dash.due_dates.attribute.label')}</th>
        <th>${message(code:'default.date.label')}</th>
        <th>${message(code:'myinst.dash.due_dates.name.label')}</th>
        <th style="width:8em; text-align: center">${message(code:'myinst.dash.due_dates.visibility.label')}</th>
        <th style="width:8em; text-align: center">${message(code:'default.status.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${dueDates}" var="dashDueDate">
        <g:set var="obj" value="${dashDueDate? genericOIDService.resolveOID(dashDueDate.dueDateObject.oid) : null}"/>
        <g:if test="${obj}">
            <tr>
                <td>
                    <g:if test="${obj instanceof AbstractPropertyWithCalculatedLastUpdated}">
                        <i class="icon tags la-list-icon"></i>
                    </g:if>
                    ${dashDueDate.dueDateObject[LocaleUtils.getLocalizedAttributeName('attribute_value')]}
                </td>
                <td>
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${dashDueDate.dueDateObject.date}"/>
                    <g:if test="${SqlDateUtils.isToday(dashDueDate.dueDateObject.date)}">
                        <span class="la-popup-tooltip" data-content="${message(code:'myinst.dash.due_date.enddate.isDueToday.label')}" data-position="top right">
                            <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                        </span>
                    </g:if>
                    <g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.dueDateObject.date)}">
                        <span class="la-popup-tooltip" data-content="${message(code:'myinst.dash.due_date.enddate.isOverdue.label')}" data-position="top right">
                            <i class="${Icon.TOOLTIP.IMPORTANT} red"></i>
                        </span>
                    </g:elseif>
                </td>
                <td>
                    <div class="la-flexbox">
                        <g:if test="${obj instanceof Subscription}">
                            <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                            <g:link controller="subscription" action="show" id="${obj.id}">${obj.name}</g:link>
                        </g:if>
                        <g:elseif test="${obj instanceof License}">
                            <i class="${Icon.LICENSE} la-list-icon"></i>
                            <g:link controller="license" action="show" id="${obj.id}">${obj.name}</g:link>
                        </g:elseif>
                        <g:elseif test="${obj instanceof SurveyInfo}">
                            <i class="${Icon.SURVEY} la-list-icon"></i>
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <g:link controller="survey" action="show" params="[surveyConfigID: obj.surveyConfigs[0].id]"
                                        id="${obj.id}">${obj.surveyConfigs[0].getSurveyName()}
                                </g:link>
                            </g:if>
                            <g:else>
                                    <g:link controller="myInstitution" action="surveyInfos" params="[surveyConfigID: obj.surveyConfigs[0].id]"
                                            id="${obj.id}">${obj.surveyConfigs[0].getSurveyName()}</g:link>
                            </g:else>
                        </g:elseif>
                        <g:elseif test="${obj instanceof Task}">
                            <span data-position="top right" class="la-popup-tooltip" data-content="Aufgabe">
                                <i class="${Icon.TASK} la-list-icon"></i>
                            </span>
                            <g:if test="${obj.subscription}">
                                <g:link controller="subscription" action="show" id="${obj.subscription.id}">${obj.title}</g:link>
                            </g:if>
                            <g:elseif test="${obj.license}">
                                <g:link controller="license" action="show" id="${obj.license.id}">${obj.title}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.org}">
                                <g:link controller="organisation" action="show" id="${obj.org.id}">${obj.title}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.vendor}">
                                <g:link controller="vendor" action="show" id="${obj.vendor.id}">${obj.title}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.provider}">
                                <g:link controller="provider" action="show" id="${obj.provider.id}">${obj.title}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.tipp}">
                                <g:link controller="tipp" action="show" id="${obj.tipp.id}">${obj.tipp}</g:link>
                            </g:elseif>
                            <g:else>
                                <g:if test="${obj.status == RDStore.TASK_STATUS_OPEN}">
                                    <g:link controller="myInstitution" action="tasks" params="${[taskName:obj.title]}">${obj.title}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="myInstitution" action="tasks" params="${[taskName:obj.title, taskStatus:obj.status.id, ctrlFilterSend:true]}">${obj.title}</g:link>
                                    &nbsp; (${obj.status.getI10n('value')})
                                </g:else>
                            </g:else>
                        </g:elseif>
                        <g:elseif test="${obj instanceof AbstractPropertyWithCalculatedLastUpdated}">
                            <g:if test="${obj.owner instanceof Person}">
                                <i class="${Icon.ACP_PUBLIC} la-list-icon"></i>
                                ${obj.owner.first_name}&nbsp;${obj.owner.last_name}
                            </g:if>
                            <g:elseif test="${obj.owner instanceof Subscription}">
                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                <g:link controller="subscription" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.owner instanceof License}">
                                <i class="${Icon.LICENSE} la-list-icon"></i>
                                <g:link controller="license" action="show" id="${obj.owner?.id}">${obj.owner?.reference}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.owner instanceof Org}">
                                <ui:customerTypeIcon org="${obj.owner}" />
                                <g:link controller="organisation" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>
                            </g:elseif>
                            <g:else>
                                ${obj.owner?.name}
                            </g:else>
                        </g:elseif>
                        <g:else>
                            Not implemented yet!
                        </g:else>
                    </div>
                </td>
                <td class="center aligned">
                    <g:if test="${dashDueDate?.isHidden}">
                        <ui:remoteLink class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                          data-content="${message(code:'myinst.dash.due_dates.visibility.off.tooltip')}"
                                          controller="ajax"
                                          action="setDashboardDueDateVisibility"
                                          params='[visibility:true]'
                                          id="${dashDueDate.id}"
                                          data-confirm-tokenMsg="Möchten Sie diesen fälligen Termin wieder auf Ihrem Dashboard anzeigen lassen? "
                                          data-confirm-term-how="ok"
                                          data-done=""
                                          data-update="container-table"
                                          role="button"
                                          ariaLabel="Termin wieder auf Ihrem Dashboard anzeigen lassen"
                        >
                            <i class="${Icon.DUE_DATE} slash"></i>
                        </ui:remoteLink>
                    </g:if>
                    <g:else>
                        <ui:remoteLink class="${Btn.MODERN.POSITIVE_TOOLTIP}"
                                          data-content="${message(code:'myinst.dash.due_dates.visibility.on.tooltip')}"
                                          controller="ajax"
                                          action="setDashboardDueDateVisibility"
                                          params='[visibility:false]'
                                          id="${dashDueDate.id}"
                                          data-done=""
                                          data-update="container-table"
                                          role="button"
                                          ariaLabel="Termin nicht auf Ihrem Dashboard anzeigen lassen"
                        >
                            <i class="${Icon.DUE_DATE}"></i>
                        </ui:remoteLink>
                    </g:else>
                </td>
                <td class="center aligned">
                <g:if test="${dashDueDate?.dueDateObject.isDone}">
                    <ui:remoteLink class="${Btn.MODERN.POSITIVE_TOOLTIP}"
                                      data-content="${message(code:'myinst.dash.due_dates.status.pending.tooltip')}"
                                      controller="ajax"
                                      action="setDueDateObjectStatus"
                                      params='[done:false]'
                                      id="${dashDueDate.dueDateObject.id}"
                                      data-confirm-tokenMsg="Möchten Sie diesen fälligen Termin auf NICHT erledigt sezten? "
                                      data-confirm-term-how="ok"
                                      data-done=""
                                      data-update="container-table"
                                      role="button"
                                      ariaLabel="fälligen Termin auf NICHT erledigt sezten"
                    >
                        <i class="${Icon.SYM.YES}"></i>
                    </ui:remoteLink>
                </g:if>
                <g:else>
                    <ui:remoteLink class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                      data-content="${message(code:'myinst.dash.due_dates.status.done.tooltip')}"
                                      controller="ajax"
                                      action="setDueDateObjectStatus"
                                      params='[done:true]'
                                      id="${dashDueDate.dueDateObject.id}"
                                      data-done=""
                                      data-update="container-table"
                                      role="button"
                                      ariaLabel="${message(code:'ariaLabel.check.universal')}"
                    >
                        <i class="${Icon.SYM.YES}"></i>
                    </ui:remoteLink>
                </g:else>
                </td>
            </tr>
        </g:if>
    </g:each>
    </tbody>
</table>