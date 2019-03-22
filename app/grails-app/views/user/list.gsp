<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.auth.*" %>
<laser:serviceInjection />
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

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'user.show_all.label')}
            <semui:totalNumber total="${total}"/>
        </h1>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <semui:filter>
                <g:form action="list" method="get" class="ui form">
                    <g:set value="${Role.findAll()}" var="auth_values"/>

                    <div class="three fields">
                        <div class="field">
                            <label>Name contains</label>
                            <input type="text" name="name" value="${params.name}"/>
                        </div>
                        <div class="field">
                            <label>Role</label>
                            <g:select from="${auth_values}" noSelection="${['null':'-Any role-']}" class="ui dropdown"
                                      value="authority" optionKey="id" optionValue="authority" name="authority" />
                        </div>
                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                            <input type="submit" value="Search" class="ui secondary button"/>
                        </div>
                    </div>
              </g:form>
            </semui:filter>
        </sec:ifAnyGranted>

        <sec:ifNotGranted roles="ROLE_ADMIN">
            <semui:filter>
                <g:form action="list" method="get" class="ui form">
                    <g:set value="${Role.findAllByRoleType('user')}" var="auth_values"/>

                    <div class="three fields">
                        <div class="field">
                            <label>Name contains</label>
                            <input type="text" name="name" value="${params.name}"/>
                        </div>
                        <div class="field">
                            <label>Role</label>
                            <g:select from="${auth_values}" noSelection="${['null':'-Any role-']}" class="ui dropdown"
                                      value="authority" optionKey="id" optionValue="authority" name="authority" />
                        </div>
                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                            <input type="submit" value="Search" class="ui secondary button"/>
                        </div>
                    </div>
                </g:form>
            </semui:filter>
        </sec:ifNotGranted>


            <semui:messages data="${flash}" />

            <sec:ifNotGranted roles="ROLE_ADMIN"> TODO: COMBO
            </sec:ifNotGranted>
        
            <table class="ui sortable celled la-table la-table-small table">
                <thead>
                    <tr>
                        <%--<g:sortableColumn property="u.username" params="${params}" title="${message(code: 'user.name.label', default: 'User Name')}" />
                        <g:sortableColumn property="u.display" params="${params}" title="${message(code: 'user.display.label', default: 'Display Name')}" />
                        <g:sortableColumn property="uo.org.instname" params="${params}" title="${message(code: 'user.instname.label', default: 'Institution')}" />
                        --%>
                        <th>User Name</th>
                        <th>Display Name</th>
                        <th>Institution</th>
                        <th>Enabled</th>
                        <th>API</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${users}" var="us">
                        <tr>
                            <td>
                                ${fieldValue(bean: us, field: "username")}

                                <g:if test="${! UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_USER'))}">
                                    <span data-tooltip="Dieser Account besitzt keine ROLE_USER." data-position="top right">
                                        <i class="icon minus circle red"></i>
                                    </span>
                                </g:if>
                            </td>
                            <td>${us.getDisplayName()}</td>
                            <td>
                                <sec:ifAnyGranted roles="ROLE_ADMIN">
                                    <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                                        ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                                    </g:each>
                                </sec:ifAnyGranted>
                                <sec:ifNotGranted roles="ROLE_ADMIN">
                                    <% int affiCount = 0 %>
                                    <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                                        <g:if test="${affi.org.id == contextService.getOrg().id}">
                                            ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                                            <% affiCount++ %>
                                        </g:if>
                                    </g:each>
                                    <g:if test="${affiCount != us.getAuthorizedAffiliations().size()}">
                                        und ${us.getAuthorizedAffiliations().size() - affiCount} weitere ..
                                    </g:if>
                                </sec:ifNotGranted>
                            </td>
                            <td>
                                <sec:ifAnyGranted roles="ROLE_YODA">
                                    <semui:xEditableBoolean owner="${us}" field="enabled"/>
                                </sec:ifAnyGranted>
                                <sec:ifNotGranted roles="ROLE_YODA">
                                    ${fieldValue(bean: us, field: "enabled")}
                                </sec:ifNotGranted>
                            </td>
                            <td>
                                <div class="ui list">
                                    <g:if test="${UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_API'))}">
                                        <div class="item"><i class="icon circle outline"></i> API</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_API_READER'))}">
                                        <div class="item"><i class="icon check circle outline"></i> Lesend</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_API_WRITER'))}">
                                        <div class="item"><i class="icon check circle"></i> Schreibend</div>
                                    </g:if>

                                    <g:if test="${UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_API_DATAMANAGER'))}">
                                        <div class="item"><i class="icon circle"></i> Datamanager</div>
                                    </g:if>
                                </div>
                            </td>
                            <td class="x">
                                <g:if test="${editor.hasRole('ROLE_ADMIN') || us.getAuthorizedAffiliations().collect{ it.org.id }.unique().size() == 1}">
                                    <g:link action="edit" id="${us.id}" class="ui icon button"><i class="write icon"></i></g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>

          <%-- <semui:paginate total="${total}" params="${params}" /> --%>

    </body>
</html>
