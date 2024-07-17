package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Button {

    // <button/>, <input type="button"/>, <g:link/>, etc.

    class MODERN {

//        public static String PRIMARY            = 'ui button primary la-modern-button'
//        public static String SECONDARY          = 'ui button secondary la-modern-button'
        public static String BLUE               = 'ui button blue la-modern-button'

        public static String POSITIVE           = 'ui button positive la-modern-button'
        public static String NEGATIVE           = 'ui button negative la-modern-button'

//        public static String PRIMARY_ICON       = 'ui button primary icon la-modern-button'
//        public static String SECONDARY_ICON     = 'ui button secondary icon la-modern-button'
        public static String BLUE_ICON          = 'ui button blue icon la-modern-button'

        public static String POSITIVE_ICON      = 'ui button positive icon la-modern-button'
        public static String NEGATIVE_ICON      = 'ui button negative icon la-modern-button'
    }

    public static String BASIC              = 'ui button'
    public static String BASIC_ICON         = 'ui button icon'

    public static String PRIMARY            = 'ui button primary'
    public static String SECONDARY          = 'ui button secondary'
    public static String POSITIVE           = 'ui button positive'
    public static String NEGATIVE           = 'ui button negative'

    @UIDoc(usage = 'Buttons/Links/Inputs with Confirmation Dialog (data-confirm-attributes needed)')
    public static String NEGATIVE_CONFIRM   = 'ui button negative js-open-confirm-modal'

    public static String PRIMARY_ICON       = 'ui button primary icon'
    public static String SECONDARY_ICON     = 'ui button secondary icon'
    public static String POSITIVE_ICON      = 'ui button positive icon'
    public static String NEGATIVE_ICON      = 'ui button negative icon'

    @UIDoc(usage = 'Buttons/Links/Inputs with Icon and Confirmation Dialog (data-confirm-attributes needed)')
    public static String NEGATIVE_ICON_CONFIRM   = 'ui button negative icon js-open-confirm-modal'
}
