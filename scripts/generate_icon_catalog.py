import os
import subprocess

def get_sdk_path(local_properties_path: str, sdk_version_path: str) -> str:
    with open(local_properties_path, "r", encoding="utf-8") as prop_file:
        for ln in prop_file.readlines():
            if ln.startswith("sdk.dir="):
                sdk_dir_path = ln.split("=")[1].strip().replace(r"\:", ":").replace("\\\\", "\\")
                sdk_path = os.path.join(sdk_dir_path, sdk_version_path)
                print("Using SDK:", sdk_path)
                return sdk_path
    raise Exception(f"sdk.dir is missing in local.properties: {local_properties_path}")

SDK_PATH = get_sdk_path("./local.properties", "platforms/android-35/android.jar")
LAYOUT_PATH = "./app/src/debug/res/layout/icon_catalog.xml"
COLUMNS_NUMBER = 4
ICON_BC_COLOR = "@android:color/black"

TEXT_FONT_SIZE = "20sp"
TEXT_FONT_STYLE = "bold"
TEXT_COLOR = "@android:color/white"
TEXT_PADDING = "10dp"

ICON_WIDTH = "70dp"
ICON_HEIGHT = "70dp"
ICON_PADDING = "5dp"
ROW_MARGIN_BOTTOM = "10dp"

PREF_FILE_CONTENT = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:ignore="UseCompoundDrawables">
"""
LINEAR_LAYOUT_START = f"""
<LinearLayout
        android:layout_width="match_parent"
        android:orientation="horizontal"
        android:layout_height="wrap_content"
        android:baselineAligned="false"
        android:layout_marginBottom="{ROW_MARGIN_BOTTOM}">
"""
LINEAR_LAYOUT_END = """</LinearLayout>"""

ICON_VALUE_TEMPLATE = f"""
<LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:gravity="center_horizontal"
        tools:ignore="UselessParent">
        
    <ImageView
            android:contentDescription="android.R.drawable.$ICON_CODE$"
            android:src="@android:drawable/$ICON_CODE$"
            android:background="{ICON_BC_COLOR}"
            android:layout_width="{ICON_WIDTH}"
            android:layout_height="{ICON_HEIGHT}"
            android:padding="{ICON_PADDING}"
            tools:ignore="ContentDescription,HardcodedText" />

    <TextView
            android:text="$ICON_CODE$"
            android:textSize="{TEXT_FONT_SIZE}"
            android:textStyle="{TEXT_FONT_STYLE}"
            android:textColor="{TEXT_COLOR}"
            android:paddingStart="{TEXT_PADDING}"
            android:paddingEnd="0dp"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center_vertical"
            tools:ignore="HardcodedText" />
</LinearLayout>
"""

drawable = subprocess.run(["javap", "-classpath", SDK_PATH, "android.R$drawable"], stdout=subprocess.PIPE)

lines = drawable.stdout.decode("utf-8").split("\n")
values = []
for line in lines:
    stripped_line = line.strip()
    if stripped_line.startswith("public static final int"):
        value = (stripped_line
                 .replace("public static final int", "")
                 .replace(";", "")
                 .strip())
        if value.startswith("ic_"):
            values.append(value)

with open(LAYOUT_PATH, "w", encoding="utf-8") as f:
    f.write(PREF_FILE_CONTENT)
    for i in range(len(values)):
        value = values[i]
        if i % COLUMNS_NUMBER == 0:
            if i != 0:
                f.write(LINEAR_LAYOUT_END)
            f.write(LINEAR_LAYOUT_START)
        f.write(ICON_VALUE_TEMPLATE
                .replace("$ICON_CODE$", value)
                .replace("$ICON_BC_COLOR$", ICON_BC_COLOR))
    f.write(LINEAR_LAYOUT_END)
    f.write(LINEAR_LAYOUT_END)