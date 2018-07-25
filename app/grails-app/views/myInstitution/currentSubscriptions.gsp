<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>
<!doctype html>

<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="currentSubscriptions" params="${params+[exportXLS:'yes']}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
            <g:render template="actions" />
        </semui:controlButtons>

        <semui:messages data="${flash}"/>

        <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</h1>

<semui:filter>
    <g:form action="currentSubscriptions" controller="myInstitution" method="get" class="form-inline ui small form">

        <div class="three fields">
            <!-- 1-1 -->
            <div class="field">
                <label>${message(code: 'default.search.text', default: 'Search text')} (Lizenz, Vertrag, Paket, Anbieter, Konsortium, Agentur)</label>

                <div class="ui input">
                    <input type="text" name="q"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.q?.encodeAsHTML()}"/>
                </div>
            </div>
            <!-- 1-2 -->
            <div class="field fieldcontain">
                <semui:datepicker label="default.valid_on.label" name="validOn" placeholder="filter.placeholder"
                                  value="${validOn}"/>
            </div>
            <% /*
            <!-- 1-3 -->
            <div class="field disabled fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.renewalDate.label" name="renewalDate"
                                  placeholder="filter.placeholder" value="${params.renewalDate}"/>
            </div>
            <!-- 1-4 -->
            <div class="field disabled fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.durationDateEnd.label"
                                  name="durationDate" placeholder="filter.placeholder" value="${params.durationDate}"/>
            </div>
            */ %>

            <!-- TMP -->
            <div class="field fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

