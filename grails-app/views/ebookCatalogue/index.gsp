<%@ page import="de.laser.storage.PropertyStore; de.laser.Org; de.laser.PersonRole; de.laser.OrgRole; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Contact; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="eBookCatalogue">
</head>

<body>
    <g:if test="${initQuery}">
        <g:set var="status" value="is-bold" />
    </g:if>
    <g:else>
        <g:if test="${subscriptionsCount == 0}">
            <g:set var="status" value="is-warning" />
        </g:if>
        <g:else>
            <g:set var="status" value="is-success" />
        </g:else>
    </g:else>

<section class="section custom-section-form pt-6 pb-6">

    <div class="container">
        <g:form method="get" autocomplete="off">

            <div class="field">
                <div class="control is-expanded">
                    <input id="q" name="q" value="${params.q}"
                           class="input is-medium" autofocus="autofocus" onfocus="this.select()"
                           type="search" placeholder="Suche nach EBooks .." />
                </div>
            </div>

            <div class="field">
                <label class="label">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <%
                    List<RefdataValue> subkinds = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)
                    subkinds -= RDStore.SUBSCRIPTION_KIND_LOCAL
                %>
                    <div class="field is-grouped">
                        <g:each in="${subkinds}" var="subKind">

                            <g:if test="${subKind.value == RDStore.SUBSCRIPTION_KIND_NATIONAL.value}">
                                <div class="control js-natLic">
                            </g:if>
                            <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_ALLIANCE.value}">
                                <div class="control js-allLic">
                            </g:elseif>
                            <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_CONSORTIAL.value}">
                                <div class="control js-consLic">
                            </g:elseif>
                            <g:else>
                                <div class="control">
                            </g:else>
                                    <label class="checkbox" for="checkSubType-${subKind.id}">
                                        <input id="checkSubType-${subKind.id}" name="subKinds" type="checkbox" value="${subKind.id}"
                                            <g:if test="${params.list('subKinds').contains(subKind.id.toString())}"> checked="" </g:if>
                                            <g:if test="${initQuery}"> checked="" </g:if>
                                               tabindex="0">
                                        ${subKind.getI10n('value')}
                                    </label>
                                </div>
                        </g:each>
                        </div>
            </div>

            <div class="field" id="js-consAuth">
                <label class="label">${message(code: 'gasco.filter.consortialAuthority')}</label>
                <div class="control is-expanded">
                    <div class="select is-fullwidth">
                        <g:select from="${allConsortia}" id="consortial"
                                  optionKey="${{ Org.class.name + ':' + it.id }}"
                                  optionValue="${{ it.getName() }}"
                                  name="consortia"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                                  value="${params.consortia}"/>
                    </div>
                </div>
            </div>

            <div class="field is-grouped is-grouped-centered">
                <div class="control">
                    <a href="${request.forwardURI}" class="button is-light">${message(code:'default.button.reset.label')}</a>
                </div>
                <div class="control">
                    <input type="submit" class="button is-info" value="${message(code:'default.button.search.label')}">
                </div>
            </div>

        </g:form>

    </div>
</section><!-- .custom-section-form -->

    <laser:script file="${this.getGroovyPageFileName()}">

            function toggleFilterPart() {
                console.log('! 1')
                if ($('.js-consLic input').prop('checked')) {
                    $('#js-consAuth select').removeAttr('disabled')
                } else {
                    $('#js-consAuth select').attr('disabled', 'disabled')
                }
            }
            function toggleTableHeading() {
                console.log('! 2')
                if ($('.js-natLic input').prop('checked') || $('.js-allLic input').prop('checked')) {
                    $('#js-negotiator-header').show()
                    $('#js-consortium-header').hide()
                } else {
                    $('#js-negotiator-header').hide()
                    $('#js-consortium-header').show()
                }
            }
            toggleFilterPart()
            $('.js-natLic').on('change', toggleFilterPart)
            $('.js-allLic').on('change', toggleFilterPart)
            $('.js-consLic').on('change', toggleFilterPart)
            toggleTableHeading()
            $('form .button.is-info').on('click', toggleTableHeading)

    </laser:script>

