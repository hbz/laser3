<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700" rel="stylesheet">
    <link rel="stylesheet" href="${resource(dir: 'vendor/swagger-ui', file: 'swagger-ui.css')}" type="text/css">
    <title>${message(code:'laser', default:'LAS:eR')} - API</title>
    <style>
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *,
        *:before, *:after {
            box-sizing: inherit;
        }
        body {
          margin:0;
          background: #fff;
        }
        #swagger-ui .topbar {
            padding: 0px 30px;
            background-color: #004678;
        }
        #swagger-ui .topbar .link,
        #swagger-ui .topbar .download-url-wrapper {
            display: none;
        }
        #swagger-ui .topbar .ui-box {
            padding: 5px 10px;
        }
        #swagger-ui .topbar .ui-box input {
            padding: 4px 6px;
        }
        #swagger-ui .information-container pre.base-url + a {
            display: none;
        }
        #swagger-ui textarea.curl {
            color: #666;
            background-color: #fff;
        }
    </style>
</head>

<body>
  <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" style="position:absolute;width:0;height:0">
    <defs>
      <symbol viewBox="0 0 20 20" id="unlocked">
            <path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V6h2v-.801C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8z"></path>
      </symbol>

      <symbol viewBox="0 0 20 20" id="locked">
        <path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8zM12 8H8V5.199C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="close">
        <path d="M14.348 14.849c-.469.469-1.229.469-1.697 0L10 11.819l-2.651 3.029c-.469.469-1.229.469-1.697 0-.469-.469-.469-1.229 0-1.697l2.758-3.15-2.759-3.152c-.469-.469-.469-1.228 0-1.697.469-.469 1.228-.469 1.697 0L10 8.183l2.651-3.031c.469-.469 1.228-.469 1.697 0 .469.469.469 1.229 0 1.697l-2.758 3.152 2.758 3.15c.469.469.469 1.229 0 1.698z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="large-arrow">
        <path d="M13.25 10L6.109 2.58c-.268-.27-.268-.707 0-.979.268-.27.701-.27.969 0l7.83 7.908c.268.271.268.709 0 .979l-7.83 7.908c-.268.271-.701.27-.969 0-.268-.269-.268-.707 0-.979L13.25 10z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="large-arrow-down">
        <path d="M17.418 6.109c.272-.268.709-.268.979 0s.271.701 0 .969l-7.908 7.83c-.27.268-.707.268-.979 0l-7.908-7.83c-.27-.268-.27-.701 0-.969.271-.268.709-.268.979 0L10 13.25l7.418-7.141z"/>
      </symbol>


      <symbol viewBox="0 0 24 24" id="jump-to">
        <path d="M19 7v4H5.83l3.58-3.59L8 6l-6 6 6 6 1.41-1.41L5.83 13H21V7z"/>
      </symbol>

      <symbol viewBox="0 0 24 24" id="expand">
        <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/>
      </symbol>

    </defs>
  </svg>

    <div id="swagger-ui"></div>

    <script src="${resource(dir: 'vendor/swagger-ui', file: 'swagger-ui-bundle.js')}"> </script>
    <script src="${resource(dir: 'vendor/swagger-ui', file: 'swagger-ui-standalone-preset.js')}"> </script>
    <script src="${resource(dir: 'vendor/cryptoJS-v3.1.2/rollups', file: 'hmac-sha256.js')}"> </script>
    <script src="${resource(dir: 'js', file: 'jquery-3.2.1.min.js')}"> </script>

    <script>
        window.onload = function() {
            const ui = SwaggerUIBundle({
                url: "${grailsApplication.config.grails.serverURL}/api/spec",
                dom_id: '#swagger-ui',
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            })

            window.ui = ui

            setTimeout(function(){
                jQuery('.topbar-wrapper').append('<span class="ui-box"><input name="apiKey" type="text" placeholder="Your API Key" value="${apiKey}"></span>')
                jQuery('.topbar-wrapper').append('<span class="ui-box"><input name="apiSecret" type="password" placeholder="Your API Secret" value="${apiSecret}"></span>')

                jQuery('.opblock').delegate('input, textarea', 'change', function() {
                    genDigist(jQuery(this).parents('.parameters').first())
                })

            }, 1200)

            // todo: change full path dynamically
            // todo: set authorization via shadow dom

            function genDigist(div) {
                var key     = jQuery('.topbar input[name=apiKey]').val().trim()
                var secret  = jQuery('.topbar input[name=apiSecret]').val().trim()
                var method  = jQuery(div).parents('.opblock').find('.opblock-summary-method').text()
                var path    = "/api/v0" + jQuery(div).parents('.opblock').find('.opblock-summary-path > span').text()
                var timestamp = ""
                var nounce    = ""
                var context = jQuery(div).find('input[placeholder="context - Optional information if user has memberships in multiple organisations"]').val().trim()
                var query     = ""
                var body      = ""

                if(method == "GET") {
                    query = "q=" + jQuery(div).find('input[placeholder="q - Identifier for this query"]').val().trim()
                    //query   = "q=" + jQuery(div).find('select').val()
                            + "&v=" + jQuery(div).find('input[placeholder="v - Value for this query"]').val().trim()
                            + (context ? "&context=" + context : '')
                }
                else if(method == "POST") {
                    query = (context ? "&context=" + context : '')
                    body  = jQuery(div).find('.body-param > textarea').val().trim()
                }

                var algorithm = "hmac-sha256"
                var digest    = CryptoJS.HmacSHA256(method + path + timestamp + nounce + query + body, secret)
                var authorization = "hmac " + key + ":" + timestamp + ":" + nounce + ":" + digest + "," + algorithm

                var input = jQuery(div).find('input[placeholder="Authorization - hmac-sha256 generated auth header"]')
                jQuery(input).val(authorization).attr('value', authorization)
            }
        }
    </script>
</body>

</html>
