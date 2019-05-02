<%@ page import="com.k_int.kbplus.*;com.k_int.kbplus.auth.Role" %>
<laser:serviceInjection />
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
                        <label for="username">${message(code:'user.username.label')}</label>
                        <input type="text" id="username" name="username" value="${params.username}"/>
                    </div>
                    <div class="field required">
                        <label for="displayName">${message(code:'user.displayName.label')}</label>
                        <input type="text" id="displayName" name="display" value="${params.display}"/>
                    </div>
                    <div class="field required">
                        <label for="password">${message(code:'user.password.label')}</label>
                        <input type="text" id="password" name="password" value="${params.password}"/>
                    </div>
                    <div class="field required">
                        <label for="email">${message(code:'user.email')}</label>
                        <input type="text" id="email" name="email" value="${params.email}"/>
                    </div>

                    <g:if test="${availableComboOrgs}">
                        <div class="two fields">
                            <div class="field">
                                <label for="userOrg">${message(code:'user.org')}</label>
                                <g:select id="consortium" name="comboOrg"
                                          from="${availableComboOrgs}"
                                          optionKey="id"
                                          optionValue="${{(it.sortname ?: '')  + ' (' + it.name + ')'}}"
                                          value="${params.org ?: contextService.getOrg().id}"
                                          class="ui fluid search dropdown"/>
                            </div>
                            <div class="field">
                                <label for="userRole">${message(code:'user.role')}</label>
                                <g:select id="userRole" name="comboFormalRole"
                                          from="${availableOrgRoles}"
                                          optionKey="id"
                                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                          value="${Role.findByAuthority('INST_USER').id}"
                                          class="ui fluid dropdown"/>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="two fields">
                            <div class="field">
                                <label for="userOrg">${message(code:'user.org')}</label>
                                <g:select id="userOrg" name="org"
                                          from="${availableOrgs}"
                                          optionKey="id"
                                          optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                                          value="${params.org ?: contextService.getOrg().id}"
                                          class="ui fluid search dropdown"/>
                            </div>
                            <div class="field">
                                <label for="userRole">${message(code:'user.role')}</label>
                                <g:select id="userRole" name="formalRole"
                                          from="${availableOrgRoles}"
                                          optionKey="id"
                                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                          value="${Role.findByAuthority('INST_USER').id}"
                                          class="ui fluid dropdown"/>
                            </div>
                        </div>

                    </g:else>

                    <div class="field">
                        <input type="submit" value="${message(code:'user.create_new.label')}" class="ui button"/>
                    </div>

                </fieldset>
            </g:form>
        </g:if>
    </body>
</html>
