<%@ page import="de.laser.IdentifierNamespace; de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:htmlStart message="myinst.subscriptionImport.pageTitle" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb message="menu.institutions.subscriptionImport" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.subscriptionImport" />

<ui:messages data="${flash}"/>

<div class="ui grid">
    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <p>
                <g:message code="myinst.subscriptionImport.manual.p1"/>
            </p>
            <p>
                <g:message code="myinst.subscriptionImport.manual.p2"/>
            <ol>
                <li><g:message code="myinst.subscriptionImport.manual.li1"/></li>
                <li><g:message code="myinst.subscriptionImport.manual.li2"/></li>
                <li><g:message code="myinst.subscriptionImport.manual.li3"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_01_03.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_03.png" src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_01_03.png')}"/></a></p></li>
                <li><g:message code="myinst.subscriptionImport.manual.li4"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_01_04.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_04.png" src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_01_04.png')}"/></a></p></li>
                <li><g:message code="myinst.subscriptionImport.manual.li5"/></li>
                <li><g:message code="myinst.subscriptionImport.manual.li6"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_06.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_06.png" src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_06.png')}"/></a></p></li>
                <li><g:message code="myinst.subscriptionImport.manual.li7"/></li>
                <li><g:message code="myinst.subscriptionImport.manual.li8"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_08.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_08.png" src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_08.png')}"/></a></p></li>
                <li><g:message code="myinst.subscriptionImport.manual.li9"/></li>
                <li><g:message code="myinst.subscriptionImport.manual.li10"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_10.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_10.png" src="${resource(dir: 'help', file: 'subscription/Abbildung_Punkt_01_10.png')}"/></a></p></li>
            </ol>
        </p>
            <p>
                <g:message code="myinst.subscriptionImport.manual.p3"/>
            <ol>
                <li><g:message code="myinst.subscriptionImport.manual.li11"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_02_01.png')}"><img class="ui small image" alt="Abbildung_Punkt_02_01.png" src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_02_01.png')}"/></a></p></li>
                <li><g:message code="myinst.subscriptionImport.manual.li12"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_02_02a.png')}"><img class="ui small image" alt="Abbildung_Punkt_02_02a.png" src="${resource(dir: 'help', file: 'finance/Abbildung_Punkt_02_02a.png')}"/></a></p></li>
            </ol>
            <g:message code="myinst.subscriptionImport.manual.p4"/>
        </p>
            <%
                String templatePath = 'LizenzImportVollnutzerBeispiel.csv'
                if(institution.isCustomerType_Consortium()) {
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
