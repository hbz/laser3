<%@ page import="com.k_int.kbplus.Org;com.k_int.kbplus.auth.Role" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="user.edit.label" /></title>
  </head>
    <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        <g:message code="user.edit.label" />: ${user.username}
    </h1>

    <semui:messages data="${flash}" />

    <div class="ui two column grid">

        <div class="column wide eight">
            <div class="ui segment form">

                <div class="ui field">
                    <label>${message(code:'user.username.label')}</label>
                    <input type="text" readonly="readonly" value="${user.username}">
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
                              data-original-title="${user.display}">${user.display}</span>
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

        <%--
        <g:if test="${editable}">
            <div class="column wide eight">
                <div class="ui segment form">

                    <g:if test="${user.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | user.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">

                        <h4 class="ui header">${message(code: 'api.label', default:'API')}</h4>

                        <div class="ui field">
                            <label>${message(code: 'api.apikey.label', default:'API-Key')}</label>
                            <input type="text" readonly="readonly" value="${user.apikey}" />
                        </div>

                        <div class="ui field">
                            <label>${message(code: 'api.apisecret.label', default:'API-Secret')}</label>
                            <input type="text" readonly="readonly" value="${user.apisecret}" />
                        </div>
                    </g:if>

                </div>
            </div><!-- .eight -->
        </g:if>
        --%>

    </div><!-- grid -->

    <div class="ui one column grid">
        <g:render template="/templates/user/membership_table" model="[userInstance: user, tmplUserEdit: true]" />
    </div>

    <g:if test="${editable}">
        <div class="ui segment form">
            <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableOrgs, availableOrgRoles: availableOrgRoles, tmplUserEdit: true]" />
        </div>

        <g:if test="${availableComboOrgs}">
            <div class="ui segment form">
                <g:set var="orgLabel" value="Für Konsorten, bzw. Einrichtung" />

                <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableComboOrgs, availableOrgRoles: availableOrgRoles, orgLabel: orgLabel, tmplUserEdit: true]" />
            </div>
        </g:if>
    </g:if>

    <sec:ifAnyGranted roles="ROLE_ADMIN">
      <h4 class="ui dividing header">${message(code:'user.role.plural', default:'Roles')}</h4>

          <table class="ui celled la-table la-table-small table">
            <thead>
              <tr>
                <th>${message(code:'user.role', default:'Role')}</th>
                <th></th>
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
                          <input type="hidden" name="__newObjectClass" value="com.k_int.kbplus.auth.UserRole"/>
                          <input type="hidden" name="__recip" value="user"/>
                          <input type="hidden" name="role" id="userRoleSelect"/>
                          <input type="submit" class="ui button" value="${message(code:'user.role.add', default:'Add Role...')}"/>
                      </g:form>
                  </td>
              </tr>
              </tfoot>
</g:if>
          </table>

          <r:script language="JavaScript">

            $(function(){
              $.fn.editable.defaults.mode = 'inline';
              $('.xEditableValue').editable();

              $("#userRoleSelect").select2({
                placeholder: "${message(code:'user.role.search.ph', default:'Search for an role...')}",
                minimumInputLength: 0,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
                },
                ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                  url: "<g:createLink controller='ajax' action='lookup'/>",
                  dataType: 'json',
                  data: function (term, page) {
                      return {
                          q: term, // search term
                          page_limit: 10,
                          baseClass:'com.k_int.kbplus.auth.Role'
                      };
                  },
                  results: function (data, page) {
                    return {results: data.values};
                  }
                }
              });
            });

          </r:script>

    </sec:ifAnyGranted>
  </body>
</html>
