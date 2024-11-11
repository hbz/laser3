<%@ page import="de.laser.ui.Icon; de.laser.storage.RDStore" %>

<h1 class="ui header">
    ${message(code:'tipp.reportTitleToProvider.mailto')}
</h1>

<div class="content">
    <div class="ui form">

        <g:render template="flyoutLanguageSelector" />

        <div class="field">
            <label for="mailto">${message(code: 'mail.to')}</label>
            <input type="text" name="mailto" id="mailto" readonly="readonly" value="${mailto}"/>
        </div>

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

                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToClipboard('de')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
                </button>
                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('de')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
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

                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToClipboard('en')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_clipboard')}
                </button>
                <button class="ui icon button right floated" onclick="JSPC.infoFlyout.copyToEmailProgram('en')">
                    ${message(code: 'menu.institutions.copy_emailaddresses_to_emailclient')}
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

    $('a.infoFlyout-language').on ('click', function(e) {
        let lang = $(this).attr('data-lang');
        $('.content_lang_de, .content_lang_en').addClass('hidden');
        $('.content_lang_' + lang).removeClass('hidden');
    });
</laser:script>