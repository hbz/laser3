<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="field required">
        <label for="${prefix}_title">Titel</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${condition?.title}" />
    </div>

    <div class="field">
        <label for="${prefix}_description">Beschreibung</label>
        <input type="text" name="${prefix}_description" id="${prefix}_description" value="${condition?.description}" />
    </div>

    <div class="fields two">
        <div class="field required">
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
            <div class="field required">
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
            <g:if test="${prefix == WfCondition.KEY}">
                <label for="${prefix}_parent">${message(code: 'workflow.object.' + WfTask.KEY)}  &uarr;</label> %{-- TODO --}%
            </g:if>
            <g:else>
                <label for="${prefix}_parent">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}  &uarr;</label> %{-- TODO --}%
            </g:else>
            <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${condition?.task?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
    </div>

    <g:if test="${condition?.getFields()}">
        <div class="ui segment" style="background-color: #d3dae3">
            <g:each in="${condition?.getFields()}" var="field">
                <g:if test="${field.startsWith('checkbox')}">
                    <div class="fields two">
                        <div class="field">
                            <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                            <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}">
                        </div>
                        <div class="field">
                            <label for="${prefix}_${field}_isTrigger">Trigger</label>
                            <div class="ui checkbox">
                                <input type="checkbox" name="${prefix}_${field}_isTrigger" id="${prefix}_${field}_isTrigger"
                                    <% if (condition?.getProperty(field + '_isTrigger')) { print 'checked="checked"' } %>
                                >
                                <label>Setzt Status des Tasks auf 'Erledigt'</label>
                            </div>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field.startsWith('date')}">
                    <div class="fields two">
                        <div class="field">
                            <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                            <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}">
                        </div>
                        <div class="field">
                        </div>
                    </div>
                </g:if>
            </g:each>
        </div>
    </g:if>

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