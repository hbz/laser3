<%@ page import="de.laser.ui.Icon" %>

<nav class="ui inverted stackable menu la-top-menu" aria-label="${message(code:'wcag.label.mainMenu')}">
    <div class="ui container">
        <ui:link controller="home" class="header item la-logo-item" addItemAttributes="true" aria-label="${message(code:'default.home.label')}">
            <img class="logo" alt="LAS:eR" src="${resource(dir: 'images', file: 'laser.svg')}"/>
        </ui:link>
        <g:link controller="public" action="licensingModel" class="item">
            <i class="icon table"></i> ${message(code: 'landingpage.hero.button.licensingModel')}
        </g:link>
        <a href="https://www.hbz-nrw.de/produkte/digitale-inhalte/las-er" class="item" target="_blank">
            <i class="${Icon.UI.INFO}"></i> ${message(code: 'landingpage.menu.about')}
        </a>
        <a href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" class="item" target="_blank">
            <i class="book reader icon"></i> Wiki
        </a>
        <a href="${message(code:'url.wekb.' + currentServer)}" class="item" target="_blank">
            <i class="${Icon.WEKB}"></i> we:kb
        </a>
        <g:link controller="gasco" action="monitor" class="item">
            <i class="${Icon.GASCO}"></i> ${message(code:'menu.public.gasco_monitor')}
        </g:link>
        <div class="right item">
            <a href="mailto:laser@hbz-nrw.de" class="ui blue la-popup-tooltip button" data-content="${message(code: 'statusCode.error.sendSupportMail')}">
                ${message(code: 'landingpage.feature.button')}
            </a>
        </div>
    </div>
</nav>