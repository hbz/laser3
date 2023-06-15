<%@ page import="de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.storage.RDStore" %>
<laser:htmlStart message="mail.sendMail.label" serviceInjection="true"/>

<ui:breadcrumbs>
    <g:if test="${surveyInfo}">
        <ui:crumb  controller="survey" action="show" id="${surveyInfo.id}"
                   text="${surveyInfo.name}" />
    </g:if>

    <ui:crumb text="${message(code: 'mail.sendMail.label')}" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="mail.sendMail.label" floated="true"/>



<ui:greySegment>

    <g:form action="processSendMail" controller="mail" method="post" class="ui form"
            params="[objectType: objectType, objectId: objectId]">

        <g:hiddenField name="reminderMail" value="${reminderMail}"/>
        <g:hiddenField name="fromMail" value="${mailFrom}"/>
        <g:if test="${userSurveyNotificationMails}">
        <g:hiddenField name="userSurveyNotificationMails" value="${userSurveyNotificationMails}"/>
            </g:if>

        <h2>${message(code: 'mail.sendMail.toOrgs', args: [orgList?.size() ?: 0])}</h2>

        <g:each in="${orgList}" var="org">
            <g:hiddenField name="selectedOrgs" value="${org.id}"/>
        </g:each>

        <g:if test="${userSurveyNotificationMails}">
            <div class="ui segment">
                <h3 class="ui header">${message(code: 'mail.sendMail.standard')}</h3>
                <div class="field">
                    <label for="userSurveyNotificationMails">${message(code: 'mail.sendMail.userMailsWithSurveyNotification')}</label>
                    ${de.laser.storage.RDStore.YN_YES.getI10n('value')}
                </div>
            </div>
        </g:if>


        <div class="ui segment">
        <g:if test="${userSurveyNotificationMails}">
                <h3 class="ui header">${message(code: 'mail.sendMail.additional')}</h3>
        </g:if>
        <g:set var="rdvAllPersonFunctions"
               value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION)}"
               scope="request"/>
        <g:set var="rdvAllPersonPositions" value="${PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION)}"
               scope="request"/>
        <div class="ui la-filter segment la-clear-before">
            <div class="field">
                <div>
                    <label><g:message code="person.function.label"/></label>
                </div>

                <div>
                    <ui:select class="ui dropdown search"
                               name="prsFunctionMultiSelect"
                               multiple=""
                               from="${rdvAllPersonFunctions}"
                               optionKey="id"
                               optionValue="value"/>
                </div>
            </div>

            <div class="field">
                <div>
                    <label><g:message code="person.position.label"/></label>
                </div>

                <div>
                    <ui:select class="ui dropdown search"
                               name="prsPositionMultiSelect"
                               multiple=""
                               from="${rdvAllPersonPositions}"
                               optionKey="id"
                               optionValue="value"/>
                </div>
            </div>
            <br/>

            <div class="field">
                <div class="ui checkbox">
                    <input type="checkbox" id="publicContacts" />
                    <label for="publicContacts">${message(code: 'email.fromPublicContacts')}</label>
                </div>

                <div class="ui checkbox">
                    <input type="checkbox" id="privateContacts" />
                    <label for="privateContacts">${message(code: 'email.fromPrivateAddressbook')}</label>
                </div>
            </div>
        </div>
        <br/>

        <div class="field">
            <label for="emailAddressesTextArea">${message(code: 'mail.sendMail.receiver')}</label>
            <g:textArea id="emailAddressesTextArea" name="emailAddresses" readonly="true" rows="5" cols="1"
                        style="width: 100%;"/>
        </div>
        </div>

        <div class="ui form">



            <div class="field">
                <label for="mailSubject">${message(code: 'mail.sendMail.mailSubject')}</label>

                <g:field type="text" name="mailSubject" id="mailSubject" readonly="true" value="${mailSubject}"/>
            </div>

            <div class="field">
                <label for="ccReceiver">${message(code: 'mail.sendMail.ccReceiver')}</label>

                <g:field type="text" name="ccReceiver" id="ccReceiver" value=""/>
            </div>

            <div class="field">
                <label for="bccReceiver">${message(code: 'mail.sendMail.bccReceiver')}</label>

                <g:field type="text" name="bccReceiver" id="bccReceiver" value=""/>
            </div>

            <div class="field">
                <label for="mailText">${message(code: 'mail.sendMail.mailText')}</label>
                <g:textArea id="emailText" name="mailText" rows="30" cols="1"
                            style="width: 100%;">${mailText}</g:textArea>
            </div>

            <g:if test="${surveyInfo}">
                <g:if test="${reminderMail}">
                    <g:link controller="survey" action="participantsReminder" id="${surveyInfo.id}">
                        <g:message code="default.button.back"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="openParticipantsAgain" id="${surveyInfo.id}">
                        <g:message code="default.button.back"/>
                    </g:link>
                </g:else>
            </g:if>

            <button class="ui icon button left floated" type="submit">
                ${message(code: 'mail.sendMail.sendButton')}
            </button>
        </div>
    </g:form>

   %{-- <button class="ui icon button right floated" onclick="JSPC.app.copyToEmailProgram()">
        ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
    </button>--}%
    <br/>
    <br/>

</ui:greySegment>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.jsonOrgIdList = <%=groovy.json.JsonOutput.toJson((Set) orgList.collect { it.id })%>;

        JSPC.app.copyToEmailProgram = function () {
            var emailAdresses = $("#emailAddressesTextArea").val();
            var emailText = $("#emailText").val();
            var emailSubject = $("#emailSubject").val();
            var mailContent = "?subject=" + emailSubject + "&body=" + emailText;

            window.location.href = "mailto:" + emailAdresses + mailContent;

        }

        JSPC.app.copyToClipboard = function () {
            $("#emailAddressesTextArea").select();
            document.execCommand("copy");
        }

        JSPC.app.updateTextArea = function () {
            var isPrivate = $("#privateContacts").is(":checked")
            var isPublic = $("#publicContacts").is(":checked")
            $("#emailAddressesTextArea").val("")
            var selectedRoleTypIds = $("#prsFunctionMultiSelect").val().concat( $("#prsPositionMultiSelect").val() );

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getEmailAddresses"/>'
                + '?isPrivate=' + isPrivate + '&isPublic=' + isPublic + '&selectedRoleTypIds=' + selectedRoleTypIds + '&orgIdList=' + JSPC.app.jsonOrgIdList,
                success: function (data) {
                    $("#emailAddressesTextArea").val(data.join('; '));
                }
            });
        }

        JSPC.app.updateTextArea();

        $("#prsFunctionMultiSelect").change(function()  { JSPC.app.updateTextArea(); });
        $("#prsPositionMultiSelect").change(function()  { JSPC.app.updateTextArea(); });
        $("#privateContacts").change(function()         { JSPC.app.updateTextArea(); });
        $("#publicContacts").change(function()          { JSPC.app.updateTextArea(); });

</laser:script>

<laser:htmlEnd/>
