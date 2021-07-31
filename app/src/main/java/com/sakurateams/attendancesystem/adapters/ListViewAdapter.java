package com.sakurateams.attendancesystem.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sakurateams.attendancesystem.R;
import com.sakurateams.attendancesystem.models.SingleContent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.AttendanceRowHolder> {
    private static final String TAG = "Attendance System";
    private ArrayList<SingleContent> itemsList;
    Context mContext;

    RecyclerView recyclerView;
    private static final int ITEM = 0;
    private static final int LOADING = 1;
    String currentId = "";
    int position = 0;
    ListViewAdapter.AttendanceRowHolder myholder;

    //private List<SingleContent> appResults;
    private Context context;

    private boolean isLoadingAdded = false;
    public ListViewAdapter(Context context) {
        //this.itemsList = itemsList;
        this.mContext = context;
        itemsList = new ArrayList<>();
    }

    @Override
    public ListViewAdapter.AttendanceRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = null;

            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_list, null);

        ListViewAdapter.AttendanceRowHolder mh = new ListViewAdapter.AttendanceRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(final ListViewAdapter.AttendanceRowHolder holder, final int i) {
        final SingleContent content = itemsList.get(i);
            holder.tvNameList.setText(content.getName());
            //Log.i(MainActivity.TAG, "1.Title: " + content.getTitle());
            holder.tvDateList.setText(content.getDatetime());

            holder.tvInOutList.setText(content.getInout());
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class AttendanceRowHolder extends RecyclerView.ViewHolder {
        protected TextView tvNameList;
        protected TextView tvDateList;
        //protected ImageView imageList;
        protected TextView tvInOutList;

        protected CardView cvList;

        public AttendanceRowHolder(View view) {
            super(view);

            this.tvNameList = (TextView) view.findViewById(R.id.name_list);
            this.tvDateList = (TextView) view.findViewById(R.id.date_list);
            //this.imageList = (ImageView) view.findViewById(R.id.picture_list);
            this.tvInOutList = (TextView) view.findViewById(R.id.inout_list);
            cvList = (CardView) view.findViewById(R.id.card_list);
            //-----------------------

            ;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    public void add(SingleContent r) {
        itemsList.add(r);
        notifyItemInserted(itemsList.size() - 1);
    }

    public void addAll(List<SingleContent> moveResults) {
        for (SingleContent result : moveResults) {
            add(result);
            //Log.i(TAG,"moveResults title: "+result.getTitle());
        }
    }

    public void remove(SingleContent r) {
        int position = itemsList.indexOf(r);
        if (position > -1) {
            itemsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        //add(new SingleContent());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = itemsList.size() - 1;
        SingleContent result = getItem(position);

        if (result != null) {
            itemsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public SingleContent getItem(int position) {
        return itemsList.get(position);
    }



    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };




}
