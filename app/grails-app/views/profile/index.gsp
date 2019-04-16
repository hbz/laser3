<%@ page import="com.k_int.kbplus.RefdataCategory; de.laser.helper.RDStore; com.k_int.kbplus.UserSettings; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg; de.laser.helper.RDStore" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'profile', default: 'LAS:eR User Profile')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="profile.bc.profile" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'profile', default: 'LAS:eR User Profile')}</h1>

<semui:messages data="${flash}" />

<div class="ui two column grid">

    <div class="column wide eight">

        <div class="ui segment">

            <g:form action="updateProfile" class="ui form updateProfile">
                <h4 class="ui dividing header">
                    ${message(code: 'profile.user', default:'User Profile')}
                </h4>

                <div class="field">
                    <label>${message(code: 'profile.username', default:'User Name')}</label>
                    <input type="text" readonly="readonly" value="${user.username}"/>
                </div>

                <div class="field ">
                    <label>${message(code: 'profile.display', default:'Display Name')}</label>
                    <input type="text" name="userDispName" value="${user.display}"/>
                </div>

                <div class="field required">
                    <label>${message(code: 'profile.email', default:'Email Address')}</label>
                    <input type="text" id="email" name="email" value="${user.email}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'profile.dash', default:'Default Dashboard')}</label>

                    <select name="defaultDash" value="${user.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.id}" class="ui fluid dropdown">
                        <option value=""></option>
                        <g:each in="${user.authorizedOrgs}" var="o">
                            <option value="${o.class.name}:${o.id}" ${user.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.id==o.id?'selected':''}>${o.name}</option>
                        </g:each>
                    </select>
                </div>

                <%--
                    <div class="ui blue message">${message(code: 'profile.requests.text', default:'Please note, membership requests may be slow to process if you do not set a meaningful display name and email address. Please ensure these are set correctly before requesting institutional memberships')}</div>
                --%>

                <div class="field">
                    <button type="submit" class="ui button">${message(code: 'profile.update.button', default:'Update Profile')}</button>
                </div>

            </g:form>
            <r:script>


            </r:script>
        </div><!-- .segment -->

    </div><!-- .column -->

    <div class="column wide eight">

        <div class="ui segment">

            <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_YODA'))}">
                <g:img dir="images" file="yoda.gif" style="
                    position: absolute;
                    top: -40px;
                    right: -25px;
                    z-index: 9;
                    padding: 10px;
                    background: #fff;
                    border: 1px solid #ccc;
                    height: 240px;
                " />
            </g:if>

            <g:form action="updatePassword" class="ui form">

                <h4 class="ui dividing header">
                    ${message(code: 'profile.password.label', default:'Update Password')}
                </h4>

                <div class="field required">
                    <label>${message(code: 'profile.password.current', default:'Current Password')}</label>
                    <input type="password" name="passwordCurrent" required class="pw"/>
                </div>
                <div class="field required">
                    <label>${message(code: 'profile.password.new', default:'New Password')}</label>
                    <input type="password" name="passwordNew" required class="pw pwn"/>
                </div>
                <div class="field required">
                    <label>${message(code: 'profile.password.new.repeat', default:'New Password (Repeat)')}</label>
                    <input type="password" name="passwordNew2" required class="pw pwn"/>
                </div>
                <div class="field">
                    <label>${message(code: 'profile.password.show', default:'Show Passwords')}</label>
                    <input type="checkbox" name="showPasswords" id="passwordToggler">
                </div>
                <div class="field">
                    <label></label>
                    <button type="submit" class="ui button" id="passwordSubmit">${message(code: 'profile.password.update.button', default:'Update Password')}</button>
                </div>

            </g:form>
        </div><!-- .segment -->
    </div><!-- .column -->

    <div class="column wide eight">

        <div class="ui segment">

            <g:form action="updateReminderSettings" class="ui form updateReminderSettings">

                <h4 class="ui dividing header">
                    ${message(code: 'profile.reminder.label')}
                </h4>

                <div class="field">
                    <label>${message(code: 'profile.reminderPeriod')}</label>
                    <g:set var="US_DASHBOARD_REMINDER_PERIOD" value="${user.getSetting(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}" />
                    <div class="ui right labeled input">
                    <input type="number" name="dashboardReminderPeriod" value="${US_DASHBOARD_REMINDER_PERIOD.strValue}"/>
                        <div class="ui basic label">
                            ${message(code: 'profile.reminderDaysbeforeData')}
                        </div>
                    </div>
                    %{--TODO: strValue überprüfen--}%
                </div>

                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isRemindByEmail" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).rdValue == RDStore.YN_YES}"/>
                        <input type="checkbox" name="isRemindByEmail" class="hidden" value="Y" ${isRemindByEmail?'checked':''}/>
                        <label>${message(code: 'profile.isRemindByEmail')}</label>
                    </div>
                </div>
                <h5 class="ui header">
                    ${message(code: 'profile.reminder.for.label')}
                </h5>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isSubscriptionsNoticePeriod" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isSubscriptionsNoticePeriod" class="hidden" value="Y" ${isSubscriptionsNoticePeriod?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.subscriptions.noticePeriod')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isSubscriptionsEnddate" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isSubscriptionsEnddate" class="hidden" value="Y" ${isSubscriptionsEnddate?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.subscriptions.enddate')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isSubscriptionsCustomProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isSubscriptionsCustomProp" class="hidden" value="Y" ${isSubscriptionsCustomProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.subscriptions.customProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isSubscriptionsPrivateProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isSubscriptionsPrivateProp" class="hidden" value="Y" ${isSubscriptionsPrivateProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.subscriptions.privateProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isLicenseCustomProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isLicenseCustomProp" class="hidden" value="Y" ${isLicenseCustomProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.license.customProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isLicensePrivateProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isLicensePrivateProp" class="hidden" value="Y" ${isLicensePrivateProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.license.privateProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isPersonPrivateProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isPersonPrivateProp" class="hidden" value="Y" ${isPersonPrivateProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.person.privateProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isOrgCustomProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isOrgCustomProp" class="hidden" value="Y" ${isOrgCustomProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.org.customProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isOrgPrivateProp" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isOrgPrivateProp" class="hidden" value="Y" ${isOrgPrivateProp?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.org.privateProperty')}</label>
                    </div>
                </div>
                <div class="inline field">
                    <div class="ui checkbox">
                        <g:set var="isTasks" value="${user.getSetting(UserSettings.KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_YES).rdValue==RDStore.YN_YES}"/>
                        <input type="checkbox" name="isTasks" class="hidden" value="Y" ${isTasks?'checked':''}/>
                        <label>${message(code: 'profile.reminder.for.tasks')}</label>
                    </div>
                </div>

                <div class="inline field">
                    <button type="submit" class="ui button" id="reminderSubmit">${message(code: 'profile.reminder.submit')}</button>
                </div>
            </g:form>
        </div><!-- .segment -->
    </div><!-- .column -->

    <div class="column wide eight">
        <div class="ui segment">
            <div class="ui form">
                <h4 class="ui dividing header">
                    ${message(code: 'profile.preferences', default:'Preferences')}
                </h4>
                <%-- TODO: check this
                <div class="field">
                    <label>${message(code: 'profile.info_icon', default:'Show Info Icon')}</label>
                    <semui:xEditableRefData owner="${user}" field="showInfoIcon" config="YN" />
                </div>
                --%>
                <div class="field">
                    <label>${message(code: 'profile.theme', default:'Theme')}</label>
                    <g:set var="US_THEME" value="${user.getSetting(UserSettings.KEYS.THEME, RefdataValue.getByValueAndCategory('default', 'User.Settings.Theme'))}" />
                    <semui:xEditableRefData owner="${US_THEME}" field="rdValue" config="${US_THEME.key.rdc}" />
                </div>
                <div class="field">
                    <label>${message(code: 'profile.dashboardTab', default:'Dashboard Tab')}</label>
                    <g:set var="US_DASHBOARD_TAB" value="${user.getSetting(UserSettings.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', 'User.Settings.Dashboard.Tab'))}" />
                    <semui:xEditableRefData owner="${US_DASHBOARD_TAB}" field="rdValue" config="${US_DASHBOARD_TAB.key.rdc}" />
                </div>
                <div class="field">
                    <label>${message(code: 'profile.language', default:'Language')}</label>
                    <g:set var="US_LANGUAGE" value="${user.getSetting(UserSettings.KEYS.LANGUAGE, RefdataValue.getByValueAndCategory('de','Language'))}" />
                    <semui:xEditableRefData owner="${US_LANGUAGE}" field="rdValue" config="${US_LANGUAGE.key.rdc}" />
                    &nbsp;
                    <g:link controller="profile" action="index" class="ui button icon" style="float:right"><i class="icon sync"></i></g:link>
                </div>
                <div class="field">
                    <label>${message(code: 'profile.emailLanguage', default:'Language in E-Mails')}</label>
                    <g:set var="US_EMAIL_LANGUAGE" value="${user.getSetting(UserSettings.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de','Language'))}" />
                    <semui:xEditableRefData owner="${US_EMAIL_LANGUAGE}" field="rdValue" config="${US_EMAIL_LANGUAGE.key.rdc}" />
                </div>

                <div class="field">
                    <label>${message(code: 'profile.editMode', default:'Show Edit Mode')}</label>
                    <g:set var="US_SHOW_EDIT_MODE" value="${user.getSetting(UserSettings.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)}" />
                    <semui:xEditableRefData owner="${US_SHOW_EDIT_MODE}" field="rdValue" config="${US_SHOW_EDIT_MODE.key.rdc}" />
                </div>

                <div class="field">
                    <label>${message(code: 'profile.simpleViews', default:'Show simple Views')}</label>
                    <g:set var="US_SHOW_SIMPLE_VIEWS" value="${user.getSetting(UserSettings.KEYS.SHOW_SIMPLE_VIEWS, null)}" />
                    <semui:xEditableRefData owner="${US_SHOW_SIMPLE_VIEWS}" field="rdValue" config="${US_SHOW_SIMPLE_VIEWS.key.rdc}" />
                </div>

                <div class="field">
                    <label>${message(code: 'profile.itemsTimeWindow', default:'Default Page Size')}</label>
                    <semui:xEditable owner="${user.getSetting(UserSettings.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" field="strValue" />
                </div>

                <div class="field">
                    <label>${message(code: 'profile.pagesize', default:'Default Page Size')}</label>
                    <semui:xEditable owner="${user.getSetting(UserSettings.KEYS.PAGE_SIZE, 10)}" field="strValue" />
                </div>

            </div>
        </div><!-- .segment -->
    </div><!-- .column -->


    <%--
        <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | user.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">
            <div class="column wide eight">
                <div class="ui segment">
                    <div class="ui form">
                        <h4 class="ui dividing header">
                            ${message(code: 'api.label', default:'API')}
                        </h4>

                        <div class="field">
                            <label>${message(code: 'api.apikey.label', default:'API-Key')}</label>
                            <input type="text" readonly="readonly" value="${user.apikey}">
                        </div>

                        <div class="field">
                            <label>${message(code: 'api.apisecret.label', default:'API-Secret')}</label>
                            <input type="text" readonly="readonly" value="${user.apisecret}">
                        </div>

                        <div class="field">
                            <label>Berechtigungen</label>
                            <div class="ui list">
                                <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER'))}">
                                    <div class="item"><i class="icon check circle outline"></i> Lesend</div>
                                </g:if>
                                <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">
                                    <div class="item"><i class="icon check circle"></i> Schreibend</div>
                                </g:if>
                            </div>
                        </div>

                        <div class="field">
                            <label></label>
                            <g:link class="ui button" controller="api" action="index">${message(code:'api.linkTo', default:'Visit API')}</g:link>
                        </div>
                    </div><!-- form -->
                </div><!-- .segment -->
            </div><!-- .column -->
        </g:if>
    --%>

</div><!-- .grid -->

<br />
<br />
<br />

<div class="ui one column grid">
    <!--<div class="column wide sixteen">
                    <h4 class="ui dividing header">
                        ${message(code: 'profile.membership', default:'Administrative memberships')}
                    </h4>
                </div>-->

    <g:render template="/templates/user/membership_table" model="[userInstance: user, tmplProfile: true]" />

    <div class="column wide sixteen">
        <div class="ui segment">
            <h4 class="ui dividing header">
                ${message(code: 'profile.membership.request')}
            </h4>

            <p style="word-break:normal">
                <g:message code="profile.membership.request.text" default="Select an organisation and a role below. Requests to join existing organisations will be referred to the administrative users of that organisation. If you feel you should be the administrator of an organisation please contact the ${message(code:'laser', default:'LAS:eR')} team for support." />
            </p>

            <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableOrgs, availableOrgRoles: availableOrgRoles, tmplProfile: true]" />
        </div><!-- .segment -->
    </div><!--.column-->

</div><!-- .grid -->

<g:if test="${grailsApplication.config.feature.notifications}">

    <div>
        <div class="ui one column grid">
            <div class="column wide sixteen">
                <h4 class="ui dividing header">${message(code: 'profile.misc', default:'Misc')}</h4>
            </div>
            <div class="column wide sixteen">
                <div id="reminders">
                    <div class="ui segment">
                        <h2 class="ui header">${message(code: 'profile.reminder.new', default:'Create new Reminders / Notifications')}</h2>
                        <p>${message(code: 'profile.reminder.new.text', default:'Select the condition you are interested about and time period you wished to be notified about said topic.')}</p>
                        <p><i>${message(code: 'profile.reminder.new.email', default:'Ensure your email or other method of contact is a valid means of reaching yourself')}</i></p>

                        <g:form name="createReminder" controller="profile" action="createReminder" class="form-search" method="POST" url="[controller:'profile', action:'createReminder']">

                            <div class="field">
                                <label>${message(code: 'profile.reminder.new.notify', default:'Notify for')}</label>
                                <g:select name="trigger"
                                          from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderTrigger')}"
                                          optionKey="id"
                                          optionValue="${{it.getI10n('value')}}"
                                          class="input-medium"/>
                            </div>
                            <div class="field">
                                <label>${message(code: 'profile.reminder.new.method', default:'Method')}</label>
                                <g:select name="method"
                                          from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderMethod')}"
                                          optionKey="id"
                                          optionValue="${{it.getI10n('value')}}"
                                          class="input-medium"/>
                            </div>
                            <div class="field">
                                <label>${message(code: 'profile.reminder.new.period', default:'Period')}</label>
                                <g:select name="unit"
                                          from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderUnit')}"
                                          optionKey="id"
                                          optionValue="${{it.getI10n('value')}}"
                                          class="input-medium"/>
                            </div>
                            <div class="field">
                                <label>${message(code: 'profile.reminder.new.time', default:'Time')}</label>
                                <select name="val" class="input-medium required-indicator" id="val" value="${params.val}" data-type="select">
                                    <g:each in="${(1..7)}" var="s">
                                        <option value="${s}" ${s==params.long('val')?'selected="selected"':''}>${s}</option>
                                    </g:each>
                                </select>
                            </div>

                            <button id="submitReminder" type="submit" class="ui button">${message(code:'default.button.create.label', default: 'Create')}</button>
                        </g:form>
                    </div>
                </div><!-- #reminders -->
            </div><!-- .column -->


            <div class="column wide sixteen">
                <div class="ui segment">
                    <h2 class="ui header">${message(code: 'profile.reminder.active', default:'Active Reminders')}</h2>

                    <table class="ui celled la-table table">
                        <thead>
                        <tr>
                            <th><g:message code="reminder.trigger" default="Trigger"/></th>
                            <th><g:message code="reminder.method" default="Method"/></th>
                            <th>${message(code:'profile.reminder.new.time', default:'Time')} (<g:message code="reminder.unit" default="Unit"/>/<g:message code="reminder.number" default="Number"/>)</th>
                            <th><g:message code="reminder.lastNotification" default="Last Notification"/></th>
                            <th><g:message code="reminder.update" default="Delete / Disable"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:if test="${user.reminders.size() == 0}">
                            <tr><td colspan="5" style="text-align:center">&nbsp;<br/>${message(code:'reminder.none', default:'No reminders exist presently...')}<br/>&nbsp;</td></tr>
                        </g:if>
                        <g:else>
                            <g:each in="${user.reminders}" var="r">
                                <tr>
                                    <td>${r.trigger.value}</td>
                                    <td>${r.reminderMethod.value}</td>
                                    <td>${r.amount} ${r.unit.value}${r.amount >1? 's':''} before</td>
                                    <g:if test="${r.lastRan}"><td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${r.lastRan}" /></td></g:if>
                                    <g:else><td>${message(code:'reminder.never_ran', default:'Never executed!')}</td></g:else>
                                    <td>
                                        <button data-op="delete" data-id="${r.id}" class="ui button reminderBtn">${message(code:'default.button.remove.label', default:'Remove')}</button>&nbsp;/&nbsp;
                                        <button data-op="toggle" data-id="${r.id}" class="ui button reminderBtn">${r.active? "${message(code:'default.button.disable.label', default:'disable')}":"${message(code:'default.button.enable.label', default:'enable')}"}</button>
                                    </td>
                                </tr>
                            </g:each>
                        </g:else>
                        </tbody>
                    </table>

                </div>
            </div><!-- .column -->

        </div><!-- .grid -->
    </div><!-- -->

</g:if>

<r:script>
    $(document).ready(function () {
                    $('.updateProfile')
                            .form({
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
                    $('.updateReminderSettings')
                        .form({
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
        $('#passwordToggler').on('change', function(e) {
            $('input.pw').attr('type', ($(this).is(":checked") ? 'text' : 'password'))
        })

        $('#passwordSubmit').on('click', function(e) {
            e.preventDefault()
            var pw1 = $('input[name=passwordNew]')
            var pw2 = $('input[name=passwordNew2]')

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
                        setupUnitAmount(val,7)
                        break;
                    case 'Week':
                        setupUnitAmount(val,4)
                        break;
                    case 'Month':
                        setupUnitAmount(val,12)
                        break
                    default :
                        console.log('Impossible selection made!');
                        break
                }
            }
        });

        $(".reminderBtn").on('click', function (e) {
            //e.preventDefault();
            var element = $(this);
            var yn = confirm("Are you sure you wish to continue?");
            if(yn)
            {
                $.ajax({
                    method: 'POST',
                    url: "<g:createLink controller='profile' action='updateReminder'/>",
                        data: {
                        op: element.data('op'),
                        id: element.data('id')
                    }
                }).done(function(data) {
                    console.log(data)
                    data.op == 'delete'? element.parents('tr').remove() : element.text(data.active);
                });
            }

            //return false;
        });
    });

    function setupUnitAmount(type, amount) {
        console.log(type);
        type.children().remove()
        for (var i = 1; i <= amount; i++) {
            type.append('<option value="' + i + '">' + i + '</option>');
        }
    }
</r:script>

</body>
</html>
