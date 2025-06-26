package de.laser.flexmark.ext.image;

import de.laser.flexmark.ext.image.internal.ImgSizeInlineParserExtension;
import de.laser.flexmark.ext.image.internal.ImgSizeNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import org.jetbrains.annotations.NotNull;

public class ImgSizeExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private ImgSizeExtension() {
    }

    public static ImgSizeExtension create() {
        return new ImgSizeExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(new ImgSizeInlineParserExtension.Factory());
    }

    @Override
    public void extend(@NotNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new ImgSizeNodeRenderer.Factory());
        } else if (htmlRendererBuilder.isRendererType("JIRA")) {
            return;
        }
    }
}
