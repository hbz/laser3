<%@ page import="com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
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
            <div class="ui la-search segment">
                <g:form action="gasco" controller="public" method="get" class="form-inline ui small form">

                    <div class="field">
                        <label>${message(code: 'gasco.filter.licenceOrProviderSearch')}</label>

                        <div class="ui input">
                            <input type="text" name="q"
                                   placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                                   value="${params.q}"/>
                        </div>
                    </div>
                    <div class="field">
                        <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>
                        <fieldset id="subscritionType">
                            <div class="inline fields la-filter-inline">

                                <g:each in="${RefdataCategory.getAllRefdataValues('Subscription Type')}" var="subType">
                                    <g:if test="${subType.value != 'Local Licence'}">
                                        <g:if test="${subType.value == 'National Licence'}">
                                            <div class="inline field js-nationallicence">
                                        </g:if>
                                        <g:elseif test="${subType.value == 'Alliance Licence'}">
                                            <div class="inline field js-alliancelicence">
                                        </g:elseif>
                                        <g:elseif test="${subType.value == 'Consortial Licence'}">
                                            <div class="inline field js-consortiallicence">
                                        </g:elseif>
                                        <g:else>
                                            <div class="inline field">
                                        </g:else>

                                            <div class="ui checkbox">
                                                <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                                <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                                    <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                                    <g:if test="${initQuery}"> checked="" </g:if>
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
                            optionValue="${{ it.getName() }}"
                            name="consortia"
                            noSelection="[null: '']"
                            value="${params.consortia}"/>
                    </div>

                    <div class="field la-field-right-aligned ">
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
                    $('#js-consotial-authority .dropdown').removeClass('disabled')
                    $('#js-consotial-authority select').removeAttr('disabled')
                } else {
                    $('#js-consotial-authority .dropdown').addClass('disabled')
                    $('#js-consotial-authority select').attr('disabled', 'disabled')
                }
            }
            function toggleTableHeading() {
                if ($('.js-nationallicence input').prop('checked') || $('.js-alliancelicence input').prop('checked')) {
                    $('#js-negotiator-header').show()
                    $('#js-consortium-header').hide()
                } else {
                    $('#js-negotiator-header').hide()
                    $('#js-consortium-header').show()
                }
            }
            toggleFilterPart()
            $('.js-nationallicence').on('click', toggleFilterPart)
            $('.js-alliancelicence').on('click', toggleFilterPart)
            $('.js-consortiallicence').on('click', toggleFilterPart)
            toggleTableHeading()
            $('.ui secondary button').on('click', toggleTableHeading)
        });
    </r:script>
    <g:if test="${subscriptions}">

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'gasco.table.product')}</th>
            <th>${message(code:'gasco.table.provider')}</th>
            %{--Task ERMS-587: Temporäres Ausblenden dieser Spalte--}%
            %{--<th>${message(code:'gasco.licenceType')}</th>--}%
            <th>
                <div id="js-consortium-header">
                    ${message(code:'gasco.table.consortium')}</div>
                <div id="js-negotiator-header">
                    ${message(code:'gasco.table.negotiator')}</div>
            </th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${subscriptions}" var="sub" status="i">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        <g:set var="anzeigeName" value="${sub.customProperties.find{ it.type == com.k_int.properties.PropertyDefinition.findByDescrAndName(PropertyDefinition.SUB_PROP, 'GASCO-Anzeigename')}?.stringValue}" />
                            ${anzeigeName ?: sub}

                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">${subPkg.pkg}</g:link>
                                %{--<g:link controller="public" action="gascoDetails" id="${subPkg.id}">${subPkg.pkg}</g:link>--}%
                            </div>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))}" var="role">
                            ${role.org?.name}<br>
                        </g:each>
                    </td>
                    %{--Task ERMS-587: Temporäres Ausblenden dieser Spalte--}%
                    %{--<td>--}%
                        %{--${sub.type?.getI10n('value')}--}%
                    %{--</td>--}%
                    <td class="la-break-all">

                    <g:set var="verhandlername" value="${sub.customProperties.find{ it.type == com.k_int.properties.PropertyDefinition.findByDescrAndName(PropertyDefinition.SUB_PROP, 'GASCO-Verhandlername')}?.stringValue}" />
                    ${verhandlername ?: sub.getConsortia()?.name}
                        <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RefdataValue.getByValueAndCategory('GASCO-Contact', 'Person Function'), sub.getConsortia())}" var="person">
                            <div class="ui list">
                                <div class="item">
                                    <div class="content">
                                        <div class="header">
                                            ${person?.getPrs()?.getFirst_name()} ${person?.getPrs()?.getLast_name()}
                                        </div>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person.getPrs(),
                                                RefdataValue.getByValueAndCategory('Url', 'ContactContentType')
                                        )}" var="prsContact">
                                            <div class="description">
                                                <i class="icon globe"></i>
                                                <span data-position="right center" data-tooltip="Diese URL aufrufen:  ${prsContact?.content}">
                                                    <a href="${prsContact?.content}" target="_blank">${prsContact?.content}</a>
                                                </span>

                                            </div>
                                        </g:each>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person.getPrs(),
                                                RefdataValue.getByValueAndCategory('E-Mail', 'ContactContentType')
                                        )}" var="prsContact">
                                            <div class="description">
                                                <i class="ui icon envelope outline"></i>
                                                <span data-position="right center" data-tooltip="Mail senden an ${person?.getPrs()?.getFirst_name()} ${person?.getPrs()?.getLast_name()}">
                                                    <a href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
                                                </span>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    </g:if>
</body>