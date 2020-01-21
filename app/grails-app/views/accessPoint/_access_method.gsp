
<div class="field required">
    <label>${message(code: 'accessMethod.label', default: 'Access Point Type')}</label>
    <laser:select class="ui dropdown values" id="accessMethod" name="accessMethod"
                  from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues(de.laser.helper.RDConstants.ACCESS_POINT_TYPE)}"
                  optionKey="value"
                  optionValue="value"
                  value="${accessMethod.value}"
                  onchange="${remoteFunction (
                          controller: 'accessPoint',
                          action: 'create',
                          params: "'template=' + this.value",
                          update: 'details',
                          onSuccess: '$(\'.ui.checkbox\').checkbox();'
                  )}"
    />
</div>

