<%@ page import="de.laser.system.MuleCache; de.laser.api.v0.ApiManager; de.laser.system.SystemActivityProfiler; de.laser.utils.AppUtils;" %>
<laser:serviceInjection />

%{-- menu: user --}%

<div class="ui dropdown item la-noBorder" role="menuitem" aria-haspopup="true">
    <a class="title">
        <i class="dropdown icon"></i> ${contextUser.displayName}
    </a>

    <div class="menu" role="menu">
        <ui:link addItemAttributes="true" controller="profile" action="index">${message(code:'profile.user')}</ui:link>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="public" action="manual">${message(code:'menu.user.manual')}</ui:link>
        <ui:link addItemAttributes="true" controller="public" action="faq">${message(code:'menu.user.faq')}</ui:link>

        <ui:link addItemAttributes="true" controller="public" action="releases">
            ${message(code:'releaseNotes')} (${AppUtils.getMeta('info.app.version')})
        </ui:link>
        <ui:link addItemAttributes="true" controller="public" action="api">
            ${message(code:'apiRelease')} (${ApiManager.VERSION})
        </ui:link>
        <ui:link addItemAttributes="true" controller="public" action="dsgvo">${message(code:'privacyNotice')}</ui:link>

        <div class="divider"></div>
        <ui:link class="la-highlightedMenueItem" addItemAttributes="true" controller="public" action="licensingModel">
            <i class="smile outline icon"></i>${message(code:'menu.user.licensingModel')}
        </ui:link>
        <div class="divider"></div>

        <ui:link addItemAttributes="true" controller="logout">
            <i class="sign out alternate icon"></i>${message(code:'menu.user.logout')}
        </ui:link>
        <div class="divider"></div>

        <div class="header grey">
            Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}
        </div>
        <g:if test="${AppUtils.getCurrentServer() in [AppUtils.LOCAL, AppUtils.DEV, AppUtils.QA] && MuleCache.getEntry(MuleCache.CFG.SYSTEM_HEARTBEAT, AppUtils.PROD)}">
            <div class="header grey">
                Datenstand:
                <g:formatDate date="${MuleCache.getEntry(MuleCache.CFG.SYSTEM_HEARTBEAT, AppUtils.PROD).dateValue}" format="${message(code:'default.date.format.notime')}" />
            </div>
        </g:if>
        <div class="header grey">
            ${SystemActivityProfiler.getNumberOfActiveUsers()} Benutzer online
        </div>
    </div>
</div>
