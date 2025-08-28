<%@ page import="de.laser.api.v0.ApiManager; de.laser.utils.LocaleUtils; de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'menu.user.help')}" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon text="${message(code: 'menu.user.help')}" type="help"/>

<div class="ui equal width grid la-clear-before" style="margin:1em 0;">
    <div class="row">
        <div class="column">
            <div class="ui divided relaxed list">
                <div class="item">
                    <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                    <div class="content">
                        <g:link controller="public" action="manual">${message(code:'menu.user.manual')}</g:link>
                    </div>
                </div>
                <div class="item">
                    <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                    <div class="content">
                        <g:link controller="public" action="faq">${message(code:'menu.user.faq')}</g:link>
                    </div>
                </div>
                <div class="item">
                    <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                    <div class="content">
                        <g:link controller="public" action="releases">${message(code:'releaseNotes')} (${AppUtils.getMeta('info.app.version')})</g:link>
                    </div>
                </div>
                <div class="item">
                    <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                    <div class="content">
                        <g:link controller="public" action="api" id="${ApiManager.HISTORY[0]}">${message(code:'apiRelease')} (${ApiManager.VERSION})</g:link>
                    </div>
                </div>
                <div class="item">
                    <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                    <div class="content">
                        <g:link controller="public" action="dsgvo">${message(code:'privacyNotice')}</g:link>
                    </div>
                </div>
            </div>
        </div>
        <div class="column"></div>
        <div class="column"></div>
        <div class="column"></div>
    </div>
</div>

<laser:htmlEnd />

