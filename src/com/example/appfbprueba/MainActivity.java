package com.example.appfbprueba;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.example.android.model.User;
import com.example.android.sqlite.MySQLiteHelper;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;


public class MainActivity extends FragmentActivity  {
	
	 private static final String PERMISSION = "read_stream, user_birthday";
	 private LoginButton loginButton;
	 private static final String TAG = "MainFragment";
	 private UiLifecycleHelper uiHelper;
	 private GraphUser user;
	 private Button postStatusUpdateButton;
	 private Button shareGeneralButton;
	 private Button sendFriendsButton;
	 private Button graphApiUserButton;
	 private Button postGraphApiDialog;
	 private Button postPhoto;
	 private Button goToHtmlButton;
	 private TextView userInfoTextView;
	 private String requestId;

	 
	 private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
	      @Override
	      public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	         // Log.e("Activity", String.format("Error: %s", error.toString()));
	      	 Toast.makeText(getApplicationContext(),
	     			  "Error: " + error.toString(),
	                   Toast.LENGTH_SHORT).show();
	      }
	
	      @Override
	      public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	          //Log.i("Activity", "Success!");
	      	 if (FacebookDialog.getNativeDialogDidComplete(data))
	           {
	      		 //LAS SIGUIENTES LINEAS SIRVEN PARA CUANDO SE HA INSTALADO ANTERIORMENTE LA APP
	               if (FacebookDialog.getNativeDialogCompletionGesture(data) == null 
	                     || FacebookDialog.COMPLETION_GESTURE_CANCEL.equals(FacebookDialog.getNativeDialogCompletionGesture(data)))
	               {
	                   // track cancel    
	              	  Toast.makeText(getApplicationContext(),
	              			  "Al no estar instalada la app. no sabemos si se ha publicado o se ha cancelado. Si esta instalada, se habria cancelado.",
	                            Toast.LENGTH_SHORT).show();
	               }
	               else
	               {
	                   // track post
	              	  Toast.makeText(getApplicationContext(),
	              			  "Se ha publicado",
	                            Toast.LENGTH_SHORT).show();
	               }
	           }
	           else
	           {
	               // track cancel    
	          	  Toast.makeText(getApplicationContext(),
	                        "Se ha cancelado",
	                        Toast.LENGTH_SHORT).show();
	           }
	      }
    };
    
    private void getRequestData(final String inRequestId) {
        // Create a new request for an HTTP GET with the
        // request ID as the Graph path.
        Request request = new Request(Session.getActiveSession(), 
                inRequestId, null, HttpMethod.GET, new Request.Callback() {

                    @Override
                    public void onCompleted(Response response) {
                        // Process the returned response
                        GraphObject graphObject = response.getGraphObject();
                        FacebookRequestError error = response.getError();
                        // Default message
                        String message = "Incoming request";
                        if (graphObject != null) {
                            // Check if there is extra data
                            if (graphObject.getProperty("data") != null) {
                                try {
                                    // Get the data, parse info to get the key/value info
                                    JSONObject dataObject = 
                                    new JSONObject((String)graphObject.getProperty("data"));
                                    // Get the value for the key - badge_of_awesomeness
                                    String badge = 
                                        dataObject.getString("badge_of_awesomeness");
                                    // Get the value for the key - social_karma
                                    String karma = 
                                        dataObject.getString("social_karma");
                                    // Get the sender's name
                                    JSONObject fromObject = 
                                        (JSONObject) graphObject.getProperty("from");
                                    String sender = fromObject.getString("name");
                                    String title = sender+" sent you a gift";
                                    // Create the text for the alert based on the sender
                                    // and the data
                                    message = title + "\n\n" + 
                                        "Badge: " + badge + 
                                        " Karma: " + karma;
                                } catch (JSONException e) {
                                    message = "Error getting request info";
                                }
                            } else if (error != null) {
                                message = "Error getting request info";
                            }
                        }
                        Toast.makeText(getApplicationContext(),
                                message,
                                Toast.LENGTH_LONG).show();
                    }
            });
        // Execute the request asynchronously.
        Request.executeBatchAsync(request);
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	getRequestData(requestId);
    	 if (session.isOpened()) {
    		 //COMPROBACION DE PERMISOS ESPECIALES
    		 if (session.getPermissions().contains("publish_stream")) {
    			 Log.e(TAG, "Logged in...");
	         	setContentView(R.layout.activity_ok);  
		        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
		            @Override
		            public void onCompleted(GraphUser user, Response response) {
	                    if (user != null) {
	                    	MainActivity.this.user = user;
	                    	MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
	                        User user_aux = db.getUser(user.getId().toString());
	                        if(user_aux == null){
	                    	   //  Log.i(TAG,"no existe el usuario");
	                           db.addUser(new User(user.getId().toString(), user.getName().toString())); 
	                        }
//    	                      else{
//    	                    	  Log.i(TAG,"siiii existe el usuario");  
//    	                      }
	                    	TextView userName = (TextView) findViewById(R.id.textViewUserName);
		                	userName.setText(user.getName().toString());
		                	TextView userId = (TextView) findViewById(R.id.textViewUserId);
		                	userId.setText(user.getId().toString());
//    		                	Log.e(TAG, user.getBirthday().toString());
//    		                	Log.e(TAG, user.getFirstName().toString());
//    		                	Log.e(TAG, user.getLastName().toString());
//    		                	Log.e(TAG, user.getLink().toString());
//    		                	Log.e(TAG, user.getUsername().toString());
		                    postStatusUpdateButton = (Button) findViewById(R.id.shareButton); 
		                    //postStatusUpdateButton.setVisibility(View.INVISIBLE);
		                    //postStatusUpdateButton.setVisibility(View.VISIBLE);
		                    postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	publishFeedDialog();
		                        }
		                    });
		                    sendFriendsButton= (Button) findViewById(R.id.sendFriendsButton); 
		                    sendFriendsButton.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	sendFriendsDialog();
		                        }
		                    });
		                    graphApiUserButton= (Button) findViewById(R.id.graphApiUser); 
		                    graphApiUserButton.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	graphApiUserDialog();
		                        }
		                    });
		                    postGraphApiDialog= (Button) findViewById(R.id.postGraphApi); 
		                    postGraphApiDialog.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	postGraphApiDialog();
		                        }
		                    });
		                    userInfoTextView = (TextView) findViewById(R.id.userInfoTextView);
		                    userInfoTextView.setVisibility(View.INVISIBLE);
		                    postPhoto= (Button) findViewById(R.id.postPhoto); 
		                    postPhoto.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	postPhotoDialog();
		                        }
		                    });
		                    goToHtmlButton= (Button) findViewById(R.id.goToHtml); 
		                    goToHtmlButton.setOnClickListener(new View.OnClickListener() {
		                        public void onClick(View view) {
		                        	goToHtmlDialog();
		                        }
		                    });
	                    }else{
	                    	setContentView(R.layout.activity_ko);  
	                    }
		            }   
		        }); 
		        Request.executeBatchAsync(request);
	        } else {
	        	session.requestNewPublishPermissions(new Session.NewPermissionsRequest(MainActivity.this, "publish_actions"));
	        }

	    } else if (session.isClosed()) {
	        Log.e(TAG, "Logged out...");
	    }
    }

    
    private void publishFeedDialog() {
    	//ESTE FORMATO SOLO SE PERMITE CUANDO SE HAN ACEPTADO LOS PERMISOS DE LA APP!!
    	Bundle params = new Bundle();
        params.putString("name", "Facebook SDK for Android");
        params.putString("caption", "Build great social apps and get more installs.");
        params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
        WebDialog feedDialog = (
            new WebDialog.FeedDialogBuilder(this,
                Session.getActiveSession(),
                params))
            .setOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(Bundle values,
                    FacebookException error) {
                    if (error == null) {
                        // When the story is posted, echo the success
                        // and the post Id.
                        final String postId = values.getString("post_id");
                        if (postId != null) {
                            Toast.makeText(getApplicationContext(),
                                "Posted story, id: "+postId,
                                Toast.LENGTH_SHORT).show();
                        } else {
                            // User clicked the Cancel button
                            Toast.makeText(getApplicationContext(), 
                                "Publish cancelled", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else if (error instanceof FacebookOperationCanceledException) {
                        // User clicked the "x" button
                        Toast.makeText(getApplicationContext(), 
                            "Publish cancelled", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        // Generic, ex: network error
                        Toast.makeText(getApplicationContext(), 
                            "Error posting story", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

            }).build();
        feedDialog.show();
    }
    
    private void publishShareDialog() {
    	//ESTE FORMATO SE PERMITE AUNQUE NO SE HAYAN INSTALADO LOS PERMISOS DE LA APP
    	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
        .setName("Facebook SDK for Android")
        .setCaption("Build great social apps and get more installs.")
        .setPicture("https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png")
        .setDescription("The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.")
        .setLink("https://developers.facebook.com/android")
        .build();
    	uiHelper.trackPendingDialogCall(shareDialog.present());
    }
    
    
    private void sendFriendsDialog(){
      	 Bundle params = new Bundle();
 	    params.putString("message", "Learn how to make your Android apps social");
 	    params.putString("data",
 		        "{\"badge_of_awesomeness\":\"1\"," +
 		        "\"social_karma\":\"5\"}");
 	    WebDialog requestsDialog = (
 	        new WebDialog.RequestsDialogBuilder(this,
 	            Session.getActiveSession(),
 	            params))
 	            .setOnCompleteListener(new OnCompleteListener() {

 	                @Override
 	                public void onComplete(Bundle values,
 	                    FacebookException error) {
 	                    if (error != null) {
 	                        if (error instanceof FacebookOperationCanceledException) {
 	                            Toast.makeText(getApplicationContext().getApplicationContext(), 
 	                                "Request cancelled", 
 	                                Toast.LENGTH_SHORT).show();
 	                        } else {
 	                            Toast.makeText(getApplicationContext().getApplicationContext(), 
 	                                "Network Error", 
 	                                Toast.LENGTH_SHORT).show();
 	                        }
 	                    } else {
 	                        final String requestId = values.getString("request");
 	                        if (requestId != null) {
 	                            Toast.makeText(getApplicationContext().getApplicationContext(), 
 	                                "Request sent",  
 	                                Toast.LENGTH_SHORT).show();
 	                        } else {
 	                            Toast.makeText(getApplicationContext().getApplicationContext(), 
 	                                "Request cancelled", 
 	                                Toast.LENGTH_SHORT).show();
 	                        }
 	                    }   
 	                }

 	            })
 	            .build();
 	    requestsDialog.show();
    }
    
    private void graphApiUserDialog(){
    	//LLAMADA A LA GRAPH API WITH FQL!!
        String fqlQuery = "SELECT uid, name, pic_square FROM user WHERE uid IN " +
                "(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 25)";
          Bundle params = new Bundle();
          params.putString("q", fqlQuery);
          Session session = Session.getActiveSession();
          Request request = new Request(session,
              "/fql",                         
              params,                         
              HttpMethod.GET,                 
              new Request.Callback(){         
                  public void onCompleted(Response response) {
                     // Log.i(TAG, "Result: " + response.toString());
                      JSONObject data = response.getGraphObject().getInnerJSONObject();
                     // Log.i(TAG, "Result: " + data);
                      JSONArray jsonMainNode = data.optJSONArray("data");
                      int lengthJsonArr = jsonMainNode.length();  
                      try {
	                      for(int i=0; i < lengthJsonArr; i++) 
	                      {
	                         JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
	                          String user_id = jsonChildNode.optString("uid").toString();
	                          String user_name = jsonChildNode.optString("name").toString();
	                          userInfoTextView.append( user_name +" => " + user_id +" \n\n ");
	                          Log.i("JSON parse", user_name +" => " + user_id );
	                     }
	                     userInfoTextView.setVisibility(View.VISIBLE);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                  }                  
          }); 
          Request.executeBatchAsync(request);        
    }
    
    private void postGraphApiDialog(){
    	//Log.i(TAG,"Aqui estoyyyy");

    	/******** PRIMERA VERSION FUNCIONAL ***************/
    	/* Session session = Session.getActiveSession();
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost("https://graph.facebook.com/me/feed?message=Pruebaaa&access_token="+session.getAccessToken());

         HttpResponse response;
         try {
             response = httpclient.execute(httppost);
              // Log.i("tag",EntityUtils.toString(response.getEntity()));
               Toast.makeText(getApplicationContext(), 
            		   EntityUtils.toString(response.getEntity()), 
                       Toast.LENGTH_SHORT).show();
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }*/
    	/**************************************************/
    	/****** SEGUNDA VERSI�N FUNCIONAL Y M�S GENERAL PARA TODO TIPO DE PUBLICACIONES *********/
    	Session session = Session.getActiveSession();
    	Bundle postParams = new Bundle();
	    postParams.putString("name", "Facebook SDK for Android");
	    postParams.putString("caption", "Build great social apps and get more installs.");
	    postParams.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
	    postParams.putString("link", "https://developers.facebook.com/android");
	    postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
	    Request.Callback callback= new Request.Callback() {
	        public void onCompleted(Response response) {
	            JSONObject graphResponse = response
	                                       .getGraphObject()
	                                       .getInnerJSONObject();
	            String postId = null;
	            try {
	                postId = graphResponse.getString("id");
	            } catch (JSONException e) {
	                //Log.i(TAG, "JSON error "+ e.getMessage());
	            	   Toast.makeText(getApplicationContext(), 
	            			   "JSON error "+ e.getMessage(), 
	                           Toast.LENGTH_SHORT).show();
	            }
	            FacebookRequestError error = response.getError();
	            if (error != null) {
	                Toast.makeText(getApplicationContext()
	                     .getApplicationContext(),
	                     error.getErrorMessage(),
	                     Toast.LENGTH_SHORT).show();
	                } else {
	                    Toast.makeText(getApplicationContext()
	                         .getApplicationContext(), 
	                         postId,
	                         Toast.LENGTH_LONG).show();
	            }
	        }
	    };
	    Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();   
    }
    
    private void postPhotoDialog(){
    	//Log.i(TAG,"here");
    	Session session = Session.getActiveSession();
    	Bundle postParams = new Bundle();
	    postParams.putString("message", "");
	   // postParams.putString("url", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
	    Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.ilustraciones_1);
	  // Log.i("IMAGEN","@drawable/ilustraciones_1.jpg");
	   // postParams.putString("source", "@drawable/ilustraciones_1.jpg");
	    postParams.putParcelable("picture", image);
	    Request.Callback callback= new Request.Callback() {
	        public void onCompleted(Response response) {
	            JSONObject graphResponse = response
	                                       .getGraphObject()
	                                       .getInnerJSONObject();
	            String postId = null;
	            try {
	                postId = graphResponse.getString("id");
	            } catch (JSONException e) {
	                //Log.i(TAG, "JSON error "+ e.getMessage());
	            	   Toast.makeText(getApplicationContext(), 
	            			   "JSON error "+ e.getMessage(), 
	                           Toast.LENGTH_SHORT).show();
	            }
	            FacebookRequestError error = response.getError();
	            if (error != null) {
	                Toast.makeText(getApplicationContext()
	                     .getApplicationContext(),
	                     error.getErrorMessage(),
	                     Toast.LENGTH_SHORT).show();
	                } else {
	                    Toast.makeText(getApplicationContext()
	                         .getApplicationContext(), 
	                         postId,
	                         Toast.LENGTH_LONG).show();
	            }
	        }
	    };
	    Request request = new Request(session, "me/photos", postParams, HttpMethod.POST, callback);
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();   
    }
    
    
    private void goToHtmlDialog(){
    	//TODO	
    	AssetManager assetManager = getAssets();
    	 InputStream input;
         try {
             input = assetManager.open("www/prueba.html");
  
              int size = input.available();
              byte[] buffer = new byte[size];
              input.read(buffer);
              input.close();
  
              // byte buffer into a string
              String text = new String(buffer);
          
              Log.i("texto",text.toString());
              TextView newTxt = (TextView) findViewById(R.id.userInfoTextView);
          		newTxt.setText(Html.fromHtml(text));
             // Spanned marked_up = Html.fromHtml(text);
             //newTxt.setText(marked_up.toString(),BufferType.SPANNABLE);
             // newTxt.setText( Html.toHtml(new SpannableString(text)));
          		userInfoTextView.setVisibility(View.VISIBLE);
          		
          		newTxt.setMovementMethod(LinkMovementMethod.getInstance());
          		newTxt.setLinksClickable(true);
          		WebView webview = new WebView(this);
          		setContentView(webview);
          		webview.getSettings().setJavaScriptEnabled(true);
          		webview.loadDataWithBaseURL("file:///android_asset/www/prueba.html", text, "text/html", "UTF-8", null);
          	
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
  
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //printHashKey();
        setContentView(R.layout.activity_main);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);  
        loginButton = (LoginButton) findViewById(R.id.authButton);
        loginButton.setReadPermissions(PERMISSION);
        shareGeneralButton = (Button) this.findViewById(R.id.shareGeneralButton); 
        shareGeneralButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	publishShareDialog();
            }
        });
        Uri intentUri =  getIntent().getData();
        if (intentUri != null) {
            String requestIdParam = intentUri.getQueryParameter("request_ids");
            if (requestIdParam != null) {
                String array[] = requestIdParam.split(",");
                requestId = array[0];
                Log.i(TAG, "Request id: "+requestId);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public View onCreateView(LayoutInflater inflater, 
            ViewGroup container, 
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.menu.main, container, false);
        return view;
    }
    
    
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // uiHelper.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }
    

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public void printHashKey() {
    	  try {
    	        PackageInfo info = getPackageManager().getPackageInfo(
    	                "com.example.appfbprueba", 
    	                PackageManager.GET_SIGNATURES);
    	        for (Signature signature : info.signatures) {
    	            MessageDigest md = MessageDigest.getInstance("SHA");
    	            md.update(signature.toByteArray());
    	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
    	            }
    	    } catch (NameNotFoundException e) {
    	    	   Log.e("TEMPTAGHASH KEY:",  new StringBuilder("no_").append(e.getMessage()).toString());

    	    } catch (NoSuchAlgorithmException e) {
    	    	  Log.e("TEMPTAGHASH KEY:", new StringBuilder("peor_").append(e.getMessage()).toString());
    	    }

    }
    
}
