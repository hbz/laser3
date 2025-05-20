package de.laser.flexmark

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.data.MutableDataHolder
import org.jetbrains.annotations.NotNull

class BaseExtension implements HtmlRenderer.HtmlRendererExtension {

    static BaseExtension create() {
        return new BaseExtension()
    }

    @Override
    void rendererOptions(@NotNull MutableDataHolder options) {
        // add any configuration settings to options you want to apply to everything, here
    }

    @Override
    void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        htmlRendererBuilder.attributeProviderFactory(BaseAttributeProvider.Factory())
    }
}
