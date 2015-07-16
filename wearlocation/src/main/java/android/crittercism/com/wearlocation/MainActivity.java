package android.crittercism.com.wearlocation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.whitewaterlabs.com.wearlocation.R;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView mLongitudeView;
    private TextView mLatitudeView;
    private TextView mUpdatedView;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crittercism.initialize(getApplicationContext(), "YOUR_API_KEY");

        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mLongitudeView = (TextView) stub.findViewById(R.id.longitude);
                mLatitudeView = (TextView) stub.findViewById(R.id.latitude);
                mUpdatedView = (TextView) stub.findViewById(R.id.updated);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int flag) {}

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("WATCH", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        // iterate through the given events
        for (DataEvent event : dataEvents) {

            // if the event is a change (other option: delete)
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // pull out the item from the event
                DataItem item = event.getDataItem();

                // check to make sure it's the location
                if (item.getUri().getPath().compareTo("/location") == 0) {

                    // de-serialize the DataMap object with the data
                    final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    // create a main thread handler
                    Handler mainThread = new Handler(Looper.getMainLooper());

                    // create a runnable that will be run on the main thread
                    Runnable myRunnable = new Runnable(){

                        @Override public void run() {
                            // pull out our attributes and update the UI
                            mLatitudeView.setText(Double.toString(dataMap.getDouble("latitude")));
                            mLongitudeView.setText(Double.toString(dataMap.getDouble("longitude")));
                            mUpdatedView.setText(dataMap.getString("updated"));
                        }
                    };

                    // execute the runnable
                    mainThread.post(myRunnable);

                }
            }
        }

    }
}
