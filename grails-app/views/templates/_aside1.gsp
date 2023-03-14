<%@page import="de.laser.interfaces.CalculatedType; de.laser.*" %>
<laser:serviceInjection />

<%
    boolean parentAtChild = false

    if (ownobj instanceof Subscription) {
        //array is created and should be extended to collective view; not yet done because collective view is not merged yet
        if(contextOrg.id == ownobj.getConsortia()?.id && ownobj.instanceOf) {
            if(ownobj._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                parentAtChild = true
        }
    }
    else if (ownobj instanceof License) {
        if(institution.id == ownobj.getLicensingConsortium()?.id && ownobj.instanceOf) {
            parentAtChild = true
        }
    }
%>
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM_BASIC")}">
    <div id="container-tasks">
        <laser:render template="/templates/tasks/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}"  />
    </div>
</g:if>

<div id="container-documents">
    <laser:render template="/templates/documents/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>

<div id="container-notes">
    <laser:render template="/templates/notes/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'', parentAtChild: parentAtChild]}" />
</div>

<g:if test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
    <div id="container-workflows">
        <laser:render template="/templates/workflow/card" model="${[checklists: checklists, parentAtChild: parentAtChild]}" />
    </div>
</g:if>