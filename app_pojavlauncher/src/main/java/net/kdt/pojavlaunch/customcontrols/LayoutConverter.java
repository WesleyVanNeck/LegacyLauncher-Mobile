package net.kdt.pojavlaunch.customcontrols;

import com.google.gson.JsonSyntaxException;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LayoutConverter {
    public static CustomControls loadAndConvertIfNecessary(String jsonPath) throws IOException, JsonSyntaxException {
        String jsonLayoutData = Tools.read(jsonPath);
        try {
            JSONObject layoutJobj = new JSONObject(jsonLayoutData);
            return convertLayout(layoutJobj);
        } catch (JSONException e) {
            throw new JsonSyntaxException("Failed to load", e);
        }
    }

    private static CustomControls convertLayout(JSONObject layoutJobj) throws JSONException {
        if (!layoutJobj.has("version")) { //v1 layout
            return convertV1Layout(layoutJobj);
        } else if (layoutJobj.getInt("version") == 2) {
            return convertV2Layout(layoutJobj);
        } else if (layoutJobj.getInt("version") >= 3 && layoutJobj.getInt("version") <= 5) {
            return convertV3_4Layout(layoutJobj);
        } else if (layoutJobj.getInt("version") == 6) {
            return Tools.GLOBAL_GSON.fromJson(layoutJobj.toString(), CustomControls.class);
        } else {
            throw new JsonSyntaxException("Unsupported layout version: " + layoutJobj.getInt("version"));
        }
    }

    /**
     * Normalize the layout to v6 from v3/4: The stroke width is no longer dependant on the button size
     */
    private static CustomControls convertV3_4Layout(JSONObject oldLayoutJson) {
        CustomControls layout = Tools.GLOBAL_GSON.fromJson(oldLayoutJson.toString(), CustomControls.class);
        convertStrokeWidth(layout);
        layout.version = 6;
        return layout;
    }

    public static CustomControls convertV2Layout(JSONObject oldLayoutJson) throws JSONException {
        CustomControls layout = Tools.GLOBAL_GSON.fromJson(oldLayoutJson.toString(), CustomControls.class);
        convertDynamicPositions(oldLayoutJson, layout);
        convertStrokeWidth(layout);
        layout.version = 3;
        return layout;
    }

    public static CustomControls convertV1Layout(JSONObject oldLayoutJson) throws JSONException {
        CustomControls empty = new CustomControls();
        convertControlDataList(oldLayoutJson.getJSONArray("mControlDataList"), empty);
        empty.scaledAt = (float) oldLayoutJson.getDouble("scaledAt");
        empty.version = 3;
        return empty;
    }

    private static void convertControlDataList(JSONArray layoutMainArray, CustomControls layout) throws JSONException {
        for (int i = 0; i < layoutMainArray.length(); i++) {
            JSONObject button = layoutMainArray.getJSONObject(i);
            ControlData n_button = new ControlData();
            n_button.isDynamicBtn = button.getBoolean("isDynamicBtn");
            n_button.dynamicX = getDynamicValue(button, "dynamicX");
            n_button.dynamicY = getDynamicValue(button, "dynamicY");
            n_button.name = button.getString("name");
            n_button.opacity = ((float) ((button.getInt("transparency") - 100) * -1)) / 100f;
            n_button.passThruEnabled = button.getBoolean("passThruEnabled");
            n_button.isToggle = button.getBoolean("isToggle");
            n_button.setHeight(button.getInt("height"));
            n_button.setWidth(button.getInt("width"));
            n_button.bgColor = 0x4d000000;
            n_button.strokeWidth = 0;
            if (button.getBoolean("isRound")) {
                n_button.cornerRadius = 35f;
            }
            int[] keycodes = getKeycodes(button);
            n_button.keycodes = keycodes;
            layout.mControlDataList.add(n_button);
        }
    }

    private static void convertDynamicPositions(JSONObject oldLayoutJson, CustomControls layout) throws JSONException {
        JSONArray layoutMainArray = oldLayoutJson.getJSONArray("mControlDataList");
        layout.mControlDataList = new ArrayList<>(layoutMainArray.length());
        for (int i = 0; i < layoutMainArray.length(); i++) {
            JSONObject button = layoutMainArray.getJSONObject(i);
            ControlData n_button = new ControlData();
            int[] keycodes = new int[]{LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN};
            n_button.isDynamicBtn = button.getBoolean("isDynamicBtn");
            n_button.dynamicX = getDynamicValue(button, "dynamicX");
            n_button.dynamicY = getDynamicValue(button, "dynamicY");
            if (n_button.dynamicX == null && button.has("x")) {
                double buttonC = button.getDouble("x");
                double ratio = buttonC / CallbackBridge.physicalWidth;
                n_button.dynamicX = ratio + " * ${screen_width}";
            }
            if (n_button.dynamicY == null && button.has("y")) {
                double buttonC = button.getDouble("y");
                double ratio = buttonC / CallbackBridge.physicalHeight;
                n_button.dynamicY = ratio + " * ${screen_height}";
            }
            n_button.name = button.getString("name");
            n_button.opacity = ((float) ((button.getInt("transparency") - 100) * -1)) / 100f;
            n_button.passThruEnabled = button.getBoolean("passThruEnabled");
            n_button.isToggle = button.getBoolean("isToggle");
            n_button.setHeight(button.getInt("height"));
            n_button.setWidth(button.getInt("width"));
            n_button.bgColor = 0x4d000000;
            n_button.strokeWidth = 0;
            if (button.getBoolean("isRound")) {
                n_button.cornerRadius = 35f;
            }
            int next_idx = 0;
            int[] keycodeArray = getKeycodeArray(button);
            if (keycodeArray != null) {
                System.arraycopy(keycodeArray, 0, keycodes, 0, keycodeArray.length);
            } else {
                if (button.getBoolean("holdShift")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
                    next_idx++;
                }
                if (button.getBoolean("holdCtrl")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
                    next_idx++;
                }
                if (button.getBoolean("holdAlt")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
                    next_idx++;
                }
                keycodes[next_idx] = button.getInt("keycode");
            }
            n_button.keycodes = keycodes;
            layout.mControlDataList.add(n_button);
        }

        JSONArray layoutDrawerArray = oldLayoutJson.getJSONArray("mDrawerDataList");
        layout.mDrawerDataList = new ArrayList<>();
        for (int i = 0; i < layoutDrawerArray.length(); i++) {
            JSONObject button = layoutDrawerArray.getJSONObject(i);
            JSONObject buttonProperties = button.getJSONObject("properties");
            ControlDrawerData n_button = new ControlDrawerData();
            int[] keycodes = new int[]{LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                    LwjglGlfwKeycode.GLFW_KEY_UNKNOWN};
            n_button.properties.isDynamicBtn = button.getBoolean("isDynamicBtn");
            n_button.properties.dynamicX = getDynamicValue(button, "dynamicX");
            n_button.properties.dynamicY = getDynamicValue(button, "dynamicY");
            if (n_button.properties.dynamicX == null && buttonProperties.has("x")) {
                double buttonC = buttonProperties.getDouble("x");
                double ratio = buttonC / CallbackBridge.physicalWidth;
                n_button.properties.dynamicX = ratio + " * ${screen_width}";
            }
            if (n_button.properties.dynamicY == null && buttonProperties.has("y")) {
                double buttonC = buttonProperties.getDouble("y");
                double ratio = buttonC / CallbackBridge.physicalHeight;
                n_button.properties.dynamicY = ratio + " * ${screen_height}";
            }
            n_button.properties.name = button.getString("name");
            n_button.properties.opacity = ((float) ((button.getInt("transparency") - 100) * -1)) / 100f;
            n_button.properties.passThruEnabled = button.getBoolean("passThruEnabled");
            n_button.properties.isToggle = button.getBoolean("isToggle");
            n_button.properties.setHeight(button.getInt("height"));
            n_button.properties.setWidth(button.getInt("width"));
            n_button.properties.bgColor = 0x4d000000;
            n_button.properties.strokeWidth = 0;
            if (button.getBoolean("isRound")) {
                n_button.properties.cornerRadius = 35f;
            }
            int next_idx = 0;
            int[] keycodeArray = getKeycodeArray(button);
            if (keycodeArray != null) {
                System.arraycopy(keycodeArray, 0, keycodes, 0, keycodeArray.length);
            } else {
                if (button.getBoolean("holdShift")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
                    next_idx++;
                }
                if (button.getBoolean("holdCtrl")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
                    next_idx++;
                }
                if (button.getBoolean("holdAlt")) {
                    keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
                    next_idx++;
                }
                keycodes[next_idx] = button.getInt("keycode");
            }
            n_button.properties.keycodes = keycodes;
            layout.mDrawerDataList.add(n_button);
        }
    }

    private static String getDynamicValue(JSONObject button, String key) throws JSONException {
        String value = button.getString(key);
        if (Tools.isValidString(value)) {
            return value;
        }
        if (button.has(key)) {
            double buttonC = button.getDouble(key);
            double ratio = buttonC / CallbackBridge.physicalWidth;
            return ratio + " * ${screen_width}";
        }
        return null;
    }

    private static int[] getKeycodeArray(JSONObject button) throws JSONException {
        if (!button.has("keycodeArray")) {
            return null;
        }
        JSONArray keycodeArray = button.getJSONArray("keycodeArray");
        int[] keycodes = new int[keycodeArray.length()];
        for (int i = 0; i < keycodeArray.length(); i++) {
            keycodes[i] = keycodeArray.getInt(i);
        }
        return keycodes;
    }

    private static int[] getKeycodes(JSONObject button) throws JSONException {
        int[] keycodes = new int[]{LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                LwjglGlfwKeycode.GLFW_KEY_UNKNOWN,
                LwjglGlfwKeycode.GLFW_KEY_UNKNOWN};
        int next_idx = 0;
        if (button.getBoolean("holdShift")) {
            keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT;
            next_idx++;
        }
        if (button.getBoolean("holdCtrl")) {
            keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL;
            next_idx++;
        }
        if (button.getBoolean("holdAlt")) {
            keycodes[next_idx] = LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT;
            next_idx++;
        }
        keycodes[next_idx] = button.getInt("keycode");
        return keycodes;
    }

    /**
     * Convert the layout stroke width to the V5 form
     */
    private static void convertStrokeWidth(CustomControls layout) {
        for (ControlData data : layout.mControlDataList) {
            data.strokeWidth = Tools.pxToDp(computeStrokeWidth(data.strokeWidth, data.getWidth(), data.getHeight()));
        }

        for (ControlDrawerData data : layout.mDrawerDataList) {
            data.properties.strokeWidth = Tools.pxToDp(computeStrokeWidth(data.properties.strokeWidth, data.properties.getWidth(), data.properties.getHeight()));
            for (ControlData subButtonData : data.buttonProperties) {
                subButtonData.strokeWidth = Tools.pxToDp(computeStrokeWidth(subButtonData.strokeWidth, data
