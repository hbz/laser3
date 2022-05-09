<%@ page import="de.laser.Person; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DueDateObject; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.SqlDateUtils; de.laser.*; de.laser.DashboardDueDate" %>
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
    ${dashDueDate}
    <g:each in="${dueDates}" var="dashDueDate">
        <g:set var="obj" value="${dashDueDate? genericOIDService.resolveOID(dashDueDate.dueDateObject.oid) : null}"/>
        <g:if test="${obj}">
            <tr>
                <td>
                    <g:if test="${obj instanceof AbstractPropertyWithCalculatedLastUpdated}">
                        <i class="icon tags la-list-icon"></i>
                    </g:if>
                %{--${dashDueDate.id} &nbsp--}%
                    <g:if test="${Locale.GERMAN.getLanguage() == LocaleContextHolder.getLocale().getLanguage()}">
                        ${dashDueDate.dueDateObject.attribute_value_de}
                    </g:if>
                    <g:else>
                        ${dashDueDate.dueDateObject.attribute_value_en}
                    </g:else>
                </td>
                <td>
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${dashDueDate.dueDateObject.date}"/>
                    <g:if test="${SqlDateUtils.isToday(dashDueDate.dueDateObject.date)}">
                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'myinst.dash.due_date.enddate.isDueToday.label')}" data-position="top right">
                            <i class="icon yellow exclamation"></i>
                        </span>
                    </g:if>
                    <g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.dueDateObject.date)}">
                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'myinst.dash.due_date.enddate.isOverdue.label')}" data-position="top right">
                            <i class="icon red exclamation"></i>
                        </span>
                    </g:elseif>
                </td>
                <td>
                    <div class="la-flexbox">
                        <g:if test="${obj instanceof Subscription}">
                            <i class="icon clipboard outline la-list-icon"></i>
                            <g:link controller="subscription" action="show" id="${obj.id}">${obj.name}</g:link>
                        </g:if>
                        <g:elseif test="${obj instanceof License}">
                            <i class="icon balance scale la-list-icon"></i>
                            <g:link controller="license" action="show" id="${obj.id}">${obj.name}</g:link>
                        </g:elseif>
                        <g:elseif test="${obj instanceof SurveyInfo}">
                            <i class="icon chart pie la-list-icon"></i>
                            <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
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
                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="Aufgabe">
                                <i class="icon checked calendar la-list-icon"></i>
                            </span>
                            <a href="#" class="header" onclick="JSPC.app.taskedit(${obj?.id});">${obj?.title}</a>
                        </g:elseif>
                        <g:elseif test="${obj instanceof AbstractPropertyWithCalculatedLastUpdated}">
                            <g:if test="${obj.owner instanceof Person}">
                                <i class="icon address book la-list-icon"></i>
                                <${obj.owner.first_name}&nbsp;${obj.owner.last_name}
                            </g:if>
                            <g:elseif test="${obj.owner instanceof Subscription}">
                                <i class="icon clipboard outline la-list-icon"></i>
                                <g:link controller="subscription" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.owner instanceof License}">
                                <i class="icon balance scale la-list-icon"></i>
                                <g:link controller="license" action="show" id="${obj.owner?.id}">${obj.owner?.reference}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj.owner instanceof Org}">
                                <i class="icon university la-list-icon"></i>
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
                <td class="x" >
                    <g:if test="${false}">
                        <laser:remoteLink class="ui icon  negative button la-modern-button js-open-confirm-modal"
                                          controller="ajax"
                                          action="deleteDashboardDueDate_does_not_exist_yet"
                                          params=''
                                          id="${genericOIDService.getOID(dashDueDate)}"
                                          data-confirm-tokenMsg="Möchten Sie wirklich diesen fälligen Termin aus dem System löschen?"
                                          data-confirm-term-how="ok"

                                          data-done=""
                                          data-update="container-table"
                                          role="button"
                                          ariaLabel="${message(code:'ariaLabel.delete.universal')}"
                        >
                            <i class="trash alternate outline icon"></i>
                        </laser:remoteLink>
                    </g:if>
                    <g:if test="${dashDueDate?.isHidden}">
                        <laser:remoteLink class="ui icon blue button la-modern-button la-popup-tooltip la-delay"
                                          data-content="${message(code:'myinst.dash.due_dates.visibility.off.tooltip')}"
                                          controller="ajax"
                                          action="showDashboardDueDate"
                                          params='[owner:"${dashDueDate.class.name}:${dashDueDate.id}"]'
                                          id="${dashDueDate.id}"
                                          data-confirm-tokenMsg="Möchten Sie diesen fälligen Termin wieder auf Ihrem Dashboard anzeigen lassen? "
                                          data-confirm-term-how="ok"
                                          data-done=""
                                          data-update="container-table"
                                          role="button"
                                          ariaLabel="Termin wieder auf Ihrem Dashboard anzeigen lassen"
                        >
                            <i class="icon bell slash la-js-editmode-icon "></i>
                        </laser:remoteLink>
                    </g:if>
                    <g:else>
                        <laser:remoteLink class="ui icon green button la-modern-button la-popup-tooltip la-delay"
                                          data-content="${message(code:'myinst.dash.due_dates.visibility.on.tooltip')}"
                                          controller="ajax"
                                          action="hideDashboardDueDate"
                                          params='[owner:"${dashDueDate.class.name}:${dashDueDate.id}"]'
                                          id="${dashDueDate.id}"
                                          data-done=""
                                          data-update="container-table"
                                          role="button"
                                          ariaLabel="Termin nicht auf Ihrem Dashboard anzeigen lassen"
                        >
                            <i class="icon bell la-js-editmode-icon"></i>
                        </laser:remoteLink>
                    </g:else>
                </td>
                <td class="x" >
                <g:if test="${dashDueDate?.dueDateObject.isDone}">
                    <laser:remoteLink class="ui green button la-modern-button la-popup-tooltip la-delay"
                                      data-content="${message(code:'myinst.dash.due_dates.status.pending.tooltip')}"
                                      controller="ajax"
                                      action="dashboardDueDateSetIsUndone"
                                      params='[owner:"${dashDueDate.dueDateObject.class.name}:${dashDueDate.dueDateObject.id}"]'
                                      id="${dashDueDate.dueDateObject.id}"
                                      data-confirm-tokenMsg="Möchten Sie diesen fälligen Termin auf NICHT erledigt sezten? "
                                      data-confirm-term-how="ok"
                                      data-done=""
                                      data-update="container-table"
                                      role="button"
                                      ariaLabel="fälligen Termin auf NICHT erledigt sezten"
                    >
                        <i class="icon check la-js-editmode-icon"></i>
                    </laser:remoteLink>
                </g:if>
                <g:else>
                    <laser:remoteLink class="ui icon blue button la-modern-button la-popup-tooltip la-delay"
                                      data-content="${message(code:'myinst.dash.due_dates.status.done.tooltip')}"
                                      controller="ajax"
                                      action="dashboardDueDateSetIsDone"
                                      params='[owner:"${dashDueDate.dueDateObject.class.name}:${dashDueDate.dueDateObject.id}"]'
                                      id="${dashDueDate.dueDateObject.id}"
                                      data-done=""
                                      data-update="container-table"
                                      role="button"
                                      ariaLabel="${message(code:'ariaLabel.check.universal')}"
                    >
                        <i class="icon check la-js-editmode-icon"></i>
                    </laser:remoteLink>
                </g:else>
                </td>
            </tr>
        </g:if>
    </g:each>
    </tbody>
</table>