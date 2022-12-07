<laser:serviceInjection/>
<ui:modal id="document-preview-${doc.uuid}" text="${doc.title}" hideSubmitButton="true">

    <g:if test="${docBase64}">
        <div style="margin-bottom:1em;">
            <embed src="data:${docDataType};base64,${docBase64}" width="100%" style="min-height:550px;border:1px solid #dedede;">
        </div>
    </g:if>

    <div>
        <g:if test="${info}">
            <ui:msg class="info" icon="robot" noClose="true">
                ${info}
            </ui:msg>
        </g:if>
        <g:if test="${error}">
            <ui:msg class="error" icon="sad tear outline" noClose="true">
                ${error}
            </ui:msg>
        </g:if>
    </div>

    <div style="margin-top:1em;">
        <div class="ui list">
            <div class="item"> <strong>${message(code:'license.docs.table.fileName')}:</strong> ${doc.filename} </div>
            <g:if test="${doc.type}">
                <div class="item"> <strong>${message(code:'license.docs.table.type')}:</strong> ${doc.type.getI10n('value')} </div>
            </g:if>
%{--            <div class="item"> <strong>MIME-Typ:</strong> ${doc.mimeType} </div>--}%
            <g:if test="${doc.confidentiality}">
                <div class="item"> <strong>${message(code:'template.addDocument.confidentiality')}:</strong> ${doc.confidentiality.getI10n('value')} </div>
            </g:if>
            <div class="item"> <strong>${message(code:'license.docs.table.creator')}:</strong> <g:link controller="org" action="show" id="${doc.owner.id}">${doc.owner}</g:link> </div>
        </div>
    </div>

</ui:modal>