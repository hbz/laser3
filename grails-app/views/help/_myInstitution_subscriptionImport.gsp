<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.IdentifierNamespace" %>

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
                        case 'identifiers': args.addAll(IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_SUBSCRIPTION).collect { IdentifierNamespace idns -> idns.getI10n('name') ? idns.getI10n('name') : idns.ns }.sort{ String val -> val.toLowerCase() })
                            break
                    }
                %>
                <tr>
                    <td>${message(code: "myinst.subscriptionImport.${mpg}", args: args ?: '')}</td>
                    <td>${message(code: "myinst.subscriptionImport.description.${mpg}") ?: ''}</td>
                    <td>
                        ${message(code: "myinst.subscriptionImport.format.${mpg}")}
                        <g:if test="${args}">
                            <ul>
                                <g:each in="${args}" var="arg">
                                    <li>${arg}</li>
                                </g:each>
                            </ul>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
