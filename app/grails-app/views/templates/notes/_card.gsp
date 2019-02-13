<%@ page import="com.k_int.kbplus.Doc;com.k_int.kbplus.DocContext" %>
<div id="container-notes">

<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []

    ownobj.documents.sort{it.owner?.title}.each{ it ->
        if (it.sharedFrom) {
            sharedItems << it
        }
        else {
            baseItems << it
        }
    }
%>

<semui:card message="license.notes" class="notes la-js-hideable ${css_class}" href="#modalCreateNote" editable="${editable}">

        <g:each in="${baseItems}" var="docctx">
            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') )}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <!--<div class="event">-->

                            <div class="summary">
                                <g:if test="${docctx.owner.title}">
                                    <a onclick="noteedit(${docctx.owner.id});">${docctx.owner.title}</a>
                                </g:if>
                                <g:else>
                                    <a onclick="noteedit(${docctx.owner.id});">Ohne Titel</a>
                                </g:else>
                                <br/>

                                ${message(code:'template.notes.created')}
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>
                            </div>

                    <!--</div>-->
                </div>
            </g:if>
        </g:each>
</semui:card>

<g:if test="${sharedItems}">
    <semui:card text="Geteilte Dokumente" class="documents la-js-hideable ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">

            <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">

                    <div class="summary">
                        <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
                            <g:if test="${docctx.owner?.title}">
                                ${docctx.owner.title}
                            </g:if>
                            <g:elseif test="${docctx.owner?.filename}">
                                ${docctx.owner.filename}
                            </g:elseif>
                            <g:else>
                                ${message(code:'template.documents.missing', default: 'Missing title and filename')}
                            </g:else>

                        </g:link>(${docctx.owner.type.getI10n("value")})
                    </div>
                </div>
            </g:if>

        </g:each>
    </semui:card>
</g:if>

</div>

<r:script>
    function noteedit(id) {

        $.ajax({
            url: '<g:createLink controller="ajax" action="NoteEdit"/>?id='+id,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalEditNote").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }
</r:script>