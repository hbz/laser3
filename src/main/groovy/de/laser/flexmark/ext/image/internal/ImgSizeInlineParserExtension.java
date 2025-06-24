package de.laser.flexmark.ext.image.internal;

import de.laser.flexmark.ext.image.ImgSize;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class ImgSizeInlineParserExtension implements InlineParserExtension {
    final public static Pattern IMAGE_PATTERN = Pattern.compile(
            "\\!\\[([^\\s\\]]*)]\\(([^\\s\\]]+)\\s*(\".*\")?\\s*([\\w\\+]*)?\\)",
            Pattern.CASE_INSENSITIVE
    );

    public ImgSizeInlineParserExtension(LightInlineParser inlineParser) {
    }

    @Override
    public void finalizeDocument(@NotNull InlineParser inlineParser) {
    }

    @Override
    public void finalizeBlock(@NotNull InlineParser inlineParser) {
    }

    @Override
    public boolean parse(@NotNull LightInlineParser inlineParser) {
        int index = inlineParser.getIndex();
        char c = inlineParser.getInput().safeCharAt(index + 1);
        if (c == '[') {
            BasedSequence[] matches = inlineParser.matchWithGroups(IMAGE_PATTERN);
            if (matches != null) {
                inlineParser.flushTextNode();

                BasedSequence alt    = matches[1] != null ? matches[1] : BasedSequence.NULL;
                BasedSequence source = matches[2] != null ? matches[2] : BasedSequence.NULL;
                BasedSequence title  = matches[3] != null ? matches[3] : BasedSequence.NULL;
                BasedSequence size   = matches[4] != null ? matches[4] : BasedSequence.NULL;

                ImgSize image = new ImgSize(alt, source, title, size);
                inlineParser.getBlock().appendChild(image);
                return true;
            }
        }
        return false;
    }

    public static class Factory implements InlineParserExtensionFactory {
        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @NotNull
        @Override
        public CharSequence getCharacters() {
            return "!";
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @NotNull
        @Override
        public InlineParserExtension apply(@NotNull LightInlineParser lightInlineParser) {
            return new ImgSizeInlineParserExtension(lightInlineParser);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }
}
