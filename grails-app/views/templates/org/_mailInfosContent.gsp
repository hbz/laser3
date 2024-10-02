<%@ page import="de.laser.storage.RDStore" %>
<h1 class="ui header">
    <g:if test="${sub}">
        ${message(code: 'mail.sub.mailInfos')} ${sub.getLabel()}
    </g:if>
    <g:else>
        ${message(code: 'mail.org.mailInfos')} (${orgInstance.name})
    </g:else>
</h1>

<div class="content">

    <div class="ui form">

        <g:if test="${actionName == 'mailInfos' && controllerName == 'organisation'}">
            <g:form controller="organisation" action="mailInfos" params="${[id: orgInstance.id, subscription: sub?.id, surveyConfigID: params.surveyConfigID]}">
                <div class="field">
                    <label for="newLanguage">${message(code: 'profile.language')}</label>
                    <select id="newLanguage" name="newLanguage" class="ui search selection fluid dropdown" onchange="this.form.submit()">
                        <g:each in="${[RDStore.LANGUAGE_DE, RDStore.LANGUAGE_EN]}" var="lan">
                            <option <%=language == lan.value ? 'selected="selected"' : ''%> value="${lan.value}">${lan.getI10n('value')}</option>
                        </g:each>
                    </select>
                </div>
            </g:form>
        </g:if>
        <g:else>
            <div class="field">
                <label for="newLanguage">${message(code: 'profile.language')}:</label>

                <div class="ui buttons">
                    <a href="#" class="ui button mailInfos-flyout-trigger" data-orgId="${orgInstance.id}" data-subId="${params.subscription}"
                       data-surveyConfigId="${params.surveyConfigID}" data-lang="${RDStore.LANGUAGE_DE.value}">
                        ${RDStore.LANGUAGE_DE}
                    </a>

                    <div class="or" data-text="${message(code: 'default.or')}"></div>
                    <a href="#" class="ui button mailInfos-flyout-trigger" data-orgId="${orgInstance.id}" data-subId="${params.subscription}"
                       data-surveyConfigId="${params.surveyConfigID}" data-lang="${RDStore.LANGUAGE_EN.value}">
                        ${RDStore.LANGUAGE_EN}
                    </a>
                </div>
            </div>
        </g:else>

        <g:if test="${mailAddressOfProvider}">
            <div class="field">
                <label for="mailAddressOfProvider">${message(code: 'provider.label')}: E-Mails</label>
                <input type="text" name="mailAddressOfProvider" id="mailAddressOfProvider" readonly="readonly" value="${mailAddressOfProvider}"/>
            </div>
        </g:if>

        <g:if test="${mailAddressOfProviderWekb}">
            <div class="field">
                <label for="mailAddressOfProviderWekb"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'org.isWekbCurated.header.label')}"></i>${message(code: 'provider.label')}: E-Mails
                </label>

                <input type="text" name="mailAddressOfProviderWekb" id="mailAddressOfProviderWekb" readonly="readonly" value="${mailAddressOfProviderWekb}"/>
            </div>
        </g:if>

        <g:if test="${mailText}">
            <div class="field">
                <label for="mailText">${message(code: 'mail.org.mailInfos')} (${orgInstance.name})</label>
                <g:textArea id="emailText" name="mailText" rows="30" cols="1"
                            style="width: 100%;">${mailText}</g:textArea>
            </div>

            <button class="ui icon button right floated" onclick="mailInfosCopyToClipboard()">
                ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
            </button>
        %{-- <button class="ui icon button right floated" onclick="mailInfosCopyToEmailProgram()">
           ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
         </button>--}%
        </g:if>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    mailInfosCopyToEmailProgram = function () {
        var emailAdresses = $("#emailText").val();
        window.location.href = "mailto:?&body=" + emailAdresses;
    }

    mailInfosCopyToClipboard = function () {
        $("#emailText").select();
        document.execCommand("copy");
    }

      $('a.mailInfos-flyout-trigger').on ('click', function(e) {
         e.preventDefault()
            let cell = $(this);
            let data = {
                id: cell.attr("data-orgId"),
                subscription: cell.attr("data-subId"),
                surveyConfigID: cell.attr("data-surveyConfigId"),
                newLanguage: cell.attr("data-lang"),
             };

         $('#globalLoadingIndicator').show()
             $.ajax ({
                 url: "<g:createLink controller="ajaxHtml" action="mailInfosFlyout"/>",
                 data: data
                }).done (function (response) {
                    $('#mailInfosFlyout').html (response)
                    $('#mailInfosFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#mailInfosFlyout')
                    r2d2.initDynamicXEditableStuff ('#mailInfosFlyout')
                })
        });

</laser:script>