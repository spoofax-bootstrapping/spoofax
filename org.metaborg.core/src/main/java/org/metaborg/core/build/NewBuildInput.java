package org.metaborg.core.build;

import com.google.common.collect.Multimap;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.transform.ITransformerGoal;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Input for a build. Use the {@link BuildInputBuilder} fluent interface to create objects of this class.
 * 
 * @see BuildInputBuilder
 */
@Deprecated
public class NewBuildInput {
    /**
     * Build state with information about previous builds.
     */
    public final BuildState state;

    /**
     * Language specification to build.
     */
    public final ILanguageSpec languageSpec;

    /**
     * Sources that have changed.
     */
    public final Iterable<ResourceChange> sourceChanges;

    /**
     * Per-language include paths;
     */
    public final Multimap<ILanguageImpl, FileObject> includePaths;

    /**
     * Language build order.
     */
    public final BuildOrder buildOrder;


    /**
     * File selector to determine which resources should be processed, or null to processed everything.
     */
    public final @Nullable FileSelector selector;


    /**
     * If analysis is enabled.
     */
    public final boolean analyze;

    /**
     * File selector to determine which resources should be analyzed, or null to analyze everything.
     */
    public final @Nullable FileSelector analyzeSelector;


    /**
     * If transformation is enabled.
     */
    public final boolean transform;

    /**
     * File selector to determine which resources should be transformed, or null to transform everything.
     */
    public final @Nullable FileSelector transformSelector;

    /**
     * Transformer goals to execute on analyzed or parsed results.
     */
    public final Iterable<ITransformerGoal> transformGoals;


    /**
     * Message printer to use during build, or null to skip printing messages.
     */
    public final @Nullable IBuildMessagePrinter messagePrinter;

    /**
     * If an exception should be thrown when there are parsing, analysis, or transformation errors.
     */
    public final boolean throwOnErrors;

    /**
     * Languages for which errors are pardoned; prevents throwing an exception when {@code throwOnErrors} is true.
     */
    public final Set<ILanguageImpl> pardonedLanguages;


    public NewBuildInput(BuildState state, ILanguageSpec languageSpec, Iterable<ResourceChange> resourceChanges,
                         Multimap<ILanguageImpl, FileObject> includePaths, BuildOrder buildOrder, @Nullable FileSelector parseSelector,
                         boolean analyze, FileSelector analyzeSelector, boolean transform, @Nullable FileSelector transformSelector,
                         Iterable<ITransformerGoal> transformGoals, @Nullable IBuildMessagePrinter messagePrinter,
                         boolean throwOnErrors, Set<ILanguageImpl> pardonedLanguages) {
        this.state = state;
        this.languageSpec = languageSpec;
        this.sourceChanges = resourceChanges;
        this.includePaths = includePaths;
        this.buildOrder = buildOrder;
        this.selector = parseSelector;
        this.analyze = analyze;
        this.analyzeSelector = analyzeSelector;
        this.transform = transform;
        this.transformSelector = transformSelector;
        this.transformGoals = transformGoals;
        this.messagePrinter = messagePrinter;
        this.throwOnErrors = throwOnErrors;
        this.pardonedLanguages = pardonedLanguages;
    }
}
