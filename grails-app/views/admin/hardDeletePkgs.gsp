<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} Admin::Package Delete</title>
      <%-- r:require module="annotations" / --%>
  </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
            <semui:crumb text="Package Delete" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Package Delete</h1>

        <semui:messages data="${flash}" />

        <semui:filter>
            <g:form action="hardDeletePkgs" method="get" params="${params}" class="ui form">
                <input type="hidden" name="offset" value="${params.offset}"/>

                <div class="field">
                    <label>Name</label>
                    <input name="pkg_name" placeholder="Partial terms accepted" value="${params.pkg_name}"/>
                </div>
                <div class="field">
                    <button type="submit" name="search" value="yes" class="ui secondary button">Search</button>
                </div>
            </g:form>
        </semui:filter>

        <table class="ui sortable celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
              <th></th>
            </tr>
          </thead>
          <tbody>
          <g:each in="${pkgs}" var="packageInstance">
            <tr>
              <td>
              <g:link controller="package" action="show" id="${packageInstance.id}">
              ${fieldValue(bean: packageInstance, field: "name")} (${packageInstance?.contentProvider?.name})</g:link>
              </td>            
              <td class="link">
                <button onclick="JSPC.app.showDetails(${packageInstance.id});" class="ui tiny button">Prepare Delete</button>
              </td>
            </tr>
          </g:each>
          </tbody>
        </table>

      <div class="paginateButtons" style="text-align:center">
        <span><g:paginate action="hardDeletePkgs" params="${params}" next="Next" prev="Prev" total="${pkgs.totalCount}" /></span>
      </div>


        <div id="packageDetails_div"></div>

        <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.showDetails = function (id) {
            jQuery.ajax({type:'get', url:"${createLink(controller:'admin', action:'hardDeletePkgs')}"+"/"+id,
                success: function(data,textStatus){
                    jQuery('#packageDetails_div').html(data);
                    $("#pkg_details_modal").modal("show");
                },
                error: function(XMLHttpRequest,textStatus,errorThrown){}
            });
        }
        </laser:script>

  </body>
</html>     