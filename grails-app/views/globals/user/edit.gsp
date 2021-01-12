<%@ page import="de.laser.auth.UserRole;de.laser.Org;de.laser.auth.Role" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : <g:message code="user.edit.label" /></title>
  </head>
    <body>

        <g:render template="/user/breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="/user/actions" />
        </semui:controlButtons>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            <g:message code="user.edit.label" />: ${user.username}
        </h1>

    <semui:messages data="${flash}" />

    <div class="ui two column grid">

        <div class="column wide eight">
            <div class="ui segment form">

                <div class="ui field">
                    <label for="username">${message(code:'user.username.label')}</label>
                    <input id="username" type="text" readonly="readonly" value="${user.username}">
                </div>

                <div class="ui field">
                    <label>${message(code:'user.displayName.label')}</label>
                    <g:if test="${editable}">
                        <span id="displayEdit"
                              class="xEditableValue"
                              data-type="text"
                              data-pk="${user.class.name}:${user.id}"
                              data-name="display"
                              data-url='<g:createLink controller="ajax" action="editableSetValue"/>'
                              data-original-title="${user.display}">${user.display}
                        </span>
                    </g:if>
                    <g:else>
                        ${user.display}
                    </g:else>
                </div>

                <div class="ui field">
                    <label>${message(code:'user.email')}</label>
                    <semui:xEditable owner="${user}" field="email" />
                </div>

                <g:if test="${editable}">

                    <div class="ui field">
                        <label>${message(code:'user.enabled.label')}</label>
                        <semui:xEditableBoolean owner="${user}" field="enabled" />
                    </div>

                    <g:form controller="user" action="newPassword" params="${[id: user.id]}">
                        <div class="ui two fields">
                            <div class="ui field">
                                <label>${message(code:'user.password.label')}</label>
                                <input type="submit" class="ui button orange" value="${message(code:'user.newPassword.text')}">
                            </div>
                        </div>
                    </g:form>

                </g:if>

            </div>
        </div><!-- .eight -->

    </div><!-- grid -->


    <g:if test="${manipulateAffiliations}">
        <div class="ui one column grid">
            <g:render template="/templates/user/membership_table" model="[userInstance: user, tmplUserEdit: true]" />
        </div>
    </g:if>


        <g:if test="${editable}">
            <g:if test="${availableOrgs}">
                <div class="ui segment form">
                    <g:set var="orgLabel" value="Organisation" />

                    <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableOrgs, availableOrgRoles: availableOrgRoles, orgLabel: orgLabel, tmplUserEdit: true]" />
                </div>
            </g:if>

            <g:if test="${availableComboDeptOrgs}">
                <div class="ui segment form">
                    <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableComboDeptOrgs, availableOrgRoles: availableOrgRoles, orgLabel: orgLabel, tmplUserEdit: true]" />
                </div>
            </g:if>

            <g:if test="${availableComboConsOrgs}">
                <div class="ui segment form">
                    <g:set var="orgLabel" value="Teilnehmer" />

                    <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableComboConsOrgs, availableOrgRoles: availableOrgRoles, orgLabel: orgLabel, tmplUserEdit: true]" />
                </div>
            </g:if>
        </g:if>


    <sec:ifAnyGranted roles="ROLE_ADMIN">
      <h4 class="ui dividing header">${message(code:'user.role.plural')}</h4>

          <table class="ui celled la-table compact table">
            <thead>
              <tr>
                <th>${message(code:'user.role')}</th>
                  <th class="la-action-info">${message(code:'default.actions.label')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${user.roles}" var="rl">
                <tr>
                  <td>${rl.role.authority}</td>
                  <td class="x">
                          <g:if test="${editable}">
                              <g:link controller="ajax" action="removeUserRole" params='${[user:"${user.class.name}:${user.id}",role:"${rl.role.class.name}:${rl.role.id}"]}'
                                      class="ui icon negative button">
                                  <i class="trash alternate icon"></i>
                              </g:link>
                          </g:if>
                      </td>
                </tr>
              </g:each>
            </tbody>
            <g:if test="${editable}">
              <tfoot>
              <tr>
                  <td colspan="2">
                      <g:form class="ui form" controller="ajax" action="addToCollection">
                          <input type="hidden" name="__context" value="${user.class.name}:${user.id}"/>
                          <input type="hidden" name="__newObjectClass" value="${UserRole.class.name}"/>
                          <input type="hidden" name="__recip" value="user"/>
                          <input type="hidden" name="role" id="userRoleSelect"/>
                          <input type="submit" class="ui button" value="${message(code:'user.role.add')}"/>
                      </g:form>

                  </td>
              </tr>
              </tfoot>
            </g:if>
          </table>

                  <laser:script file="${this.getGroovyPageFileName()}">
                      $("#userRoleSelect").select2({
                        placeholder: "${message(code:'user.role.search.ph')}",
                        minimumInputLength: 0,
                        formatInputTooShort: function () {
                            return "${message(code:'select2.minChars.note')}";
                        },
                        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                          url: "<g:createLink controller='ajaxJson' action='lookup'/>",
                          dataType: 'json',
                          data: function (term, page) {
                              return {
                                  q: term, // search term
                                  page_limit: 10,
                                  baseClass: '${Role.class.name}'
                              };
                          },
                          results: function (data, page) {
                            return {results: data.values};
                          }
                        }
                      });
                  </laser:script>
</sec:ifAnyGranted>
</body>
</html>
