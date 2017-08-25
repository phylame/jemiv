package jem.format.epub;

import jclp.function.Provider;
import jclp.util.CollectionUtils;
import jclp.value.Lazy;
import jem.epm.Parser;
import jem.epm.impl.AbstractFactory;

import java.util.Set;

public class EpubFactory extends AbstractFactory {
    @Override
    public String getName() {
        return "ePub for Epm";
    }

    @Override
    public Set<String> getNames() {
        return CollectionUtils.setOf("epub");
    }

    @Override
    public boolean hasParser() {
        return true;
    }

    @Override
    public Parser getParser() {
        return parser.get();
    }

    private final Lazy<Parser> parser = new Lazy<>(new Provider<Parser>() {
        @Override
        public Parser provide() throws Exception {
            return new EpubParser();
        }
    });
}
