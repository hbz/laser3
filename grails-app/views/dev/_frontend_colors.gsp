<style>


.ui.container {
    width: 1127px;
    margin: 0 auto;
}
.ui.segment.__color-box-segment {
    background: 0 0!important;
    -webkit-box-shadow: none!important;
    box-shadow: none!important;
    display: inline-block;
    width: 220px;
    text-align: center;
}
.ui.segment.__color-box-segment .color-container {
    height: 200px;
    width: 160px;
    overflow: hidden;
    border-radius: 0 0 20px 20px;
    display: inline-block;
}
.ui.segment.__color-box-segment .color-container .color-wrapper {
    display: inline-block;
    border-radius: 20px;
    overflow: hidden;
    padding: 0;
    -webkit-transform: skew(0deg,13deg);
    transform: skew(0deg,13deg);
    font-size: 0;
    margin: 30px 0 0 0;
    height: 250px;
    width: 160px;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__default {
    background: #d3dae3!important;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__primary {
    background: #004678!important;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__primary-light {
    background: #2185d0 !important;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__secondary {
    background: #1B1C1D !important;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__secondary-light {
    background: #545454 !important;
}

.ui.segment.__color-box-segment .color-container .color-wrapper.__negative {
    background: #BB1600!important;
}
.ui.segment.__color-box-segment .color-container .color-wrapper.__success{
    background: #98B500!important;
}
</style>

<h2 class="ui dividing header">Colors<a class="anchor" id="colors"></a></h2>
<h4 class="ui header">Zu verwenden über die jeweilen Klassen</h4>

<div class="html ui top attached segment example">

    <div class="ui doubling five column grid">
        <div class="ui segment">
            <div class="ui grid stackable equal width">
                <div class="row">
                    <div class="column">
                        <div class="ui segment __color-box-segment">
                            <div class="color-container">
                                <div class="color-wrapper __primary"></div>
                            </div>
                            <div class="text-container">
                                <h5>PRIMARY</h5>
                                <p>CSS-Klasse: <b>blue la-modern-button</b> bei Icon-Buttons, die nur bei Hover ausgefüllt sind
                                <p>CSS-Klasse: <b>primary</b> bei Submit-Buttons
                            </div>
                        </div>
                    </div>
                    <div class="column">
                        <div class="ui segment __color-box-segment">
                            <div class="color-container">
                                <div class="color-wrapper __secondary-light"></div>
                            </div>
                            <div class="text-container">
                                <h5>SECONDARY</h5>
                                <p>CSS-Klasse: <b>secondary</b>
                            </div>
                        </div>
                    </div>
                    <div class="column">
                        <div class="ui segment __color-box-segment">
                            <div class="color-container">
                                <div class="color-wrapper __success"></div>
                            </div>
                            <div class="text-container">
                                <h5>SUCCESS</h5>
                                <p>CSS-Klasse: <b>success</b>
                            </div>
                        </div>
                    </div>
                    <div class="column">
                        <div class="ui segment __color-box-segment">
                            <div class="color-container">
                                <div class="color-wrapper __negative"></div>
                            </div>
                            <div class="text-container">
                                <h5>NEGATIVE</h5>
                                <p>CSS-Klasse: <b>negative</b>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <div class="ui top attached label">Laser Button Color Palette</div>


</div>
