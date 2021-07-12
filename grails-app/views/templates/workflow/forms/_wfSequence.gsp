<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="fields two">
        <div class="field">
            <label for="${prefix}_title">Titel</label>
            <input type="text" name="${prefix}_title" id="${prefix}_title" value="${sequence?.title}" />
        </div>

        <div class="field">
            <label for="${prefix}_description">Beschreibung</label>
            <input type="text" name="${prefix}_description" id="${prefix}_description" value="${sequence?.description}" />
        </div>
    </div>

    <div class="fields two">
        <div class="field">
            <label for="${prefix}_type">Typ</label>
            <laser:select class="ui dropdown" id="${prefix}_type" name="${prefix}_type"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WORKFLOW_SEQUENCE_TYPE )}"
                          value="${sequence?.type?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>
    </div>

    <g:if test="${prefix == WfSequence.KEY}">

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_status">Status</label>
                <laser:select class="ui dropdown" id="${prefix}_status" name="${prefix}_status"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WORKFLOW_SEQUENCE_STATUS )}"
                              value="${sequence?.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>

            <div class="field">
                <label for="${prefix}_comment">Kommentar</label>
                <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${sequence?.comment}" />
            </div>
        </div>

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_prototype">Prototyp</label>
                <g:select class="ui dropdown" id="${prefix}_prototype" name="${prefix}_prototype"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_prototypeList}"
                              value="${sequence?.prototype?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
            </div>

            <div class="field">
                <label for="${prefix}_subscription">Subscription</label>
                <g:select class="ui dropdown" id="${prefix}_subscription" name="${prefix}_subscription"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_subscriptionList}"
                              value="${sequence?.subscription?.id}"
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
                          value="${sequence?.child?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
    </div>

    <input type="hidden" name="cmd" value="${cmd}:${prefix}" />

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="id" value="${sequence?.id}" />
    </g:if>

    <g:if test="${! tmplIsModal}">
        <div class="field">
            <button type="submit" class="ui button"><% if (prefix == WfSequencePrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
        </div>
        </div>
    </g:if>

</g:form>