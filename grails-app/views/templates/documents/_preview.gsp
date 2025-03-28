<laser:serviceInjection/>

<ui:modal id="${modalId}" text="${modalTitle}" modalSize="large" hideSubmitButton="true">

    <g:if test="${docBase64}">
        <div style="margin-bottom:1em;">
            <embed src="data:${docDataType};base64,${docBase64}" width="100%" style="height:550px;border:1px solid #dedede;">
        </div>
    </g:if>

    <div>
        <g:if test="${info}">
            <ui:msg class="info" hideClose="true" text="${info}" />
        </g:if>
        <g:if test="${warning}">
            <ui:msg class="warning" hideClose="true" text="${warning}" />
        </g:if>
        <g:if test="${error}">
            <ui:msg class="error" hideClose="true" text="${error}" />
        </g:if>
    </div>

    <g:if test="${doc}">
        <div style="margin-top:1em;">
            <div class="ui list">
                <div class="item">
                    <strong>${message(code:'license.docs.table.fileName')}:</strong> ${doc.filename}
                    <g:if test="${doc.ckey}">
                        <span style="float:right;"><i class="icon award pink"></i>${message(code:'license.docs.table.encrypted')} </span>
                    </g:if>
                </div>
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
    </g:if>

</ui:modal>