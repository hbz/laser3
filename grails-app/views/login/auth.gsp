<%@ page import="de.laser.ui.Icon; de.laser.system.SystemMessage; de.laser.utils.AppUtils; de.laser.ui.Btn; grails.util.Environment; grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<g:set var="currentServer" scope="page" value="${AppUtils.getCurrentServer()}"/>
<html>
<head>
    <meta name="layout" content="public">
    <meta name="description" content="${message(code: 'metaDescription.landingPage')}">
    <title>${message(code: 'laser')}</title>
</head>
<body>
<laser:render template="/templates/system/serverIndicator" />
    <div class="landingpage">
    <!--Page Contents-->

        <nav class="ui inverted stackable menu  la-top-menu" aria-label="${message(code:'wcag.label.mainMenu')}">
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
            <div class="ui stackable centered grid container la-login middle aligned">
                <div class="row">
                    <div class="column">
                        <div class="ui middle aligned stackable two column grid" style="border: 1px solid #004678;border-radius: .28571429rem;">
                            <div class="column" style="padding:0;">
                                <g:img class="ui large image la-hero-image" dir="images"
                                       file="landingpage/hero.png"/>
                            </div>

                            <div class="column" style="padding:4rem;">
                                <h1 class="ui header" style="font-size: 2.8rem; padding-bottom: 1rem">
                                    <div class="content">
                                        ${message(code: 'landingpage.hero.h1')}
                                    </div>
                                </h1>
                                <div id="login">
                                        <ui:messages data="${flash}"/>
                                        <form action="${postUrl}" method="POST" id="basicLoginForm" class="ui big form"
                                              autocomplete="off" style="display:block !important;">
                                            <div class="field">
                                                <label for="username"><g:message
                                                    code="springSecurity.login.username.label"/>:</label>

                                                <div class="ui left icon input">
                                                    <i class="${Icon.ATTR.TASK_CREATOR}"></i>
                                                    <input type="text" class="text_"
                                                           name="${SpringSecurityUtils.securityConfig.apf.usernameParameter}"
                                                           id="username"/>
                                                </div>
                                            </div>

                                            <div class="field">
                                                <label for="password"><g:message
                                                    code="springSecurity.login.password.label"/>:</label>

                                                <div class="ui left icon input">
                                                    <i class="${Icon.ATTR.DOCUMENT_CONFIDENTIALITY}"></i>
                                                    <input type="password" class="text_"
                                                           name="${SpringSecurityUtils.securityConfig.apf.passwordParameter}"
                                                           id="password"/>
                                                </div>
                                            </div>

                                            <div class="field">
                                                <label for="remember_me"><g:message
                                                    code="springSecurity.login.remember.me.label"/></label>
                                                <input type="checkbox" class="chk"
                                                       name="${SpringSecurityUtils.securityConfig.rememberMe.parameter}"
                                                       id="remember_me" <g:if test="${hasCookie}">checked="checked"</g:if>/>
                                            </div>

                                            <div class="field">
                                                <input type="submit" class="${Btn.SIMPLE} fluid large"
                                                       value="${message(code: "menu.user.login")}"/>
                                            </div>
                                        </form>

                                        <div class="field">
                                            <a data-ui="modal"
                                               href="#forgottenPassword">${message(code: "menu.user.forgottenPassword")}</a>
                                        </div>
                                        <br>

                                        <div class="field">
                                            <a data-ui="modal"
                                               href="#forgottenUsername">${message(code: "menu.user.forgottenUsername")}</a>
                                        </div>

                                        <ui:modal id="forgottenPassword" message="menu.user.forgottenPassword"
                                                  msgSave="${message(code: 'default.button.submit.label')}">
                                            <g:form class="ui form" controller="login" action="resetForgottenPassword"
                                                    method="post">
                                                <div class="field required">
                                                    <label for="forgotten_username"><g:message
                                                        code="menu.user.forgottenPassword.username"/>:</label>

                                                    <div class="ui left icon input">
                                                        <i class="${Icon.ATTR.TASK_CREATOR}"></i>
                                                        <input type="text" class="text_" name="forgotten_username"
                                                               id="forgotten_username"/>
                                                    </div>
                                                </div>
                                            </g:form>
                                        </ui:modal>

                                        <ui:modal id="forgottenUsername" message="menu.user.forgottenUsername"
                                                  msgSave="${message(code: 'default.button.submit.label')}">
                                            <g:form class="ui form" controller="login" action="getForgottenUsername"
                                                    method="post">
                                                <div class="field required">
                                                    <label for="forgotten_username_mail"><g:message
                                                        code="menu.user.forgottenUsername.email"/>:</label>

                                                    <div class="ui left icon input">
                                                        <i class="envelope icon"></i>
                                                        <input type="text" class="text_" name="forgotten_username_mail"
                                                               id="forgotten_username_mail"/>
                                                    </div>
                                                </div>
                                            </g:form>
                                        </ui:modal>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>


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
        <!-- CLAIM -->
%{--            <div class="ui segment la-eye-catcher">
                <div class="ui container">
                    <div class="ui labeled button" tabindex="0" >
                        <div class="ui blue button la-eye-catcher-header">
                            <h1>${message(code: 'landingpage.hero.h2')}</h1>
                        </div>

                        <div class="ui basic blue left pointing label la-eye-catcher-txt">


                                <div>

                                    ${message(code: 'landingpage.hero.h1')}
                                </div>


                        </div>
                    </div>
                </div>
            </div>--}%
        </main>

        <laser:render template="/layouts/footer" />
    </div>
<laser:script file="${this.getGroovyPageFileName()}">
    document.forms['basicLoginForm'].elements['username'].focus();
</laser:script>
</body>
</html>
