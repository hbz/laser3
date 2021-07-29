<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="field required">
        <label for="${prefix}_title">Titel</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${task?.title}" />
    </div>

    <div class="field">
        <label for="${prefix}_description">Beschreibung</label>
        <input type="text" name="${prefix}_description" id="${prefix}_description" value="${task?.description}" />
    </div>

    <div class="field required">
        <label for="${prefix}_priority">Priorität</label>
        <laser:select class="ui dropdown" id="${prefix}_priority" name="${prefix}_priority"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_PRIORITY )}"
                      value="${task?.priority?.id}"
                      optionKey="id"
                      optionValue="value" />
    </div>

    <g:if test="${prefix == WfTaskPrototype.KEY}">

        <div class="field">
            <label for="${prefix}_condition">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}</label>
            <g:select class="ui dropdown" id="${prefix}_condition" name="${prefix}_condition"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_conditionList}"
                      value="${task?.condition?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

        <div class="field">
            <div class="fields two">
                <div class="field">
                    <label for="${prefix}_next">Nachfolger &rarr;</label>
                    <g:select class="ui dropdown" id="${prefix}_next" name="${prefix}_next"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_nextList}"
                              value="${task?.next?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
                <div class="field">
                    <label for="${prefix}_child">Kind &darr;</label>
                    <g:select class="ui dropdown" id="${prefix}_child" name="${prefix}_child"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_childList}"
                              value="${task?.child?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
            </div>
        </div>

    </g:if>

%{-- <div class="field">
<label for="${prefix}_type">Typ</label>
<laser:select class="ui dropdown" id="${prefix}_type" name="${prefix}_type"
              noSelection="${['' : message(code:'default.select.choose.label')]}"
              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_TYPE )}"
              value="${task?.type?.id}"
              optionKey="id"
              optionValue="value" />
</div> --}%

    <g:if test="${prefix == WfTask.KEY}">

        <div class="field required">
            <label for="${prefix}_status">Status</label>
            <laser:select class="ui dropdown" id="${prefix}_status" name="${prefix}_status"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_STATUS )}"
                          value="${task?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        <div class="field">
            <label for="${prefix}_comment">Kommentar</label>
            <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${task?.comment}" />
        </div>

        <div class="field">
            <label for="${prefix}_condition">${message(code: 'workflow.object.' + WfCondition.KEY)}</label>
            <g:select class="ui dropdown disabled" id="${prefix}_condition" name="${prefix}_condition"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_conditionList}"
                      value="${task?.condition?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

        <div class="field">
            <div class="fields two">
                <div class="field">
                    <label for="${prefix}_next">Nachfolger &rarr;</label>
                    <g:select class="ui dropdown disabled" id="${prefix}_next" name="${prefix}_next"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_nextList}"
                              value="${task?.next?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
                <div class="field">
                    <label for="${prefix}_child">Kind &darr;</label>
                    <g:select class="ui dropdown disabled" id="${prefix}_child" name="${prefix}_child"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_childList}"
                              value="${task?.child?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
            </div>
        </div>

    </g:if>

    <div class="field">
        <div class="fields two">
            <div class="field">
                <label for="${prefix}_previous">Vorgänger &larr;</label> %{-- TODO --}%
                <g:select class="ui dropdown disabled" id="${prefix}_previous" name="${prefix}_previous"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_previousList}"
                          value="${task?.getPrevious()?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
            </div>
            <div class="field">
                <label for="${prefix}_parent">Super &uarr;</label> %{-- TODO --}%
                <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_parentList}"
                          value="${task?.getParent()?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
            </div>
        </div>
    </div>

    <g:if test="${prefix == WfTask.KEY}">

        <div class="fields two">
            <div class="field">
                <label for="${prefix}_prototype">Prototyp</label>
                <g:select class="ui dropdown disabled" id="${prefix}_prototype" name="${prefix}_prototype"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_prototypeList}"
                          value="${task?.prototype?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
            </div>
        </div>

    </g:if>

    <input type="hidden" name="cmd" value="${cmd}:${prefix}" />

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="id" value="${task?.id}" />
    </g:if>

    <g:if test="${! tmplIsModal}">
            <div class="field">
                <button type="submit" class="ui button"><% if (prefix == WfTaskPrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
            </div>
        </div>
    </g:if>

</g:form>