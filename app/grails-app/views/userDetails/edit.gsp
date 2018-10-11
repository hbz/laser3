<%@ page import="com.k_int.kbplus.Org;com.k_int.kbplus.auth.Role" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title>${ui.display}</title>
  </head>
  <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

     <h1 class="ui left aligned icon header"><semui:headerIcon />

         ${ui.username} :
         <span id="displayEdit"
               class="xEditableValue"
               data-type="textarea"
               data-pk="${ui.class.name}:${ui.id}"
               data-name="display"
               data-url='<g:createLink controller="ajax" action="editableSetValue"/>'
               data-original-title="${ui.display}">${ui.display}</span></h1>


            <semui:messages data="${flash}" />

            <sec:ifAnyGranted roles="ROLE_YODA">
                <h3 class="ui header">Enabled</h3>
                <p><semui:xEditable owner="${ui}" field="enabled"/></p>
            </sec:ifAnyGranted>

          <h3 class="ui header">
            ${message(code:'user.affiliation.plural', default:'Affiliations')}
          </h3>

          <table class="ui celled la-table table">
            <thead>
              <tr>
                <th>${message(code:'user.id', default:'Id')}</th>
                <th>${message(code:'user.org', default:'Org')}</th>
                <th>${message(code:'user.role', default:'Role')}</th>
                <th>${message(code:'user.status', default:'Status')}</th>
                <th>${message(code:'user.actions', default:'Actions')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${ui.affiliations}" var="af">
                <tr>
                  <td>${af.id}</td>
                  <td>${af.org.name}</td>
                  <td>${af.formalRole?.authority}</td>
                  <td>${message(code:"cv.membership.status.${af.status}")}</td>
                  <td class="x">
                      <g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${ui.class.name}:${ui.id}",contextProperty:"affiliations",targetOid:"${af.class.name}:${af.id}"]}'
                              class="ui icon negative button">
                          <i class="trash alternate icon"></i>
                      </g:link>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <h3 class="ui header">${message(code:'user.role.plural', default:'Roles')}</h3>

          <table class="ui celled la-table table">
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
          </table>

           <g:form class="ui form" controller="ajax" action="addToCollection">
              <input type="hidden" name="__context" value="${ui.class.name}:${ui.id}"/>
              <input type="hidden" name="__newObjectClass" value="com.k_int.kbplus.auth.UserRole"/>
              <input type="hidden" name="__recip" value="user"/>
              <input type="hidden" name="role" id="userRoleSelect"/>
              <input type="submit" class="ui button" value="${message(code:'user.role.add', default:'Add Role...')}"/>
            </g:form>


        <div class="ui form">
            <g:if test="${ui.getAuthorities().contains(Role.findByAuthority('ROLE_API_READER')) | ui.getAuthorities().contains(Role.findByAuthority('ROLE_API_WRITER'))}">
                <h3 class="ui header">${message(code: 'api.label', default:'API')}</h3>

                <p>${message(code: 'api.apikey.label', default:'API-Key')}</p>
                <input type="text" readonly="readonly" value="${ui.apikey}">

                <p>${message(code: 'api.apisecret.label', default:'API-Secret')}</p>
                <input type="text" readonly="readonly" value="${ui.apisecret}">
            </g:if>
        </div>

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
