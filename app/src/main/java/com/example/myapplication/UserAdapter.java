package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    private final Context context; // Context for accessing application resources
    private final ArrayList<UserData> arrayList; // List to hold the user data
    private final OnEditButtonClickListener onEditButtonClickListener; // Listener for edit button clicks
    private final OnRemoveButtonClickListener removeButtonClickListener; // Listener for remove button clicks

    // Constructor for initializing the adapter with context, data list, and listeners
    public UserAdapter(Context context, ArrayList<UserData> arrayList,
                       OnEditButtonClickListener listener, OnRemoveButtonClickListener removeListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.onEditButtonClickListener = listener;
        this.removeButtonClickListener = removeListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the user card layout to create view for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new MyHolder(view); // Return a new holder for the view
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get the user data at the current position
        UserData user = arrayList.get(position);

        // Set the user's first name, last name, and email in the corresponding TextViews
        holder.firstName.setText(user.getFirstName());
        holder.lastName.setText(user.getLastName());
        holder.email.setText(user.getEmail());

        // Load the user's avatar using Picasso library or use a placeholder if the avatar is not available
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Picasso.get().load(user.getAvatar()).placeholder(R.drawable.anonymous).into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.anonymous); // Set default avatar if no avatar is provided
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the data list
        return arrayList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        TextView firstName, lastName, email; // TextViews for displaying user details
        ImageView avatar; // ImageView for displaying user's avatar

        public MyHolder(View itemView) {
            super(itemView);

            // Initialize the TextViews and ImageView from the item layout
            firstName = itemView.findViewById(R.id.first_name);
            lastName = itemView.findViewById(R.id.last_name);
            email = itemView.findViewById(R.id.email);
            avatar = itemView.findViewById(R.id.avatar);

            // Set click listener for the Edit button
            itemView.findViewById(R.id.BTN_Edit).setOnClickListener(v -> {
                int position = getAdapterPosition(); // Get the adapter position of the item clicked
                if (onEditButtonClickListener != null && position != RecyclerView.NO_POSITION) {
                    // Trigger the listener callback with the selected user data
                    onEditButtonClickListener.onEditButtonClick(arrayList.get(position));
                }
            });

            // Set click listener for the Remove button
            itemView.findViewById(R.id.BTN_Remove).setOnClickListener(v -> {
                int position = getAdapterPosition(); // Get the adapter position of the item clicked
                if (removeButtonClickListener != null && position != RecyclerView.NO_POSITION) {
                    // Trigger the listener callback to remove the selected user
                    removeButtonClickListener.onRemoveButtonClick(arrayList.get(position));
                }
            });
        }
    }

    // Interface to handle Edit button clicks
    public interface OnEditButtonClickListener {
        void onEditButtonClick(UserData userData);
    }

    // Interface to handle Remove button clicks
    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(UserData userData);
    }

    // Method to update the data list and refresh the RecyclerView
    public void updateList(ArrayList<UserData> newList) {
        arrayList.clear(); // Clear the existing data
        arrayList.addAll(newList); // Add the new data
        notifyDataSetChanged(); // Notify the adapter of the data change
    }
}
