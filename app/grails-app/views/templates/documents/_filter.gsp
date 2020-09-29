<%@page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDConstants" %>
<%
    List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License', RDConstants.DOCUMENT_TYPE),
                         RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE),
                         RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)]
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
<r:script>
    $(document).ready(function(){
        var org = false, license = false, subscription = false, pkg = false;

        setupDropdown();
        function setupDropdown() {
            $("#docTarget").dropdown({
               apiSettings: {
                   url: "<g:createLink controller="ajaxJson" action="lookupCombined"/>?query={query}&org="+org+"&license="+license+"&subscription="+subscription+"&package="+pkg,
                   cache: false
               },
               clearable: true,
               minCharacters: 0
            });
        }

        $(".targetList").checkbox({
            onChecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": org = true;
                    break;
                    case "license": license = true;
                    break;
                    case "subscription": subscription = true;
                    break;
                    case "pkg": pkg = true;
                    break;
                }
                setupDropdown();
            },
            onUnchecked: function() {
                $("#docTarget").dropdown('destroy');
                switch($(this).attr("id")){
                    case "org": org = false;
                    break;
                    case "license": license = false;
                    break;
                    case "subscription": subscription = false;
                    break;
                    case "pkg": pkg = false;
                    break;
                }
                setupDropdown();
            }
        });
    });
</r:script>