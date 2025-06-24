package de.laser.flexmark.ext.image.internal;

import de.laser.flexmark.ext.image.ImgSize;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ImgSizeNodeRenderer implements NodeRenderer
{
    public ImgSizeNodeRenderer(DataHolder options) {
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        // @formatter:off
        set.add(new NodeRenderingHandler<>(ImgSize.class, ImgSizeNodeRenderer.this::render));
        // @formatter:on
        return set;
    }

    public void render(ImgSize node, NodeRendererContext context, HtmlWriter html) {
        if (context.isDoNotRenderLinks()) {
            context.renderChildren(node);
        } else {
            ResolvedLink link = context.resolveLink(LinkType.IMAGE, node.getSource(), true);
            html.srcPos(node.getChars()).attr("src", link.getUrl());

            if (node.getAlt().isNotEmpty()){
                html.attr("alt", node.getAlt());
            }
            if (node.getTitle().isNotEmpty()){
                html.attr("title", node.getTitle().removePrefix("\"").removeSuffix("\""));
            }

            String cls = "ui image medium";
            if (node.getSize().isNotEmpty()) {
                if (node.getSize().endsWith("+")) {
                    cls = "ui image " + node.getSize().removeSuffix("+") + " la-preview";
                } else {
                    cls = "ui image " + node.getSize();
                }
            }
            html.attr("class", cls);

            html.withAttr().tag("img");
            html.tag("/img");
        }
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new ImgSizeNodeRenderer(options);
        }
    }
}
