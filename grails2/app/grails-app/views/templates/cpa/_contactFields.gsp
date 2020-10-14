<%@ page import="de.laser.Contact; de.laser.helper.RDConstants; de.laser.RefdataCategory;" %>


   <div id="contactFields" class="three fields">
       <div class="field three wide fieldcontain">
           <label></label>
            <laser:select class="ui dropdown" name="contentType.id"
                          from="${Contact.getAllRefdataValues(RDConstants.CONTACT_CONTENT_TYPE)}"
                          optionKey="id"
                          optionValue="value"
                          value="${contactInstance?.contentType?.id}"/>
        </div>

        <div class="field one wide fieldcontain">

        </div>


        <div class="field twelve wide fieldcontain">
            <label></label>
            <g:textField id="content" name="content" value="${contactInstance?.content}"/>
        </div>
    </div>


