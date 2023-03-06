<%@ page import="de.laser.workflow.WfChecklist; de.laser.workflow.WfCheckpoint; de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.workflow.WorkflowHelper; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>
<laser:serviceInjection />

<g:set var="wfEditPerm" value="${workflowService.hasUserPerm_edit()}" />

<ui:modal id="wfModal" text="${tmplModalTitle}" msgSave="${message(code:'default.button.save')}" hideSubmitButton="${!wfEditPerm}">

<g:form name="wfForm" url="${tmplFormUrl}" method="POST" class="ui form" style="margin-bottom:0">

    %{-- CHECKLIST -- usage --}%
    %{-- CHECKLIST -- usage --}%

    <g:if test="${prefix == WfChecklist.KEY}">
        <g:set var="prefixOverride" value="${WfChecklist.KEY}" />
        <g:set var="wfInfo" value="${checklist.getInfo()}" />
        ${params}
        <br/>
        ${wfInfo}
    </g:if>

    %{-- CHECKPOINT -- usage --}%
    %{-- CHECKPOINT -- usage --}%

    <g:elseif test="${prefix == WfCheckpoint.KEY}">
        <g:set var="prefixOverride" value="${WfCheckpoint.KEY}" />
        <g:set var="wfInfo" value="${checkpoint.checklist.getInfo()}" />

    %{--        <g:if test="${checkpoint.description}">--}%
        <div class="field">
            <label for="cpDescription">Beschreibung</label>
            <p id="cpDescription">${checkpoint.description}</p>
        </div>
    %{--        </g:if>--}%

%{--        <div class="fields two">--}%
%{--            <div class="field">--}%
%{--                <label for="cpTitle">Aufgabe</label>--}%
%{--                <p id="cpTitle">${checkpoint.title}</p>--}%
%{--            </div>--}%
            <div class="field">
                <label for="clTitle">Workflow</label>
                <p id="clTitle">${checkpoint.checklist.title}</p>
            </div>
%{--        </div>--}%

        <div class="field">
            <label for="${prefixOverride}_comment">${message(code:'default.comment.label')}</label>
            <g:if test="${wfEditPerm}">
                <textarea id="${prefixOverride}_comment" name="${prefixOverride}_comment" rows="4">${checkpoint.comment}</textarea>
            </g:if>
            <g:else>
                <p id="${prefixOverride}_comment">${checkpoint.comment ?: '-'}</p>
            </g:else>
        </div>

        <div class="fields two">
            <div class="field">
                <g:set var="field" value="done" />
                <label for="${prefixOverride}_${field}">${message(code:'workflow.checkpoint.done')}</label>
                <div class="ui checkbox ${wfEditPerm ? '' : 'read-only'}">
                    <input type="checkbox" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                        <% print checkpoint.getProperty(field) == true ? 'checked="checked"' : '' %>
                    />
                </div>
            </div>

            <div class="field">
                <g:set var="field" value="date" />
                <label for="${prefixOverride}_${field}">${message(code:'workflow.checkpoint.date')}</label>
                <g:if test="${wfEditPerm}">
                    <ui:datepicker hideLabel="true" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                   value="${checkpoint.getProperty(field) ? DateUtils.getSDF_yyyyMMdd().format(checkpoint.getProperty(field)) : ''}">
                    </ui:datepicker>
                </g:if>
                <g:else>
                    <input type="text" id="${prefixOverride}_${field}" readonly="readonly" value="${checkpoint.getProperty(field) ? DateUtils.getLocalizedSDF_noTime().format(checkpoint.getProperty(field)) : ''}" />
                </g:else>
            </div>
        </div>

        <g:if test="${info}">
            <input type="hidden" name="info" value="${info}" />
        </g:if>
        <input type="hidden" name="cmd" value="modal:${prefix}:${checkpoint.id}" />
    </g:elseif>

</g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wfModal .wfModalLink').on('click', function(e) {
            e.preventDefault();
            $('#wfModal').modal('hide');
            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
            func();
        });
    </laser:script>

</ui:modal>


