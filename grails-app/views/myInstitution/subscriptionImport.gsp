<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
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

            <div class="ui flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">
                <h1 class="ui header">
                    <g:message code="myinst.subscriptionImport.template.description"/>
                </h1>
                <div class="content">
                    <table class="ui la-ignore-fixed compact table">
                        <thead>
                        <tr>
                            <th><g:message code="myinst.subscriptionImport.tsvColumnName"/></th>
                            <th><g:message code="myinst.subscriptionImport.descriptionColumnName"/></th>
                            <th><g:message code="myinst.subscriptionImport.necessaryFormat"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${mappingCols}" var="mpg">
                            <%
                                List args = []
                                switch (mpg) {
                                    case 'status': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS).collect { it -> it.getI10n('value') })
                                        break
                                    case 'type': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND).collect { it -> it.getI10n('value') })
                                        break
                                    case 'form': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM).collect { it -> it.getI10n('value') })
                                        break
                                    case 'resource': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE).collect { it -> it.getI10n('value') })
                                        break
                                    case 'hasPerpetualAccess': args.addAll(RDStore.YN_YES.getI10n('value'), RDStore.YN_NO.getI10n('value'))
                                        break
                                    case 'hasPublishComponent': args.addAll(RDStore.YN_YES.getI10n('value'), RDStore.YN_NO.getI10n('value'))
                                        break
                                    case 'isAutomaticRenewAnnually': args.addAll(RDStore.YN_YES.getI10n('value'), RDStore.YN_NO.getI10n('value'))
                                        break
                                    case 'isPublicForApi': args.addAll(RDStore.YN_YES.getI10n('value'), RDStore.YN_NO.getI10n('value'))
                                        break
                                }
                            %>
                            <tr>
                                <td>${message(code: "myinst.subscriptionImport.${mpg}", args: args ?: '')}</td>
                                <td>${message(code: "myinst.subscriptionImport.description.${mpg}") ?: ''}</td>
                                <td>${message(code: "myinst.subscriptionImport.format.${mpg}", args: [raw("<ul><li>${args.join('</li><li>')}</li></ul>")]) ?: ''}</td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>


            <g:uploadForm action="processSubscriptionImport" method="post">
                <dl>
                    <div class="field">
                        <dt><g:message code="myinst.subscriptionImport.upload"/></dt>
                        <dd>
                            <input type="file" name="tsvFile"/>
                        </dd>
                    </div>
                    <button class="ui button" name="load" type="submit" value="Go"><g:message
                            code="myinst.subscriptionImport.upload"/></button>
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
