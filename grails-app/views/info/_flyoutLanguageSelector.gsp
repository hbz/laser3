<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore" %>

<div class="field">
    <label>${message(code: 'profile.language')}:</label>
    <div id="infoFlyout-languageSelector">
        <button class="${Btn.ICON.SIMPLE}" data-lang="${RDStore.LANGUAGE_DE.value}">
            <i class="flag de"></i> ${message(code: 'default.language.label')}
        </button>
        <button class="${Btn.ICON.SIMPLE}" data-lang="${RDStore.LANGUAGE_EN.value}">
            <i class="flag gb"></i> ${message(code: 'default.english.label')}
        </button>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#infoFlyout-languageSelector > button').on ('click', function(e) {
        let lang = $(this).attr('data-lang');
        $('#infoFlyout .content_lang_de, #infoFlyout .content_lang_en').addClass('hidden');
        $('#infoFlyout .content_lang_' + lang).removeClass('hidden');
    });
</laser:script>