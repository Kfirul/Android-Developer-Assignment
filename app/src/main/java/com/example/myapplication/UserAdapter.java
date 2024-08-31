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

    private final Context context;
    private final ArrayList<UserData> arrayList;
    private final OnEditButtonClickListener onEditButtonClickListener;
    private final OnRemoveButtonClickListener removeButtonClickListener;

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
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        UserData user = arrayList.get(position);
        holder.firstName.setText(user.getFirstName());
        holder.lastName.setText(user.getLastName());
        holder.email.setText(user.getEmail());

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Picasso.get().load(user.getAvatar()).placeholder(R.drawable.anonymous).into(holder.avatar);
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

            itemView.findViewById(R.id.BTN_Edit).setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onEditButtonClickListener != null && position != RecyclerView.NO_POSITION) {
                    onEditButtonClickListener.onEditButtonClick(arrayList.get(position));
                }
            });

            itemView.findViewById(R.id.BTN_Remove).setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (removeButtonClickListener != null && position != RecyclerView.NO_POSITION) {
                    removeButtonClickListener.onRemoveButtonClick(arrayList.get(position));
                }
            });
        }
    }

    public interface OnEditButtonClickListener {
        void onEditButtonClick(UserData userData);
    }

    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(UserData userData);
    }

    public void updateList(ArrayList<UserData> newList) {
        arrayList.clear();
        arrayList.addAll(newList);
        notifyDataSetChanged();
    }
}
