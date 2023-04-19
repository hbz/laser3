<div class="inline field">
    <div class="ui">%{-- add checkbox; js fix needed --}%

        <label for="activeCheckbox">${message(code: "accessPoint.linkedSubscription.statusCheckboxLabel")}</label>

        <% String jsHandler = ui.remoteJsOnChangeHandler(
                controller: "accessPoint",
                action: "dynamicSubscriptionList",
                data: "{id:${accessPoint.id},checked:this.checked}",
                update: "#subPkgPlatformTable"
        ) %>
        <input id="activeCheckbox" name="currentLicences"
               type="checkbox" ${activeSubsOnly ? 'checked' : ''}
               onchange="${jsHandler}"/>

    </div>
</div>

<laser:render template="linked_subs_table"/>