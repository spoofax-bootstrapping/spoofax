package org.metaborg.spoofax.core.stratego.primitives;

import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class GenericPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    final String name;


    public GenericPrimitiveLibrary(Iterable<AbstractPrimitive> primitives, String name) {
        this.name = name;
        for(AbstractPrimitive primitive : primitives) {
            add(primitive);
        }
    }


    @Override public String getOperatorRegistryName() {
        return name;
    }
}
