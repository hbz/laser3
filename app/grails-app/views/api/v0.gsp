<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700" rel="stylesheet">

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
        #swagger-ui {
            margin-top: 100px;
        }
        #swagger-ui .topbar {
            position: fixed;
            top: 0;
            width: 100%;
            padding: 0px 30px;
            background-color: rgba(0,0,0, 0.75);
            z-index: 99;
        }
        #swagger-ui .topbar input {
            width: 250px;
            padding: 8px 10px;
            border: none;
        }
        #swagger-ui .topbar .ui-box {
            font-family: "Courier New";
            font-size: 12px;
            padding: 5px 10px;
            color: #fff;
        }

        #swagger-ui .topbar .link,
        #swagger-ui .topbar .download-url-wrapper {
            display: none;
        }
        #swagger-ui .information-container pre.base-url + a {
            display: none;
        }
        #swagger-ui textarea.curl {
            color: #666;
            background-color: #fff;
        }

        .glow {
            background-color: yellow !important;
            transition: background-color 0.5s;
        }
    </style>

    <r:require modules="swaggerApi" />

    <r:layoutResources/>
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

    <script>
        window.onload = function() {

            var placeholders = {
                query_q: 'q - Identifier for this query',
                query_v: 'v - Value for this query',
                authorization: 'x-authorization - hmac-sha256 generated auth header',
                context: 'context - Concrete globalUID of context organisation'
            }

            var jabba = SwaggerUIBundle({
                url: "${grailsApplication.config.grails.serverURL}/api/${apiVersion}/spec",
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

            window.jabba = jabba

            setTimeout(function(){
                jQuery('.topbar-wrapper').append('<span class="ui-box">Key <input name="apiKey" type="text" placeholder="Current API Key" value="${apiKey}"></span>')
                jQuery('.topbar-wrapper').append('<span class="ui-box">Pass <input name="apiPassword" type="password" placeholder="Current API Password" value="${apiPassword}"></span>')
                jQuery('.topbar-wrapper').append('<span class="ui-box">Context <input name="apiContext" type="text" placeholder="Current Context" value="${apiContext}"></span>')
                jQuery('.topbar-wrapper').append('<span class="ui-box">Authorization <input name="apiAuth" type="text" placeholder="Will be generated" value=""></span>')

            }, 1000)

            setTimeout(function(){
                jQuery('.opblock').delegate('input, textarea', 'change', function() {
                    var div = jQuery(this).parents('.parameters').first()
                    var auth = genDigist(div)

                    jQuery('.topbar-wrapper input[name="apiAuth"]').val(auth)
                })

                jQuery('.topbar-wrapper input').on('focus', function(){
                    jQuery(this).select()
                })

                jQuery('.opblock-summary').append('<button name="generateApiAuth" class="btn">Generate Auth</button>')

                jQuery('button[name=generateApiAuth]').on('click', function(event) {
                    event.stopPropagation()

                    var div = jQuery(event.target).parents('.opblock').find('.parameters').first()
                    if (div.length) {
                        var auth = genDigist(div)
                        jQuery('.topbar-wrapper input[name="apiAuth"]').val(auth)
                    }
                })
            }, 2000)

            function genDigist(div) {
                var id      = jQuery('.topbar-wrapper input[name=apiKey]').val().trim()
                var key     = jQuery('.topbar-wrapper input[name=apiPassword]').val().trim()
                var method  = jQuery(div).parents('.opblock').find('.opblock-summary-method').text()
                var path    = "/api/${apiVersion}" + jQuery(div).parents('.opblock').find('.opblock-summary-path span').text()
                var timestamp = ""
                var nounce    = ""

                var context = jQuery(div).find('input[placeholder="' + placeholders.context + '"]')
                if (context.length) {
                    context = context.val().trim()
                }
                else {
                    context = ""
                }

                var query     = ""
                var body      = ""

                if(method == "GET") {
                    var q = jQuery(div).find('input[placeholder="' + placeholders.query_q + '"]')
                    q = q.length ? "q=" + q.val().trim() : ''

                    var v = jQuery(div).find('input[placeholder="' + placeholders.query_v + '"]')
                    v = v.length ? "v=" + v.val().trim() : ''

                    query = q + ( q ? '&' : '') + v + (context ? (q || v ? '&' : '') + "context=" + context : '')

                    console.log(query)
                }
                /*
                if(method == "GET") {
                    query = "q=" + jQuery(div).find('input[placeholder="' + placeholders.query_q + '"]').val().trim()
                            + "&v=" + jQuery(div).find('input[placeholder="' + placeholders.query_v + '"]').val().trim()
                            + (context ? "&context=" + context : '')
                }*/
                else if(method == "POST") {
                    query = (context ? "&context=" + context : '')
                    body  = jQuery(div).find('.body-param > textarea').val().trim()
                }

                var algorithm     = "hmac-sha256"
                var digest        = CryptoJS.HmacSHA256(method + path + timestamp + nounce + query + body, key)
                var authorization = "hmac " + id + ":" + timestamp + ":" + nounce + ":" + digest + "," + algorithm

                return authorization
            }
        }
    </script>

    <r:layoutResources/>

</body>
</html>
