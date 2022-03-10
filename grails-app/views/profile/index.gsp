<%@ page import="de.laser.UserSetting; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.auth.Role; de.laser.auth.UserOrg; de.laser.UserSetting.KEYS" %>
<%@ page import="de.laser.helper.RDStore; de.laser.helper.RDConstants" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: 'profile.user')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="profile.user" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
        <g:link class="ui item" controller="profile" action="delete">
            ${message(code:'profile.account.delete.button')}
        </g:link>
    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'profile.user')}</h1>

<semui:messages data="${flash}" />

<div class="ui two column grid la-clear-before">

    <div class="column wide eight">
        <div class="la-inline-lists">

            <div class="ui card">
                <div class="ui content">
                    <h2 class="ui dividing header">
                        ${message(code: 'profile.bc.profile')}
                    </h2>

                    <g:form action="updateProfile" class="ui form updateProfile">
                        <div class="field">
                            <label for="profile_username">${message(code: 'profile.username')}</label>
                            <input type="text" readonly="readonly" id="profile_username" value="${user.username}"/>
                        </div>
                        <div class="field ">
                            <label for="profile_display">${message(code: 'profile.display')}</label>
                            <input type="text" name="profile_display" id="profile_display" value="${user.display}"/>
                        </div>
                        <div class="field required">
                            <label for="profile_email">${message(code: 'profile.email')} <g:message code="messageRequiredField" /></label>
                            <input type="text" name="profile_email" id="profile_email" value="${user.email}"/>
                        </div>

                        <div class="field">
                            <label for="profile_dashboard">${message(code: 'profile.dash')}</label>
                            <select name="profile_dashboard" id="profile_dashboard" class="ui fluid dropdown">
                                <option value=""></option>
                                <g:each in="${user.authorizedOrgs}" var="o">
                                    <option value="${o.class.name}:${o.id}" ${user.getSettingsValue(KEYS.DASHBOARD)?.id == o.id ? 'selected' : ''}>${o.name}</option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <button type="submit" class="ui button">${message(code: 'profile.update.button')}</button>
                        </div>
                    </g:form><!-- updateProfile -->

                </div><!-- .content -->
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui dividing header">
                        ${message(code: 'profile.notification.label')}
                    </h2>

                    <g:form action="updateNotificationSettings" class="ui form updateNotificationSettings">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <g:set var="isNotificationByEmail" value="${user.getSetting(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                                <input type="checkbox" name="isNotificationByEmail" id="isNotificationByEmail" class="hidden" value="Y" ${isNotificationByEmail?'checked':''}/>
                                <label for="isNotificationByEmail">${message(code: 'profile.isNotificationByEmail')}</label>
                            </div>
                        </div>
                        <div class="inline field">
                            <div class="ui checkbox">
                                <g:set var="isNotificationCCByEmail" value="${user.getSetting(KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                                <input type="checkbox" name="isNotificationCCByEmail" id="isNotificationCCByEmail" class="hidden" value="Y" ${isNotificationCCByEmail?'checked':''}/>
                                <label for="isNotificationCCByEmail">${message(code: 'profile.isNotificationCCByEmail')}</label>
                            </div>
                            <g:set var="notificationCCEmailaddress" value="${user.getSettingsValue(KEYS.NOTIFICATION_CC_EMAILADDRESS)}"/>
                            <input type="text" id="emailCC" name="notificationCCEmailaddress" value="${notificationCCEmailaddress}"/>
                        </div>

                        <table class="ui celled la-js-responsive-table la-table compact table">
                            <thead>
                                <tr>
                                    <th></th>
                                    <th>${message(code: 'profile.notification.for.label')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isNotificationForSurveysStart" value="${user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_NO).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isNotificationForSurveysStart" class="hidden" value="Y" ${isNotificationForSurveysStart?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.notification.for.SurveysStart')}</td>
                                </tr>

                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isNotificationForSurveysParticipationFinish"
                                                   value="${user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isNotificationForSurveysParticipationFinish"
                                                   class="hidden"
                                                   value="Y" ${isNotificationForSurveysParticipationFinish ? 'checked' : ''}/>
                                        </div>
                                    </td>
                                    <td>${contextService.getOrg().getCustomerType()  == 'ORG_CONSORTIUM' ? message(code: 'profile.notification.for.SurveysParticipationFinish') : message(code: 'profile.notification.for.SurveysParticipationFinish2')}</td>
                                </tr>

                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isNotificationForSystemMessages" value="${user.getSetting(KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_NO).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isNotificationForSystemMessages" class="hidden" value="Y" ${isNotificationForSystemMessages?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.notification.for.SystemMessages')}</td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="inline field">
                            <button type="submit" class="ui button" id="notificationSubmit">${message(code: 'profile.notification.submit')}</button>
                        </div>
                    </g:form><!-- updateNotificationSettings -->

                </div><!-- .content -->
            </div><!-- .card -->

            <div class="ui card">
                <div class="ui content">
                    <h2 class="ui dividing header">
                        ${message(code: 'profile.reminder.label')}
                    </h2>

                    <g:form action="updateReminderSettings" class="ui form updateReminderSettings">

                        <div class="inline field">
                            <div class="ui checkbox">
                                <g:set var="isRemindByEmail" value="${user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                                <input type="checkbox" name="isRemindByEmail" id="isRemindByEmail" class="hidden" value="Y" ${isRemindByEmail?'checked':''}/>
                                <label for="isRemindByEmail">${message(code: 'profile.isRemindByEmail')}</label>
                            </div>
                        </div>
                        <div class="inline field">
                            <div class="ui checkbox">
                                <g:set var="isRemindCCByEmail" value="${user.getSetting(KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                                <input type="checkbox" name="isRemindCCByEmail" id="isRemindCCByEmail" class="hidden" value="Y" ${isRemindCCByEmail?'checked':''}/>
                                <label for="isRemindCCByEmail">${message(code: 'profile.isRemindCCByEmail')}</label>
                            </div>
                            <g:set var="remindCCEmailaddress" value="${user.getSettingsValue(KEYS.REMIND_CC_EMAILADDRESS)}"/>
                            <input type="text" id="emailCC" name="remindCCEmailaddress" value="${remindCCEmailaddress}"/>
                        </div>

                        <table class="ui celled la-js-responsive-table la-table compact table">
                            <g:set var="defaultRemindPeriod" value="${UserSetting.DEFAULT_REMINDER_PERIOD}" />
                            <thead>
                            <tr>
                                <th></th>
                                <th>${message(code: 'profile.reminder.for.label')}</th>
                                <th>${message(code: 'profile.reminderDaysbeforeData')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:if test="${ ! isOrgBasicMember}">
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isSubscriptionsNoticePeriod" value="${user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isSubscriptionsNoticePeriod" class="hidden" value="Y" ${isSubscriptionsNoticePeriod?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.subscriptions.noticePeriod')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForSubscriptionNoticeperiod" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isSubscriptionsEnddate" value="${user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isSubscriptionsEnddate" class="hidden" value="Y" ${isSubscriptionsEnddate?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.subscriptions.enddate')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForSubscriptionEnddate" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isSubscriptionsCustomProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isSubscriptionsCustomProp" class="hidden" value="Y" ${isSubscriptionsCustomProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.subscriptions.customProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForSubscriptionsCustomProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isSubscriptionsPrivateProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isSubscriptionsPrivateProp" class="hidden" value="Y" ${isSubscriptionsPrivateProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.subscriptions.privateProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForSubscriptionsPrivateProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isLicenseCustomProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isLicenseCustomProp" class="hidden" value="Y" ${isLicenseCustomProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.license.customProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForLicenseCustomProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isLicensePrivateProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isLicensePrivateProp" class="hidden" value="Y" ${isLicensePrivateProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.license.privateProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForLicensePrivateProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isPersonPrivateProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isPersonPrivateProp" class="hidden" value="Y" ${isPersonPrivateProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.person.privateProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForPersonPrivateProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isOrgCustomProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isOrgCustomProp" class="hidden" value="Y" ${isOrgCustomProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.org.customProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForOrgCustomProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isOrgPrivateProp" value="${user.getSetting(KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isOrgPrivateProp" class="hidden" value="Y" ${isOrgPrivateProp?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.org.privateProperty')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForOrgPrivateProp" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <div class="ui checkbox">
                                            <g:set var="isTasks" value="${user.getSetting(KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                            <input type="checkbox" name="isTasks" class="hidden" value="Y" ${isTasks?'checked':''}/>
                                        </div>
                                    </td>
                                    <td>${message(code: 'profile.reminder.for.tasks')}</td>
                                    <td>
                                        <input type="number" name="remindPeriodForTasks" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_TASKS, defaultRemindPeriod)?.strValue}"/>
                                    </td>
                                </tr>
                            </g:if>
                            <tr>
                                <td>
                                    <div class="ui checkbox">
                                        <g:set var="isSurveysNotMandatoryEndDate" value="${user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                        <input type="checkbox" name="isSurveysNotMandatoryEndDate" class="hidden" value="Y" ${isSurveysNotMandatoryEndDate?'checked':''}/>
                                    </div>
                                </td>
                                <td>${message(code: 'profile.reminder.for.surveys.endDate')}</td>
                                <td>
                                    <input type="number" name="remindPeriodForSurveysEndDate" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, defaultRemindPeriod)?.strValue}"/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div class="ui checkbox">
                                        <g:set var="isSurveysMandatoryEndDate" value="${user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                                        <input type="checkbox" name="isSurveysMandatoryEndDate" class="hidden" value="Y" ${isSurveysMandatoryEndDate?'checked':''}/>
                                    </div>
                                </td>
                                <td>${message(code: 'profile.reminder.for.surveysMandatory.endDate')}</td>
                                <td>
                                    <input type="number" name="remindPeriodForSurveysMandatoryEndDate" value="${user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, defaultRemindPeriod)?.strValue}"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div class="inline field">
                            <button type="submit" class="ui button" id="reminderSubmit">${message(code: 'profile.reminder.submit')}</button>
                        </div>
                    </g:form><!-- updateReminderSettings -->
                </div><!-- .content -->
            </div><!-- .card -->

        </div><!-- .la-inline-lists -->
    </div><!-- .column -->

    <div class="column wide eight">
        <div class="la-inline-lists">

            <div class="ui card la-js-changePassword">
                <div class="content">
                    <h2 class="ui dividing header">
                        ${message(code: 'profile.password.label')}
                    </h2>

                    <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_YODA'))}">
                        <div id="profile-image">
                            <g:if test="${user.image}">
                                <g:img dir="images" file="profile/${user.image}" />
                            </g:if>
                            <g:else>
                                <g:img dir="images" file="profile/yoda.gif" />
                            </g:else>
                            <style>
                                #profile-image {
                                    position: absolute;
                                    top: -25px;
                                    right: -25px;
                                    z-index: 9;
                                    perspective: 800px;
                                }
                                #profile-image img {
                                    padding: 10px;
                                    background: #fff;
                                    border: 1px solid #ccc;
                                    height: 240px;
                                    transition: transform 1s;
                                }
                                .la-js-changePassword:hover #profile-image img {
                                    transform: rotateY(180deg);
                                    transform-style: preserve-3d;
                                }
                            </style>
                        </div>
                    </g:if>

                    <g:form action="updatePassword" class="ui form updatePassword">
                        <div class="field required">
                            <label for="password_current">${message(code: 'profile.password.current')} <g:message code="messageRequiredField" /></label>
                            <input type="password" name="password_current" id="password_current" class="pw"/>
                        </div>
                        <div class="field required">
                            <label for="password_new">${message(code: 'profile.password.new')} <g:message code="messageRequiredField" /></label>
                            <input type="password" name="password_new" id="password_new" class="pw pwn"/>
                        </div>
                        <div class="field required">
                            <label for="password_new_repeat">${message(code: 'profile.password.new.repeat')} <g:message code="messageRequiredField" /></label>
                            <input type="password" name="password_new_repeat" id="password_new_repeat" class="pw pwn"/>
                        </div>
                        <div class="field">
                            <label for="password_show_toggler">${message(code: 'profile.password.show')}</label>
                            <input type="checkbox" name="showPasswords" id="password_show_toggler">
                        </div>
                        <div class="field">
                            <label></label>
                            <button type="submit" class="ui button" id="password_submit">${message(code: 'profile.password.update.button')}</button>
                        </div>
                    </g:form><!-- updatePassword -->

                </div><!-- .content -->
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui dividing header">
                        ${message(code: 'profile.preferences')}
                    </h2>

                    <div class="ui form">
                        <%-- TODO: check this
                        <div class="field">
                            <label>${message(code: 'profile.info_icon')}</label>
                            <semui:xEditableRefData owner="${user}" field="showInfoIcon" config="${RDConstants.Y_N}" />
                        </div>
                        --%>
                        <div class="field">
                            <label>${message(code: 'profile.theme')}</label>
                            <g:set var="US_THEME" value="${user.getSetting(KEYS.THEME, RefdataValue.getByValueAndCategory('default', RDConstants.USER_SETTING_THEME))}" />
                            <semui:xEditableRefData owner="${US_THEME}" field="rdValue" config="${US_THEME.key.rdc}" />
                        </div>
                        <div class="field">
                            <label>${message(code: 'profile.dashboardTab')}</label>
                            <g:set var="US_DASHBOARD_TAB" value="${user.getSetting(KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))}" />
                            <semui:xEditableRefData owner="${US_DASHBOARD_TAB}" field="rdValue" config="${US_DASHBOARD_TAB.key.rdc}" />
                        </div>
                        <div class="field">
                            <label>${message(code: 'profile.language')}</label>
                            <g:set var="US_LANGUAGE" value="${user.getSetting(KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE))}" />
                            <semui:xEditableRefData owner="${US_LANGUAGE}" field="rdValue" config="${US_LANGUAGE.key.rdc}" />
                            &nbsp;
                            <g:link controller="profile" action="index" class="ui button icon" style="float:right"><i class="icon sync"></i></g:link>
                        </div>
                        <div class="field">
                            <label>${message(code: 'profile.emailLanguage')}</label>
                            <g:set var="US_EMAIL_LANGUAGE" value="${user.getSetting(KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE))}" />
                            <semui:xEditableRefData owner="${US_EMAIL_LANGUAGE}" field="rdValue" config="${US_EMAIL_LANGUAGE.key.rdc}" />
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.editMode')}</label>
                            <g:set var="US_SHOW_EDIT_MODE" value="${user.getSetting(KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)}" />
                            <semui:xEditableRefData owner="${US_SHOW_EDIT_MODE}" field="rdValue" config="${US_SHOW_EDIT_MODE.key.rdc}" />
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.simpleViews')}</label>
                            <g:set var="US_SHOW_SIMPLE_VIEWS" value="${user.getSetting(KEYS.SHOW_SIMPLE_VIEWS, null)}" />
                            <semui:xEditableRefData owner="${US_SHOW_SIMPLE_VIEWS}" field="rdValue" config="${US_SHOW_SIMPLE_VIEWS.key.rdc}" />
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.extendedFilter')}</label>
                            <g:set var="US_SHOW_EXTENDED_FILTER" value="${user.getSetting(KEYS.SHOW_EXTENDED_FILTER, RDStore.YN_YES)}" />
                            <semui:xEditableRefData owner="${US_SHOW_EXTENDED_FILTER}" field="rdValue" config="${US_SHOW_EXTENDED_FILTER.key.rdc}" />
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.itemsTimeWindow')}</label>
                            <semui:xEditable owner="${user.getSetting(KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" field="strValue" />
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.pagesize')}</label>
                            <semui:xEditable owner="${user.getSetting(KEYS.PAGE_SIZE, 10)}" field="strValue" />
                        </div>

                    </div>
                </div><!-- .content -->
            </div><!-- .card -->

        </div><!-- .la-inline-lists -->
    </div><!-- .column -->

</div><!-- .grid -->

    <div class="la-inline-lists">
        <g:render template="/templates/user/membership_table" model="[userInstance: user]" />
    </div>

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <div class="column wide sixteen">
            <div class="la-inline-lists">

                <div class="ui card la-full-width">
                    <div class="content">
                        %{-- <h2 class="ui dividing header">
                            ${message(code: 'profile.membership.request')}
                        </h2>

                        -- outdated due instant accept: <p style="word-break:normal">
                            <g:message code="profile.membership.request.text" />
                        </p>--}%
                        <g:form controller="profile" action="addAffiliation" class="ui form addAffiliationForm" method="get">
                            <div class="two fields">
                                <div class="field">
                                    <label for="org">Organisation</label>
                                    <g:select name="org" id="org"
                                              from="${availableOrgs}"
                                              optionKey="id"
                                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                                              class="ui fluid search dropdown"/>
                                </div>

                                <div class="field">
                                    <label for="formalRole">Role</label>
                                    <g:select name="formalRole" id="formalRole"
                                              from="${Role.findAllByRoleType('user')}"
                                              optionKey="id"
                                              optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                              value="${Role.findByAuthority('INST_USER').id}"
                                              class="ui fluid dropdown"/>
                                </div>
                            </div>

                            <div class="field">
                                <button id="submitARForm" data-complete-text="Request Membership" type="submit" class="ui button">${message(code: 'profile.membership.add.button')}</button>
                            </div>
                        </g:form>
                    </div><!-- .content -->
                </div><!-- .card -->

            </div><!-- .la-inline-lists -->
        </div><!--.column-->
    </sec:ifAnyGranted>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.setupUnitAmount = function (type, amount) {
        console.log(type);
        type.children().remove()
        for (var i = 1; i <= amount; i++) {
            type.append('<option value="' + i + '">' + i + '</option>');
        }
    }

                    $('.updateProfile').form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            email: {
                                identifier  : 'email',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    }
                                ]
                            }
                        }
                    });
                    $('.la-js-changePassword .form').form({
                        on: 'change',
                        inline: true,
                        fields: {
                            password_current: {
                                identifier  : 'password_current',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    }
                                ]
                            },
                            password_new: {
                                identifier  : 'password_new',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    }
                                ]
                            },
                            password_new_repeat: {
                                identifier  : 'password_new_repeat',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" default=" muss ausgefüllt werden" />'
                                    },
                                    {
                                        type: 'match[password_new]',
                                        prompt : '{name} <g:message code="validation.mustMatch" default=" muss übereinstimmen" />'
                                    }
                                ]
                            }
                        }
                    });
                    $('.updateReminderSettings').form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            dashboardReminderPeriod: {
                                identifier  : 'dashboardReminderPeriod',
                                rules: [
                                    {
                                        type   : 'regExp[/^[0-9]/]',
                                        prompt : '{name} <g:message code="validation.onlyInteger" default=" darf nur aus Ziffern bestehen" />'
                                    }
                                ]
                            }
                        }
                    });

        $('#password_show_toggler').on('change', function(e) {
            $('input.pw').attr('type', ($(this).is(":checked") ? 'text' : 'password'))
        })

        $('#password_submit').on('click', function(e) {
            e.preventDefault()
            var pw1 = $('input[name=password_new]')
            var pw2 = $('input[name=password_new_repeat]')

            $('input.pwn').parents('div.field').removeClass('error')

            if ( pw1.val() && (pw1.val() == pw2.val()) ) {
                $(this).parents('form').submit()
            } else {
                $('input.pwn').parents('div.field').addClass('error')
            }
        })

        $("#unit").on('change', function (e) {
            var unit = this.options[e.target.selectedIndex].text;
            var val = $(this).next();
            if (unit) {
                switch (unit) {
                    case 'Day':
                        JSPC.app.setupUnitAmount(val,7)
                        break;
                    case 'Week':
                        JSPC.app.setupUnitAmount(val,4)
                        break;
                    case 'Month':
                        JSPC.app.setupUnitAmount(val,12)
                        break
                    default :
                        console.log('Impossible selection made!');
                        break
                }
            }
        });

        $('#isRemindByEmail').change( function (e) {
            if (this.checked) {
                $('#isRemindCCByEmail').attr("disabled", false);
            } else {
                $('#isRemindCCByEmail').attr("disabled", true);
            }
        });

        $('#isRemindByEmail').trigger('change');

</laser:script>

</body>
</html>
