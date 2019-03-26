<%@ page import="com.k_int.kbplus.*;com.k_int.kbplus.auth.Role" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'user.create_new.label')}</title>
    </head>
    <body>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'user.create_new.label')}</h1>

        <semui:messages data="${flash}" />

        <g:if test="${editable}">
            <g:form class="ui form" action="create" method="post">
                <fieldset>
                    <div class="field required">
                        <label>Username</label>
                        <input type="text" name="username" value="${params.username}"/>
                    </div>
                    <div class="field required">
                        <label>Dispay Name</label>
                        <input type="text" name="display" value="${params.display}"/>
                    </div>
                    <div class="field required">
                        <label>Password</label>
                        <input type="text" name="password" value="${params.password}"/>
                    </div>
                    <div class="field required">
                        <label>E-Mail</label>
                        <input type="text" name="email" value="${params.email}"/>
                    </div>

                    <div class="two fields">
                        <div class="field">
                            <label>Organisation</label>
                            <g:select name="org"
                                      from="${availableOrgs}"
                                      optionKey="id"
                                      optionValue="name"
                                      value="${params.org}"
                                      class="ui fluid search dropdown"/>
                        </div>
                        <div class="field">
                            <label>Role</label>
                            <g:select name="formalRole"
                                      from="${availableOrgRoles}"
                                      optionKey="id"
                                      optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                      value="${Role.findByAuthority('INST_USER').id}"
                                      class="ui fluid dropdown"/>
                        </div>
                    </div>

                    <g:if test="${availableComboOrgs}">
                        <div class="two fields">
                            <div class="field">
                                <label>Für Konsorten, bzw. Einrichtung</label>
                                <g:select name="comboOrg"
                                          from="${availableComboOrgs}"
                                          optionKey="id"
                                          optionValue="name"
                                          value="${params.org}"
                                          class="ui fluid search dropdown"/>
                            </div>
                            <div class="field">
                                <label>Role</label>
                                <g:select name="comboFormalRole"
                                          from="${availableOrgRoles}"
                                          optionKey="id"
                                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                          value="${Role.findByAuthority('INST_USER').id}"
                                          class="ui fluid dropdown"/>
                            </div>
                        </div>
                    </g:if>

                    <div class="field">
                        <input type="submit" value="Anlegen" class="ui button"/>
                    </div>

                </fieldset>
            </g:form>
        </g:if>
    </body>
</html>
