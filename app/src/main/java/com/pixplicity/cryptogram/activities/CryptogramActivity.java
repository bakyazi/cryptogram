package com.pixplicity.cryptogram.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.CryptogramAdapter;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.views.CryptogramView;

import butterknife.BindView;

public class CryptogramActivity extends BaseActivity {

    private static final String TAG = CryptogramActivity.class.getSimpleName();

    @BindView(R.id.vg_cryptogram)
    protected ViewGroup mVgCryptogram;

    @BindView(R.id.tv_author)
    protected TextView mTvAuthor;

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.tv_error)
    protected TextView mTvError;

    @BindView(R.id.rv_drawer)
    protected RecyclerView mRvDrawer;

    private CryptogramAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);

        mRvDrawer.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CryptogramAdapter(this, new CryptogramAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mDrawerLayout.closeDrawers();
                updateCryptogram(cryptogramProvider.get(position));
            }
        });
        mRvDrawer.setAdapter(mAdapter);

        updateCryptogram(cryptogramProvider.getCurrent());
    }

    private void updateCryptogram(Cryptogram cryptogram) {
        if (cryptogram != null) {
            CryptogramProvider provider = CryptogramProvider.getInstance(this);
            provider.setCurrent(cryptogram.getId());
            mRvDrawer.smoothScrollToPosition(
                    provider.getCurrentIndex());
            mAdapter.notifyDataSetChanged();
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            mCryptogramView.setCryptogram(cryptogram);
            mTvAuthor.setText(getString(R.string.quote, cryptogram.getAuthor()));
            mToolbar.setSubtitle(getString(
                    R.string.puzzle_number,
                    cryptogram.getId() + 1,
                    provider.getCount()));
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cryptogram, menu);
        {
            MenuItem item = menu.findItem(R.id.action_randomize);
            item.setChecked(PrefsUtils.getRandomize());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        final Cryptogram cryptogram = mCryptogramView.getCryptogram();
        switch (item.getItemId()) {
            case R.id.action_next: {
                if (cryptogram == null || cryptogram.isCompleted()) {
                    nextPuzzle();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.skip_puzzle)
                            .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    nextPuzzle();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reveal: {
                if (cryptogram == null || !mCryptogramView.hasSelectedCharacter()) {
                    Snackbar.make(mVgContent, "Please select a letter first.", Snackbar.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reveal_confirmation)
                            .setPositiveButton(R.string.reveal, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mCryptogramView.revealCharacterMapping(
                                            mCryptogramView.getSelectedCharacter())) {
                                        // Answer revealed; clear the selection
                                        mCryptogramView.setSelectedCharacter((char) 0);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reset: {
                if (cryptogram != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reset_puzzle)
                            .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    cryptogram.reset();
                                    mCryptogramView.invalidate();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_go_to: {
                String currentId = String.valueOf(cryptogram.getId() + 1);
                new MaterialDialog.Builder(this)
                        .content(R.string.go_to_puzzle_content)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(null, currentId, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
                                try {
                                    //noinspection ResultOfMethodCallIgnored
                                    Integer.parseInt(input.toString());
                                    button.setEnabled(true);
                                } catch (NumberFormatException ignored) {
                                    button.setEnabled(false);
                                }
                            }
                        })
                        .alwaysCallInputCallback()
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                MaterialDialog dialog = (MaterialDialog) dialogInterface;
                                //noinspection ConstantConditions
                                dialog.getInputEditText().selectAll();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //noinspection ConstantConditions
                                Editable input = dialog.getInputEditText().getText();
                                try {
                                    int id = Integer.parseInt(input.toString());
                                    CryptogramProvider provider = CryptogramProvider
                                            .getInstance(CryptogramActivity.this);
                                    Cryptogram cryptogram = provider.get(id - 1);
                                    if (cryptogram == null) {
                                        Snackbar.make(mVgContent, getString(R.string.puzzle_nonexistant, id),
                                                Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        updateCryptogram(cryptogram);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }).show();
            }
            return true;
            case R.id.action_randomize: {
                boolean randomize = !item.isChecked();
                PrefsUtils.setRandomize(randomize);
                item.setChecked(randomize);
            }
            return true;
            case R.id.action_about: {
                startActivity(AboutActivity.create(this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextPuzzle() {
        Cryptogram cryptogram = CryptogramProvider.getInstance(CryptogramActivity.this).getNext();
        updateCryptogram(cryptogram);
    }

}
