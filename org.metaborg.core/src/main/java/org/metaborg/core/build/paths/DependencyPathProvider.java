package org.metaborg.core.build.paths;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.core.project.IProject;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DependencyPathProvider implements ILanguagePathProvider {
    private final IDependencyService dependencyService;


    @Inject public DependencyPathProvider(IDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }


    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.compileDependencies(project);
        final Collection<FileObject> sources = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Iterable<LanguagePathFacet> facets = dependency.facets(LanguagePathFacet.class);
            for(LanguagePathFacet facet : facets) {
                final Collection<String> paths = facet.sources.get(languageName);
                if(paths != null) {
                    resolve(project.location(), paths, sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.runtimeDependencies(project);
        final Collection<FileObject> includes = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Iterable<FacetContribution<LanguagePathFacet>> facets =
                dependency.facetContributions(LanguagePathFacet.class);
            for(FacetContribution<LanguagePathFacet> facetContribution : facets) {
                final Collection<String> paths = facetContribution.facet.includes.get(languageName);
                if(paths != null) {
                    resolve(facetContribution.contributor.location(), paths, includes);
                }
            }

            // HACK: transitive dependencies do not work with Maven, disable them for now.
            // final Iterable<ILanguageComponent> transitiveDeps = dependencyService.compileDependencies(dependency);
            // for(ILanguageComponent transitiveDep : transitiveDeps) {
            // final Iterable<FacetContribution<LanguagePathFacet>> transFacets =
            // transitiveDep.facetContributions(LanguagePathFacet.class);
            // for(FacetContribution<LanguagePathFacet> facetContribution : transFacets) {
            // final Collection<String> paths = facetContribution.facet.sources.get(languageName);
            // if(paths != null) {
            // resolve(dependency.location(), paths, includes);
            // }
            // }
            // }
        }


        return includes;
    }


    private void resolve(FileObject basedir, @Nullable Collection<String> paths, Collection<FileObject> filesToAppend) {
        if(paths != null) {
            for(String path : paths) {
                try {
                    filesToAppend.add(basedir.resolveFile(path));
                } catch(FileSystemException ex) {
                    throw new MetaborgRuntimeException(ex);
                }
            }
        }
    }
}
