<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="fields two">
        <div class="field">
            <label for="${prefix}_title">Titel</label>
            <input type="text" name="${prefix}_title" id="${prefix}_title" value="${condition?.title}" />
        </div>

        <div class="field">
            <label for="${prefix}_description">Beschreibung</label>
            <input type="text" name="${prefix}_description" id="${prefix}_description" value="${condition?.description}" />
        </div>
    </div>
    <div class="fields two">
        <div class="field">
            <label for="${prefix}_type">Typ</label>
            <g:select class="ui dropdown" id="${prefix}_type" name="${prefix}_type"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${WfConditionBase.TYPES}"
                          value="${condition?.type}"
                          optionKey="${{ it }}"
                          optionValue="${{ RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + it).getI10n('value') }}" />
        </div>
    </div>

    <g:if test="${prefix == WfCondition.KEY}">

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_status">Status</label>
                <laser:select class="ui dropdown" id="${prefix}_status" name="${prefix}_status"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                              value="${condition?.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>

            <div class="field">
                <label for="${prefix}_comment">Kommentar</label>
                <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${condition?.comment}" />
            </div>
        </div>

    </g:if>

    <div class="fields three">
        <div class="field">
            <label for="${prefix}_parent">Task &uarr;</label>
            <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${condition?.task?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
    </div>

    <input type="hidden" name="cmd" value="${cmd}:${prefix}" />

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="id" value="${condition?.id}" />
    </g:if>

    <g:if test="${! tmplIsModal}">
        <div class="field">
            <button type="submit" class="ui button"><% if (prefix == WfConditionPrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
        </div>
        </div>
    </g:if>

</g:form>