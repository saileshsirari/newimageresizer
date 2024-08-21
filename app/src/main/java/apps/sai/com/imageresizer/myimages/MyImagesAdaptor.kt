package apps.sai.com.imageresizer.myimages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import apps.sai.com.imageresizer.BaseFragment
import apps.sai.com.imageresizer.R
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.FileApi
import apps.sai.com.imageresizer.data.ImageInfo
import apps.sai.com.imageresizer.resize.ResizeFragment.OnImagedSavedListener
import apps.sai.com.imageresizer.settings.SettingsManager
import apps.sai.com.imageresizer.util.ImageDetailFragment
import apps.sai.com.imageresizer.util.SpacesItemDecoration
import apps.sai.com.imageresizer.util.Utils
import java.io.File

/**
 * Created by sailesh on 20/01/18.
 */
class MyImagesAdaptor(
    myImagesPresenter: MyImagesPresenter?,
    context: BaseFragment,
    imageInfoList: List<ImageInfo>,
    recyclerView: RecyclerView,
    layoutManager: GridLayoutManager,
    var mOnUiUpdateListener: OnUiUpdateListener?,
    onULoadingCancelListener: OnULoadingCancelListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnImagedSavedListener {
    private var mImageInfoList: MutableList<ImageInfo> = mutableListOf()
    private var mContext: BaseFragment = context
    private var mRecyclerView: RecyclerView = recyclerView
    var mDataApi: DataApi
    private val adImageInfoList: List<ImageInfo>

    override fun onImageSaved(imageInfo: ImageInfo) {
        reload()
    }

    fun remove(imageInfo: ImageInfo) {
        val index1 = mImageInfoList.indexOf(imageInfo)
        if (index1 != -1) {
            mImageInfoList.removeAt(index1)
            notifyItemRemoved(index1)
        }
        val index = mSelectedImageInfoList.indexOf(imageInfo)

        if (index != -1) {
            mSelectedImageInfoList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun setItems(imageInfoList: MutableList<ImageInfo>) {
        setAdImages(imageInfoList, layoutManager)
        this.mImageInfoList = imageInfoList
        notifyDataSetChanged()
    }

    interface OnUiUpdateListener {
        fun onDataChanged(newAdaptorSize: Int)
        fun onImageDeleted(imageInfo: ImageInfo)
        fun onAllImagesDeleted(mImageInfoList: List<ImageInfo>)
    }

    interface OnULoadingCancelListener {
        fun onLoadingCancel()
    }

    private var mOnDetailedScreenLaunchedListener: OnDetailedScreenLaunchedListener? = null

    interface OnDetailedScreenLaunchedListener {
        fun onDetailScreenLaunched()
    }

    fun setOnDetailedScreenLaunchedListener(mOnDetailedScreenLaunchedListener: OnDetailedScreenLaunchedListener?) {
        this.mOnDetailedScreenLaunchedListener = mOnDetailedScreenLaunchedListener
    }

    private var mOnULoadingCancelListener: OnULoadingCancelListener?

    fun setOnUiUpdateListener(mOnUiUpdateListener: OnUiUpdateListener?) {
        this.mOnUiUpdateListener = mOnUiUpdateListener
    }

    private fun getNonAdList(imageInfoList: List<ImageInfo>?): List<ImageInfo> {
        val nonAdImageInfoList: MutableList<ImageInfo> = ArrayList()
        for (imageInfo in imageInfoList!!) {
            if (imageInfo.isAd == false) {
                nonAdImageInfoList.add(imageInfo)
            }
        }
        return nonAdImageInfoList
    }

    fun selectAllItems() {
        mSelectedImageInfoList.clear()
        mSelectedImageInfoList.addAll(getNonAdList(mImageInfoList))
        notifyDataSetChanged()
    }

    fun clearAllItems() {
        mSelectedImageInfoList.clear()
        notifyDataSetChanged()
    }

    fun deleteAllItems() {
        mSelectedImageInfoList.clear()
        mImageInfoList.clear()
        notifyDataSetChanged()
    }

    private var mCount: Int = 0
    private var shownError: Boolean = false

    fun deleteSelectedImages() {
        mCount = 0
        if (mOnULoadingCancelListener != null) {
            mOnULoadingCancelListener!!.onLoadingCancel()
        }
        shownError = false
        val imageInfoList: MutableList<ImageInfo> = ArrayList()

        for (imageInfo in mSelectedImageInfoList) {
            if (imageInfo.isAd) {
                continue
            }
            if (imageInfo.imageUri != null) {
                val imageInfoProcessed =
                    Utils.getImageInfo(imageInfo, mContext.context, imageInfo.imageUri, mDataApi)
                imageInfoProcessed.imageUri = imageInfo.absoluteFilePathUri
                imageInfoList.add(imageInfoProcessed)
            }
        }
        mDataApi.deleteImageFiles(mContext.context as Activity?, imageInfoList)
        if (mOnUiUpdateListener != null) {
            val list: MutableList<ImageInfo> = ArrayList()
            for (imageInfo in imageInfoList) {
                if (!imageInfo.isDeleted) {
                    list.add(imageInfo)
                }
            }
            mOnUiUpdateListener!!.onAllImagesDeleted(list)
        }
    }

    private fun showError(errorId: Int) {
        try {
            val s = mContext.requireActivity().getString(errorId)
            Toast.makeText(mContext.context, s, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }

    fun setImageInfo(imageInfo: ImageInfo): Int {
        val index = mImageInfoList.indexOf(imageInfo)
        if (index != -1) {
            mImageInfoList[index] = imageInfo
            notifyItemChanged(index)
        }
        return index
    }

    interface OnImagesSelectedListener {
        fun onAnyImageSelected()
        fun onNoneImageSelected()
    }

    private var mOnImagesSelectedListener: OnImagesSelectedListener
    private var myImagesPresenter: MyImagesPresenter?

    private var appereance: Int

    private var repeatAfter = 0
    private val layoutManager: GridLayoutManager

    private fun setAdImages(
        imageInfoList: MutableList<ImageInfo>,
        layoutManager: GridLayoutManager
    ) {
        if (appereance == 0) { //large
            repeatAfter = layoutManager.spanCount * 2 //after these rows show ad
        } else if (appereance == 2) { //small
            repeatAfter = layoutManager.spanCount * 5
        } else if (appereance == 1) { //normal
            repeatAfter = layoutManager.spanCount * 3
        }
        repeatAfter *= 2

        //remove old ads if any
        val size = imageInfoList.size
        val nonAdImageInfoList: MutableList<ImageInfo> = ArrayList()
        for (i in imageInfoList.indices) {
            if (!imageInfoList[i].isAd) {
                nonAdImageInfoList.add(imageInfoList[i])
            }
        }
        imageInfoList.clear()
        imageInfoList.addAll(nonAdImageInfoList)
        var i = repeatAfter
        while (i < size) {
            val imageInfo = ImageInfo()
            imageInfo.isAd = true
            imageInfoList.add(i, imageInfo)
            i += repeatAfter
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM) {
            val view =
                LayoutInflater.from(mContext.context).inflate(R.layout.mymultiple_image_row, null)

            return MultipleImagesHolder(view)
        }

        val view = LayoutInflater.from(mContext.context).inflate(R.layout.header_gridview, null)
        val headerView = HeaderView(view)

        return headerView
    }

    internal class HeaderView(var rootView: View) : RecyclerView.ViewHolder(rootView)

    private var mSelectedImageInfoList: MutableList<ImageInfo> = ArrayList()
    override fun onBindViewHolder(holder1: RecyclerView.ViewHolder, position: Int) {
        val imageInfo = mImageInfoList[position]
        if (holder1 is MultipleImagesHolder) {
            val reLayoutParams = RelativeLayout.LayoutParams(
                holder1.imageView.layoutParams.width,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            holder1.imageView.tag = imageInfo
            val span = layoutManager.spanCount
            if (appereance == 0) { //large

                if (span == 1 || span == 2) {
                    reLayoutParams.height =
                        mContext.resources.getDimension(R.dimen.large_app).toInt()
                } else {
                    reLayoutParams.height =
                        mContext.resources.getDimension(R.dimen.large_app).toInt()
                    reLayoutParams.height = reLayoutParams.height - reLayoutParams.height / 3
                }
                holder1.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                holder1.imageView.layoutParams = reLayoutParams
            } else if (appereance == 2) { //small
                reLayoutParams.height = mContext.resources.getDimension(R.dimen.small_app).toInt()
                holder1.imageView.layoutParams = reLayoutParams
                holder1.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            } else if (appereance == 1) { //normal
                if (span == 1 || span == 2) {
                    reLayoutParams.height =
                        mContext.resources.getDimension(R.dimen.normal_app).toInt()
                } else {
                    reLayoutParams.height =
                        mContext.resources.getDimension(R.dimen.normal_app).toInt() / 2
                }
                holder1.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                holder1.imageView.layoutParams = reLayoutParams
            }
            if (imageInfo.absoluteThumbFilePath != null) {
                val file = File(imageInfo.absoluteThumbFilePath)
                holder1.imageView.setImageURI(Uri.fromFile(file))
            }
            if (imageInfo.width > 0 && imageInfo.height > 0) {
                holder1.resTextView.text = imageInfo.width.toString() + " x " + imageInfo.height
            }

            if (imageInfo.formatedFileSize != null && imageInfo.formatedFileSize.length > 0) {
                holder1.sizeTextView.text = String.format("(%s)", imageInfo.formatedFileSize)
            }

            holder1.checkBox.tag = mImageInfoList[position]
            holder1.checkBox.setOnCheckedChangeListener(null)

            holder1.checkBox.isChecked = mSelectedImageInfoList.contains(mImageInfoList[position])

            holder1.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val selImageInfo = buttonView.tag as ImageInfo
                if (isChecked) {
                    if (!mSelectedImageInfoList.contains(selImageInfo)) {
                        mSelectedImageInfoList.add(selImageInfo)
                    }
                } else {
                    mSelectedImageInfoList.remove(selImageInfo)
                }
                if (mSelectedImageInfoList.size > 0) {
                    mOnImagesSelectedListener.onAnyImageSelected()
                } else {
                    mOnImagesSelectedListener.onNoneImageSelected()
                }
            }

            holder1.imageView.setOnClickListener {
                val imageInfoIn = holder1.imageView.tag as ImageInfo
                val intent = Intent()
                val uri = imageInfoIn.imageUri
                intent.setData(uri)

                if (mOnDetailedScreenLaunchedListener != null) {
                    mOnDetailedScreenLaunchedListener!!.onDetailScreenLaunched()
                }


                val imageDetailFragment = ImageDetailFragment.newInstance(uri, null)

                Utils.addFragment(
                    mContext.activity as AppCompatActivity?,
                    imageDetailFragment,
                    R.id.contentFrame,
                    true
                )
            }
        } else if (holder1 is HeaderView) {
            try {
                val next = bannerIds[count++ % (bannerIds.size - 1)]
                Utils.showFacebookBanner(
                    mContext.context,
                    holder1.rootView,
                    R.id.banner_container_top,
                    next
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showFacebookBanner(
                    mContext.context,
                    holder1.rootView,
                    R.id.banner_container_top,
                    bannerIds[0]
                )
            }
        }
    }

    private var count: Int = 0

    private var bannerIds: Array<String> = arrayOf(
        "179547122769778_189237791800711",
        "179547122769778_179622146095609",
        "179547122769778_189046365153187",
        "179547122769778_189237791800711"
    )


    init {
        mRecyclerView.layoutManager = layoutManager
        this.mImageInfoList.addAll(imageInfoList)
        this.myImagesPresenter = myImagesPresenter
        appereance = SettingsManager.getInstance().gridAppearnece
        mOnImagesSelectedListener = context as OnImagesSelectedListener
        this.mOnULoadingCancelListener = onULoadingCancelListener
        mDataApi = FileApi(context.context)
        adImageInfoList = ArrayList()
        this.layoutManager = layoutManager
        val spacingInPixels = context.resources.getDimensionPixelSize(R.dimen.spacing)
        mRecyclerView.addItemDecoration(SpacesItemDecoration(spacingInPixels))
        layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (getItemViewType(position)) {
                    HEADER -> layoutManager.spanCount
                    ITEM -> 1
                    else -> 1
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (!mImageInfoList[position].isAd) {
            return ITEM
        }
        return HEADER
    }


    private fun reload() {
        if (myImagesPresenter != null) {
            try {
                // this.mImageInfoList = myImagesPresenter.getImages(mContext.getContext());
                this.mSelectedImageInfoList = ArrayList()
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val selectedImageInfoList: List<ImageInfo>
        get() = mSelectedImageInfoList


    override fun getItemCount(): Int {
        return mImageInfoList.size
    }

    class MultipleImagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.image_multiple)
        var resTextView: TextView = itemView.findViewById(R.id.text_name_resolution)
        var sizeTextView: TextView = itemView.findViewById(R.id.text_name_size)
        var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    companion object {
        const val HEADER: Int = 0
        const val ITEM: Int = 1
    }
}