<%--
            <!-- 2-1 -->
            <div class="field disabled fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                <laser:select name="status" class="ui dropdown"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.consortium}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>

            </div>
            <!-- 2-2 -->
            <div class="field disabled fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <laser:select name="status" class="ui dropdown"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <!-- 2-3 -->
            <div class="field disabled fieldcontain la-combi-input-left">
                <label>${message(code:'subscription.property.search')}</label>
                <g:select class="ui dropdown" id="availablePropertyTypes" name="availablePropertyTypes"
                          from="${custom_prop_types}" optionKey="value" optionValue="key" value="${params.propertyFilterType}"/>
            </div>
            <!-- 2-4 -->
            <div class="field disabled fieldcontain la-combi-input-right">
                <label for="propertyFilter">Wert</label>

                <input id="propertyFilter" type="text" name="propertyFilter"
                       placeholder="${message(code: 'license.search.property.ph')}" value="${params.propertyFilter ?: ''}"/>
                <input type="hidden" id="propertyFilterType" name="propertyFilterType" value="${params.propertyFilterType}"/>
            </div>

           --%>

            <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>

                <fieldset id="subscritionType">
                    <div class="inline fields la-filter-inline">

                        <g:each in="${RefdataCategory.getAllRefdataValues('Subscription Type')}" var="subType">
                            <div class="inline field">
                                <div class="ui checkbox">
                                    <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                    <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                        <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                           tabindex="0">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </fieldset>
            </div>

            <div class="field">
                <div class="two fields">

                    <g:if test="${institution?.orgType?.value == 'Consortium'}">
                        <div class="field">

                            <%--
                            <g:if test="${params.orgRole == 'Subscriber'}">
                                <input id="radioSubscriber" type="hidden" value="Subscriber" name="orgRole" tabindex="0" class="hidden">
                            </g:if>
                            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                                <input id="radioKonsortium" type="hidden" value="Subscription Consortia" name="orgRole" tabindex="0" class="hidden">
                            </g:if>
                            --%>

                            <label>${message(code: 'myinst.currentSubscriptions.filter.filterForRole.label')}</label>

                            <div class="inline fields la-filter-inline">
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input id="radioSubscriber" type="radio" value="Subscriber" name="orgRole" tabindex="0" class="hidden"
                                               <g:if test="${params.orgRole == 'Subscriber'}">checked=""</g:if>
                                            >
                                        <label for="radioSubscriber">${message(code: 'subscription.details.members.label')}</label>
                                    </div>
                                </div>

                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input id="radioKonsortium" type="radio" value="Subscription Consortia" name="orgRole" tabindex="0" class="hidden"
                                               <g:if test="${params.orgRole == 'Subscription Consortia'}">checked=""</g:if>
                                            >
                                        <label for="radioKonsortium">${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </g:if>

                    <div class="field la-filter-search">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:filter>

<div class="subscription-results">
    <table class="ui celled sortable table table-tworow la-table">
        <thead>
        <tr>
            <g:sortableColumn params="${params}" property="s.name" title="${message(code: 'license.slash.name')}"/>
            <th>
                <g:annotatedLabel owner="${institution}" property="linkedPackages">${message(code: 'license.details.linked_pkg', default: 'Linked Packages')}</g:annotatedLabel>
            </th>
            <% /*
            <th>
                ${message(code: 'myinst.currentSubscriptions.subscription_type', default: 'Subscription Type')}
            </th>
            */ %>

            <g:if test="${params.orgRole == 'Subscriber'}">
                <th>${message(code: 'consortium', default: 'Consortia')}</th>
            </g:if>

            <th>${message(code: 'default.provider.label', default: 'Provider')} / ${message(code: 'default.agency.label', default: 'Agency')}</th>
            <%--
            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                <th>${message(code: 'consortium.subscriber', default: 'Subscriber')}</th>
            </g:if>
            --%>
            <g:sortableColumn params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>

            <g:sortableColumn params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label', default: 'End Date')}"/>

            <% /* <g:sortableColumn params="${params}" property="s.manualCancellationDate"
                              title="${message(code: 'default.cancellationDate.label', default: 'Cancellation Date')}"/> */ %>
            <th class="two wide"></th>
        </tr>
        </thead>
        <g:each in="${subscriptions}" var="s">
            <g:if test="${true || !s.instanceOf}">
                <tr>
                    <td>
                        <g:link controller="subscriptionDetails" action="show" id="${s.id}">
                            <g:if test="${s.name}">
                                ${s.name}
                            </g:if>
                            <g:else>
                                -- ${message(code: 'myinst.currentSubscriptions.name_not_set', default: 'Name Not Set')}  --
                            </g:else>
                            <g:if test="${s.instanceOf}">
                                (${message(code: 'subscription.isInstanceOf.label', default: 'Dependent')}
                                <g:if test="${s.consortia && s.consortia == institution}">
                                    : ${s.subscriber?.name}
                                </g:if>)
                            </g:if>
                        </g:link>
                        <g:if test="${s.owner}">
                                <div class="la-flexbox">
                                    <i class="icon balance scale la-list-icon"></i>
                                    <g:link  controller="licenseDetails" action="show" id="${s.owner.id}">${s.owner?.reference?:message(code:'missingLicenseReference', default:'** No License Reference Set **')}</g:link>
                                </div>
                        </g:if>
                    </td>
                    <td>
                    <!-- packages -->
                        <g:each in="${s.packages}" var="sp" status="ind">
                            <g:if test="${ind < 10}">
                                <div class="la-flexbox">
                                    <i class="icon gift la-list-icon"></i>
                                    <g:link controller="packageDetails" action="show" id="${sp.pkg?.id}"
                                            title="${sp.pkg?.contentProvider?.name}">
                                        ${sp.pkg.name}
                                    </g:link>
                                </div>
                            </g:if>
                        </g:each>
                        <g:if test="${s.packages.size() > 10}">
                            <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                        </g:if>
                        <g:if test="${editable && (s.packages == null || s.packages.size() == 0)}">
                            <i>
                                ${message(code: 'myinst.currentSubscriptions.no_links', default: 'None currently, Add packages via')}
                                <g:link controller="subscriptionDetails" action="linkPackage"
                                    id="${s.id}">${message(code: 'subscription.details.linkPackage.label', default: 'Link Package')}</g:link>
                            </i>
                        </g:if>
                    <!-- packages -->
                    </td>
                    <%--
                    <td>
                        ${s.type?.getI10n('value')}
                    </td>
                    --%>

                    <g:if test="${params.orgRole == 'Subscriber'}">
                        <td>
                            ${s.getConsortia()?.name}
                        </td>
                    </g:if>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(s, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))}" var="role">
                            <g:link controller="Organisations" action="show" id="${role.org?.id}">${role.org?.name}</g:link><br />
                        </g:each>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(s, RefdataValue.getByValueAndCategory('Agency', 'Organisational Role'))}" var="role">
                            <g:link controller="Organisations" action="show" id="${role.org?.id}">${role.org?.name} (${message(code: 'default.agency.label', default: 'Agency')})</g:link><br />
                        </g:each>
                    </td>
                    <%--
                    <td>
                        <g:if test="${params.orgRole == 'Subscription Consortia'}">
                            <g:each in="${s.getDerivedSubscribers()}" var="subscriber">
                                <g:link controller="organisations" action="show" id="${subscriber.id}">${subscriber.name}</g:link> <br />
                            </g:each>
                        </g:if>
                    </td>
                    --%>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></td>

                    <td class="x">
                        <g:if test="${statsWibid && (s.getCommaSeperatedPackagesIsilList()?.trim()) && s.hasOrgWithUsageSupplierId()}">
                          <laser:statsLink class="ui icon button"
                                         base="${grailsApplication.config.statsApiUrl}"
                                         module="statistics"
                                         controller="default"
                                         action="select"
                                         target="_blank"
                                         params="[mode:usageMode,
                                                  packages:s.getCommaSeperatedPackagesIsilList(),
                                                  institutions:statsWibid
                                         ]"
                                         title="Springe zu Statistik im Nationalen Statistikserver"> <!-- TODO message -->
                            <i class="chart bar outline icon"></i>
                          </laser:statsLink>
                        </g:if>

                        <g:if test="${editable && ((institution in s.allSubscribers) || s.consortia == institution)}">
                            <g:link controller="myInstitution" action="actionCurrentSubscriptions"
                                    class="ui icon negative button"
                                    params="${[curInst: institution.id, basesubscription: s.id]}"
                                    onclick="return confirm('${message(code: 'license.details.delete.confirm', args: [(s.name ?: 'this subscription')])}')">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>
