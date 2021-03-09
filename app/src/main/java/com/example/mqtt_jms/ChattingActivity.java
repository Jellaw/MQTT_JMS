package com.example.mqtt_jms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChattingActivity extends AppCompatActivity implements MqttCallback {
    private RecyclerView mMessageRecycler;
    MessageListAdapter mMessageAdapter;
    ArrayList<UserMessage> messageList = new ArrayList<>();
    LinearLayout chatbox;
    //=================================
    private static final String BROKER_URI = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC = "testtopic/2";
    private static final int QOS = 2;
    // user name for the chat
    private static final String USER_NAME = Build.DEVICE;
    String name;
    ImageView avaUser;
    TextView nameUser;
    EditText chattext;

    // global types
    private MqttAndroidClient client;
    private EditText textMessage;
    private String message;
    Handler mHandler;
    Intent i;
    ImageView back_btn, camera, location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chatting);
        chatbox=findViewById(R.id.layout_chatbox);
        chattext=findViewById(R.id.edittext_chatbox);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //set chatbox match parent khi typing
        chattext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                camera.setVisibility(View.INVISIBLE);
                location.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                chatbox.setLayoutParams(param);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        //=======RecycleView=====================================================
        mMessageRecycler = (RecyclerView) findViewById(R.id.messenger_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mMessageRecycler.setLayoutManager(manager);
        mMessageRecycler.setHasFixedSize(true);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        //=====================switch info from fragment==========================
        i = getIntent();
        avaUser.setImageResource(i.getIntExtra("img_ava",0));
        nameUser.setText(i.getStringExtra("nameUser"));

        //=========================================================================
        // get text elements to re-use them
        textMessage = (EditText) findViewById(R.id.edittext_chatbox);

        // when the activity is created call to connect to the broker
        connect();
        this.mHandler = new Handler();
        m_Runnable.run();

    }
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            ChattingActivity.this.mHandler.postDelayed(m_Runnable,5000);
        }

    };
    private void connect(){
        // create a new MqttAndroidClient using the current context
        client = new MqttAndroidClient(this, BROKER_URI, USER_NAME);
        client.setCallback(this); // set this as callback to listen for messages

        try{
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // clean session in order to don't get duplicate messages each time we connect

            client.connect(options, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Toast.makeText(ChattingActivity.this, "Ready for chat", Toast.LENGTH_SHORT).show();
                    // once connected call to subscribe to receive messages
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(ChattingActivity.this, "Unavailable chat, cause: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }

            });
        } catch (MqttException e){
            Toast.makeText(this, "ERROR, client not connected to broker in " + BROKER_URI, Toast.LENGTH_LONG).show();
        }
    }

    public void publish(View view) {
        // we are in the right view?
        if (view.getId() == R.id.button_chatbox_send) {
            // we only publish if connected
            if (null != client && client.isConnected()) {

                message = textMessage.getText().toString();
                // we only publish if there is message to publish
                if (!message.isEmpty()) {
                    name = USER_NAME;
                    message = name+" " + message.toString();
                    textMessage.setText("");
                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(QOS);
                    try {
                        client.publish(TOPIC, mqttMessage, null, new IMqttActionListener() {

                            @Override
                            public void onSuccess(IMqttToken iMqttToken) {
                                //Toast.makeText(MainActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                                Toast.makeText(ChattingActivity.this, "Failed on publish, cause: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        });
                    } catch (MqttException e) {
                        Toast.makeText(this, " ERROR, an error occurs when publishing", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "WARNING, client not connected", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void subscribe() {
        try {
            client.subscribe(TOPIC, QOS, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    //Toast.makeText(MainActivity.this, "Subscribed to:" + TOPIC, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Toast.makeText(ChattingActivity.this, "Failed on subscribe, cause: " + throwable, Toast.LENGTH_LONG).show();
                }

            });

        } catch (MqttException e) {
            Toast.makeText(this, "ERROR, an error occurs when subscribing", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(this, "Connection lost!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Toast.makeText(this, "message arrived", Toast.LENGTH_SHORT).show();
        // format message in html
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());


        if (message.toString().contains(Build.DEVICE)) {
            String message1 = message.toString().substring(message.toString().indexOf(" "));
            messageList.add(new UserMessage(name, message1.toString(), currentTime));
        } else {
            String message1 = message.toString().substring(message.toString().indexOf(" "));
            messageList.add(new UserMessage("", message1.toString(), currentTime));
        }
        mMessageAdapter.notifyDataSetChanged();


    }
}
