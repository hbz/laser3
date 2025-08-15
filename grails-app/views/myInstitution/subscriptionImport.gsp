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
            <div class="ui styled fluid accordion">
                <div class="title">
                    <i class="dropdown icon"></i>
                    <g:message code="myinst.subscriptionImport.csvManual.header"/>
                </div>
                <div class="content">
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
                        <ul>
                            <li><g:message code="myinst.subscriptionImport.manual.li5"/></li>
                        </ul>
                        <ol>
                            <li><g:message code="myinst.subscriptionImport.manual.li6"/></li>
                            <li><g:message code="myinst.subscriptionImport.manual.li6b"/></li>
                            <li><g:message code="myinst.subscriptionImport.manual.li7"/></li>
                        </ol>
                        <img class="ui image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_3.png')}"/>
                    </div>
                    <hr/>
                    <div>
                        <ol>
                            <li><g:message code="myinst.subscriptionImport.manual.li8"/></li>
                            <li><g:message code="myinst.subscriptionImport.manual.li9"/></li>
                        </ol>
                        <img class="ui  image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_4.png')}"/>
                    </div>
                    <hr/>
                    <div>
                        <ol>
                            <li><g:message code="myinst.subscriptionImport.manual.li10"/></li>
                            <li><g:message code="myinst.subscriptionImport.manual.li11"/></li>
                        </ol>
                        <img class="ui image" alt="subscription upload" src="${resource(dir: 'media', file: 'subscription/subscriptionupload_5.png')}"/>
                    </div>
                    <hr/>
                    <div>
                        <g:message code="myinst.subscriptionImport.manual.p3"/>
                        <ul>
                            <li><g:message code="myinst.subscriptionImport.manual.li12"/></li>
                        </ul>
                        <img class="ui image" alt="financeupload_6.png" src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"/>
                    </div>
                    <hr/>
                    <div>
                        <ul>
                            <li><g:message code="myinst.subscriptionImport.manual.li14"/></li>
                        </ul>
                        <img class="ui image" alt="financeupload_7.png" src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"/>
                        <g:message code="myinst.subscriptionImport.manual.p4"/>
                    </div>
                </div>
            </div>
        </div>

<%
    String templatePathCSV = 'LizenzImportVollnutzerBeispiel.csv', templatePathXLS = 'LizenzImportVollnutzerBeispiel.xlsx'
    if(contextService.getOrg().isCustomerType_Consortium()) {
        templatePathCSV = 'bulk_load_subscription_records_template.csv'
        templatePathXLS = 'bulk_load_subscription_records_template.xlsx'
    }
%>
<a href="${resource(dir: 'files', file: templatePathXLS)}" download="${templatePathXLS}" class="${Btn.ICON.SIMPLE} xls" style="margin-bottom: 1em" >
    <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.subscriptionImport.template.xls"/>
</a>
<a href="${resource(dir: 'files', file: templatePathCSV)}" download="${templatePathCSV}" class="${Btn.ICON.SIMPLE} csv" style="margin-bottom: 1em" >
    <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.subscriptionImport.template.csv"/>
</a>
<g:render template="/templates/genericFileImportForm" model="[processAction: 'processSubscriptionImport', fixedHeaderSetting: true]"/>

<g:render template="/public/markdownScript" />
<laser:htmlEnd />
