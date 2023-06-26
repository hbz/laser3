<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.UserSetting" %>
<laser:htmlStart text="Plan "> %{-- <!doctype html><html><head>--}%
    <style>
        .mk-licence:before {
            content: "\f328";
        }
        .mk-asService:before {
            content: "\f2d2";
        }
        .mk-accessRights:before {
            content: "\f505";
        }
        .mk-propertiesUse:before {
            content: "\f19c";
        }
        .mk-erms:before {
            content: "\f19c";
        }
        .mk-propertiesUse:before {
            content: "\f19c";
        }
        .mk-propertiesCreation:before {
            content: "\f19c";
        }
        .mk-cost:before {
            content: "\f155";
        }
        .mk-ie:before {
            content: "\f02d";
        }
        .mk-docs:before {
            content: "\f15c";
        }
        .mk-tasks:before {
            content: "\f0ae";
        }
        .mk-notifications:before {
            content: "\f024";
        }
        .mk-address:before {
            content: "\f0e0";
        }
        .mk-budget:before {
            content: "\f0d6";
        }
        .mk-reporting:before {
            content: "\f200";
        }
        .mk-testSystem:before {
            content: "\e90f";
            font-family: icomoon;
        }
        .mk-community:before {
            content: "\e917";
            font-family: icomoon;
        }
        .mk-wekb:before {
            content: "\e90e";
            font-family: icomoon;
        }
        .mk-api:before {
            content: "\f120";
        }

        .mk-support:before {
            content: "\f51c";
        }
        .mk-help:before {
            content: "\e05c";
        }
        .mk-handbook:before {
            content: "\f5da";
        }
        .mk-progression:before {
            content: "\f120";
        }
        .mk-trainingFundamentals:before {
            content: "\f51c";
        }
        .mk-trainingIndividual:before {
            content: "\f51c";
        }
        .mk-userMeeting:before {
            content: "\f51c";
        }
    </style>

</laser:htmlStart> %{-- </head><body>--}%

<h1 class="ui icon header la-clear-before left floated aligned la-positionRelative"><i class="icon blue icon la-laser la-object"></i> Die LAS:eR Vorteile</h1>

<table class="ui large striped structured la-table table">
    <thead>
    <tr>
        <th class="two wide" rowspan="2">Die LAS:eR Vorteile</th>
        <th class="center aligned"  rowspan="2"></th>
        <th class="center aligned" colspan="2">Las:eR</th>
    </tr>
    <tr>
        <th class="four wide center aligned">Basic</th>
        <th class="four wide center aligned">Pro</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${mappingColsBasic}" var="mpg">
        <tr>
            <td class="four wide">
                <div class="la-flexbox la-minor-object">
                    <i class="icon la-list-icon mk-${mpg}"></i>
                    ${message(code:"marketing.featureList.${mpg}")}
                </div>
            </td>

            <td>
            <g:if test="${mpg === 'licence'}">
                    <button class="ui icon blue button la-modern-button la-modal" >
                        <i class="film icon"></i>
                    </button>
            </g:if>
            </td>
            <td class="four wide center aligned">
                <i class="large green checkmark icon"></i>
            </td>
            <td class="four wide center aligned ">
                <i class="large green checkmark icon"></i>
            </td>
        </tr>
    </g:each>
    <g:each in="${mappingColsPro}" var="mpg">
        <tr>
            <td class="four wide">
                <div class="la-flexbox la-minor-object">
                    <i class="icon la-list-icon mk-${mpg}"></i>
                    ${message(code:"marketing.featureList.${mpg}")}
                </div>
            </td>
             <td>
                 <g:if test="${mpg === 'propertiesUse'}">
                     <button class="ui icon blue button la-modern-button la-modal" >
                         <i class="film icon"></i>
                     </button>
                 </g:if>
             </td>
             <td class="four wide center aligned">

             </td>
            <td class="four wide center aligned ">
                <i class="large green checkmark icon"></i>
            </td>
        </tr>
    </g:each>
    </tbody>
    <tfooter>
        <tr>
            <td class="four wide">

            </td>
            <td></td>
            <td class="center aligned">

            </td>
            <td class="center aligned ">
                <a href="mailto:laser@hbz-nrw.de" class="ui huge first button">
                    Beratungsgespäch vereinbaren<i class="right arrow icon"></i>
                </a>
            </td>
        </tr>

    </tfooter>
