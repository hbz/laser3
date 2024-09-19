<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'releaseNotes')}" serviceInjection="true"/>

%{--<ui:h1HeaderWithIcon text="${message(code: 'releaseNotes')}" type="help"/>--}%
%{--<h2 class="ui header right aligned">Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}</h2>--}%

<ui:h1HeaderWithIcon text="Version: ${AppUtils.getMeta('info.app.version')} – ${AppUtils.getMeta('info.app.build.date')}" type="help"/>

<div class="ui segment">
    <g:if test="${helpService.getResource( currentVersionMarkdownFile )}">
        <md:render file="${currentVersionMarkdownFile}" />
    </g:if>
</div>

<laser:htmlEnd />

