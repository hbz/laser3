<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Contact; com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition;de.laser.helper.RDConstants;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser')} : ${message(code: 'gasco.title')}</title>
</head>

<body>
    <br>
    <div class="ui grid">
        <div class="eleven wide column">
            <div class="ui la-search segment">
                <g:form action="gasco" controller="public" method="get" class="ui small form">

                    <div class="field">
                        <label for="search">${message(code: 'default.search.label')}</label>

                        <div class="ui input">
                            <input type="text" id="search" name="q"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.q}"/>
                        </div>
                    </div>
                    <div class="field">
                        <fieldset id="subscritionKind">
                            <legend>${message(code: 'myinst.currentSubscriptions.subscription_kind')}</legend>
                            <div class="inline fields la-filter-inline">

                                <%
                                    List subkinds = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)
                                    subkinds -= RDStore.SUBSCRIPTION_KIND_LOCAL
                                %>

                                <g:each in="${subkinds}" var="subKind">

                                        <g:if test="${subKind.value == 'National Licence'}">
                                            <div class="inline field js-nationallicence">
                                        </g:if>
                                        <g:elseif test="${subKind.value == 'Alliance Licence'}">
                                            <div class="inline field js-alliancelicence">
                                        </g:elseif>
                                        <g:elseif test="${subKind.value == 'Consortial Licence'}">
                                            <div class="inline field js-consortiallicence">
                                        </g:elseif>
                                        <g:else>
                                            <div class="inline field">
                                        </g:else>

                                            <div class="ui checkbox">
                                                <label for="checkSubType-${subKind.id}">${subKind.getI10n('value')}</label>
                                                <input id="checkSubType-${subKind.id}" name="subKinds" type="checkbox" value="${subKind.id}"
                                                    <g:if test="${params.list('subKinds').contains(subKind.id.toString())}"> checked="" </g:if>
                                                    <g:if test="${initQuery}"> checked="" </g:if>
                                                       tabindex="0">

                                            </div>
                                        </div>

                                </g:each>

                            </div>
                        </fieldset>
                    </div>
                    <div class="field" id="js-consotial-authority">
                        <fieldset>
                            <legend id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</legend>

                            <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                optionKey="${{ "com.k_int.kbplus.Org:" + it.id }}"
                                optionValue="${{ it.getName() }}"
                                name="consortia"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                value="${params.consortia}"/>
                        </fieldset>

                    </div>

                    <div class="field la-field-right-aligned ">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label')}">
                    </div>

                </g:form>
            </div>
        </div>
        <div class="five wide column">
            <img class="ui fluid image" alt="Logo GASCO" src="images/gasco/GASCO-Logo-2_klein.jpg"/>
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
        <g:set var="GASCO_INFORMATION_LINK" value="${PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP)}" />
        <g:set var="GASCO_ANZEIGENAME" value="${PropertyDefinition.getByNameAndDescr('GASCO display name', PropertyDefinition.SUB_PROP)}" />
        <g:set var="GASCO_VERHANDLERNAME" value="${PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP)}" />
            <g:each in="${subscriptions}" var="sub" status="i">
                <g:set var="gasco_infolink" value="${sub.customProperties.find{ it.type == GASCO_INFORMATION_LINK}?.urlValue}" />
                <g:set var="gasco_anzeigename" value="${sub.customProperties.find{ it.type == GASCO_ANZEIGENAME}?.stringValue}" />
                <g:set var="gasco_verhandlername" value="${sub.customProperties.find{ it.type == GASCO_VERHANDLERNAME}?.stringValue}" />
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>

                        <g:if test="${gasco_infolink}">
                            <span  class="la-popup-tooltip la-delay" data-position="right center" data-content="Diese URL aufrufen:  ${gasco_infolink}">
                                <a href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                            </span>
                        </g:if>
                        <g:else>
                            ${gasco_anzeigename ?: sub}
                        </g:else>

                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">${subPkg.pkg}</g:link>
                                %{--<g:link controller="public" action="gascoDetails" id="${subPkg.id}">${subPkg.pkg}</g:link>--}%
                            </div>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RDStore.OR_PROVIDER)}" var="role">
                            ${role.org?.name}<br>
                        </g:each>
                    </td>
                    %{--Task ERMS-587: Temporäres Ausblenden dieser Spalte--}%
                    %{--<td>--}%
                        %{--${sub.type?.getI10n('value')}--}%
                    %{--</td>--}%
                    <td class="la-break-all">

                    ${gasco_verhandlername ?: sub.getConsortia()?.name}
                    <br>
                    <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, sub.getConsortia())}" var="personRole">
                        <g:set var="person" value="${personRole.getPrs()}" />
                        <g:if test="${person.isPublic}">
                            <div class="ui list">
                                <div class="item">
                                    <div class="content">
                                        <div class="header">
                                            ${person?.getFirst_name()} ${person?.getLast_name()}
                                        </div>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person,
                                                RDStore.CCT_URL
                                        )}" var="prsContact">
                                            <div class="description">
                                                <i class="icon globe"></i>
                                                <span  class="la-popup-tooltip la-delay" data-position="right center" data-content="Diese URL aufrufen:  ${prsContact?.content}">
                                                    <a href="${prsContact?.content}" target="_blank">${prsContact?.content}</a>
                                                </span>

                                            </div>
                                        </g:each>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person,
                                                RDStore.CCT_EMAIL
                                        )}" var="prsContact">
                                            <div class="description">
                                                <i class="ui icon envelope outline"></i>
                                                <span  class="la-popup-tooltip la-delay" data-position="right center " data-content="Mail senden an ${person?.getFirst_name()} ${person?.getLast_name()}">
                                                    <a href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
                                                </span>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    </g:if>
<r:style>
.ui.table thead tr:first-child>th {
    top: 48px!important;
}
</r:style>
<sec:ifAnyGranted roles="ROLE_USER">
    <r:style>
        .ui.table thead tr:first-child>th {
            top: 90px!important;
        }
    </r:style>
</sec:ifAnyGranted>
</body>