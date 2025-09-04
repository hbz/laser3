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
            ${message(code:'releaseNotes')}
        </ui:link>
        <ui:link addItemAttributes="true" controller="public" action="api" id="${ApiManager.HISTORY[0]}">
            ${message(code:'apiRelease')}
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

        <div class="header">
            <p class="sc_grey">
                Version: ${AppUtils.getMeta('info.app.version')} / ${AppUtils.getMeta('info.app.build.date')}
            </p>
            <g:if test="${AppUtils.getCurrentServer() in [AppUtils.LOCAL, AppUtils.DEV, AppUtils.QA] && MuleCache.getEntry(MuleCache.CFG.SYSTEM_HEARTBEAT, AppUtils.PROD)}">
                <p class="sc_grey">
                    Datenbasis: <g:formatDate date="${MuleCache.getEntry(MuleCache.CFG.SYSTEM_HEARTBEAT, AppUtils.PROD).dateValue}" format="${message(code:'default.date.format.notime')}" />
                </p>
            </g:if>
            <p class="sc_grey">
                ${SystemActivityProfiler.getNumberOfActiveUsers()} Benutzer online
            </p>
        </div>
    </div>
</div>
