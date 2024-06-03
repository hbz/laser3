<%@ page import="de.laser.Contact; de.laser.storage.RDConstants; de.laser.RefdataCategory;" %>


<div class="three fields contactField">
    <div class="field four wide">
        <label></label>
        <ui:select class="ui dropdown" name="contentType.id"
                   from="${Contact.getAllRefdataValues(RDConstants.CONTACT_CONTENT_TYPE)}"
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
        <button type="button"  class="ui icon negative button la-modern-button removeContactElement">
            <i class="trash alternate outline icon"></i>
        </button>
    </div>
</div>


