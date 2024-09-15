package apps.sai.com.imageresizer.myimages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apps.sai.com.imageresizer.BaseFragment
import apps.sai.com.imageresizer.R
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.FileApi
import apps.sai.com.imageresizer.data.ImageInfo
import apps.sai.com.imageresizer.myimages.MyImagesAdaptor.OnImagesSelectedListener
import apps.sai.com.imageresizer.myimages.MyImagesAdaptor.OnUiUpdateListener
import apps.sai.com.imageresizer.select.SelectActivity
import apps.sai.com.imageresizer.settings.SettingsManager
import apps.sai.com.imageresizer.util.ImageInfoLoader
import apps.sai.com.imageresizer.util.ImageOperations
import kotlinx.coroutines.launch

/**
 * Created by sailesh on 20/01/18.
 */
class MyImagesFragment : BaseFragment(), MyImagesContract.View, OnImagesSelectedListener {

    interface OnImageDeletedListener {
        fun onImageDeleted(imageInfo: ImageInfo?)
        fun onAllImagesDeleted()
    }

    var onImageDeletedListener: OnImageDeletedListener? = null
    override fun showAd() {
        val selectActivity = activity as SelectActivity?
        //        selectActivity.showFullScreenAd();
    }

    override fun showError(th: Throwable) {
    }

    override fun processGalleryImage(data: Intent) {
    }

