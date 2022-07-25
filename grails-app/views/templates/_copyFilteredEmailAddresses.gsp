<!-- _copyEmailAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyFilteredEmailAddresses_ajaxModal'}"/>

<ui:modal id="${modalID}" text="${orgList ? message(code:'menu.institutions.copy_emailaddresses', args:[orgList.size()?:0]) : message(code:'menu.institutions.copy_emailaddresses.button')}" hideSubmitButton="true">

    <div class="ui form">
        <div class="field">
            <g:textArea id="filteredEmailAddressesTextArea" name="filteredEmailAddresses" readonly="false"
                        rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" >${emailAddresses ? emailAddresses.join('; '): ''}</g:textArea>
        </div>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br />
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.copyToEmailProgram = function() {
            var emailAdresses = $("#filteredEmailAddressesTextArea").val();
            window.location.href = "mailto:"+emailAdresses;
        }

        JSPC.app.copyToClipboard = function() {
            $("#filteredEmailAddressesTextArea").select();
            document.execCommand("copy");
        }
    </laser:script>

</ui:modal>
<!-- _copyEmailAddresses.gsp -->
