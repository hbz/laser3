<%@ page import="de.laser.api.v0.ApiManager; de.laser.utils.LocaleUtils; de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'apiRelease')}" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon text="${message(code: 'apiRelease')}" type="help"/>
%{--<ui:h1HeaderWithIcon text="${message(code: 'apiRelease')} (${ApiManager.VERSION})" type="help"/>--}%

<div class="ui secondary pointing menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="api" id="${h}" class="item ${h == version ? 'active' : ''}">${h != 'legacy' ? 'Version: ' + h : 'Archiv'}</g:link>
    </g:each>

    <g:link controller="public" action="releases" class="item right floated"><icon:arrow /> ${message(code:'releaseNotes')}</g:link>
</div>

<div class="ui segment la-markdown">
    <ui:renderMarkdown api="${version}" />
</div>

<g:render template="markdownScript" />

<laser:htmlEnd />

