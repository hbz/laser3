<%@ page import="de.laser.helper.RDStore" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : Titles Enrichment</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
</semui:breadcrumbs>

<br>
<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>Title Enrichment of series_name, monograph_parent_collection_title, subject_reference, summary_of_content</h1>

<br>
<br>
<br>

<semui:messages data="${flash}"/>

<br>
<g:form class="ui form" controller="admin" action="titleEnrichment" params="" method="post"
        enctype="multipart/form-data">
    <div class="two fields">
        <div class="field">
            <div class="ui fluid action input">
                <input type="text" readonly="readonly"
                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                       style="display: none;">

                <div class="ui icon button">
                    <i class="attach icon"></i>
                </div>
            </div>
        </div>

        <div class="field">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.preselect')}"
                   class="fluid ui button"/>
        </div>
    </div>
</g:form>

<r:script>
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</r:script>

</body>
</html>
