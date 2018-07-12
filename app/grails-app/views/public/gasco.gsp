<%@ page import="com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} - ${message(code: 'gasco.title')}</title>
</head>

<body>
    <br>
    <h1 class="ui header">${message(code:'gasco.title')}</h1>
    <div class="ui grid">
        <div class="eleven wide column">
            <semui:filter>
                <g:form action="currentSubscriptions" controller="myInstitution" method="get" class="form-inline ui small form">

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


                                    <div class="inline field">
                                        <div class="ui checkbox">

                                            <input id="checkSubType-514" name="subTypes" type="checkbox" value="514" tabindex="0" class="hidden"><label for="checkSubType-514">${message(code: 'gasco.filter.nationalLicence')}</label>
                                        </div>
                                    </div>

                                    <div class="inline field">
                                        <div class="ui checkbox">

                                            <input id="checkSubType-517" name="subTypes" type="checkbox" value="517" tabindex="0" class="hidden"><label for="checkSubType-517">${message(code: 'gasco.filter.allianceLicence')}</label>
                                        </div>
                                    </div>

                                    <div class="inline field">
                                        <div class="ui checkbox js-consortiallicence">

                                            <input id="checkSubType-516" name="subTypes" type="checkbox" value="516" tabindex="0" class="hidden"><label for="checkSubType-516">${message(code: 'gasco.filter.consotialLicence')}</label>
                                        </div>
                                    </div>

                                </div>
                            </fieldset>
                        </div>

                    <div class="field" id="js-consotial-authority">
                        <label>${message(code: 'gasco.filter.consotialAuthority')}</label>

                        <div class="ui fluid search selection dropdown" >
                            <input type="hidden" name="country">
                            <i class="dropdown icon"></i>

                            <div class="default text">${message(code: 'gasco.filter.chooseConsotialAuthority')}</div>

                            <div class="menu">
                                <div class="item">Option 1</div>

                                <div class="item">Option 2</div>

                                <div class="item">Option 3</div>

                            </div>
                        </div>
                    </div>
                    <div class="field la-filter-search ">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label', default:'Search')}">
                    </div>

                </g:form>
            </semui:filter>
        </div>
        <div class="five wide column">
            <img class="ui fluid image" alt="Logo GASCO" class="ui fluid image" src="images/gasco/GASCO-Logo-2_klein.jpg"/>
        </div>
    </div>

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
                        <br>
                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                    ${subPkg.pkg}
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

        <g:each in="${subscriptions}" var="sub" status="i">
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

</body>
<r:script>
    $(document).ready(function() {

        $( '.js-consortiallicence' ).click(function() {
                $('#js-consotial-authority').toggleClass('disabled')
        });
    });
</r:script>