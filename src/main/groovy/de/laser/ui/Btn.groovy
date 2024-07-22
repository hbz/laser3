package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Btn {
    // please do not touch during refactoring ..

    // <button/>, <input type="button"/>, <g:link/>, etc.

    class MODERN {
        // please do not touch during refactoring ..

//        public static String PRIMARY_ICON       = 'ui button primary icon la-modern-button'
//        public static String SECONDARY_ICON     = 'ui button secondary icon la-modern-button'

        @UIDoc(usage = 'Buttons/Links/Inputs with Icon. Colors by declaration!')
        public static String BASIC_ICON         = 'ui button icon la-modern-button'
        @UIDoc(usage = 'TODO: refactoring; la-modern-button does not work without declared color!')
        public static String SIMPLE_ICON        = 'ui button blue icon la-modern-button'        // TODO: remove blue; merge with PRIMARY_ICON ?
        public static String POSITIVE_ICON      = 'ui button positive icon la-modern-button'
        public static String NEGATIVE_ICON      = 'ui button negative icon la-modern-button'

//        @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Js: Confirmation Dialog (data-confirm-attributes needed). Colors by declaration!')
//        public static String BASIC_ICON_CONFIRM     = 'ui button icon la-modern-button js-open-confirm-modal'
        @UIDoc(usage = 'Buttons/Links/Inputs (green) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed)')
        public static String POSITIVE_ICON_CONFIRM  = 'ui button positive icon la-modern-button js-open-confirm-modal'
        @UIDoc(usage = 'Buttons/Links/Inputs (red) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed)')
        public static String NEGATIVE_ICON_CONFIRM  = 'ui button negative icon la-modern-button js-open-confirm-modal'

        @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Js: Tooltip (data-attributes needed). Colors by declaration!')
        public static String BASIC_ICON_TOOLTIP       = 'ui button icon la-modern-button la-popup-tooltip la-delay'             // todo: remove la-delay
         @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Js: Tooltip (data-attributes needed); TODO: refactoring; la-modern-button does not work without declared color!')
        public static String SIMPLE_ICON_TOOLTIP      = 'ui button blue icon la-modern-button la-popup-tooltip la-delay'        // todo: remove la-delay
        @UIDoc(usage = 'Buttons/Links/Inputs (green) with Icon and Js: Tooltip (data-attributes needed)')
        public static String POSITIVE_ICON_TOOLTIP    = 'ui button positive icon la-modern-button la-popup-tooltip la-delay'    // todo: remove la-delay
        @UIDoc(usage = 'Buttons/Links/Inputs (red) with Icon and Js: Tooltip (data-attributes needed)')
        public static String NEGATIVE_ICON_TOOLTIP    = 'ui button negative icon la-modern-button la-popup-tooltip la-delay'    // todo: remove la-delay

        @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Js: Confirmation Dialog (data-confirm-attributes needed) and Tooltip (data-attributes needed); TODO: refactoring; la-modern-button does not work without declared color!')
        public static String SIMPLE_ICON_CONFIRM_TOOLTIP      = 'ui button blue icon la-modern-button js-open-confirm-modal la-popup-tooltip la-delay'        // todo: remove la-delay
        @UIDoc(usage = 'Buttons/Links/Inputs (green) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed) and Tooltip (data-attributes needed)')
        public static String POSITIVE_ICON_CONFIRM_TOOLTIP    = 'ui button positive icon la-modern-button js-open-confirm-modal la-popup-tooltip la-delay'    // todo: remove la-delay
        @UIDoc(usage = 'Buttons/Links/Inputs (red) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed) and Tooltip (data-attributes needed)')
        public static String NEGATIVE_ICON_CONFIRM_TOOLTIP    = 'ui button negative icon la-modern-button js-open-confirm-modal la-popup-tooltip la-delay'    // todo: remove la-delay
    }

    class ICON {

        public static String SIMPLE         = 'ui button icon'          // merge with PRIMARY_ICON ?
        public static String PRIMARY        = 'ui button primary icon'
        public static String SECONDARY      = 'ui button secondary icon'
        public static String POSITIVE       = 'ui button positive icon'
        public static String NEGATIVE       = 'ui button negative icon'

//    @UIDoc(usage = 'Buttons/Links/Inputs (green) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed)')
//    public static String POSITIVE_CONFIRM  = 'ui button positive icon js-open-confirm-modal'
        @UIDoc(usage = 'Buttons/Links/Inputs (red) with Icon and Js: Confirmation Dialog (data-confirm-attributes needed)')
        public static String NEGATIVE_CONFIRM  = 'ui button negative icon js-open-confirm-modal'

        @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Js: Tooltip (data-attributes needed)')
        public static String SIMPLE_TOOLTIP    = 'ui button icon la-popup-tooltip la-delay'  // todo: remove la-delay
    }

    // --- WITHOUT ICONS ---

    public static String SIMPLE             = 'ui button'               // merge with PRIMARY ?
    public static String PRIMARY            = 'ui button primary'
    public static String SECONDARY          = 'ui button secondary'
    public static String POSITIVE           = 'ui button positive'
    public static String NEGATIVE           = 'ui button negative'

    @UIDoc(usage = 'Buttons/Links/Inputs (green) with Confirmation Dialog (data-confirm-attributes needed)')
    public static String POSITIVE_CONFIRM       = 'ui button positive js-open-confirm-modal'
    @UIDoc(usage = 'Buttons/Links/Inputs (red) with Confirmation Dialog (data-confirm-attributes needed)')
    public static String NEGATIVE_CONFIRM       = 'ui button negative js-open-confirm-modal'

    @UIDoc(usage = 'Buttons/Links/Inputs with Js: Click Control')
    public static String SIMPLE_CLICKCONTROL    = 'ui button js-click-control'
    @UIDoc(usage = 'Buttons/Links/Inputs (red) with Js: Click Control')
    public static String NEGATIVE_CLICKCONTROL  = 'ui button negative js-click-control'

    @UIDoc(usage = 'Buttons/Links/Inputs with Js: Tooltip (data-attributes needed)')
    public static String SIMPLE_TOOLTIP         = 'ui button la-popup-tooltip la-delay'  // todo: remove la-delay

}
