package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Btn {

    // <button/>, <input type="button"/>, <g:link/>, etc.

    class MODERN {

//        public static String PRIMARY            = 'ui button primary la-modern-button'
//        public static String SECONDARY          = 'ui button secondary la-modern-button'

        @UIDoc(usage = 'TODO: refactoring; la-modern-button does not work without declared color!')
        public static String SIMPLE             = 'ui button la-modern-button'
        public static String POSITIVE           = 'ui button positive la-modern-button'
        public static String NEGATIVE           = 'ui button negative la-modern-button'

        @UIDoc(usage = 'Buttons/Links/Inputs with Confirmation Dialog (data-confirm-attributes needed)')
        public static String NEGATIVE_CONFIRM   = 'ui button negative la-modern-button js-open-confirm-modal'

//        public static String PRIMARY_ICON       = 'ui button primary icon la-modern-button'
//        public static String SECONDARY_ICON     = 'ui button secondary icon la-modern-button'

        @UIDoc(usage = 'TODO: refactoring; la-modern-button does not work without declared color!')
        public static String SIMPLE_ICON        = 'ui button icon la-modern-button'
        public static String POSITIVE_ICON      = 'ui button positive icon la-modern-button'
        public static String NEGATIVE_ICON      = 'ui button negative icon la-modern-button'

        @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Confirmation Dialog (data-confirm-attributes needed)')
        public static String NEGATIVE_ICON_CONFIRM   = 'ui button negative icon la-modern-button js-open-confirm-modal'
    }

    public static String SIMPLE             = 'ui button'               // merge with PRIMARY ?
    public static String PRIMARY            = 'ui button primary'
    public static String SECONDARY          = 'ui button secondary'
    public static String POSITIVE           = 'ui button positive'
    public static String NEGATIVE           = 'ui button negative'

    @UIDoc(usage = 'Buttons/Links/Inputs with Js Click Control')
    public static String SIMPLE_CLICKCONTROL    = 'ui button js-click-control'
    @UIDoc(usage = 'Buttons/Links/Inputs with Confirmation Dialog (data-confirm-attributes needed)')
    public static String POSITIVE_CONFIRM       = 'ui button positive js-open-confirm-modal'
    @UIDoc(usage = 'Buttons/Links/Inputs with Confirmation Dialog (data-confirm-attributes needed)')
    public static String NEGATIVE_CONFIRM       = 'ui button negative js-open-confirm-modal'

    public static String SIMPLE_ICON        = 'ui button icon'          // merge with PRIMARY_ICON ?
    public static String PRIMARY_ICON       = 'ui button primary icon'
    public static String SECONDARY_ICON     = 'ui button secondary icon'
    public static String POSITIVE_ICON      = 'ui button positive icon'
    public static String NEGATIVE_ICON      = 'ui button negative icon'

    @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Confirmation Dialog (data-confirm-attributes needed)')
    public static String POSITIVE_ICON_CONFIRM   = 'ui button positive icon js-open-confirm-modal'
    @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Confirmation Dialog (data-confirm-attributes needed)')
    public static String NEGATIVE_ICON_CONFIRM   = 'ui button negative icon js-open-confirm-modal'
}
