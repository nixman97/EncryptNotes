package com.pogacean.victor.notes;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements Filterable, ItemTouchHelperAdapter {
    private final CoordinatorLayout snackLay;
    private final RecyclerView rView;
    private NoteDao notes;
    private List<Note> filtered;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private List<Note> filteredList;

    CustomAdapter(RecyclerView rView, CoordinatorLayout snackLay, Context context, NoteDao data) {
        this.mInflater = LayoutInflater.from(context);
        this.notes = data;
        this.snackLay = snackLay;
        this.rView = rView;

    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!notes.getFromPosition(position).isHidden()) {
            holder.titleTextView.setVisibility(View.VISIBLE);
            holder.bodyTextView.setVisibility(View.VISIBLE);
            holder.line.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(notes.getFromPosition(position).decrypt().getTitle());
            holder.bodyTextView.setText(notes.getFromPosition(position).decrypt().getBody());
            if (notes.getFromPosition(position).decrypt().getTitle().equals(""))
                holder.titleTextView.setText(R.string.Untitled);
            if (notes.getFromPosition(position).decrypt().getBody().equals(""))
                holder.bodyTextView.setText(R.string.emptyBody);
        } else {
            holder.titleTextView.setVisibility(View.GONE);
            holder.bodyTextView.setVisibility(View.GONE);
            holder.line.setVisibility(View.GONE);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (Utils.getInstance().isEnc())
            return notes.getNotes().size() - 1;
        else
            return notes.getNotes().size();
    }
    //change order in sqlite when dragging items
    public void onItemMove(int fromPosition, int toPosition) {
        if (notes.getHidden().size()==0) {
            Note tmp = notes.getFromPosition(fromPosition);
            notes.delete(notes.getFromPosition(fromPosition));
            tmp.setPosition(toPosition);
            Note tmp0 = notes.getFromPosition(toPosition);
            notes.delete(notes.getFromPosition(toPosition));
            tmp0.setPosition(fromPosition);
            notes.insert(tmp);
            notes.insert(tmp0);
        }

        notifyItemMoved(fromPosition, toPosition);
    }
    //on swipe item will be set as backup and restored if undo is hit
    public void onItemDismiss(final int position) {
        final Note backup = notes.getFromPosition(position);
        notes.delete(notes.getFromPosition(position));
        notes.decreasePositions(position);
        notifyItemRemoved(position);
        rView.getRecycledViewPool().clear();
        Snackbar snackbar = Snackbar
                .make(snackLay, "Note deleted", Snackbar.LENGTH_LONG).setActionTextColor(Color.WHITE)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notes.increasePositions(position - 1);
                        notes.insert(backup);
                        rView.getRecycledViewPool().clear();

                        notifyItemInserted(position);
                        rView.getRecycledViewPool().clear();

                        notifyItemChanged(position);
                    }
                });

        snackbar.show();
        snackbar.show();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                notes.showAll();
                for(int i=0;i<notes.getNotes().size();i++) {
                    if (!notes.getFromPosition(i).decrypt().getBody().contains(charString)&&
                    !notes.getFromPosition(i).decrypt().getTitle().contains(charString)) {
                        notes.setHidden(notes.getFromPosition(i).getPosition());
                    }
                }
                //notes.search(charString);

                FilterResults filterResults = new FilterResults();
                filterResults.values = filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // refresh the list with filtered data
                rView.getRecycledViewPool().clear();
                notifyDataSetChanged();
            }
        }

                ;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView;
        TextView bodyTextView;
        View line;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textView);
            bodyTextView = itemView.findViewById(R.id.textView2);
            line = itemView.findViewById(R.id.line);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(getAdapterPosition());
        }


    }

    // convenience method for getting data at click position

    Note getItem(int id) {
        return notes.getFromPosition(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position);

    }
}