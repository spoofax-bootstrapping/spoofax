package org.metaborg.core.project.configuration;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.project.settings.ILegacyProjectSettings;

@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfig extends LegacyLanguageComponentConfig implements ILanguageSpecConfig {
    private static final long serialVersionUID = 4321718437339177753L;


    public LegacyLanguageSpecConfig(ILegacyProjectSettings settings) {
        super(settings);
    }


    @Override public String metaborgVersion() {
        return MetaborgConstants.METABORG_VERSION;
    }
}
