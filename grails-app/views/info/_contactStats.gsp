<%@page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.storage.RDStore" %>

<h1 class="ui header">
    <g:message code="default.stats.contact.link"/>
</h1>

<div class="content">
    <div class="ui form">

        <g:render template="flyoutLanguageSelector" />

        <g:if test="${mailtoList}">
            <div class="field">
                <label for="mailto">${message(code: 'mail.to')}</label>
                <div class="ui fluid selection dropdown">
                    <input type="hidden" name="mailto" id="mailto">
                    <i class="dropdown icon"></i>
                    <div class="default text">Bitte auswählen</div>
                    <div class="menu">
                        <g:each in="${mailtoList}" var="mt">
                            <div class="item" data-value="${mt.content}">
                                <g:if test="${!mt.language}"><i class="flag x"></i></g:if>
                                <g:if test="${mt.language?.value == 'ger'}"><i class="flag de"></i></g:if>
                                <g:if test="${mt.language?.value == 'eng'}"><i class="flag gb"></i></g:if>
                                <span class="description">${mt.prs.IsPublic ? '' : ' (Mein Adressbuch)'}</span>
                                <span class="text">${mt.content}</span>
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

                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToClipboard('de')">
                    ${message(code: 'mail.copyToClipboard')}
                </button>
                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('de')">
                    ${message(code: 'mail.openExternalMailer')}
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

                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToClipboard('en')">
                    ${message(code: 'mail.copyToClipboard')}
                </button>
                <button class="${Btn.SIMPLE} right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('en')">
                    ${message(code: 'mail.openExternalMailer')}
                </button>
            </div>

        </g:if>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.infoFlyout = {
        copyToEmailProgram: function (lang) {
            let mailto = $('#infoFlyout #mailto').val();
            let mailcc = $('#infoFlyout #mailcc').val();
            let subject = $('#infoFlyout #mailSubject_' + lang).val();
            let body = $('#infoFlyout #mailText_' + lang).val()
            let href = 'mailto:' + mailto + '?subject=' + subject + '&cc=' + mailcc + '&body=' + body;
%{--            console.log(href);--}%
%{--            console.log(encodeURI(href));--}%

            window.location.href = encodeURI(href);
        },
        copyToClipboard: function (lang) {
            let content = $('#infoFlyout #mailText_' + lang);
            content.select();
            document.execCommand('copy');
            content.blur();
        }
    }
</laser:script>