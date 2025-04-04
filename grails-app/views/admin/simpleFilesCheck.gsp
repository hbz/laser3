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
            <div class="item">
                <div class="content"><i class="file outline icon"></i> Dateien gefunden: <strong>${dsFiles.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.YES}"></i> Matchende DOC-Verweise: <strong>${validDocs.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.YES} green"></i> Dateien mit gültigem DOC-Verweis: <strong>${validFiles.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.NO} red"></i>Dateien ohne gültigen DOC-Verweis: <strong>${invalidFiles.size()}</strong></div>
            </div>
        </div>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Matchende DOC-Verweise: ${validDocs.size()}</div>
    </div>
    <div class="content">
        <g:each in="${validDocs}" var="doc" status="i">
            <g:if test="${doc.ckey}"><span>${doc.id}</span></g:if><g:else><span class="sc_darkgrey">${doc.id}</span></g:else>
            <g:if test="${i < validDocs.size()-1}">, </g:if>
        </g:each>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Dateien mit gültigem DOC-Verweis: ${validFiles.size()}</div>
    </div>
    <div class="content">
        ${validFiles.join(', ')}
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Dateien ohne gültigen DOC-Verweis: ${invalidFiles.size()}</div>
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
