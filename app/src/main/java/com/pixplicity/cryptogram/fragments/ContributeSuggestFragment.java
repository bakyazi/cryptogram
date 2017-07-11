package com.pixplicity.cryptogram.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.SimpleAdapter;
import com.pixplicity.cryptogram.events.SubmissionEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.Database;
import com.pixplicity.cryptogram.utils.EventProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    @BindView(R.id.sp_language)
    protected Spinner mSpLanguage;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Locale> locales = new ArrayList<>();
        locales.add(Locale.getDefault());
        final SimpleAdapter<Locale> adapter = new SimpleAdapter<Locale>(locales) {
            @Override
            public String getText(int position) {
                return getItem(position).getDisplayName();
            }
        };
        mSpLanguage.setAdapter(adapter);
        mSpLanguage.setSelection(0);
        new AsyncTask<Void, Void, List<Locale>>() {
            public int mPosition;
            Comparator<Locale> mComparator = new Comparator<Locale>() {
                @Override
                public int compare(Locale l1, Locale l2) {
                    return l1.getDisplayName().compareTo(l2.getDisplayName());
                }
            };

            @Override
            protected List<Locale> doInBackground(Void... voids) {
                List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
                Collections.sort(locales, mComparator);
                mPosition = Collections.binarySearch(locales, Locale.getDefault(), mComparator);
                return locales;
            }

            @Override
            protected void onPostExecute(List<Locale> locales) {
                adapter.setItems(locales);
                mSpLanguage.setSelection(mPosition);
            }
        }.execute();
        mSpLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Locale item = adapter.getItem(position);
                String localeString = item.getLanguage();
                String country = item.getCountry();
                if (!TextUtils.isEmpty(country)) {
                    localeString += "-r" + country;
                }
                Toast.makeText(getContext(), localeString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
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
        if (!confirmLength(text, "puzzle text", 10, 200)) {
            return;
        }
        String author = mEtAuthor.getText().toString();
        if (!confirmLength(author, "puzzle author", 3, 40)) {
            return;
        }
        String topic = mEtTopic.getText().toString();
        if (!confirmLength(topic, "puzzle topic", 3, 40)) {
            return;
        }
        // TODO
        String language = null;
        // Show progress
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setIndeterminate(true);
        pd.setMessage(getString(R.string.submitting_puzzle));
        pd.setCancelable(false);
        pd.show();
        // Create the puzzle
        Puzzle puzzle = new Puzzle.Suggestion(
                text,
                author,
                topic,
                language,
                mCbExplicit.isChecked());
        // Submit it
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
            showError(getString(R.string.error_min_characters, fieldName, min));
            return false;
        }
        if (text.length() > max) {
            showError(getString(R.string.error_max_characters, fieldName, min));
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Snackbar.make(mVgRoot, message, Snackbar.LENGTH_SHORT).show();
    }

}
