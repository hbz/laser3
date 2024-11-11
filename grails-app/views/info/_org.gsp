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

%{--        <g:if test="${actionName == 'mailInfos' && controllerName == 'organisation'}">--}%
%{--            <g:form controller="organisation" action="mailInfos" params="${[id: orgInstance.id, subscription: sub?.id, surveyConfigID: params.surveyConfigID]}">--}%
%{--                <div class="field">--}%
%{--                    <label for="newLanguage">${message(code: 'profile.language')}</label>--}%
%{--                    <select id="newLanguage" name="newLanguage" class="ui search selection fluid dropdown" onchange="this.form.submit()">--}%
%{--                        <g:each in="${[RDStore.LANGUAGE_DE, RDStore.LANGUAGE_EN]}" var="lan">--}%
%{--                            <option <%=language == lan.value ? 'selected="selected"' : ''%> value="${lan.value}">${lan.getI10n('value')}</option>--}%
%{--                        </g:each>--}%
%{--                    </select>--}%
%{--                </div>--}%
%{--            </g:form>--}%
%{--        </g:if>--}%

        <g:render template="flyoutLanguageSelector" />

        <g:if test="${mailAddressOfProvider}">
            <div class="field">
                <label for="mailAddressOfProvider">${message(code: 'provider.label')}: E-Mails</label>
                <input type="text" name="mailAddressOfProvider" id="mailAddressOfProvider" readonly="readonly" value="${mailAddressOfProvider}"/>
            </div>
        </g:if>

        <g:if test="${mailAddressOfProviderWekb}">
            <div class="field">
                <label for="mailAddressOfProviderWekb">
                    <i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code: 'org.isWekbCurated.header.label')}"></i>
                    ${message(code: 'provider.label')}: E-Mails
                </label>

                <input type="text" name="mailAddressOfProviderWekb" id="mailAddressOfProviderWekb" readonly="readonly" value="${mailAddressOfProviderWekb}"/>
            </div>
        </g:if>

        <g:if test="${mailText}">
            <div class="content_lang_de">
                <div class="field">
                    <label for="mailText">${message(code: 'mail.org.mailInfos')} (${orgInstance.name})</label>
                    <g:textArea id="mailText_de" name="mailText" rows="30" cols="1">${mailText['de']}</g:textArea>
                </div>

                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToClipboard('mailText_de')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
                </button>
            </div>
            <div class="content_lang_en hidden">
                <div class="field">
                    <label for="mailText">${message(code: 'mail.org.mailInfos')} (${orgInstance.name})</label>
                    <g:textArea id="mailText_en" name="mailText" rows="30" cols="1">${mailText['en']}</g:textArea>
                </div>

                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToClipboard('mailText_en')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
                </button>
            </div>

        %{-- <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToEmailProgram()">
           ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
         </button>--}%

        </g:if>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.infoFlyout = {
        copyToEmailProgram: function (cid) {
            window.location.href = "mailto:?&body=" + $('#' + cid).val();
        },
        copyToClipboard: function (cid) {
            let content = $('#' + cid);
            content.select();
            document.execCommand('copy');
            content.blur();
        }
    }

    $('a.infoFlyout-language').on ('click', function(e) {
        let lang = $(this).attr('data-lang');
        $('.content_lang_de, .content_lang_en').addClass('hidden');
        $('.content_lang_' + lang).removeClass('hidden');
    });
</laser:script>