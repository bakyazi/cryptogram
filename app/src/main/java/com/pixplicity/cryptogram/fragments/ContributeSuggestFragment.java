package com.pixplicity.cryptogram.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.SubmissionEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.Database;
import com.pixplicity.cryptogram.utils.EventProvider;

import butterknife.BindView;


public class ContributeSuggestFragment extends BaseFragment {

    private static final String TAG = ContributeSuggestFragment.class.getSimpleName();

    @BindView(R.id.vg_root)
    protected ViewGroup mVgRoot;

    @BindView(R.id.et_text)
    protected EditText mEtText;

    @BindView(R.id.et_author)
    protected EditText mEtAuthor;

    @BindView(R.id.et_topic)
    protected EditText mEtTopic;

    @BindView(R.id.cb_explicit)
    protected CheckBox mCbExplicit;

    public static Fragment create() {
        return new ContributeSuggestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contribute_suggest, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_suggest, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit: {
                onSubmit();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSubmit() {
        String text = mEtText.getText().toString();
        if (!confirmLength(text, "Puzzle text", 10, 200)) {
            return;
        }
        String author = mEtAuthor.getText().toString();
        if (!confirmLength(author, "Puzzle author", 3, 40)) {
            return;
        }
        String topic = mEtTopic.getText().toString();
        if (!confirmLength(topic, "Puzzle topic", 3, 40)) {
            return;
        }
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setIndeterminate(true);
        pd.setMessage(getString(R.string.submitting_puzzle));
        pd.show();
        Puzzle puzzle = new Puzzle.Suggestion(
                text,
                author,
                topic,
                mCbExplicit.isChecked());
        Database.getInstance()
                .getSuggestions()
                .push()
                .setValue(puzzle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        EventProvider.postEvent(new SubmissionEvent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        showError(e.getMessage());
                    }
                });
    }

    private boolean confirmLength(String text, String fieldName, int min, int max) {
        if (text.length() < min) {
            showError(fieldName + " must be at least " + min + " characters.");
            return false;
        }
        if (text.length() > max) {
            showError(fieldName + " must be at most " + max + " characters.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Snackbar.make(mVgRoot, message, Snackbar.LENGTH_SHORT).show();
    }

}
