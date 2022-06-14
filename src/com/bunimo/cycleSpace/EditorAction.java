package com.bunimo.cycleSpace;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;

import java.util.List;

public class EditorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (editor == null || !EditorModificationUtil.checkModificationAllowed(editor)) {
            return;
        }

        JustOneSpaceAt(editor);
    }

    private void JustOneSpaceAt(Editor editor) {
        List<Caret> allCarets = editor.getCaretModel().getAllCarets();

        for (Caret caret : Lists.reverse(allCarets)) {
            int offset = caret.getOffset();
            int begin  = beginOffset(offset, editor.getDocument());
            int end    = endOffset(offset, editor.getDocument());

            if (end > begin - 1) {
                makeOneSpaceBetween(editor, begin, end);
            }
        }
    }

    private void makeZeroSpaceBetween(Editor editor, final int begin, final int end) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                editor.getSelectionModel().setSelection(begin, end);

                EditorModificationUtil.deleteSelectedText(editor);
            }
        };
        AppUtil.runWriteAction(runnable, editor);
    }


    private void makeOneSpaceBetween(Editor editor, final int begin, final int end) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                editor.getSelectionModel().setSelection(begin, end);
                EditorModificationUtil.deleteSelectedText(editor);
                EditorModificationUtil.insertStringAtCaret(editor, " ");
            }
        };
        AppUtil.runWriteAction(runnable, editor);
    }

    private int beginOffset(final int offset, Document document) {
        String doc = document.getText();

        int i = offset;
        int startOfLine = startOfLineOffset(offset, document);

        while (i > startOfLine && Character.isWhitespace(doc.charAt(i - 1)))  {
            i--;
        }
        return i;
    }

    private int endOffset(int offset, Document document) {
        String doc = document.getText();
        int endOfLine = endOfLineOffset(offset, document);

        int i = offset;
        while (i < endOfLine && Character.isWhitespace(doc.charAt(i))) {
            i++;
        }
        return i;
    }

    private int startOfLineOffset(int offset, Document document) {
        return document.getLineStartOffset(document.getLineNumber(offset));
    }

    private int endOfLineOffset(int offset, Document document) {
        int currentLineNumber = document.getLineNumber(offset);
        int startOfLine = document.getLineStartOffset(currentLineNumber + 1);
        int maxOffset = document.getText().length();

        return Math.min(startOfLine - 1, maxOffset);
    }

}
