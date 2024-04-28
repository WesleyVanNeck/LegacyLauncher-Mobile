package com.kdt.mcgui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.authenticator.microsoft.PresentedException;
import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class McAccountSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {
    private static final int MAX_LOGIN_STEP = 5;
    private static final int LOGIN_BAR_ALPHA = 255;
    private static final int LOGIN_BAR_WIDTH_ANIMATION_DURATION = 200;

    private final List<String> mAccountList = List.of("Add account", "Existing account");
    private MinecraftAccount mSelectedAccount = null;
    private BitmapDrawable mHeadDrawable;
    private ObjectAnimator mLoginBarAnimator;
    private float mLoginBarWidth = -1;
    private int mLoginStep = 0;
    private final Paint mLoginBarPaint = new Paint();

    public McAccountSpinner(@NonNull Context context) {
        this(context, null);
    }

    public McAccountSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundColor(getResources().getColor(R.color.background_status_bar));
        mLoginBarPaint.setColor(getResources().getColor(R.color.minebutton_color));
        mLoginBarPaint.setAlpha(0);
        mLoginBarPaint.setStrokeWidth(getResources().getDimensionPixelOffset(R.dimen._2sdp));

        reloadAccounts(true, 0);
        setOnItemSelectedListener(this);

        ExtraCore.addExtraListener(ExtraConstants.MOJANG_LOGIN_TODO, mMojangLoginListener);
        ExtraCore.addExtraListener(ExtraConstants.MICROSOFT_LOGIN_TODO, mMicrosoftLoginListener);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return;
        }

        pickAccount(position);
        if (mSelectedAccount != null) {
            performLogin(mSelectedAccount);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLoginBarWidth == -1) {
            mLoginBarWidth = getWidth(); // Initial draw
        }

        float bottom = getHeight() - mLoginBarPaint.getStrokeWidth() / 2f;
        canvas.drawRect(0, bottom, mLoginBarWidth, bottom + mLoginBarPaint.getStrokeWidth(), mLoginBarPaint);
    }

    public void removeCurrentAccount() {
        int position = getSelectedItemPosition();
        if (position == 0) return;

        File accountFile = new File(Tools.DIR_ACCOUNT_NEW, mAccountList.get(position) + ".json");
        if (accountFile.exists()) {
            accountFile.delete();
        }

        mAccountList.remove(position);
        reloadAccounts(false, 0);
    }

    @Keep
    public void setLoginBarWidth(float value) {
        mLoginBarWidth = value;
        invalidate(); // Need to redraw each time this is changed
    }

    public boolean isAccountOnline() {
        return mSelectedAccount != null && !mSelectedAccount.accessToken.equals("0");
    }

    public MinecraftAccount getSelectedAccount() {
        return mSelectedAccount;
    }

    public int getLoginState() {
        return mLoginStep;
    }

    public boolean isLoginDone() {
        return mLoginStep >= MAX_LOGIN_STEP;
    }

    private void setNoAccountBehavior() {
        if (mAccountList.size() != 1) {
            setOnTouchListener(null);
            return;
        }

        setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return false;
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return true;
        });
    }

    private void reloadAccounts(boolean fromFiles, int overridePosition) {
        if (fromFiles) {
            mAccountList.clear();

            mAccountList.add("Add account");
            File accountFolder = new File(Tools.DIR_ACCOUNT_NEW);
            if (accountFolder.exists()) {
                for (String fileName : accountFolder.list()) {
                    mAccountList.add(fileName.substring(0, fileName.length() - 5));
                }
            }
        }

        AccountAdapter accountAdapter = new AccountAdapter(getContext(), R.layout.item_minecraft_account, mAccountList);
        accountAdapter.setDropDownViewResource(R.layout.item_minecraft_account);
        setAdapter(accountAdapter);

        pickAccount(overridePosition == 0 ? -1 : overridePosition);
        if (mSelectedAccount != null) {
            performLogin(mSelectedAccount);
        }

        setNoAccountBehavior();
    }

    private void performLogin(MinecraftAccount minecraftAccount) {
        if (minecraftAccount.isLocal()) return;

        mLoginBarPaint.setColor(getResources().getColor(R.color.minebutton_color));
        if (minecraftAccount.isMicrosoft) {
            if (System.currentTimeMillis() > minecraftAccount.expiresAt) {
                new MicrosoftBackgroundLogin(true, minecraftAccount.msaRefreshToken)
                        .performLogin(mProgressListener, mDoneListener, mErrorListener);
            }
            return;
        }
    }

    private void pickAccount(int position) {
        MinecraftAccount selectedAccount;
        if (position != -1) {
            PojavProfile.setCurrentProfile(getContext(), mAccountList.get(position));
            selectedAccount = PojavProfile.getCurrentProfileContent(getContext(), mAccountList.get(position));

            if (selectedAccount == null) {
                removeCurrentAccount();
                pickAccount(-1);
                setSelection(0);
                return;
            }

            setSelection(position);
        } else {
            selectedAccount = PojavProfile.getCurrentProfileContent(getContext(), null);
            int spinnerPosition = selectedAccount == null
                    ? mAccountList.size() <= 1 ? 0 : 1
                    : mAccountList.indexOf(selectedAccount.username);
            setSelection(spinnerPosition, false);
        }

        mSelectedAccount = selectedAccount;
        setImageFromSelectedAccount();
    }

    private void setImageFromSelectedAccount() {
        if (mSelectedAccount != null) {
            ExtendedTextView textview = (ExtendedTextView) getSelectedView();
            if (textview != null) {
                Bitmap bitmap = mSelectedAccount.getSkinFace();
                if (bitmap != null) {
                    mHeadDrawable = new BitmapDrawable(getResources(), bitmap);
                    textview.setCompoundDrawables(mHeadDrawable, null, null, null);
                } else {
                    textview.setCompoundDrawables(null, null, null, null);
                }
                textview.postProcessDrawables();
            }
        }
    }

    private static class AccountAdapter extends ArrayAdapter<String> {

        private final Map<String, Drawable> mImageCache = new HashMap<>();

        public AccountAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minecraft_account, parent, false);
            }
            ExtendedTextView textview = (ExtendedTextView) convertView;
            textview.setText(super.getItem(position));

            if (position == 0) {
                textview.setCompoundDrawables(ResourcesCompat.getDrawable(parent.getResources(), R.drawable.ic_add, null), null, null, null);
            } else {
                String username = super.getItem(position);
                Drawable accountHead = mImageCache.get(username);
                if (accountHead == null) {
                    accountHead = new BitmapDrawable(parent.getResources(), MinecraftAccount.getSkinFace(username));
                    mImageCache.put(username, accountHead);
                }
                textview.setCompoundDrawables(accountHead, null, null, null);
            }

            return convertView;
        }
    }
}
