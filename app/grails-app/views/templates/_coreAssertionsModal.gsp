
<div name="coreAssertionEdit" class="modal hide">

  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">×</button>
    <h3>${message(code:'template.coreAssertionsModal.label', args:[tip?.title?.title], default:"Core Dates for ${tip?.title?.title}")}</h3>
  </div>

  <div class="modal-body">

    <g:if test="${message}">
      <bootstrap:alert class="alert-info">${message}</bootstrap:alert>
    </g:if>

    <p>${message(code:'template.coreAssertionsModal.note', default:'Edit existing core dates using the table below. Click the start and end dates to modify them and then the tick to accept your change. Once finished, click the Done button below')}</p>
    
    <table class="table table-bordered">
      <thead>
        <th>${message(code:'subscription.details.coreStartDate', default:'Core Start Date')}</th>
        <th>${message(code:'subscription.details.coreEndDate', default:'Core End Date')}</th>
        <th>${message(code:'default.action.label', default:'Action')}</th>
      </thead>
      <tbody>
         <g:each in="${coreDates}" var="coreDate">
            <tr>
              <td>
                <g:xEditable owner="${coreDate}" type="date" field="startDate" /> 
              </td>
              <td>
                <g:xEditable owner="${coreDate}" type="date" field="endDate" /> 
              </td>
              <td>
              <g:if test="${editable == 'true' || editable == true}">
                <g:remoteLink url="[controller: 'ajax', action: 'deleteCoreDate', params:[tipID:tipID,title:title,coreDateID:coreDate.id]]" method="get" name="show_core_assertion_modal" 
                before="hideModal()" onComplete="showCoreAssertionModal()" update="magicArea" class="delete-coreDate">${message(code:'default.button.delete.label', default:'Delete')} </g:remoteLink>
                </g:if>
              </td>
            </tr>
         </g:each>
      </tbody>
    </table>


    <div class="well" style="word-break: normal;">
      <h4>${message(code:'template.coreAssertionsModal.addDate', default:'Add new core date range')}</h4>
      <p>${message(code:'template.coreAssertionsModal.addDate.note', default:'Use this form to add new core date ranges. Set the start date and optionally an end date then click apply. If the dates you specify overlap with existing core dates in the table above they will be merged into a single core statement, otherwise a new line will be added to the table.')}</p>
      
      <g:formRemote  name="coreExtendForm" url="[controller: 'ajax', action: 'coreExtend']" before="hideModal()" onComplete="showCoreAssertionModal()" update="magicArea">
        <input type="hidden" name="tipID" value="${tipID}"/>
        <input type="hidden" name="title" value="${title}"/>
        <table style="width:100%">
          <tr>
            <td>
              <label class="property-label">${message(code:'subscription.details.coreStartDate', default:'Core Start Date')}:</label>
              <g:simpleHiddenValue  id="coreStartDate" name="coreStartDate" type="date"/>
            </td>
            <td>
             <label class="property-label">${message(code:'subscription.details.coreEndDate', default:'Core End Date')}:</label>
              <g:simpleHiddenValue id="coreEndDate" name="coreEndDate" type="date"/>
            </td>
            <td>
              <input type="submit" value="${message(code:'default.button.apply.label', default:'Apply')}" class="btn btn-primary btn-small pull-right"/>&nbsp;
            </td>
          </tr>
        </table>
      </g:formRemote>
    </div>

  </div>

  <div class="modal-footer">
    <button type="button" data-dismiss="modal">${message(code:'default.done', default:'Done')}</button>
  </div>

</div>

<g:if test="${editable=='true' || editable == true}">
  <script type="text/javascript">
    $('.xEditableValue').editable();
    $(".simpleHiddenRefdata").editable({
      url: function(params) {
        var hidden_field_id = $(this).data('hidden-id');
        $("#"+hidden_field_id).val(params.value);
        // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
      }
    });
  </script>
</g:if>


