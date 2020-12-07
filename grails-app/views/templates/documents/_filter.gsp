<%@page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDConstants; de.laser.helper.RDStore" %>
<%
    List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
    List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
%>
<semui:filter>
    <g:form id="documentFilter" class="ui form" controller="${controllerName}" action="documents">
        <div class="two fields">
            <div class="field">
                <label for="docTitle">${message(code:'license.docs.table.title')}</label>
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
            <a href="${request.forwardURI}" class="ui reset primary primary button">${message(code:'default.button.reset.label')}</a>

            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
        </div>
    </g:form>
</semui:filter>
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.cfg = {}
    JSPC.cfg.org = false;
    JSPC.cfg.lic = false
    JSPC.cfg.sub = false;
    JSPC.cfg.pkg = false;

    JSPC.setupDropdown = function () {
        $("#docTarget").dropdown({
           apiSettings: {
                url: "<g:createLink controller="ajaxJson" action="lookupCombined"/>?query={query}&org="+ JSPC.cfg.org +"&license="+ JSPC.cfg.lic +"&subscription="+ JSPC.cfg.sub +"&package="+ JSPC.cfg.pkg,
                cache: false
               },
               clearable: true,
               minCharacters: 0
            });
    };
        
    JSPC.setupDropdown();

        $(".targetList").checkbox({
            onChecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": JSPC.cfg.org = true;
                    break;
                    case "license": JSPC.cfg.lic = true;
                    break;
                    case "subscription": JSPC.cfg.sub = true;
                    break;
                    case "pkg": JSPC.cfg.pkg = true;
                    break;
                }
                JSPC.setupDropdown();
            },
            onUnchecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": JSPC.cfg.org = false;
                    break;
                    case "license": JSPC.cfg.lic = false;
                    break;
                    case "subscription": JSPC.cfg.sub = false;
                    break;
                    case "pkg": JSPC.cfg.pkg = false;
                    break;
                }
                JSPC.setupDropdown();
            }
        });

</laser:script>