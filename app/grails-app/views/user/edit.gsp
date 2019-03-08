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
        <g:message code="user.edit.label" />: ${ui.username}
    </h1>

    <semui:messages data="${flash}" />

    <div class="ui two column grid">

        <div class="column wide eight">
            <div class="ui segment form">

                <div class="ui field">
                    <label>Anzeigename</label>
                    <span id="displayEdit"
                          class="xEditableValue"
                          data-type="text"
                          data-pk="${ui.class.name}:${ui.id}"
                          data-name="display"
                          data-url='<g:createLink controller="ajax" action="editableSetValue"/>'
                          data-original-title="${ui.display}">${ui.display}</span>
                </div>

                <div class="ui field">
                    <label>Email</label>
                    <input type="text" readonly="readonly" value="${ui.email}">
                </div>
            </div>

            <sec:ifAnyGranted roles="ROLE_YODA">

                <div class="ui segment form">

                    <div class="ui field">
                        <label>Enabled</label>
                        <semui:xEditableBoolean owner="${ui}" field="enabled" />
                    </div>

                    <g:form controller="user" action="newPassword" params="${[id: ui.id]}">
                        <div class="ui two fields">
                            <div class="ui field">
                                <label>Passwort</label>
                                <input type="submit" class="ui button orange" value="Neues Passwort per Mail verschicken">
                            </div>
                        </div>
                    </g:form>

                </div>

            </sec:ifAnyGranted>
        </div>

        <div class="column wide eight">
            <div class="ui segment form">

                <g:if test="${ui.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | ui.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">

                    <h4 class="ui header">${message(code: 'api.label', default:'API')}</h4>

                    <div class="ui field">
                        <label>${message(code: 'api.apikey.label', default:'API-Key')}</label>
                        <input type="text" readonly="readonly" value="${ui.apikey}" />
                    </div>

                    <div class="ui field">
                        <label>${message(code: 'api.apisecret.label', default:'API-Secret')}</label>
                        <input type="text" readonly="readonly" value="${ui.apisecret}" />
                    </div>
                </g:if>

            </div>
        </div>

    </div><!-- grid -->

    <div class="ui one column grid">
        <g:render template="/templates/user/membership_table" model="[userInstance: ui, tmplAdmin: true]" />
    </div>

    <div class="ui segment form">
        <g:render template="/templates/user/membership_form" model="[userInstance: ui, tmplAdmin: true]" />
    </div>

          <h4 class="ui dividing header">${message(code:'user.role.plural', default:'Roles')}</h4>

          <table class="ui celled la-table la-table-small table">
            <thead>
              <tr>
                <th>${message(code:'user.role', default:'Role')}</th>
                <th>${message(code:'user.actions', default:'Actions')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${ui.roles}" var="rl">
                <tr>
                  <td>${rl.role.authority}</td>
                  <td class="x">
                      <g:link controller="ajax" action="removeUserRole" params='${[user:"${ui.class.name}:${ui.id}",role:"${rl.role.class.name}:${rl.role.id}"]}'
                              class="ui icon negative button">
                          <i class="trash alternate icon"></i>
                      </g:link></td>
                </tr>
              </g:each>
            </tbody>
              <tfoot>
              <tr>
                  <td colspan="2">
                      <g:form class="ui form" controller="ajax" action="addToCollection">
                          <input type="hidden" name="__context" value="${ui.class.name}:${ui.id}"/>
                          <input type="hidden" name="__newObjectClass" value="com.k_int.kbplus.auth.UserRole"/>
                          <input type="hidden" name="__recip" value="user"/>
                          <input type="hidden" name="role" id="userRoleSelect"/>
                          <input type="submit" class="ui button" value="${message(code:'user.role.add', default:'Add Role...')}"/>
                      </g:form>
                  </td>
              </tr>
              </tfoot>
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
  </body>
</html>
