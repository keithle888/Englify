package teamenglify.englify.Settings;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

public class DeleteGrade extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RootListing mRootListing;

    public DeleteGrade() {
        // Required empty public constructor
    }

    public static DeleteGrade newInstance() {
        DeleteGrade fragment = new DeleteGrade();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete_grade, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.delete_grade_recycler_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mainActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
        //specify adapter
        mAdapter = new DeleteGradeAdapter(mRootListing);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class DeleteGradeAdapter extends RecyclerView.Adapter<DeleteGradeAdapter.DeleteGradeViewHolder> {
        private RootListing rootListing;

        public DeleteGradeAdapter(RootListing rootListing) {
            //filter and remove grades that are not downloaded
            RootListing newListing = new RootListing(new ArrayList<Grade>());
            for (int i = 0; i < rootListing.grades.size(); i++) {
                Grade grade = rootListing.grades.get(i);
                Log.d("Englify", "Class DeleteGrade: Method Constructor DeleteGradeAdapter: Looking at -> " + grade.toString());
                if (grade.lessons.size() != 0) {
                    Log.d("Englify", "Class DeleteGrade: Method Constructor DeleteGradeAdapter: " + grade.name + " has been downloaded.");
                    newListing.grades.add(grade);
                }
            }
            this.rootListing = newListing;
        }

        @Override
        public DeleteGradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_box, parent, false);
            return new DeleteGradeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeleteGradeViewHolder holder, final int position) {
            holder.updateUI(rootListing.grades.get(position).name);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DataManager().deleteGrade(rootListing.grades.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return rootListing.grades.size();
        }

        public class DeleteGradeViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;

            public DeleteGradeViewHolder(View view) {
                super(view);
                mTextView = (TextView) itemView.findViewById(R.id.textUpdate);
            }

            public void updateUI(String text) {mTextView.setText(text);}
        }
    }
}
