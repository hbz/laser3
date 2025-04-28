<%@ page import="de.laser.IdentifierNamespace; de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:htmlStart message="myinst.subscriptionImport.pageTitle" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb message="menu.institutions.subscriptionImport" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.subscriptionImport" />

<ui:messages data="${flash}"/>

<div class="ui grid">
    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <div>
                <g:message code="myinst.subscriptionImport.manual.p1"/>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Fragezeichen_Icon.png')}"><img class="ui small image" alt="Abbildung_Fragezeichen_Icon.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Fragezeichen_Icon.png')}"/></a>
            </div>
            <div>
                <g:message code="myinst.subscriptionImport.manual.p2"/>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li1"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li2"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li3"/></li>
                </ul>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_1.png')}"><img class="ui small image" alt="financeupload_1.png" src="${resource(dir: 'media', file: 'finance/financeupload_1.png')}"/></a>
            </div>
            <hr/>
            <div>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li4"/></li>
                </ul>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_2.png')}"><img class="ui small image" alt="financeupload_2.png" src="${resource(dir: 'media', file: 'finance/financeupload_2.png')}"/></a>
            </div>
            <hr/>
            <div>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li5"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li6"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li7"/></li>
                </ol>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_06.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_06.png" src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_06.png')}"/></a>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li8"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li9"/></li>
                </ol>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_08.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_08.png" src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_08.png')}"/></a>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li10"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li11"/></li>
                </ol>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_10.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_10.png" src="${resource(dir: 'media', file: 'subscription/Abbildung_Punkt_01_10.png')}"/></a>
            </div>
            <hr/>
            <div>
                <g:message code="myinst.subscriptionImport.manual.p3"/>
                <ol>
                    <li><g:message code="myinst.subscriptionImport.manual.li12"/></li>
                    <li><g:message code="myinst.subscriptionImport.manual.li13"/></li>
                </ol>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"><img class="ui small image" alt="financeupload_6.png" src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"/></a>
                <ul>
                    <li><g:message code="myinst.subscriptionImport.manual.li14"/></li>
                </ul>
                <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"><img class="ui small image" alt="financeupload_7.png" src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"/></a>
                <g:message code="myinst.subscriptionImport.manual.p4"/>
            </div>
            <%
                String templatePath = 'LizenzImportVollnutzerBeispiel.csv'
                if(contextService.getOrg().isCustomerType_Consortium()) {
                    templatePath = 'bulk_load_subscription_records_template.csv'
                }
            %>
            <a href="${resource(dir: 'files', file: templatePath)}"
               download="template_bulk_load_subscription_records.csv">
                <p><g:message code="myinst.subscriptionImport.template"/></p>
            </a>

            <g:uploadForm action="processSubscriptionImport" method="post">
                <dl>
                    <div class="field">
                        <dt><g:message code="myinst.subscriptionImport.upload"/></dt>
                        <dd>
                            <input type="file" name="tsvFile"/>
                        </dd>
                    </div>
                    <button class="${Btn.SIMPLE}" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.upload"/></button>
                </dl>
            </g:uploadForm>
        </div>
    </div>
</div>
<ui:modal id="fullsizeImage" hideSubmitButton="true">
    <img class="ui image" src="#" alt="fullsize image"/>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.previewImage').click(function() {
        $('#fullsizeImage img').attr('src', $(this).attr('data-src'));
        $('#fullsizeImage').modal('show');
    });
</laser:script>
<laser:htmlEnd />