    var dataApi: DataApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataApi = FileApi(context)
        setHasOptionsMenu(true)
    }

    private lateinit var mRecyclerView: RecyclerView
    private var mProgressBar: ProgressBar? = null
    private lateinit var mNoImagesTextView: TextView
    private var mHandler: Handler = Handler()
    private var myImagesPresenter: MyImagesPresenter? = null
    private var myImagesAdaptor: MyImagesAdaptor? = null
    private var menu: Menu? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.resized_images_view, null)
        mRecyclerView = view.findViewById(R.id.multiple_image_recycler_view)
        mProgressBar = view.findViewById(R.id.progressMyImages)
        mNoImagesTextView = view.findViewById(R.id.no_images)
        setLoadingIndicator(false)
        val imageInfoList: List<ImageInfo> = ArrayList()
        val size = SettingsManager.getInstance().gridSize
        myImagesPresenter = MyImagesPresenter()
        loadMyImages()
        myImagesAdaptor = MyImagesAdaptor(myImagesPresenter,
            this,
            imageInfoList,
            mRecyclerView,
            GridLayoutManager(
                context, size
            ),
            mOnUiUpdateListener = object : OnUiUpdateListener {
                override fun onDataChanged(newAdaptorSize: Int) {
                    if (newAdaptorSize == 0) {
                        showNoImagesMenu(menu, false)
                    }
                }

                override fun onImageDeleted(imageInfo: ImageInfo) {
                    if (onImageDeletedListener != null) {
                        onImageDeletedListener!!.onImageDeleted(imageInfo)
                    }
                }

                override fun onAllImagesDeleted(mImageInfoList: List<ImageInfo>) {
                    if (onImageDeletedListener != null) {
                        onImageDeletedListener!!.onAllImagesDeleted()
                    }
                }
            }, onULoadingCancelListener = object : MyImagesAdaptor.OnULoadingCancelListener {
                override fun onLoadingCancel() {
                    cancelLoading(false)
                }

            })

        myImagesAdaptor?.setOnDetailedScreenLaunchedListener(object :
            MyImagesAdaptor.OnDetailedScreenLaunchedListener {
            override fun onDetailScreenLaunched() {
                menu?.clear()
            }
        })
        mRecyclerView.setAdapter(myImagesAdaptor)
        return view
    }

    @Synchronized
    fun loadMyImages() {
        lifecycleScope.launch {
            myImagesPresenter?.getImages(requireContext())?.collect { imageInfoList ->
                myImagesAdaptor?.setItems(imageInfoList.reversed().toMutableList())
                if (imageInfoList.isNotEmpty()) {
                    showNoImagesMenu(menu, true)
                }
                loadImagesInfo(imageInfoList)
            }
        }
    }

    private fun loadImagesInfo(imageInfoList: List<ImageInfo>) {
        dataApi?.let { dataApi ->
            for (i in imageInfoList.indices) {
                val imageInfoInner = imageInfoList[i]
                val imageInfoLoader =
                    ImageInfoLoader(
                        imageInfoInner, dataApi,
                        ImageOperations.IMAGE_INFO_LOAD
                    )
                val info = imageInfoLoader.process(requireContext())
                if (info.width == 0 || info.height == 0) {
                    myImagesAdaptor?.remove(info)
                } else {
                    myImagesAdaptor?.setImageInfo(info)
                }
            }
        }
    }

    @Volatile
    private var mDone = false
    fun cancelLoading(delete: Boolean) {
        mDone = true
        if (delete) {
            myImagesAdaptor!!.deleteAllItems()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        val appCompatActivity = activity as AppCompatActivity?
        appCompatActivity!!.supportActionBar!!.displayOptions =
            ActionBar.DISPLAY_SHOW_HOME
        appCompatActivity.supportActionBar!!.setDisplayShowCustomEnabled(true)
        inflater.inflate(R.menu.myimages, menu)
        this.menu = menu
        if (myImagesAdaptor!!.itemCount == 0) {
            showNoImagesMenu(menu, false)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showNoImagesMenu(menu: Menu?, hide: Boolean) {
        if (menu == null) {
            return
        }
        if (!hide) {
            mNoImagesTextView.visibility = View.VISIBLE
            menu.setGroupEnabled(R.id.my_images_group, false)
            menu.setGroupEnabled(R.id.my_images_select_all_group, false)
            menu.setGroupEnabled(R.id.my_images_clear_all_group, false)
        } else {
            mNoImagesTextView.visibility = View.GONE
            menu.setGroupEnabled(R.id.my_images_group, true)
            menu.setGroupEnabled(R.id.my_images_select_all_group, true)
            menu.setGroupEnabled(R.id.my_images_clear_all_group, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelLoading(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_select_all_my_images) {
            myImagesAdaptor!!.selectAllItems()
            menu!!.setGroupEnabled(R.id.my_images_clear_all_group, true)
            menu!!.setGroupEnabled(R.id.my_images_select_all_group, false)
            menu!!.setGroupEnabled(R.id.my_images_group, true)
        } else if (itemId == R.id.action_clear_all_my_images) {
            myImagesAdaptor!!.clearAllItems()
            menu!!.setGroupEnabled(R.id.my_images_select_all_group, true)
            menu!!.setGroupEnabled(R.id.my_images_clear_all_group, false)
            menu!!.setGroupEnabled(R.id.my_images_group, false)
        } else if (itemId == R.id.action_delete_my_image) { //                cancelPendingTasks();

            showDeleteAlert(context) {
                myImagesAdaptor?.setOnUiUpdateListener(object : OnUiUpdateListener {
                    override fun onDataChanged(newAdaptorSize: Int) {
                    }

                    override fun onImageDeleted(imageInfo: ImageInfo) {
                    }

                    override fun onAllImagesDeleted(mImageInfoList: List<ImageInfo>) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            for (imageInfo in mImageInfoList) {
                                myImagesAdaptor?.remove(imageInfo)
                            }
                            mProgressBar!!.visibility = View.GONE
                        }, 2000)
                    }
                })
                mProgressBar!!.visibility = View.VISIBLE
                myImagesAdaptor!!.deleteSelectedImages()
            }
        } else if (itemId == R.id.action_share_my_image) {
            shareImage(requireContext(), null)
        }
        if (myImagesAdaptor!!.itemCount == 0) {
            showNoImagesMenu(menu, false)
        } else {
            mNoImagesTextView!!.visibility = View.GONE
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onGalleryImageSelected(data: Intent) {
    }

    override fun shareImage(context: Context, mUrlString: String?) {
        val uriList = ArrayList<Uri>()
        //multiple
        val imageInfoList = myImagesAdaptor!!.selectedImageInfoList
        for (i in imageInfoList.indices) {
            uriList.add(imageInfoList[i].imageUri)
        }

        if (uriList.size > 0) {
            shareImageMultiple(context, uriList)
        }
    }

    override fun setLoadingIndicator(b: Boolean) {
        mHandler.post {
            if (b == true) {
                mProgressBar!!.visibility = View.VISIBLE
            } else {
                mProgressBar!!.visibility = View.GONE
            }
        }
    }

    override fun showResult(result: String) {
    }

    override fun showError(errorId: Int) {
    }

    override fun setImages(imageInfoList: List<ImageInfo>) {
        if (imageInfoList.isNotEmpty()) {
            showNoImagesMenu(menu, true)
            myImagesAdaptor?.setItems(imageInfoList.toMutableList())
        }
        loadImagesInfo(imageInfoList)
    }

    override fun onAnyImageSelected() {
        menu!!.setGroupEnabled(R.id.my_images_group, true)
        menu!!.setGroupEnabled(R.id.my_images_select_all_group, true)
    }

    override fun onNoneImageSelected() {
        //hide menu bar items
        menu!!.setGroupEnabled(R.id.my_images_group, false)
        menu!!.setGroupEnabled(R.id.my_images_select_all_group, true)
    }

    companion object {
        @JvmStatic
        fun newInstance(): MyImagesFragment {
            val args = Bundle()

            val fragment = MyImagesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
