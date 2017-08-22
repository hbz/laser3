<div class="control-group">
  <label class="control-label" for="sector">${message(code:'platform.softwareProvider', default:'Software Provider')}</label>
  <div class="controls">
    <g:set value="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('YNO')}" var="refvalues"/>
    <g:select from="${refvalues}" optionKey="id" optionValue="value" name="softwareProvider" />
  </div>
</div>
