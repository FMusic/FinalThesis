package fm.pathfinder.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class InputDialogFragment extends DialogFragment {
    private String title = "";
    public InputDialogFragment setTitle(String title){
        this.title = title;
        return this;
    }
    private OnInputCompleteListener listener;

    // Interface for communicating the input to the calling activity
    public interface OnInputCompleteListener {
        void onInputComplete(String inputText);
    }

    // Set the listener for input completion
    public InputDialogFragment setOnInputCompleteListener(OnInputCompleteListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        // Create the EditText view
        final EditText inputEditText = new EditText(getActivity());
        builder.setView(inputEditText);

        // Set up the button action
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Retrieve the entered text from the EditText view
            String inputText = inputEditText.getText().toString();
            if (listener != null) {
                // Call the listener with the entered text
                listener.onInputComplete(inputText);
            }
        });

        // Create and return the dialog
        return builder.create();
    }
}
