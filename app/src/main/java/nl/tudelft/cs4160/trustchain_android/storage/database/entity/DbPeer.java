package nl.tudelft.cs4160.trustchain_android.storage.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * The peer that should be accessible through inbox. The peer can be added to inbox either manually,
 * or automatically when we receive a block from the given peer.
 */
@Entity(tableName = "peer")
public class DbPeer {
    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "port")
    public int port;

    @PrimaryKey
    @ColumnInfo(name = "public_key")
    @NonNull
    public String publicKey;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "connection_type")
    public int connectionType;

    @ColumnInfo(name = "last_sent_time")
    @Nullable
    public Date lastSentTime;

    @ColumnInfo(name = "last_received_time")
    @Nullable
    public Date lastReceivedTime;

    @ColumnInfo(name = "creation_time")
    public Date creationTime;
}
