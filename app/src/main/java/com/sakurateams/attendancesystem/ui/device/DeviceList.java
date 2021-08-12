package com.sakurateams.attendancesystem.ui.device;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sakurateams.attendancesystem.MainActivity;
import com.sakurateams.attendancesystem.R;
import com.sakurateams.attendancesystem.bluetooth.BluetoothController;
import com.sakurateams.attendancesystem.models.SingleContent;
import com.sakurateams.attendancesystem.ui.home.HomeFragment;
import com.sakurateams.attendancesystem.view.DeviceRecyclerViewAdapter;
import com.sakurateams.attendancesystem.view.ListInteractionListener;
import com.sakurateams.attendancesystem.view.RecyclerViewProgressEmptySupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class DeviceList extends Fragment implements ListInteractionListener<BluetoothDevice> {

    private DeviceListViewModel mViewModel;
    public static View myview;
    /**
     * Tag string used for logging.
     */
    private static final String TAG = "Attendance System";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");
    /**
     * The controller for Bluetooth functionalities.
     */
    private BluetoothController bluetooth;

    /**
     * The Bluetooth discovery button.
     */
    private FloatingActionButton fab;

    /**
     * Progress dialog shown during the pairing process.
     */
    private ProgressDialog bondingProgressDialog;

    /**
     * Adapter for the recycler view.
     */
    private DeviceRecyclerViewAdapter recyclerViewAdapter;

    private RecyclerViewProgressEmptySupport recyclerView;

    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    public static OutputStream outputStream;
    public static InputStream inputStream;
    public static boolean btConnected = false;
    public static ConnectedThread mConnectedThread;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String receivestring = "";
    Fragment me=this;

    public static DeviceList newInstance() {
        return new DeviceList();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        myview = inflater.inflate(R.layout.device_list_fragment, container, false);
        //return inflater.inflate(R.layout.device_list_fragment, container, false);

        // Sets up the RecyclerView.
        this.recyclerViewAdapter = new DeviceRecyclerViewAdapter(this);
        this.recyclerView = (RecyclerViewProgressEmptySupport) myview.findViewById(R.id.list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(myview.getContext()));

        // Sets the view to show when the dataset is empty. IMPORTANT : this method must be called
        // before recyclerView.setAdapter().
        View emptyView = myview.findViewById(R.id.empty_list);
        this.recyclerView.setEmptyView(emptyView);

        // Sets the view to show during progress.
        ProgressBar progressBar = (ProgressBar) myview.findViewById(R.id.progressBar);
        this.recyclerView.setProgressView(progressBar);

        this.recyclerView.setAdapter(recyclerViewAdapter);

        // [#11] Ensures that the Bluetooth is available on this device before proceeding.
        boolean hasBluetooth = myview.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        if (!hasBluetooth) {
            AlertDialog dialog = new AlertDialog.Builder(myview.getContext()).create();
            dialog.setTitle(getString(R.string.bluetooth_not_available_title));
            dialog.setMessage(getString(R.string.bluetooth_not_available_message));
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Closes the dialog and terminates the activity.
                            dialog.dismiss();
                            //myview.getContext().finish();
                        }
                    });
            dialog.setCancelable(false);
            dialog.show();
        }

        // Sets up the bluetooth controller.
        this.bluetooth = new BluetoothController(MainActivity.activity, BluetoothAdapter.getDefaultAdapter(), recyclerViewAdapter);

        fab = (FloatingActionButton) myview.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If the bluetooth is not enabled, turns it on.
                if (!bluetooth.isBluetoothEnabled()) {
                    Snackbar.make(view, R.string.enabling_bluetooth, Snackbar.LENGTH_SHORT).show();
                    bluetooth.turnOnBluetoothAndScheduleDiscovery();
                } else {
                    //Prevents the user from spamming the button and thus glitching the UI.
                    if (!bluetooth.isDiscovering()) {
                        // Starts the discovery.
                        Snackbar.make(view, R.string.device_discovery_started, Snackbar.LENGTH_SHORT).show();
                        bluetooth.startDiscovery();
                    } else {
                        Snackbar.make(view, R.string.device_discovery_stopped, Snackbar.LENGTH_SHORT).show();
                        bluetooth.cancelDiscovery();
                    }
                }
            }
        });



        return myview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DeviceListViewModel.class);
        // TODO: Use the ViewModel
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(BluetoothDevice device) {
        Log.d(TAG, "Item clicked : " + BluetoothController.deviceToString(device));
        if (bluetooth.isAlreadyPaired(device)) {
            Log.d(TAG, "Device already paired!");
            this.device = device;
            //Toast.makeText(this, R.string.device_already_paired, Toast.LENGTH_SHORT).show();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(myview.getContext(),
                    R.style.AppTheme_AppBarOverlay)
                    .setMessage("Device already paired, Connect ?")
                    .setPositiveButton("Yes",dialogClickListener)
                    .setNegativeButton("No", dialogClickListener);

            AlertDialog alert11 = builder.create();
            alert11.show();
        } else {
            Log.d(TAG, "Device not paired. Pairing.");
            boolean outcome = bluetooth.pair(device);

            // Prints a message to the user.
            String deviceName = BluetoothController.getDeviceName(device);
            if (outcome) {
                // The pairing has started, shows a progress dialog.
                Log.d(TAG, "Showing pairing dialog");
                bondingProgressDialog = ProgressDialog.show(myview.getContext(), "", "Pairing with device " + deviceName + "...", true, false);
            } else {
                Log.d(TAG, "Error while pairing with device " + deviceName + "!");
                Toast.makeText(myview.getContext(), "Error while pairing with device " + deviceName + "!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startLoading() {
        this.recyclerView.startLoading();

        // Changes the button icon.
        this.fab.setImageResource(R.drawable.ic_bluetooth_searching_white_24dp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endLoading(boolean partialResults) {
        this.recyclerView.endLoading();

        // If discovery has ended, changes the button icon.
        if (!partialResults) {
            fab.setImageResource(R.drawable.ic_bluetooth_white_24dp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endLoadingWithDialog(boolean error, BluetoothDevice device) {
        if (this.bondingProgressDialog != null) {
            //View view = myview.findViewById(R.id.main_content);
            String message;
            String deviceName = BluetoothController.getDeviceName(device);

            // Gets the message to print.
            if (error) {
                message = "Failed pairing with device " + deviceName + "!";
            } else {
                message = "Succesfully paired with device " + deviceName + "!";
            }

            // Dismisses the progress dialog and prints a message to the user.
            this.bondingProgressDialog.dismiss();
            Snackbar.make(myview, message, Snackbar.LENGTH_SHORT).show();

            // Cleans up state.
            this.bondingProgressDialog = null;
        }

    }

    /**
     * {@inheritDoc}
     */
    /*@Override
    protected void onDestroy() {
        bluetooth.close();
        super.onDestroy();
    }*/

    /**
     * {@inheritDoc}
     */
    /*@Override
    protected void onRestart() {
        super.onRestart();
        // Stops the discovery.
        if (this.bluetooth != null) {
            this.bluetooth.cancelDiscovery();
        }
        // Cleans the view.
        if (this.recyclerViewAdapter != null) {
            this.recyclerViewAdapter.cleanView();
        }
    }*/

    /**
     * {@inheritDoc}
     */
    //@Override
    /*protected void onStop() {
        super.onStop();
        // Stoops the discovery.
        if (this.bluetooth != null) {
            this.bluetooth.cancelDiscovery();
        }
    }*/

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //reloadAd();
        }
    }*/

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"Error: "+e.getMessage());
            connected=false;
        }
        if(connected)
        {
            this.btConnected = true;
            Toast.makeText(myview.getContext(), "Device is connected", Toast.LENGTH_SHORT).show();

            mConnectedThread = new ConnectedThread(socket);
            mConnectedThread.start();
            //mConnectedThread.run();
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String sendstr = "TIME "+currentTime+"\n";
            try {
                byte[] buffer = sendstr.getBytes("UTF-8");
                mConnectedThread.write(buffer);
            } catch (Exception e){
                Log.e(TAG,"Error: "+e.getMessage());
            }



        }
        return connected;
    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //if (BTconnect()){
//
                    //}
                    ConnectThread connect = new ConnectThread(device);
                    connect.start();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(PORT_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        Log.d(TAG, "manageMyConnectedSocket: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void back(){
        ((FragmentActivity) MainActivity.context).getSupportFragmentManager().popBackStack();
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            // set TIME
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String sendstr = "TIME "+currentTime+"\n";
            try {
                byte[] buffer = sendstr.getBytes("UTF-8");
                mmOutStream.write(buffer);
            } catch (Exception e){
                Log.e(TAG,"Error: "+e.getMessage());
            }
            back();
            //((FragmentActivity) MainActivity.context).getSupportFragmentManager().popBackStack();
            //getActivity().onBackPressed();
            //getFragmentManager().popBackStack();
        }

        public void run() {
            mmBuffer = new byte[1024];
            //final int numBytes ; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    final int numBytes = mmInStream.read(mmBuffer);
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
                    receivestring += incomingMessage;
                    //if (mmBuffer[numBytes]=='\n')
                    //Log.d(TAG, "InputStream: " + incomingMessage);

                    MainActivity.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //view_data.setText(incomingMessage);
                            //Log.d(TAG, "InputStream: " + incomingMessage);
                            if (mmBuffer[numBytes-1]=='\n') {
                                Log.d(TAG, "receivestring: " + receivestring);
                                String strlist[] = receivestring.split(",");
                                String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                                SingleContent content = new SingleContent();
                                content.setDatetime(currentDate);
                                content.setName(strlist[1]);
                                content.setUid(strlist[0]);
                                content.setInout(strlist[2]);
                                HomeFragment.adapter.add(content);
                                receivestring = "";
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    public void SendMessage(String sendstring) {
        byte[] bytes = new byte[0];//send_data.getText().toString().getBytes(Charset.defaultCharset());
        try {
            bytes = sendstring.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mConnectedThread.write(bytes);
    }

    public void Start_Server(View view) {

        AcceptThread accept = new AcceptThread();
        accept.start();

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("appname", PORT_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    //mmServerSocket.close();
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }



}