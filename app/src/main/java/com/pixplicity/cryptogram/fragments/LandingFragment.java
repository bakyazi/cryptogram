package com.pixplicity.cryptogram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.HowToPlayActivity;
import com.pixplicity.cryptogram.activities.PuzzleActivity;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.providers.PuzzleProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.OnClick;


public class LandingFragment extends BaseFragment {

    @BindView(R.id.vg_help)
    protected ViewGroup mVgHelp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_landing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (PrefsUtils.getNeverShowHelp()) {
            mVgHelp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_landing, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_to: {
                new MaterialDialog.Builder(getContext())
                        .content(R.string.go_to_puzzle_content)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(null, null, (dialog, input) -> {
                            MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
                            try {
                                button.setEnabled(Integer.parseInt(input.toString()) > 0);
                            } catch (NumberFormatException ignored) {
                                button.setEnabled(false);
                            }
                        })
                        .alwaysCallInputCallback()
                        .showListener(dialogInterface -> {
                            MaterialDialog dialog = (MaterialDialog) dialogInterface;
                            //noinspection ConstantConditions
                            dialog.getInputEditText().selectAll();
                        })
                        .onPositive((dialog, which) -> {
                            //noinspection ConstantConditions
                            Editable input = dialog.getInputEditText().getText();
                            try {
                                int puzzleNumber = Integer.parseInt(input.toString());
                                PuzzleProvider provider = PuzzleProvider
                                        .getInstance(getContext());
                                Puzzle puzzle1 = provider.getByNumber(puzzleNumber);
                                if (puzzle1 == null) {
                                    showSnackbar(getString(R.string.puzzle_nonexistant, puzzleNumber));
                                } else {
                                    startActivity(PuzzleActivity.create(getContext()));
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vg_help)
    protected void onClickHelp() {
        startActivity(new Intent(getContext(), HowToPlayActivity.class));
    }

    @OnClick(R.id.bt_help_dismiss)
    protected void onClickHelpDismiss() {
        // Save preference
        PrefsUtils.setNeverShowHelp(true);
        // Animation is determined by `animateLayoutChanges` of view parent
        mVgHelp.setVisibility(View.GONE);
    }

}
