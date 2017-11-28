<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div>
            <h1 class="ui header">New User</h1>
            <semui:messages data="${flash}" />

                <g:form class="ui form" id="createUserForm" action="create" method="post">
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
                            <input type="password" name="password" value="${params.password}"/>
                        </div>
                        <div class="field">
                            <label>eMail</label>
                            <input type="text" name="email" value="${params.email}"/>
                        </div>
                        <div class="field">
                            <input type="submit" value="Create &amp; Don't forget to set ENABLED flag in database" class="ui primary button"/>
                        </div>
                    </fieldset>
                </g:form>

        </div>
    </body>
</html>
