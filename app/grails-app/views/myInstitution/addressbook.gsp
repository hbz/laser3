<%@ page
import="com.k_int.kbplus.Org"  
import="com.k_int.kbplus.Person" 
import="com.k_int.kbplus.PersonRole"
import="com.k_int.kbplus.RefdataValue" 
import="com.k_int.kbplus.RefdataCategory" 
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


        <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'menu.institutions.myAddressbook', default:'My Addressbook')}</h1>

        <semui:messages data="${flash}" />

        <div class="ui icon positive message">
            <i class="close icon"></i>
            <div class="content">
                <div class="header">
                    ${message(code: 'message.information')}
                </div>
                <p>${message(code:'myinst.addressBook.visible', default:'These persons are visible to you due your membership')}</p>
            </div>
        </div>

        <g:render template="/person/formModal" model="['org': institution,
                                                       'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'No'),
                                                       tmplHideResponsibilities: true
        ]"/>

        <g:if test="${visiblePersons}">

            <semui:filter>
                <g:form action="addressbook" controller="myInstitution" method="get" class="form-inline ui small form">
                    <div class="field">
                        <div class="three fields">
                            <div class="field">
                                <label>${message(code: 'person.filter.name')}</label>
                                <div class="ui input">
                                    <input type="text" name="prs" value="${params.prs}"
                                           placeholder="${message(code: 'person.filter.name')}" />
                                </div>
                            </div>
                            <div class="field">
                                <label>${message(code: 'person.filter.org')}</label>
                                <div class="ui input">
                                    <input type="text" name="org" value="${params.org}"
                                           placeholder="${message(code: 'person.filter.org')}" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="field">
                        <div class="three fields">
                            <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
                            <div class="field la-filter-search">
                                <label></label>
                                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                            </div>
                        </div>
                    </div>

                </g:form>
            </semui:filter>

            <g:render template="/templates/cpa/person_table" model="${[persons: visiblePersons]}"></g:render>

            <% /*
            <h5 class="ui header"><g:message code="org.prsLinks.label" default="Persons" /></h5>
            <div class="ui relaxed list">

                <g:each in="${visiblePersons}" var="p">
                    <g:render template="/templates/cpa/person_details" model="${[person: p]}"></g:render>
                </g:each>
            </div>
            */ %>
        </g:if>

  </body>
</html>
