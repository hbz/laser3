<%@ page import="de.laser.RefdataValue;de.laser.auth.Role;de.laser.utils.LocaleUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>

<laser:htmlStart message="help.technicalHelp.uploadFile.manuel" />

<ui:breadcrumbs>
    <ui:crumb message="menu.institutions.help" controller="profil" action="help"/>
    <ui:crumb message="help.technicalHelp.uploadFile.manuel" class="active"/>

</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="help.technicalHelp.uploadFile.manuel" type="help"/>
<br />

<div class="ui segment la-markdown">
    <ui:renderMarkdown manual="variousImport" />
</div>

<g:render template="/public/markdownScript" />


<laser:htmlEnd />
