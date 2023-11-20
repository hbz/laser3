<ui:modal id="financeImportTemplate" hideSubmitButton="true">
    <p>
        <g:message code="myinst.financeImport.manual.p1a"/>
    </p>
    <g:link class="ui primary button" controller="myInstitution" action="generateFinanceImportWorksheet" id="${subscription.id}"><g:message code="myinst.financeImport.manual.p1button"/></g:link>
    <p>
        <g:message code="myinst.financeImport.manual.p1b" args="${[createLink(controller: "myInstitution", action: "financeImport", id:subscription.id)]}"/>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p2"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li1"/></li>
            <li><g:message code="myinst.financeImport.manual.li2"/></li>
            <li><g:message code="myinst.financeImport.manual.li3"/><p><a target="_blank" class="previewImage" href="/assets/manuals/Abbildung_Punkt_01_03.png"><g:img class="ui medium image" file="manuals/Abbildung_Punkt_01_03.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li4"/><p><a target="_blank" class="previewImage" href="/assets/manuals/Abbildung_Punkt_01_04.png"><g:img class="ui medium image" file="manuals/Abbildung_Punkt_01_04.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li5"/></li>
            <li><g:message code="myinst.financeImport.manual.li6"/><p><a target="_blank" class="previewImage" href="/assets/manuals/Abbildung_Punkt_01_06.png"><g:img class="ui medium image" file="manuals/Abbildung_Punkt_01_06.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li7"/></li>
            <li><g:message code="myinst.financeImport.manual.li8"/><p><a target="_blank" class="previewImage" href="/assets/manuals/Abbildung_Punkt_01_08.png"><g:img class="ui medium image" file="manuals/Abbildung_Punkt_01_08.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li9"/></li>
            <li><g:message code="myinst.financeImport.manual.li10"/><p><a target="_blank" class="previewImage" href="/assets/manuals/Abbildung_Punkt_01_10.png"><g:img class="ui medium image" file="manuals/Abbildung_Punkt_01_10.png"/></a></p></li>
        </ol>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p3a" args="${[createLink(controller: "myInstitution", action: "financeImport", id: subscription.id)]}"/>
    </p>
</ui:modal>