<%@ page import="grails.util.Environment; grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart text="Login" />

<div id="login" class="container">
    <div class="inner">
        <ui:messages data="${flash}" />

        <ui:card>
            <form action="${postUrl}" method="POST" id="basicLoginForm" class="ui form" autocomplete="off" style="display:block !important;">
                <div class="field">
                    <label for="username"><g:message code="springSecurity.login.username.label"/>:</label>
                    <input type="text" class="text_" name="${SpringSecurityUtils.securityConfig.apf.usernameParameter}" id="username"/>
                </div>

                <div class="field">
                    <label for="password"><g:message code="springSecurity.login.password.label"/>:</label>
                    <input type="password" class="text_" name="${SpringSecurityUtils.securityConfig.apf.passwordParameter}" id="password"/>
                </div>

                <div class="field">
                    <label for="remember_me"><g:message code="springSecurity.login.remember.me.label"/></label>
                    <input type="checkbox" class="chk" name="${SpringSecurityUtils.securityConfig.rememberMe.parameter}" id="remember_me" <g:if test="${hasCookie}">checked="checked"</g:if>/>
                </div>

                <div class="field">
                    <input type="submit" class="ui button" value="${message(code: "menu.user.login")}"/>
                </div>
            </form>
            <div class="field">
                <a data-ui="modal" href="#forgottenPassword">${message(code:"menu.user.forgottenPassword")}</a>
            </div>
            <br>
            <div class="field">
                <a data-ui="modal" href="#forgottenUsername">${message(code:"menu.user.forgottenUsername")}</a>
            </div>

            <ui:modal id="forgottenPassword" message="menu.user.forgottenPassword" msgSave="${message(code: 'default.button.submit.label')}">
                <g:form class="ui form" controller="login" action="resetForgottenPassword" method="post">
                    <div class="field required">
                        <label for="forgotten_username"><g:message code="menu.user.forgottenPassword.username"/>:</label>
                        <input type="text" class="text_" name="forgotten_username" id="forgotten_username"/>
                    </div>
                </g:form>
            </ui:modal>

            <ui:modal id="forgottenUsername" message="menu.user.forgottenUsername" msgSave="${message(code: 'default.button.submit.label')}">
                <g:form class="ui form" controller="login" action="getForgottenUsername" method="post">
                    <div class="field required">
                        <label for="forgotten_username_mail"><g:message code="menu.user.forgottenUsername.email"/>:</label>
                        <input type="text" class="text_" name="forgotten_username_mail" id="forgotten_username_mail"/>
                    </div>
                </g:form>
            </ui:modal>
        </ui:card>
    </div>
</div>
<laser:script file="${this.getGroovyPageFileName()}">
    document.forms['basicLoginForm'].elements['username'].focus();
</laser:script>

<laser:htmlEnd />