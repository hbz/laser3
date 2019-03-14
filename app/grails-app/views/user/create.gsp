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

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'user.create_new.label')}</h1>

        <semui:messages data="${flash}" />

        <g:if test="${editable}">
            <g:form class="ui form" action="create" method="post">
                <fieldset>
                    <div class="field">
                        <label>Username</label>
                        <input type="text" name="username" value="${params.username}"/>
                    </div>
                    <div class="field">
                        <label>Dispay Name</label>
                        <input type="text" name="display" value="${params.display}"/>
                    </div>
                    <div class="field">
                        <label>Password</label>
                        <input type="text" name="password" value="${params.password}"/>
                    </div>
                    <div class="field">
                        <label>eMail</label>
                        <input type="text" name="email" value="${params.email}"/>
                    </div>

                    <g:render template="/templates/user/membership_form" model="[userInstance: user, availableOrgs: availableOrgs, availableOrgRoles: availableOrgRoles, tmplUserCreate: true]" />

                    <div class="field">
                        <input type="submit" value="Create &amp; Don't forget to ENABLE" class="ui button"/>
                    </div>

                </fieldset>
            </g:form>
        </g:if>
    </body>
</html>
