<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.storage.RDStore" %>

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
                    <i class="${Icon.WEKB} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code: 'org.isWekbCurated.header.label')}"></i>
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

                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToClipboard('de')">
                    ${message(code: 'mail.copyToClipboard')}
                </button>
%{--                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('de')">--}%
%{--                    ${message(code: 'mail.openExternalMailer')}--}%
%{--                </button>--}%
            </div>
            <div class="content_lang_en hidden">
                <div class="field">
                    <label for="mailText">${message(code: 'mail.org.mailInfos')} (${orgInstance.name})</label>
                    <g:textArea id="mailText_en" name="mailText" rows="30" cols="1">${mailText['en']}</g:textArea>
                </div>

                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToClipboard('en')">
                    ${message(code: 'mail.copyToClipboard')}
                </button>
%{--                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('en')">--}%
%{--                    ${message(code: 'mail.openExternalMailer')}--}%
%{--                </button>--}%
            </div>
        </g:if>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.infoFlyout = {
        copyToEmailProgram: function (cid) {
            window.location.href = "mailto:?&body=" + $('#' + cid).val();
        },
        copyToClipboard: function (lang) {
            let content = $('#infoFlyout #mailText_' + lang);
            content.select();
            document.execCommand('copy');
            content.blur();
        }
    }
</laser:script>