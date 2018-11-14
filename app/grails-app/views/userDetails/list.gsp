<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.auth.*" %>
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
                    <g:set value="${Role.findAll()}" var="auth_values"/>

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
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${users}" var="user">
                        <tr>
                            <td>${fieldValue(bean: user, field: "username")}</td>
                            <td>${user.getDisplayName()}</td>
                            <td>
                                <g:each in="${user.getAuthorizedAffiliations()}" var="affi">
                                    ${affi.org?.getDesignation()}<br />
                                </g:each>
                            </td>
                            <td>
                                <sec:ifAnyGranted roles="ROLE_YODA">
                                    <semui:xEditable owner="${user}" field="enabled"/>
                                </sec:ifAnyGranted>
                                <sec:ifNotGranted roles="ROLE_YODA">
                                    ${fieldValue(bean: user, field: "enabled")}
                                </sec:ifNotGranted>
                            </td>
                            <td>
                                <div class="ui list">
                                    <g:if test="${UserRole.findByUserAndRole(user, Role.findByAuthority('ROLE_API'))}">
                                        <div class="item"><i class="icon circle outline"></i> API</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(user, Role.findByAuthority('ROLE_API_READER'))}">
                                        <div class="item"><i class="icon check circle outline"></i> Lesend</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(user, Role.findByAuthority('ROLE_API_WRITER'))}">
                                        <div class="item"><i class="icon check circle"></i> Schreibend</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(user, Role.findByAuthority('ROLE_API_DATAMANAGER'))}">
                                        <div class="item"><i class="icon circle"></i> Datamanager</div>
                                    </g:if>
                                </div>
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
