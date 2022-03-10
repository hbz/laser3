<g:if test="${orgSubjectGroups}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <% List availableSubjectGroupIds = availableSubjectGroups.collect{ it.id }%>
        <g:each in="${orgSubjectGroups}" var="subjectGroup">
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${subjectGroup.subjectGroup.getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <g:if test="${subjectGroup.subjectGroup.id in availableSubjectGroupIds}">
                        <div class="content la-space-right">
                            <div class="ui icon buttons">
                                <g:link class="ui negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.subjectgroup.organisation", args: [subjectGroup.subjectGroup.getI10n('value')])}"
                                        data-confirm-term-how="delete"
                                        controller="organisation" action="deleteSubjectGroup" params="[id: org.id, removeOrgSubjectGroup: subjectGroup.id]"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </div>
        </g:each>
    </div>
</g:if>