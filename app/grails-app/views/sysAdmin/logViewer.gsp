<!DOCTYPE html>
<html>
<head>
<meta name="layout" content="semanticUI"/>
<title>${message(code:'laser', default:'LAS:eR')} Log Watcher</title>
<r:script type="JavaScript" >
  var initScroll = false;

  (function($) {
    var modCache = {};

    $(document).ready(function() {

      $('a.display-inline').each(function(){

        var link = $(this);
        var href = link.attr("href");

        // Selector of the sub=part of the page to pull in.
        var desired_selector = link.attr("data-content-selector");
        var auto_refresh = link.attr("data-auto-refresh");

        if (!desired_selector) desired_selector = "#mainarea";

        // The function to refresh content of the target element from the url.
        var refreshContent = function (target, url, type, the_data) {

          var key = "mod-" + url;

          // Grab the data.
          var grabContent = function() {

            console.log("Updated grabbing contents");

            if (the_data == undefined) the_data = {};

            if (type == undefined) {
              type = "get";

            } else if (type != "get") {

              // Make it a post.
              type = "post";
            }
            $[type](url, the_data, function(data, textStatus, xhr) {
              modCache[key] = xhr.getResponseHeader("Last-Modified");

              // The returned data.
              var dataDom = $("<div>" + data + "</div>");
              var desired_content = dataDom.find(desired_selector);
              if (desired_content.length == 1) {
                target.html(desired_content.html());
              } else {
                target.html(dataDom.html());
              }
            });
          };

          if(modCache[key]) {
            $.ajax({
              url:url,
              type:"head",
              success:function(res,code,xhr) {
                if(modCache[key] != xhr.getResponseHeader("Last-Modified")) {
                  // Get the content.
                  grabContent();
                } else { /* Content hasn't changed so let's leave it alone */ }
              }
            });

          } else grabContent();

          return target;
        };

        // Start with a new div.
        var content = refreshContent($("<div class='inline-content' />"), href);

        // The next thing to do is to bind an event listener to the content,
        // that can intercept links that are nav. These links should reload in the content area
        // rather than navigating away from the page.
        content.on ('click', function(event) {
          // The clicks should bubble up here before being actioned.

          // The clicked item. Get the closest matching a tag.
          var clicked = $(event.target).closest('.nav a, .inline-nav a, a.open-inline');

          if (clicked.length > 0) {
            // Is a nav link. First thing to do is to stop the event default.
            event.preventDefault();

            refreshContent( $(this), clicked.attr('href') );
          }

          // Else just allow the event to propagate.
        });

        // Add the new submit listener for forms here too.
        // We should catch the form submit and then simply serialise the form and do
        // the get or post n the background using ajax.
        content.on ('submit', function(event) {
          // The clicks should bubble up here before being actioned.

          // The clicked item. Get the closest matching a tag.
          var form = $(event.target).closest('form.open-inline');

          if (form.length > 0) {

            event.preventDefault();

            refreshContent( $(this), form.attr('action'), form.attr('method'), form.serialize());
          }
        });

        // Then swap out the link for the new content.
        link.replaceWith(content);

        // Automatically update the content
        if (auto_refresh && !isNaN(auto_refresh)) {
          auto_refresh = parseInt(auto_refresh)

          // Add an autorefresh method.
          setInterval(function() {
            refreshContent( content, href );
          }, auto_refresh);
        }
      });
    });

    $(document).ready(function(){
      var availableSpace = $( window ).innerHeight();
      availableSpace = availableSpace - $("h1.page-header").outerHeight(true);
      availableSpace = availableSpace - $('#page-wrapper').siblings('.navbar').outerHeight(true);
      $('#log-wrapper')
        .css('height', (availableSpace -10) + 'px');
        
      // Listen to the ajax event.
      $(document).on("ajaxStop", function () {
        if (!initScroll) {
          var height = $('#log-wrapper')[0].scrollHeight;
          $('#log-wrapper').scrollTop(height);
          initScroll = true;
        }
      });
    });
  
  })(jQuery);
</r:script>
</head>
<body>
  <semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Application Log" class="active"/>
  </semui:breadcrumbs>

    <h1 class="page-header">Log Viewer</h1>
    <div id="log-wrapper">
      <g:link class="display-inline" controller="file" params="[filePath: (file)]" data-auto-refresh="1000" data-content-selector=".fileContents" />
    </div>

</body>
</html>