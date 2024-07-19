<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.Contact; de.laser.storage.RDConstants; de.laser.RefdataCategory;" %>


<div class="three fields contactField">
    <div class="field one wide la-contactIconField">
        <i class="${Icon.SYM.EMAIL} large la-js-contactIcon"></i>
    </div>
    <div class="field wide four">
        <ui:select class="ui dropdown contentType" name="contentType.id"
                   from="${[RDStore.CCT_EMAIL, RDStore.CCT_FAX, RDStore.CCT_MOBILE, RDStore.CCT_PHONE, RDStore.CCT_URL]}"
                   optionKey="id"
                   optionValue="value"
                   value="${contactInstance?.contentType?.id}"/>
    </div>

    <div class="field four wide">
        <ui:select class="ui search dropdown" name="contactLang.id"
                   from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)}"
                   optionKey="id"
                   optionValue="value"
                   value="${contactInstance?.language?.id}"
                   noSelection="['null': message(code: 'person.contacts.selectLang.default')]"/>
    </div>


    <div class="field seven wide">
        <g:textField class="la-js-contactContent" data-validate="contactContent" id="content" name="content" value="${contactInstance?.content}"/>
    </div>
    <div class="field one wide">
        <button type="button" class="${Btn.MODERN.NEGATIVE_ICON} removeContactElement">
            <i class="${Icon.CMD.DELETE}"></i>
        </button>
    </div>
</div>


