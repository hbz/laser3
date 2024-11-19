<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'releaseNotes')}" />

<ui:h1HeaderWithIcon text="${message(code: 'releaseNotes')} (${AppUtils.getMeta('info.app.version')} / ${AppUtils.getMeta('info.app.build.date')})" type="help"/>

<br />

<div class="ui top attached menu">
    <g:each in="${history.reverse()}" var="h">
        <g:link controller="public" action="releaseNotes" id="${h}" class="item ${h == version ? 'active' : ''}">Version: ${h}</g:link>
    </g:each>
</div>

<div class="ui bottom attached segment la-markdown">
    <ui:renderMarkdown releaseNotes="${version}" />
</div>

<laser:htmlEnd />

