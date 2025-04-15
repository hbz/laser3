<%@ page import="de.laser.ui.Icon; de.laser.system.SystemMessage; de.laser.utils.AppUtils; de.laser.ui.Btn; grails.util.Environment; grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<g:set var="currentServer" scope="page" value="${AppUtils.getCurrentServer()}"/>
<html>
<head>
    <meta name="layout" content="public">
    <meta name="description" content="${message(code: 'metaDescription.landingPage')}">
    <title>${message(code: 'laser')}</title>
    <style>
    /** inline style here with intention:
        flex layout helps footer to stick at bottom when main high not high enough
        but would make a bug when using the sidebars at all the other sides without the footer
     */
    .pusher {
        flex: 1;
    }
    body {
        display: flex;
        min-height: 100vh;
        flex-direction: column;
    }

    main {
        flex: 1;
        display: flex;
        flex-direction: column;

    }
    .la-login {
        flex: 3!important; /* 3/4 of height of main */
        display: flex!important;
    }
    .la-login-left,
    .la-login-right{
        min-height: 100%;
    }
    img.la-hero-image {
        max-height: 100%;
        min-width: 100%;
        object-fit: cover;
        vertical-align: bottom;
        border-radius: 1vmin;
    }
    .pushable>.pusher {
        overflow-y: visible!important;
        overflow-x: visible!important;
        min-height: auto!important;
    }
    .la-js-verticalNavi {
        display: block!important;
    }

    </style>
</head>
<body>

    <div class="landingpage">
    <!--Page Contents-->

        <nav class="ui inverted menu stackable la-top-menu" aria-label="${message(code:'wcag.label.mainMenu')}">
            <div class="ui container">
                <img class="logo" alt="Logo Laser" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                <a href="https://www.hbz-nrw.de/produkte/digitale-inhalte/las-er" class="item" target="_blank">${message(code: 'landingpage.menu.about')}</a>
                <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" target="_blank">Wiki</a>
                <a class="item" href="${message(code:'url.wekb.' + currentServer)}" target="_blank"><i class="${Icon.WEKB}"></i> we:kb</a>
                <g:link class="item" controller="gasco">${message(code:'menu.public.gasco_monitor')}</g:link>
                <div class="right item">
                    <a href="mailto:laser@hbz-nrw.de" class="ui blue button">
                        ${message(code: 'landingpage.feature.button')}
                    </a>
                </div>
            </div>
        </nav>

        <main>
            <div class="ui middle aligned stackable centered grid container la-login">
                <div class="row">

                    <div class="eight wide column la-login-left">
                        <g:img  class="ui large bordered rounded image la-hero-image" dir="images" file="landingpage/hero.png" />
                    </div>
                    <div class="eight wide column la-login-right">
                        <h1 class="ui header">
                            <div class="content">
                                Login bei LAS:eR
                            </div>
                        </h1>
                        <div id="login" class="container">
                            <div class="inner">
                                <ui:messages data="${flash}" />

                                <ui:card>
                                    <form action="${postUrl}" method="POST" id="basicLoginForm" class="ui big form" autocomplete="off" style="display:block !important;">
                                        <div class="field">
                                            <label for="username"><g:message code="springSecurity.login.username.label"/>:</label>
                                            <div class="ui left icon input">
                                                <i class="user icon"></i>
                                                <input type="text" class="text_" name="${SpringSecurityUtils.securityConfig.apf.usernameParameter}" id="username"/>
                                            </div>
                                        </div>

                                        <div class="field">
                                            <label for="password"><g:message code="springSecurity.login.password.label"/>:</label>
                                            <div class="ui left icon input">
                                                <i class="lock icon"></i>
                                                <input type="password" class="text_" name="${SpringSecurityUtils.securityConfig.apf.passwordParameter}" id="password"/>
                                            </div>
                                        </div>

                                        <div class="field">
                                            <label for="remember_me"><g:message code="springSecurity.login.remember.me.label"/></label>
                                            <input type="checkbox" class="chk" name="${SpringSecurityUtils.securityConfig.rememberMe.parameter}" id="remember_me" <g:if test="${hasCookie}">checked="checked"</g:if>/>
                                        </div>

                                        <div class="field">
                                            <input type="submit" class="${Btn.SIMPLE}" value="${message(code: "menu.user.login")}"/>
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
                                                <div class="ui left icon input">
                                                    <i class="user icon"></i>
                                                    <input type="text" class="text_" name="forgotten_username" id="forgotten_username"/>
                                                </div>
                                            </div>
                                        </g:form>
                                    </ui:modal>

                                    <ui:modal id="forgottenUsername" message="menu.user.forgottenUsername" msgSave="${message(code: 'default.button.submit.label')}">
                                        <g:form class="ui form" controller="login" action="getForgottenUsername" method="post">
                                            <div class="field required">
                                                <label for="forgotten_username_mail"><g:message code="menu.user.forgottenUsername.email"/>:</label>
                                                <div class="ui left icon input">
                                                    <i class="user icon"></i>
                                                    <input type="text" class="text_" name="forgotten_username_mail" id="forgotten_username_mail"/>
                                                </div>
                                            </div>
                                        </g:form>
                                    </ui:modal>
                                </ui:card>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
%{--           <!-- HERO -->
            <div class="ui masthead center aligned segment">
                <div class="ui container">

                    <div class="ui grid">
                        <div class="five wide column la-hero left aligned" style="background-color: #447294;">
                            <h1 class="ui inverted header">
                                ${message(code: 'landingpage.hero.h1')}
                            </h1>
                            <h2 style="padding-bottom: 1rem;">
                                ${message(code: 'landingpage.hero.h2',args: [message(code: 'Umfragen')]) as String}
                            </h2>
                            <g:link controller="public" action="licensingModel" class="ui massive white button">
                                ${message(code: 'landingpage.hero.button.licensingModel')} <icon:arrow />
                            </g:link>
                        </div>
                    </div>
                </div>

            </div>--}%

            <!-- NEWS -->
            <g:set var="startpageMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_STARTPAGE)}" />

            <g:if test="${startpageMessages}">
                <div class="ui segment la-eye-catcher">
                    <div class="ui container">
                        <div class="ui labeled button" tabindex="0" >
                            <div class="ui blue button la-eye-catcher-header">
                                <h1>NEWS</h1>
                            </div>

                            <div class="ui basic blue left pointing label la-eye-catcher-txt">

                            <g:each in="${startpageMessages}" var="news" status="i">
                                <div <g:if test="${i>0}">style="padding-top:1.6em"</g:if>>
                                    <ui:renderContentAsMarkdown>${news.getLocalizedContent()}</ui:renderContentAsMarkdown>
                                </div>
                            </g:each>

                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

%{--            <div class="ui center aligned segment">
                <a href="mailto:laser@hbz-nrw.de" class="ui huge secondary button">
                    ${message(code: 'landingpage.feature.button')} <icon:arrow />
                </a>
                <g:link controller="home" action="index" class="ui huge blue button">
                    ${message(code: 'landingpage.login')} <icon:arrow />
                </g:link>
            </div>--}%
        </main>

        <laser:render template="/layouts/footer" />
    </div>
<laser:script file="${this.getGroovyPageFileName()}">
    document.forms['basicLoginForm'].elements['username'].focus();
</laser:script>
</body>
</html>
