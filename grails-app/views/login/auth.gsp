<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart text="Login" />

<div id='login' class="container">
    <div class='inner'>
        <div class='header'>
            <ui:h1HeaderWithIcon message="springSecurity.login.header" />
        </div>
    <p>
        <ui:messages data="${flash}" />
    </p>
    <ui:card >
        <div class="content">
            <form action='${postUrl}' method='POST' id='loginForm' class='ui form cssform' autocomplete='off'>
                <div class="field">
                    <label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
                    <input type='text' class='text_' name='${SpringSecurityUtils.securityConfig.apf.usernameParameter}' id='username'/>
                </div>

                <div class="field">
                    <label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
                    <input type='password' class='text_' name='${SpringSecurityUtils.securityConfig.apf.passwordParameter}' id='password'/>
                </div>

                <div class="field" id="remember_me_holder">
                    <label for='remember_me'><g:message code="springSecurity.login.remember.me.label"/></label>
                    <input type='checkbox' class='chk' name='${SpringSecurityUtils.securityConfig.rememberMe.parameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                </div>

                <div class="field">
                    <input type='submit' id="submit" class="ui button" value='${message(code: "menu.user.login")}'/>
                </div>
            </form>
            <g:form name="forgottenPassword" id="forgottenPassword" action="resetForgottenPassword" method="post">
                <input type="hidden" id="forgotten_username" name="forgotten_username">
                <div class="field">
                    <a id="forgotten" href="#">${message(code:'menu.user.forgottenPassword')}</a>
                </div>
            </g:form>
        </div>
    </ui:card>
    </div>
</div>
<laser:script file="${this.getGroovyPageFileName()}">
    document.forms['loginForm'].elements['username'].focus();

    $("#forgotten").click(function(e){
        e.preventDefault();
        var username = prompt("<g:message code="menu.user.forgottenPassword.username"/>");
        console.log(username);
        if(username){
            $("#forgotten_username").val(username);
            $("#forgottenPassword").submit();
        }
    });
</laser:script>

<laser:htmlEnd />