</div>

    <g:if test="${subscriptions}">
        <semui:paginate action="currentSubscriptions" controller="myInstitution" params="${params}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                        total="${num_sub_rows}"/>
    </g:if>

    <r:script type="text/javascript">
        $(document).ready(function(){
              // initialize the form an fields
              $('.ui.form')
              .form();
            var val = "${params.dateBeforeFilter}";
            if(val == "null"){
                $(".dateBefore").addClass("hidden");
            }else{
                $(".dateBefore").removeClass("hidden");
            }
        });

        $("[name='dateBeforeFilter']").change(function(){
            var val = $(this)['context']['selectedOptions'][0]['label'];

            if(val != "${message(code:'default.filter.date.none', default:'-None-')}"){
                $(".dateBefore").removeClass("hidden");
            }else{
                $(".dateBefore").addClass("hidden");
            }
        })
    </r:script>

    <%--
    <r:script type="text/javascript">

        function availableTypesSelectUpdated(optionSelected) {

            var selectedOption = $( "#availablePropertyTypes option:selected" )
            var selectedValue = selectedOption.val()

            if (selectedValue) {
                //Set the value of the hidden input, to be passed on controller
                $('#propertyFilterType').val(selectedOption.text())

                updateInputType(selectedValue)
            }
        }

        function updateInputType(selectedValue) {
            //If we are working with RefdataValue, grab the values and create select box
            if(selectedValue.indexOf("RefdataValue") != -1) {
                var refdataType = selectedValue.split("&&")[1]
                $.ajax({
                    url:'<g:createLink controller="ajax" action="sel2RefdataSearch"/>'+'/'+refdataType+'?format=json',
                    success: function(data) {
                        var select = ' <select id="propertyFilter" name="propertyFilter" > '
                        //we need empty when we dont want to search by property
                        select += ' <option></option> '
                        for (var index=0; index < data.length; index++ ) {
                            var option = data[index]
                            select += ' <option value="'+option.text+'">'+option.text+'</option> '
                        }
                        select += '</select>'
                        $('#propertyFilter').replaceWith(select)
                    },async:false
                });
            }else{
                //If we dont have RefdataValues,create a simple text input
                $('#propertyFilter').replaceWith('<input id="propertyFilter" type="text" name="propertyFilter" placeholder="${message(code:'license.search.property.ph', default:'property value')}" />')
            }
        }

        function setTypeAndSearch(){
            var selectedType = $("#propertyFilterType").val()
            //Iterate the options, find the one with the text we want and select it
            var selectedOption = $("#availablePropertyTypes option").filter(function() {
                return $(this).text() == selectedType ;
            }).prop('selected', true); //This will trigger a change event as well.


            //Generate the correct select box
            availableTypesSelectUpdated(selectedOption)

            //Set selected value for the actual search
            var paramPropertyFilter = "${params.propertyFilter}";
            var propertyFilterElement = $("#propertyFilter");
            if(propertyFilterElement.is("input")){
                propertyFilterElement.val(paramPropertyFilter);
            }
            else {
                $("#propertyFilter option").filter(function() {
                    return $(this).text() == paramPropertyFilter ;
                }).prop('selected', true);
            }
        }

        $('#availablePropertyTypes').change(function(e) {
            var optionSelected = $("option:selected", this);
            availableTypesSelectUpdated(optionSelected);
        });

        window.onload = setTypeAndSearch()
    </r:script>
    --%>


  </body>
</html>
