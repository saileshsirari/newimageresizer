package apps.sai.com.imageresizer.resize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.listener.OnResolutionSelectedListener;

/**
 * Created by sailesh on 06/01/18.
 */

public class ResolutionAdaptor extends RecyclerView.Adapter<ResolutionAdaptor.ResolutionHolder> {
    List<ResolutionInfo> mResolutionInfoList;
    Context mContext;
    RecyclerView mRecyclerView;
    OnResolutionSelectedListener mOnResolutionSelectedListener;
    int mResLayoutFile;


    public ResolutionAdaptor(Context context, Iterable<ResolutionInfo> resolveInfoIterable, OnResolutionSelectedListener onResolutionSelectedListener,
                             RecyclerView recyclerView, RecyclerView.LayoutManager layoutManager, int resLayoutFile) {
        mContext = context;
        mRecyclerView = recyclerView;

        mRecyclerView.setLayoutManager(layoutManager);

        mResLayoutFile = resLayoutFile;

        mResolutionInfoList = new ArrayList<>();
        for (Iterator<ResolutionInfo> i = resolveInfoIterable.iterator(); i.hasNext(); ) {

            ResolutionInfo resolutionInfo = i.next();
            mResolutionInfoList.add(resolutionInfo);
        }
        mOnResolutionSelectedListener = onResolutionSelectedListener;

    }


    @Override
    public ResolutionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (mResLayoutFile == 0) {
            view = LayoutInflater.from(mContext).inflate(R.layout.resolution_row, null);
        } else {
            view = LayoutInflater.from(mContext).inflate(mResLayoutFile, null);

        }
        return new ResolutionHolder(view);
    }

    @Override
    public void onBindViewHolder(ResolutionHolder holder, int position) {
        ResolutionInfo resolutionInfo = mResolutionInfoList.get(position);

        holder.textView.setText(resolutionInfo.getFormatedString());
        holder.textView.setTag(resolutionInfo);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnResolutionSelectedListener.onResolutionSelected((ResolutionInfo) view.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResolutionInfoList.size();
    }

    public static class ResolutionHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView textView;
        public ResolutionHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            textView = itemView.findViewById(R.id.text_res);
        }
    }

}
