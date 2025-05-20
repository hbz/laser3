package de.laser.flexmark

import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.LinkRef
import com.vladsch.flexmark.ast.Reference
import com.vladsch.flexmark.html.AttributeProvider
import com.vladsch.flexmark.html.AttributeProviderFactory
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory
import com.vladsch.flexmark.html.renderer.AttributablePart
import com.vladsch.flexmark.html.renderer.LinkResolverContext
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.html.MutableAttributes
import org.jetbrains.annotations.NotNull

class BaseAttributeProvider implements AttributeProvider {

    static AttributeProviderFactory Factory() {
        return new IndependentAttributeProviderFactory() {
            @NotNull
            @Override
            AttributeProvider apply(@NotNull LinkResolverContext context) {
                return new BaseAttributeProvider()
            }
        };
    }

    @Override
    void setAttributes(@NotNull Node node, @NotNull AttributablePart attributablePart, @NotNull MutableAttributes mutableAttributes) {
//        println ''
//        println node
//        println attributablePart.getName()
//        println mutableAttributes


//        if (node instanceof Heading) {
//            mutableAttributes.replaceValue('class', 'ui header')
//        }
//        else if (node instanceof BulletList) {
//            mutableAttributes.replaceValue('class', 'ui bulleted list')
//        }
//        else if (node instanceof BulletListItem) {
//            mutableAttributes.replaceValue('class', 'item')
//        }
//        else if (node instanceof OrderedList) {
//            mutableAttributes.replaceValue('class', 'ui ordered list')
//        }
//        else if (node instanceof OrderedListItem) {
//            mutableAttributes.replaceValue('class', 'item')
//        }
//        else
        if (node instanceof Link && attributablePart == AttributablePart.LINK) {
            mutableAttributes.replaceValue('target', '_blank')
        }
        else if (node instanceof AutoLink && attributablePart == AttributablePart.LINK) {
            mutableAttributes.replaceValue('target', '_blank')
        }
        else if (node instanceof LinkRef && attributablePart == AttributablePart.LINK) {
            mutableAttributes.replaceValue('target', '_blank')
        }
        else if (node instanceof Reference && attributablePart == AttributablePart.LINK) {
            mutableAttributes.replaceValue('target', '_blank')
        }
    }
}