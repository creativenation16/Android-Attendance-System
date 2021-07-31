package com.sakurateams.attendancesystem.ui.card;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sakurateams.attendancesystem.R;
import com.sakurateams.attendancesystem.ui.device.DeviceList;

public class WriteCard extends Fragment implements View.OnClickListener {

    private static final String TAG = "Attendance System";
    private WriteCardViewModel mViewModel;
    private EditText name;
    private Button btnwrite;
    private Button btnstop;

    public static WriteCard newInstance() {
        return new WriteCard();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.write_card_fragment, container, false);

        name = (EditText) view.findViewById(R.id.name_et);
        btnwrite = (Button) view.findViewById(R.id.btnwrite);
        btnstop = (Button) view.findViewById(R.id.btnstop);
        btnwrite.setOnClickListener(this);
        btnstop.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WriteCardViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnwrite:
                if (name.getText().toString()!="") {
                    try {
                        String sendstr = "NAME " + name.getText().toString()+"\n";
                        byte[] bytes = sendstr.getBytes("UTF-8");
                        DeviceList.mConnectedThread.write(bytes);
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                    }
                }
                break;
            case R.id.btnstop:
                try {
                    String sendstr = "STOP\n" + name.getText().toString();
                    byte[] bytes = sendstr.getBytes("UTF-8");
                    DeviceList.mConnectedThread.write(bytes);
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
                break;
        }
    }

}