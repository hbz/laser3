<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="fields two">
        <div class="field">
            <label for="${prefix}_title">Titel</label>
            <input type="text" name="${prefix}_title" id="${prefix}_title" value="${workflow?.title}" />
        </div>

        <div class="field">
            <label for="${prefix}_description">Beschreibung</label>
            <input type="text" name="${prefix}_description" id="${prefix}_description" value="${workflow?.description}" />
        </div>
    </div>

    <div class="fields two">
        <div class="field">
            <label for="${prefix}_type">Zustand</label>
            <laser:select class="ui dropdown" id="${prefix}_state" name="${prefix}_state"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATE )}"
                          value="${workflow?.state?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>
    </div>

    <g:if test="${prefix == WfWorkflow.KEY}">

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_status">Status</label>
                <laser:select class="ui dropdown" id="${prefix}_status" name="${prefix}_status"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                              value="${workflow?.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>

            <div class="field">
                <label for="${prefix}_comment">Kommentar</label>
                <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${workflow?.comment}" />
            </div>
        </div>

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_prototype">Prototyp</label>
                <g:select class="ui dropdown" id="${prefix}_prototype" name="${prefix}_prototype"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_prototypeList}"
                              value="${workflow?.prototype?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
            </div>

            <div class="field">
                <label for="${prefix}_subscription">Subscription</label>
                <g:select class="ui dropdown" id="${prefix}_subscription" name="${prefix}_subscription"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_subscriptionList}"
                              value="${workflow?.subscription?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.name}}" />
            </div>
        </div>

    </g:if>

    <div class="fields three">
        <div class="field">
            <label for="${prefix}_child">Child &darr;</label>
            <g:select class="ui dropdown" id="${prefix}_child" name="${prefix}_child"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_childList}"
                          value="${workflow?.child?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
    </div>

    <input type="hidden" name="cmd" value="${cmd}:${prefix}" />

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="id" value="${workflow?.id}" />
    </g:if>

    <g:if test="${! tmplIsModal}">
        <div class="field">
            <button type="submit" class="ui button"><% if (prefix == WfWorkflowPrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
        </div>
        </div>
    </g:if>

</g:form>