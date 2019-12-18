package nl.tudelft.cs4160.trustchain_android.ui.torrent;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.frostwire.jlibtorrent.FileStorage;

import java.io.File;

import nl.tudelft.cs4160.trustchain_android.R;

public class TorrentFilesAdapter extends RecyclerView.Adapter<TorrentFilesAdapter.ViewHolder> {
    private FileStorage fileStorage;

    private OnItemClickListener onItemClickListener;

    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_torrent_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.filename.setText(fileStorage.fileName(position));
        holder.filesize.setText((fileStorage.fileSize(position)/1000) + " kB");
        holder.itemView.setOnClickListener((view) -> {
            String path = fileStorage.filePath(position);
            onItemClickListener.onItemClick(path);
        });
    }

    @Override
    public int getItemCount() {
        return (fileStorage != null) ? fileStorage.numFiles() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView filename;
        TextView filesize;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.file_name);
            filesize = itemView.findViewById(R.id.file_size);
        }
    }

    interface OnItemClickListener {
        void onItemClick(String path);
    }
}
