<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} - ${message(code: 'license.new')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentLicenses" message="license.current"/>
    <semui:crumb message="license.new" class="active"/>
</semui:breadcrumbs>
<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>
<h1 class="ui header"><semui:headerIcon/>${institution?.name} - ${message(code: 'license.new')}</h1>

<!--
<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="myInstitution" action="currentLicenses" message="license.current"/>
    <semui:subNavItem controller="myInstitution" action="addLicense_DEPR" message="license.copy"/>
    <g:if test="${is_inst_admin}">
        <semui:subNavItem controller="myInstitution" action="addLicense_DEPR" message="license.add.blank"/>
    </g:if>
</semui:subNav>-->

<h3>${message(code: 'license.new', default: 'Create new License')}</h3>
<g:form action="processEmptyLicense" controller="myInstitution" method="post" class="ui form newLicence">
    <g:hiddenField name="sub" value="${params.sub}"/>


    <g:if test="${params.sub}">
        <g:hiddenField name="sub" value="${params.sub}"/>
        <div class="ui info message">${message(code: 'myinst.licensewithSub.notice', default: 'Create a new license and link it to the subscription "{0}"', args: [params.subName])}</div>
    </g:if>

    <g:if test="${params.baselicense}">
        <g:hiddenField name="baselicense" value="${params.baselicense}"/>
        <div class="ui info message">${message(code: 'myinst.copyLicense.notice', default: 'Create new License from License Template "{0}"', args: [params.licenseName])}</div>
    </g:if>

    <div class="field required">
        <label>${message(code: 'myinst.emptyLicense.name', default: 'New License Name')}</label>
        <input required type="text" name="licenseName" value="${params.licenseName}" placeholder=""/>
    </div>

    <semui:datepicker label="license.startDate" name="licenseStartDate" value="${params.licenseStartDate?:defaultStartYear}" />

    <semui:datepicker label="license.endDate" name="licenseEndDate" value="${params.licenseEndDate?:defaultEndYear}"/>

    <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label', default: 'Create')}"/>
</g:form>

<hr>


<h3>${message(code: 'license.copy')}</h3>
<g:if test="${numLicenses > 0 || (params.filter)}">

    <semui:filter>
        <g:form action="emptyLicense" params="${params}" method="get" class="ui form">
            <div class="fields">
                <div class="field">
                    <label>${message(code: 'license.name')}</label>
                    <input name="filter" type="text" value="${params.filter}"/>
                </div>


                <div class="field">
                    <label>&nbsp;</label>
                    <input type="submit" value="${message(code:'default.button.filter.label', default:'Filter')}" class="ui secondary button">
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.filterreset.label')}</a>
                </div>

            </div>
            <input type="hidden" name="sort" value="${params.sort}">
            <input type="hidden" name="order" value="${params.order}">
        </g:form>
    </semui:filter>

    <!--
        <div>
            <div class="well license-options">
    <g:if test="${is_inst_admin}">
        <input type="submit" name="copy-license" value="${message(code: 'default.button.copySelected.label', default: 'Copy Selected')}" class="ui negative button" />
    </g:if>
    <g:else>${message(code: 'myinst.addLicense.no_permission', default: 'Sorry, you must have editor role to be able to add licenses')}</g:else>
    </div>
</div>
-->
    <semui:messages data="${flash}"/>

    <div class="license-results">
        <table class="ui sortable celled la-table table">
            <thead>
            <tr>
                <g:sortableColumn params="${params}" property="reference" title="${message(code: 'license.name')}"/>
                <th>${message(code: 'license.licensor.label', default: 'Licensor')}</th>
                <g:sortableColumn params="${params}" property="startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <g:sortableColumn params="${params}" property="endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
                <th>${message(code: 'default.actions.label', default: 'Action')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${licenses}" var="l">
                <tr>
                    <td>
                        <g:link action="show"
                                controller="licenseDetails"
                                id="${l.id}">
                            <g:if test="${l.reference}">${l.reference}</g:if>
                            <g:else>${message(code: 'myinst.addLicense.no_ref', args: [l.id])}</g:else>
                        </g:link>
                        <g:if test="${l.pkgs && (l.pkgs.size() > 0)}">
                            <ul>
                                <g:each in="${l.pkgs.sort { it.name }}" var="pkg">
                                    <li><g:link controller="packageDetails" action="show"
                                                id="${pkg.id}">${pkg.id} (${pkg.name})</g:link><br/></li>
                                </g:each>
                            </ul>
                        </g:if>
                        <g:else>
                            <br/>${message(code: 'myinst.addLicense.no_results', default: 'No linked packages.')}
                        </g:else>
                    </td>
                    <td>${l.licensor?.name}</td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${l.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${l.endDate}"/></td>
                    <td class="x">
                        <g:link controller="myInstitution" action="emptyLicense"
                                params="${[baselicense: l.id, 'copy-license': 'Y', licenseName: l.reference, licenseStartDate: l.startDate, licenseEndDate: l.endDate, sub: params.sub]}" class="ui icon positive button">
                            <i class="copy icon"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <g:if test="${licenses}">
            <semui:paginate action="emptyLicense" controller="myInstitution" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                            total="${numLicenses}"/>
        </g:if>

    </div>

</g:if><g:else>
    <br><b>${message(code: 'license.template.empty', default: 'Currently there are no contract templates available')}</b>
</g:else>


<r:script type="text/javascript">
    $('.license-results input[type="radio"]').click(function () {
        $('.license-options').slideDown('fast');
    });

    $('.license-options .delete-license').click(function () {
        $('.license-results input:checked').each(function () {
            $(this).parent().parent().fadeOut('slow');
            $('.license-options').slideUp('fast');
        })
    })
</r:script>

</body>
</html>
