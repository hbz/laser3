<%@ page import="de.laser.Contact; de.laser.storage.RDConstants; de.laser.RefdataCategory;" %>


   <div id="contactFields" class="three fields">
       <div class="field four wide">
           <label></label>
            <laser:select class="ui dropdown" name="contentType.id"
                          from="${Contact.getAllRefdataValues(RDConstants.CONTACT_CONTENT_TYPE)}"
                          optionKey="id"
                          optionValue="value"
                          value="${contactInstance?.contentType?.id}"/>
        </div>

        <div class="field four wide">
            <label></label>
            <laser:select class="ui search dropdown" name="contactLang.id"
                          from="${RefdataCategory.getAllRefdataValues(RDConstants.LANGUAGE_ISO)}"
                          optionKey="id"
                          optionValue="value"
                          value="${contactInstance?.language?.id}"
                          noSelection="['null': message(code: 'person.contacts.selectLang.default')]"/>
        </div>


        <div class="field eight wide">
            <label></label>
            <g:textField id="content" name="content" value="${contactInstance?.content}"/>
        </div>
    </div>


