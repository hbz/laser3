<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart message="releaseNotes" serviceInjection="true"/>

<ui:h1HeaderWithIcon message="releaseNotes" type="help"/>

<g:set var="currentRelease" value="releases/${AppUtils.getMeta('info.app.version').take(3)}.md" />

<div class="ui segment">
    <h2>Version: ${AppUtils.getMeta('info.app.version')}</h2>

    <br/>

    <g:if test="${helpService.getResource( currentRelease )}">
        <% print helpService.parseMarkdown( currentRelease ) %>
    </g:if>
</div>

<laser:htmlEnd />

