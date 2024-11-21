<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'releaseNotes')}" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon text="${message(code: 'releaseNotes')} (${AppUtils.getMeta('info.app.version')})" type="help"/>
%{--<ui:h1HeaderWithIcon text="${message(code: 'releaseNotes')} (${AppUtils.getMeta('info.app.version')} / ${AppUtils.getMeta('info.app.build.date')})" type="help"/>--}%

%{--<div class="ui dropdown item">--}%
%{--    Schnellnavigation--}%
%{--    <i class="dropdown icon"></i>--}%
%{--    <div class="menu">--}%
%{--        <g:link controller="public" action="manual" class="item">${message(code: 'menu.user.manual')}</g:link>--}%
%{--        <g:link controller="public" action="faq" class="item">${message(code: 'menu.user.faq')}</g:link>--}%
%{--        <g:link controller="public" action="releaseNotes" class="item">${message(code: 'releaseNotes')}</g:link>--}%
%{--        <g:link controller="public" action="api" class="item">${message(code: 'apiRelease')}</g:link>--}%
%{--    </div>--}%
%{--</div>--}%

<div class="ui secondary pointing menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="releases" id="${h}" class="item ${h == version ? 'active' : ''}">Version: ${h}</g:link>
    </g:each>

    <g:link controller="public" action="api" class="item right floated"><icon:arrow /> ${message(code:'apiRelease')}</g:link>
</div>

<div class="ui segment la-markdown">
    <ui:renderMarkdown releaseNotes="${version}" />
</div>

<g:render template="markdownScript" />

<laser:htmlEnd />

