package net.kdt.pojavlaunch.authenticator.microsoft;

import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class MicrosoftBackgroundLogin {

    private static final String TAG = "MicrosoftBackgroundLogin";

    private static final String AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final boolean mIsRefresh;
    private final String mAuthCode;

    private final ExecutorService mExecutorService = PojavApplication.sExecutorService;

    private String msRefreshToken;
    private String mcName;
    private String mcToken;
    private String mcUuid;
    private boolean doesOwnGame;
    private long expiresAt;

    public MicrosoftBackgroundLogin(boolean isRefresh, String authCode) {
        mIsRefresh = isRefresh;
        mAuthCode = authCode;
    }

    public void performLogin(@Nullable final ProgressListener progressListener,
                             @Nullable final DoneListener doneListener,
                             @Nullable final ErrorListener errorListener) {
        mExecutorService.execute(() -> {
            try {
                if (progressListener != null) {
                    notifyProgress(progressListener, 1);
                }
                String accessToken = acquireAccessToken(mIsRefresh, mAuthCode);
                if (progressListener != null) {
                    notifyProgress(progressListener, 2);
                }
                String xboxLiveToken = acquireXBLToken(accessToken);
                if (progressListener != null) {
                    notifyProgress(progressListener, 3);
              
