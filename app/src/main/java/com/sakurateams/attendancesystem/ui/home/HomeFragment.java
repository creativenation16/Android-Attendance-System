package com.sakurateams.attendancesystem.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakurateams.attendancesystem.MainActivity;
import com.sakurateams.attendancesystem.R;
import com.sakurateams.attendancesystem.adapters.ListViewAdapter;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    public static ListViewAdapter adapter;
    RecyclerView rv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rv = (RecyclerView) view.findViewById(R.id.home_recycler);
        LinearLayoutManager recyclerViewLayoutManager = new LinearLayoutManager(MainActivity.context, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(recyclerViewLayoutManager);
        adapter = new ListViewAdapter(getContext());
        rv.setAdapter(adapter);

        return view;
    }
}