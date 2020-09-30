<!-- _copyEmailAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.helper.RDStore; de.laser.helper.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyFilteredEmailAddresses_ajaxModal'}"/>

<semui:modal id="${modalID ?: 'copyFilteredEmailAddresses_ajaxModal'}" text="${orgList ? message(code:'menu.institutions.copy_emailaddresses', args:[orgList.size()?:0]) : message(code:'menu.institutions.copy_emailaddresses.button')}" hideSubmitButton="true">

    <div class="ui form">
        <div class="field">
            <g:textArea id="filteredEmailAddressesTextArea" name="filteredEmailAddresses" readonly="false"
                        rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" >${emailAddresses ? emailAddresses.join('; '): ''}</g:textArea>
        </div>
        <button class="ui icon button right floated" onclick="copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br>
    </div>

    <g:javascript>
        function copyToEmailProgram() {
            var emailAdresses = $("#filteredEmailAddressesTextArea").val();
            window.location.href = "mailto:"+emailAdresses;
        }

        function copyToClipboard() {
            $("#filteredEmailAddressesTextArea").select();
            document.execCommand("copy");
        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
