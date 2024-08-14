package apps.sai.com.imageresizer;

import static android.Manifest.permission.MANAGE_DOCUMENTS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import apps.sai.com.imageresizer.resize.ResolutionAdaptor;
import apps.sai.com.imageresizer.resize.SPermissionHelper;
import apps.sai.com.imageresizer.select.SelectActivity;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 08/01/18.
 */

public abstract class BaseFragment extends Fragment {
    protected static final int REQUEST_IMAGE_GALLERY = 1;
    //    protected static final int PICK_IMAGE_MULTIPLE = 20;
    public static final int REQUEST_WRITE_STORAGE = 10;

    public abstract void showError(Throwable th);

    public static final int MAX_LOAD_IMAGES = 300;


    public void shareImageMultiple(Context context, ArrayList<Uri> uriList) {

//        Uri fileUri = Uri.fromFile(new File(mUrlString));
        shareFiles = true;
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

            }
        });
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
        //        void onCompressTypeChanged(ResizeFragment.Compress_TYPES compress_types);
        void doCompress(int quality, boolean kbEntered);
    }

    public abstract void proccessGalleryImage(Intent data);

    private static boolean shareFiles;

    public void launchgalleryExternalApp(boolean singlePhoto) {

        Intent pictureChooseIntent = null;
        ((SelectActivity) getActivity()).doNotShowAd(true);

        //new Intent(Intent.ACTION_GET_CONTENT,
        //      MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


        pictureChooseIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pictureChooseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureChooseIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (!singlePhoto) {
            pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }


        pictureChooseIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//        pictureChooseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pictureChooseIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        startActivityForResult(Intent.createChooser(pictureChooseIntent,
                getResources().getString(R.string.select_gallery_file)), REQUEST_IMAGE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_IMAGE_GALLERY) {


                proccessGalleryImage(data);
            }
            return;
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

            mTextView.setText(String.valueOf(progress) + "%");

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//                compressTypeChangedListener.doCompress(seekBar.getProgress());

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
//    listView.setAdapter();

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
        // final View dialogView = inflater.inflate(R.layout.alert_compress, null);


    }

    public void showConfirmDiscardAlert(Context context, final OnDeleteSelectedListener onDeleteSelectedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(getString(R.string.discard_changes));


        final AlertDialog alertDialog = dialogBuilder.create();
//    listView.setAdapter();

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
        // final View dialogView = inflater.inflate(R.layout.alert_compress, null);


    }

    public void showCompressAlert(Context context,
                                  final OnCompressTypeChangedListener compressTypeChangedListener,
                                  HashMap<ResizeFragment.Compress_TYPES, String> hashMap, boolean isSingleImage) {


//  if(mCompressAdaptor==null) {

//       mCompressAdaptor = new CompressAdaptor(getActivity(), compressList,mFileSize);
//   }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.alert_compress, null);
        final RadioGroup radioGroup = dialogView.findViewById(R.id.quality_radiogroup);
        long id = radioGroup.getCheckedRadioButtonId();

        final TextView seekBarPercentTextView = (TextView) dialogView.findViewById(R.id.seekbar_percentage);

        final TextView textViewSize = dialogView.findViewById(R.id.newSize);


        dialogBuilder.setView(dialogView);
        final SeekBar seekBar = dialogView.findViewById(R.id.seekbar_quality);
        RadioChangeListener radioChangeListner = new RadioChangeListener(seekBar);
        seekBarPercentTextView.setText(String.valueOf(seekBar.getProgress()) + "%");
//        seekBar.setVisibility(View.GONE);


        final SeekBarListener seekBarListener = new SeekBarListener(radioGroup, seekBarPercentTextView);
        seekBar.setOnSeekBarChangeListener(seekBarListener);

        radioGroup.setOnCheckedChangeListener(radioChangeListner);


        radioChangeListner.setmSeekBarListener(seekBarListener);

        seekBarListener.setmRadioChangeListener(radioChangeListner);


        final AlertDialog alertDialog = dialogBuilder.create();
//    listView.setAdapter();

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


//       editTextWidth.addTextChangedListener(textWatcherHeight);
//       editTextHeight.addTextChangedListener(textWatcherWidth);
//    List<Compress> compressList = new ArrayList<>();
        RadioButton radio = (RadioButton) radioGroup.getChildAt(0);
        ResizeFragment.Compress_TYPES compress_types = ResizeFragment.Compress_TYPES.EIGHTY;
        hashMap.put(compress_types, getString(R.string.low_quality));
        radio.setTag(compress_types);
//    Compress compress = new Compress();

        radio.setText(hashMap.get(compress_types));


       /* if(id ==radio.getId()){

        }*/

//       compressList.add(new Compress("70 %",Compress.Compress_TYPES.SEVENTY));
        radio = (RadioButton) radioGroup.getChildAt(1);
        compress_types = ResizeFragment.Compress_TYPES.FIFTY;
        radio.setTag(compress_types);

        hashMap.put(compress_types, getString(R.string.medium_quality));

//    compressList.add(new Compress(radio.getText().toString(),compress_types));
        if (id == radio.getId()) {
            //   compressTypeChangedListener.onCompressTypeChanged(compress_types);

        }
        compress_types = ResizeFragment.Compress_TYPES.THIRTY;


        radio = (RadioButton) radioGroup.getChildAt(2);
        radio.setTag(compress_types);
        hashMap.put(compress_types, getString(R.string.high_quality));

//    compressList.add(new Compress(radio.getText().toString(),compress_types));

        if (id == radio.getId()) {
            //   compressTypeChangedListener.onCompressTypeChanged(compress_types);
//        mCompressPercentage = compress_types;
        }


        alertDialog.show();


        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//
                alertDialog.dismiss();
            /*  View view1 =   radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
              if(view1!=null) {

                  ResizeFragment.Compress_TYPES compress_types1 =
                          (ResizeFragment.Compress_TYPES) view1.getTag();

                  compressTypeChangedListener.doCompress(compress_types1.getCode());
              }else{*/

                String size = textViewSize.getText().toString();
                if (size.length() == 0) {
//                    seekBar.getProgress();
                    compressTypeChangedListener.doCompress(seekBar.getProgress(), false);
                    return;
                }
                if (size.length() > 0 && Character.isDigit(size.charAt(0)) == false) {
                    Toast.makeText(getContext(), "Please enter numeric values", Toast.LENGTH_SHORT).show();

                    return;
                }
                int sizeL = 0;

                try {

                    sizeL = Integer.valueOf(size);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Please enter valid numeric value", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (sizeL <= 1) {
                    Toast.makeText(getContext(), "Please enter value more than 1kb", Toast.LENGTH_SHORT).show();

                    return;
                }

                compressTypeChangedListener.doCompress(sizeL, true);

//              }


                //onResolutionSelected(s1+"x"+s2);
            }
        });

    }

    public final int maxWidth = 4000, maxHeight = 4000;//for standard 5 inch camera

    public final int maxResolution = maxWidth * maxHeight;//default in case error loading camera

    public int meanDimension = (int) Math.sqrt(maxResolution); //mean dimension value

    public static class MyTextWatcher implements android.text.TextWatcher, CompoundButton.OnCheckedChangeListener {
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

            //  editText.removeTextChangedListener(other);

        }

        public void setManualSetText(boolean manualSetText) {
            this.manualSetText = manualSetText;

        }

        public void setOther(MyTextWatcher other) {

            this.other = other;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            editText.removeTextChangedListener(other);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

//            editText.removeTextChangedListener(other);


        }

        @Override
        public void afterTextChanged(Editable editable) {

            String s = editable.toString();


            if (s.length() > 0) {
                    /*if(manualSetText ==true){
//                        return;
                    }*/
                int dimen = Integer.valueOf(s);
                int nw = 0, nh = 0;


                if (R.id.height == editTextOther.getId()) {
                    //this is width text  calculate height
                    nw = dimen;
                    resolutionInfoWatched.setWidth(nw);
                } else {
                    nh = dimen;
                    resolutionInfoWatched.setHeight(nh);

                }


                if (checked == true) {

//                        if(editText.getText().toString().equals(width+"") ==false) {

                    if (resolutionInfo != null) {
                        if (R.id.height == editTextOther.getId()) {

                            nh = (int) Utils.calculateAspectRatioHeight(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).y;
                        } else {
                            nw = (int) Utils.calculateAspectRatioWidth(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).x;

                        }

//                        mManualSetText =true;
                        editTextOther.removeTextChangedListener(other);

//                            other.setManualSetText(true);
                        if (R.id.height == editTextOther.getId()) {
                            editTextOther.setText(nh + "");
                        } else {
                            editTextOther.setText(nw + "");

                        }


                        editTextOther.addTextChangedListener(other);
                    } else {
                        if (R.id.height == editTextOther.getId()) {

                            nh = 0;//(int) Utils.calculateAspectRatioHeight(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).y;
                        } else {
                            nw = 0;//(int) Utils.calculateAspectRatioWidth(new Point(resolutionInfo.getWidth(), resolutionInfo.getHeight()), dimen).x;

                        }

//                        mManualSetText =true;
                        editTextOther.removeTextChangedListener(other);

//                            other.setManualSetText(true);
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

//            if(resolutionInfo==null){//multiple
            if (checked == true) {

                int nw = 0, nh = 0;


                infoTextView.setVisibility(View.VISIBLE);
                editText.removeTextChangedListener(this);
                editText.setText("");

                editText.addTextChangedListener(this);

                editTextOther.removeTextChangedListener(other);
                editTextOther.setText("");

                editTextOther.addTextChangedListener(other);


            } else {
                infoTextView.setVisibility(View.GONE);

//                }

            }


        }
    }

    Handler mhHandler = new Handler();
    // OnResolutionSelectedListener mOnResolutionSelectedListener;

    static ResolutionInfo resolutionInfoWatched = new ResolutionInfo();

    static TextView infoTextView;

    public void showCustomScaleAlert(final boolean multiple, OnResolutionSelectedListener onResolutionSelectedListener, final ImageInfo imageInfo
            , final OnResolutionSelectedListener resolutionSelectedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();

        resolutionInfoWatched = new ResolutionInfo();

        final View dialogView = inflater.inflate(R.layout.alert_custom_scale, null);
        dialogBuilder.setView(dialogView);


        final ResolutionInfo orgResolutionInfo = new ResolutionInfo();
        orgResolutionInfo.setWidth(imageInfo.getWidth());
        orgResolutionInfo.setHeight(imageInfo.getHeight());
        List<ResolutionInfo> resolutionInfoList = null;


        resolutionInfoList = setResolutionsFromFile(getContext(), R.raw.new_res, imageInfo);

        final Spinner spinnerResolutions = dialogView.findViewById(R.id.custom_res__recycler_view);

        final EditText customPercentagEditBox = dialogView.findViewById(R.id.customPercentEdit);
//        final TextView textViewPercetnage = dialogView.findViewById(R.id.seekbar_percentage);
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
                            resolutionInfoWatched.setFormatedString(String.format("%dx%d", resolutionInfoWatched.getWidth(), resolutionInfoWatched.getHeight()));

                            boolean aspect = checkBoxAspect.isChecked();
                            int orgWidth = imageInfo.getWidth();
                            int orgHeight = imageInfo.getHeight();
                            int newWidth = resolutionInfoWatched.getWidth();
                            int newHeight = resolutionInfoWatched.getHeight();

                            if (aspect) {
                                if (orgWidth > orgHeight) {
                                    newHeight = (int) Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), (int) newWidth).y;
                                } else {
                                    newWidth = (int) Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), (int) newHeight).x;
                                }
                            }

                            if (!multiple) {

                                resolutionInfoWatched.setWidth(newWidth);
                                resolutionInfoWatched.setHeight(newHeight);
                                resolutionInfoWatched.setFormatedString(String.format("%dx%d", newWidth, newHeight));
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
                            if (perc.length() > 0) {
                                percentage = Integer.valueOf(perc);


                            }
                            if (percentage < 0 || percentage > 200) {
                                mhHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Please enter value less than 200", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                return;
                            }
                            if (!multiple) {
                                resolutionInfoWatched.setWidth((int) (imageInfo.getWidth() * percentage / 100f));

                                resolutionInfoWatched.setHeight((int) (imageInfo.getHeight() * percentage / 100f));

                                resolutionInfoWatched.setFormatedString(String.format("%dx%d", resolutionInfoWatched.getWidth()
                                        , resolutionInfoWatched.getHeight()));
                            } else {
                                resolutionInfoWatched.setWidth(percentage);

                                resolutionInfoWatched.setHeight(percentage);


//                           resolutionInfoWatched.setFormatedString(String.format("%dx%d", resolutionInfoWatched.getWidth()
//                                   , resolutionInfoWatched.getHeight()));
                            }
                            resolutionInfoWatched.setPreResolutionSelected(false);
                            resolutionInfoWatched.setPercentageSelected(true);

//                  ResolutionInfo resolutionInfo = calculateNewDimensions(seekBar.getProgress(), orgResolutionInfo, isChecked);

                            textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 300);

            }

        });

