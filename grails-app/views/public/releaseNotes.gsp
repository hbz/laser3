<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'releaseNotes')}" serviceInjection="true"/>

<ui:h1HeaderWithIcon text="Aktuelle Version: ${AppUtils.getMeta('info.app.version')}" type="help"/>
%{--<ui:h1HeaderWithIcon text="Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}" type="help"/>--}%

<br />

<div class="ui menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="releaseNotes" id="${h}" class="item ${h == version ? 'active' : ''}">Version: ${h}</g:link>
    </g:each>
</div>

<div class="ui segment">
    <ui:renderMarkdown releaseNotes="${version}" />
</div>

<laser:htmlEnd />

