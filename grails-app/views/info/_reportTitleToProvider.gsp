<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>

<h1 class="ui header">
    ${message(code:'tipp.reportTitleToProvider.mailto')}
</h1>

<div class="content">
    <div class="ui form">

        <g:render template="flyoutLanguageSelector" />

        <g:if test="${mailtoList}">
            <div class="field">
                <label for="mailto">${message(code: 'mail.to')}</label>
                <div class="ui fluid selection dropdown la-not-clearable">
                    <input type="hidden" name="mailto" id="mailto" value="${mailtoList[0][2].content}">
                    <i class="dropdown icon"></i>
                    <div class="default text">Bitte auswählen</div>
                    <div class="menu">
                        <g:each in="${mailtoList}" var="prc" status="i">
                            <div class="item" data-value="${prc[2].content}">
                                <span class="description">
                                    <i class="${prc[0].isPublic ? Icon.WEKB : Icon.ACP_PRIVATE}"></i>
                                    <g:if test="${prc[2].language?.value == 'ger'}"><i class="flag de"></i></g:if>
                                    <g:if test="${prc[2].language?.value == 'eng'}"><i class="flag gb"></i></g:if>

                                    ${prc[1].getI10n('value')}
                                </span>
                                <span class="text">
                                    ${prc[2].content}
                                </span>
                            </div>
                        </g:each>
                    </div>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="field">
                <label for="mailto">${message(code: 'mail.to')}</label>
                <input type="text" name="mailto" id="mailto" value="${mailto}"/>
            </div>
        </g:else>

        <div class="field">
            <label for="mailcc">${message(code: 'mail.cc')}</label>
            <input type="text" name="mailcc" id="mailcc" readonly="readonly" value="${mailcc}"/>
        </div>

        <g:if test="${mailText}">
            <div class="content_lang_de">
                <div class="field">
                    <label for="mailSubject_de">${message(code: 'mail.subject')}</label>
                    <input id="mailSubject_de" name="mailSubject" readonly="readonly" value="${mailSubject['de']}" />
                </div>
                <div class="field">
                    <label for="mailText_de">${message(code: 'mail.body')}</label>
                    <g:textArea id="mailText_de" name="mailText" readonly="readonly" rows="30" cols="1">${mailText['de']}</g:textArea>
                </div>

                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.openMailClient('de')">
                    <i class="${Icon.SYM.EMAIL}"></i> ${message(code: 'mail.openExternalMailer')}
                </button>
                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.clipboardContent('de')">
                    <i class="${Icon.CMD.COPY}"></i> ${message(code: 'mail.copyToClipboard.content')}
                </button>
                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.clipboardRecipient()">
                    <i class="${Icon.CMD.COPY}"></i> ${message(code: 'mail.copyToClipboard.recipient')}
                </button>
            </div>
            <div class="content_lang_en hidden">
                <div class="field">
                    <label for="mailSubject_en">${message(code: 'mail.subject')}</label>
                    <input id="mailSubject_en" name="mailSubject" readonly="readonly" value="${mailSubject['en']}" />
                </div>
                <div class="field">
                    <label for="mailText_en">${message(code: 'mail.body')}</label>
                    <g:textArea id="mailText_en" name="mailText" readonly="readonly" rows="30" cols="1">${mailText['en']}</g:textArea>
                </div>

                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.openMailClient('en')">
                    <i class="${Icon.SYM.EMAIL}"></i> ${message(code: 'mail.openExternalMailer')}
                </button>
                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.clipboardContent('en')">
                    <i class="${Icon.CMD.COPY}"></i> ${message(code: 'mail.copyToClipboard.content')}
                </button>
                <button class="${Btn.ICON.SIMPLE} right floated" onclick="JSPC.infoFlyout.clipboardRecipient()">
                    <i class="${Icon.CMD.COPY}"></i> ${message(code: 'mail.copyToClipboard.recipient')}
                </button>

            </div>

        </g:if>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.infoFlyout = {
        openMailClient: function (lang) {
            let mailto = $('#infoFlyout #mailto').val();
            let mailcc = $('#infoFlyout #mailcc').val();
            let subject = $('#infoFlyout #mailSubject_' + lang).val();
            let body = $('#infoFlyout #mailText_' + lang).val();
            let href = 'mailto:' + mailto + '?subject=' + subject + '&cc=' + mailcc + '&body=' + body;

            window.location.href = encodeURI(href);
        },
        clipboardContent: function (lang) {
            let tt = $('#infoFlyout #mailText_' + lang).text();
            navigator.clipboard.writeText(tt);
        },
        clipboardRecipient: function () {
            let tt = $('#infoFlyout #mailto + i + .text > .text').text().trim();
            navigator.clipboard.writeText(tt);
        }
    }
</laser:script>