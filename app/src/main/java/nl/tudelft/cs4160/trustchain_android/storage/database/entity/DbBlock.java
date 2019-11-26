package nl.tudelft.cs4160.trustchain_android.storage.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Date;

@Entity(tableName = "block", primaryKeys = {"public_key", "sequence_number"})
public class DbBlock {
    @ColumnInfo(name = "tx", typeAffinity = ColumnInfo.BLOB)
    public byte[] tx;

    @ColumnInfo(name = "tx_format")
    public String txFormat;

    @ColumnInfo(name = "public_key")
    @NonNull
    public String publicKey;

    @ColumnInfo(name = "sequence_number")
    @NonNull
    public int sequenceNumber;

    @ColumnInfo(name = "link_public_key")
    public String linkPublicKey;

    @ColumnInfo(name = "link_sequence_number")
    public int linkSequenceNumber;

    @ColumnInfo(name = "link_previous_hash")
    public String previousHash;

    @ColumnInfo(name = "signature")
    public String signature;

    @ColumnInfo(name = "insert_time")
    public Date insertTime;

    @ColumnInfo(name = "block_hash")
    public String blockHash;
}
