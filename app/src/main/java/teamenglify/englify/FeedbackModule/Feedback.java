package teamenglify.englify.FeedbackModule;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Feedback#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Feedback extends Fragment {
    private ProgressDialog progressDialog;
    public static boolean sendResult = true;



    public Feedback() {
        // Required empty public constructor
    }

    public static Feedback newInstance(String param1, String param2) {
        Feedback fragment = new Feedback();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);
        final EditText name = (EditText) v.findViewById(R.id.nameEditText);
        final EditText email = (EditText) v.findViewById(R.id.emailEditText);
        final EditText feedback = (EditText) v.findViewById(R.id.feedEditText);

        Button sendBtn = (Button)v.findViewById(R.id.sendFeedback);
        MainActivity.getMainActivity().getSupportActionBar().setTitle("Feedback");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().length()==0){
                    Toast.makeText(getContext(), "Please Enter Your Name", Toast.LENGTH_SHORT).show();
                } else if(email.getText().toString().length()==0){
                    Toast.makeText(getContext(), "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                } else if(feedback.getText().toString().length()==0){
                    Toast.makeText(getContext(), "Please Enter Your Feedback", Toast.LENGTH_SHORT).show();
//                } else if (){
//                    Toast.makeText(getContext(), "Please Enter Your Feedback", Toast.LENGTH_SHORT).show();
                } else {
                    new AsyncSendMail(MainActivity.getMainActivity(),name.getText().toString(), email.getText().toString(), feedback.getText().toString()).execute();
                }
                if(sendResult==true){
                    name.setText("");
                    email.setText("");
                    feedback.setText("");
                }
            }
        });

        return v;
    }

    private class AsyncSendMail extends AsyncTask<Void,Void,Void> {
        private MainActivity context;
        private String name;
        private String emailOrPhone;
        private String body;


        public AsyncSendMail(MainActivity mainActivity, String name, String emailOrPhone, String body) {
            context = mainActivity;
            progressDialog = new ProgressDialog(context);
            this.name = name;
            this.emailOrPhone = emailOrPhone;
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Sending Your Feedback");
            progressDialog.setMax(10);
            progressDialog.setProgress(0);
            progressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                GMailSender sender = new GMailSender("englifyfeedbackmy@gmail.com", "ilovejava");
                sender.sendMail("Feedback",
                        "Name:" + name + "\n" + "Email Or Phone:" + emailOrPhone + "\n" + "Feedback:" + body,
                        "englifyfeedbackmy@gmail.com",
                        "englifyteam@gmail.com");
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if(sendResult==true) {
                Toast.makeText(getContext(), "Thanks for Your Feedback! Your Feedback Is Sent Successfully.", Toast.LENGTH_SHORT).show();

            }
            else {
                Toast.makeText(getContext(), "Error! Your Feedback Cannot Be Sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
