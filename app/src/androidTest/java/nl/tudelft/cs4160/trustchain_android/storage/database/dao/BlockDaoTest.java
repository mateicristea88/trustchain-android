package nl.tudelft.cs4160.trustchain_android.storage.database.dao;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbBlock;

public class BlockDaoTest {
    private AppDatabase db;
    private BlockDao blockDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        blockDao = db.blockDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetBlock() {
        DbBlock dbBlock = new DbBlock();
        dbBlock.publicKey = "abc";
        dbBlock.sequenceNumber = 1;
        dbBlock.tx = new byte[]{1, 2, 3};
        blockDao.insertOrUpdate(dbBlock);
        DbBlock result = blockDao.getBlock("abc", 1);
        Assert.assertArrayEquals(dbBlock.tx, result.tx);
    }

    @Test
    public void updateAndGetBlock() {
        DbBlock dbBlock = new DbBlock();
        dbBlock.publicKey = "abc";
        dbBlock.sequenceNumber = 1;
        dbBlock.tx = new byte[]{1, 2, 3};
        blockDao.insertOrUpdate(dbBlock);
        dbBlock.tx = new byte[]{1, 2, 3, 4};
        blockDao.update(dbBlock);
        DbBlock result = blockDao.getBlock("abc", 1);
        Assert.assertArrayEquals(dbBlock.tx, result.tx);
    }

    @Test
    public void getLinkedBlock() {
        DbBlock dbBlock = new DbBlock();
        dbBlock.publicKey = "abc";
        dbBlock.sequenceNumber = 1;
        dbBlock.linkPublicKey = "def";
        dbBlock.linkSequenceNumber = 2;
        dbBlock.blockHash = "h1";
        blockDao.insertOrUpdate(dbBlock);
        DbBlock block1 = blockDao.getLinkedBlock("def", 2, "abc", 1);
        Assert.assertEquals("h1", block1.blockHash);
    }

    @Test
    public void getBlockBefore() {
        DbBlock block1 = new DbBlock();
        block1.publicKey = "abc";
        block1.sequenceNumber = 1;
        block1.blockHash = "h1";
        blockDao.insertOrUpdate(block1);

        DbBlock block2 = new DbBlock();
        block2.publicKey = "abc";
        block2.sequenceNumber = 2;
        block2.blockHash = "h2";
        blockDao.insertOrUpdate(block2);

        DbBlock before2 = blockDao.getBlockBefore("abc", 2);
        DbBlock before1 = blockDao.getBlockBefore("abc", 1);
        Assert.assertEquals(1, before2.sequenceNumber);
        Assert.assertNull(before1);
    }

    @Test
    public void getBlockAfter() {
        DbBlock block1 = new DbBlock();
        block1.publicKey = "abc";
        block1.sequenceNumber = 1;
        block1.blockHash = "h1";
        blockDao.insertOrUpdate(block1);

        DbBlock block2 = new DbBlock();
        block2.publicKey = "abc";
        block2.sequenceNumber = 2;
        block2.blockHash = "h2";
        blockDao.insertOrUpdate(block2);

        DbBlock after2 = blockDao.getBlockAfter("abc", 2);
        DbBlock after1 = blockDao.getBlockAfter("abc", 1);
        Assert.assertEquals(2, after1.sequenceNumber);
        Assert.assertNull(after2);
    }
}
