<%@page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<%
    List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
    List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
%>
<ui:filter simple="true">
    <g:form id="documentFilter" class="ui form" controller="${controllerName}" action="documents">
        <div class="two fields">
            <div class="field">
                <label for="docTitle">${message(code:'default.title.label')}</label>
                <input type="text" id="docTitle" name="docTitle" value="${params.docTitle}">
            </div>
            <div class="field">
                <label for="docFilename">${message(code:'license.docs.table.fileName')}</label>
                <input type="text" id="docFilename" name="docFilename" value="${params.docFilename}">
            </div>
        </div>
        <div class="two fields">
            <div class="field">
                <label for="docCreator">${message(code:'license.docs.table.creator')}</label>
                <g:select class="ui fluid search dropdown" id="docCreator" name="docCreator" from="${availableUsers}" optionKey="id" optionValue="display" value="${params.docCreator}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}" />
            </div>
            <div class="field">
                <label for="docType">${message(code:'license.docs.table.type')}</label>
                <g:select class="ui fluid search dropdown" id="docType" name="docType" from="${documentTypes}" optionKey="id" optionValue="${{it.getI10n("value")}}" value="${params.docType}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}" />
            </div>
        </div>
        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>

            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
        </div>
    </g:form>
</ui:filter>
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.cfg = {
        org: false, lic: false, sub: false, pkg: false
    }

    JSPC.app.setupDropdown = function () {
        $("#docTarget").dropdown({
           apiSettings: {
                url: "<g:createLink controller="ajaxJson" action="lookupCombined"/>?query={query}&org="+ JSPC.app.cfg.org +"&license="+ JSPC.app.cfg.lic +"&subscription="+ JSPC.app.cfg.sub +"&package="+ JSPC.app.cfg.pkg,
                cache: false
               },
               clearable: true,
               minCharacters: 0
            });
    };
        
    JSPC.app.setupDropdown();

        $(".targetList").checkbox({
            onChecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": JSPC.app.cfg.org = true;
                    break;
                    case "license": JSPC.app.cfg.lic = true;
                    break;
                    case "subscription": JSPC.app.cfg.sub = true;
                    break;
                    case "pkg": JSPC.app.cfg.pkg = true;
                    break;
                }
                JSPC.app.setupDropdown();
            },
            onUnchecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": JSPC.app.cfg.org = false;
                    break;
                    case "license": JSPC.app.cfg.lic = false;
                    break;
                    case "subscription": JSPC.app.cfg.sub = false;
                    break;
                    case "pkg": JSPC.app.cfg.pkg = false;
                    break;
                }
                JSPC.app.setupDropdown();
            }
        });

</laser:script>