//      final SeekBar seekBar = dialogView.findViewById(R.id.seekbar_custom_percentage);

        checkBoxAspect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//              if(multiple==false) {
                String customPerc = customPercentagEditBox.getText().toString();
                if (customPerc.length() > 0) {

                    try {
                        int perc = Integer.valueOf(customPerc);
                        ResolutionInfo resolutionInfo = new ResolutionInfo();
                        if (multiple == false) {
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

                          /*if (resolutionInfo.getWidth() * resolutionInfo.getHeight() >= maxResolution) {
                              mhHandler.post(new Runnable() {
                                  @Override
                                  public void run() {
                                      Toast.makeText(getContext(), "Please enter smaller values", Toast.LENGTH_SHORT).show();

                                  }
                              });
                              return;
                          }*/
                        textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));

//                        textViewPercetnage.setText(String.format("%d %s", perc, "%"));


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                  }
                } else {

                }

            }
        });

        final EditText editTextWidth = dialogView.findViewById(R.id.width);
        final EditText editTextHeight = dialogView.findViewById(R.id.height);
//      final View enterPercentageLabel = dialogView.findViewById(R.id.enterPercentageLabel);
//      final TextView enterDimenLabel = dialogView.findViewById(R.id.enterDimenLabel);
        infoTextView = dialogView.findViewById(R.id.info);


        if (multiple == true) {
//          spinnerResolutions.setVisibility(View.GONE);
//          customPercentagEditBox.setVisibility(View.GONE);
//          enterPercentageLabel.setVisibility(View.GONE);
//          enterDimenLabel.setText(getString(R.string.custom_label_dime_multi));
            textViewNewDim.setVisibility(View.GONE);
//          infoTextView.setVisibility(View.VISIBLE);
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

                mhHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        String w = s.toString();
                        String h = editTextHeight.getText().toString();

                        try {
                            int ww = 0, hh = 0;
                            if (w.length() > 0) {
                                ww = Integer.valueOf(w);


                            }
                            if (h.length() > 0) {
                                hh = Integer.valueOf(h);
                            }


                            resolutionInfoWatched.setPreResolutionSelected(false);
                            resolutionInfoWatched.setPercentageSelected(false);
                            if (multiple == true) {
                                resolutionInfoWatched.setWidth(ww);
                                resolutionInfoWatched.setHeight(hh);
                            }
//                  resolutionInfoWatched.setWidth(ww);
//                  resolutionInfoWatched.setHeight(hh);

                            resolutionInfoWatched.setFormatedString(String.format("%dx%d", ww, hh));

//                  ResolutionInfo resolutionInfo = calculateNewDimensions(seekBar.getProgress(), orgResolutionInfo, isChecked);

                            textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

                mhHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        String h = s.toString();
                        String w = editTextWidth.getText().toString();

                        try {
                            int ww = 0, hh = 0;
                            if (w.length() > 0) {
                                ww = Integer.valueOf(w);


                            }
                            if (h.length() > 0) {
                                hh = Integer.valueOf(h);
                            }
                            resolutionInfoWatched.setPreResolutionSelected(false);
                            resolutionInfoWatched.setPercentageSelected(false);
                            if (multiple == true) {

                                resolutionInfoWatched.setWidth(ww);
                                resolutionInfoWatched.setHeight(hh);
                            }
//                  resolutionInfoWatched.setWidth(ww);
//                  resolutionInfoWatched.setHeight(hh);

                            resolutionInfoWatched.setFormatedString(String.format("%dx%d", ww, hh));

//                  ResolutionInfo resolutionInfo = calculateNewDimensions(seekBar.getProgress(), orgResolutionInfo, isChecked);

                            textViewNewDim.setText(String.format("%s %s", getString(R.string.new_dimensions_label), resolutionInfoWatched.getFormatedString()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 300);

            }

        });


        final ResolutionInfo resolutionInfo = new ResolutionInfo();
        resolutionInfo.setWidth(imageInfo.getWidth());
        resolutionInfo.setHeight(imageInfo.getHeight());

        MyTextWatcher textWatcherWidth = new MyTextWatcher(editTextWidth, editTextHeight, multiple == true ? null : resolutionInfo);
        editTextWidth.addTextChangedListener(textWatcherWidth);

        MyTextWatcher textWatcherHeight = new MyTextWatcher(editTextHeight, editTextWidth, multiple == true ? null : resolutionInfo);
        textWatcherWidth.setOther(textWatcherHeight);

        textWatcherHeight.setOther(textWatcherWidth);

        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.check_aspect);
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String text = textViewNewDim.getText().toString();

                if (multiple == false) {
                    text = resolutionInfoWatched.getWidth() + "x" + resolutionInfoWatched.getHeight();


                    String a[] = text.split(":");
                    if (a != null && a.length > 1) {

                        text = a[1].trim();

                        String b[] = text.split("x");
                        try {
                            resolutionInfoWatched.setWidth(Integer.valueOf(b[0]));
                            resolutionInfoWatched.setHeight(Integer.valueOf(b[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (text.length() == 0) {
                        Toast.makeText(getContext(),
                                String.format(getString(R.string.min_length_error)), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

//              String s1 =editTextWidth.getText().toString();
//              String s2 =editTextHeight.getText().toString();


                int width = resolutionInfoWatched.getWidth();
                int height = resolutionInfoWatched.getHeight();
                int res = width * height;

                if (width <= 1 && height <= 1) {


                    Toast.makeText(getContext(), getString(R.string.min_length_error), Toast.LENGTH_LONG).show();
                    return;
                }

                if (checkBox.isChecked() == false && (width <= 1 || height <= 1)) {

                    if (resolutionInfoWatched.isPreResolutionSelected() == false && resolutionInfoWatched.isPercentageSelected() == false) {

                        try {
                            if (width <= 1) {
                                resolutionInfoWatched.setWidth(Integer.valueOf(editTextWidth.getText().toString()));
                            }
                            if (height <= 1) {
                                resolutionInfoWatched.setHeight(Integer.valueOf(editTextHeight.getText().toString()));

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        width = resolutionInfoWatched.getWidth();
                        height = resolutionInfoWatched.getHeight();
                    }
//                         if(multiple ==false || (multiple==true && checkBox.isChecked()==false)) {
                    if ((width <= 1 || height <= 1)) {
                        Toast.makeText(getContext(), getString(R.string.min_length_error), Toast.LENGTH_LONG).show();
                        return;
                    }
//                         }


                }
                if (multiple == false && (checkBox.isChecked() == true && width >= 1 && height >= 1)) {
                    float ratio = 0;
                    int expectedHeight = 0;
                    int expectedWidth = 0;
                    if (imageInfo.getWidth() > imageInfo.getHeight()) {
                        expectedWidth = width;
                        expectedHeight = (int) Utils.calculateAspectRatioHeight(new Point(imageInfo.getWidth(), imageInfo.getHeight()), width).y;
                    } else {
                        expectedHeight = height;
                        expectedWidth = (int) Utils.calculateAspectRatioWidth(new Point(imageInfo.getWidth(), imageInfo.getHeight()), height).x;
                    }


                    int diffw = Math.abs(expectedWidth - width);
                    int diffh = Math.abs(expectedHeight - height);
                    int error = 5;
                    if (multiple == false) {
                        if (diffw > error || diffh > error) {
                            Toast.makeText(getContext(), getString(R.string.info_aspect), Toast.LENGTH_LONG).show();

                            return;
                        }
                    }
                }
            /*  if(res>maxResolution){
                  if(width>maxWidth || height>maxHeight) {
                      Toast.makeText(getContext(),
                              String.format(getString(R.string.max_length_error),meanDimension,meanDimension), Toast.LENGTH_SHORT).show();
                      return;
                  }


              }*/


                if (editTextWidth.hasFocus()) {

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextWidth.getWindowToken(), 0);


                } else if (editTextHeight.hasFocus()) {


                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextHeight.getWindowToken(), 0);

                }


                editTextWidth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {


                        }
                    }
                });

                editTextHeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            editTextHeight.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(editTextHeight.getWindowToken(), 0);
                                }
                            }, 100);

                        }
                    }
                });

                if (!multiple) {
                    resolutionSelectedListener.onResolutionSelected(resolutionInfoWatched);
                } else {

                    boolean aspect = checkBox.isChecked();
                    resolutionInfoWatched.setFormatedString(resolutionInfo.getWidth() + "x" + resolutionInfo.getHeight());
                    resolutionInfoWatched.setAspect(aspect);
/*
                  if(resolutionInfoWatched.isPreResolutionSelected() ==true){



                  }else if(resolutionInfoWatched.isPercentageSelected() ==true){

                  }else{

                  }*/

                    resolutionSelectedListener.onResolutionSelected(resolutionInfoWatched);
                }


                alertDialog.dismiss();
