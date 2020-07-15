<!-- _copyEmailAddresses.gsp -->
<%@ page import="com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact" %>
<%@ page import="de.laser.helper.RDStore;de.laser.helper.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyEmailaddresses_ajaxModal'}"/>

<semui:modal id="${modalID ?: 'copyEmailaddresses_ajaxModal'}" text="${message(code:'menu.institutions.copy_emailaddresses', args:[orgList?.size()?:0])}" hideSubmitButton="true">
    %{--<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'menu.institutions.copy_emailaddresses', args:[orgList?.size()?:0])}--}%
    %{--<semui:totalNumber total="${orgList?.size()?:0}"/>--}%
    %{--</h1>--}%
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
        <button class="ui icon button right floated" onclick="copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br>
    </div>

    <g:javascript>
        // modals
        $("*[data-semui='modal']").click(function() {

            var href = $(this).attr('data-href')
            if (! href) {
                href = $(this).attr('href')
            }
            $(href + '.ui.modal').modal({
                onVisible: function() {
                    updateTextArea();
                    // $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                },
                detachable: true,
                autofocus: false,
                closable: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                }
            }).modal('show')
        });

        var jsonOrgIdList = <%=groovy.json.JsonOutput.toJson((Set)orgList.collect{it.id})%>;

        $("#prsFunctionMultiSelect").change(function() { updateTextArea(); });
        $("#prsPositionMultiSelect").change(function() { updateTextArea(); });
        $("#privateContacts").change(function() { updateTextArea(); });
        $("#publicContacts").change(function() { updateTextArea(); });

        function copyToEmailProgram() {
            var emailAdresses = $("#emailAddressesTextArea").val();
            window.location.href = "mailto:"+emailAdresses;
        }

        function copyToClipboard() {
            $("#emailAddressesTextArea").select();
            document.execCommand("copy");
        }

        function updateTextArea() {
            var isPrivate = $("#privateContacts").is(":checked")
            var isPublic = $("#publicContacts").is(":checked")
            $("#emailAddressesTextArea").val("")
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );

            $.ajax({
                url: '<g:createLink controller="ajax" action="getEmailAddresses"/>'
                + '?isPrivate=' + isPrivate + '&isPublic=' + isPublic + '&selectedRoleTypIds=' + selectedRoleTypIds + '&orgIdList=' + jsonOrgIdList + '&format=json',
                success: function (data) {
                    $("#emailAddressesTextArea").val(data.join('; '));
                }
            });

        }
    </g:javascript>

</semui:modal>
<!-- _copyEmailAddresses.gsp -->
