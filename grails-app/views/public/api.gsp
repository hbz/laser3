<%@ page import="de.laser.api.v0.ApiManager; de.laser.utils.LocaleUtils; de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="API-${message(code: 'releaseNotes')}" />

<ui:h1HeaderWithIcon text="API-${message(code: 'releaseNotes')} (${ApiManager.VERSION})" type="help"/>

<br />

<div class="ui top attached menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="api" id="${h}" class="item ${h == version ? 'active' : ''}">${h != 'legacy' ? 'Version: ' + h : 'Archiv'}</g:link>
    </g:each>
</div>

<div class="ui bottom attached segment la-markdown">
    <ui:renderMarkdown api="${version}" />
</div>

<laser:htmlEnd />

