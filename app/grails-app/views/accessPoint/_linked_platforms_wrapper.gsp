<g:form action="" controller="accessPoint" method="post" class="ui form">
  <div class="inline field">
    <div class="ui checkbox">
      <label for="activeCheckboxForPlatformList">${message(code: "accessPoint.linkedSubscription.statusCheckboxLabel")}</label>
      <input id="activeCheckboxForPlatformList" autocomplete="off" name="currentPlatforms" type="checkbox" ${activeSubsOnly ? 'checked' : ''}
             onchange="${remoteFunction(
                 controller: 'accessPoint',
                 action: 'dynamicPlatformList',
                 params: "'id=' + ${accessPoint.id}+'&checked='+this.checked",
                 update: 'platformTable',
             )}">
    </div>
  </div>
</g:form>
<g:render template="linked_platforms_table"/>