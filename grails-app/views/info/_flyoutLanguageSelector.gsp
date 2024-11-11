<%@ page import="de.laser.storage.RDStore" %>

<div class="field">
    <label>${message(code: 'profile.language')}:</label>
    <div>
        <a href="#" class="ui icon button infoFlyout-language" data-lang="${RDStore.LANGUAGE_DE.value}">
            <i class="flag de"></i>
            %{--                    ${message(code: 'default.language.label')}--}%
        </a>
        <a href="#" class="ui icon button infoFlyout-language" data-lang="${RDStore.LANGUAGE_EN.value}">
            <i class="flag gb"></i>
            %{--                    ${message(code: 'default.english.label')}--}%
        </a>
    </div>
</div>