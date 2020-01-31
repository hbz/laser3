<div class="control-group">
  <label class="control-label" for="status">${message(code:'default.status.label', default:'Status')}</label>
  <div class="controls">
    <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.PLATFORM_STATUS)}" var="refvalues"/>
    <g:select from="${refvalues}" optionKey="id" optionValue="${{it.getI10n('value')}}" noSelection="${['':(platformInstance.status?.value ?: 'Not Set')]}" name="status"  class="many-to-one"/>
  </div>
</div>
