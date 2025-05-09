<%@ page import="de.laser.IdentifierNamespace; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:htmlStart message="myinst.subscriptionImport.pageTitle" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb message="menu.institutions.subscriptionImport" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.subscriptionImport" />

<ui:messages data="${flash}"/>

<div class="ui segment la-markdown">
            <div>
                <g:message code="myinst.subscriptionImport.manual.p1"/>
                <img class="ui mini spaced image la-js-questionMark" alt="Abbildung_Fragezeichen_Icon.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Fragezeichen_Icon.png')}"/>
            </div>
            <div>
                <g:message code="myinst.subscriptionImport.manual.p2"/>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li1"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li2"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li3"/></li>
                </ul>
                <img class="ui image" alt="financeupload_1.png" src="${resource(dir: 'media', file: 'finance/financeupload_1.png')}"/>
            </div>
            <hr/>
            <div>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li4"/></li>
                </ul>
                <img class="ui image" alt="financeupload_2.png" src="${resource(dir: 'media', file: 'finance/financeupload_2.png')}"/>
            </div>
            <hr/>
            <div>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li5"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li6"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li7"/></li>
                </ol>
                <img class="ui image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_3.png')}"/>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li8"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li9"/></li>
                </ol>
                <img class="ui  image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_4.png')}"/>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li10"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li11"/></li>
                </ol>
                <img class="ui image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_5.png')}"/>
            </div>
            <hr/>
            <div>
                <g:message code="myinst.subscriptionImport.manual.p3"/>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li12"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li13"/></li>
                </ol>
                <img class="ui image" alt="financeupload_6.png" src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"/>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li14"/></li>
                </ul>
                <img class="ui image" alt="financeupload_7.png" src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"/>
                <g:message code="myinst.subscriptionImport.manual.p4"/>
            </div>
</div>
<%
    String templatePath = 'LizenzImportVollnutzerBeispiel.csv'
    if(contextService.getOrg().isCustomerType_Consortium()) {
        templatePath = 'bulk_load_subscription_records_template.csv'
    }
%>
<a href="${resource(dir: 'files', file: templatePath)}" download="template_bulk_load_subscription_records.csv" class="${Btn.ICON.SIMPLE}" style="margin-bottom: 1em" >
    <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.subscriptionImport.template"/>
</a>
<g:uploadForm action="processSubscriptionImport" method="post">
    <ui:msg class="warning" header="Achtung" text="" message="myinst.subscriptionImport.attention" showIcon="true" hideClose="true" />
    <div class="field">
        <div class="two fields">
                <div class="ui action input">
                    <input type="text" readonly="readonly" class="ui input"
                               placeholder="${message(code: 'myinst.subscriptionImport.uploadCSV')}">

                    <input type="file" name="tsvFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                           style="display: none;">
                    <div class="${Btn.ICON.SIMPLE}">
                        <i class="${Icon.CMD.ATTACHMENT}"></i>
                    </div>
                </div>

                <button class="${Btn.SIMPLE}" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.upload"/></button>
        </div>
    </div>
</g:uploadForm>


<g:render template="/public/markdownScript" />
<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</laser:script>
<laser:htmlEnd />
