<%--
  Created by IntelliJ IDEA.
  User: ioannis
  Date: 30/06/2014
  Time: 16:22
--%>

<%@ page import="com.k_int.kbplus.JasperReportsController; org.jasper.JasperExportFormat" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'jasper.reports.label', default:'Jasper Reports')}</title>
    <meta name="layout" content="mmbootstrap"/>
</head>

<body>

<div class="container">

        <ul class="breadcrumb">
            <li><g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span></li>
            <li><g:link controller="jasperReports" action="index">${message(code:'jasper.reports.label', default:'Jasper Reports')}</g:link> <span
                    class="divider">/</span></li>
        </ul>

        <g:if test="${flash.message}">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>
        <g:if test="${flash.error}">
            <bootstrap:alert class="alert-danger">${flash.error}</bootstrap:alert>
        </g:if>

        <div class="inline-lists">
            <dl>
                <dt>${message(code:'jasper.reports.selected', default:'Selected Report')}:</dt>
                <dd>
                    <span>
                        <g:select id="available_reports" name="report_name" from="${available_reports}"/>
                    </span>
                </dd>
                <dt>${message(code:'jasper.reports.format', default:'Download Format')}:</dt>
                <dd>
                    <span>
                        <g:select id="selectRepFormat" name="rep_format" from="${available_formats}"/>
                    </span>
                </dd>
            </dl>
        </div>

        <div id="report_details">
            <g:render template="report_details" model="params"/>
        </div>
</div>

</body>
<r:script language="JavaScript">

     $(function () {
        var repname = $('#available_reports option:selected').text()
        jQuery.ajax({type:'POST',data:{'report_name': repname}, url:'${createLink(controller: 'jasperReports', action: 'index')}'
        ,success:function(data,textStatus){jQuery('#report_details').html(data);}
        ,error:function(XMLHttpRequest,textStatus,errorThrown){}
        ,complete:function(XMLHttpRequest,textStatus){runJasperJS()}});
    });

    $(function () {
        $('#available_reports').change(function() {
            var repname = $('#available_reports option:selected').text()
            jQuery.ajax({type:'POST',data:{'report_name': repname}, url:'${createLink(controller: 'jasperReports', action: 'index')}'
            ,success:function(data,textStatus){jQuery('#report_details').html(data);}
            ,error:function(XMLHttpRequest,textStatus,errorThrown){}
            ,complete:function(XMLHttpRequest,textStatus){runJasperJS()}});
        });
    });

    $(function () {
        $('#selectRepFormat').change(function() {
            $('#hiddenReportFormat').val($("#selectRepFormat").val())
        });
    });
    function runJasperJS(){
        copyReportVals();
        activateDatepicker();
    }
    function copyReportVals() {
        $("#hiddenReportName").val($("#available_reports").val())
        $('#hiddenReportFormat').val($("#selectRepFormat").val())

    }
    function createSelect2Search(objectId, className) {
        $(objectId).select2({
            width: "90%",
            placeholder: "${message(code:'jasper.reports.search.ph', default:'Type name...')}",
            minimumInputLength: 1,
            ajax: { 
                url: '<g:createLink controller='ajax' action='lookup'/>',
                dataType: 'json',
                data: function (term, page) {
                    return {
                        hideIdent: true,
                        q: term, // search term
                        page_limit: 10,
                        baseClass:className
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            }
            });

    }
    function activateDatepicker(){

        $("div.date").children('input').datepicker()
    }
    document.onload = runJasperJS();
</r:script>
</html>
