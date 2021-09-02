package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jsquaredstudios.journalapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journalList;

    /* Constructor */
    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl;

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        holder.name.setText(journal.getUserName());


        /* Using Picasso to display Image */
        imageUrl = journal.getImageUrl();
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_three)
                .into(holder.image);

        /* Date Added (Making it so says something like "3 minutes ago" */
        //Source: https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000); //Turning into Milliseconds
        holder.dateAdded.setText(timeAgo);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, thoughts, dateAdded, name;
        public ImageView image;
        public ImageButton shareButton;
        String userId;
        String username;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.TV_journalTitleList);
            thoughts = itemView.findViewById(R.id.TV_journalThoughtList);
            dateAdded = itemView.findViewById(R.id.TV_journalTimeStampList);
            image = itemView.findViewById(R.id.IV_journalImageList);
            name = itemView.findViewById(R.id.TV_journalUsernameList);
            shareButton = itemView.findViewById(R.id.BTN_journalShareButtonRow);

            /* Share Button */
            shareButton.setOnClickListener(v -> {
                //context.startActivity(); Setting up share button
            });

        }
    }
}
