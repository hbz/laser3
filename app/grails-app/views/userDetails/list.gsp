<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
    <body>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

            <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'user.show_all.label')}</h1>

            <semui:filter>
                <g:form action="list" method="get" class="ui form">
                    <g:set value="${com.k_int.kbplus.auth.Role.findAll()}" var="auth_values"/>

                    <div class="fields">
                        <div class="field">
                            <label>Name contains</label>
                            <input type="text" name="name" value="${params.name}"/>
                        </div>
                        <div class="field">
                            <label>Role</label>
                            <g:select from="${auth_values}" noSelection="${['null':'-Any role-']}"
                                      value="authority" optionKey="id" optionValue="authority" name="authority" />
                        </div>
                        <div class="field">
                            <label>&nbsp;</label>
                            <input type="submit" value="Search" class="ui secondary button"/>
                        </div>
                    </div>
              </g:form>
            </semui:filter>

            <semui:messages data="${flash}" />
        
            <table class="ui sortable celled la-table la-table-small table">
                <thead>
                    <tr>
                        <g:sortableColumn property="username" params="${params}" title="${message(code: 'user.name.label', default: 'User Name')}" />
                        <g:sortableColumn property="display" params="${params}" title="${message(code: 'user.display.label', default: 'Display Name')}" />
                        <g:sortableColumn property="instname" params="${params}" title="${message(code: 'user.instname.label', default: 'Institution')}" />
                        <th>Enabled</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${users}" var="user">
                        <tr>
                            <td>${fieldValue(bean: user, field: "username")}</td>
                            <td>${user.getDisplayName()}</td>
                            <td>${fieldValue(bean: user, field: "instname")}</td>
                            <td>
                                <sec:ifAnyGranted roles="ROLE_YODA">
                                    <semui:xEditable owner="${user}" field="enabled"/>
                                </sec:ifAnyGranted>
                                <sec:ifNotGranted roles="ROLE_YODA">
                                    ${fieldValue(bean: user, field: "enabled")}
                                </sec:ifNotGranted>
                            </td>
                            <td class="x">
                                <g:link action="edit" id="${user.id}" class="ui icon button"><i class="write icon"></i></g:link>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

          <semui:paginate total="${total}" params="${params}" />

    </body>
</html>