//              onResolutionSelectedListener.onResolutionSelected(resolutionInfo);
//                onResolutionSelected(s1+"x"+s2);
            }
        });
        ArrayAdapter<ResolutionInfo> dataAdapter = new ArrayAdapter<>(getContext(),
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


    /*  if(aspect){
          if(orgWidth>orgHeight) {
              newHeight = (int) Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight),(int) newWidth).y;
          }else{
              newWidth = (int) Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight),(int) newHeight).x;
          }

      }*/

        resolutionInfo.setWidth((int) newWidth);
        resolutionInfo.setHeight((int) newHeight);
        resolutionInfo.setFormatedString(String.format("%dx%d", (int) newWidth, (int) newHeight));


        return resolutionInfo;
    }

    public List<ResolutionInfo> setResolutionsFromFile(Context context, int resId, ImageInfo imageInfo
    ) {
        String s = Utils.loadResolutionsFromAssets(context, resId);
        List<ResolutionInfo> newResolutionsList = new ArrayList<>();

        if (s != null) {

            String b[] = s.split("\n");
            for (int i = 0; i < b.length; i++) {
                String a[] = b[i].split("×");

                ResolutionInfo resolutionInfo = new ResolutionInfo();
                try {
                    resolutionInfo.setWidth(Integer.valueOf(a[0]));
                    resolutionInfo.setHeight(Integer.valueOf(a[1]));
//               float oldRes = imageInfo.getWidth()*imageInfo.getHeight();
//               float newRes = resolutionInfo.getWidth()*resolutionInfo.getHeight();

//               float percent = (newRes/oldRes)*100;
//               resolutionInfo.setFormatedString(String.format("%dx%d(%.2f)%s", resolutionInfo.getWidth(), resolutionInfo.getHeight(),percent,"%"));
                    resolutionInfo.setFormatedString(String.format("%dx%d", resolutionInfo.getWidth(), resolutionInfo.getHeight()));

                } catch (Exception e) {
                    e.printStackTrace();
                }


                newResolutionsList.add(resolutionInfo);
            }

        }
        ResolutionInfo resolutionInfo = new ResolutionInfo();
        resolutionInfo.setFormatedString("Choose a size");
        newResolutionsList.add(0, resolutionInfo);
        return newResolutionsList;

    }


    public List<ResolutionInfo> setResolutionsByPercentages(Context context, ImageInfo imageInfo,
                                                            OnResolutionSelectedListener onResolutionSelectedListener) {

        if (true) {
            if (imageInfo == null) {
                imageInfo = new ImageInfo();
                imageInfo.setWidth(100);
                imageInfo.setHeight(100);
            }
        }

        int orgWidth = imageInfo.getWidth();
        int orgHeight = imageInfo.getHeight();
       /* mResFileContent = getArguments().getString(FileApi.RES_FILE_NAME);

        if(mResFileContent==null){
            mResFileContent = loadResolutionsFromAssets(getContext());
        }
        if(mResFileContent==null){
            return;
        }*/
        List<String> nameList;
        int newMaxResolution = 0;
//        int index =-1;
        int[] percentages = {10, 20, 25, 40, 50, 60, 75};

        String maxRes = null;//a[0];


        //calculate aspect ratio height , lets make width fix
        int newHeight = 0;
//                    (int )Utils.calculateAspectRatioHeight(new Point(orgWidth,orgHeight),mWidth).y;
        int newWidth = 0;
        List<ResolutionInfo> newResolutionsList = new ArrayList<>();
        for (int i = 0; i < percentages.length; i++) {
            int percent = percentages[i];
//                index = maxRes.indexOf("x");
            if (orgWidth > orgHeight) {
                newWidth = (int) (orgWidth * percent / (float) 100);

                newHeight = (int) Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), newWidth).y;
            } else {
                newHeight = (int) (orgHeight * percent / (float) 100);
                newWidth = (int) Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), newHeight).x;
            }

