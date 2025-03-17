<%@ page import="de.laser.storage.RDStore; de.laser.utils.DateUtils; de.laser.ui.Icon; de.laser.Subscription" %>
<laser:serviceInjection />

<g:if test="${cts}">

    <div class="ui fluid card">
        <div class="ui top attached label">
            Aktuelle Testlizenzen: ${cts.size()}
        </div>
        <div class="content">
            <table class="ui unstackable scrolling compact table">
                <tbody style="max-height:290px">
                    <g:each in="${cts}" var="ts">
                        <tr>
                            <td colspan="12">
                                <g:link controller="subscription" action="show" id="${ts.id}" target="_blank">
                                    <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>${ts}
                                </g:link>

                                <g:each in="${ts.packages}" var="sp">
                                    &nbsp;-&nbsp;
                                    <g:link controller="package" action="show" id="${sp.pkg.id}" target="_blank">
                                        <i class="${Icon.PACKAGE} la-list-icon"></i>${sp.pkg}
                                    </g:link>
                                </g:each>

%{--                                <g:if test="${ts.provider}">--}%
%{--                                    <g:link controller="provider" action="show" id="${ts.provider.id}" target="_blank">--}%
%{--                                        <i class="${Icon.PROVIDER} la-list-icon"></i> ${ts.provider}--}%
%{--                                    </g:link>--}%
%{--                                </g:if>--}%
                            </td>
                            <td colspan="4" style="text-align:center">
                                <g:if test="${ts.startDate || ts.endDate}">
                                    ${ts.startDate ? DateUtils.getLocalizedSDF_noTime().format(ts.startDate) : 'ohne Startdatum'}
                                    -
                                    ${ts.endDate ? DateUtils.getLocalizedSDF_noTime().format(ts.endDate) : 'ohne Enddatum'}
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <div class="extra content">
            <div class="right floated">
                <g:link controller="myInstitution" action="currentSubscriptions" params="${[status: RDStore.SUBSCRIPTION_TEST_ACCESS.id]}">
                    ${message(code:'menu.my.subscriptions')}
                </g:link>
            </div>
        </div>
    </div>

</g:if>