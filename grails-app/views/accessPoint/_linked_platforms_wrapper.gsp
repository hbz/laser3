<div class="ui form">
    <div class="field">
        <h3><g:message code="default.filter.label"/>:</h3>
        <div class="ui segment">
            <div class="field">
                <div class="ui toggle checkbox">
                    <% String jsHandler = ui.remoteJsOnChangeHandler(
                            controller: "accessPoint",
                            action: "dynamicPlatformList",
                            data: "{id:${accessPoint.id},checked:this.checked}",
                            update: "#platformTable"
                    ) %>
                    <input id="activeCheckboxForPlatformList" name="currentPlatforms"
                           type="checkbox" ${activeSubsOnly ? 'checked' : ''}
                           onchange="${jsHandler}" />
                    <label for="activeCheckboxForPlatformList">${message(code: "accessPoint.linkedSubscription.statusCheckboxLabel")}</label>
                </div>
            </div>
        </div>
    </div>
</div>

<laser:render template="linked_platforms_table"/>