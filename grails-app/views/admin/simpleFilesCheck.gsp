<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.simpleFilesCheck" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.simpleFilesCheck" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.simpleFilesCheck" type="admin"/>

<div class="ui fluid card">
    <div class="content">
        <div class="header">documentStorage: ${dsPath}</div>
    </div>
    <div class="content">
        <div class="ui list">
            <div class="item">Dateien gefunden: ${dsFiles.size()}</div>
            <div class="item">Zugehörige DOC-Verweise insgesamt: ${validDocs.size()}</div>
            <div class="item">Dateien mit gültigem DOC-Verweis: ${validFiles.size()}</div>
            <div class="item">Dateien ohne gültigen DOC-Verweis: ${invalidFiles.size()}</div>
        </div>
    </div>
</div>

%{--<div class="ui fluid card">--}%
%{--    <div class="content">--}%
%{--        <div class="header">Dateien</div>--}%
%{--    </div>--}%
%{--    <div class="content">--}%
%{--        ${dsFiles.join(', ')}--}%
%{--    </div>--}%
%{--</div>--}%

<div class="ui fluid card">
    <div class="content">
        <div class="header">DOC-Verweise</div>
    </div>
    <div class="content">
        ${validDocs.collect{ it.ckey ? '!' + it.id : it.id }.join(', ')}
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Dateien mit gültigem DOC-Verweis</div>
    </div>
    <div class="content">
        ${validFiles.join(', ')}
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Dateien ohne gültigen DOC-Verweis</div>
    </div>
    <div class="content">
        ${invalidFiles.join(', ')}

%{--        <g:if test="${invalidFiles}">--}%
%{--            <br/>--}%
%{--            <br/>--}%
%{--            <g:link controller="admin" action="simpleFilesCheck" params="${[moveOutdatedFiles: 1]}"--}%
%{--                    class="${Btn.NEGATIVE_CONFIRM}"--}%
%{--                    data-confirm-tokenMsg="Ungültige Dateien in den 'Outdated'-Ordner verschieben?"--}%
%{--                    data-confirm-term-how="ok">Aufräumen</g:link>--}%
%{--        </g:if>--}%
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">documentStorage (outdated) : ${xxPath}</div>
    </div>
    <div class="content">
        <div class="ui list">
            <div class="item">Dateien gefunden: ${xxFiles.size()}</div>
        </div>
    </div>
</div>

<laser:htmlEnd />
