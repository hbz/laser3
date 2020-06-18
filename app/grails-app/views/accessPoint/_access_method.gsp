<div class="field required">
    <label>${message(code: 'accessMethod.label')}</label>
    <laser:select class="ui dropdown" id="accessMethod" name="accessMethod"
                  from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues(de.laser.helper.RDConstants.ACCESS_POINT_TYPE).sort{it.value}}"
                  optionKey="value"
                  optionValue="value"
                  value="${accessMethod}"
                  onchange="${remoteFunction (
                          controller: 'accessPoint',
                          action: 'create',
                          params: "'template=' + this.value",
                          update: 'details',
                  )}"
    />
</div>

