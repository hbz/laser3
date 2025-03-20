<%@ page import="de.laser.storage.RDStore; de.laser.utils.DateUtils; de.laser.ui.Icon; de.laser.Subscription" %>
<laser:serviceInjection />

<g:if test="${cts}">

    <div class="ui fluid card">
        <div class="ui top attached label">
            ${message(code: 'dashboard.card.currentTestSubscriptions')}: ${cts.size()}
        </div>
        <div class="content">
            <table class="ui unstackable scrolling compact table">
                <tbody style="max-height:145px">%{-- count:4 --}%
                    <g:each in="${cts}" var="ts">
                        <tr>
                            <td colspan="12">
                                <g:link controller="subscription" action="show" id="${ts.id}" target="_blank">
                                    <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>${ts}
                                </g:link>

                                <g:each in="${ts.packages}" var="sp">
                                    &nbsp;>&nbsp;
                                    <g:if test="${ts.packages.size() > 1}">
                                        <g:link controller="subscription" action="index" id="${sp.subscription.id}" params="${[pkgfilter: sp.pkg.id]}" target="_blank">
                                            <i class="${Icon.PACKAGE} la-list-icon"></i>${sp.pkg}
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <g:link controller="subscription" action="index" id="${sp.subscription.id}" target="_blank">
                                            <i class="${Icon.PACKAGE} la-list-icon"></i>${sp.pkg}
                                        </g:link>
                                    </g:else>
                                </g:each>
                            </td>
                            <td colspan="4" class="center aligned">
                                <g:if test="${ts.startDate || ts.endDate}">
                                    ${ts.startDate ? DateUtils.getLocalizedSDF_noTime().format(ts.startDate) : message(code: 'default.startDate.without')}
                                    -
                                    ${ts.endDate ? DateUtils.getLocalizedSDF_noTime().format(ts.endDate) : message(code: 'default.endDate.without')}
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