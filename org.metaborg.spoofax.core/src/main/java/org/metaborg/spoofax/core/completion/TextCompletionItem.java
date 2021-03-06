package org.metaborg.spoofax.core.completion;

import org.metaborg.core.completion.ITextCompletionItem;


public class TextCompletionItem implements ITextCompletionItem {
    public final String text;


    public TextCompletionItem(String text) {
        this.text = text;
    }


    @Override public String text() {
        return text;
    }
    
    
    @Override public String toString() {
        return text;
    }
}