<%@ page import="com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'gasco.title')}</title>
</head>

<body>
    <br />
    <br />
    <br />
    <div class="ui grid">
        <div class="eleven wide column">
            <div class="ui la-gasco-search segment">
                <g:form action="gasco" controller="public" method="get" class="form-inline ui small form">

                    <div class="field">
                        <label>${message(code: 'gasco.filter.licenceOrProviderSearch')}</label>

                        <div class="ui input">
                            <input type="text" name="q"
                                   placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                                   value="${params.q?.encodeAsHTML()}"/>
                        </div>
                    </div>

                    <div class="field">
                        <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>

                        <fieldset id="subscritionType">
                            <div class="inline fields la-filter-inline">

                                <g:each in="${RefdataCategory.getAllRefdataValues('Subscription Type')}" var="subType">

                                    <g:if test="${subType.value != 'Local Licence'}">
                                        <g:if test="${subType.value == 'National Licence'}">
                                            <div class="inline field js-consortiallicence">
                                        </g:if>
                                        <g:else>
                                            <div class="inline field">
                                        </g:else>

                                            <div class="ui checkbox">
                                                <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                                <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                                    <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                                       tabindex="0">
                                            </div>
                                        </div>
                                    </g:if>
                                </g:each>

                            </div>
                        </fieldset>
                    </div>

                    <div class="field" id="js-consotial-authority">
                        <label>${message(code: 'gasco.filter.consotialAuthority')}</label>

                        <g:select from="${allConsortia}" class="ui fluid search selection dropdown"
                            optionKey="${{ "com.k_int.kbplus.Org:" + it.id }}"
                            optionValue="${{ it.getDesignation() }}"
                            name="consortia"
                            noSelection="[null: '']"
                            value="${params.consortia}"/>
                    </div>

                    <div class="field la-filter-search ">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label', default:'Search')}">
                    </div>

                </g:form>
            </div>
        </div>
        <div class="five wide column">
            <img class="ui fluid image" alt="Logo GASCO" class="ui fluid image" src="images/gasco/GASCO-Logo-2_klein.jpg"/>
        </div>
    </div>

    <r:script>
        $(document).ready(function() {

            function toggleFilterPart() {
                if ($('.js-consortiallicence input').prop('checked')) {
                    $('#js-consotial-authority .dropdown').addClass('disabled')
                    $('#js-consotial-authority select').attr('disabled', 'disabled')
                } else {
                    $('#js-consotial-authority .dropdown').removeClass('disabled')
                    $('#js-consotial-authority select').removeAttr('disabled')
                }
            }
            toggleFilterPart()
            $('.js-consortiallicence').on('click', toggleFilterPart)
        });
    </r:script>

    <g:if test="${subscriptions}">

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'gasco.table.product')}</th>
            <th>${message(code:'gasco.table.Ppovider')}</th>
            <th>${message(code:'gasco.licenceType')}</th>
            <th class="center aligned">${message(code:'gasco.table.consortium')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${subscriptions}" var="sub" status="i">
                <tr>
                    <td class="center aligned">
                        ${i}
                    </td>
                    <td>
                        ${sub}

                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="public" action="gascoDetails" id="${subPkg.id}">${subPkg.pkg}</g:link>
                            </div>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))}" var="role">
                            ${role.org?.name}
                        </g:each>
                    </td>
                    <td>
                        ${sub.type?.getI10n('value')}
                    </td>
                    <td>
                        ${sub.getConsortia()?.name}
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    </g:if>
<%--
        <g:each in="${test}" var="sub" status="i">
            <br />
            <br />
            <!-- subscription -->

            &nbsp; SUB: ${i} - ${sub}

            <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))}" var="role">
                <br />
                &nbsp;&nbsp;&nbsp; Anbieter: ${role.org?.name}
            </g:each>

            <br />
            &nbsp;&nbsp;&nbsp; Lizenztyp: ${sub.type?.getI10n('value')}

            <br />
            &nbsp;&nbsp;&nbsp; Konsortium: ${sub.getConsortia()?.name}

            <g:each in="${sub.packages}" var="subPkg" status="j">
                <br />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; PKG: ${j} - ${subPkg.pkg}
            </g:each>

            <!-- subscription -->
        </g:each>
--%>
</body>