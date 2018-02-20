<!doctype html>
<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} - ${institution.name} - ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</title>
</head>

<body>

    <semui:modeSwitch controller="subscriptionDetails" action="index" params="${params}"/>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode: institution.shortcode]}"
                     text="${institution.getDesignation()}"/>
        <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
    </semui:breadcrumbs>


    <semui:controlButtons>
        <g:render template="actions"/>
    </semui:controlButtons>

    <semui:messages data="${flash}"/>

<h1 class="ui header">${institution?.name} - ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</h1>

<semui:filter>
    <g:form action="currentSubscriptions" params="${[shortcode: institution.shortcode]}" controller="myInstitutions"
            method="get" class="form-inline ui small form">

        <div class="four fields">
            <!-- 1-1 -->
            <div class="field">
                <label>${message(code: 'default.search.text', default: 'Search text')}</label>

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
            <!-- 1-3 -->
            <div class="field fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.renewalDate.label" name="renewalDate"
                                  placeholder="filter.placeholder" value="${xyz}"/>
            </div>
            <!-- 1-4 -->
            <div class="field fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.durationDateEnd.label"
                                  name="durationDateEnd" placeholder="filter.placeholder" value="${xyz}"/>
            </div>
        </div>

        <div class="four fields">
            <!-- 2-1 -->
            <div class="field fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.consortium.label" name="consortium"
                                  placeholder="filter.placeholder" value="${xyz}"/>
            </div>
            <!-- 2-2 -->
            <div class="field fieldcontain">
                <semui:datepicker label="myinst.currentSubscriptions.filter.status.label" name="status"
                                  placeholder="filter.placeholder" value="${xyz}"/>
            </div>
            <!-- 2-3 -->
            <div class="field fieldcontain la-combi-input-left">
                <label>${message(code: 'myinst.currentSubscriptions.licence_property')}</label>
                <g:select class="ui dropdown" id="availablePropertyTypes" name="availablePropertyTypes"
                          from="${custom_prop_types}" optionKey="value" optionValue="key"
                          value="${params.propertyFilterType}"/>

            </div>
            <!-- 2-4 -->
            <div class="field fieldcontain la-combi-input-right">
                <label for="dateBeforeVal">Wert</label>
                <input id="selectVal" type="text" name="propertyFilter"
                       placeholder="${message(code: 'license.search.property.ph', default: 'property value...')}"
                       value="${params.propertyFilter ?: ''}"/>
                <input type="hidden" id="propertyFilterType" name="propertyFilterType"
                       value="${params.propertyFilterType}"/>
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>
                <fieldset id="subscritionType">
                    <div class="inline fields la-filter-inline">
                        <!-- 3-1 -->
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkConstortial">${message(code: 'myinst.currentSubscriptions.filter.consortial.label')}</label>
                                <input id="checkConstortial" type="checkbox" tabindex="0">
                            </div>
                        </div>
                        <!-- 3-2 -->
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkLocal">${message(code: 'myinst.currentSubscriptions.filter.lokal.label')}</label>
                                <input id="checkLocal" type="checkbox" tabindex="0">
                            </div>
                        </div>
                        <!-- 3-3 -->
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkAllinz">${message(code: 'myinst.currentSubscriptions.filter.alliance.label')}</label>
                                <input id="checkAllinz" type="checkbox" tabindex="0">
                            </div>
                        </div>
                        <!-- 3-4 -->
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkNational">${message(code: 'myinst.currentSubscriptions.filter.national.label')}</label>
                                <input id="checkNational" type="checkbox">
                            </div>
                        </div>
                    </div>
                </fieldset>
            </div>

            <div class="field">
                <div class="two fields">
                    <div class="field">
                        <label>${message(code: 'myinst.currentSubscriptions.filter.filterForRole.label')}</label>

                        <div class="inline fields la-filter-inline">
                            <div class="field">
                                <div class="ui radio checkbox">
                                    <input id="radioSubscriber" type="radio" name="fruit" tabindex="0" class="hidden">
                                    <label for="radioSubscriber">${message(code: 'subscription.details.members.label')}</label>
                                </div>
                            </div>

                            <div class="field">
                                <div class="ui radio checkbox">
                                    <input id="radioKonsortium" type="radio" name="fruit" tabindex="0" class="hidden">
                                    <label for="radioKonsortium">${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="field la-filter-search ">
                        <input type="submit" class="ui secondary button" value="Suchen">
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
            <th><g:annotatedLabel owner="${institution}"
                                  property="linkedPackages">${message(code: 'license.details.linked_pkg', default: 'Linked Packages')}</g:annotatedLabel></th>
            <th>${message(code: 'myinst.currentSubscriptions.subscription_type', default: 'Subscription Type')}</th>

            <g:if test="${params.orgRole == 'Subscriber'}">
                <th>${message(code: 'consortium', default: 'Consortia')}</th>
            </g:if>
            <g:if test="${params.orgRole == 'Subscription Consortia'}">
                <th>${message(code: 'consortium.subscriber', default: 'Subscriber')}</th>
            </g:if>

            <g:sortableColumn params="${params}" property="s.startDate"
                              title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
            <g:sortableColumn params="${params}" property="s.endDate"
                              title="${message(code: 'default.endDate.label', default: 'End Date')}"/>

        <!--<g:sortableColumn params="${params}" property="s.manualCancellationDate"
                              title="${message(code: 'default.cancellationDate.label', default: 'Cancellation Date')}"/>-->
            <th></th>
        </tr>
        </thead>
        <g:each in="${subscriptions}" var="s">
            <g:if test="${true || !s.instanceOf}">
                <tr>
                    <td>
                        <g:link controller="subscriptionDetails" action="details"
                                params="${[shortcode: institution.shortcode]}" id="${s.id}">
                            <g:if test="${s.name}">${s.name}</g:if><g:else>-- ${message(code: 'myinst.currentSubscriptions.name_not_set', default: 'Name Not Set')}  --</g:else>
                            <g:if test="${s.instanceOf}">(${message(code: 'subscription.isInstanceOf.label', default: 'Dependent')}<g:if
                                    test="${s.consortia && s.consortia == institution}">: ${s.subscriber?.name}</g:if>)</g:if>
                        </g:link>
                        <g:if test="${s.owner}">
                            <g:link class="icon ico-object-link sub-link-icon law" controller="licenseDetails"
                                    action="index" id="${s.owner.id}">${s.owner?.reference}</g:link>
                        </g:if>
                    </td>
                    <td>
                    <!-- packages -->
                        <g:each in="${s.packages}" var="sp" status="ind">
                            <g:if test="${ind < 10}">
                                <g:link controller="packageDetails" action="show" id="${sp.pkg?.id}"
                                        title="${sp.pkg?.contentProvider?.name}">
                                    ${sp.pkg.name}
                                </g:link>
                            </g:if>
                        </g:each>
                        <g:if test="${s.packages.size() > 10}">
                            <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                        </g:if>
                        <g:if test="${editable && (s.packages == null || s.packages.size() == 0)}">
                            <i>${message(code: 'myinst.currentSubscriptions.no_links', default: 'None currently, Add packages via')}
                            <g:link controller="subscriptionDetails" action="linkPackage"
                                    id="${s.id}">${message(code: 'subscription.details.linkPackage.label', default: 'Link Package')}</g:link>
                            </i>
                        </g:if>
                    <!-- packages -->
                    </td>
                    <td>
                        <!-- subscriptions type -->
                        <!-- subscriptions type -->
                    </td>
                    <g:if test="${params.orgRole == 'Subscriber'}">
                        <td>${s.consortia?.name}</td>
                    </g:if>
                    <g:if test="${params.orgRole == 'Subscription Consortia'}">
                        <td>
                            <g:each in="${s.allSubscribers}" var="subscriber">
                                <g:link controller="organisations" action="show"
                                        id="${subscriber.id}">${subscriber}</g:link>
                            </g:each>
                        </td>
                    </g:if>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></td>

                    <td class="x">
                        <g:if test="${editable && ((institution in s.allSubscribers) || s.consortia == institution)}">
                            <g:link controller="myInstitutions" action="actionCurrentSubscriptions"
                                    class="ui icon basic negative button"
                                    params="${[shortcode: institution.shortcode, curInst: institution.id, basesubscription: s.id]}"
                                    onclick="return confirm('${message(code: 'license.details.delete.confirm', args: [(s.name ?: 'this subscription')])}')">
                                <i class="trash icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>
</div>

<g:if test="${subscriptions}">
    <semui:paginate action="currentSubscriptions" controller="myInstitutions" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${num_sub_rows}"/>
</g:if>

<r:script type="text/javascript">
        $(document).ready(function(){
            var val = "${params.dateBeforeFilter}";
            if(val == "null"){
                $(".dateBefore").addClass("hidden");
            }else{
                $(".dateBefore").removeClass("hidden");
            }
            $( "input" ).keyup(function() {
              var val2 =  $( "input" ).val();
              if(val2 == ""){
                $( this ).removeClass( "la-input-selected" );
              }
              else {
                $( this ).addClass( "la-input-selected" );
              }
            });
        });

        $("[name='dateBeforeFilter']").change(function(){
            var val = $(this)['context']['selectedOptions'][0]['label'];

            if(val != "${message(code: 'default.filter.date.none', default: '-None-')}"){
                $(".dateBefore").removeClass("hidden");
            }else{
                $(".dateBefore").addClass("hidden");
            }
        })
</r:script>

</body>
</html>
