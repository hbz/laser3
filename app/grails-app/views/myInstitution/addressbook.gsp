<%@ page
import="com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory"
%>

<!doctype html>
<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.addressbook', default:'Addressbook')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.institutions.addressbook" class="active"/>
        </semui:breadcrumbs>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>


        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.myAddressbook', default:'My Addressbook')}</h1>

        <semui:messages data="${flash}" />

        <semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible" />

        <g:render template="/person/formModal" model="['org': institution,
                                                       'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No'),
                                                       'presetFunctionType': RefdataValue.getByValueAndCategory('General contact person', 'Person Function')
        ]"/>

        <g:if test="${visiblePersons}">

            <semui:filter>
                <g:form action="addressbook" controller="myInstitution" method="get" class="form-inline ui small form">
                    <div class="four fields">
                        <div class="field">
                            <label for="prs">${message(code: 'person.filter.name')}</label>
                            <div class="ui input">
                                <input type="text" id="prs" name="prs" value="${params.prs}"
                                       placeholder="${message(code: 'person.filter.name')}" />
                            </div>
                        </div>
                        <div class="field">
                            <label for="org">${message(code: 'person.filter.org')}</label>
                            <div class="ui input">
                                <input type="text" id="org" name="org" value="${params.org}"
                                       placeholder="${message(code: 'person.filter.org')}" />
                            </div>
                        </div>
                        <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
                    </div>

                    <div class="field la-field-right-aligned">
                        <label></label>
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                    </div>
                </g:form>
            </semui:filter>

            <g:render template="/templates/cpa/person_table" model="${[persons: visiblePersons, restrictToOrg: null]}" />

            <semui:paginate action="addressbook" controller="myInstitution" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                            max="${max}"
                            total="${num_visiblePersons}"/>

        </g:if>
    </body>
</html>
