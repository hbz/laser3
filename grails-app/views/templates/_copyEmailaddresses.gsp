<!-- _copyEmailAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyEmailaddresses_ajaxModal'}"/>

<ui:modal id="${modalID}" text="${message(code:'menu.institutions.copy_emailaddresses', args:[orgList?.size()?:0])}" hideSubmitButton="true">
    <g:set var="rdvAllPersonFunctions" value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}" scope="request"/>
    <g:set var="rdvAllPersonPositions" value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}" scope="request"/>
    <div class="ui form la-filter segment la-clear-before">
        <div class="two fields">
            <div class="field">
                <label><g:message code="person.function.label" /></label>
                <ui:select class="ui dropdown search"
                           name="prsFunctionMultiSelect"
                           multiple=""
                           from="${rdvAllPersonFunctions}"
                           optionKey="id"
                           optionValue="value"
                           value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.id}"/>
            </div>
            <div class="field">
                <label><g:message code="person.position.label" /></label>
                <ui:select class="ui dropdown search"
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
        <%--<div class="field">
            <g:textArea id="emailAddressesTextArea" name="emailAddresses" readonly="false" rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" />
        </div>--%>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
    </div>
    <table class="ui table">
        <thead>
            <tr>
                <th><g:checkBox name="copyMailToggler" id="copyMailToggler" checked="true"/></th>
                <th><g:message code="org.sortname.label"/></th>
                <th>${RDStore.CCT_EMAIL.getI10n('value')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${orgList}" var="org">
                <tr id="org${org.id}">
                    <td><g:checkBox id="toCopyMail_${org.id}" name="copyMail" class="orgSelector" value="${org.id}" checked="true"/></td>
                    <td>${org.sortname}</td>
                    <td><span class="address"></span></td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.jsonOrgIdListDefault = <%=groovy.json.JsonOutput.toJson((Set) orgList.collect { it.id })%>;
        JSPC.app.jsonOrgIdList = null

        JSPC.app.copyToEmailProgram = function () {
            let emailAdresses = $(".address:visible").map((i, el) => el.innerText.trim()).get().join('; ');
            window.location.href = "mailto:" + emailAdresses;
        }

        JSPC.app.copyToClipboard = function () {
            let textArea = document.createElement("textarea");
            textArea.value = $(".address:visible").map((i, el) => el.innerText.trim()).get().join('; ');
            $("body").append(textArea);
            textArea.select();
            document.execCommand("copy");
            textArea.remove();
        }

        JSPC.app.updateTextArea = function () {
            var isPrivate = $("#privateContacts").is(":checked");
            var isPublic = $("#publicContacts").is(":checked");
            $(".address").text("");
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getEmailAddresses"/>'
                + '?isPrivate=' + isPrivate + '&isPublic=' + isPublic + '&selectedRoleTypIds=' + selectedRoleTypIds + '&orgIdList=' + JSPC.app.jsonOrgIdList,
                success: function (data) {
                    //$("#emailAddressesTextArea").val(data.join('; '));
                    $.each(data, function (i, e) {
                        $("#"+i+" span.address").text(e.join('; '));
                    });
                }
            });
        }

        JSPC.callbacks.modal.onShow['${modalID}'] = function(trigger) {
            if ($(trigger).attr('data-orgIdList')) {
                JSPC.app.jsonOrgIdList = $(trigger).attr('data-orgIdList').split(',');
            } else {
                JSPC.app.jsonOrgIdList = JSPC.app.jsonOrgIdListDefault;
            }
            JSPC.app.updateTextArea();
        };

        function updateMailAddressList() {
            JSPC.app.jsonOrgIdList = [];
            $('.orgSelector:checked').each(function(i) {
                JSPC.app.jsonOrgIdList.push($(this).val());
            });
            JSPC.app.updateTextArea();
        }

        $("#prsFunctionMultiSelect").change(function()  { JSPC.app.updateTextArea(); });
        $("#prsPositionMultiSelect").change(function()  { JSPC.app.updateTextArea(); });
        $("#privateContacts").change(function()         { JSPC.app.updateTextArea(); });
        $("#publicContacts").change(function()          { JSPC.app.updateTextArea(); });

        $('.orgSelector').change(function() {
            //updateMailAddressList();
            $('#org'+$(this).val()+' span.address').toggle();
        });

        $('#copyMailToggler').change(function() {
            if ($(this).prop('checked')) {
                $(".orgSelector").prop('checked', true);
                $('.address').show();
            } else {
                $(".orgSelector").prop('checked', false);
                $('.address').hide();
            }
            //updateMailAddressList();
        });

    </laser:script>

</ui:modal>
<!-- _copyEmailAddresses.gsp -->
