<%@page import="de.laser.interfaces.TemplateSupport; com.k_int.kbplus.*" %>
<%
    boolean parentAtChild = false

    if(ownobj instanceof Subscription) {
        //array is created and should be extended to collective view; not yet done because collective view is not merged yet
        if(contextService.org.id in [ownobj.getConsortia()?.id,ownobj.getCollective()?.id] && ownobj.instanceOf) {
            if(contextService.org.id == ownobj.getConsortia()?.id && ownobj.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE)
                parentAtChild = true
            else if(ownobj.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_PARTICIPATION)
                parentAtChild = true
        }
    }
    else if(ownobj instanceof License) {
        if(contextService.org.id == ownobj.getLicensingConsortium()?.id && ownobj.instanceOf) {
            parentAtChild = true
        }
    }
%>
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
    <g:render template="/templates/tasks/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}"  />
</g:if>
<div id="container-documents">
    <g:render template="/templates/documents/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>

<div id="container-notes">
    <g:render template="/templates/notes/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>
