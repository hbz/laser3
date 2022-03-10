<div id="pkg_details_modal" class="modal hide">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal">×</button>
    <h6 class="ui header">Hard Delete: ${pkg}</h6>
  </div>
  <div class="modal-body">
    <p> Items requiring action are marked with red circle. When these items are addressed, 'Confirm Delete' button will be enabled.</p>
    <table class="ui celled la-js-responsive-table la-table table">
      <thead>
      <tr>
        <th>Item</th>
        <th>Details</th>
        <th>Action</th>
      </tr>
      </thead>
      <tbody>
      <g:set var="actions_needed" value="false"/>
         <g:each in="${conflicts_list}" var="conflict_item">
            <tr>
              <td>
                ${conflict_item.name}
              </td>
              <td>
	              	<ul>
	              	<g:each in="${conflict_item.details}" var="detail_item">
				      	<li> 
                <g:if test="${detail_item.link}">
                  <a href="${detail_item.link}" target="_blank">${detail_item.text}</a>
                </g:if>
                <g:else>
                  ${detail_item.text}
                </g:else>
                </li>
	              	</g:each>
	              	</ul>
              </td>
              <td>
              %{-- Add some CSS based on actionRequired to show green/red status --}%
              <g:if test="${conflict_item.action.actionRequired}">
                  <i style="color:red" class="fa fa-times-circle"></i>
                  <g:set var="actions_needed" value="true"/>

              </g:if>
              <g:else>
                <i style="color:green" class="fa fa-check-circle"></i>
              </g:else>
                 ${conflict_item.action.text}				
              </td>
            </tr>
         </g:each>
      </tbody>
    </table>

  </div>
  <div class="modal-footer">
    <g:form action="performPackageDelete" id="${pkg.id}" onsubmit="return confirm('Deleting this package is PERMANENT. Delete package?')" method="POST">
    <g:if test="${actions_needed == 'true'}">
      <button type="submit" disabled="disabled" class="ui negative button">Confirm Delete</button>
    </g:if>
    <g:else>
      <button type="submit" class="ui negative button">Confirm Delete</button>
    </g:else>
    </g:form>
  </div>
</div>  
