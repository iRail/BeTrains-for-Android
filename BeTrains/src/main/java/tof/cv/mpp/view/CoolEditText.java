package tof.cv.mpp.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;


public class CoolEditText extends EditText {
    public CoolEditText(Context context) {
        super(context);
    }

    public CoolEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoolEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String[]{"image/gif"});
        Log.e("CVE","onCreateInputConnection");
        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        Log.e("CVE","onCommitContent");
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            } catch (Exception e) {
                                return false; // return false if failed
                            }
                        }
                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }
}
