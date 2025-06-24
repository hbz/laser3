package de.laser.flexmark.ext.image;

import com.vladsch.flexmark.util.ast.VisitHandler;

public class ImgSizeVisitorExt {
    public static <V extends ImgSizeVisitor> VisitHandler<?>[] VISIT_HANDLERS(V visitor) {
        return new VisitHandler<?>[] {
                new VisitHandler<>(ImgSize.class, visitor::visit),
        };
    }
}
