<%@ page import="de.laser.Doc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.simpleFilesCheck" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.simpleFilesCheck" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.simpleFilesCheck" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="admin" action="simpleFilesCheck" class="item active">Dateisystem</g:link>
    <g:link controller="admin" action="simpleDocsCheck" class="item">Datenbank</g:link>
    <g:link controller="admin" action="simpleShareConfCheck" class="item">Sichtbarkeit</g:link>
</nav>

<div class="ui fluid card">
    <div class="content">
        <div class="header">documentStorage</div>
    </div>
    <div class="content">
        <div class="ui list">
            <div class="item">
                <div class="content"><icon:pathFolder/> Pfad: <strong>${dsPath}</strong></div>
            </div>
            <div class="item">
                <div class="content"><icon:pathFile/> Dateien gefunden: <strong>${dsFiles.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="check double icon"></i> Matchende Doc.UUID-Referenzen: <strong>${validDocs.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.YES} green"></i> Dateien mit gültiger Doc.UUID-Referenz: <strong>${validFiles.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.YES} yellow"></i> Dateien mit gültiger Doc.UUID-Referenz, aber unverschlüsselt: <strong>${validFilesRaw.size()}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.NO} red"></i> Dateien ohne gültige Doc.UUID-Referenz: <strong>${invalidFiles.size()}</strong></div>
            </div>
        </div>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"><i class="check double icon"></i> Matchende Doc.UUID-Referenzen: ${validDocs.size()}</div>
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
        <div class="header"><i class="${Icon.SYM.YES} green"></i> Dateien mit gültiger Doc.UUID-Referenz: ${validFiles.size()}</div>
    </div>
    <div class="content">
        ${validFiles.join(', ')}
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"><i class="${Icon.SYM.YES} yellow"></i> Dateien mit gültiger Doc.UUID-Referenz, aber unverschlüsselt: ${validFilesRaw.size()}</div>
    </div>
    <div class="content">
        ${validFilesRaw.join(', ')}

        <g:if test="${validFilesRaw}">
            <br/>
            <br/>
            <g:link controller="admin" action="simpleFilesCheck" params="${[encryptRawFiles: 1]}"
                    class="${Btn.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="Unverschlüsselte Dateien verschlüsseln?"
                    data-confirm-term-how="ok">Verschlüsseln (max.250)</g:link>
        </g:if>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"><i class="${Icon.SYM.NO} red"></i> Dateien ohne gültige Doc.UUID-Referenz: ${invalidFiles.size()}</div>
    </div>
    <div class="content">
        ${invalidFiles.join(', ')}

        <g:if test="${invalidFiles}">
            <br/>
            <br/>
            <g:link controller="admin" action="simpleFilesCheck" params="${[moveOutdatedFiles: 1]}"
                    class="${Btn.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="Ungültige Dateien in den 'Outdated'-Ordner verschieben?"
                    data-confirm-term-how="ok">Aufräumen (max.1000)</g:link>
        </g:if>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">documentStorage (outdated)</div>
    </div>
    <div class="content">
        <div class="ui list">
            <div class="item">
                <div class="content"><icon:pathFolder/> Pfad: <strong>${xxPath}</strong></div>
            </div>
            <div class="item">
                <div class="content"><icon:pathFile/> Dateien gefunden: <strong>${xxFiles.size()}</strong></div>
            </div>
        </div>
%{--        <g:if test="${xxFiles}">--}%
%{--            <br />--}%
%{--        </g:if>--}%
%{--        ${xxFiles.join(', ')}--}%
    </div>
</div>

<laser:htmlEnd />
