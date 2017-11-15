<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'myinst.tipview.label', default:'Edit Core Titles')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
      <semui:crumb text="(JUSP & KB+)" message="myinst.tipview.label" class="active" />
    </semui:breadcrumbs>

      <div class="container">


      <g:if test="${flash.message}">
      <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </g:if>
        <g:if test="${flash.error}">
        <bootstrap:alert class="alert alert-error">${flash.error}</bootstrap:alert>
      </g:if>

      <ul class="nav nav-pills">
          <g:set var="nparams" value="${params.clone()}"/>
          <g:set var="active_filter" value="${nparams.remove('filter')}"/>

          <li class="${(active_filter=='core' || active_filter == null)?'active':''}">
            <g:link action="tipview" params="${nparams + [filter:'core']}">${message(code:'subscription.details.core', default:'Core')}</g:link>
          </li>
          <li class="${active_filter=='not'?'active':''}"><g:link action="tipview" params="${nparams + [filter:'not']}">${message(code:'myinst.tipview.notCore', default:'Not Core')}</g:link></li>
          <li class="${active_filter=='all'?'active':''}"><g:link action="tipview" params="${nparams + [filter:'all']}">${message(code:'myinst.tipview.all', default:'All')}</g:link></li>

      </ul>
      <div class="row">
        <div class="span12">
          <g:form action="tipview" method="get" params="${[shortcode:params.shortcode]}">

          <div class="well form-horizontal" style="clear:both">
            <div style="float:left;margin:8px;">
              ${message(code:'title.search', default:'Search For')}:
              <select name="search_for">
                <option ${params.search_for=='title' ? 'selected' : ''} value="title">${message(code:'title.label', default:'Title')}</option>
                <option ${params.search_for=='provider' ? 'selected' : ''} value="provider">${message(code:'default.provider.label', default:'Provider')}</option>
              </select>
            </div>
            <div style="float:left;margin:8px;">
              ${message(code:'default.name.label', default:'Name')}:
              <input name="search_str" style="padding-left:8px" placeholder="${message(code:'myinst.tipview.search.ph', default:'Partial terms accepted')}" value="${params.search_str}"/>
            </div>
            <div style="float:left;margin:8px;">
              ${message(code:'default.sort.label', default:'Sort')}:
              <select name="sort">
                <option ${params.sort=='title-title' ? 'selected' : ''} value="title-title">${message(code:'title.label', default:'Title')}</option>
                <option ${params.sort=='provider-name' ? 'selected' : ''} value="provider-name">${message(code:'default.provider.label', default:'Provider')}</option>
              </select>
            </div>
            <div style="float:left;margin:8px;">
              ${message(code:'default.order.label', default:'Order')}:
              <select name="order" value="${params.order}">
                <option ${params.order=='asc' ? 'selected' : ''} value="asc">${message(code:'default.asc', default:'Ascending')}</option>
                <option ${params.order=='desc' ? 'selected' : ''} value="desc">${message(code:'default.desc', default:'Descending')}</option>
              </select>
              <input type="hidden" name="filter" value="${params.filter}"/>
            </div>
            <div style="float:left;margin:8px;">
              <button type="submit" name="search">${message(code:'default.button.search.label', default:'Search')}</button>
            </div>
            <div style="clear:both">
          </div>
          </g:form>
        </div>
      </div>


        <table class="ui celled striped table">
          <thead>
            <tr>
              <th>${message(code:'myinst.tipview.tip_tid', default:'Title in Package; Title Details')}</th>
              <th>${message(code:'default.provider.label', default:'Provider')}</th>
              <th>${message(code:'default.status.label', default:'Status')}</th>
            </tr>
          </thead>
          <tbody>
          <g:each in="${tips}" var="tip">
            <tr>

              <td>
              <g:link controller="myInstitutions" action="tip" params="${[shortcode:params.shortcode]}" id="${tip.id}">${tip?.title?.title} ${message(code:'default.via', default:'via')} ${tip?.provider?.name}</g:link>;
              &nbsp;
              <g:link controller="titleDetails" action="show" id="${tip?.title?.id}">${message(code:'myinst.tipview.link_to_title', default:'Link to Title Details')}</g:link>
              </td>
              <td>
              <g:link controller="org" action="show" id="${tip?.provider?.id}">${tip?.provider?.name}</g:link>
              </td>
              <td class="link">

                <g:set var="coreStatus" value="${tip?.coreStatus(null)}"/>
                <a href="#" class="editable-click" onclick="showDetails(${tip.id});">${coreStatus?'True(Now)':coreStatus==null?'False(Never)':'False(Now)'}</a>
              </td>
            </tr>
          </g:each>
          </tbody>
        </table>
          <div class="pagination" style="text-align:center">
            <span><bootstrap:paginate action="tipview" max="${user?.defaultPageSize?:10}" params="${[:]+params}" next="Next" prev="Prev" total="${tips.totalCount}" /></span>
          </div>
        <div id="magicArea">
        </div>
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
          $("input.datepicker-class").datepicker({
            format:"${session.sessionPreferences?.globalDatepickerFormat}"
          });
          $("[name='coreAssertionEdit']").modal('show');
          $('.xEditableValue').editable();
        }
        </g:javascript>

  </body>
</html>
