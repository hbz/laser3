<!-- __generateEmailWithAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: '_generateEmailWithAddresses_ajaxModal'}"/>

<ui:modal id="${modalID}" text="${messageCode ? message(code:messageCode) : ''}" hideSubmitButton="true">

    <label id="countHeader" class="ui header"></label>

    <g:set var="rdvAllPersonFunctions" value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}" scope="request"/>
    <g:set var="rdvAllPersonPositions" value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}" scope="request"/>
    <div class="ui la-filter segment la-clear-before">
        <div class="field">
            <div>
                <label><g:message code="person.function.label" /></label>
            </div>
            <div>
                <ui:select class="ui dropdown search"
                              name="prsFunctionMultiSelect2"
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
                <ui:select class="ui dropdown search"
                              name="prsPositionMultiSelect2"
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
                <input type="checkbox" id="publicContacts2" checked/>
                <label for="publicContacts2">${message(code:'email.fromPublicContacts')}</label>
            </div>
        <div class="ui checkbox">
            <input type="checkbox" id="privateContacts2" checked/>
            <label for="privateContacts2">${message(code:'email.fromPrivateAddressbook')}</label>
        </div>
    </div>
    </div>
    <br />

    <div class="ui form">
        <g:form name="generateMail" url="${formUrl}">
            <div class="field">
                <g:textArea id="emailAddressesTextArea2" name="emailAddresses" readonly="false" rows="5" cols="1"
                            class="myTargetsNeu" style="width: 100%;"/>
            </div>

            <div class="field">
                <g:textArea id="emailText" name="emailText" readonly="false" rows="5" cols="1"
                            class="myTargetsNeu" style="width: 100%;">${mailText}</g:textArea>
            </div>

            <button class="ui icon button right floated" onclick="JSPC.app.copyToClipboard()">
                ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
            </button>
            <button class="ui icon button right floated" onclick="JSPC.app.copyToEmailProgram()">
                ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
            </button>
            <g:if test="${submitButtonValue == 'ReminderMail'}">
                <button name="openOption" type="submit" value="ReminderMail" class="ui button left floated">
                    ${message(code: 'openParticipantsAgain.reminder.participantsHasAccess')}
                </button>
            </g:if>
            <br>
        </g:form>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.jsonOrgIdList = null

        JSPC.app.copyToEmailProgram = function () {
            var emailAdresses = $("#emailAddressesTextArea2").val();
            window.location.href = "mailto:" + emailAdresses;
        }

        JSPC.app.copyToClipboard = function () {
            $("#emailAddressesTextArea2").select();
            document.execCommand("copy");
        }

        JSPC.app.updateTextArea = function () {
            var isPrivate = $("#privateContacts2").is(":checked")
            var isPublic = $("#publicContacts2").is(":checked")

            var selectedRoleTypIds = $("#prsFunctionMultiSelect2").val().concat( $("#prsPositionMultiSelect2").val() );
            console.log('updateTextArea');
            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getEmailAddresses"/>'
                + '?isPrivate=' + isPrivate + '&isPublic=' + isPublic + '&selectedRoleTypIds=' + selectedRoleTypIds + '&orgIdList=' + JSPC.app.jsonOrgIdList,
                success: function (data) {
                    let addresses = [];
                    $.each(data, function (i, e) {
                        addresses.push(e.join('; ')); //join multiple addresses within an org - inner row
                    });
                    $("#emailAddressesTextArea2").val(addresses.join('; ')); //join addresses of all orgs - outer row
                }
            });
        }


        JSPC.callbacks.modal.onShow['${modalID}'] = function(trigger) {
          var orgIdList = '';
          var countChecked = 0;
          $.each($("tr[class!=disabled] input[name=selectedOrgs]"), function(i, checkbox) {


            if($(checkbox).prop('checked')){
            console.log($(checkbox).value);
                orgIdList = (orgIdList != '') ? orgIdList + ',' + $(checkbox).val() : $(checkbox).val() + ',';
                countChecked++;
            }

        });

            $("#countHeader").html(countChecked + " ${g.message(code: 'generateEmailWithAddresses.selectedOrgs')}");
            JSPC.app.jsonOrgIdList = orgIdList;
            JSPC.app.updateTextArea();
        };

        $("#prsFunctionMultiSelect2").change(function()  { JSPC.app.updateTextArea(); });
        $("#prsPositionMultiSelect2").change(function()  { JSPC.app.updateTextArea(); });
        $("#privateContacts2").change(function()         { JSPC.app.updateTextArea(); });
        $("#publicContacts2").change(function()          { JSPC.app.updateTextArea(); });

    </laser:script>

</ui:modal>
<!-- __generateEmailWithAddresses.gsp -->
