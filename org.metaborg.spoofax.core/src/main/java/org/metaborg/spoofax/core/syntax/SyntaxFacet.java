package org.metaborg.spoofax.core.syntax;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.syntax.FenceCharacters;
import org.metaborg.core.syntax.MultiLineCommentCharacters;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

/**
 * Represents the syntax (or parsing) facet of a language.
 */
public class SyntaxFacet implements IFacet {
    private static final ILogger logger = LoggerUtils.logger(SyntaxFacet.class);

    public final FileObject parseTable;
    public final Iterable<String> startSymbols;
    public final Iterable<String> singleLineCommentPrefixes;
    public final Iterable<MultiLineCommentCharacters> multiLineCommentCharacters;
    public final Iterable<FenceCharacters> fenceCharacters;


    /**
     * Creates a syntax facet from a parse table provider and start symbols.
     * 
     * @param parseTable
     *            Parse table.
     * @param startSymbols
     *            Set of start symbols.
     */
    public SyntaxFacet(FileObject parseTable, Iterable<String> startSymbols) {
        this(parseTable, startSymbols, Iterables2.<String>empty(), Iterables2.<MultiLineCommentCharacters>empty(),
            Iterables2.<FenceCharacters>empty());
    }

    /**
     * Creates a syntax facet from syntax configuration.
     * 
     * @param parseTable
     *            Parse table.
     * @param startSymbols
     *            Set of start symbols.
     * @param singleLineCommentPrefixes
     *            Single line comment prefixes.
     * @param multiLineCommentCharacters
     *            Multi line comment characters.
     * @param fenceCharacters
     *            Fence characters.
     */
    public SyntaxFacet(FileObject parseTable, Iterable<String> startSymbols,
        Iterable<String> singleLineCommentPrefixes, Iterable<MultiLineCommentCharacters> multiLineCommentCharacters,
        Iterable<FenceCharacters> fenceCharacters) {
        this.parseTable = parseTable;
        this.startSymbols = startSymbols;
        this.singleLineCommentPrefixes = singleLineCommentPrefixes;
        this.multiLineCommentCharacters = multiLineCommentCharacters;
        this.fenceCharacters = fenceCharacters;
    }


    /**
     * Checks if the parse table file exists, returns errors if not.
     * 
     * @throws FileSystemException
     *             When an error occurs while checking if the parse table file exists.
     * @return Errors, or empty if there are no errors.
     */
    public Iterable<String> available() throws FileSystemException {
        if(!parseTable.exists()) {
            final String message = logger.format("Parse table {} does not exist", parseTable);
            return Iterables2.singleton(message);
        }
        return Iterables2.empty();
    }
}
