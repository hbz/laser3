<%@ page import="de.laser.ui.Icon; de.laser.system.SystemMessage; de.laser.utils.AppUtils; de.laser.ui.Btn; grails.util.Environment; grails.plugin.springsecurity.SpringSecurityUtils" %>
<nav class="ui inverted stackable menu  la-top-menu" aria-label="${message(code:'wcag.label.mainMenu')}">
    <div class="ui container">
        <ui:link addItemAttributes="true" controller="home" aria-label="${message(code:'default.home.label')}" class="header item la-logo-item">
            <img class="logo" alt="Logo Laser" src="${resource(dir: 'images', file: 'laser.svg')}"/>
        </ui:link>
        <g:link controller="public" action="licensingModel" class="item"><i class="icon table"></i>${message(code: 'landingpage.hero.button.licensingModel')}</g:link>
        <a href="https://www.hbz-nrw.de/produkte/digitale-inhalte/las-er" class="item" target="_blank"><i class="${Icon.UI.INFO}"></i>${message(code: 'landingpage.menu.about')}</a>
        <a class="item" href="https://wiki1.hbz-nrw.de/display/LAS/Startseite" target="_blank"><i class="book reader icon"></i>Wiki</a>
        <a class="item" href="${message(code:'url.wekb.' + currentServer)}" target="_blank"><i class="${Icon.WEKB}"></i> we:kb</a>
        <g:link class="item" controller="gasco"><i class="${Icon.GASCO}"></i>${message(code:'menu.public.gasco_monitor')}</g:link>
        <div class="right item">
            <a  data-content="${message(code: 'statusCode.error.sendSupportMail')}" href="mailto:laser@hbz-nrw.de" class="ui blue la-popup-tooltip button">
                ${message(code: 'landingpage.feature.button')}
            </a>
        </div>
    </div>
</nav>