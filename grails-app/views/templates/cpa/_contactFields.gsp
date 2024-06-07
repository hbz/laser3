<%@ page import="de.laser.storage.RDStore; de.laser.Contact; de.laser.storage.RDConstants; de.laser.RefdataCategory;" %>


<div class="three fields contactField">
    <div class="field four wide">
        <label></label>
        <ui:select class="ui dropdown" name="contentType.id"
                   from="${[RDStore.CCT_EMAIL, RDStore.CCT_FAX, RDStore.CCT_MOBILE, RDStore.CCT_PHONE, RDStore.CCT_URL]}"
                   optionKey="id"
                   optionValue="value"
                   value="${contactInstance?.contentType?.id}"/>
    </div>

    <div class="field four wide">
        <label></label>
        <ui:select class="ui search multiple selection dropdown" name="contactLang.id"
                   from="${RefdataCategory.getAllRefdataValues(RDConstants.LANGUAGE_ISO)}"
                   optionKey="id"
                   optionValue="value"
                   value="${contactInstance?.language?.id}"
                   noSelection="['null': message(code: 'person.contacts.selectLang.default')]"/>
    </div>


    <div class="field seven wide">
        <label></label>
        <g:textField id="content" name="content" value="${contactInstance?.content}"/>
    </div>
    <div class="field one wide">
        <button type="button"  class="ui icon negative button la-modern-button la-margin-top-1-7em removeContactElement">
            <i class="trash alternate outline icon"></i>
        </button>
    </div>
</div>


