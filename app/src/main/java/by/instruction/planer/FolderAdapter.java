package by.instruction.planer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.VH> {

    public static class TreeItem {
        public final FolderStorage.FolderNode node;
        public final int level;

        public TreeItem(FolderStorage.FolderNode node, int level) {
            this.node = node;
            this.level = level;
        }
    }

    private final List<TreeItem> data = new ArrayList<>();
    private OnItemActionListener listener;
    private OnItemClickListener clickListener;

    public interface OnItemActionListener {
        void onItemLongClick(FolderStorage.FolderNode node);
    }

    public interface OnItemClickListener {
        void onItemClick(FolderStorage.FolderNode node);
    }

    public void setOnItemActionListener(OnItemActionListener l) {
        this.listener = l;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }

    public void setData(List<TreeItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public List<FolderStorage.FolderNode> getFlat() {
        List<FolderStorage.FolderNode> flat = new ArrayList<>();
        for (TreeItem item : data) {
            flat.add(item.node);
        }
        return flat;
    }

    public void addNode(FolderStorage.FolderNode node) {
        data.add(new TreeItem(node, computeLevel(node.parentId)));
        notifyDataSetChanged();
    }

    private int computeLevel(String parentId) {
        if (parentId == null) return 0;
        for (TreeItem item : data) {
            if (item.node.id.equals(parentId)) {
                return item.level + 1;
            }
        }
        return 0;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TreeItem item = data.get(position);
        holder.title.setText(item.node.name);
        int paddingStart = (int) (16 * holder.itemView.getResources().getDisplayMetrics().density * item.level);
        holder.itemView.setPadding(paddingStart, holder.itemView.getPaddingTop(), holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(item.node);
            return true;
        });
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(item.node);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.folder_title);
        }
    }
}

