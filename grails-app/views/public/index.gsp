<%@ page import="de.laser.ui.Icon; de.laser.system.SystemMessage; de.laser.utils.AppUtils" %>
<!doctype html>
<g:set var="currentServer" scope="page" value="${AppUtils.getCurrentServer()}"/>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>${message(code: 'laser')}</title>
</head>
<body>

    <div class="landingpage">
    <!-- NAVIGATION FIX -->

        <div class="ui top fixed hidden inverted stackable menu la-fixed-menu" aria-hidden="true" focusable="false">
            <div class="ui container">
                <img class="logo" alt="Logo Laser" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                <a tabindex='-1'  href="https://wiki1.hbz-nrw.de/display/LAS/Projekthintergrund" class="item" target="_blank">${message(code: 'landingpage.menu.about')}</a>
                <a tabindex='-1' class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" target="_blank">Wiki</a>

                <div class="right item">
                    <g:link controller="home" tabindex='-1'  action="index" class="ui button blue">
                        ${message(code: 'landingpage.login')}
                    </g:link>
                </div>
            </div>
        </div>

    <!--Page Contents-->

        <nav class="ui inverted menu la-top-menu" aria-label="${message(code:'wcag.label.mainMenu')}">
            <div class="ui container">
                <img class="logo" alt="Logo Laser" src="${resource(dir: 'images', file: 'laser.svg')}"/>
                <a href="https://www.hbz-nrw.de/produkte/digitale-inhalte/las-er" class="item" target="_blank">${message(code: 'landingpage.menu.about')}</a>
                <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" target="_blank">Wiki</a>
                <a class="item" href="${message(code:'url.wekb.' + currentServer)}" target="_blank"><i class="${Icon.WEKB}"></i> we:kb</a>
                <g:link class="item" controller="gasco">${message(code:'menu.public.gasco_monitor')}</g:link>
                <div class="right item">
                    <g:link controller="home" action="index" class="ui button blue">
                        ${message(code: 'landingpage.login')}
                    </g:link>
                </div>
            </div>
        </nav>

        <main>
           <!-- HERO -->
            <div class="ui masthead center aligned segment">
                <div class="ui container">

                    <div class="ui grid">
                        <div class="five wide column la-hero left aligned">
                            <h1 class="ui inverted header">
                                ${message(code: 'landingpage.hero.h1')}
                            </h1>
                            <h2 style="padding-bottom: 1rem;">
                                ${message(code: 'landingpage.hero.h2',args: [message(code: 'Umfragen')]) as String}
                            </h2>
                            <g:link controller="public" action="licensingModel" class="ui massive white button">
                                ${message(code: 'landingpage.hero.button.licensingModel')}<i class="right arrow icon"></i>
                            </g:link>
                        </div>
                    </div>
                </div>

            </div>

            <!-- NEWS -->
            <g:set var="systemMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_STARTPAGE_NEWS)}" />

            <g:if test="${systemMessages}">
                <div class="ui segment la-eye-catcher">
                    <div class="ui container">
                        <div class="ui labeled button" tabindex="0" >
                            <div class="ui blue button la-eye-catcher-header">
                                <h1>NEWS</h1>
                            </div>

                            <div class="ui basic blue left pointing label la-eye-catcher-txt">

                            <g:each in="${systemMessages}" var="news" status="i">
                                <div <g:if test="${i>0}">style="padding-top:1.6em"</g:if>>
                                    <% println news.getLocalizedContent() %>
                                </div>
                            </g:each>

                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <div class="ui center aligned segment">
                <a href="mailto:laser@hbz-nrw.de" class="ui huge secondary button">
                    ${message(code: 'landingpage.feature.button')}<i class="right arrow icon"></i>
                </a>
                <g:link controller="home" action="index" class="ui huge blue button">
                    ${message(code: 'landingpage.login')}
                    <i class="right arrow icon"></i>
                </g:link>
            </div>
        </main>

        <laser:render template="/layouts/footer" />
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
                // fix menu when passed
                $('.masthead').visibility({
                        once: false,
                        onBottomPassed: function () {
                            $('.fixed.menu').transition('fade in');
                        },
                        onBottomPassedReverse: function () {
                            $('.fixed.menu').transition('fade out');
                        }
                });

                // create sidebar and attach to menu open
                $('.ui.sidebar').sidebar('attach events', '.toc.item');
    </laser:script>

</body>
</html>