//                int newResolution = newWidth * newHeight;
            ResolutionInfo resolutionInfo = new ResolutionInfo();

            //nameList.remove(maxRes);

            maxRes = String.format("%d %s %d (%.0f", newWidth, "x", newHeight, (float) percent) + "%)";

            resolutionInfo.setHeight(newHeight);
            resolutionInfo.setWidth(newWidth);
            resolutionInfo.setPercentageOfOriginal((int) (float) percent);
            resolutionInfo.setFormatedString(maxRes);
            if (newResolutionsList.contains(resolutionInfo) == false) {
                newResolutionsList.add(resolutionInfo);
            }

        }

        ResolutionInfo resolutionInfo = new ResolutionInfo();
        resolutionInfo.setFormatedString("Select");
        newResolutionsList.add(0, resolutionInfo);

//            return;




       /* ResolutionAdaptor mResolutionAdaptor = new ResolutionAdaptor(context,
                newResolutionsList, onResolutionSelectedListener,
                mRecyclerView, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false),R.layout.resolution_row);

*/
//        mRecyclerView.setAdapter(mResolutionAdaptor);

        return newResolutionsList;
    }

    public void showCustomResolutionAlert(final OnResolutionSelectedListener onResolutionSelectedListener, Bitmap mBitmap, final long maxResolution, final long maxWidth,
                                          final long meanDimension) {

//       checked =true;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.alert_custom_resolution, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextWidth = (EditText) dialogView.findViewById(R.id.width);
//       editText.setText("test label");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle(getString(R.string.dialog_title));


        final EditText editTextHeight = (EditText) dialogView.findViewById(R.id.height);

//        ResolutionInfo resolutionInfo = new ResolutionInfo();
//        resolutionInfo.setWidth(mBitmap.getWidth());
//        resolutionInfo.setHeight(mBitmap.getHeight());

        final MyTextWatcher textWatcherWidth = new MyTextWatcher(editTextWidth, editTextHeight, null);
        editTextWidth.addTextChangedListener(textWatcherWidth);

        final MyTextWatcher textWatcherHeight = new MyTextWatcher(editTextHeight, editTextWidth, null);
        textWatcherWidth.setOther(textWatcherHeight);

        textWatcherHeight.setOther(textWatcherWidth);

        final CheckBox checkBox = dialogView.findViewById(R.id.check_aspect);
        checkBox.setOnCheckedChangeListener(textWatcherHeight);


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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
//                onResolutionSelected(s1+"x"+s2);
            }
        });

    }

    public void showFIleSize(ImageInfo imageInfo, TextView textView) {


        String res = String.format(" %s x %s", imageInfo.getWidth(), imageInfo.getHeight());
//        float fileSize =bitmap.getByteCount()/(4*3);
        String output = imageInfo.getFormatedFileSize();


//        mFileSize = (long) fileSize;
        textView.setText(String.format("%s (%s) ", res, output));
    }

}