<g:if test="${!initQuery}">

<section class="section custom-section-result">
    <div class="container is-widescreen">

    <g:if test="${subscriptionsCount == 0}">
        <div class="level">
            <div class="level-item"><span class="tag is-medium is-warning">Leider keine Treffer</span></div>
        </div>
    </g:if>
    <g:else>
        <div class="level mb-6">
            <div class="level-item"><span class="tag is-medium is-success">${subscriptionsCount} Treffer</span></div>
        </div>

        <table class="table is-striped is-fullwidth">
            <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code:'gasco.table.product')}</th>
                <th>${message(code:'gasco.table.provider')}</th>
                <th>
                    <div id="js-consortium-header">${message(code:'gasco.table.consortium')}</div>
                    <div id="js-negotiator-header">${message(code:'gasco.table.negotiator')}</div>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:set var="GASCO_INFORMATION_LINK" value="${PropertyStore.SUB_PROP_GASCO_INFORMATION_LINK}" />
            <g:set var="GASCO_ANZEIGENAME" value="${PropertyStore.SUB_PROP_GASCO_DISPLAY_NAME}" />
            <g:set var="GASCO_VERHANDLERNAME" value="${PropertyStore.SUB_PROP_GASCO_NEGOTIATOR_NAME}" />

                <g:each in="${subscriptions}" var="sub" status="i">
                    <g:set var="gasco_infolink" value="${sub.propertySet.find{ it.type == GASCO_INFORMATION_LINK}?.urlValue}" />
                    <g:set var="gasco_anzeigename" value="${sub.propertySet.find{ it.type == GASCO_ANZEIGENAME}?.stringValue}" />
                    <g:set var="gasco_verhandlername" value="${sub.propertySet.find{ it.type == GASCO_VERHANDLERNAME}?.stringValue}" />
                    <tr>
                        <td class="has-text-centered">
                            ${i + 1}
                        </td>
                        <td>
                            <g:if test="${gasco_infolink}">
                                <a href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                            </g:if>
                            <g:else>
                                ${gasco_anzeigename ?: sub}
                            </g:else>

                            <g:each in="${sub.packages}" var="subPkg" status="j">
                                <br/><g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">
                                    <span class="icon">
                                        <span class="tag">&#128269;</span>
                                    </span>
                                    ${subPkg.pkg}
                                </g:link>
                            </g:each>
                        </td>
                        <td>
                            <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RDStore.OR_PROVIDER)}" var="role">
                                ${role.org?.name}<br/>
                            </g:each>
                        </td>
                        <td>
                            <div class="block">
                                ${gasco_verhandlername ?: sub.getConsortia()?.name}

                                <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, sub.getConsortia())}" var="personRole">
                                    <g:set var="person" value="${personRole.getPrs()}" />
                                        <g:if test="${person.isPublic}">

                                            <g:if test="${person && (person.getFirst_name() != 'Kontakt' && person.getLast_name() != 'Kontakt')}">
                                                <br/>${person.getFirst_name()} ${person.getLast_name()}
                                            </g:if>
                                            <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_URL)}" var="prsContact">
                                                <br/><a href="${prsContact?.content}" target="_blank">
                                                    <span class="icon">
                                                        <span class="tag">&#127760;</span>
                                                    </span>
                                                    ${prsContact?.content}</a>
                                            </g:each>
                                            <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_EMAIL)}" var="prsContact">
                                                <br/><a href="mailto:${prsContact?.content}">
                                                    <span class="icon">
                                                        <span class="tag">&#128236;</span>
                                                    </span>
                                                    ${prsContact?.content}</a>
                                            </g:each>
                                        </g:if>
                                    </g:each>
                            </div>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:else>

    </div>
</section><!-- .custom-section-result -->

</g:if>

</body>
</html>