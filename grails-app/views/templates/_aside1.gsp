<%@page import="de.laser.interfaces.CalculatedType; de.laser.*" %>
<%
    boolean parentAtChild = false

    if(ownobj instanceof Subscription) {
        //array is created and should be extended to collective view; not yet done because collective view is not merged yet
        if(contextOrg.id == ownobj.getConsortia()?.id && ownobj.instanceOf) {
            if(ownobj._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                parentAtChild = true
        }
    }
    else if(ownobj instanceof License) {
        if(institution.id == ownobj.getLicensingConsortium()?.id && ownobj.instanceOf) {
            parentAtChild = true
        }
    }
%>
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
    <div id="container-tasks">
        <g:render template="/templates/tasks/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}"  />
    </div>
</g:if>

<div id="container-documents">
    <g:render template="/templates/documents/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>

<div id="container-notes">
    <g:render template="/templates/notes/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>