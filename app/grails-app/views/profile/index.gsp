<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'profile', default: 'LAS:eR User Profile')}</title>
  </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="profile.bc.profile" class="active"/>
        </semui:breadcrumbs>

        <semui:messages data="${flash}" />

        <div>
            <div class="ui two column grid">

                <div class="column wide eight">

                    <g:form action="updateProfile" class="ui form">
                        <h4 class="ui dividing header">
                            ${message(code: 'profile.user', default:'User Profile')}
                        </h4>

                        <div class="field">
                            <label>${message(code: 'profile.display', default:'Display Name')}</label>
                            <input type="text" name="userDispName" value="${user.display}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.email', default:'Email Address')}</label>
                            <input type="text" name="email" value="${user.email}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'profile.dash', default:'Default Dashboard')}</label>

                            <select name="defaultDash" value="${user.defaultDash?.id}">
                                <g:each in="${user.authorizedOrgs}" var="o">
                                    <option value="${o.id}" ${user.defaultDash?.id==o.id?'selected':''}>${o.name}</option>
                                </g:each>
                            </select>
                        </div>

                        <div class="alert alert-info" style="width:95%">${message(code: 'profile.requests.text', default:'Please note, membership requests may be slow to process if you do not set a meaningful display name and email address. Please ensure these are set correctly before requesting institutional memberships')}</div>

                        <div class="field">
                            <label></label>
                            <button type="submit" class="ui primary button">${message(code: 'profile.update.button', default:'Update Profile')}</button>
                        </div>

                    </g:form>

                </div><!-- .column -->

                <div class="column wide eight">
                    <g:form action="updatePassword" class="ui form">

                        <h4 class="ui dividing header">
                            ${message(code: 'profile.password.label', default:'Update Password')}
                        </h4>

                        <div class="field">
                            <label>${message(code: 'profile.password.current', default:'Current Password')}</label>
                            <input type="password" name="passwordCurrent" value=""/>
                        </div>
                        <div class="field">
                            <label>${message(code: 'profile.password.new', default:'New Password')}</label>
                            <input type="text" name="passwordNew" value=""/>
                        </div>
                        <div class="field">
                            <label></label>
                            <button type="submit" class="ui primary button">${message(code: 'profile.password.update.button', default:'Update Password')}</button>
                        </div>

                    </g:form>
                </div><!-- .column -->

                <div class="column wide eight">
                    <div class="ui form">
                      <h4 class="ui dividing header">
                          ${message(code: 'profile.preferences', default:'Preferences')}
                      </h4>

                      <div class="field">
                          <label>${message(code: 'profile.info_icon', default:'Show Info Icon')}</label>
                          <g:xEditableRefData owner="${user}" field="showInfoIcon" config="YN" />
                      </div>

                      <div class="field">
                          <label>${message(code: 'profile.simpleViews', default:'Show simple Views')}</label>
                          <g:xEditableRefData owner="${user}" field="showSimpleViews" config="YN" />
                      </div>

                      <div class="field">
                          <label>${message(code: 'profile.pagesize', default:'Default Page Size')}</label>
                          <g:xEditable owner="${user}" field="defaultPageSize" />
                      </div>
                    </div>
                  </div><!-- .column -->

                <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | user.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">
                    <div class="column wide eight">
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
                                <label></label>
                                <g:link class="ui button" controller="api" action="index">${message(code:'api.linkTo', default:'Visit API')}</g:link>
                            </div>
                        </div><!-- form -->
                    </div><!-- .column -->
                </g:if>

        </div><!-- .grid -->
    </div><!-- -->

        <div>
            <div class="ui one column grid">
                <!--<div class="column wide sixteen">
                    <h4 class="ui dividing header">
                        ${message(code: 'profile.membership', default:'Administrative memberships')}
                    </h4>
                </div>-->

              <div class="column wide sixteen">
                <h4 class="ui dividing header">
                    ${message(code: 'profile.membership.existing')}
                </h4>
                          <table class="ui celled striped table">
                                <thead>
                                      <tr>
                                          <th>${message(code: 'profile.membership.org', default:'Organisation')}</th>
                                          <th>${message(code: 'profile.membership.role', default:'Role')}</th>
                                          <th>${message(code: 'profile.membership.status', default:'Status')}</th>
                                          <th>${message(code: 'profile.membership.date', default:'Date Requested / Actioned')}</th>
                                          <th>${message(code: 'profile.membership.actions', default:'Actions')}</th>
                                      </tr>
                                </thead>
                                <tbody>
                                      <g:each in="${user.affiliations}" var="assoc">
                                            <tr>
                                                  <td><g:link controller="organisations" action="info" id="${assoc.org.id}">${assoc.org.name}</g:link></td>
                                                  <td><g:message code="cv.roles.${assoc.formalRole?.authority}"/></td>
                                                  <td><g:message code="cv.membership.status.${assoc.status}"/></td>
                                                  <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${assoc.dateRequested}"/> / <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${assoc.dateActioned}"/></td>
                                                  <td style="vertical-align:middle">
                                                      <g:if test="${assoc.status != UserOrg.STATUS_CANCELLED}">
                                                          <g:link class="ui button" controller="profile" action="processCancelRequest" params="${[assoc:assoc.id]}">${message(code:'default.button.revoke.label', default:'Revoke')}</g:link>
                                                      </g:if>
                                                  </td>
                                            </tr>
                                      </g:each>
                                </tbody>
                          </table>
              </div><!--.column-->

                <div class="column wide eight">
                    <h4 class="ui dividing header">
                        ${message(code: 'profile.membership.request')}
                    </h4>

                      <p style="word-break:normal">
                          <g:message code="profile.membership.request.text" default="Select an organisation and a role below. Requests to join existing organisations will be referred to the administrative users of that organisation. If you feel you should be the administrator of an organisation please contact the ${message(code:'laser', default:'LAS:eR')} team for support." />
                      </p>

                  <g:form name="affiliationRequestForm" controller="profile" action="processJoinRequest" class="ui form" method="get">
                      <div class="field">
                        <label>Organisation</label>
                        <g:select name="org"
                          from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')}"
                          optionKey="id"
                          optionValue="name"
                          class="input-medium"/>
                      </div>

                      <div class="field">
                        <label>Role</label>
                        <g:select name="formalRole"
                              from="${com.k_int.kbplus.auth.Role.findAllByRoleType('user')}"
                              optionKey="id"
                              optionValue="${ {role->g.message(code:'cv.roles.'+role.authority) } }"
                              class="input-medium"/>
                      </div>

                      <div class="field">
                        <label></label>
                        <button id="submitARForm" data-complete-text="Request Membership" type="submit" class="ui primary button">${message(code: 'profile.membership.request.button', default:'Request Membership')}</button>
                      </div>
                  </g:form>
            </div><!--.column-->

            </div><!-- .grid -->
        </div><!-- -->

    <g:if test="${grailsApplication.config.feature.notifications}">

        <div>
            <div class="ui one column grid">
                <div class="column wide sixteen">
                    <h4 class="ui dividing header">${message(code: 'profile.misc', default:'Misc')}</h4>
                </div>
                <div class="column wide sixteen">
                    <div id="reminders">
                        <div class="well">
                            <h2>${message(code: 'profile.reminder.new', default:'Create new Reminders / Notifications')}</h2>
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

                                <button id="submitReminder" type="submit" class="ui primary button">${message(code:'default.button.create.label', default: 'Create')}</button>
                            </g:form>
                        </div>
                    </div><!-- #reminders -->
                </div><!-- .column -->


                <div class="column wide sixteen">
                    <div class="well">
                        <h2>${message(code: 'profile.reminder.active', default:'Active Reminders')}</h2>

                  <table class="ui celled striped table">
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
                                      <button data-op="delete" data-id="${r.id}" class="btn btn-small reminderBtn">${message(code:'default.button.remove.label', default:'Remove')}</button>&nbsp;/&nbsp;
                                      <button data-op="toggle" data-id="${r.id}" class="btn btn-small reminderBtn">${r.active? "${message(code:'default.button.disable.label', default:'disable')}":"${message(code:'default.button.enable.label', default:'enable')}"}</button>
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
  </body>
</html>

<r:script>
    $(document).ready(function () {
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
