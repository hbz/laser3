<g:if test="${orgSubjectGroups}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <% List availableSubjectGroupIds = availableSubjectGroups.collect{ it.id }%>
        <g:each in="${orgSubjectGroups.sort { it?.subjectGroup?.getI10n("value") }}" var="subjectGroup">
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${subjectGroup.subjectGroup?.getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <g:if test="${subjectGroup.subjectGroup.id in availableSubjectGroupIds}">
                        <div class="content la-space-right">
                            <div class="ui mini icon buttons">
                                <g:link class="ui negative button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.subjectgroup.organisation", args: [subjectGroup?.subjectGroup?.getI10n('value')])}"
                                        data-confirm-term-how="delete"
                                        controller="organisation" action="deleteSubjectGroup" params="[org: org.id, removeOrgSubjectGroup: subjectGroup.id]">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </div>
        </g:each>
    </div>
</g:if>