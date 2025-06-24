package de.laser.flexmark.ext.image;

import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class ImgSize extends Node implements DoNotDecorate {
    protected BasedSequence alt = BasedSequence.NULL;
    protected BasedSequence source = BasedSequence.NULL;
    protected BasedSequence title = BasedSequence.NULL;
    protected BasedSequence size = BasedSequence.NULL;

    @NotNull
    @Override
    public BasedSequence[] getSegments() {
        return new BasedSequence[] { alt, source, title, size };
    }

    public ImgSize(BasedSequence alt, BasedSequence source, BasedSequence title, BasedSequence size) {
        super(spanningChars(alt, source, title, size));
        this.alt = alt;
        this.source = source;
        this.title = title;
        this.size = size;
    }

    public BasedSequence getAlt() {
        return alt;
    }

    public BasedSequence getSource() {
        return source;
    }

    public BasedSequence getTitle() {
        return title;
    }

    public BasedSequence getSize() {
        return size;
    }
}
