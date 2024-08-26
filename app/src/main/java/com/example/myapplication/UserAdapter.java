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
    private Context context;
    private ArrayList<UserData> arrayList;
    private OnSelectButtonClickListener onSelectButtonClickListener;

    public UserAdapter(Context context, ArrayList<UserData> arrayList, OnSelectButtonClickListener listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.onSelectButtonClickListener = listener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        UserData userData = arrayList.get(position);
        holder.firstName.setText(userData.getFirstName());
        holder.lastName.setText(userData.getLastName());
        holder.email.setText(userData.getEmail());

        String imageURL = userData.getAvatar();
        if (imageURL != null && !imageURL.isEmpty()) {
            Picasso.get()
                    .load(imageURL)
                    .placeholder(R.drawable.anonymous) // Placeholder image while loading
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.anonymous);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView firstName, lastName, email;
        ImageView avatar;

        public MyHolder(View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.first_name);
            lastName = itemView.findViewById(R.id.last_name);
            email = itemView.findViewById(R.id.email);
            avatar = itemView.findViewById(R.id.avatar);

            itemView.findViewById(R.id.BTN_Return).setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onSelectButtonClickListener != null && position != RecyclerView.NO_POSITION) {
                    onSelectButtonClickListener.onSelectButtonClick(arrayList.get(position));
                }
            });
        }
    }

    public interface OnSelectButtonClickListener {
        void onSelectButtonClick(UserData userData);
    }
    public void updateList(ArrayList<UserData> newList) {
        arrayList.clear();
        arrayList.addAll(newList);
    }
}
