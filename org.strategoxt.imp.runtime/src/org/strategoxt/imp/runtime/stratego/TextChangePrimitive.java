package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import lpg.runtime.ILexStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;
import org.strategoxt.lang.terms.StrategoInt;
import org.strategoxt.lang.terms.StrategoTuple;

/**
 * Applies a text-change in the current document
 * Arguments:
 * a. (AstNode, "textfragment") of 
 * b. ((offset, end-offset), "textfragment") of
 * c. ((l, c, end-l, end-c), "textfragment")
 * @author Maartje de Jonge
 */
public class TextChangePrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_text_change";

	public TextChangePrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (tvars.length!=2 || !isTermString(tvars[1]))
			return false;
		if(!(tvars[0] instanceof IWrappedAstNode || islocationTuple(tvars[0])))
			return false;
		EditorState editor = EditorState.getActiveEditor();
		ILexStream lexStream= editor.getParseController().getCurrentAst().getLeftIToken().getILexStream();
		int position_start = -1;
		int position_end = -1;
		if(tvars[0] instanceof IWrappedAstNode){
			position_start=TextPositions.getStartPosNode(((IWrappedAstNode)tvars[0]).getNode());
			position_end=TextPositions.getEndPosNode(((IWrappedAstNode)tvars[0]).getNode());
		}
		else{
			StrategoTuple tuple=(StrategoTuple)tvars[0];
			if(tuple.size()==2){
				position_start=((StrategoInt)tuple.get(0)).intValue();
				position_end=((StrategoInt)tuple.get(1)).intValue()-1; //exclusive end pos
			}
			if(tuple.size()==4){
				int line_start=((StrategoInt)tuple.get(0)).intValue()-1;
				int col_start=((StrategoInt)tuple.get(1)).intValue();
				int line_end=((StrategoInt)tuple.get(2)).intValue()-1;
				int col_end=((StrategoInt)tuple.get(3)).intValue();
				position_start=lexStream.getLineOffset(line_start)+col_start; //FIXME deal with bad location
				position_end=lexStream.getLineOffset(line_end)+col_end+1;
			}
		}
		String text = ((IStrategoString)tvars[1]).stringValue();
		try {
			IDocument doc = editor.getDocument();
			if(TextPositions.isUnvalidInterval(position_start, position_end, lexStream))
				return false;
			doc.replace(position_start, position_end-position_start, text);
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean islocationTuple(IStrategoTerm term) {
		if (!(term instanceof StrategoTuple))
			return false;
		StrategoTuple tuple=(StrategoTuple)term;
		if(tuple.size()!=2 && tuple.size()!=4)
			return false;
		for (int i = 0; i < tuple.size(); i++) {
			if(!(tuple.get(i) instanceof StrategoInt))
				return false;
		}
		return true;
	}
}