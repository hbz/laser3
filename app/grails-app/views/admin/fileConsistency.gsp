<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code: "menu.admin.fileConsistency")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.fileConsistency" class="active"/>
</semui:breadcrumbs>
<br>

<h2 class="ui header la-noMargin-top">${message(code: "menu.admin.fileConsistency")}</h2>

<div class="ui grid">
    <div class="sixtenn wide column">

        <h3 class="ui headerline">Filesystem</h3>

        <table class="ui sortable celled la-table la-table-small la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Beschreibung</th>
                    <th></th>
                    <th>Anzahl</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Dateien im Filesystem</strong></td>
                    <td>${filePath}</td>
                    <td>${listOfFiles.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Dateiobjekt in der Datenbank existiert</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfFilesMatchingDocs.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entspr. Dateiobjekt in der Datenbank existiert nicht</td>
                    <td class="table-td-error">${listOfFilesNotMatchingDocs.join(', ')}</td>
                    <td class="table-td-error">${listOfFilesNotMatchingDocs.size()}</td>
                </tr>
            </tbody>
        </table>

        <h3 class="ui headerline">Tabelle Doc</h3>

         <table class="ui sortable celled la-table la-table-small la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Beschreibung</th>
                    <th></th>
                    <th>Anzahl</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Dateiobjekte in der Datenbank</strong></td>
                    <td>Doc(contentType = CONTENT_TYPE_BLOB)</td>
                    <td>${listOfDocs.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Datei existiert</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfDocs.size() - listOfDocsNotMatchingFiles.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entsprechende Datei existiert nicht</td>
                    <td class="table-td-error">${listOfDocsNotMatchingFiles.collect{ it.id }.join(', ')}</td>
                    <td class="table-td-error">${listOfDocsNotMatchingFiles.size()}</td>
                </tr>

                <tr>
                    <td><strong>Referenzierte Dateiobjekte in der Datenbank</strong></td>
                    <td>DocContext.owner = Doc(contentType = CONTENT_TYPE_BLOB)</td>
                    <td>${listOfDocsInUse.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Datei existiert</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfDocsInUse.size() - listOfDocsInUseNotMatchingFiles.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entspr. Datei existiert nicht</td>
                    <td class="table-td-error">${listOfDocsInUseNotMatchingFiles.collect{ it.id }.join(', ')}</td>
                    <td class="table-td-error">${listOfDocsInUseNotMatchingFiles.size()}</td>
                </tr>
            </tbody>
         </table>

        <h3 class="ui headerline">Tabelle DocContext</h3>

        <table class="ui sortable celled la-table la-table-small la-ignore-fixed table">
            <thead>
            <tr>
                <th>Beschreibung</th>
                <th>Spec.</th>
                <th>Anzahl</th>
            </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Gültige Referenzen auf Dateiobjekte in der Datenbank</strong></td>
                    <td>DocContext.owner(status != deleted) = Doc(contentType = CONTENT_TYPE_BLOB)</td>
                    <td>${numberOfDocContextsInUse}</td>
                </tr>
                <tr>
                    <td><strong>Gelöschte Referenzen auf Dateiobjekte in der Datenbank</strong></td>
                    <td>DocContext.owner(status = deleted) = Doc(contentType = CONTENT_TYPE_BLOB)</td>
                    <td>${numberOfDocContextsDeleted}</td>
                </tr>
            </tbody>
         </table>


    </div>
</div>


</body>
</html>
