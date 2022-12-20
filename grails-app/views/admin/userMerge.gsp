<laser:htmlStart text="Admin: User Merge" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index" />
        <ui:crumb text="User Merge" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="User Merge" type="admin"/>

    <ui:messages data="${flash}" />

<ui:form controller="admin" action="userMerge">

        <p>Select the user to keep, and the user whose rights will be transferred. When 'merge' is clicked,
        a confirmation screen with 'user to merge' current rights will be displayed.</p>

        <div class="control-group">
            <div class="field">
                <label for="userToKeep">User to Keep</label>
                <g:select name="userToKeep" from="${usersActive}" optionKey="id"
                    optionValue="${{it.displayName + ' ( ' + it.id +' )'}}" noSelection="${['null':'-Choose user to keep-']}" />
            </div>
            <div class="field">
                <label for="userToMerge">User to Merge</label>
                <g:select name="userToMerge" from="${usersAll}" optionKey="id"
                    optionValue="${{it.displayName + ' ( ' + it.id +' )'}}" noSelection="${['null':'-Choose user to merge-']}"/>
            </div>
            <div class="field">
                <input type="submit" value="Merge" class="ui button"/>
            </div>
        </div>
</ui:form>

  
  <div id="user_merge_modal" class="modal hide">
     
     <div class="modal-header">
       <button type="button" class="close" data-dismiss="modal">×</button>
       <h3 class="ui header">Merge ${userMerge?.displayName} (${userMerge?.id}) into ${userKeep?.displayName} (${userKeep?.id}) </h3>
     </div>
      <g:form action="userMerge" method="POST">
      
      <div class="modal-body">
          <input type="hidden" name="userToKeep" value="${params.userToKeep}"/>
          <input type="hidden" name="userToMerge" value="${params.userToMerge}"/>

          <p>Current Roles and Affiliations that will be copied to ${userKeep?.displayName}</p>

          <strong> User Roles </strong>
          <ul>
            <g:each in="${userRoles}" var="userRole">
              <li> ${userRole.authority}</li>
            </g:each>
          </ul>
          <strong> Affiliations </strong>

          <div style="height:300px;line-height:3em;overflow:auto;padding:5px;">
           <ul>
            <g:each in="${userAffiliations}" var="affil">
              <li> ${affil.org.name} :: ${affil.formalRole.authority}</li>
            </g:each>
          </ul>
          </div>
        </div>
        
        <div class="modal-footer">
        <input type="submit" id="mergeUsersBtn" value="Apply" class="ui button"/>
     </div>
        </g:form>
  </div>


  <g:if test="${userRoles}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#user_merge_modal').modal('show');
    </laser:script>
  </g:if>

<laser:htmlEnd />