</table>
<h1 class="ui icon header la-clear-before left floated aligned la-positionRelative"><i class="icon blue icon la-laser la-object"></i>Der LAS:eR-Service</h1>

<table class="ui large striped structured la-table table">
    <thead>
    <tr>
        <th class="two wide" rowspan="2">Der LAS:eR-Service</th>
        <th class="center aligned"  rowspan="2"></th>
        <th class="center aligned" colspan="2">Las:eR</th>
    </tr>
    <tr>
        <th class="four wide center aligned">Basic</th>
        <th class="four wide center aligned">Pro</th>
    </tr>
    </thead>
    <tbody>

    <g:each in="${mappingColsServiceBasic}" var="mpg">
        <tr>
            <td class="four wide">
                <div class="la-flexbox la-minor-object">
                    <i class="icon la-list-icon mk-${mpg}"></i>
                    ${message(code:"marketing.featureList.${mpg}")}
                </div>
            </td>
            <td>
               %{--     <button class="ui icon blue button la-modern-button la-modal" >
                   <i class="film icon"></i>
               </button>--}%
            </td>
            <td class="four wide center aligned">
                <i class="large green checkmark icon"></i>
            </td>
            <td class="four wide center aligned ">
                <i class="large green checkmark icon"></i>
            </td>
        </tr>
    </g:each>
    <g:each in="${mappingColsServicePro}" var="mpg">
        <tr>
            <td class="four wide">
                <div class="la-flexbox la-minor-object">
                    <i class="icon la-list-icon mk-${mpg}"></i>
                    ${message(code:"marketing.featureList.${mpg}")}
                </div>
            </td>
            <td>%{--
                <button class="ui icon blue button la-modern-button la-modal" >
                    <i class="film icon"></i>
                </button>--}%
            </td>
            <td class="four wide center aligned">

            </td>
            <td class="four wide center aligned ">
                <i class="large green checkmark icon"></i>
            </td>
        </tr>
    </g:each>
    </tbody>
    <tfooter>
        <tr>
            <td class="four wide">

            </td>
            <td></td>
            <td class="center aligned">

            </td>
            <td class="center aligned ">
                <a href="mailto:laser@hbz-nrw.de" class="ui huge first button">
                    Beratungsgespäch vereinbaren<i class="right arrow icon"></i>
                </a>
            </td>
        </tr>

    </tfooter>
</table>
<g:if test="(${mpg === 'licence'} || ${mpg === 'propertiesUse'})">
    <g:each in="${mappingColsBasic}" var="mpg">
        <div class="ui large modal">
            <div class="content">${mpg}
                <button class="ui right floated button la-animatedGif-redo ">Animation wiederholen</button>
                <img width="100%" alt="" class="la-animatedGif-img la-padding-top-1em" src="${resource(dir: 'images', file: "${mpg}.gif")}"/>
            </div>
            <div class="actions">
                <a href="#" class="ui positive button" >Schließen</a>
            </div>
        </div>
    </g:each>
</g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-modal').click(function(){
        $('.large.modal')
            .modal('show')
        ;
    });

    if ($('.la-animatedGif-img').length) {
      var gifSrc, srcGif;

      $('.la-animatedGif-img').each(function () {
        gifSrc = $(this).attr("src");
        $(this).attr("data-gif", gifSrc);

      });
    } /* end img[src$="gif"] */

    $('.la-animatedGif-redo').on("click", function () {
      srcGif = $(this).next().attr("data-gif");
      $(this).next().attr("src", srcGif);
    }); /* end $win.on('scroll.gifrun') */

</laser:script>
<laser:htmlEnd /> %{-- </body></html> --}%
