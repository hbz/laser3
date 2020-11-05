<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>

<div id="ajaxLoginModal" class="ui coupled modal">

    <div class='header'>
        <i class="hourglass end icon"></i>
        <g:message code="ajaxLogin.modal.header"/>
    </div>

    <div class='content'>
        <p><g:message code="ajaxLogin.modal.info"/></p>

        <div id="ajaxLoginMessage" style="padding: 1em 0"></div>

        <form action='${request.contextPath}/login/authenticate' method='POST' id='ajaxLoginForm' class='ui form cssform' autocomplete='off'>

            <div class="field">
                <label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
                <input type='text' class='text_' name='${SpringSecurityUtils.securityConfig.apf.usernameParameter}' id='username'/>
            </div>

            <div class="field">
                <label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
                <input type='password' class='text_' name='${SpringSecurityUtils.securityConfig.apf.passwordParameter}' id='password'/>
            </div>

            <div class="field">
                <input type='submit' id="submit" class="ui button" value='${message(code: "menu.user.login")}'/>
            </div>
        </form>
    </div>

    <div class="actions">
        <p>
            <g:link controller="home" action="index" class="ui button"><g:message code="ajaxLogin.modal.loginLink"/></g:link>
        </p>
    </div>

</div>