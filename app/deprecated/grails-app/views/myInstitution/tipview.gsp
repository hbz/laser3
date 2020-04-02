<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${institution.name} - ${message(code:'myinst.tipview.label')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb text="(JUSP & KB+)" message="myinst.tipview.label" class="active" />
        </semui:breadcrumbs>
        <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.institutions.myCoreTitles')}</h1>

        <ul class="nav nav-pills">
            <g:set var="nparams" value="${params.clone()}"/>
            <g:set var="active_filter" value="${nparams.remove('filter')}"/>

            <li class="${(active_filter=='core' || active_filter == null)?'active':''}">
                <g:link action="tipview" params="${nparams + [filter:'core']}">${message(code:'subscription.details.core')}</g:link>
            </li>
            <li class="${active_filter=='not'?'active':''}"><g:link action="tipview" params="${nparams + [filter:'not']}">${message(code:'myinst.tipview.notCore')}</g:link></li>
            <li class="${active_filter=='all'?'active':''}"><g:link action="tipview" params="${nparams + [filter:'all']}">${message(code:'myinst.tipview.all')}</g:link></li>
        </ul>

        <semui:messages data="${flash}" />

        <g:render template="/templates/filter/javascript" />
        <semui:filter showFilterButton="true">
            <g:form class="ui form" action="tipview" method="get">

                <div class="fields">
                    <div class="field">
                        <label>${message(code:'title.search')}</label>
                        <select name="search_for">
                            <option ${params.search_for=='title' ? 'selected' : ''} value="title">${message(code:'title.label')}</option>
                            <option ${params.search_for=='provider' ? 'selected' : ''} value="provider">${message(code:'default.provider.label')}</option>
                        </select>
                    </div>
                    <div class="field">
                        <label>${message(code:'default.name.label')}</label>
                        <input name="search_str" style="padding-left:8px" placeholder="${message(code:'myinst.tipview.search.ph', default:'Partial terms accepted')}" value="${params.search_str}"/>
                    </div>
                    <div class="field">
                        <label>${message(code:'default.sort.label')}</label>
                        <select name="sort">
                            <option ${params.sort=='title-title' ? 'selected' : ''} value="title-title">${message(code:'title.label')}</option>
                            <option ${params.sort=='provider-name' ? 'selected' : ''} value="provider-name">${message(code:'default.provider.label')}</option>
                        </select>
                    </div>
                    <div class="field">
                        <label>${message(code:'default.order.label')}</label>
                        <select name="order" value="${params.order}">
                            <option ${params.order=='asc' ? 'selected' : ''} value="asc">${message(code:'default.asc')}</option>
                            <option ${params.order=='desc' ? 'selected' : ''} value="desc">${message(code:'default.desc')}</option>
                        </select>
                        <input type="hidden" name="filter" value="${params.filter}"/>
                    </div>
                    <div class="field">
                        <label>&nbsp;</label>
                        <button type="submit" class="ui secondary button" name="search">${message(code:'default.button.filter.label')}</button>
                    </div>
                </div>

            </g:form>
        </semui:filter>

        <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'myinst.tipview.tip_tid')}</th>
              <th>${message(code:'default.provider.label')}</th>
              <th>${message(code:'default.status.label')}</th>
            </tr>
          </thead>
          <tbody>
          <g:each in="${tips}" var="tip">
            <tr>

              <td>
                  <semui:listIcon type="${tip.title.medium?.value}"/>
                  <strong><g:link controller="myInstitution" action="tip" id="${tip.id}">${tip?.title?.title} ${message(code:'default.via')} ${tip?.provider?.name}</g:link></strong><br>
                  <g:link controller="title" action="show" id="${tip?.title?.id}">${message(code:'myinst.tipview.link_to_title')}</g:link>
              </td>
              <td>
              <g:link controller="organisation" action="show" id="${tip?.provider?.id}">${tip?.provider?.name}</g:link>
              </td>
              <td class="link">

                <g:set var="coreStatus" value="${tip?.coreStatus(null)}"/>
                <a href="#" class="editable-click" onclick="showDetails(${tip.id});">${coreStatus?'True(Now)':coreStatus==null?'False(Never)':'False(Now)'}</a>
              </td>
            </tr>
          </g:each>
          </tbody>
        </table>

          <semui:paginate action="tipview" max="${user?.getDefaultPageSizeTMP()?:10}" params="${[:]+params}" next="Next" prev="Prev" total="${tips.totalCount}" />

        <div id="magicArea">
        </div>


        <g:javascript>
        function showDetails(id){
          console.log(${editable});
          jQuery.ajax({type:'get', url:"${createLink(controller:'ajax', action:'getTipCoreDates')}?editable="+${editable}+"&tipID="+id,success:function(data,textStatus){jQuery('#magicArea').html(data);$('div[name=coreAssertionEdit]').modal("show")},error:function(XMLHttpRequest,textStatus,errorThrown){}
        });
        }

         function hideModal(){
          $("[name='coreAssertionEdit']").modal('hide');
         }

        function showCoreAssertionModal(){
          $("[name='coreAssertionEdit']").modal('show');
          $('.xEditableValue').editable();
        }
        </g:javascript>

  </body>
</html>
