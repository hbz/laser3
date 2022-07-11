<g:form action="" controller="accessPoint" method="post" class="ui form">
    <div class="inline field">
        <div class="ui">%{-- add checkbox; js fix needed --}%

            <label for="activeCheckboxForPlatformList">${message(code: "accessPoint.linkedSubscription.statusCheckboxLabel")}</label>

            <% String jsHandler = ui.remoteJsOnChangeHandler(
                  controller: "accessPoint",
                  action: "dynamicPlatformList",
                  data: "{id:${accessPoint.id},checked:this.checked}",
                  update: "#platformTable"
            ) %>
            <input id="activeCheckboxForPlatformList" name="currentPlatforms"
                   type="checkbox" ${activeSubsOnly ? 'checked' : ''}
                   onchange="${jsHandler}" />

        </div>
    </div>
</g:form>

<laser:render template="linked_platforms_table"/>