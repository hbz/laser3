<%@ page import="de.laser.storage.RDStore" %>

<laser:htmlStart text="Titles Enrichment" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Title Enrichment of series_name, monograph_parent_collection_title, subject_reference, summary_of_content" />

<ui:messages data="${flash}"/>

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

<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</laser:script>

<laser:htmlEnd />
