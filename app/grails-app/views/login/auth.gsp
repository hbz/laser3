<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Login</title>
</head>

<body>

<br />
<br />

<div id='login' class="container">
    <div class='inner'>
        <div class='header'>
            <h1 class="ui header"><semui:headerIcon /><g:message code="springSecurity.login.header"/></h1>
        </div>
    <p>
        <semui:messages data="${flash}" />
    </p>
    <semui:card >
        <div class="content">
            <form action='${postUrl}' method='POST' id='loginForm' class='ui form cssform' autocomplete='off'>
                <form-login always-use-default-target="true" />
                <div class="field">
                    <label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
                    <input type='text' class='text_' name='j_username' id='username'/>
                </div>

                <div class="field">
                    <label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
                    <input type='password' class='text_' name='j_password' id='password'/>
                </div>

                <div class="field" id="remember_me_holder">
                    <label for='remember_me'><g:message code="springSecurity.login.remember.me.label"/></label>
                    <input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                </div>

                <input type='submit' id="submit" class="ui button" value='${message(code: "menu.user.login")}'/>
            </form>
        </div>
    </semui:card>
    </div>
</div>
<r:script type='text/javascript'>
    (function () {
        document.forms['loginForm'].elements['j_username'].focus();
    })();
</r:script>
</body>
</html>
