<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'releaseNotes')}" serviceInjection="true"/>

<ui:h1HeaderWithIcon text="${message(code: 'releaseNotes')}" type="help"/>

<g:set var="currentRelease" value="releases/${AppUtils.getMeta('info.app.version').take(3)}.md" />

<div class="ui segment">
    <g:if test="${helpService.getResource( currentRelease )}">
        <% print helpService.parseMarkdown( currentRelease ) %>
    </g:if>
</div>

<laser:htmlEnd />

