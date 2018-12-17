<!doctype html>
<html>
<head>
<meta name="layout" content="semanticUI" />
<title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.comp_lic')}</title>
</head>
<body>

<r:require modules="onixMatrix" />

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />

        <semui:crumb class="active" message="menu.institutions.comp_lic" />

        <!--<li class="dropdown pull-right">
          <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<strong class="caret"></strong></a>&nbsp;
          <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
            <li>
              <g:link action="compare" params="${params+[format:'csv']}">CSV Export</g:link>
            </li>
          </ul>
        </li>-->
    </semui:breadcrumbs>

    <h2 class="ui header">${message(code:'menu.institutions.comp_lic')}</h2>

  <div class="onix-matrix-wrapper">

    <table class="onix-matrix-STOP_JAVASCRIPT ui la-table la-table-small table">
        <thead>
            <th class="cell-1">Merkmal</th>
            <g:each in="${licenses}" var="license" status="counter">
                <th class="cell-${ (counter + 2) }"><span class="cell-inner" >${license.reference}</span></th>
            </g:each>
        </thead>
        <tbody>
            <g:each in="${map}" var="entry">
                <tr>
                    <td>${entry.getKey()}</td>
                    <g:each in="${licenses}" var="lic">
                        <g:if test="${entry.getValue().containsKey(lic.reference)}">
                            <td>
                                <g:set var="point" value="${entry.getValue().get(lic.reference)}"/>
                                <%--<div class="onix-icons">
                                    <g:if test="${point.getNote()}">
                                        <span class='main-annotation'><i class='icon-edit' data-content='Click the icon to view the annotations.' title='Annotations'></i></span>
                                        <div class="textelement">
                                            <ul><li><span>${point.getNote()}</span></li></ul>
                                        </div>
                                    </g:if>
                                </div>--%>
                                <g:if test="${['stringValue','intValue','decValue'].contains(point.getValueType())}">
                                    <span class="cell-inner">  <strong>${point.getValue()}</strong></span>
                                </g:if>
                                <g:else>
                                    <g:set var="val" value="${point.getValue()}"/>
                                    <g:if test="${val == 'Y' || val=="Yes"}">
                                        <span class="cell-inner">
                                            <span title="${val}" class="onix-status onix-tick" />
                                        </span>
                                    </g:if>
                                    <g:elseif test="${val=='N' || val=="No"}">
                                        <span class="cell-inner">
                                            <span title="${val}" class="onix-status onix-pl-prohibited" />
                                        </span>
                                    </g:elseif>
                                    <g:elseif test="${['O','Other','Specified'].contains(val)}">
                                        <span class="cell-inner">
                                            <span title="${val}" class="onix-status onix-info" />
                                        </span>
                                    </g:elseif>
                                    <g:elseif test="${['U','Unknown','Not applicable','Not Specified'].contains(val)}">
                                        <span class="cell-inner-undefined">
                                            <span title="${val}" class="onix-status onix-pl-undefined" ></span>
                                        </span>
                                    </g:elseif>
                                    <g:else>
                                     <span class="cell-inner">  <strong>${point.getValue()}</strong></span>
                                    </g:else>
                                </g:else>
                            </td>
                        </g:if>
                        <g:else>
                            <td>
                                <span class="cell-inner-undefined">
                                    <span title='Not Set' class="onix-status onix-pl-undefined" ></span>
                                </span>
                            </td>
                        </g:else>
                    </g:each>
                </tr>
            </g:each>
        </tbody>
   </table>

   </div>

   <div id="onix-modal" class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">Modal title</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer">
        <button type="button" class="ui button" data-dismiss="modal">Close</button>
      </div>
  </div>
  </div>
</div>
    <r:script language="JavaScript">

        $(function(){/*
          $(".onix-pl-undefined").each(function(){
            var title = $(this).attr("data-original-title")
            $(this).replaceWith("<span title='"+title+"' style='height:1em' class='onix-status fa-stack fa-4x'> <i class='fa fa-info-circle fa-stack-1x' style='color:#166fe7;' ></i> <i class='fa fa-ban fa-stack-1x' style='color:#FF0000'></i> </span>")
          })*/
            // Tooltips.
        });

    </r:script>
     </body>

 </html>
