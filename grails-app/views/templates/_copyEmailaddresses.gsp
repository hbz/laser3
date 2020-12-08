<!-- _copyEmailAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.helper.RDStore; de.laser.helper.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyEmailaddresses_ajaxModal'}"/>

<semui:modal id="${modalID ?: 'copyEmailaddresses_ajaxModal'}" text="${message(code:'menu.institutions.copy_emailaddresses', args:[orgList?.size()?:0])}" hideSubmitButton="true">
    <g:set var="rdvAllPersonFunctions"  value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}" scope="request"/>
    <g:set var="rdvAllPersonPositions"  value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}" scope="request"/>
    <div class="ui la-filter segment la-clear-before">
        <div class="field">
            <div>
                <label><g:message code="person.function.label" /></label>
            </div>
            <div>
                <laser:select class="ui dropdown search"
                              name="prsFunctionMultiSelect"
                              multiple=""
                              from="${rdvAllPersonFunctions}"
                              optionKey="id"
                              optionValue="value"
                              value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.id}"/>
            </div>
        </div>
        <div class="field">
            <div>
                <label><g:message code="person.position.label" /></label>
            </div>
            <div>
                <laser:select class="ui dropdown search"
                              name="prsPositionMultiSelect"
                              multiple=""
                              from="${rdvAllPersonPositions}"
                              optionKey="id"
                              optionValue="value"
                              />
            </div>
        </div>
        <br />
        <div class="field">
            <div class="ui checkbox">
                <input type="checkbox" id="publicContacts" checked/>
                <label for="publicContacts">${message(code:'email.fromPublicContacts')}</label>
            </div>
        <div class="ui checkbox">
            <input type="checkbox" id="privateContacts" checked/>
            <label for="privateContacts">${message(code:'email.fromPrivateAddressbook')}</label>
        </div>
    </div>
    </div>
    <br />

    <div class="ui form">
        <div class="field">
            <g:textArea id="emailAddressesTextArea" name="emailAddresses" readonly="false" rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" />
        </div>
        <button class="ui icon button right floated" onclick="JSPC.copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="JSPC.copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br />
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.jsonOrgIdListDefault = <%=groovy.json.JsonOutput.toJson((Set) orgList.collect { it.id })%>;
        JSPC.jsonOrgIdList = null

        JSPC.copyToEmailProgram = function () {
            var emailAdresses = $("#emailAddressesTextArea").val();
            window.location.href = "mailto:" + emailAdresses;
        }

        JSPC.copyToClipboard = function () {
            $("#emailAddressesTextArea").select();
            document.execCommand("copy");
        }

        JSPC.updateTextArea = function () {
            var isPrivate = $("#privateContacts").is(":checked")
            var isPublic = $("#publicContacts").is(":checked")
            $("#emailAddressesTextArea").val("")
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getEmailAddresses"/>'
                + '?isPrivate=' + isPrivate + '&isPublic=' + isPublic + '&selectedRoleTypIds=' + selectedRoleTypIds + '&orgIdList=' + JSPC.jsonOrgIdList,
                success: function (data) {
                    $("#emailAddressesTextArea").val(data.join('; '));
                }
            });
        }

        JSPC.callbacks.modal.show.${modalID ?: 'copyEmailaddresses_ajaxModal'} = function(trigger) {
            if ($(trigger).attr('data-orgIdList')) {
                JSPC.jsonOrgIdList = $(trigger).attr('data-orgIdList').split(',');
            } else {
                JSPC.jsonOrgIdList = JSPC.jsonOrgIdListDefault;
            }
            JSPC.updateTextArea();
        };

        $("#prsFunctionMultiSelect").change(function()  { JSPC.updateTextArea(); });
        $("#prsPositionMultiSelect").change(function()  { JSPC.updateTextArea(); });
        $("#privateContacts").change(function()         { JSPC.updateTextArea(); });
        $("#publicContacts").change(function()          { JSPC.updateTextArea(); });

    </laser:script>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
