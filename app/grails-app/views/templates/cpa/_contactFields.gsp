<%@ page import="de.laser.Contact; de.laser.helper.RDConstants; com.k_int.kbplus.RefdataCategory;" %>


   <div id="contactFormModal" class="two fields">
       <div class="field three wide fieldcontain">
           <label></label>
            <laser:select class="ui dropdown" name="contentType.id"
                          from="${Contact.getAllRefdataValues(RDConstants.CONTACT_CONTENT_TYPE)}"
                          optionKey="id"
                          optionValue="value"
                          value="${contactInstance?.contentType?.id}"/>
        </div>

        <div class="field thirteen wide fieldcontain">
            <label></label>
            <g:textField id="content" name="content" value="${contactInstance?.content}"/>
        </div>
    </div>


