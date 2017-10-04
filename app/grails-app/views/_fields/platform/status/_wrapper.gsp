<div class="control-group">
  <label class="control-label" for="status">${message(code:'platform.status', default:'Status')}</label>
  <div class="controls">
    <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Platform Status')}" var="refvalues"/>
    <g:select from="${refvalues}" optionKey="id" optionValue="value" noSelection="${['':(platformInstance.status?.value ?: 'Not Set')]}" name="status"  class="many-to-one"/>
  </div>
</div>
