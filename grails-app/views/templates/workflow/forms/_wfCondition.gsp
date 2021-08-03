<%@ page import="de.laser.helper.DateUtils; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

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

    <div class="field required">
        <label for="${prefix}_type">Typ</label>
        <g:select class="ui dropdown la-not-clearable" id="${prefix}_type" name="${prefix}_type"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${WfConditionBase.TYPES}"
                      value="${condition?.type}"
                      optionKey="${{ it }}"
                      optionValue="${{ RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + it).getI10n('value') }}" />
    </div>

    %{--<g:if test="${prefix == WfCondition.KEY}">

        <div class="field required">
            <label for="${prefix}_status">Status</label>
            <laser:select class="ui dropdown la-not-clearable" id="${prefix}_status" name="${prefix}_status"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                          value="${condition?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

    </g:if> --}%

    <g:if test="${condition?.getFields()}">
        <div class="ui blue segment" style="background-color: #f3f3f3">
            <g:each in="${condition.getFields()}" var="field">
                <g:if test="${field.startsWith('checkbox')}">
                    <div class="fields two">
                        <div class="field">
                            <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                            <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}" />
                        </div>
                        <div class="field">
                            <label for="${prefix}_${field}_isTrigger">Automatische Status-Änderung</label>
                            <div class="ui checkbox">
                                <input type="checkbox" name="${prefix}_${field}_isTrigger" id="${prefix}_${field}_isTrigger"
                                    <% if (condition?.getProperty(field + '_isTrigger')) { print 'checked="checked"' } %>
                                />
                                <label>${condition.getFieldLabel(field)}-Status ändert Aufgaben-Status</label>
                            </div>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field.startsWith('date')}">
                    <div class="fields two">
                        <div class="field">
                            <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                            <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}" />
                        </div>
                        <div class="field">
                        </div>
                    </div>
                </g:if>
            </g:each>
        </div>
        <div class="ui teal segment" style="background-color: #f3f3f3">
            <g:each in="${condition.getFields()}" var="field" status="fi">
                <g:if test="${fi == 0 || fi%2 == 0}">
                    <div class="field">
                        <div class="fields two">
                </g:if>

                <g:if test="${field.startsWith('checkbox')}">
                    <div class="field">
                        <label for="${prefix}_${field}">${condition.getProperty(field + '_title')}</label>
                        <div class="ui checkbox">
                            <input type="checkbox" name="${prefix}_${field}" id="${prefix}_${field}"
                                <% print condition.getProperty(field) == true ? 'checked="checked"' : '' %>
                            />
                            <g:if test="${condition.getProperty(field + '_isTrigger')}">
                                <label>Angabe ändert Aufgaben-Status</label>
                            </g:if>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field.startsWith('date')}">
                    <div class="field">
                        <label for="${prefix}_${field}">${condition.getProperty(field + '_title')}</label>
                        <input type="date" name="${prefix}_${field}" id="${prefix}_${field}"
                            <% print condition.getProperty(field) ? 'value="' + DateUtils.getSDF_ymd().format(condition.getProperty(field)) + '"' : '' %>
                        />
                    </div>
                </g:if>

                <g:if test="${fi + 1 == condition.getFields().size() || fi%2 == 1}">
                        </div>
                    </div>
                </g:if>
            </g:each>
        </div>
    </g:if>

    <g:if test="${prefix == WfConditionPrototype.KEY}">

        <div class="field">
            <label for="${prefix}_parent">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}  &uarr;</label>
            <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${condition?.task?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

    </g:if>

    <g:if test="${prefix == WfCondition.KEY}">

        <div class="field">
            <label for="${prefix}_parent">${message(code: 'workflow.object.' + WfTask.KEY)}  &uarr;</label>
            <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${condition?.task?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

        <div class="field">
            <label for="${prefix}_prototype">Prototyp</label>
            <g:select class="ui dropdown disabled" id="${prefix}_prototype" name="${prefix}_prototype"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_prototypeList}"
                      value="${condition?.prototype?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

    </g:if>

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${condition.id}" />
    </g:if>
    <g:else>
        <input type="hidden" name="cmd" value="${cmd}:${prefix}" />
    </g:else>

    <g:if test="${! tmplIsModal}">
        <div class="field">
            <button type="submit" class="ui button"><% if (prefix == WfConditionPrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
        </div>
        </div>
    </g:if>

</g:form>