<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title><g:message code="laser"/> : <g:message code="myinst.subscriptionImport.pageTitle"/></title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <semui:crumb message="menu.institutions.subscriptionImport" class="active"/>
</semui:breadcrumbs>

<semui:headerWithIcon message="menu.institutions.subscriptionImport" />

<semui:messages data="${flash}"/>

<div class="ui grid">
    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <div class="header">${message(code: 'message.information')}</div>
                </div>

                <div class="content">
                    <g:message code="myinst.subscriptionImport.headline"/>
                    <br>
                    <br>
                    <a href="${resource(dir: 'files', file: 'LizenzImportVollnutzerBeispiel.csv')}"
                       download="template_bulk_load_subscription_records.csv">
                        <p><g:message code="myinst.subscriptionImport.template"/></p>
                    </a>
                </div>
            </div>


            <div class="ui card">
                <div class="content">
                    <div class="header">${message(code: 'myinst.subscriptionImport.template.description')}</div>
                </div>

                <div class="content">

                    <table class="ui celled striped table la-js-responsive-table la-table">
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

            <div class="ui card">
                <div class="content">
                    <div class="header">${message(code: 'myinst.subscriptionImport.upload')}</div>
                </div>

                <div class="content">

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
    </div>
</div>
</body>
</html>
