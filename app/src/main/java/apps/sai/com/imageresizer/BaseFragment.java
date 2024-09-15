package apps.sai.com.imageresizer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.listener.OnResolutionSelectedListener;
import apps.sai.com.imageresizer.resize.ResizeFragment;
import apps.sai.com.imageresizer.resize.SPermissionHelper;
import apps.sai.com.imageresizer.select.SelectActivity;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 08/01/18.
 */

public abstract class BaseFragment extends Fragment {
    protected static final int REQUEST_IMAGE_GALLERY = 1;
    public static final int REQUEST_WRITE_STORAGE = 10;
    private static final String TAG = "BaseFragment";

    public abstract void showError(Throwable th);

    public static final int MAX_LOAD_IMAGES = 300;

    public void shareImageMultiple(Context context, ArrayList<Uri> uriList) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriList);
        shareIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(shareIntent, context.getResources().getText(R.string.share_with)), 100);

    }

    public void showMessage(Handler handler, final String message) {
        if (handler == null || getContext() == null || isDetached()) {
            return;
        }
        handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
    }

    public void shareImage(Context context, String mUrlString) {
        ArrayList<Uri> uriList = new ArrayList<>();
        uriList.add(Uri.parse(mUrlString));
        shareImageMultiple(context, uriList);
    }

    public boolean mayRequestExternalStorage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (SPermissionHelper.hasPermissions(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)) {
                return true;
            }
            SPermissionHelper.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            if (SPermissionHelper.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return true;
            }
            SPermissionHelper.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        return false;
    }


    public interface OnCompressTypeChangedListener {
        void doCompress(int quality, boolean kbEntered);
    }

    public abstract void processGalleryImage(Intent data);

    public void launchGalleryExternalApp(boolean singlePhoto) {

        Intent pictureChooseIntent;
        ((SelectActivity) requireActivity()).doNotShowAd(true);
        pictureChooseIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureChooseIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (!singlePhoto) {
            pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        pictureChooseIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        pictureChooseIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(pictureChooseIntent,
                getResources().getString(R.string.select_gallery_file)), REQUEST_IMAGE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                processGalleryImage(data);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public abstract void showAd();

    public static boolean openCvLoaded;

    class RadioChangeListener implements RadioGroup.OnCheckedChangeListener {
        private final SeekBar mSeekBar;
        SeekBarListener mSeekBarListener;

        RadioChangeListener(SeekBar seekBar) {
            this.mSeekBar = seekBar;
        }

        public void setmSeekBarListener(SeekBarListener mSeekBarListener) {
            this.mSeekBarListener = mSeekBarListener;
        }

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            mSeekBar.setOnSeekBarChangeListener(null);
            mSeekBar.setProgress(0);
            mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);

        }
    }


    class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        RadioGroup mRadioGroup;
        RadioChangeListener mRadioChangeListener;
        TextView mTextView;

        SeekBarListener(RadioGroup radioGroup, TextView textView) {

            mRadioGroup = radioGroup;
            this.mTextView = textView;

        }

        public void setmRadioChangeListener(RadioChangeListener mRadioChangeListener) {
            this.mRadioChangeListener = mRadioChangeListener;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTextView.setText(progress + getString(R.string.percentage));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mRadioGroup.setOnCheckedChangeListener(null);
            mRadioGroup.clearCheck();
            mRadioGroup.setOnCheckedChangeListener(mRadioChangeListener);
        }
    }

    public interface OnDeleteSelectedListener {
        void onDeleteSelected();
    }

    public void showDeleteAlert(Context context, final OnDeleteSelectedListener onDeleteSelectedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(getString(R.string.delete_confirm));
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onDeleteSelectedListener.onDeleteSelected();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showConfirmDiscardAlert(Context context, final OnDeleteSelectedListener onDeleteSelectedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(getString(R.string.discard_changes));
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onDeleteSelectedListener.onDeleteSelected();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showCompressAlert(Context context,
                                  final OnCompressTypeChangedListener compressTypeChangedListener,
                                  HashMap<ResizeFragment.Compress_TYPES, String> hashMap, boolean isSingleImage) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.alert_compress, null);
        final RadioGroup radioGroup = dialogView.findViewById(R.id.quality_radiogroup);
        final TextView seekBarPercentTextView = dialogView.findViewById(R.id.seekbar_percentage);
        final TextView textViewSize = dialogView.findViewById(R.id.newSize);
        dialogBuilder.setView(dialogView);
        final SeekBar seekBar = dialogView.findViewById(R.id.seekbar_quality);
        RadioChangeListener radioChangeListner = new RadioChangeListener(seekBar);
        seekBarPercentTextView.setText(seekBar.getProgress() + "%");
        final SeekBarListener seekBarListener = new SeekBarListener(radioGroup, seekBarPercentTextView);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        radioGroup.setOnCheckedChangeListener(radioChangeListner);
        radioChangeListner.setmSeekBarListener(seekBarListener);
        seekBarListener.setmRadioChangeListener(radioChangeListner);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialogInterface, i) -> {
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        RadioButton radio = (RadioButton) radioGroup.getChildAt(0);
        ResizeFragment.Compress_TYPES compress_types = ResizeFragment.Compress_TYPES.EIGHTY;
        hashMap.put(compress_types, getString(R.string.low_quality));
        radio.setTag(compress_types);
        radio.setText(hashMap.get(compress_types));
        radio = (RadioButton) radioGroup.getChildAt(1);
        compress_types = ResizeFragment.Compress_TYPES.FIFTY;
        radio.setTag(compress_types);
        hashMap.put(compress_types, getString(R.string.medium_quality));
        compress_types = ResizeFragment.Compress_TYPES.THIRTY;
        radio = (RadioButton) radioGroup.getChildAt(2);
        radio.setTag(compress_types);
        hashMap.put(compress_types, getString(R.string.high_quality));
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                String size = textViewSize.getText().toString();
                if (size.isEmpty()) {
                    compressTypeChangedListener.doCompress(seekBar.getProgress(), false);
                    return;
                }
                if (!Character.isDigit(size.charAt(0))) {
                    Toast.makeText(getContext(), "Please enter numeric values", Toast.LENGTH_SHORT).show();
                    return;
                }
                int sizeL;

                try {
                    sizeL = Integer.parseInt(size);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Please enter valid numeric value", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sizeL <= 1) {
                    Toast.makeText(getContext(), "Please enter value more than 1kb", Toast.LENGTH_SHORT).show();

                    return;
                }
                compressTypeChangedListener.doCompress(sizeL, true);
            }
        });

    }

    public final int maxWidth = 4000, maxHeight = 4000;//for standard 5 inch camera

    public final int maxResolution = maxWidth * maxHeight;//default in case error loading camera

    public int meanDimension = (int) Math.sqrt(maxResolution); //mean dimension value

    private static class MyTextWatcher implements android.text.TextWatcher, CompoundButton.OnCheckedChangeListener {
        private final ResolutionInfo resolutionInfo;
        static boolean checked = true;
        EditText editTextOther;
        EditText editText;
        boolean manualSetText;

        MyTextWatcher other;

        public MyTextWatcher(EditText editText, EditText editTextOther, ResolutionInfo resolutionInfo) {
            this.editTextOther = editTextOther;
            this.resolutionInfo = resolutionInfo;
            this.editText = editText;
            checked = true;
        }

        public void setManualSetText(boolean manualSetText) {
            this.manualSetText = manualSetText;

        }

        public void setOther(MyTextWatcher other) {

            this.other = other;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if (!s.isEmpty()) {
                int dimen = Integer.parseInt(s);
                int nw = 0, nh = 0;
                if (R.id.height == editTextOther.getId()) {
                    nw = dimen;
                    resolutionInfoWatched.setWidth(nw);
                } else {
                    nh = dimen;
                    resolutionInfoWatched.setHeight(nh);
                }
                if (checked) {
                    if (resolutionInfo != null) {
                        if (R.id.height == editTextOther.getId()) {
                            nh = Utils.calculateAspectRatioHeight(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).y;
                        } else {
                            nw = Utils.calculateAspectRatioWidth(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).x;
                        }
                        editTextOther.removeTextChangedListener(other);
                        if (R.id.height == editTextOther.getId()) {
                            editTextOther.setText(String.valueOf(nh));
                        } else {
                            editTextOther.setText(String.valueOf(nw));
                        }
                        editTextOther.addTextChangedListener(other);
                    } else {
                        editTextOther.removeTextChangedListener(other);
                        if (R.id.height == editTextOther.getId()) {
                            editTextOther.setText("");
                        } else {
                            editTextOther.setText("");
                        }
                        editTextOther.addTextChangedListener(other);
                    }
                }
            } else {
                if (checked) {
                    editTextOther.removeTextChangedListener(other);
                    editTextOther.setText("");
                    editTextOther.addTextChangedListener(other);
                }
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            checked = b;
            if (checked) {
                editText.removeTextChangedListener(this);
                editText.setText("");
                editText.addTextChangedListener(this);
                editTextOther.removeTextChangedListener(other);
                editTextOther.setText("");
                editTextOther.addTextChangedListener(other);
            }
        }
    }

    Handler mhHandler = new Handler();
    static ResolutionInfo resolutionInfoWatched = new ResolutionInfo();

    public void showCustomScaleAlert(final boolean multiple, OnResolutionSelectedListener onResolutionSelectedListener, final ImageInfo imageInfo
            , final OnResolutionSelectedListener resolutionSelectedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        resolutionInfoWatched = new ResolutionInfo();
        final View dialogView = inflater.inflate(R.layout.alert_custom_scale, null);
        dialogBuilder.setView(dialogView);
        final ResolutionInfo orgResolutionInfo = new ResolutionInfo();
        orgResolutionInfo.setWidth(imageInfo.getWidth());
        orgResolutionInfo.setHeight(imageInfo.getHeight());
        List<ResolutionInfo> resolutionInfoList;
        resolutionInfoList = setResolutionsFromFile(getContext(), R.raw.new_res, imageInfo);
        final Spinner spinnerResolutions = dialogView.findViewById(R.id.custom_res__recycler_view);
        final EditText customPercentagEditBox = dialogView.findViewById(R.id.customPercentEdit);
        final TextView textViewNewDim = dialogView.findViewById(R.id.newDime);

        final CheckBox checkBoxAspect = dialogView.findViewById(R.id.check_aspect);


        spinnerResolutions.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String res = ((ResolutionInfo) parent.getAdapter().getItem(position)).getFormatedString();
                        if (res.contains("x")) {

                            String[] a = res.split("x");
                            resolutionInfoWatched.setWidth(Integer.parseInt(a[0]));
                            resolutionInfoWatched.setHeight(Integer.parseInt(a[1]));
                            resolutionInfoWatched.setFormatedString(resolutionInfoWatched.getWidth() + "x" + resolutionInfoWatched.getHeight());

                            boolean aspect = checkBoxAspect.isChecked();
                            int orgWidth = imageInfo.getWidth();
                            int orgHeight = imageInfo.getHeight();
                            int newWidth = resolutionInfoWatched.getWidth();
                            int newHeight = resolutionInfoWatched.getHeight();

                            if (aspect) {
                                if (orgWidth > orgHeight) {
                                    newHeight = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), newWidth).y;
                                } else {
                                    newWidth = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), newHeight).x;
                                }
                            }

                            if (!multiple) {

                                resolutionInfoWatched.setWidth(newWidth);
                                resolutionInfoWatched.setHeight(newHeight);
                                resolutionInfoWatched.setFormatedString(newWidth + "x" + newHeight);
                            }

                            resolutionInfoWatched.setPreResolutionSelected(true);
                            resolutionInfoWatched.setPercentageSelected(false);


                            textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }


                });

        customPercentagEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            @Override
            public void afterTextChanged(final Editable s) {

                mhHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        String perc = s.toString();

                        try {
                            int ww = 0, hh = 0, percentage = 0;
                            if (!perc.isEmpty()) {
                                percentage = Integer.parseInt(perc);


                            }
                            if (percentage < 0 || percentage > 200) {
                                mhHandler.post(() -> Toast.makeText(getContext(), "Please enter value less than 200", Toast.LENGTH_SHORT).show());
                                return;
                            }
                            if (!multiple) {
                                resolutionInfoWatched.setWidth((int) (imageInfo.getWidth() * percentage / 100f));

                                resolutionInfoWatched.setHeight((int) (imageInfo.getHeight() * percentage / 100f));

                                resolutionInfoWatched.setFormatedString(resolutionInfoWatched.getWidth() + "x" +
                                        resolutionInfoWatched.getHeight());
                            } else {
                                resolutionInfoWatched.setWidth(percentage);

                                resolutionInfoWatched.setHeight(percentage);
                            }
                            resolutionInfoWatched.setPreResolutionSelected(false);
                            resolutionInfoWatched.setPercentageSelected(true);
                            textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));
                        } catch (Exception e) {
                            Log.e(TAG, "onTextChanged: " + e.toString());
                        }
                    }
                }, 300);

            }

        });
        mhHandler.postDelayed(() -> {
            spinnerResolutions.setSelection(1);
        }, 300);

        checkBoxAspect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String customPerc = customPercentagEditBox.getText().toString();
            if (!customPerc.isEmpty()) {

                try {
                    int perc = Integer.parseInt(customPerc);
                    ResolutionInfo resolutionInfo = new ResolutionInfo();
                    if (!multiple) {
                        resolutionInfo = calculateNewDimensions(perc, orgResolutionInfo, isChecked);
                        resolutionInfoWatched.setWidth(resolutionInfo.getWidth());
                        resolutionInfoWatched.setHeight(resolutionInfo.getHeight());
                    } else {
                        resolutionInfoWatched.setWidth(perc);
                        resolutionInfoWatched.setHeight(perc);


                    }
                    resolutionInfoWatched.setPreResolutionSelected(false);
                    resolutionInfoWatched.setPercentageSelected(true);
                    if (resolutionInfo.getWidth() <= 0 && resolutionInfo.getHeight() <= 0) {
                        mhHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Please enter positive values", Toast.LENGTH_SHORT).show();

                            }
                        });
                        return;
                    }
                    if (resolutionInfo.getWidth() <= 0 || resolutionInfo.getHeight() <= 0) {
                        mhHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Please enter positive values", Toast.LENGTH_SHORT).show();

                            }
                        });
                        return;
                    }
                    textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

            }
        });

        final EditText editTextWidth = dialogView.findViewById(R.id.width);
        final EditText editTextHeight = dialogView.findViewById(R.id.height);
        if (multiple) {
            textViewNewDim.setVisibility(View.GONE);
        }

        editTextWidth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            @Override
            public void afterTextChanged(final Editable s) {
                mhHandler.postDelayed(() -> {
                    String w = s.toString();
                    String h = editTextHeight.getText().toString();
                    try {
                        int ww = 0, hh = 0;
                        if (!w.isEmpty()) {
                            ww = Integer.parseInt(w);
                        }
                        if (!h.isEmpty()) {
                            hh = Integer.parseInt(h);
                        }
                        resolutionInfoWatched.setPreResolutionSelected(false);
                        resolutionInfoWatched.setPercentageSelected(false);
                      //  if (multiple) {
                            resolutionInfoWatched.setWidth(ww);
                            resolutionInfoWatched.setHeight(hh);
                      //  }
                        resolutionInfoWatched.setFormatedString(ww + "x" + hh);
                        textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }, 300);
            }
        });

        editTextHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            final ResolutionInfo resolutionInfoWatched = new ResolutionInfo();

            @Override
            public void afterTextChanged(final Editable s) {
                mhHandler.postDelayed(() -> {
                    String h = s.toString();
                    String w = editTextWidth.getText().toString();
                    try {
                        int ww = 0, hh = 0;
                        if (!w.isEmpty()) {
                            ww = Integer.parseInt(w);
                        }
                        if (!h.isEmpty()) {
                            hh = Integer.parseInt(h);
                        }
                        resolutionInfoWatched.setPreResolutionSelected(false);
                        resolutionInfoWatched.setPercentageSelected(false);
                        if (multiple) {
                            resolutionInfoWatched.setWidth(ww);
                            resolutionInfoWatched.setHeight(hh);
                        }
                        resolutionInfoWatched.setFormatedString(ww + "x" + hh);
                        textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }, 300);

            }

        });


        final ResolutionInfo resolutionInfo = new ResolutionInfo();
        resolutionInfo.setWidth(imageInfo.getWidth());
        resolutionInfo.setHeight(imageInfo.getHeight());

        MyTextWatcher textWatcherWidth = new MyTextWatcher(editTextWidth, editTextHeight, multiple ? null : resolutionInfo);
        editTextWidth.addTextChangedListener(textWatcherWidth);

        MyTextWatcher textWatcherHeight = new MyTextWatcher(editTextHeight, editTextWidth, multiple ? null : resolutionInfo);
        textWatcherWidth.setOther(textWatcherHeight);

        textWatcherHeight.setOther(textWatcherWidth);

        final CheckBox checkBox = dialogView.findViewById(R.id.check_aspect);
        checkBox.setOnCheckedChangeListener(textWatcherHeight);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String text;
            if (!multiple) {
                text = resolutionInfoWatched.getWidth() + "x" + resolutionInfoWatched.getHeight();
                String[] a = text.split(":");
                if (a.length > 1) {
                    text = a[1].trim();
                    String[] b = text.split("x");
                    try {
                        resolutionInfoWatched.setWidth(Integer.parseInt(b[0]));
                        resolutionInfoWatched.setHeight(Integer.parseInt(b[1]));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                if (text.isEmpty()) {
                    Toast.makeText(getContext(),
                            String.format(getString(R.string.min_length_error)), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            int width = resolutionInfoWatched.getWidth();
            int height = resolutionInfoWatched.getHeight();
            if (width <= 1 && height <= 1) {
                Toast.makeText(getContext(), getString(R.string.min_length_error), Toast.LENGTH_LONG).show();
                return;
            }

            if (!checkBox.isChecked() && (width <= 1 || height <= 1)) {
                if (!resolutionInfoWatched.isPreResolutionSelected() && !resolutionInfoWatched.isPercentageSelected()) {
                    try {
                        if (width <= 1) {
                            resolutionInfoWatched.setWidth(Integer.parseInt(editTextWidth.getText().toString()));
                        }
                        if (height <= 1) {
                            resolutionInfoWatched.setHeight(Integer.parseInt(editTextHeight.getText().toString()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    width = resolutionInfoWatched.getWidth();
                    height = resolutionInfoWatched.getHeight();
                }
                if ((width <= 1 || height <= 1)) {
                    Toast.makeText(getContext(), getString(R.string.min_length_error), Toast.LENGTH_LONG).show();
                    return;
                }

            }
            if (!multiple && (checkBox.isChecked() && width >= 1 && height >= 1)) {
                int expectedHeight;
                int expectedWidth;
                if (imageInfo.getWidth() > imageInfo.getHeight()) {
                    expectedWidth = width;
                    expectedHeight = Utils.calculateAspectRatioHeight(new Point(imageInfo.getWidth(), imageInfo.getHeight()), width).y;
                } else {
                    expectedHeight = height;
                    expectedWidth = Utils.calculateAspectRatioWidth(new Point(imageInfo.getWidth(), imageInfo.getHeight()), height).x;
                }


                int diffw = Math.abs(expectedWidth - width);
                int diffh = Math.abs(expectedHeight - height);
                int error = 5;
                if (diffw > error || diffh > error) {
                    Toast.makeText(getContext(), getString(R.string.info_aspect), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (editTextWidth.hasFocus()) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextWidth.getWindowToken(), 0);
            } else if (editTextHeight.hasFocus()) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextHeight.getWindowToken(), 0);

            }


            editTextWidth.setOnFocusChangeListener((v, hasFocus) -> {

            });

            editTextHeight.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editTextHeight.postDelayed(() -> {
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editTextHeight.getWindowToken(), 0);
                    }, 100);
                }
            });

            if (!multiple) {
                resolutionSelectedListener.onResolutionSelected(resolutionInfoWatched);
            } else {

                boolean aspect = checkBox.isChecked();
                resolutionInfoWatched.setFormatedString(resolutionInfo.getWidth() + "x" + resolutionInfo.getHeight());
                resolutionInfoWatched.setAspect(aspect);
                resolutionSelectedListener.onResolutionSelected(resolutionInfoWatched);
            }
            alertDialog.dismiss();
        });
        ArrayAdapter<ResolutionInfo> dataAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, resolutionInfoList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolutions.setAdapter(dataAdapter);
        alertDialog.show();
    }


    private ResolutionInfo calculateNewDimensions(int progress, ResolutionInfo orgResolutionInfo, boolean aspect) {

        ResolutionInfo resolutionInfo = new ResolutionInfo();
        int orgWidth = orgResolutionInfo.getWidth(), orgHeight = orgResolutionInfo.getHeight();
        float newWidth = ((progress / 100.0f) * orgWidth);
        float newHeight = ((progress / 100.0f) * orgHeight);
        resolutionInfo.setWidth((int) newWidth);
        resolutionInfo.setHeight((int) newHeight);
        resolutionInfo.setFormatedString(newWidth + "x" + newHeight);
        return resolutionInfo;
    }

    public List<ResolutionInfo> setResolutionsFromFile(Context context, int resId, ImageInfo imageInfo
    ) {
        String s = Utils.loadResolutionsFromAssets(context, resId);
        List<ResolutionInfo> newResolutionsList = new ArrayList<>();
        String[] b = s.split("\n");
        for (String string : b) {
            String[] a = string.split("×");
            ResolutionInfo resolutionInfo = new ResolutionInfo();
            try {
                resolutionInfo.setWidth(Integer.parseInt(a[0]));
                resolutionInfo.setHeight(Integer.parseInt(a[1]));
                resolutionInfo.setFormatedString(resolutionInfo.getWidth() + "x" + resolutionInfo.getHeight());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            newResolutionsList.add(resolutionInfo);
        }

        ResolutionInfo resolutionInfo = new ResolutionInfo();
        resolutionInfo.setFormatedString("Choose a size");
        newResolutionsList.add(0, resolutionInfo);
        return newResolutionsList;
    }


    public void showCustomResolutionAlert(final OnResolutionSelectedListener onResolutionSelectedListener, final long maxResolution, final long maxWidth,
                                          final long meanDimension) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.alert_custom_resolution, null);
        dialogBuilder.setView(dialogView);
        final EditText editTextWidth = dialogView.findViewById(R.id.width);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle(getString(R.string.dialog_title));
        final EditText editTextHeight = dialogView.findViewById(R.id.height);
        final MyTextWatcher textWatcherWidth = new MyTextWatcher(editTextWidth, editTextHeight, null);
        editTextWidth.addTextChangedListener(textWatcherWidth);
        final MyTextWatcher textWatcherHeight = new MyTextWatcher(editTextHeight, editTextWidth, null);
        textWatcherWidth.setOther(textWatcherHeight);
        textWatcherHeight.setOther(textWatcherWidth);
        final CheckBox checkBox = dialogView.findViewById(R.id.check_aspect);
        checkBox.setOnCheckedChangeListener(textWatcherHeight);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialogInterface, i) -> {
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String s1 = editTextWidth.getText().toString();
            String s2 = editTextHeight.getText().toString();
            if (s1.length() <= 2 || s2.length() <= 2) {
                Toast.makeText(getContext(), getString(R.string.min_length_error), Toast.LENGTH_LONG).show();
                return;
            }
            int width = Integer.parseInt(s1);
            int height = Integer.parseInt(s2);
            int res = width * height;

            long available = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 4;
            if (available < (long) width * width || available < (long) height * height || available < (long) width * height) {
                Toast.makeText(getContext(),
                        getString(R.string.not_enough_memory), Toast.LENGTH_SHORT).show();
                return;
            }

            if (res > maxResolution) {
                if (width > maxWidth) {
                    Toast.makeText(getContext(),
                            String.format(getString(R.string.max_length_error), meanDimension, meanDimension), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            ResolutionInfo resolutionInfo = new ResolutionInfo();
            resolutionInfo.setWidth(width);
            resolutionInfo.setHeight(height);
            resolutionInfo.setFormatedString("");
            alertDialog.dismiss();
            onResolutionSelectedListener.onResolutionSelected(resolutionInfo);
        });

    }

    public void showFIleSize(ImageInfo imageInfo, TextView textView) {
        String res = String.format(" %s x %s", imageInfo.getWidth(), imageInfo.getHeight());
        String output = imageInfo.getFormatedFileSize();
        textView.setText(String.format("%s (%s) ", res, output));
    }
}


