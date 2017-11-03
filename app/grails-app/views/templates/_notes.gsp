<laser:card title="license.notes" class="card-grey notes">
<%-- <h5>Notes</h5> --%>
  <ul>
    <g:each in="${ownobj.documents}" var="docctx">
      <g:if test="${((docctx.owner?.contentType==0) && !(docctx.domain) && (docctx.status?.value!='Deleted') )}">
        <li>
          <g:xEditable owner="${docctx.owner}" field="content"/><br/>
          <i>${message(code:'template.notes.created')} <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>
          <g:if test="${docctx.alert}">
            ${message(code:'template.notes.shared')} ${docctx.alert.createdBy.displayName}
            <g:if test="${docctx.alert.sharingLevel==1}">${message(code:'template.notes.shared_jc')}</g:if>
            <g:if test="${docctx.alert.sharingLevel==2}">${message(code:'template.notes.shared_community')}</g:if>
            <div class="comments"><a href="#modalComments" class="announce" data-id="${docctx.alert.id}">${docctx.alert?.comments != null ? docctx.alert?.comments?.size() : 0} Comment(s)</a></div>
          </g:if>
          <g:else>${message(code:'template.notes.not_shared')}</g:else></i>
        </li>
      </g:if>
    </g:each>
  </ul>
  <g:if test="${editable}">
    <input type="submit" class="ui primary button" value="${message(code:'license.addNewNote', default:'Add New Note')}" data-toggle="modal" href="#modalCreateNote" />
<%-- <input type="submit" class="ui primary button" value="Add new note" data-toggle="modal" href="#modalCreateNote" /> --%>
  </g:if>
</laser:card>

<g:render template="/templates/addNote" />

<div class="modal hide fade" id="modalComments">
</div>

