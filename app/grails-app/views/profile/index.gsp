<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code: 'profile', default: 'LAS:eR User Profile')}</title>
  </head>

  <body>

    <laser:breadcrumbs>
        <laser:crumb message="profile.bc.profile" class="active"/>
    </laser:breadcrumbs>

    <g:if test="${flash.error}">
        <div class="container">
            <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
        </div>
    </g:if>

    <g:if test="${flash.message}">
        <div class="container">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </div>
    </g:if>

    <div class="container">
        <div class="row">

            <div class="span6">
              <h2>${message(code: 'profile.user', default:'User Profile')}</h2>

              <g:form action="updateProfile" class="form-inline">
                  <dl class="dl-horizontal">

                  <div class="control-group">
                      <dt>${message(code: 'profile.display', default:'Display Name')}</dt>
                      <dd><input type="text" name="userDispName" value="${user.display}"/></dd>
                  </div>

                  <div class="control-group">
                      <dt>${message(code: 'profile.email', default:'Email Address')}</dt>
                      <dd><input type="text" name="email" value="${user.email}"/></dd>
                  </div>

                  <!--<div class="control-group">
                      <dt>${message(code: 'profile.pagesize', default:'Default Page Size')}</dt>
                      <dd><input type="text" name="defaultPageSize" value="${user.defaultPageSize}"/></dd>
                  </div>-->

                  <div class="control-group">
                      <dt>${message(code: 'profile.dash', default:'Default Dashboard')}</dt>
                      <dd>
                          <select name="defaultDash" value="${user.defaultDash?.id}">
                              <g:each in="${user.authorizedOrgs}" var="o">
                                  <option value="${o.id}" ${user.defaultDash?.id==o.id?'selected':''}>${o.name}</option>
                              </g:each>
                          </select>
                      </dd>
                  </div>

                  <p style="width:95%">${message(code: 'profile.requests.text', default:'Please note, membership requests may be slow to process if you do not set a meaningful display name and email address. Please ensure these are set correctly before requesting institutional memberships')}</p>

                  <div class="control-group">
                      <dt></dt>
                      <dd><input type="submit" value="${message(code: 'profile.update.button', default:'Update Profile')}" class="btn btn-primary"/></dd>
                  </div>

              </g:form>

          </div><!-- .span6 -->

            <div class="span6">
                <h2>${message(code: 'profile.password.label', default:'Update Password')}</h2>

                <g:form action="updatePassword" class="form-inline">
                    <dl class="dl-horizontal">
                        <div class="control-group">
                            <dt>${message(code: 'profile.password.current', default:'Current Password')}</dt>
                            <dd><input type="password" name="passwordCurrent" value=""/></dd>
                        </div>
                        <div class="control-group">
                            <dt>${message(code: 'profile.password.new', default:'New Password')}</dt>
                            <dd><input type="text" name="passwordNew" value=""/></dd>
                        </div>
                        <div class="control-group">
                            <dt></dt>
                            <dd><input type="submit" value="${message(code: 'profile.password.update.button', default:'Update Password')}" class="btn btn-primary"/></dd>
                        </div>
                    </dl>
                </g:form>
            </div><!-- .span6 -->

        </div><!-- .row -->
    </div><!-- .container -->

  <div class="container">
      <div class="row">

          <div class="span6">
              <h2>${message(code: 'profile.preferences', default:'Preferences')}</h2>

              <dl class="dl-horizontal">
                  <div class="control-group">
                      <dt>${message(code: 'profile.info_icon', default:'Show Info Icon')}</dt>
                      <dd>
                          <g:xEditableRefData owner="${user}" field="showInfoIcon" config="YN" />
                      </dd>
                  </div>

                  <div class="control-group">
                      <dt>${message(code: 'profile.simpleViews', default:'Show simple Views')}</dt>
                      <dd>
                          <g:xEditableRefData owner="${user}" field="showSimpleViews" config="YN" />
                      </dd>
                  </div>

                  <div class="control-group">
                      <dt>${message(code: 'profile.pagesize', default:'Default Page Size')}</dt>
                      <dd>
                          <g:xEditable owner="${user}" field="defaultPageSize" />
                      </dd>
                  </div>
              </dl>
          </div><!-- .span6 -->

          <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | user.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">

              <div class="span6">
                  <h2>${message(code: 'api.label', default:'API')}</h2>
                  <dl class="dl-horizontal">
                      <dt>${message(code: 'api.apikey.label', default:'API-Key')}</dt>
                      <dd>
                          <input type="text" readonly="readonly" value="${user.apikey}">
                      </dd>
                      <dt>${message(code: 'api.apisecret.label', default:'API-Secret')}</dt>
                      <dd>
                          <input type="text" readonly="readonly" value="${user.apisecret}">
                      </dd>
                      <dd>
                          <g:link controller="api" action="index">&rArr; ${message(code:'api.linkTo', default:'Visit API')}</g:link>
                      </dd>
                  </dl>
              </div><!-- .span6 -->
          </g:if>

        </div><!-- .row -->
    </div><!-- .container -->

    <div class="container">
        <div class="span12">
            <h2>${message(code: 'profile.membership', default:'Administrative memberships')}</h2>
        </div>
    </div>

    <div class="container">
        <div class="row">
              <div class="span6">
                    <laser:card title="profile.membership.existing" class="card-grey">
                          <table class="table table-striped table-bordered table-condensed" style="word-break:normal;">
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
                                                  <td><g:formatDate format="dd MMMM yyyy" date="${assoc.dateRequested}"/> / <g:formatDate format="dd MMMM yyyy" date="${assoc.dateActioned}"/></td>
                                                  <td><!--<button class="btn">Remove</button>--></td>
                                            </tr>
                                      </g:each>
                                </tbody>
                          </table>
                    </laser:card>
              </div><!--.span6-->

            <div class="span6">
                <laser:card title="profile.membership.request" class="card-grey">

                  <p style="word-break:normal"><g:message code="profile.membership.request.text" default="Select an organisation and a role below. Requests to join existing organisations will be referred to the administrative users of that organisation. If you feel you should be the administrator of an organisation please contact the ${message(code:'laser', default:'LAS:eR')} team for support." />
                  </p>

                  <g:form name="affiliationRequestForm" controller="profile" action="processJoinRequest" class="form-search" method="get">

                    <g:select name="org"
                              from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')}"
                              optionKey="id"
                              optionValue="name"
                              class="input-medium"/>

                    <g:select name="formalRole"
                              from="${com.k_int.kbplus.auth.Role.findAllByRoleType('user')}"
                              optionKey="id"
                              optionValue="${ {role->g.message(code:'cv.roles.'+role.authority) } }"
                              class="input-medium"/>

                    <button id="submitARForm" data-complete-text="Request Membership" type="submit" class="btn btn-primary btn-small">${message(code: 'profile.membership.request.button', default:'Request Membership')}</button>
                  </g:form>
                </laser:card>
            </div>
        </div><!--.row-->
    </div><!--.container-->

    <g:if test="${grailsApplication.config.feature.notifications}">

      <div class="container">
        <div class="span12">
          <h2>${message(code: 'profile.misc', default:'Misc')}</h2>
        </div>
      </div>

      <div id="reminders" class="container">
        <div class="row-fluid">
          <div class="span12">
              <div class="well">
                  <h2>${message(code: 'profile.reminder.new', default:'Create new Reminders / Notifications')}</h2>
                  <p>${message(code: 'profile.reminder.new.text', default:'Select the condition you are interested about and time period you wished to be notified about said topic.')}</p>
                  <p><i>${message(code: 'profile.reminder.new.email', default:'Ensure your email or other method of contact is a valid means of reaching yourself')}</i></p>

                  <g:form name="createReminder" controller="profile" action="createReminder" class="form-search" method="POST" url="[controller:'profile', action:'createReminder']">

                      ${message(code: 'profile.reminder.new.notify', default:'Notify for')}:<g:select name="trigger"
                      from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderTrigger')}"
                      optionKey="id"
                      optionValue="value"
                      class="input-medium"/>

                      ${message(code: 'profile.reminder.new.method', default:'Method')}:<g:select name="method"
                      from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderMethod')}"
                      optionKey="id"
                      optionValue="value"
                      class="input-medium"/>

                      ${message(code: 'profile.reminder.new.period', default:'Period')}:<g:select name="unit"
                      from="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','ReminderUnit')}"
                      optionKey="id"
                      optionValue="value"
                      class="input-medium"/>

                      ${message(code: 'profile.reminder.new.time', default:'Time')}:<select name="val" class="input-medium required-indicator" id="val" value="${params.val}" data-type="select">
                          <g:each in="${(1..7)}" var="s">
                              <option value="${s}" ${s==params.long('val')?'selected="selected"':''}>${s}</option>
                          </g:each>
                      </select>


                      <button id="submitReminder" type="submit" class="btn btn-primary btn-small">${message(code:'default.button.create.label', default: 'Create')}</button>
                  </g:form>
              </div>
          </div>
        </div>
      </div>

      <div class="container">
        <div class="row-fluid">
          <div class="span12">
              <div class="well">
                  <h2>${message(code: 'profile.reminder.active', default:'Active Reminders')}</h2>

                  <table class="table table-striped table-bordered table-condensed">
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
                                  <g:if test="${r.lastRan}"><td><g:formatDate format="dd MMMM yyyy" date="${r.lastRan}" /></td></g:if>
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
          </div>
        </div>
      </div>

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
