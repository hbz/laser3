<g:form action="" controller="accessPoint" method="post" class="ui form">
    <div class="inline field">
      <div class="ui checkbox">
        <label for="activeCheckbox">${message(code: "accessPoint.linkedSubscription.statusCheckboxLabel")}</label>
        <input id="activeCheckbox" autocomplete="off" name="currentLicences" type="checkbox" ${activeSubsOnly ? 'checked' : ''}
               onchange="${remoteFunction(
                   controller: 'accessPoint',
                   action: 'dynamicSubscriptionList',
                   params: "'id=' + ${accessPoint.id}+'&checked='+this.checked",
                   update: 'subPkgPlatformTable',
               )}">
      </div>
    </div>
  </g:form>
<g:render template="linked_subs_table"/>