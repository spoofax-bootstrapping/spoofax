package org.metaborg.spoofax.core.syntax;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.messages.MessageUtils;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.Asfix2TreeBuilder;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.FilterException;
import org.spoofax.jsglr.client.SGLRParseResult;
import org.spoofax.jsglr.client.StartSymbolException;
import org.spoofax.jsglr.client.imploder.NullTokenizer;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.attachments.ParentTermFactory;

public class JSGLRI {
    private final IParserConfig config;
    private final ITermFactory termFactory;
    private final ILanguageImpl language;
    private final ILanguageImpl dialect;
    @Nullable private final FileObject resource;
    private final String input;

    private final SGLR parser;


    public JSGLRI(IParserConfig config, ITermFactory termFactory, ILanguageImpl language, ILanguageImpl dialect,
        @Nullable FileObject resource, String input) throws IOException {
        this.config = config;
        this.termFactory = termFactory;
        this.language = language;
        this.dialect = dialect;
        this.resource = resource;
        this.input = input;

        final TermTreeFactory factory = new TermTreeFactory(new ParentTermFactory(termFactory));
        this.parser = new SGLR(new TreeBuilder(factory), config.getParseTableProvider().parseTable());
    }


    public ParseContrib parse(@Nullable JSGLRParserConfiguration parserConfig) throws IOException {
        if(parserConfig == null) {
            parserConfig = new JSGLRParserConfiguration();
        }

        // NOTE: FileObject.getName().getPath() will not include the drive letter on Windows.
        // To fix this, use FileObject.getURL().getPath() instead.
        final String fileName = resource != null ? resource.getURL().getPath() : null;

        final JSGLRParseErrorHandler errorHandler =
            new JSGLRParseErrorHandler(this, resource, config.getParseTableProvider().parseTable().hasRecovers());

        final Timer timer = new Timer(true);
        SGLRParseResult result;
        try {
            result = actuallyParse(input, fileName, parserConfig);
        } catch(SGLRException | InterruptedException e) {
            result = null;
            errorHandler.setRecoveryFailed(parserConfig.recovery);
            errorHandler.processFatalException(new NullTokenizer(input, fileName), e);
        }
        final long duration = timer.stop();

        final IStrategoTerm ast;
        if(result != null) {
            ast = (IStrategoTerm) result.output;
            if(ast != null) {
                errorHandler.setRecoveryFailed(false);
                errorHandler.gatherNonFatalErrors(ast);
                if(resource != null) {
                    SourceAttachment.putSource(ast, resource);
                }
            }
        } else {
            ast = null;
        }

        final Iterable<IMessage> messages = errorHandler.messages();
        return new ParseContrib(ast != null, !MessageUtils.containsSeverity(messages, MessageSeverity.ERROR), ast,
            messages, duration);
    }

    public SGLRParseResult actuallyParse(String text, @Nullable String filename,
        @Nullable JSGLRParserConfiguration parserConfig) throws SGLRException, InterruptedException {
        if(!parserConfig.implode) {
            // GTODO: copied from existing code. Is this correct? Seems like this should be the tree builder when
            // implode is set to true. Also, there is no else branch.
            parser.setTreeBuilder(new Asfix2TreeBuilder(termFactory));
        }
        parser.setUseStructureRecovery(parserConfig.recovery);
        parser.setCompletionParse(parserConfig.completion, parserConfig.cursorPosition);
        parser.setTimeout(parserConfig.timeout);

        final Disambiguator disambiguator = parser.getDisambiguator();

        if(dialect != null) {
            disambiguator.setHeuristicFilters(true);
        } else {
            disambiguator.setHeuristicFilters(false);
        }

        String startSymbol = getOrDefaultStartSymbol(parserConfig);
        try {
            return parser.parse(text, filename, startSymbol);
        } catch(FilterException e) {
            if(e.getCause() == null && disambiguator.getFilterPriorities()) {
                disambiguator.setFilterPriorities(false);
                try {
                    return parser.parse(text, filename, startSymbol);
                } finally {
                    disambiguator.setFilterPriorities(true);
                }
            }
            throw e;
        } catch(StartSymbolException e) {
            // we don't want to allow any start symbol if it has been explicitly provided
            if(parserConfig != null && parserConfig.overridingStartSymbol != null) {
                throw e;
            }
            return parser.parse(text, filename, null);
        }
    }

    private String getOrDefaultStartSymbol(@Nullable JSGLRParserConfiguration parserConfig) {
        if(parserConfig != null && parserConfig.overridingStartSymbol != null) {
            return parserConfig.overridingStartSymbol;
        } else {
            return config.getStartSymbol();
        }
    }

    public IParserConfig getConfig() {
        return config;
    }

    public ILanguageImpl getLanguage() {
        return language;
    }

    public ILanguageImpl getDialect() {
        return dialect;
    }

    @Nullable public FileObject getResource() {
        return resource;
    }

    public String getInput() {
        return input;
    }

    protected SGLR getParser() {
        return parser;
    }
}
